package me.aleksilassila.litematica.printer.guide.blocks;

import me.aleksilassila.litematica.printer.enums.BlockMatchResult;
import me.aleksilassila.litematica.printer.guide.Guide;
import me.aleksilassila.litematica.printer.guide.Result;
import me.aleksilassila.litematica.printer.printer.SchematicBlockContext;
import me.aleksilassila.litematica.printer.printer.action.Action;
import me.aleksilassila.litematica.printer.printer.action.ClickAction;
import me.aleksilassila.litematica.printer.config.Configs;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * 门放置
 */
public class DoorGuide extends Guide {

    /**
     * 门的铰链侧：DOOR_HINGE
     */
    private final @Nullable DoorHingeSide doorHinge;

    /**
     * 门/床的上/下段：DOUBLE_BLOCK_HALF
     */
    private final @Nullable DoubleBlockHalf doubleBlockHalf;

    public DoorGuide(SchematicBlockContext context) {
        super(context);
        this.doorHinge = getProperty(requiredState, BlockStateProperties.DOOR_HINGE).orElse(null);
        this.doubleBlockHalf = getProperty(requiredState, BlockStateProperties.DOUBLE_BLOCK_HALF).orElse(null);
    }

    @Override
    protected Result onBuildActionMissingBlock(BlockMatchResult state) {
        Direction facing = getProperty(requiredState, DoorBlock.FACING).orElse(null);
        if (facing == null || doorHinge == null || doubleBlockHalf == null) return Result.PASS;

        // 只放置门的下半，上半由游戏自动生成
        if (doubleBlockHalf == DoubleBlockHalf.UPPER) return Result.PASS;

        BlockPos upperPos = blockPos.above();

        // 铰链侧
        Direction hingeSide = facing.getCounterClockWise();
        double offset = doorHinge == DoorHingeSide.RIGHT ? 0.25 : -0.25;
        Vec3 hingeVec = facing.getAxis() == Direction.Axis.X
                ? new Vec3(0, 0, offset)
                : new Vec3(offset, 0, 0);

        Map<Direction, Vec3> sides = new HashMap<>();
        sides.put(hingeSide, Vec3.ZERO);
        sides.put(Direction.DOWN, hingeVec);
        sides.put(facing, hingeVec);

        // 检查左右方块占用情况，决定是否可以放置
        Direction left = facing.getCounterClockWise();
        Direction right = facing.getClockWise();
        BlockState leftState = level.getBlockState(blockPos.relative(left));
        BlockState leftUpperState = level.getBlockState(upperPos.relative(left));
        BlockState rightState = level.getBlockState(blockPos.relative(right));
        BlockState rightUpperState = level.getBlockState(upperPos.relative(right));

        int occupancy = (leftState.isCollisionShapeFullBlock(level, blockPos.relative(left)) ? -1 : 0)
                + (leftUpperState.isCollisionShapeFullBlock(level, upperPos.relative(left)) ? -1 : 0)
                + (rightState.isCollisionShapeFullBlock(level, blockPos.relative(right)) ? 1 : 0)
                + (rightUpperState.isCollisionShapeFullBlock(level, upperPos.relative(right)) ? 1 : 0);

        boolean isLeftDoor = leftState.getBlock() instanceof DoorBlock
                && getProperty(leftState, BlockStateProperties.DOUBLE_BLOCK_HALF).orElse(null) == DoubleBlockHalf.LOWER;
        boolean isRightDoor = rightState.getBlock() instanceof DoorBlock
                && getProperty(rightState, BlockStateProperties.DOUBLE_BLOCK_HALF).orElse(null) == DoubleBlockHalf.LOWER;

        boolean canPlace = (doorHinge == DoorHingeSide.RIGHT && ((isLeftDoor && !isRightDoor) || occupancy > 0))
                || (doorHinge == DoorHingeSide.LEFT && ((isRightDoor && !isLeftDoor) || occupancy < 0))
                || (occupancy == 0 && (isLeftDoor == isRightDoor));

        return Result.resultIf(canPlace,
                new Action().setSides(sides).setLookDirection(facing).setRequiresSupport());
    }

    @Override
    protected Result onBuildActionWrongState(BlockMatchResult state) {
        // 铁门 / 铁活板门无法手动交互
        if (requiredState.is(Blocks.IRON_DOOR) || requiredState.is(Blocks.IRON_TRAPDOOR)) {
            return Result.SKIP;
        }
        // 开关状态不一致 → 右键点击切换
        if (!getProperty(requiredState, BlockStateProperties.OPEN).equals(getProperty(currentState, BlockStateProperties.OPEN))) {
            return Result.success(new ClickAction());
        }
        // 朝向不一致 → 根据配置决定是否破坏
        Direction facing = getProperty(requiredState, DoorBlock.FACING).orElse(null);
        if (Configs.Print.BREAK_WRONG_STATE_BLOCK.getBooleanValue()
                && facing != null
                && facing != getProperty(currentState, BlockStateProperties.FACING)
                .or(() -> getProperty(currentState, BlockStateProperties.HORIZONTAL_FACING))
                .orElse(null)) {
            me.aleksilassila.litematica.printer.utils.InteractionUtils.INSTANCE.add(context);
        }
        return Result.SKIP;
    }
}
