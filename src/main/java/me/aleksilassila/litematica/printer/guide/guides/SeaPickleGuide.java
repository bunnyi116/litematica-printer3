package me.aleksilassila.litematica.printer.guide.guides;

import me.aleksilassila.litematica.printer.config.Configs;
import me.aleksilassila.litematica.printer.guide.BlockMatchResult;
import me.aleksilassila.litematica.printer.guide.Guide;
import me.aleksilassila.litematica.printer.guide.Result;
import me.aleksilassila.litematica.printer.printer.SchematicBlockContext;
import me.aleksilassila.litematica.printer.action.Action;
import me.aleksilassila.litematica.printer.action.ClickAction;
import me.aleksilassila.litematica.printer.utils.InteractionUtils;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.SeaPickleBlock;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

/**
 * 海泡菜
 */
public class SeaPickleGuide extends Guide {

    public SeaPickleGuide(SchematicBlockContext context) {
        super(context);
    }

    @Override
    protected Result onBuildActionMissingBlock(BlockMatchResult state) {
        // 读取原理图海泡菜是否要求浸水
        boolean schematicNeedWater = getProperty(requiredState, SeaPickleBlock.WATERLOGGED).orElse(false);
        FluidState realWorldFluid = currentState.getFluidState();
        boolean realHasWaterSource = realWorldFluid.is(Fluids.WATER) && realWorldFluid.getAmount() == 8;
        // 原理图需要浸水 && 当前世界没有满源水 → 先生成放水动作，再摆放海泡菜
        if (schematicNeedWater && !realHasWaterSource) {
            return Result.skipOtherGuide();
        }
        // 不需要水 / 已经有水：正常放置海泡菜
        // 海泡菜只能放在支撑方块上，需要从下方点击放置
        return Result.success(new Action()
                .setSides(Direction.DOWN)
                .setNeedSupportBlock());
    }

    @Override
    protected Result onBuildActionErrorState(BlockMatchResult state) {
        if (currentState.getBlock() instanceof SeaPickleBlock) {
            int currentPickles = getProperty(currentState, SeaPickleBlock.PICKLES).orElse(1);
            int requiredPickles = getProperty(requiredState, SeaPickleBlock.PICKLES).orElse(1);
            if (currentPickles < requiredPickles) {
                return Result.success(new ClickAction().setItem(Items.SEA_PICKLE));
            }
            if (Configs.Print.BREAK_WRONG_STATE_BLOCK.getBooleanValue()) {
                InteractionUtils.INSTANCE.add(context);
            }
        }
        return Result.skipOtherGuide();
    }
}
