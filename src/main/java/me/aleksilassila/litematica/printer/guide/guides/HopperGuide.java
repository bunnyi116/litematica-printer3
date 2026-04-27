package me.aleksilassila.litematica.printer.guide.guides;

import me.aleksilassila.litematica.printer.enums.BlockMatchResult;
import me.aleksilassila.litematica.printer.guide.Guide;
import me.aleksilassila.litematica.printer.guide.Result;
import me.aleksilassila.litematica.printer.printer.SchematicBlockContext;
import me.aleksilassila.litematica.printer.printer.action.Action;
import net.minecraft.world.level.block.HopperBlock;

/**
 * 漏斗
 */
public class HopperGuide extends Guide {

    public HopperGuide(SchematicBlockContext context) {
        super(context);
    }

    @Override
    protected Result onBuildActionMissingBlock(BlockMatchResult state) {
        var hopperFacing = getProperty(requiredState, HopperBlock.FACING).orElseThrow();
        return Result.success(new Action().setSides(hopperFacing));
    }
}
