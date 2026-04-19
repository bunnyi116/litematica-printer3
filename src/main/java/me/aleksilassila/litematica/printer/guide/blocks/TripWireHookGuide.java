package me.aleksilassila.litematica.printer.guide.blocks;

import me.aleksilassila.litematica.printer.enums.BlockMatchResult;
import me.aleksilassila.litematica.printer.guide.Guide;
import me.aleksilassila.litematica.printer.printer.SchematicBlockContext;
import me.aleksilassila.litematica.printer.printer.action.Action;
import net.minecraft.world.level.block.TripWireHookBlock;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 绊线钩放置指南。
 * 注册到：TripWireHookBlock.class
 */
public class TripWireHookGuide extends Guide {

    public TripWireHookGuide(SchematicBlockContext context) {
        super(context);
    }

    @Override
    protected Optional<Action> onBuildActionMissingBlock(BlockMatchResult state, AtomicReference<Boolean> skipOtherGuide) {
        var hookFacing = getProperty(requiredState, TripWireHookBlock.FACING).orElse(null);
        if (hookFacing == null) return Optional.empty();
        return Optional.of(new Action().setSides(hookFacing));
    }
}
