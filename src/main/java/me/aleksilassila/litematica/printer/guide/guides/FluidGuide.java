package me.aleksilassila.litematica.printer.guide.guides;

import me.aleksilassila.litematica.printer.config.Configs;
import me.aleksilassila.litematica.printer.enums.BlockMatchResult;
import me.aleksilassila.litematica.printer.guide.Guide;
import me.aleksilassila.litematica.printer.guide.Result;
import me.aleksilassila.litematica.printer.printer.SchematicBlockContext;
import me.aleksilassila.litematica.printer.printer.action.Action;
import me.aleksilassila.litematica.printer.utils.InteractionUtils;
import me.aleksilassila.litematica.printer.utils.minecraft.BlockStateUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.IceBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;

/**
 * 流体统一处理指南。
 * <ul>
 *   <li>水源：破冰放水</li>
 *   <li>含水方块：水已满足时 PASS 给块指南</li>
 *   <li>熔岩：暂跳过</li>
 *   <li>水生植物：检查水环境，无水则跳过</li>
 * </ul>
 */
@SuppressWarnings("BooleanMethodIsAlwaysInverted")
public class FluidGuide extends Guide {
    private static final int ICE_PLACE_COOLDOWN = 40;

    public FluidGuide(SchematicBlockContext context) {
        super(context);
    }

    @Override
    protected boolean canExecute() {
        return BlockStateUtils.isWaterBlock(requiredState) || requiredState.is(Blocks.LAVA);
    }

    @Override
    protected Result onBuildAction(BlockMatchResult state) {
        if (requiredState.is(Blocks.LAVA)) {
            return Result.SKIP;
        }
        if (Configs.Print.SKIP_WATERLOGGED_BLOCK.getBooleanValue()) {
            return Result.SKIP;
        }
        if (client.gameMode == null || client.gameMode.getPlayerMode().isCreative()) {
            return Result.PASS;
        }
        if (Configs.Print.PRINT_ICE_FOR_WATER.getBooleanValue()) {
            if (BlockStateUtils.isCorrectWaterLevel(requiredState, currentState)) {
                return Result.PASS;
            }
            if (!canIceMeltIntoWaterSource(level, blockPos)) {
                return Result.SKIP;
            }
            if (currentBlock instanceof IceBlock) {
                InteractionUtils.INSTANCE.add(context);
                return Result.SKIP;
            }
            return Result.success(new Action().setItem(Items.ICE));
        }
        return Result.PASS;
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
}