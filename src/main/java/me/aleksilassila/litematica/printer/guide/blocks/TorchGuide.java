package me.aleksilassila.litematica.printer.guide.blocks;

import me.aleksilassila.litematica.printer.enums.BlockMatchResult;
import me.aleksilassila.litematica.printer.guide.Guide;
import me.aleksilassila.litematica.printer.printer.SchematicBlockContext;
import me.aleksilassila.litematica.printer.printer.action.Action;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 火把
 */
public class TorchGuide extends Guide {

    public TorchGuide(SchematicBlockContext context) {
        super(context);
    }

    @Override
    protected Optional<Action> onBuildActionMissingBlock(BlockMatchResult state, AtomicReference<Boolean> skipOtherGuide) {
        // 火把 FACING 表示附着面方向（UP=地面向上，NORTH=北墙等）
        Direction attachDirection = facing != null
                ? facing
                : getProperty(requiredState, BlockStateProperties.FACING).orElse(Direction.UP);
        return Optional.of(new Action()
                .setSides(attachDirection)
                .setLookDirection(attachDirection.getOpposite())
                .setRequiresSupport());
    }
}
