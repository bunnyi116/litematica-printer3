package me.aleksilassila.litematica.printer.guide.blocks;

import me.aleksilassila.litematica.printer.config.Configs;
import me.aleksilassila.litematica.printer.enums.BlockMatchResult;
import me.aleksilassila.litematica.printer.guide.Guide;
import me.aleksilassila.litematica.printer.printer.SchematicBlockContext;
import me.aleksilassila.litematica.printer.printer.action.Action;
import me.aleksilassila.litematica.printer.printer.action.ClickAction;
import me.aleksilassila.litematica.printer.utils.InteractionUtils;
import net.minecraft.world.level.block.RepeaterBlock;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 红石中继器
 */
public class RepeaterGuide extends Guide {

    public RepeaterGuide(SchematicBlockContext context) {
        super(context);
    }

    @Override
    protected Optional<Action> onBuildActionWrongState(BlockMatchResult state, AtomicReference<Boolean> skipOtherGuide) {
        if (!requiredState.getValue(RepeaterBlock.DELAY).equals(currentState.getValue(RepeaterBlock.DELAY))) {
            return Optional.of(new ClickAction());
        }
        // POWERED 和 LOCKED 都相同时，无法通过交互修正，按配置破坏
        if (Configs.Print.BREAK_WRONG_STATE_BLOCK.getBooleanValue()
                && requiredState.getValue(RepeaterBlock.POWERED) == currentState.getValue(RepeaterBlock.POWERED)
                && requiredState.getValue(RepeaterBlock.LOCKED) == currentState.getValue(RepeaterBlock.LOCKED)) {
            InteractionUtils.INSTANCE.add(context);
        }
        return Optional.empty();
    }
}
