package me.aleksilassila.litematica.printer.guide.blocks;

import me.aleksilassila.litematica.printer.enums.BlockMatchResult;
import me.aleksilassila.litematica.printer.guide.Guide;
import me.aleksilassila.litematica.printer.printer.SchematicBlockContext;
import me.aleksilassila.litematica.printer.printer.action.Action;
import me.aleksilassila.litematica.printer.printer.action.ClickAction;
import net.minecraft.world.level.block.DaylightDetectorBlock;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 阳光探测器交互指南。
 * 注册到：DaylightDetectorBlock.class
 *
 * <p>WRONG_STATE：反转状态不同 → 右键切换。
 */
public class DaylightDetectorGuide extends Guide {

    public DaylightDetectorGuide(SchematicBlockContext context) {
        super(context);
    }

    @Override
    protected Optional<Action> onBuildActionWrongState(BlockMatchResult state, AtomicReference<Boolean> skipOtherGuide) {
        if (requiredState.getValue(DaylightDetectorBlock.INVERTED) != currentState.getValue(DaylightDetectorBlock.INVERTED)) {
            return Optional.of(new ClickAction());
        }
        return Optional.empty();
    }
}
