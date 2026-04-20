package me.aleksilassila.litematica.printer.guide.blocks;

import me.aleksilassila.litematica.printer.enums.BlockMatchResult;
import me.aleksilassila.litematica.printer.guide.Guide;
import me.aleksilassila.litematica.printer.printer.SchematicBlockContext;
import me.aleksilassila.litematica.printer.printer.action.Action;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 花放置指南。
 * 注册到：FlowerBlock.class
 *
 * <p>花没有朝向属性，只需点击下方并确保底部有支撑方块。
 */
public class FlowerGuide extends Guide {

    public FlowerGuide(SchematicBlockContext context) {
        super(context);
    }

    @Override
    protected Optional<Action> onBuildActionMissingBlock(BlockMatchResult state, AtomicReference<Boolean> skipOtherGuide) {
        return Optional.of(new Action().setRequiresSupport());
    }
}
