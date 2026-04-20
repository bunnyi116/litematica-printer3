package me.aleksilassila.litematica.printer.guide.blocks;

import me.aleksilassila.litematica.printer.config.Configs;
import me.aleksilassila.litematica.printer.enums.BlockMatchResult;
import me.aleksilassila.litematica.printer.guide.Guide;
import me.aleksilassila.litematica.printer.printer.SchematicBlockContext;
import me.aleksilassila.litematica.printer.printer.action.Action;
import me.aleksilassila.litematica.printer.printer.action.ClickAction;
import net.minecraft.world.level.block.NoteBlock;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 音符盒
 */
public class NoteBlockGuide extends Guide {

    public NoteBlockGuide(SchematicBlockContext context) {
        super(context);
    }

    @Override
    protected Optional<Action> onBuildActionWrongState(BlockMatchResult state, AtomicReference<Boolean> skipOtherGuide) {
        if (Configs.Print.NOTE_BLOCK_TUNING.getBooleanValue()
                && !Objects.equals(requiredState.getValue(NoteBlock.NOTE), currentState.getValue(NoteBlock.NOTE))) {
            return Optional.of(new ClickAction());
        }
        return Optional.empty();
    }
}
