package me.aleksilassila.litematica.printer.guide;

import com.google.common.collect.ImmutableList;
import me.aleksilassila.litematica.printer.enums.BlockMatchResult;
import me.aleksilassila.litematica.printer.printer.SchematicBlockContext;
import me.aleksilassila.litematica.printer.printer.action.Action;
import me.aleksilassila.litematica.printer.utils.minecraft.DirectionUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public class DefaultGuide extends Guide {
    private static final ImmutableList<Class<? extends Block>> NEED_SUPPORT_CENTER_BLOCKS = ImmutableList.of(
            RodBlock.class, TorchBlock.class, FlowerBlock.class
    );

    private static final ImmutableList<Class<? extends Block>> NO_REVERSE_FACING_BLOCKS = ImmutableList.of(
            ObserverBlock.class, StairBlock.class, FenceGateBlock.class
    );

    public DefaultGuide(SchematicBlockContext context) {
        super(context);
    }

    @Override
    protected Optional<Action> onBuildActionMissingBlock(BlockMatchResult state, AtomicReference<Boolean> skipOtherGuide) {
        Action action = new Action();

        Optional<BedPart> bedPart = getProperty(requiredState, BlockStateProperties.BED_PART);
        if (bedPart.isPresent() && bedPart.get() == BedPart.HEAD) {
            return Optional.empty();
        }

        getProperty(requiredState, BlockStateProperties.FACING)
                .or(() -> getProperty(requiredState, BlockStateProperties.HORIZONTAL_FACING))
                .or(() -> getProperty(requiredState, BlockStateProperties.VERTICAL_DIRECTION))
                .or(() -> getProperty(requiredState, BlockStateProperties.FACING_HOPPER))
                .ifPresent(facing -> {
                    boolean noReverseFacing = NO_REVERSE_FACING_BLOCKS.stream()
                            .anyMatch(clazz -> clazz.isInstance(requiredBlock));
                    if (noReverseFacing) {
                        action.setLookDirection(facing);
                    } else {
                        action.setLookDirection(facing.getOpposite());
                    }
                });

        // 处理 AXIS 轴方块
        getProperty(requiredState, BlockStateProperties.AXIS)
                .or(() -> getProperty(requiredState, BlockStateProperties.HORIZONTAL_AXIS))
                .ifPresent(action::setSides);


        getProperty(requiredState, BlockStateProperties.HALF).ifPresent(half -> {
            Direction side = half == Half.BOTTOM ? Direction.DOWN : Direction.UP;
            action.setSides(side);
        });

        getProperty(requiredState, BlockStateProperties.SLAB_TYPE).ifPresent(slabType -> {
            action.setSides(getSlabSides(context.level, context.blockPos, slabType));
        });

        if (NEED_SUPPORT_CENTER_BLOCKS.stream().anyMatch(clazz -> clazz.isInstance(requiredBlock))) {
            action.setRequiresSupport();
        }

        return Optional.of(action);
    }

    @Override
    protected Optional<Action> onBuildActionWrongState(BlockMatchResult state, AtomicReference<Boolean> skipOtherGuide) {
        Action action = new Action();
        getProperty(requiredState, BlockStateProperties.SLAB_TYPE).ifPresent(requiredSlabType -> {
            if (requiredSlabType == SlabType.DOUBLE) {
                getProperty(currentState, BlockStateProperties.SLAB_TYPE).ifPresent(currentSlabType -> {
                    action.setSides(currentSlabType == SlabType.BOTTOM ? Direction.UP : Direction.DOWN);
                });
            }
        });

        return super.onBuildActionWrongState(state, skipOtherGuide);
    }

    public static Map<Direction, Vec3> getSlabSides(Level world, BlockPos pos, SlabType requiredHalf) {
        if (requiredHalf == SlabType.DOUBLE) {
            requiredHalf = SlabType.BOTTOM;
        }
        Direction requiredDir = requiredHalf == SlabType.TOP ? Direction.UP : Direction.DOWN;
        Map<Direction, Vec3> sides = new HashMap<>();
        sides.put(requiredDir, new Vec3(0, 0, 0));
        if (world.getBlockState(pos).hasProperty(SlabBlock.TYPE)) {
            sides.put(requiredDir.getOpposite(), Vec3.atLowerCornerOf(DirectionUtils.getVector(requiredDir)).scale(0.5));
        }
        for (Direction side : Direction.Plane.HORIZONTAL) {
            BlockState neighborCurrentState = world.getBlockState(pos.relative(side));
            if (neighborCurrentState.hasProperty(SlabBlock.TYPE) && neighborCurrentState.getValue(SlabBlock.TYPE) != SlabType.DOUBLE) {
                if (neighborCurrentState.getValue(SlabBlock.TYPE) != requiredHalf) {
                    continue;
                }
            }
            sides.put(side, Vec3.atLowerCornerOf(DirectionUtils.getVector(requiredDir)).scale(0.25));
        }
        return sides;
    }
}