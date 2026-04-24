package me.aleksilassila.litematica.printer.guide.blocks;

import me.aleksilassila.litematica.printer.config.Configs;
import me.aleksilassila.litematica.printer.enums.BlockMatchResult;
import me.aleksilassila.litematica.printer.guide.Guide;
import me.aleksilassila.litematica.printer.guide.Result;
import me.aleksilassila.litematica.printer.printer.SchematicBlockContext;
import me.aleksilassila.litematica.printer.printer.action.ClickAction;
import me.aleksilassila.litematica.printer.utils.InteractionUtils;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

/**
 * 花簇
 */
//#if MC >= 11904
public class FlowerBedGuide extends Guide {

    public FlowerBedGuide(SchematicBlockContext context) {
        super(context);
    }

    @Override
    protected Result onBuildActionWrongState(BlockMatchResult state) {
        int requiredAmount = getProperty(requiredState, BlockStateProperties.FLOWER_AMOUNT).orElse(1);
        int currentAmount = getProperty(currentState, BlockStateProperties.FLOWER_AMOUNT).orElse(1);
        if (currentAmount <= requiredAmount) {
            return Result.success(new ClickAction().setItem(requiredBlock.asItem()));
        }
        if (Configs.Print.BREAK_WRONG_STATE_BLOCK.getBooleanValue()) {
            InteractionUtils.INSTANCE.add(context);
        }
        return Result.SKIP;
    }
}
//#endif
