package me.aleksilassila.litematica.printer.guide.guides;

import me.aleksilassila.litematica.printer.config.Configs;
import me.aleksilassila.litematica.printer.enums.BlockMatchResult;
import me.aleksilassila.litematica.printer.guide.Guide;
import me.aleksilassila.litematica.printer.guide.Result;
import me.aleksilassila.litematica.printer.printer.SchematicBlockContext;
import me.aleksilassila.litematica.printer.printer.action.Action;
import me.aleksilassila.litematica.printer.printer.action.ClickAction;
import me.aleksilassila.litematica.printer.utils.InteractionUtils;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.SeaPickleBlock;
import net.minecraft.world.item.Items;

/**
 * 海泡菜
 */
public class SeaPickleGuide extends Guide {

    public SeaPickleGuide(SchematicBlockContext context) {
        super(context);
    }

    @Override
    protected Result onBuildActionMissingBlock(BlockMatchResult state) {
        // 海泡菜只能放在支撑方块上（珊瑚块/海晶石等），需要从下方点击放置
        return Result.success(new Action()
                .setSides(Direction.DOWN)
                .setRequiresSupport());
    }

    @Override
    protected Result onBuildActionWrongState(BlockMatchResult state) {
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
        return Result.SKIP;
    }
}
