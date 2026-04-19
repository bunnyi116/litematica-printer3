package me.aleksilassila.litematica.printer.guide.blocks;

import me.aleksilassila.litematica.printer.enums.BlockMatchResult;
import me.aleksilassila.litematica.printer.guide.Guide;
import me.aleksilassila.litematica.printer.config.Configs;
import me.aleksilassila.litematica.printer.printer.SchematicBlockContext;
import me.aleksilassila.litematica.printer.printer.action.Action;
import me.aleksilassila.litematica.printer.utils.InteractionUtils;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 告示牌放置指南。
 * 注册到：AbstractSignBlock.class（覆盖站立/墙壁/悬挂三类）
 *
 * <p>放置规则：
 * <ul>
 *   <li>站立告示牌（StandingSignBlock）→ 点击下方，按 rotation 设置偏转角</li>
 *   <li>墙壁告示牌（WallSignBlock）→ 点击对应墙面，玩家朝向与面一致</li>
 *   <li>悬挂告示牌（WallHangingSignBlock）→ 按轴方向点击侧面</li>
 *   <li>天花板悬挂（CeilingHangingSignBlock）→ 点击上方，按 rotation 旋转</li>
 * </ul>
 */
public class SignGuide extends Guide {

    public SignGuide(SchematicBlockContext context) {
        super(context);
    }

    @Override
    protected Optional<Action> onBuildActionMissingBlock(BlockMatchResult state, AtomicReference<Boolean> skipOtherGuide) {
        Block block = requiredBlock;

        // 站立告示牌
        if (block instanceof StandingSignBlock) {
            int rotation = requiredState.getValue(StandingSignBlock.ROTATION);
            return Optional.of(new Action()
                    .setSides(Direction.DOWN)
                    .setLookRotation(rotation)
                    .setRequiresSupport());
        }

        // 墙壁告示牌
        if (block instanceof WallSignBlock && facing != null) {
            return Optional.of(new Action()
                    .setSides(facing.getOpposite())
                    .setLookDirection(facing.getOpposite())
                    .setRequiresSupport());
        }

        //#if MC >= 12002
        // 墙壁悬挂告示牌（WallHangingSignBlock）
        if (block instanceof WallHangingSignBlock && facing != null) {
            List<Direction> sides = facing.getAxis() == Direction.Axis.X
                    ? List.of(Direction.NORTH, Direction.SOUTH)
                    : List.of(Direction.EAST, Direction.WEST);
            return Optional.of(new Action()
                    .setSides(sides.toArray(new Direction[0]))
                    .setLookDirection(facing.getOpposite())
                    .setRequiresSupport());
        }

        // 天花板悬挂告示牌（CeilingHangingSignBlock）
        if (block instanceof CeilingHangingSignBlock) {
            int rotation = requiredState.getValue(CeilingHangingSignBlock.ROTATION);
            boolean attached = requiredState.getValue(BlockStateProperties.ATTACHED);
            return Optional.of(new Action()
                    .setShift(attached)
                    .setSides(Direction.UP)
                    .setLookRotation(rotation)
                    .setRequiresSupport());
        }
        //#endif

        return Optional.empty();
    }

    @Override
    protected Optional<Action> onBuildActionWrongBlock(BlockMatchResult state, AtomicReference<Boolean> skipOtherGuide) {
        if (Configs.Print.BREAK_WRONG_BLOCK.getBooleanValue() && InteractionUtils.canBreakBlock(blockPos)) {
            boolean isLegitimateSign = currentBlock instanceof StandingSignBlock
                    || currentBlock instanceof WallSignBlock
                    //#if MC >= 12002
                    || currentBlock instanceof WallHangingSignBlock
                    || currentBlock instanceof CeilingHangingSignBlock
                    //#endif
                    ;
            if (!isLegitimateSign) {
                InteractionUtils.INSTANCE.add(context);
            }
        }
        return Optional.empty();
    }
}
