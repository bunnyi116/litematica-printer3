package me.aleksilassila.litematica.printer.guide.blocks;

import me.aleksilassila.litematica.printer.config.Configs;
import me.aleksilassila.litematica.printer.enums.BlockMatchResult;
import me.aleksilassila.litematica.printer.guide.Guide;
import me.aleksilassila.litematica.printer.printer.SchematicBlockContext;
import me.aleksilassila.litematica.printer.printer.action.Action;
import me.aleksilassila.litematica.printer.printer.action.ClickAction;
import me.aleksilassila.litematica.printer.utils.InteractionUtils;
import net.minecraft.world.level.block.LeverBlock;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 拉杆放置/交互指南。
 * 注册到：LeverBlock.class
 *
 * <p>WRONG_STATE：开关状态不同 → 右键点击
 */
public class LeverGuide extends Guide {

    public LeverGuide(SchematicBlockContext context) {
        super(context);
    }

    @Override
    protected Optional<Action> onBuildActionWrongState(BlockMatchResult state, AtomicReference<Boolean> skipOtherGuide) {
        if (requiredState.getValue(LeverBlock.POWERED) != currentState.getValue(LeverBlock.POWERED)) {
            return Optional.of(new ClickAction());
        }
        if (Configs.Print.BREAK_WRONG_STATE_BLOCK.getBooleanValue()) {
            InteractionUtils.INSTANCE.add(context);
        }
        return Optional.empty();
    }
}
