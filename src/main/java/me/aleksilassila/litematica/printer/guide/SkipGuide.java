package me.aleksilassila.litematica.printer.guide;

import me.aleksilassila.litematica.printer.enums.BlockMatchResult;
import me.aleksilassila.litematica.printer.printer.SchematicBlockContext;

public class SkipGuide extends Guide {

    public SkipGuide(SchematicBlockContext context) {
        super(context);
    }

    @Override
    protected Result onBuildAction(BlockMatchResult state) {
        return Result.SKIP;
    }
}