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

    /** 朝向：FACING / HORIZONTAL_FACING / VERTICAL_DIRECTION / FACING_HOPPER，取第一个命中的 */
    protected final @Nullable Direction facing;

    /** 轴向：AXIS（原木/锁链/音符盒等） */
    protected final Direction.@Nullable Axis axis;

    /** 水平轴：HORIZONTAL_AXIS */
    protected final Direction.@Nullable Axis horizontalAxis;

    // -------------------------------------------------------
    // 分层属性
    // -------------------------------------------------------

    /** 楼梯/活板门的上/下半：HALF */
    protected final @Nullable Half half;

    // -------------------------------------------------------
    // 附着面属性
    // -------------------------------------------------------

    /** 附着面（按钮/拉杆/火把）：ATTACH_FACE */
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

        // 前置检查：液体方块（水/熔岩）不可通过放置物品来处理
        // 如果用户没开 SKIP_WATERLOGGED_BLOCK，液体方块会走到 DefaultGuide MISSING，
        // 而 Blocks.WATER.asItem() == Items.AIR，导致一直切空手死循环
        if (requiredBlock instanceof LiquidBlock) {
            return Optional.empty();
        }

        // 前置检查：破冰放水
        // isWaterBlock 匹配的是 waterlogged=true 的方块（含水楼梯、含水活板门等），
        // 不是纯水方块。纯水方块已经被上面的 LiquidBlock 检查跳过了。
        // 必须在 SKIP_WATERLOGGED_BLOCK 之前，否则含水方块会被含水跳过拦截
        Optional<Action> iceAction = handleIceForWater();
        if (iceAction.isPresent()) {
            return iceAction;
        }

        // 破冰放水：水位正确时，当前方块是纯水，但 BlockMatchResult 是 WRONG_BLOCK（水≠楼梯），
        // 如果不做修正，DefaultGuide.onBuildActionWrongBlock 会去破坏水，导致永远无法放置含水方块。
        // 修正：当水位正确且当前是水时，将 WRONG_BLOCK 当作 MISSING 处理（水是可替换的）。
        if (state == BlockMatchResult.WRONG_BLOCK
                && Configs.Print.PRINT_ICE_FOR_WATER.getBooleanValue()
                && BlockStateUtils.isWaterBlock(requiredState)
                && BlockStateUtils.isCorrectWaterLevel(requiredState, currentState)) {
            state = BlockMatchResult.MISSING;
        }

        // 前置检查：方块无法在此位置存活
        if (!requiredState.canSurvive(level, blockPos)) {
            return Optional.empty();
        }

        // 前置检查：水生植物（海草等）需要水中才能放置
        // canSurvive 只检查支撑，不检查水，但实际放置需要水
        // 如果当前位置没有水，跳过避免死循环切换物品
        if (BlockStateUtils.requiresWaterToPlace(requiredBlock)) {
            BlockPos waterPos = requiredState.hasProperty(BlockStateProperties.WATERLOGGED)
                    ? blockPos : blockPos.above();
            if (!level.getBlockState(waterPos).is(Blocks.WATER)) {
                return Optional.empty();
            }
        }

        // 前置检查：含水方块跳过
        // 但如果启用了破冰放水，含水方块由 handleIceForWater 专门处理，不应被跳过
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
     * 处理「破冰放水」逻辑。
     * 匹配的是 isWaterBlock(requiredState)，即 waterlogged=true 的方块（含水楼梯、含水活板门等），
     * 纯水方块（LiquidBlock）已被前置检查跳过，不会到达此处。
     *
     * <p>流程（基于状态判断，不使用冷却）：
     * 1. 当前是冰块 + 周围有水 → 破冰（水会流来）
     * 2. 当前是冰块 + 周围没水 + 亮度≥12 → 跳过（冰会自然融化变水）
     * 3. 当前是冰块 → 破冰（冰被破坏后会产生水，无需等待周围有水）
     * 4. 水位不对且有方块 → 先破坏
     * 5. 水位不对且位置空 → 放置冰块（冰融化/被破坏后变水）
     */
    private Optional<Action> handleIceForWater() {
        if (!Configs.Print.PRINT_ICE_FOR_WATER.getBooleanValue() || !BlockStateUtils.isWaterBlock(requiredState)) {
            return Optional.empty();
        }
        if (client.gameMode == null || client.gameMode.getPlayerMode().isCreative()) {
            return Optional.empty();
        }
        if (currentBlock instanceof IceBlock) {
            // 直接破冰：冰被破坏后会产生水（Minecraft 原版机制），
            // 无需等待周围有水或冰自然融化，避免在亮度不足时永远卡住
            InteractionUtils.INSTANCE.add(context);
            // 冰在位时返回 empty（不执行放置操作），破冰后水流入，走含水方块放置
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

    /** 所有状态均会先经过此钩子，可在此拦截任意状态 */
    protected Optional<Action> onBuildAction(BlockMatchResult state, AtomicReference<Boolean> skipOtherGuide) {
        return Optional.empty();
    }

    /** 位置为空气 / 可替换方块：需要放置 */
    protected Optional<Action> onBuildActionMissingBlock(BlockMatchResult state, AtomicReference<Boolean> skipOtherGuide) {
        return Optional.empty();
    }

    /** 方块类型完全不同：需要先破坏再放置 */
    protected Optional<Action> onBuildActionWrongBlock(BlockMatchResult state, AtomicReference<Boolean> skipOtherGuide) {
        return Optional.empty();
    }

    /** 方块类型相同但状态不对：可能需要交互修正 */
    protected Optional<Action> onBuildActionWrongState(BlockMatchResult state, AtomicReference<Boolean> skipOtherGuide) {
        return Optional.empty();
    }

    /** 完全正确：通常无需操作 */
    protected Optional<Action> onBuildActionCorrect(BlockMatchResult state, AtomicReference<Boolean> skipOtherGuide) {
        return Optional.empty();
    }
}
