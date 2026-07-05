package me.aleksilassila.litematica.printer.printer.guide.guides;

import me.aleksilassila.litematica.printer.config.Configs;
import me.aleksilassila.litematica.printer.printer.guide.BlockMatchResult;
import me.aleksilassila.litematica.printer.printer.guide.Guide;
import me.aleksilassila.litematica.printer.printer.guide.Result;
import me.aleksilassila.litematica.printer.printer.SchematicBlockContext;
import me.aleksilassila.litematica.printer.printer.action.Action;
import me.aleksilassila.litematica.printer.utils.InteractionUtils;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.piston.PistonBaseBlock;

/**
 * 活塞放置
 */
public class PistonGuide extends Guide {

    public PistonGuide(SchematicBlockContext context) {
        super(context);
    }

    @Override
    protected Result onBuildActionMissingBlock(BlockMatchResult state) {
        Direction facing = getProperty(requiredState, PistonBaseBlock.FACING).orElse(null);
        if (facing == null) return Result.skip();
        return Result.success(new Action().setLookDirection(facing.getOpposite()));
    }

    @Override
    protected Result onBuildActionWrongState(BlockMatchResult state) {
        if (Configs.Print.BREAK_WRONG_STATE_BLOCK.getBooleanValue()) {
            if (currentState.hasProperty(PistonBaseBlock.FACING)) {
                if (!getProperty(currentState, PistonBaseBlock.FACING).equals(getProperty(requiredState, PistonBaseBlock.FACING))) {
                    InteractionUtils.INSTANCE.add(context);
                }
            }
        }
        return Result.skip();
    }
}
