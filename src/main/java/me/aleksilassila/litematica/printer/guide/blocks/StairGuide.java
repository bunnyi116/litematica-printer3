package me.aleksilassila.litematica.printer.guide.blocks;

import me.aleksilassila.litematica.printer.config.Configs;
import me.aleksilassila.litematica.printer.enums.BlockMatchResult;
import me.aleksilassila.litematica.printer.guide.Guide;
import me.aleksilassila.litematica.printer.guide.Result;
import me.aleksilassila.litematica.printer.printer.SchematicBlockContext;
import me.aleksilassila.litematica.printer.printer.action.Action;
import me.aleksilassila.litematica.printer.utils.InteractionUtils;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;

/**
 * 楼梯
 */
public class StairGuide extends Guide {

    public StairGuide(SchematicBlockContext context) {
        super(context);
    }

    @Override
    protected Result onBuildActionMissingBlock(BlockMatchResult state) {
        Direction facing = getProperty(requiredState, StairBlock.FACING).orElse(null);
        Half half = getProperty(requiredState, StairBlock.HALF).orElse(null);
        if (facing == null || half == null) return Result.PASS;

        Map<Direction, Vec3> sides = new HashMap<>();
        if (half == Half.BOTTOM) {
            sides.put(Direction.DOWN, Vec3.ZERO);
            sides.put(facing, Vec3.ZERO);
        } else {
            sides.put(Direction.UP, new Vec3(0, 0.75, 0));
            sides.put(facing.getOpposite(), new Vec3(0, 0.75, 0));
        }

        return Result.success(new Action().setSides(sides).setLookDirection(facing));
    }

    @Override
    protected Result onBuildActionWrongState(BlockMatchResult state) {
        Direction facing = getProperty(requiredState, StairBlock.FACING).orElse(null);
        Half half = getProperty(requiredState, StairBlock.HALF).orElse(null);
        if (Configs.Print.BREAK_WRONG_STATE_BLOCK.getBooleanValue()) {
            Direction currentFacing = getProperty(currentState, StairBlock.FACING).orElse(null);
            Half currentHalf = getProperty(currentState, StairBlock.HALF).orElse(null);
            if (facing != currentFacing || half != currentHalf) {
                InteractionUtils.INSTANCE.add(context);
            }
        }
        return Result.SKIP;
    }
}
