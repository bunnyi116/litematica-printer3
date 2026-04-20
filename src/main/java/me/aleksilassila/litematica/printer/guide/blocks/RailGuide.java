package me.aleksilassila.litematica.printer.guide.blocks;

import me.aleksilassila.litematica.printer.enums.BlockMatchResult;
import me.aleksilassila.litematica.printer.guide.Guide;
import me.aleksilassila.litematica.printer.printer.SchematicBlockContext;
import me.aleksilassila.litematica.printer.printer.action.Action;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.RailBlock;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.RailShape;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 铁轨。
 */
public class RailGuide extends Guide {
    public RailGuide(SchematicBlockContext context) {
        super(context);
    }

    @Override
    protected Optional<Action> onBuildActionMissingBlock(BlockMatchResult state, AtomicReference<Boolean> skipOtherGuide) {
        Optional<RailShape> railShape = getProperty(requiredState, BlockStateProperties.RAIL_SHAPE)
                .or(() -> getProperty(requiredState, BlockStateProperties.RAIL_SHAPE_STRAIGHT));

        if (railShape.isEmpty()) return Optional.empty();

        Action action = new Action();
        switch (railShape.get()) {
            case EAST_WEST, ASCENDING_EAST -> action.setLookDirection(Direction.EAST);
            case NORTH_SOUTH, ASCENDING_NORTH -> action.setLookDirection(Direction.NORTH);
            case ASCENDING_WEST -> action.setLookDirection(Direction.WEST);
            case ASCENDING_SOUTH -> action.setLookDirection(Direction.SOUTH);
            default -> {
            }
        }
        return Optional.of(action);
    }

    @Override
    protected Optional<Action> onBuildActionWrongState(BlockMatchResult state, AtomicReference<Boolean> skipOtherGuide) {
        if (currentState.hasProperty(BlockStateProperties.POWERED)
                && !currentState.getValue(BlockStateProperties.POWERED).equals(requiredState.getValue(BlockStateProperties.POWERED))) {
            skipOtherGuide.set(true);
            return Optional.empty();
        }
        return Optional.empty();
    }
}
