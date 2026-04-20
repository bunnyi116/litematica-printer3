package me.aleksilassila.litematica.printer.guide.blocks;

import me.aleksilassila.litematica.printer.enums.BlockMatchResult;
import me.aleksilassila.litematica.printer.guide.Guide;
import me.aleksilassila.litematica.printer.printer.SchematicBlockContext;
import me.aleksilassila.litematica.printer.printer.action.Action;
import me.aleksilassila.litematica.printer.utils.minecraft.DirectionUtils;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.WallSkullBlock;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 头颅
 */
public class SkullGuide extends Guide {

    public SkullGuide(SchematicBlockContext context) {
        super(context);
    }

    @Override
    protected Optional<Action> onBuildActionMissingBlock(BlockMatchResult state, AtomicReference<Boolean> skipOtherGuide) {
        if (requiredBlock instanceof SkullBlock) {
            int rotation = requiredState.getValue(SkullBlock.ROTATION);
            return Optional.of(new Action()
                    .setSides(Direction.DOWN)
                    .setLookRotation(DirectionUtils.getOppositeRotation(rotation))
                    .setRequiresSupport());
        }
        if (requiredBlock instanceof WallSkullBlock && facing != null) {
            return Optional.of(new Action()
                    .setSides(facing.getOpposite())
                    .setLookDirection(facing.getOpposite())
                    .setRequiresSupport());
        }
        return Optional.empty();
    }
}
