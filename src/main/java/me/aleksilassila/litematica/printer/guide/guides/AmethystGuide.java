package me.aleksilassila.litematica.printer.guide.guides;

import me.aleksilassila.litematica.printer.enums.BlockMatchResult;
import me.aleksilassila.litematica.printer.guide.Guide;
import me.aleksilassila.litematica.printer.guide.Result;
import me.aleksilassila.litematica.printer.printer.SchematicBlockContext;
import me.aleksilassila.litematica.printer.printer.action.Action;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.AmethystClusterBlock;

/**
 * 紫水晶芽
 */
public class AmethystGuide extends Guide {

    public AmethystGuide(SchematicBlockContext context) {
        super(context);
    }

    @Override
    protected Result onBuildActionMissingBlock(BlockMatchResult state) {
        Direction attachDirection = getProperty(requiredState, AmethystClusterBlock.FACING)
                .orElseThrow()
                .getOpposite();

        return Result.success(new Action()
                .setSides(attachDirection)
                .setRequiresSupport());
    }
}
