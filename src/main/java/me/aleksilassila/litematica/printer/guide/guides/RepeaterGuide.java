package me.aleksilassila.litematica.printer.guide.guides;

import me.aleksilassila.litematica.printer.config.Configs;
import me.aleksilassila.litematica.printer.enums.BlockMatchResult;
import me.aleksilassila.litematica.printer.guide.Guide;
import me.aleksilassila.litematica.printer.guide.Result;
import me.aleksilassila.litematica.printer.printer.SchematicBlockContext;
import me.aleksilassila.litematica.printer.printer.action.ClickAction;
import me.aleksilassila.litematica.printer.utils.InteractionUtils;
import net.minecraft.world.level.block.RepeaterBlock;

/**
 * 红石中继器
 */
public class RepeaterGuide extends Guide {

    public RepeaterGuide(SchematicBlockContext context) {
        super(context);
    }

    @Override
    protected Result onBuildActionWrongState(BlockMatchResult state) {
        if (!getProperty(requiredState, RepeaterBlock.DELAY).equals(getProperty(currentState, RepeaterBlock.DELAY))) {
            return Result.success(new ClickAction());
        }
        // POWERED 和 LOCKED 都相同时，无法通过交互修正，按配置破坏
        if (Configs.Print.BREAK_WRONG_STATE_BLOCK.getBooleanValue()
                && getProperty(requiredState, RepeaterBlock.POWERED).equals(getProperty(currentState, RepeaterBlock.POWERED))
                && getProperty(requiredState, RepeaterBlock.LOCKED).equals(getProperty(currentState, RepeaterBlock.LOCKED))) {
            InteractionUtils.INSTANCE.add(context);
        }
        return Result.SKIP;
    }
}
