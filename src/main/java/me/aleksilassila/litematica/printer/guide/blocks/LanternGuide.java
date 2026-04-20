package me.aleksilassila.litematica.printer.guide.blocks;

import me.aleksilassila.litematica.printer.enums.BlockMatchResult;
import me.aleksilassila.litematica.printer.guide.Guide;
import me.aleksilassila.litematica.printer.printer.SchematicBlockContext;
import me.aleksilassila.litematica.printer.printer.action.Action;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.LanternBlock;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 灯笼
 */
public class LanternGuide extends Guide {

    public LanternGuide(SchematicBlockContext context) {
        super(context);
    }

    @Override
    protected Optional<Action> onBuildActionMissingBlock(BlockMatchResult state, AtomicReference<Boolean> skipOtherGuide) {
        if (requiredState.getValue(LanternBlock.HANGING)) {
            return Optional.of(new Action().setLookDirection(Direction.UP));
        }
        return Optional.of(new Action().setLookDirection(Direction.DOWN));
    }
}
