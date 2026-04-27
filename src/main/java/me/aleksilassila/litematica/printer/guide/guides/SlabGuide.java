package me.aleksilassila.litematica.printer.guide.guides;

import me.aleksilassila.litematica.printer.config.Configs;
import me.aleksilassila.litematica.printer.enums.BlockMatchResult;
import me.aleksilassila.litematica.printer.guide.Guide;
import me.aleksilassila.litematica.printer.guide.Result;
import me.aleksilassila.litematica.printer.printer.PrinterUtils;
import me.aleksilassila.litematica.printer.printer.SchematicBlockContext;
import me.aleksilassila.litematica.printer.printer.action.Action;
import me.aleksilassila.litematica.printer.printer.action.ClickAction;
import me.aleksilassila.litematica.printer.utils.InteractionUtils;
import me.aleksilassila.litematica.printer.utils.minecraft.DirectionUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;

/**
 * 台阶
 */
public class SlabGuide extends Guide {

    /** 台阶类型：SLAB_TYPE（从 requiredState 提取） */
    private final SlabType slabType;

    public SlabGuide(SchematicBlockContext context) {
        super(context);
        this.slabType = getProperty(requiredState, SlabBlock.TYPE).orElseThrow();
    }

    @Override
    protected Result onBuildActionMissingBlock(BlockMatchResult state) {
        // DOUBLE + WRONG_STATE：在已有单层台阶上点击另一面来合并
        // 交给 onBuildActionWrongState 处理（使用 ClickAction 直接点击方块本身）
        if (slabType == SlabType.DOUBLE && state == BlockMatchResult.WRONG_STATE) {
            return Result.SKIP;
        }

        // DOUBLE：MISSING 时当前位置是空气，需要先放一个单层台阶（BOTTOM）
        if (slabType == SlabType.DOUBLE && state == BlockMatchResult.MISSING) {
            // 使用 PrinterUtils.getSlabSides 确保只在有支撑的面放置
            Map<Direction, Vec3> slabSides = PrinterUtils.getSlabSides(level, blockPos, SlabType.BOTTOM);
            return Result.success(new Action().setSides(slabSides));
        }

        Map<Direction, Vec3> sides = new HashMap<>();
        Direction half;

        if (slabType == SlabType.TOP) {
            half = Direction.UP;
        } else if (slabType == SlabType.BOTTOM) {
            half = Direction.DOWN;
        } else {
            // DOUBLE + MISSING（上面已处理，这里不会到达）
            half = Direction.DOWN;
        }

        sides.put(half, Vec3.ZERO);

        // 检查水平相邻台阶
        for (Direction side : Direction.Plane.HORIZONTAL) {
            BlockPos neighborPos = blockPos.relative(side);
            BlockState neighborState = level.getBlockState(neighborPos);
            if (neighborState.hasProperty(SlabBlock.TYPE)) {
                SlabType neighborType = getProperty(neighborState, SlabBlock.TYPE).orElse(SlabType.BOTTOM);
                if (neighborType != SlabType.DOUBLE && neighborType != slabType) {
                    continue;
                }
            }
            sides.put(side, Vec3.atLowerCornerOf(DirectionUtils.getVector(half)).scale(0.25));
        }

        return Result.success(new Action().setSides(sides));
    }

    @Override
    protected Result onBuildActionWrongState(BlockMatchResult state) {

        // DOUBLE：在已有单层台阶上点击另一面来合并成双层
        // 使用 ClickAction 直接点击方块本身，因为普通 Action 的 getValidSide
        // 会检查相邻方块是否可点击——台阶上方通常是空气，UP 面会被过滤掉
        if (slabType == SlabType.DOUBLE) {
            if (currentState.hasProperty(SlabBlock.TYPE)) {
                SlabType current = getProperty(currentState, SlabBlock.TYPE).orElse(SlabType.BOTTOM);
                // 点击面应该是当前台阶的「缺失面」：BOTTOM 台阶缺上方 → 点 UP，TOP 台阶缺下方 → 点 DOWN
                Direction clickFace = current == SlabType.BOTTOM ? Direction.UP : Direction.DOWN;
                return Result.success(new ClickAction()
                        .setSides(clickFace)
                        .setItem(requiredBlock.asItem()));
            }
        }

        // 其他 WRONG_STATE：根据配置决定是否破坏
        if (Configs.Print.BREAK_WRONG_STATE_BLOCK.getBooleanValue()) {
            InteractionUtils.INSTANCE.add(context);
        }
        return Result.SKIP;
    }
}
