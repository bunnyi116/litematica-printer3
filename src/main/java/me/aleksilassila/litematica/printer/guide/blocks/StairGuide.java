package me.aleksilassila.litematica.printer.guide.blocks;

import me.aleksilassila.litematica.printer.config.Configs;
import me.aleksilassila.litematica.printer.enums.BlockMatchResult;
import me.aleksilassila.litematica.printer.guide.Guide;
import me.aleksilassila.litematica.printer.printer.SchematicBlockContext;
import me.aleksilassila.litematica.printer.printer.action.Action;
import me.aleksilassila.litematica.printer.utils.InteractionUtils;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 楼梯
 */
public class StairGuide extends Guide {

    public StairGuide(SchematicBlockContext context) {
        super(context);
    }

    @Override
    protected Optional<Action> onBuildActionMissingBlock(BlockMatchResult state, AtomicReference<Boolean> skipOtherGuide) {
        if (facing == null || half == null) return Optional.empty();

        Map<Direction, Vec3> sides = new HashMap<>();
        if (half == Half.BOTTOM) {
            sides.put(Direction.DOWN, Vec3.ZERO);
            sides.put(facing, Vec3.ZERO);
        } else {
            sides.put(Direction.UP, new Vec3(0, 0.75, 0));
            sides.put(facing.getOpposite(), new Vec3(0, 0.75, 0));
        }

        return Optional.of(new Action().setSides(sides).setLookDirection(facing));
    }

    @Override
    protected Optional<Action> onBuildActionWrongState(BlockMatchResult state, AtomicReference<Boolean> skipOtherGuide) {
        if (Configs.Print.BREAK_WRONG_STATE_BLOCK.getBooleanValue()) {
            Direction currentFacing = getProperty(currentState, BlockStateProperties.FACING).orElse(null);
            Half currentHalf = getProperty(currentState, BlockStateProperties.HALF).orElse(null);
            if (facing != currentFacing || half != currentHalf) {
                InteractionUtils.INSTANCE.add(context);
            }
        }
        return Optional.empty();
    }
}
