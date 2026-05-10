package me.aleksilassila.litematica.printer.guide.guides;

import me.aleksilassila.litematica.printer.config.Configs;
import me.aleksilassila.litematica.printer.enums.BlockMatchResult;
import me.aleksilassila.litematica.printer.guide.Guide;
import me.aleksilassila.litematica.printer.guide.Result;
import me.aleksilassila.litematica.printer.printer.SchematicBlockContext;
import me.aleksilassila.litematica.printer.printer.action.Action;
import me.aleksilassila.litematica.printer.utils.CooldownUtils;
import me.aleksilassila.litematica.printer.utils.InteractionUtils;
import me.aleksilassila.litematica.printer.utils.InventoryUtils;
import me.aleksilassila.litematica.printer.utils.minecraft.BlockStateUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.IceBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;

import java.util.HashMap;
import java.util.Map;

/**
 * 水源/含水方块处理指南。
 */
@SuppressWarnings("BooleanMethodIsAlwaysInverted")
public class WaterGuide extends Guide {
    public WaterGuide(SchematicBlockContext context) {
        super(context);
    }

    @Override
    protected boolean canExecute() {
        return BlockStateUtils.isWaterBlock(requiredState);
    }

    @Override
    protected Result onBuildAction(BlockMatchResult state) {
        if (client.gameMode == null || client.gameMode.getPlayerMode().isCreative()) {
            return Result.PASS;
        }

        // 跳过打印含水方块
        if (Configs.Print.SKIP_WATERLOGGED_BLOCK.getBooleanValue()) {
            return Result.SKIP;
        }

        // 破冰放水
        if (Configs.Print.PRINT_ICE_FOR_WATER.getBooleanValue()) {
            if (BlockStateUtils.isCorrectWaterLevel(requiredState, currentState)) {
                return Result.PASS;
            }
            if (!canIceBecomeWaterSource()) {
                return Result.SKIP;
            }
            // 冰块已经放置, 那么添加到破坏队列等待破坏同时跳过其他指南处理
            if (currentBlock instanceof IceBlock) {
                InteractionUtils.INSTANCE.add(context);
                return Result.SKIP;
            }
            // 玩家有足够的冰块情况下才使用破冰放水
            if (InventoryUtils.playerHasAccessToItem(client.player, Items.ICE)) {
                return Result.success(new Action().setItem(Items.ICE));
            }
            return Result.SKIP;
        }
        return Result.PASS;
    }

    private boolean canIceBecomeWaterSource() {
        BlockPos belowPos = blockPos.below();
        BlockState belowState = level.getBlockState(belowPos);
        //#if MC > 11904
        return !belowState.getCollisionShape(level, belowPos, CollisionContext.empty()).isEmpty()
                || !belowState.getFluidState().isEmpty();
        //#else
        //$$ return belowState.getMaterial().blocksMotion() || belowState.getMaterial().isLiquid();
        //#endif
    }
}
