package me.aleksilassila.litematica.printer.guide.blocks;

import me.aleksilassila.litematica.printer.enums.BlockMatchResult;
import me.aleksilassila.litematica.printer.guide.Guide;
import me.aleksilassila.litematica.printer.printer.SchematicBlockContext;
import me.aleksilassila.litematica.printer.printer.action.Action;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 床
 */
public class BedGuide extends Guide {
    public BedGuide(SchematicBlockContext context) {
        super(context);
    }

    @Override
    protected Optional<Action> onBuildActionMissingBlock(BlockMatchResult state, AtomicReference<Boolean> skipOtherGuide) {
        BedPart bedPart = getProperty(requiredState, BlockStateProperties.BED_PART).orElse(null);
        if (facing == null || bedPart == null) return Optional.empty();
        // 只放置床尾，床头自动生成
        if (bedPart == BedPart.HEAD) {
            return Optional.empty();
        }
        return Optional.of(new Action().setLookDirection(facing));
    }

    @Override
    protected Optional<Action> onBuildActionWrongState(BlockMatchResult state, AtomicReference<Boolean> skipOtherGuide) {
        skipOtherGuide.set(true);
        return Optional.empty();
    }
}
