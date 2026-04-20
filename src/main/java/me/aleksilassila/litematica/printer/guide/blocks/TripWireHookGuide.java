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
 * 绊线钩
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

    @Override
    protected Optional<Action> onBuildActionWrongState(BlockMatchResult state, AtomicReference<Boolean> skipOtherGuide) {
        // POWERED 由红石/绊线决定，ATTACHED 由绊线连接决定 → 环境决定，跳过
        skipOtherGuide.set(true);
        return Optional.empty();
    }
}
