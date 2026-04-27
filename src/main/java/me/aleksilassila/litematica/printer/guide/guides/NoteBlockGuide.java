package me.aleksilassila.litematica.printer.guide.guides;

import me.aleksilassila.litematica.printer.config.Configs;
import me.aleksilassila.litematica.printer.enums.BlockMatchResult;
import me.aleksilassila.litematica.printer.guide.Guide;
import me.aleksilassila.litematica.printer.guide.Result;
import me.aleksilassila.litematica.printer.printer.SchematicBlockContext;
import me.aleksilassila.litematica.printer.printer.action.ClickAction;
import net.minecraft.world.level.block.NoteBlock;

/**
 * 音符盒
 */
public class NoteBlockGuide extends Guide {

    public NoteBlockGuide(SchematicBlockContext context) {
        super(context);
    }

    @Override
    protected Result onBuildActionWrongState(BlockMatchResult state) {
        if (Configs.Print.NOTE_BLOCK_TUNING.getBooleanValue()
                && !getProperty(requiredState, NoteBlock.NOTE).equals(getProperty(currentState, NoteBlock.NOTE))) {
            return Result.success(new ClickAction());
        }
        return Result.SKIP;
    }
}
