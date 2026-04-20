package me.aleksilassila.litematica.printer.guide.blocks;

import me.aleksilassila.litematica.printer.enums.BlockMatchResult;
import me.aleksilassila.litematica.printer.guide.Guide;
import me.aleksilassila.litematica.printer.printer.SchematicBlockContext;
import me.aleksilassila.litematica.printer.printer.action.Action;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 可可豆
 */
public class CocoaGuide extends Guide {

    public CocoaGuide(SchematicBlockContext context) {
        super(context);
    }

    @Override
    protected Optional<Action> onBuildActionMissingBlock(BlockMatchResult state, AtomicReference<Boolean> skipOtherGuide) {
        var cocoaFacing = getProperty(requiredState, BlockStateProperties.HORIZONTAL_FACING).orElse(null);
        if (cocoaFacing == null) return Optional.empty();
        return Optional.of(new Action().setSides(cocoaFacing));
    }

    @Override
    protected Optional<Action> onBuildActionWrongState(BlockMatchResult state, AtomicReference<Boolean> skipOtherGuide) {
        // AGE 由生长决定，环境决定 → 跳过
        skipOtherGuide.set(true);
        return Optional.empty();
    }
}
