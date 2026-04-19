package me.aleksilassila.litematica.printer.guide.blocks;

import me.aleksilassila.litematica.printer.enums.BlockMatchResult;
import me.aleksilassila.litematica.printer.guide.Guide;
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
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 门放置 / 交互指南。
 * 注册到：DoorBlock.class
 *
 * <p>放置规则：
 * <ul>
 *   <li>只处理门的下半部分（LOWER），上半部分随下半自动生成</li>
 *   <li>根据铰链方向和周围方块占用情况确定放置侧面和点击偏移</li>
 * </ul>
 *
 * <p>交互规则（WRONG_STATE）：
 * <ul>
 *   <li>铁门不可交互</li>
 *   <li>开关状态不同时，发送右键点击使其改变</li>
 *   <li>朝向不同时，根据配置决定是否破坏</li>
 * </ul>
 */
public class DoorGuide extends Guide {

    /** 门的铰链侧：DOOR_HINGE */
    private final @Nullable DoorHingeSide doorHinge;

    /** 门/床的上/下段：DOUBLE_BLOCK_HALF */
    private final @Nullable DoubleBlockHalf doubleBlockHalf;

    public DoorGuide(SchematicBlockContext context) {
        super(context);
        this.doorHinge = getProperty(requiredState, BlockStateProperties.DOOR_HINGE).orElse(null);
        this.doubleBlockHalf = getProperty(requiredState, BlockStateProperties.DOUBLE_BLOCK_HALF).orElse(null);
    }

    @Override
    protected Optional<Action> onBuildActionMissingBlock(BlockMatchResult state, AtomicReference<Boolean> skipOtherGuide) {
        if (facing == null || doorHinge == null || doubleBlockHalf == null) return Optional.empty();

        // 只放置门的下半，上半由游戏自动生成
        if (doubleBlockHalf == DoubleBlockHalf.UPPER) return Optional.empty();

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
                && leftState.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.LOWER;
        boolean isRightDoor = rightState.getBlock() instanceof DoorBlock
                && rightState.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.LOWER;

        boolean canPlace = (doorHinge == DoorHingeSide.RIGHT && ((isLeftDoor && !isRightDoor) || occupancy > 0))
                || (doorHinge == DoorHingeSide.LEFT && ((isRightDoor && !isLeftDoor) || occupancy < 0))
                || (occupancy == 0 && (isLeftDoor == isRightDoor));

        if (canPlace) {
            return Optional.of(new Action().setSides(sides).setLookDirection(facing).setRequiresSupport());
        }
        return Optional.empty();
    }

    @Override
    protected Optional<Action> onBuildActionWrongState(BlockMatchResult state, AtomicReference<Boolean> skipOtherGuide) {
        // 铁门 / 铁活板门无法手动交互
        if (requiredState.is(Blocks.IRON_DOOR) || requiredState.is(Blocks.IRON_TRAPDOOR)) {
            return Optional.empty();
        }
        // 开关状态不一致 → 右键点击切换
        if (requiredState.getValue(BlockStateProperties.OPEN) != currentState.getValue(BlockStateProperties.OPEN)) {
            return Optional.of(new ClickAction());
        }
        // 朝向不一致 → 根据配置决定是否破坏
        if (Configs.Print.BREAK_WRONG_STATE_BLOCK.getBooleanValue()
                && facing != null
                && facing != getProperty(currentState, BlockStateProperties.FACING)
                        .or(() -> getProperty(currentState, BlockStateProperties.HORIZONTAL_FACING))
                        .orElse(null)) {
            me.aleksilassila.litematica.printer.utils.InteractionUtils.INSTANCE.add(context);
        }
        return Optional.empty();
    }
}
