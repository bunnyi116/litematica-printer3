package me.aleksilassila.litematica.printer.guide.guides;

import me.aleksilassila.litematica.printer.enums.BlockMatchResult;
import me.aleksilassila.litematica.printer.guide.Guide;
import me.aleksilassila.litematica.printer.guide.Result;
import me.aleksilassila.litematica.printer.printer.SchematicBlockContext;
import me.aleksilassila.litematica.printer.printer.action.Action;
import net.minecraft.world.level.block.LadderBlock;

/**
 * 梯子
 */
public class LadderGuide extends Guide {

    public LadderGuide(SchematicBlockContext context) {
        super(context);
    }

    @Override
    protected Result onBuildActionMissingBlock(BlockMatchResult state) {
        var ladderFacing = getProperty(requiredState, LadderBlock.FACING).orElseThrow();
        return Result.success(new Action()
                .setSides(ladderFacing)
                .setLookDirection(ladderFacing.getOpposite()));
    }
}
