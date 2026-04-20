package me.aleksilassila.litematica.printer.guide.blocks;

import me.aleksilassila.litematica.printer.enums.BlockMatchResult;
import me.aleksilassila.litematica.printer.guide.Guide;
import me.aleksilassila.litematica.printer.printer.SchematicBlockContext;
import me.aleksilassila.litematica.printer.printer.action.Action;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.BellAttachType;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 钟
 */
public class BellGuide extends Guide {
    public BellGuide(SchematicBlockContext context) {
        super(context);
    }

    @Override
    protected Optional<Action> onBuildActionMissingBlock(BlockMatchResult state, AtomicReference<Boolean> skipOtherGuide) {
        BellAttachType bellAttachment = getProperty(requiredState, BlockStateProperties.BELL_ATTACHMENT).orElse(null);

        if (bellAttachment == null || facing == null) return Optional.empty();

        Direction side = switch (bellAttachment) {
            case FLOOR -> Direction.DOWN;
            case CEILING -> Direction.UP;
            default -> facing;
        };

        Direction look = (bellAttachment == BellAttachType.SINGLE_WALL || bellAttachment == BellAttachType.DOUBLE_WALL)
                ? null : facing;

        return Optional.of(new Action().setSides(side).setLookDirection(look));
    }
}
