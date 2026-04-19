package me.aleksilassila.litematica.printer.guide.blocks;

import me.aleksilassila.litematica.printer.config.Configs;
import me.aleksilassila.litematica.printer.enums.BlockMatchResult;
import me.aleksilassila.litematica.printer.guide.Guide;
import me.aleksilassila.litematica.printer.printer.SchematicBlockContext;
import me.aleksilassila.litematica.printer.printer.action.Action;
import me.aleksilassila.litematica.printer.printer.action.ClickAction;
import me.aleksilassila.litematica.printer.utils.InteractionUtils;
import net.minecraft.world.level.block.EndPortalFrameBlock;
import net.minecraft.world.item.Items;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 末地传送门框架交互指南。
 * 注册到：EndPortalFrameBlock.class
 *
 * <p>WRONG_STATE：需要嵌入末影之眼 → 右键嵌入。
 */
public class EndPortalFrameGuide extends Guide {

    public EndPortalFrameGuide(SchematicBlockContext context) {
        super(context);
    }

    @Override
    protected Optional<Action> onBuildActionWrongState(BlockMatchResult state, AtomicReference<Boolean> skipOtherGuide) {
        if (requiredState.getValue(EndPortalFrameBlock.HAS_EYE) && !currentState.getValue(EndPortalFrameBlock.HAS_EYE)) {
            return Optional.of(new ClickAction().setItem(Items.ENDER_EYE));
        }
        if (Configs.Print.BREAK_WRONG_STATE_BLOCK.getBooleanValue()) {
            InteractionUtils.INSTANCE.add(context);
        }
        return Optional.empty();
    }
}
