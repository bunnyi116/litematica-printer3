package me.aleksilassila.litematica.printer.printer.guide.guides;

import me.aleksilassila.litematica.printer.printer.guide.BlockMatchResult;
import me.aleksilassila.litematica.printer.printer.guide.Guide;
import me.aleksilassila.litematica.printer.printer.guide.Result;
import me.aleksilassila.litematica.printer.printer.SchematicBlockContext;
import me.aleksilassila.litematica.printer.printer.action.Action;
import net.minecraft.world.level.block.CocoaBlock;

/**
 * 可可豆
 */
public class CocoaGuide extends Guide {

    public CocoaGuide(SchematicBlockContext context) {
        super(context);
    }

    @Override
    protected Result onBuildActionMissingBlock(BlockMatchResult state) {
        var cocoaFacing = getProperty(requiredState, CocoaBlock.FACING).orElseThrow();
        return Result.success(new Action().setSides(cocoaFacing));
    }

    @Override
    protected Result onBuildActionWrongState(BlockMatchResult state) {
        return Result.skip();
    }
}
