package me.aleksilassila.litematica.printer.guide.guides;

import me.aleksilassila.litematica.printer.config.Configs;
import me.aleksilassila.litematica.printer.enums.BlockMatchResult;
import me.aleksilassila.litematica.printer.guide.Guide;
import me.aleksilassila.litematica.printer.guide.Result;
import me.aleksilassila.litematica.printer.printer.SchematicBlockContext;
import me.aleksilassila.litematica.printer.printer.action.Action;
import me.aleksilassila.litematica.printer.utils.InteractionUtils;
import me.aleksilassila.litematica.printer.utils.minecraft.BlockStateUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.IceBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 水源/含水方块处理指南。
 *
 * <p>处理「破冰放水」逻辑：当 schematic 要求水源方块时，使用冰块放置后融化的方式生成水。
 * 同时处理现有冰块破碎放水、错误方块破坏等场景。</p>
 *
 * <p>仅处理 {@link Blocks#WATER} 方块，对其他液体（如熔岩）交由 {@link me.aleksilassila.litematica.printer.guide.SkipGuide} 跳过。</p>
 */
public class WaterGuide extends Guide {

    public WaterGuide(SchematicBlockContext context) {
        super(context);
    }

    @Override
    protected boolean canExecute() {
        return BlockStateUtils.isWaterBlock(requiredState);
    }

    @Override
    protected Result onBuildAction(BlockMatchResult state) {
        // 生存模式检查
        if (client.gameMode == null || client.gameMode.getPlayerMode().isCreative()) {
            return Result.SKIP;
        }

        // 未启用破冰放水 → 跳过水源方块
        if (!Configs.Print.PRINT_ICE_FOR_WATER.getBooleanValue()) {
            return Result.SKIP;
        }

        // 当前是冰块 → 破冰放水
        if (currentBlock instanceof IceBlock) {
            InteractionUtils.INSTANCE.add(context);
            return Result.SKIP;
        }

        // 当前水源等级正确 → 无需操作
        if (BlockStateUtils.isCorrectWaterLevel(requiredState, currentState)) {
            return Result.PASS;
        }

        // 当前是错误方块（非空气、非液体）→ 按配置破坏
        if (!currentState.isAir() && !(currentBlock instanceof LiquidBlock)) {
            if (Configs.Print.BREAK_WRONG_BLOCK.getBooleanValue()) {
                InteractionUtils.INSTANCE.add(context);
            }
            return Result.SKIP;
        }

        // 当前是空气或流动水 → 放置冰块（冰块融化后形成水源）
        // 放置前检查水流动安全性
        if (!canWaterFlowSafely()) {
            return Result.SKIP;
        }

        return Result.success(new Action().setItem(Items.ICE));
    }

    // ============================================================
    // 水流动安全检测
    // ============================================================

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
                // 检查目标位置下方的方块是否已放置成功（支撑流动路径）
                if (!isSchematicBlockPlaced(targetPos.below())) {
                    return false;
                }
                // 检查目标位置本身（schematic 中如果有方块，水流动到这里会破坏它）
                if (!isSchematicBlockPlaced(targetPos)) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * 检查 schematic 中指定位置的方块是否已放置成功（与当前世界方块匹配）。
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
}
