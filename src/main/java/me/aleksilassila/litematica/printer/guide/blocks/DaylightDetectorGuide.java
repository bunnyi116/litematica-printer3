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
 * 阳光探测器交互指南
 */
public class DaylightDetectorGuide extends Guide {

    public DaylightDetectorGuide(SchematicBlockContext context) {
        super(context);
    }

    @Override
    protected Optional<Action> onBuildActionWrongState(BlockMatchResult state, AtomicReference<Boolean> skipOtherGuide) {
        // POWER 由光照强度决定，无法修正，跳过
        if (!requiredState.getValue(DaylightDetectorBlock.POWER).equals(currentState.getValue(DaylightDetectorBlock.POWER))) {
            skipOtherGuide.set(true);
            return Optional.empty();
        }
        // INVERTED 可通过右键切换
        if (requiredState.getValue(DaylightDetectorBlock.INVERTED) != currentState.getValue(DaylightDetectorBlock.INVERTED)) {
            return Optional.of(new ClickAction());
        }
        return Optional.empty();
    }
}
