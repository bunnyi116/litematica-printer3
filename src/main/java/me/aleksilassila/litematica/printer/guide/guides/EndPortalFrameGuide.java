package me.aleksilassila.litematica.printer.guide.guides;

import me.aleksilassila.litematica.printer.config.Configs;
import me.aleksilassila.litematica.printer.enums.BlockMatchResult;
import me.aleksilassila.litematica.printer.guide.Guide;
import me.aleksilassila.litematica.printer.guide.Result;
import me.aleksilassila.litematica.printer.printer.SchematicBlockContext;
import me.aleksilassila.litematica.printer.printer.action.ClickAction;
import me.aleksilassila.litematica.printer.utils.InteractionUtils;
import net.minecraft.world.level.block.EndPortalFrameBlock;
import net.minecraft.world.item.Items;

/**
 * 末地传送门框架
 */
public class EndPortalFrameGuide extends Guide {

    public EndPortalFrameGuide(SchematicBlockContext context) {
        super(context);
    }

    @Override
    protected Result onBuildActionWrongState(BlockMatchResult state) {
        boolean requiredHasEye = getProperty(requiredState, EndPortalFrameBlock.HAS_EYE).orElseThrow();
        boolean currentHasEye = getProperty(currentState, EndPortalFrameBlock.HAS_EYE).orElseThrow();
        if (requiredHasEye && !currentHasEye) {
            return Result.success(new ClickAction().setItem(Items.ENDER_EYE));
        }
        if (Configs.Print.BREAK_WRONG_STATE_BLOCK.getBooleanValue()) {
            InteractionUtils.INSTANCE.add(context);
        }
        return Result.SKIP;
    }
}
