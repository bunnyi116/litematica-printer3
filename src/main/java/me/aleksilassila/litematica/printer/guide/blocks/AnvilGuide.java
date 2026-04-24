package me.aleksilassila.litematica.printer.guide.blocks;

import me.aleksilassila.litematica.printer.enums.BlockMatchResult;
import me.aleksilassila.litematica.printer.guide.Guide;
import me.aleksilassila.litematica.printer.guide.Result;
import me.aleksilassila.litematica.printer.printer.SchematicBlockContext;
import me.aleksilassila.litematica.printer.printer.action.Action;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.AnvilBlock;

/**
 * 铁砧。
 */
public class AnvilGuide extends Guide {

    public AnvilGuide(SchematicBlockContext context) {
        super(context);
    }

    @Override
    protected Result onBuildActionMissingBlock(BlockMatchResult state) {
        Direction anvilFacing = getProperty(requiredState, AnvilBlock.FACING).orElseThrow();
        return Result.success(new Action().setLookDirection(anvilFacing.getCounterClockWise()));
    }
}
