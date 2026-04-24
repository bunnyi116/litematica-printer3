package me.aleksilassila.litematica.printer.guide.blocks;

import me.aleksilassila.litematica.printer.config.Configs;
import me.aleksilassila.litematica.printer.enums.BlockMatchResult;
import me.aleksilassila.litematica.printer.guide.Guide;
import me.aleksilassila.litematica.printer.guide.Result;
import me.aleksilassila.litematica.printer.printer.SchematicBlockContext;
import me.aleksilassila.litematica.printer.printer.action.Action;
import me.aleksilassila.litematica.printer.utils.minecraft.BlockStateUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.CrossCollisionBlock;
import net.minecraft.world.level.block.ObserverBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
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

        // 安全放置模式
        SchematicBlockContext input = context.offset(facing);          // 输入端（侦测面）
        SchematicBlockContext output = context.offset(facing.getOpposite()); // 输出端（红点面）

        // 获取输入端方块需要忽略的属性
        List<Property<?>> inputPropertiesToIgnore = new ArrayList<>();
        if (input.requiredState.getBlock() instanceof WallBlock) {
            BlockStateUtils.getWallFacingProperty(facing.getOpposite()).ifPresent(inputPropertiesToIgnore::add);
        }
        if (output.requiredState.getBlock() instanceof CrossCollisionBlock) {
            BlockStateUtils.getCrossCollisionBlock(facing.getOpposite()).ifPresent(inputPropertiesToIgnore::add);
        }

        BlockMatchResult inputState = BlockMatchResult.compare(input, inputPropertiesToIgnore.toArray(new Property<?>[0]));
        BlockMatchResult outputState = BlockMatchResult.compare(output);

        // 输入端与输出端均正确
        if (inputState == BlockMatchResult.CORRECT && outputState == BlockMatchResult.CORRECT) {
            // 检查输入端是否是侦测器链
            SchematicBlockContext temp = input;
            while (temp.requiredState.getBlock() instanceof ObserverBlock) {
                Direction tempFacing = temp.requiredState.hasProperty(net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING)
                        ? temp.requiredState.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING) : null;
                if (tempFacing != null) {
                    SchematicBlockContext offset = temp.offset(tempFacing);
                    if (BlockMatchResult.compare(offset) != BlockMatchResult.CORRECT) {
                        return Result.SKIP;
                    }
                    temp = offset;
                } else {
                    break;
                }
            }
            return Result.success(new Action().setLookDirection(facing));
        }

        // 输入端正确但输出端有问题
        if (inputState == BlockMatchResult.CORRECT) {
            // 检查输入端后面的落地方块链
            SchematicBlockContext temp = input;
            while (temp.requiredState.getBlock() instanceof net.minecraft.world.level.block.FallingBlock) {
                SchematicBlockContext offset = temp.offset(Direction.DOWN);
                if (BlockMatchResult.compare(offset) != BlockMatchResult.CORRECT) {
                    return Result.SKIP;
                }
                temp = offset;
            }
            if (!output.requiredState.isAir()) {
                // 检查输入端侦测器链
                temp = input;
                while (temp.requiredState.getBlock() instanceof ObserverBlock) {
                    Direction tempFacing = temp.requiredState.hasProperty(net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING)
                            ? temp.requiredState.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING) : null;
                    if (tempFacing != null) {
                        SchematicBlockContext offset = temp.offset(tempFacing);
                        if (BlockMatchResult.compare(offset) != BlockMatchResult.CORRECT) {
                            return Result.SKIP;
                        }
                        temp = offset;
                    } else {
                        break;
                    }
                }
            }

            // 侦测器隔空激活活塞检查
            for (Direction direction : Direction.values()) {
                SchematicBlockContext offset = output.offset(direction);
                if (offset.blockPos.equals(output.blockPos)) continue;
                if (offset.blockPos.equals(input.blockPos)) continue;
                if (offset.blockPos.equals(blockPos)) continue;
                if (offset.requiredState.getBlock() instanceof PistonBaseBlock) {
                    if (!offset.currentState.isAir()) {
                        return Result.SKIP;
                    }
                }
            }
        } else if (inputState == BlockMatchResult.WRONG_STATE) {
            return Result.SKIP;
        } else {
            if (!output.requiredState.isAir()) {
                return Result.SKIP;
            }
        }

        return Result.success(new Action().setLookDirection(facing));
    }

    @Override
    protected Result onBuildActionWrongState(BlockMatchResult state) {
        return Result.SKIP;
    }
}
