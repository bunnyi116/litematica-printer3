package me.aleksilassila.litematica.printer.guide;

import fi.dy.masa.litematica.world.WorldSchematic;
import me.aleksilassila.litematica.printer.config.Configs;
import me.aleksilassila.litematica.printer.enums.BlockMatchResult;
import me.aleksilassila.litematica.printer.printer.SchematicBlockContext;
import me.aleksilassila.litematica.printer.printer.action.Action;
import me.aleksilassila.litematica.printer.utils.minecraft.BlockStateUtils;

import me.aleksilassila.litematica.printer.utils.InteractionUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.IceBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public abstract class Guide extends BlockStateUtils {
    protected final SchematicBlockContext context;
    public final Minecraft client;
    public final ClientLevel level;
    public final WorldSchematic schematic;
    public final BlockPos blockPos;
    public final BlockState currentState;
    public final BlockState requiredState;
    protected final Block currentBlock;
    protected final Block requiredBlock;

    // -------------------------------------------------------
    // 通用朝向 / 旋转属性（从 requiredState 自动提取）
    // -------------------------------------------------------

    /**
     * 朝向：FACING / HORIZONTAL_FACING / VERTICAL_DIRECTION / FACING_HOPPER，取第一个命中的
     */
    protected final @Nullable Direction facing;

    /**
     * 轴向：AXIS（原木/锁链/音符盒等）
     */
    protected final Direction.@Nullable Axis axis;

    /**
     * 水平轴：HORIZONTAL_AXIS
     */
    protected final Direction.@Nullable Axis horizontalAxis;

    // -------------------------------------------------------
    // 分层属性
    // -------------------------------------------------------

    /**
     * 楼梯/活板门的上/下半：HALF
     */
    protected final @Nullable Half half;

    // -------------------------------------------------------
    // 附着面属性
    // -------------------------------------------------------

    /**
     * 附着面（按钮/拉杆/火把）：ATTACH_FACE
     */
    protected final @Nullable AttachFace attachFace;

    public Guide(SchematicBlockContext context) {
        this.context = context;
        this.client = context.client;
        this.level = context.level;
        this.schematic = context.schematic;
        this.blockPos = context.blockPos;
        this.currentBlock = context.currentState.getBlock();
        this.requiredBlock = context.requiredState.getBlock();
        this.currentState = context.currentState;
        this.requiredState = context.requiredState;

        // 朝向 / 旋转
        this.facing = getProperty(requiredState, BlockStateProperties.FACING)
                .or(() -> getProperty(requiredState, BlockStateProperties.HORIZONTAL_FACING))
                .or(() -> getProperty(requiredState, BlockStateProperties.VERTICAL_DIRECTION))
                .or(() -> getProperty(requiredState, BlockStateProperties.FACING_HOPPER))
                .orElse(null);
        this.axis = getProperty(requiredState, BlockStateProperties.AXIS).orElse(null);
        this.horizontalAxis = getProperty(requiredState, BlockStateProperties.HORIZONTAL_AXIS).orElse(null);

        // 分层
        this.half = getProperty(requiredState, BlockStateProperties.HALF).orElse(null);

        // 附着面
        this.attachFace = getProperty(requiredState, BlockStateProperties.ATTACH_FACE).orElse(null);
    }

    public boolean canExecute(AtomicReference<Boolean> skipOtherGuide) {
        return true;
    }

    /**
     * 构建 Action 的入口方法。
     * <p>
     * 先做全局前置检查（含水跳过、破冰放水、canSurvive），然后分发给子类钩子。
     *
     * @return Optional.empty() 表示此 Guide 不处理，交给下一个
     */
    public final Optional<Action> buildAction(BlockMatchResult state, AtomicReference<Boolean> skipOtherGuide) {
        // 前置检查：CORRECT 直接返回
        if (state == BlockMatchResult.CORRECT) {
            return this.onBuildActionCorrect(state, skipOtherGuide);
        }

        // 液体方块（水/熔岩）无法通过放置物品来处理，直接跳过
        if (requiredBlock instanceof LiquidBlock) {
            return Optional.empty();
        }

        // 破冰放水：必须在 SKIP_WATERLOGGED_BLOCK 检查之前，
        // 否则含水方块会被提前拦截
        Optional<Action> iceAction = handleIceForWater();
        if (iceAction.isPresent()) {
            return iceAction;
        }

        // 方块无法在此位置自然存活，跳过
        if (!requiredState.canSurvive(level, blockPos)) {
            return Optional.empty();
        }

        // 水生植物（海草等）需要水环境才能放置
        if (BlockStateUtils.requiresWaterToPlace(requiredBlock)) {
            BlockPos waterPos = requiredState.hasProperty(BlockStateProperties.WATERLOGGED)
                    ? blockPos : blockPos.above();
            if (!level.getBlockState(waterPos).is(Blocks.WATER)) {
                return Optional.empty();
            }
        }

        // 含水方块跳过（破冰放水模式下由 handleIceForWater 处理，此处不干预）
        if (Configs.Print.SKIP_WATERLOGGED_BLOCK.getBooleanValue()
                && BlockStateUtils.isWaterBlock(requiredState)
                && !Configs.Print.PRINT_ICE_FOR_WATER.getBooleanValue()) {
            return Optional.empty();
        }

        // 交给子类的 onBuildAction 拦截钩子
        Optional<Action> result = this.onBuildAction(state, skipOtherGuide);
        if (result.isPresent()) {
            return result;
        }

        // 分状态分发
        return switch (state) {
            case MISSING -> this.onBuildActionMissingBlock(state, skipOtherGuide);
            case WRONG_BLOCK -> this.onBuildActionWrongBlock(state, skipOtherGuide);
            case WRONG_STATE -> this.onBuildActionWrongState(state, skipOtherGuide);
            default -> Optional.empty();
        };
    }

    // -------------------------------------------------------
    // 全局前置处理
    // -------------------------------------------------------

    /**
     * 处理「破冰放水」逻辑（仅在启用 PRINT_ICE_FOR_WATER 且生存模式下生效）。
     *
     * <p>目标方块为含水方块（waterlogged=true），纯水方块（LiquidBlock）已由上层跳过。
     *
     * <ul>
     *   <li>当前是冰块 → 直接破冰；冰被破坏后 Minecraft 会原地生成水，后续再放置含水方块。</li>
     *   <li>水位不对且当前有实体方块 → 若启用破坏则先破坏，否则跳过。</li>
     *   <li>水位不对且当前位置为空或液体 → 放置冰块；冰融化后产生水。</li>
     * </ul>
     */
    private Optional<Action> handleIceForWater() {
        if (!Configs.Print.PRINT_ICE_FOR_WATER.getBooleanValue() || !BlockStateUtils.isWaterBlock(requiredState)) {
            return Optional.empty();
        }
        if (client.gameMode == null || client.gameMode.getPlayerMode().isCreative()) {
            return Optional.empty();
        }
        if (currentBlock instanceof IceBlock) {
            InteractionUtils.INSTANCE.add(context);
            return Optional.empty();
        }
        if (!BlockStateUtils.isCorrectWaterLevel(requiredState, currentState)) {
            if (!currentState.isAir() && !(currentBlock instanceof LiquidBlock)) {
                if (Configs.Print.BREAK_WRONG_BLOCK.getBooleanValue()) {
                    InteractionUtils.INSTANCE.add(context);
                }
                return Optional.empty();
            }
            return Optional.of(new Action().setItem(Items.ICE));
        }
        return Optional.empty();
    }

    // -------------------------------------------------------
    // 子类钩子
    // -------------------------------------------------------

    /**
     * 所有状态均会先经过此钩子，可在此拦截任意状态
     */
    protected Optional<Action> onBuildAction(BlockMatchResult state, AtomicReference<Boolean> skipOtherGuide) {
        return Optional.empty();
    }

    /**
     * 位置为空气 / 可替换方块：需要放置
     */
    protected Optional<Action> onBuildActionMissingBlock(BlockMatchResult state, AtomicReference<Boolean> skipOtherGuide) {
        return Optional.empty();
    }

    /**
     * 方块类型完全不同：需要先破坏再放置
     */
    protected Optional<Action> onBuildActionWrongBlock(BlockMatchResult state, AtomicReference<Boolean> skipOtherGuide) {
        return Optional.empty();
    }

    /**
     * 方块类型相同但状态不对：可能需要交互修正
     */
    protected Optional<Action> onBuildActionWrongState(BlockMatchResult state, AtomicReference<Boolean> skipOtherGuide) {
        return Optional.empty();
    }

    /**
     * 完全正确：通常无需操作
     */
    protected Optional<Action> onBuildActionCorrect(BlockMatchResult state, AtomicReference<Boolean> skipOtherGuide) {
        return Optional.empty();
    }

    protected Optional<Direction> getFacing() {
        return getProperty(requiredState, BlockStateProperties.FACING)
                .or(() -> getProperty(requiredState, BlockStateProperties.HORIZONTAL_FACING))
                .or(() -> getProperty(requiredState, BlockStateProperties.VERTICAL_DIRECTION))
                .or(() -> getProperty(requiredState, BlockStateProperties.FACING_HOPPER));
    }
}
