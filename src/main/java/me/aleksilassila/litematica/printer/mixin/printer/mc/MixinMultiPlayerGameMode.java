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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings({"DataFlowIssue", "DuplicateCondition"})
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
            long currentTick = getClientTickCount();
            int elapsedTicks = (int) (currentTick - this.delayedDestroyStartTick);
            float delayedDestroyProgress = blockState.getDestroyProgress(player, level, this.delayedDestroyPos) * elapsedTicks;
            if (delayedDestroyProgress >= 1.0F) {
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
    private ServerboundPlayerActionPacket getActionPacket(Action action, BlockPos blockPos, Direction direction, int sequence) {
        //#if MC > 11802
        return new ServerboundPlayerActionPacket(action, blockPos, direction, sequence);
        //#else
        //$$ return new ServerboundPlayerActionPacket(action, blockPos, direction);
        //#endif
    }

    @Override
    public BlockBreakResult litematica_printer$continueDestroyBlock(boolean localPrediction, BlockPos blockPos, Direction direction) {
        LocalPlayer player = minecraft.player;
        ClientLevel level = minecraft.level;
        MultiPlayerGameMode gameMode = minecraft.gameMode;
        if (player == null || level == null || gameMode == null) {
            return BlockBreakResult.FAILED;
        }
        BlockState blockState = level.getBlockState(blockPos);
        if (blockState.isAir() || blockState.getBlock() instanceof LiquidBlock) {
            return BlockBreakResult.COMPLETED;
        }
        if (this.hasDelayedDestroy) {
            BlockState blockState2 = minecraft.level.getBlockState(this.delayedDestroyPos);
            long currentTick = getClientTickCount();
            int elapsedTicks = (int) (currentTick - this.delayedDestroyStartTick);
            float delayedDestroyProgress = blockState2.getDestroyProgress(player, level, this.delayedDestroyPos) * elapsedTicks;
            if (delayedDestroyProgress >= 1.0F) {
                this.destroyBlock(this.delayedDestroyPos);
                this.hasDelayedDestroy = false;
            }
        }
        if (!level.getWorldBorder().isWithinBounds(blockPos)) {
            return BlockBreakResult.FAILED;
        }
        if (player.getAbilities().instabuild) {
            NetworkUtils.sendPacket(sequence -> {
                if (localPrediction) {
                    destroyBlock(blockPos);
                }
                return getActionPacket(Action.START_DESTROY_BLOCK, blockPos, direction, sequence);
            });
            return BlockBreakResult.COMPLETED;
        }
        if (ModLoadUtils.isTweakerooLoaded()) {
            if (TweakerooUtils.isToolSwitchEnabled()) {
                TweakerooUtils.trySwitchToEffectiveTool(blockPos);
            }
        } else {
            ensureHasSentCarriedItem();
        }
        boolean useDelayedDestroy = Configs.Break.BREAK_USE_DELAYED_DESTROY.getBooleanValue();
        if (blockState.isAir()) {
            if (this.hasDelayedDestroy && blockPos.equals(this.delayedDestroyPos)) {
                this.hasDelayedDestroy = false;
            }
            if (this.isDestroying && this.sameDestroyTarget(blockPos)) {
                this.isDestroying = false;
            }
            return BlockBreakResult.COMPLETED;
        }
        if (this.hasDelayedDestroy && blockPos.equals(this.delayedDestroyPos)) {
            return isDestroying ? BlockBreakResult.IN_PROGRESS : BlockBreakResult.COMPLETED;
        }
        if (this.isDestroying && !blockPos.equals(this.destroyBlockPos)) {
            NetworkUtils.sendPacket(getActionPacket(Action.ABORT_DESTROY_BLOCK, this.destroyBlockPos, direction, 0));
            this.isDestroying = false;
            this.destroyProgress = 0.0F;
        }
        if (blockPos.equals(this.destroyBlockPos)) {
            this.destroyProgress = this.destroyProgress + blockState.getDestroyProgress(player, level, blockPos);
            if (this.destroyProgress >= ConfigUtils.getBreakProgressThreshold()) {
                NetworkUtils.sendPacket(sequence -> {
                    if (localPrediction) {
                        destroyBlock(blockPos);
                        level.destroyBlockProgress(player.getId(), blockPos, this.litematica_printer$getDestroyStage());
                    }
                    this.isDestroying = false;
                    this.destroyProgress = 0.0F;
                    return getActionPacket(Action.STOP_DESTROY_BLOCK, blockPos, direction, sequence);
                });
                return BlockBreakResult.COMPLETED;
            }
            return BlockBreakResult.IN_PROGRESS;
        }
        if (!this.isDestroying || !blockPos.equals(this.destroyBlockPos)) {
            if (this.isDestroying) {
                NetworkUtils.sendPacket(getActionPacket(Action.ABORT_DESTROY_BLOCK, this.destroyBlockPos, direction, 0));
                this.isDestroying = false;
                this.destroyProgress = 0.0F;
            }
            if (this.destroyProgress == 0.0F) {
                if (localPrediction) {
                    blockState.attack(level, blockPos, player);
                }
            }
            NetworkUtils.sendPacket(sequence -> getActionPacket(Action.START_DESTROY_BLOCK, blockPos, direction, sequence));
            float destroyProgress = blockState.getDestroyProgress(player, level, blockPos);
            if (destroyProgress >= 1.0F) {
                if (localPrediction) {
                    destroyBlock(blockPos);
                }
                return BlockBreakResult.COMPLETED;
            }
            if (useDelayedDestroy) {
                if (destroyProgress >= ConfigUtils.getBreakProgressThreshold()) {
                    NetworkUtils.sendPacket(sequence -> {
                        if (localPrediction) {
                            this.destroyBlock(blockPos);
                            level.destroyBlockProgress(player.getId(), blockPos, this.litematica_printer$getDestroyStage());
                        }
                        this.hasDelayedDestroy = false;
                        return getActionPacket(Action.STOP_DESTROY_BLOCK, blockPos, direction, sequence);
                    });
                    return BlockBreakResult.COMPLETED;
                } else {
                    // 发送STOP让服务端当前处理位置状态转移到延迟破坏位置中
                    NetworkUtils.sendPacket(sequence -> {
                        this.hasDelayedDestroy = true;
                        this.delayedDestroyPos = blockPos;
                        this.delayedDestroyStartTick = getClientTickCount();
                        this.isDestroying = false;
                        this.destroyProgress = 0.0F;
                        return getActionPacket(Action.STOP_DESTROY_BLOCK, blockPos, direction, sequence);
                    });
                    level.destroyBlockProgress(player.getId(), blockPos, this.litematica_printer$getDestroyStage());
                    return this.isDestroying ? BlockBreakResult.IN_PROGRESS : BlockBreakResult.COMPLETED;
                }
            }
            this.isDestroying = true;
            this.destroyBlockPos = blockPos;
            this.destroyProgress = destroyProgress;
            this.destroyingItem = player.getMainHandItem();
            if (localPrediction) {
                level.destroyBlockProgress(player.getId(), blockPos, this.litematica_printer$getDestroyStage());
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