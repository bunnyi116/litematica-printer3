package me.aleksilassila.litematica.printer.mixin.printer.mc;

import me.aleksilassila.litematica.printer.config.Configs;
import me.aleksilassila.litematica.printer.mixin.MinecraftAccessor;
import me.aleksilassila.litematica.printer.mixin_extension.BlockBreakResult;
import me.aleksilassila.litematica.printer.mixin_extension.MultiPlayerGameModeExtension;
import me.aleksilassila.litematica.printer.utils.ConfigUtils;
import me.aleksilassila.litematica.printer.utils.minecraft.NetworkUtils;
import me.aleksilassila.litematica.printer.utils.mods.ModLoadUtils;
import me.aleksilassila.litematica.printer.utils.mods.TweakerooUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket.Action;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("DataFlowIssue")
@Mixin(value = MultiPlayerGameMode.class, priority = 1020)
public abstract class MixinMultiPlayerGameMode implements MultiPlayerGameModeExtension {
    // @formatter:off
    @Shadow
    private BlockPos destroyBlockPos;
    @Shadow
    private ItemStack destroyingItem;
    @Shadow
    private float destroyProgress;
    @Shadow
    private boolean isDestroying;
    @Shadow
    @Final
    private Minecraft minecraft;
    @Unique
    private float delayedDestroyProgress;
    @Unique
    private BlockPos delayedDestroyPos;
    @Unique
    private boolean hasDelayedDestroy;
    @Unique
    private long delayedDestroyStartTick;

    @Shadow
    public abstract boolean destroyBlock(final BlockPos pos);

    @Shadow
    protected abstract boolean sameDestroyTarget(final BlockPos pos);

    @Shadow
    protected abstract void ensureHasSentCarriedItem();

    //#if MC > 11802
    @Shadow public abstract InteractionResult useItemOn(LocalPlayer player, InteractionHand hand, BlockHitResult blockHitResult);
    //#else
    //$$ @Shadow public abstract InteractionResult useItemOn(LocalPlayer player,ClientLevel level, InteractionHand hand, BlockHitResult blockHitResult);
    //#endif

    // @formatter:on

    @Inject(at = @At("HEAD"), method = "tick")
    public void tick(CallbackInfo ci) {
        if (this.hasDelayedDestroy) {
            LocalPlayer player = this.minecraft.player;
            ClientLevel level = this.minecraft.level;
            if (player == null || level == null) {
                return;
            }

            BlockState blockState = level.getBlockState(this.delayedDestroyPos);
            if (blockState.isAir()) {
                this.hasDelayedDestroy = false;
                return;
            }

            // 基于 tick 计算进度
            long currentTick = getClientTickCount();
            int elapsedTicks = (int) (currentTick - this.delayedDestroyStartTick);
            float perTickProgress = blockState.getDestroyProgress(player, level, this.delayedDestroyPos);
            this.delayedDestroyProgress = Math.min(1.0F, this.delayedDestroyProgress + perTickProgress * elapsedTicks);

            if (this.delayedDestroyProgress >= 1.0F) {
                this.destroyBlock(this.delayedDestroyPos);
                this.hasDelayedDestroy = false;
            }
        }
    }

    @Override
    public InteractionResult litematica_printer$useItemOn(boolean localPrediction, InteractionHand hand, BlockHitResult blockHit) {
        if (localPrediction) {
            //#if MC > 11802
            return useItemOn(minecraft.player, hand, blockHit);
            //#else
            //$$ return useItemOn(minecraft.player, minecraft.level, hand, blockHit);
            //#endif
        }
        this.ensureHasSentCarriedItem();
        if (!this.minecraft.level.getWorldBorder().isWithinBounds(blockHit.getBlockPos())) {
            return InteractionResult.FAIL;
        }
        //#if MC > 11802
        NetworkUtils.sendPacket(sequence -> new ServerboundUseItemOnPacket(hand, blockHit, sequence));
        //#else
        //$$ NetworkUtils.sendPacket(sequence -> new ServerboundUseItemOnPacket(hand, blockHit));
        //#endif
        return InteractionResult.PASS;
    }


    @Unique
    private int litematica_printer$getDestroyStage() {
        float breakingProgress = this.destroyProgress >= ConfigUtils.getBreakProgressThreshold() ? 1.0F : this.destroyProgress;
        return breakingProgress > 0.0F ? (int) (breakingProgress * 10.0F) : -1;
    }

    @Unique
    private ServerboundPlayerActionPacket litematica_printer$getServerboundPlayerActionPacket(Action action, BlockPos blockPos, Direction direction, int sequence) {
        //#if MC > 11802
        return new ServerboundPlayerActionPacket(action, blockPos, direction, sequence);
        //#else
        //$$ return new ServerboundPlayerActionPacket(action, blockPos, direction);
        //#endif
    }

    @Override
    public BlockBreakResult litematica_printer$continueDestroyBlock(boolean localPrediction, BlockPos blockPos, Direction direction) {
        if (this.hasDelayedDestroy) {
            BlockState blockState = minecraft.level.getBlockState(this.delayedDestroyPos);
            long currentTick = getClientTickCount();
            int elapsedTicks = (int) (currentTick - this.delayedDestroyStartTick);
            float perTickProgress = blockState.getDestroyProgress(minecraft.player, minecraft.level, this.delayedDestroyPos);
            this.delayedDestroyProgress = Math.min(1.0F, this.delayedDestroyProgress + perTickProgress * elapsedTicks);
            if (this.delayedDestroyProgress >= 1.0F) {
                this.destroyBlock(this.delayedDestroyPos);
                this.hasDelayedDestroy = false;
            }
        }
        LocalPlayer player = minecraft.player;
        ClientLevel level = minecraft.level;
        MultiPlayerGameMode gameMode = minecraft.gameMode;
        if (player == null || level == null || gameMode == null) {
            return BlockBreakResult.FAILED;
        }
        if (!level.getWorldBorder().isWithinBounds(blockPos)) {
            return BlockBreakResult.FAILED;
        }

        // 创造模式
        if (player.getAbilities().instabuild) {
            NetworkUtils.sendPacket(sequence -> {
                if (localPrediction) {
                    destroyBlock(blockPos);
                }
                return litematica_printer$getServerboundPlayerActionPacket(Action.START_DESTROY_BLOCK, blockPos, direction, sequence);
            });
            return BlockBreakResult.COMPLETED;
        }

        // Tweakeroo 工具切换
        if (ModLoadUtils.isTweakerooLoaded()) {
            if (TweakerooUtils.isToolSwitchEnabled()) {
                TweakerooUtils.trySwitchToEffectiveTool(blockPos);
            }
        } else {
            ensureHasSentCarriedItem();
        }

        boolean useDelayedDestroy = Configs.Break.BREAK_USE_DELAYED_DESTROY.getBooleanValue();
        BlockState blockState = level.getBlockState(blockPos);

        // 方块已是空气
        if (blockState.isAir()) {
            if (this.hasDelayedDestroy && blockPos.equals(this.delayedDestroyPos)) {
                this.hasDelayedDestroy = false;
            }
            if (this.isDestroying && this.sameDestroyTarget(blockPos)) {
                this.isDestroying = false;
            }
            return BlockBreakResult.COMPLETED;
        }

        // 延迟破坏中
        if (this.hasDelayedDestroy) {
            if (blockPos.equals(this.delayedDestroyPos)) {
                return BlockBreakResult.IN_PROGRESS;
            } else {
                // 切换目标，停止延迟破坏
                NetworkUtils.sendPacket(sequence ->
                        litematica_printer$getServerboundPlayerActionPacket(Action.STOP_DESTROY_BLOCK, this.delayedDestroyPos, direction, sequence)
                );
                this.hasDelayedDestroy = false;
            }
        }

        // 中断原版破坏
        if (this.isDestroying && !this.sameDestroyTarget(blockPos)) {
            NetworkUtils.sendPacket(
                    litematica_printer$getServerboundPlayerActionPacket(Action.ABORT_DESTROY_BLOCK, this.destroyBlockPos, direction, 0)
            );
            this.isDestroying = false;
            this.destroyProgress = 0.0F;
        }

        // 继续破坏同一目标
        if (this.sameDestroyTarget(blockPos)) {
            this.destroyProgress = this.destroyProgress + blockState.getDestroyProgress(player, level, blockPos);

            if (this.destroyProgress >= ConfigUtils.getBreakProgressThreshold()) {
                this.isDestroying = false;
                this.destroyProgress = 0.0F;
                NetworkUtils.sendPacket(sequence -> {
                    if (localPrediction) {
                        destroyBlock(blockPos);
                    }
                    return litematica_printer$getServerboundPlayerActionPacket(Action.STOP_DESTROY_BLOCK, blockPos, direction, sequence);
                });
                if (localPrediction) {
                    level.destroyBlockProgress(player.getId(), this.destroyBlockPos, this.litematica_printer$getDestroyStage());
                }
                return BlockBreakResult.COMPLETED;
            }

            if (useDelayedDestroy) {
                if (this.hasDelayedDestroy) {
                    return BlockBreakResult.IN_PROGRESS;
                }
                this.isDestroying = false;
                this.destroyProgress = 0.0F;
                NetworkUtils.sendPacket(sequence ->
                        litematica_printer$getServerboundPlayerActionPacket(Action.STOP_DESTROY_BLOCK, blockPos, direction, sequence)
                );
                if (localPrediction) {
                    level.destroyBlockProgress(player.getId(), this.destroyBlockPos, this.litematica_printer$getDestroyStage());
                }
            }
            return BlockBreakResult.IN_PROGRESS;

        } else if (!this.isDestroying || !this.sameDestroyTarget(blockPos)) {
            if (this.isDestroying) {
                NetworkUtils.sendPacket(
                        litematica_printer$getServerboundPlayerActionPacket(Action.ABORT_DESTROY_BLOCK, this.destroyBlockPos, direction, 0)
                );
            }
            if (this.destroyProgress == 0.0F) {
                if (localPrediction) {
                    blockState.attack(level, blockPos, player);
                }
            }
            NetworkUtils.sendPacket(sequence ->
                    litematica_printer$getServerboundPlayerActionPacket(Action.START_DESTROY_BLOCK, blockPos, direction, sequence)
            );
            float destroyProgress = blockState.getDestroyProgress(player, level, blockPos);

            // 目标方块可以被瞬间破坏
            if (destroyProgress >= 1.0F) {
                if (localPrediction) {
                    destroyBlock(blockPos);
                }
                return BlockBreakResult.COMPLETED;
            }

            // 目标方块进度初始已经是70%进度及以上，使用STOP提前破坏
            if (!this.hasDelayedDestroy && useDelayedDestroy && destroyProgress > ConfigUtils.getBreakProgressThreshold()) {
                NetworkUtils.sendPacket(sequence -> {
                    if (localPrediction) {
                        this.destroyBlock(blockPos);
                    }
                    return litematica_printer$getServerboundPlayerActionPacket(Action.STOP_DESTROY_BLOCK, blockPos, direction, sequence);
                });
                return BlockBreakResult.COMPLETED;
            }

            this.isDestroying = true;
            this.destroyBlockPos = blockPos;
            this.destroyProgress = destroyProgress;
            this.destroyingItem = player.getMainHandItem();
            if (localPrediction) {
                level.destroyBlockProgress(player.getId(), this.destroyBlockPos, this.litematica_printer$getDestroyStage());
            }

            if (!this.hasDelayedDestroy && useDelayedDestroy) {
                this.hasDelayedDestroy = true;
                this.delayedDestroyPos = this.destroyBlockPos;
                this.delayedDestroyProgress = this.destroyProgress;
                this.delayedDestroyStartTick = getClientTickCount();
                this.isDestroying = false;
                this.destroyProgress = 0.0F;
                NetworkUtils.sendPacket(sequence ->
                        litematica_printer$getServerboundPlayerActionPacket(Action.STOP_DESTROY_BLOCK, this.delayedDestroyPos, direction, sequence)
                );
                return BlockBreakResult.IN_PROGRESS;
            }

            return BlockBreakResult.IN_PROGRESS;
        }

        return BlockBreakResult.FAILED;
    }

    @Unique
    private long getClientTickCount() {
        return ((MinecraftAccessor) Minecraft.getInstance()).getClientTickCount();
    }
}