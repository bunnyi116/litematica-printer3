package me.aleksilassila.litematica.printer.guide;

import fi.dy.masa.litematica.world.WorldSchematic;
import me.aleksilassila.litematica.printer.config.Configs;
import me.aleksilassila.litematica.printer.enums.BlockMatchResult;
import me.aleksilassila.litematica.printer.printer.SchematicBlockContext;
import me.aleksilassila.litematica.printer.printer.action.Action;
import me.aleksilassila.litematica.printer.utils.ConfigUtils;
import me.aleksilassila.litematica.printer.utils.minecraft.BlockStateUtils;

import me.aleksilassila.litematica.printer.utils.InteractionUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.item.Items;

import java.util.Optional;

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
    }

    /**
     * 构建 Action 的入口方法。
     */
    public final Result buildAction(BlockMatchResult state) {
        // 前置检查: 完全一致
        if (state == BlockMatchResult.CORRECT) {
            return this.onBuildActionCorrect(state);
        }

        // 液体方块: 破冰放水或跳过
        if (requiredBlock instanceof LiquidBlock) {
            if (Configs.Print.PRINT_ICE_FOR_WATER.getBooleanValue() && requiredState.is(Blocks.WATER)) {
                Result iceResult = handleIceForWater();
                if (iceResult.hasAction()) {
                    return iceResult;
                }
            }
            return Result.SKIP;
        }

        // 方块无法在此位置自然存活，跳过
        if (!requiredState.canSurvive(level, blockPos)) {
            return Result.PASS;
        }

        // 水生植物（海草等）需要水环境才能放置
        if (BlockStateUtils.requiresWaterToPlace(requiredBlock)) {
            BlockPos waterPos = requiredState.hasProperty(BlockStateProperties.WATERLOGGED)
                    ? blockPos : blockPos.above();
            if (!level.getBlockState(waterPos).is(Blocks.WATER)) {
                return Result.PASS;
            }
        }

        // 交给子类的 onBuildAction 拦截钩子
        Result result = this.onBuildAction(state);
        if (!result.passToNext()) {
            return result;
        }

        // 分状态分发
        return switch (state) {
            case MISSING -> this.onBuildActionMissingBlock(state);
            case WRONG_BLOCK -> this.onBuildActionWrongBlock(state);
            case WRONG_STATE -> this.onBuildActionWrongState(state);
            default -> Result.PASS;
        };
    }

    // -------------------------------------------------------
    // 全局前置处理
    // -------------------------------------------------------

    /**
     * 处理「破冰放水」逻辑（仅在启用 PRINT_ICE_FOR_WATER 且生存模式下生效）。
     */
    private Result handleIceForWater() {
        if (!Configs.Print.PRINT_ICE_FOR_WATER.getBooleanValue() || !BlockStateUtils.isWaterBlock(requiredState)) {
            return Result.PASS;
        }
        if (client.gameMode == null || client.gameMode.getPlayerMode().isCreative()) {
            return Result.PASS;
        }
        if (currentBlock instanceof IceBlock) {
            InteractionUtils.INSTANCE.add(context);
            return Result.PASS;
        }
        if (!BlockStateUtils.isCorrectWaterLevel(requiredState, currentState)) {
            if (!currentState.isAir() && !(currentBlock instanceof LiquidBlock)) {
                if (Configs.Print.BREAK_WRONG_BLOCK.getBooleanValue()) {
                    InteractionUtils.INSTANCE.add(context);
                }
                return Result.PASS;
            }
            // 放置冰块前，检查周围方块是否已放置成功
            if (!canWaterFlowSafely()) {
                return Result.PASS;
            }
            return Result.success(new Action().setItem(Items.ICE));
        }
        return Result.PASS;
    }

    /**
     * 检查水流动是否安全（周围的方块是否都已放置成功）。
     * 水从当前位置流动，会影响：
     * 1. 下方方块
     * 2. 水平四个方向的方块（如果水可以流动过去）
     *
     * @return 是否安全
     */
    private boolean canWaterFlowSafely() {
        // 检查下方方块是否已放置成功
        if (!isSchematicBlockPlaced(blockPos.below())) {
            return false;
        }

        // 检查水可能流动到的位置
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos targetPos = blockPos.relative(direction);
            // 如果目标位置是空气，水会流动过去
            if (level.getBlockState(targetPos).isAir()) {
                // 检查目标位置下方的方块是否已放置成功（支持流动路径）
                if (!isSchematicBlockPlaced(targetPos.below())) {
                    return false;
                }
                // 检查目标位置本身（ schematic 中如果有方块，水流动到这里会破坏它）
                if (!isSchematicBlockPlaced(targetPos)) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * 检查 schematic 中指定位置的方块是否已放置成功（与当前世界方块匹配）
     */
    private boolean isSchematicBlockPlaced(BlockPos pos) {
        BlockState schematicState = schematic.getBlockState(pos);
        BlockState worldState = level.getBlockState(pos);

        // schematic 中是空气，无需放置，水流到这里没问题
        if (schematicState.isAir()) {
            return true;
        }

        // 检查世界中的方块是否与 schematic 匹配
        return schematicState.getBlock() == worldState.getBlock();
    }

    /**
     * 检查此 Guide 是否应该处理当前方块
     * 可被子类覆盖以实现更细粒度的过滤
     * @return true 表示应该执行此 Guide
     */
    protected boolean canExecute() {
        return true;
    }

    // -------------------------------------------------------
    // 子类钩子
    // -------------------------------------------------------

    /**
     * 所有状态均会先经过此钩子，可在此拦截任意状态
     */
    protected Result onBuildAction(BlockMatchResult state) {
        return Result.PASS;
    }

    /**
     * 位置为空气 / 可替换方块：需要放置
     */
    protected Result onBuildActionMissingBlock(BlockMatchResult state) {
        return Result.PASS;
    }

    /**
     * 方块类型完全不同：需要先破坏再放置
     */
    protected Result onBuildActionWrongBlock(BlockMatchResult state) {
        return Result.PASS;
    }

    /**
     * 方块类型相同但状态不对：可能需要交互修正
     */
    protected Result onBuildActionWrongState(BlockMatchResult state) {
        return Result.PASS;
    }

    /**
     * 完全正确：通常无需操作
     */
    protected Result onBuildActionCorrect(BlockMatchResult state) {
        return Result.PASS;
    }

}
