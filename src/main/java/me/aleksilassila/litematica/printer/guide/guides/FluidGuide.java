package me.aleksilassila.litematica.printer.guide.guides;

import me.aleksilassila.litematica.printer.config.Configs;
import me.aleksilassila.litematica.printer.guide.BlockMatchResult;
import me.aleksilassila.litematica.printer.guide.Guide;
import me.aleksilassila.litematica.printer.guide.Result;
import me.aleksilassila.litematica.printer.printer.SchematicBlockContext;
import me.aleksilassila.litematica.printer.action.Action;
import me.aleksilassila.litematica.printer.utils.ConfigUtils;
import me.aleksilassila.litematica.printer.utils.InteractionUtils;
import me.aleksilassila.litematica.printer.utils.minecraft.BlockStateUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.IceBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.shapes.CollisionContext;

import java.util.Optional;

/**
 * 流体。
 */
@SuppressWarnings("BooleanMethodIsAlwaysInverted")
public class FluidGuide extends Guide {
    public FluidGuide(SchematicBlockContext context) {
        super(context);
    }

    @Override
    protected boolean canExecute() {
        return BlockStateUtils.isWaterBlock(requiredState) || requiredState.is(Blocks.LAVA);
    }

    @Override
    protected Result onBuildAction(BlockMatchResult state) {
        // 不处理岩浆打印, 跳过
        if (requiredState.is(Blocks.LAVA)) {
            return Result.skipOtherGuide();
        }
        // 玩家可跳过含水方块打印
        if (Configs.Print.SKIP_WATERLOGGED_BLOCK.getBooleanValue()) {
            return Result.skipOtherGuide();
        }
        // 创造跳过
        if (client.gameMode == null || client.gameMode.getPlayerMode().isCreative()) {
            return Result.passToNext();
        }
        // 破冰放水逻辑
        if (Configs.Print.PRINT_ICE_FOR_WATER.getBooleanValue() && BlockStateUtils.isWaterSource(requiredState)) {
            if (isOnCooldown()) {
                return Result.skipOtherGuide().setIterationNextBlockPos(blockPos);
            }
            if (isCorrectWaterLevel(requiredState, currentState)) {
                if (BlockStateUtils.isReplaceable(currentState)) {
                    return Result.passToNext().setIterationNextBlockPos(blockPos);
                }
                return Result.passToNext();
            }
            if (!canIceMeltIntoWaterSource(level, blockPos)) {
                return Result.skipOtherGuide().setIterationNextBlockPos(blockPos);
            }
            if (currentBlock instanceof IceBlock) {
                if (!InteractionUtils.INSTANCE.contains(blockPos)) {
                    InteractionUtils.INSTANCE.add(context);
                    setCooldown(4);
                }
                return Result.skipOtherGuide().setIterationNextBlockPos(blockPos);
            }
            if (BlockStateUtils.isReplaceable(currentState)) {
                return Result.success(new Action().setItem(Items.ICE)).setIterationNextBlockPos(blockPos);
            }
        }
        return Result.passToNext();
    }

    @Override
    protected Result onBuildActionErrorBlock(BlockMatchResult state) {
        if (Configs.Print.BREAK_WRONG_BLOCK.getBooleanValue() && requiredBlock != currentBlock) {
            InteractionUtils.INSTANCE.add(context);
        }
        return Result.skipOtherGuide();
    }

    @Override
    protected Result onBuildActionErrorState(BlockMatchResult state) {
        return Result.skipOtherGuide();
    }

    /**
     * 判断冰块在此位置是否能成水源
     */
    private boolean canIceMeltIntoWaterSource(BlockGetter level, BlockPos pos) {
        BlockPos belowPos = pos.below();
        BlockState belowState = level.getBlockState(belowPos);
        // 1. 下方是完整固体方块（能挡住水）
        boolean isSolidBelow = !belowState.getCollisionShape(level, belowPos, CollisionContext.empty()).isEmpty();
        // 2. 下方是液体（水/熔岩等）
        boolean isFluidBelow = !belowState.getFluidState().isEmpty();
        // 3. 满足任意一个 = 冰可以融化成水
        return isSolidBelow || isFluidBelow;
    }

    /**
     * 判断当前位置是否已满足"有水"条件。
     * 含水方块（WATERLOGGED=true）视为已满足，不需要再破冰放水。
     * 优化：使用 Optional 工具方法消除重复的属性检查
     */
    public static boolean isCorrectWaterLevel(BlockState requiredState, BlockState currentState) {
        // 目标是纯液体水方块
        if (requiredState.is(Blocks.WATER)) {
            // 如果当前也是水方块，精确比较水位；否则只要有水源即可
            return currentState.is(Blocks.WATER)
                    ? getProperty(requiredState, BlockStateProperties.LEVEL).equals(getProperty(currentState, BlockStateProperties.LEVEL))
                    : isWaterSource(currentState);
        }

        // 请求是需要水
        if (BlockStateUtils.isWaterSource(requiredState)) {
            return BlockStateUtils.isWaterSource(currentState);
        }

        // 两个方块都有 WATERLOGGED 属性
        Optional<Boolean> requiredWaterlogged = getProperty(requiredState, BlockStateProperties.WATERLOGGED);
        Optional<Boolean> currentWaterlogged = getProperty(currentState, BlockStateProperties.WATERLOGGED);
        if (requiredWaterlogged.isPresent() && currentWaterlogged.isPresent()) {
            if (!BlockStateUtils.isWaterBlock(requiredState) || !BlockStateUtils.isWaterBlock(requiredState)) {
                return requiredWaterlogged.get().equals(currentWaterlogged.get());
            }
        }
        if (requiredWaterlogged.orElse(false)) {
            return isWaterSource(currentState);
        }

        // 其他情况：目标不需要水，当前位置也不能有水
        return !isWaterBlock(currentState);
    }
}