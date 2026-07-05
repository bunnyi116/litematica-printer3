package me.aleksilassila.litematica.printer.utils.minecraft;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Optional;

@SuppressWarnings("EnhancedSwitchMigration")
public class BlockStateUtils extends BlockUtils {

    public static Comparable<?> getPropertyByName(BlockState state, String name) {
        for (Property<?> prop : state.getProperties()) {
            if (prop.getName().equalsIgnoreCase(name)) {
                return state.getValue(prop);
            }
        }
        return null;
    }

    public static boolean statesEqualIgnoreProperties(BlockState state1, BlockState state2, Property<?>... propertiesToIgnore) {
        if (state1.getBlock() != state2.getBlock()) {
            return false;
        }
        loop:
        for (Property<?> property : state1.getProperties()) {
            if (property == BlockStateProperties.WATERLOGGED && !(state1.getBlock() instanceof CoralPlantBlock)) {
                continue;
            }
            for (Property<?> ignoredProperty : propertiesToIgnore) {
                if (property == ignoredProperty) {
                    continue loop;
                }
            }
            try {
                if (!state1.getValue(property).equals(state2.getValue(property))) {
                    return false;
                }
            } catch (Exception e) {
                return false;
            }
        }
        return true;
    }

    public static <T extends Comparable<T>> Optional<T> getProperty(BlockState blockState, Property<T> property) {
        if (blockState.hasProperty(property)) {
            return Optional.of(blockState.getValue(property));
        }
        return Optional.empty();
    }

    public static boolean statesEqual(BlockState state1, BlockState state2) {
        return statesEqualIgnoreProperties(state1, state2);
    }

    protected static boolean canBeClicked(Level world, BlockPos pos) {
        return getOutlineShape(world, pos) != Shapes.empty();
    }

    private static VoxelShape getOutlineShape(Level level, BlockPos pos) {
        return level.getBlockState(pos).getShape(level, pos);
    }

    private static VoxelShape getOutlineShape(BlockState state, Level level, BlockPos pos) {
        return state.getShape(level, pos);
    }

    public static Optional<Property<?>> getWallFacingProperty(Direction wallFacing) {
        switch (wallFacing) {
            case NORTH:
                return Optional.of(BlockStateProperties.NORTH_WALL);
            case SOUTH:
                return Optional.of(BlockStateProperties.SOUTH_WALL);
            case WEST:
                return Optional.of(BlockStateProperties.WEST_WALL);
            case EAST:
                return Optional.of(BlockStateProperties.EAST_WALL);
        }
        return Optional.empty();
    }

    public static Optional<Property<?>> getCrossCollisionBlock(Direction wallFacing) {
        switch (wallFacing) {
            case NORTH:
                return Optional.of(BlockStateProperties.NORTH);
            case SOUTH:
                return Optional.of(BlockStateProperties.SHORT);
            case WEST:
                return Optional.of(BlockStateProperties.WEST);
            case EAST:
                return Optional.of(BlockStateProperties.EAST);
        }
        return Optional.empty();
    }

    // ==================== 重构后的流体判断方法 ====================

    /**
     * 判断该方块是否包含任何形式的水（完全兼容原方法）
     * 包括：水源方块、流动水方块、含水方块、气泡柱
     *
     * @param blockState 要判断的方块状态
     * @return true 如果包含水
     */
    public static boolean isWaterBlock(BlockState blockState) {
        // 使用官方标准的 FluidState 判断，自动包含所有情况
        return blockState.getFluidState().is(FluidTags.WATER);
    }

    /**
     * 判断是否是水源方块（包括含水方块中的水源）
     * 注意：原方法只判断纯水源方块，现在已扩展为包含含水方块
     * 如果需要原行为，请使用 isPureWaterSource()
     *
     * @param blockState 要判断的方块状态
     * @return true 如果是水源
     */
    public static boolean isWaterSource(BlockState blockState) {
        FluidState fluidState = blockState.getFluidState();
        return fluidState.is(FluidTags.WATER) && fluidState.isSource();
    }

    /**
     * 判断是否是纯水源方块（不包括含水方块）
     * 这是原 isWaterSource() 方法的行为
     *
     * @param blockState 要判断的方块状态
     * @return true 如果是纯水源方块
     */
    public static boolean isPureWaterSource(BlockState blockState) {
        FluidState fluidState = blockState.getFluidState();
        return fluidState.isSource()
                && fluidState.is(FluidTags.WATER)
                && !blockState.hasProperty(BlockStateProperties.WATERLOGGED);
    }

    /**
     * 判断是否是流动水（包括水平流动和垂直下落）
     *
     * @param blockState 要判断的方块状态
     * @return true 如果是流动水
     */
    public static boolean isFlowingWater(BlockState blockState) {
        return blockState.getFluidState().getType() == Fluids.FLOWING_WATER;
    }

    /**
     * 判断是否是垂直下落的水
     *
     * @param blockState 要判断的方块状态
     * @return true 如果是垂直下落的水
     */
    public static boolean isFallingWater(BlockState blockState) {
        if (!blockState.is(Blocks.WATER)) return false;
        int levelValue = blockState.getValue(BlockStateProperties.LEVEL);
        return levelValue >= 8;
    }

    /**
     * 判断是否是含水方块（WATERLOGGED=true）
     *
     * @param blockState 要判断的方块状态
     * @return true 如果是含水方块
     */
    public static boolean isWaterlogged(BlockState blockState) {
        return blockState.hasProperty(BlockStateProperties.WATERLOGGED)
                && blockState.getValue(BlockStateProperties.WATERLOGGED);
    }

    public static boolean requiresWaterToPlace(Block block) {
        return block instanceof SeagrassBlock
                || block instanceof KelpBlock
                || block instanceof KelpPlantBlock;
    }

    // ==================== 新增：岩浆相关判断方法 ====================

    /**
     * 判断该方块是否包含任何形式的岩浆
     * 包括：源岩浆方块、流动岩浆方块
     *
     * @param blockState 要判断的方块状态
     * @return true 如果包含岩浆
     */
    public static boolean isLavaBlock(BlockState blockState) {
        return blockState.getFluidState().is(FluidTags.LAVA);
    }

    /**
     * 判断是否是源岩浆方块
     *
     * @param blockState 要判断的方块状态
     * @return true 如果是源岩浆
     */
    public static boolean isLavaSource(BlockState blockState) {
        FluidState fluidState = blockState.getFluidState();
        return fluidState.is(FluidTags.LAVA) && fluidState.isSource();
    }

    /**
     * 判断是否是流动岩浆
     *
     * @param blockState 要判断的方块状态
     * @return true 如果是流动岩浆
     */
    public static boolean isFlowingLava(BlockState blockState) {
        return blockState.getFluidState().getType() == Fluids.FLOWING_LAVA;
    }

    /**
     * 判断是否是任何液体（水或岩浆）
     *
     * @param blockState 要判断的方块状态
     * @return true 如果是液体
     */
    public static boolean isAnyLiquid(BlockState blockState) {
        FluidState fluidState = blockState.getFluidState();
        return fluidState.is(FluidTags.WATER) || fluidState.is(FluidTags.LAVA);
    }
}