package me.aleksilassila.litematica.printer.guide.guides;

import me.aleksilassila.litematica.printer.guide.BlockMatchResult;
import me.aleksilassila.litematica.printer.guide.Guide;
import me.aleksilassila.litematica.printer.guide.Result;
import me.aleksilassila.litematica.printer.printer.SchematicBlockContext;
import me.aleksilassila.litematica.printer.action.Action;
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
        var facing = getProperty(requiredState, TripWireHookBlock.FACING).orElseThrow();
        return Result.success(new Action()
                .setSides(facing.getOpposite())
                .setNeedSupportBlock()
        );
    }

    @Override
    protected Result onBuildActionWrongState(BlockMatchResult state) {
        return Result.skipOtherGuide();
    }
}
