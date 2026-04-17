package me.aleksilassila.litematica.printer.guide;

import me.aleksilassila.litematica.printer.enums.BlockMatchResult;
import me.aleksilassila.litematica.printer.printer.SchematicBlockContext;
import me.aleksilassila.litematica.printer.printer.action.Action;
import me.aleksilassila.litematica.printer.utils.minecraft.BlockStateUtils;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public abstract class Guide extends BlockStateUtils {
    protected final SchematicBlockContext context;
    protected final Block currentBlock;
    protected final Block requiredBlock;
    protected final BlockState currentState;
    protected final BlockState requiredState;

    public Guide(SchematicBlockContext context) {
        this.context = context;
        this.currentBlock = context.currentState.getBlock();
        this.requiredBlock = context.requiredState.getBlock();
        this.currentState = context.currentState;
        this.requiredState = context.requiredState;
    }

    public boolean canExecute(AtomicReference<Boolean> skipOtherGuide) {
        return true;
    }

    public final Optional<Action> buildAction(BlockMatchResult state, AtomicReference<Boolean> skipOtherGuide) {
        return this.onBuildAction(state, skipOtherGuide)
                .or(() -> switch (state) {
                    case MISSING -> this.onBuildActionMissingBlock(state, skipOtherGuide);
                    case WRONG_BLOCK -> this.onBuildActionWrongBlock(state, skipOtherGuide);
                    case WRONG_STATE -> this.onBuildActionWrongState(state, skipOtherGuide);
                    case CORRECT -> this.onBuildActionCorrect(state, skipOtherGuide);
                });
    }

    protected Optional<Action> onBuildAction(BlockMatchResult state, AtomicReference<Boolean> skipOtherGuide) {
        return Optional.empty();
    }

    protected Optional<Action> onBuildActionMissingBlock(BlockMatchResult state, AtomicReference<Boolean> skipOtherGuide) {
        return Optional.empty();
    }

    protected Optional<Action> onBuildActionWrongBlock(BlockMatchResult state, AtomicReference<Boolean> skipOtherGuide) {
        return Optional.empty();
    }

    protected Optional<Action> onBuildActionWrongState(BlockMatchResult state, AtomicReference<Boolean> skipOtherGuide) {
        return Optional.empty();
    }

    protected Optional<Action> onBuildActionCorrect(BlockMatchResult state, AtomicReference<Boolean> skipOtherGuide) {
        return Optional.empty();
    }
}