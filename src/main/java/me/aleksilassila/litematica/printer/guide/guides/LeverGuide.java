package me.aleksilassila.litematica.printer.guide.guides;

import me.aleksilassila.litematica.printer.enums.BlockMatchResult;
import me.aleksilassila.litematica.printer.guide.Guide;
import me.aleksilassila.litematica.printer.guide.Result;
import me.aleksilassila.litematica.printer.printer.SchematicBlockContext;
import me.aleksilassila.litematica.printer.printer.action.ClickAction;
import net.minecraft.world.level.block.LeverBlock;

/**
 * 拉杆
 */
public class LeverGuide extends Guide {

    public LeverGuide(SchematicBlockContext context) {
        super(context);
    }

    @Override
    protected Result onBuildActionWrongState(BlockMatchResult state) {
        if (!getProperty(requiredState, LeverBlock.POWERED).equals(getProperty(currentState, LeverBlock.POWERED))) {
            return Result.success(new ClickAction());
        }
        return Result.SKIP;
    }
}
