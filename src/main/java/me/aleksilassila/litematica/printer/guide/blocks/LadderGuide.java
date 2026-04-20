package me.aleksilassila.litematica.printer.guide.blocks;

import me.aleksilassila.litematica.printer.enums.BlockMatchResult;
import me.aleksilassila.litematica.printer.guide.Guide;
import me.aleksilassila.litematica.printer.printer.SchematicBlockContext;
import me.aleksilassila.litematica.printer.printer.action.Action;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 梯子
 */
public class LadderGuide extends Guide {

    public LadderGuide(SchematicBlockContext context) {
        super(context);
    }

    @Override
    protected Optional<Action> onBuildActionMissingBlock(BlockMatchResult state, AtomicReference<Boolean> skipOtherGuide) {
        var ladderFacing = getProperty(requiredState, LadderBlock.FACING).orElse(null);
        if (ladderFacing == null) return Optional.empty();
        return Optional.of(new Action()
                .setSides(ladderFacing)
                .setLookDirection(ladderFacing.getOpposite()));
    }
}
