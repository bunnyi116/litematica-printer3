package me.aleksilassila.litematica.printer.guide.blocks;

import me.aleksilassila.litematica.printer.config.Configs;
import me.aleksilassila.litematica.printer.enums.BlockMatchResult;
import me.aleksilassila.litematica.printer.guide.Guide;
import me.aleksilassila.litematica.printer.guide.Result;
import me.aleksilassila.litematica.printer.printer.SchematicBlockContext;
import me.aleksilassila.litematica.printer.printer.action.Action;
import me.aleksilassila.litematica.printer.utils.InteractionUtils;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;

/**
 * 箱子
 */
public class ChestGuide extends Guide {

    public ChestGuide(SchematicBlockContext context) {
        super(context);
    }

    @Override
    protected Result onBuildActionMissingBlock(BlockMatchResult state) {
        Direction facing = getProperty(requiredState, ChestBlock.FACING).orElseThrow();

        Direction facingOpposite = facing.getOpposite();
        ChestType chestType = getProperty(requiredState, BlockStateProperties.CHEST_TYPE).orElse(ChestType.SINGLE);

        // 收集所有不与其他箱子相邻的面
        Map<Direction, Vec3> noChestSides = new HashMap<>();
        for (Direction side : Direction.values()) {
            if (level.getBlockState(blockPos.relative(side)).getBlock() instanceof ChestBlock) {
                continue;
            }
            noChestSides.put(side, Vec3.ZERO);
        }

        if (chestType == ChestType.SINGLE) {
            // 有水平方向的箱子邻居 → 潜行放置（防止自动合并）
            boolean hasChestNeighbor = Direction.Plane.HORIZONTAL.stream()
                    .anyMatch(s -> !noChestSides.containsKey(s));
            if (hasChestNeighbor) {
                return Result.success(new Action().setLookDirection(facingOpposite).setShift());
            }
            return Result.success(new Action().setSides(noChestSides).setLookDirection(facingOpposite));
        }

        // 双箱子：不潜行放置，让 Minecraft 自动合并
        // 无论另一半是否已放，都不能潜行，否则会阻止合并
        Direction partnerDir = chestType == ChestType.LEFT
                ? facing.getCounterClockWise()
                : facing.getClockWise();

        Map<Direction, Vec3> clickSides = new HashMap<>(noChestSides);
        clickSides.put(partnerDir, Vec3.ZERO);  // 也允许从另一半方向点击

        return Result.success(new Action()
                .setSides(clickSides)
                .setLookDirection(facingOpposite)
                .setShift(false));
    }

    @Override
    protected Result onBuildActionWrongState(BlockMatchResult state) {
        // 目标是双箱子：当前箱子类型不对，需要破坏后重新放置来触发自动合并
        if (Configs.Print.BREAK_WRONG_STATE_BLOCK.getBooleanValue()) {
            InteractionUtils.INSTANCE.add(context);
        }
        return Result.SKIP;
    }
}
