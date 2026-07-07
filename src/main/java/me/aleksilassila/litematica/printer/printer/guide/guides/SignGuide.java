package me.aleksilassila.litematica.printer.printer.guide.guides;

import me.aleksilassila.litematica.printer.printer.guide.BlockMatchResult;
import me.aleksilassila.litematica.printer.printer.guide.Guide;
import me.aleksilassila.litematica.printer.printer.guide.Result;
import me.aleksilassila.litematica.printer.printer.SchematicBlockContext;
import me.aleksilassila.litematica.printer.printer.action.Action;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.List;

/**
 * 告示牌
 */
public class SignGuide extends Guide {

    public SignGuide(SchematicBlockContext context) {
        super(context);
    }

    @Override
    protected Result onBuildActionMissingBlock(BlockMatchResult state) {
        Direction facing = getProperty(requiredState, HorizontalDirectionalBlock.FACING).orElse(null);

        // 站立告示牌
        if (requiredBlock instanceof StandingSignBlock) {
            int rotation = getProperty(requiredState, StandingSignBlock.ROTATION).orElseThrow();
            return Result.success(new Action()
                    .setSides(Direction.DOWN)
                    .setLookRotation(rotation)
                    .setNeedSupportBlock());
        }

        // 墙壁告示牌
        if (requiredBlock instanceof WallSignBlock && facing != null) {
            return Result.success(new Action()
                    .setSides(facing.getOpposite())
                    .setLookDirection(facing.getOpposite())
                    .setNeedSupportBlock());
        }

        //#if MC >= 12002
        // 墙壁悬挂告示牌（WallHangingSignBlock）
        if (requiredBlock instanceof WallHangingSignBlock && facing != null) {
            List<Direction> sides = facing.getAxis() == Direction.Axis.X
                    ? List.of(Direction.NORTH, Direction.SOUTH)
                    : List.of(Direction.EAST, Direction.WEST);
            return Result.success(new Action()
                    .setSides(sides.toArray(new Direction[0]))
                    .setLookDirection(facing.getOpposite())
                    .setNeedSupportBlock());
        }

        // 天花板悬挂告示牌（CeilingHangingSignBlock）
        if (requiredBlock instanceof CeilingHangingSignBlock) {
            int rotation = getProperty(requiredState, CeilingHangingSignBlock.ROTATION).orElse((int) 0);
            boolean attached = getProperty(requiredState, BlockStateProperties.ATTACHED).orElse(false);
            return Result.success(new Action()
                    .setSneak(attached)
                    .setSides(Direction.UP)
                    .setLookRotation(rotation)
                    .setNeedSupportBlock());
        }
        //#endif

        return Result.skipOtherGuide();
    }

    @Override
    protected Result onBuildActionWrongBlock(BlockMatchResult state) {
        return Result.skipOtherGuide();
    }

    @Override
    protected Result onBuildActionWrongState(BlockMatchResult state) {
        return Result.skipOtherGuide();
    }
}
