package me.aleksilassila.litematica.printer.guide.blocks;

import me.aleksilassila.litematica.printer.enums.BlockMatchResult;
import me.aleksilassila.litematica.printer.guide.Guide;
import me.aleksilassila.litematica.printer.printer.SchematicBlockContext;
import me.aleksilassila.litematica.printer.printer.action.Action;
import me.aleksilassila.litematica.printer.printer.action.ClickAction;
import net.minecraft.world.level.block.LeverBlock;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 拉杆
 */
public class LeverGuide extends Guide {

    public LeverGuide(SchematicBlockContext context) {
        super(context);
    }

    @Override
    protected Optional<Action> onBuildActionWrongState(BlockMatchResult state, AtomicReference<Boolean> skipOtherGuide) {
        // POWERED 可通过右键切换
        if (requiredState.getValue(LeverBlock.POWERED) != currentState.getValue(LeverBlock.POWERED)) {
            return Optional.of(new ClickAction());
        }
        // facing/attachFace 等放置属性不对 → 放置性错误，交给 DefaultGuide 破坏重放
        return Optional.empty();
    }
}
