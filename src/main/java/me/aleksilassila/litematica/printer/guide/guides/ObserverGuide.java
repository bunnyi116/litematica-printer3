package me.aleksilassila.litematica.printer.guide.guides;

import me.aleksilassila.litematica.printer.config.Configs;
import me.aleksilassila.litematica.printer.guide.BlockMatchResult;
import me.aleksilassila.litematica.printer.guide.Guide;
import me.aleksilassila.litematica.printer.guide.Result;
import me.aleksilassila.litematica.printer.printer.SchematicBlockContext;
import me.aleksilassila.litematica.printer.action.Action;
import me.aleksilassila.litematica.printer.utils.minecraft.BlockStateUtils;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.CrossCollisionBlock;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.ObserverBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.properties.Property;

import java.util.*;

/**
 * 侦测器
 */
public class ObserverGuide extends Guide {
    public ObserverGuide(SchematicBlockContext context) {
        super(context);
    }

    @Override
    protected Result onBuildActionMissingBlock(BlockMatchResult state) {
        Direction facing = getProperty(requiredState, ObserverBlock.FACING).orElseThrow();
        if (!Configs.Print.SAFELY_OBSERVER.getBooleanValue()) {
            return Result.success(new Action().setLookDirection(facing));
        }
        // 输入端方块（侦测面）
        SchematicBlockContext input = context.offset(facing);

        // 获取输入端方块需要忽略的属性
        List<Property<?>> inputPropertiesToIgnore = new ArrayList<>();
        Direction ignoreWallSide = facing.getOpposite();
        if (input.requiredState.getBlock() instanceof WallBlock) {
            if (facing == Direction.DOWN) {
                return Result.success(new Action().setLookDirection(facing));
            }
            SchematicBlockContext offsetUp = input.offset(Direction.UP);
            if (offsetUp.compare() == BlockMatchResult.CORRECT) {
                BlockStateUtils.getWallFacingProperty(ignoreWallSide)
                        .ifPresent(inputPropertiesToIgnore::add);
            }
        }
        if (input.requiredState.getBlock() instanceof CrossCollisionBlock) {
            if (facing == Direction.DOWN) {
                return Result.success(new Action().setLookDirection(facing));
            }
            SchematicBlockContext offsetUp = input.offset(Direction.UP);
            if (offsetUp.compare() == BlockMatchResult.CORRECT) {
                BlockStateUtils.getCrossCollisionBlock(ignoreWallSide)
                        .ifPresent(inputPropertiesToIgnore::add);
            }
        }
        BlockMatchResult inputResult = input.compare(inputPropertiesToIgnore.toArray(new Property[0]));
        if (inputResult != BlockMatchResult.CORRECT) {
            // 针对输出端是空气，允许放置
            SchematicBlockContext output = context.offset(facing.getOpposite());    // 输出端块（红点面）
            if (!output.requiredState.isAir()) {
                //TODO: 正常来讲, 应该还要检查输出端周围是否还有活塞, 因为可能会触发更新导致激活
                return Result.skipOtherGuide();
            }
        }

        // 寻找侦测器链的源头, 检查源头是否已经正确放置
        SchematicBlockContext temp = input;
        while (temp.requiredState.getBlock() instanceof ObserverBlock) {
            Direction tempFacing = temp.requiredState.getValue(ObserverBlock.FACING);
            SchematicBlockContext offset = temp.offset(tempFacing);
            if (BlockMatchResult.compare(offset) != BlockMatchResult.CORRECT) {
                return Result.skipOtherGuide();
            }
            temp = offset;
        }

        // 下落的方块检查
        if (Configs.Placement.FALLING_CHECK.getBooleanValue()) {
            temp = input;
            while (temp.requiredState.getBlock() instanceof FallingBlock) {
                SchematicBlockContext offset = temp.offset(Direction.DOWN);
                if (BlockMatchResult.compare(offset) != BlockMatchResult.CORRECT) {
                    return Result.skipOtherGuide();
                }
                temp = offset;
            }
        }

        return Result.success(new Action().setLookDirection(facing));
    }

    @Override
    protected Result onBuildActionErrorState(BlockMatchResult state) {
        return Result.skipOtherGuide();
    }

}
