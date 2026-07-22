package me.aleksilassila.litematica.printer.guide.guides;

import me.aleksilassila.litematica.printer.guide.BlockMatchResult;
import me.aleksilassila.litematica.printer.guide.Guide;
import me.aleksilassila.litematica.printer.guide.Result;
import me.aleksilassila.litematica.printer.printer.SchematicBlockContext;
import me.aleksilassila.litematica.printer.action.ClickAction;
import net.minecraft.world.level.block.DaylightDetectorBlock;

/**
 * 阳光探测器交互指南
 */
public class DaylightDetectorGuide extends Guide {

    public DaylightDetectorGuide(SchematicBlockContext context) {
        super(context);
    }

    @Override
    protected Result onBuildActionErrorState(BlockMatchResult state) {
        int requiredPower = getProperty(requiredState, DaylightDetectorBlock.POWER).orElseThrow();
        int currentPower = getProperty(currentState, DaylightDetectorBlock.POWER).orElseThrow();
        boolean requiredInverted = getProperty(requiredState, DaylightDetectorBlock.INVERTED).orElseThrow();
        boolean currentInverted = getProperty(currentState, DaylightDetectorBlock.INVERTED).orElseThrow();

        // POWER 由光照强度决定，无法修正
        if (requiredPower != currentPower) {
            return Result.skipOtherGuide();
        }
        // POWER 相同但 INVERTED 不同 → 右键切换
        if (requiredInverted != currentInverted) {
            return Result.success(new ClickAction());
        }
        return Result.skipOtherGuide();
    }
}
