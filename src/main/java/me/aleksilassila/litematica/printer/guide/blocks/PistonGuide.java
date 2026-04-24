package me.aleksilassila.litematica.printer.guide.blocks;

import me.aleksilassila.litematica.printer.enums.BlockMatchResult;
import me.aleksilassila.litematica.printer.guide.Guide;
import me.aleksilassila.litematica.printer.printer.SchematicBlockContext;
import me.aleksilassila.litematica.printer.printer.action.Action;
import me.aleksilassila.litematica.printer.utils.InteractionUtils;
import me.aleksilassila.litematica.printer.utils.mods.LitematicaUtils;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 活塞放置
 */
public class PistonGuide extends Guide {

    public PistonGuide(SchematicBlockContext context) {
        super(context);
    }

    @Override
    protected Optional<Action> onBuildActionMissingBlock(BlockMatchResult state, AtomicReference<Boolean> skipOtherGuide) {
        return getFacing().map(direction -> new Action().setLookDirection(direction.getOpposite()));
    }

    @Override
    protected Optional<Action> onBuildActionWrongState(BlockMatchResult state, AtomicReference<Boolean> skipOtherGuide) {
        if (currentState.hasProperty(PistonBaseBlock.FACING)) {
            if (!currentState.getValue(PistonBaseBlock.FACING).equals(requiredState.getValue(PistonBaseBlock.FACING))) {
                InteractionUtils.INSTANCE.add(context);
                skipOtherGuide.set(true);
                return Optional.empty();
            }
        }
        return Optional.empty();
    }
}
