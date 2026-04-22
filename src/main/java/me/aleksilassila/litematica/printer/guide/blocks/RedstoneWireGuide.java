package me.aleksilassila.litematica.printer.guide.blocks;

import me.aleksilassila.litematica.printer.enums.BlockMatchResult;
import me.aleksilassila.litematica.printer.guide.Guide;
import me.aleksilassila.litematica.printer.printer.SchematicBlockContext;
import me.aleksilassila.litematica.printer.printer.action.Action;
import me.aleksilassila.litematica.printer.printer.action.ClickAction;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.RedstoneSide;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 红石线
 */
public class RedstoneWireGuide extends Guide {
    public RedstoneWireGuide(SchematicBlockContext context) {
        super(context);
    }

    @Override
    protected Optional<Action> onBuildActionWrongState(BlockMatchResult state, AtomicReference<Boolean> skipOtherGuide) {
        RedstoneSide rNorth = requiredState.getValue(BlockStateProperties.NORTH_REDSTONE);
        RedstoneSide rEast = requiredState.getValue(BlockStateProperties.EAST_REDSTONE);
        RedstoneSide rSouth = requiredState.getValue(BlockStateProperties.SOUTH_REDSTONE);
        RedstoneSide rWest = requiredState.getValue(BlockStateProperties.WEST_REDSTONE);

        RedstoneSide cNorth = currentState.getValue(BlockStateProperties.NORTH_REDSTONE);
        RedstoneSide cEast = currentState.getValue(BlockStateProperties.EAST_REDSTONE);
        RedstoneSide cSouth = currentState.getValue(BlockStateProperties.SOUTH_REDSTONE);
        RedstoneSide cWest = currentState.getValue(BlockStateProperties.WEST_REDSTONE);

        boolean allNoneRequired = rNorth == RedstoneSide.NONE
                && rSouth == RedstoneSide.NONE
                && rEast == RedstoneSide.NONE
                && rWest == RedstoneSide.NONE;

        boolean allSideCurrent = cNorth == RedstoneSide.SIDE
                && cSouth == RedstoneSide.SIDE
                && cEast == RedstoneSide.SIDE
                && cWest == RedstoneSide.SIDE;

        if (allNoneRequired && allSideCurrent) {
            return Optional.of(new ClickAction().setItem(Items.AIR));
        }
        return Optional.empty();
    }
}
