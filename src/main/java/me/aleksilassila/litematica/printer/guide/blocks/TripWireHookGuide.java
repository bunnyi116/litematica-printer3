package me.aleksilassila.litematica.printer.guide.blocks;

import me.aleksilassila.litematica.printer.enums.BlockMatchResult;
import me.aleksilassila.litematica.printer.guide.Guide;
import me.aleksilassila.litematica.printer.guide.Result;
import me.aleksilassila.litematica.printer.printer.SchematicBlockContext;
import me.aleksilassila.litematica.printer.printer.action.Action;
import net.minecraft.world.level.block.TripWireHookBlock;

/**
 * 绊线钩
 */
public class TripWireHookGuide extends Guide {

    public TripWireHookGuide(SchematicBlockContext context) {
        super(context);
    }

    @Override
    protected Result onBuildActionMissingBlock(BlockMatchResult state) {
        var hookFacing = getProperty(requiredState, TripWireHookBlock.FACING).orElse(null);
        if (hookFacing == null) return Result.PASS;
        return Result.success(new Action().setSides(hookFacing));
    }

    @Override
    protected Result onBuildActionWrongState(BlockMatchResult state) {
        return Result.SKIP;
    }
}
