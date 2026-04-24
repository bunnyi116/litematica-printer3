package me.aleksilassila.litematica.printer.guide.blocks;

import me.aleksilassila.litematica.printer.enums.BlockMatchResult;
import me.aleksilassila.litematica.printer.guide.Guide;
import me.aleksilassila.litematica.printer.guide.Result;
import me.aleksilassila.litematica.printer.printer.SchematicBlockContext;
import me.aleksilassila.litematica.printer.printer.action.Action;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.RailShape;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * 铁轨。
 */
public class RailGuide extends Guide {
    public RailGuide(SchematicBlockContext context) {
        super(context);
    }

    @Override
    protected Result onBuildActionMissingBlock(BlockMatchResult state) {
        Optional<RailShape> railShape = getProperty(requiredState, BlockStateProperties.RAIL_SHAPE)
                .or(() -> getProperty(requiredState, BlockStateProperties.RAIL_SHAPE_STRAIGHT));

        if (railShape.isEmpty()) return Result.PASS;

        Action action = new Action();
        switch (railShape.get()) {
            case EAST_WEST, ASCENDING_EAST -> action.setLookDirection(Direction.EAST);
            case NORTH_SOUTH, ASCENDING_NORTH -> action.setLookDirection(Direction.NORTH);
            case ASCENDING_WEST -> action.setLookDirection(Direction.WEST);
            case ASCENDING_SOUTH -> action.setLookDirection(Direction.SOUTH);
            default -> {
            }
        }
        return Result.success(action);
    }

    @Override
    protected Result onBuildActionWrongState(BlockMatchResult state) {
        return Result.SKIP;
    }
}
