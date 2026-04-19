package me.aleksilassila.litematica.printer.guide.blocks;

import me.aleksilassila.litematica.printer.enums.BlockMatchResult;
import me.aleksilassila.litematica.printer.guide.Guide;
import me.aleksilassila.litematica.printer.printer.SchematicBlockContext;
import me.aleksilassila.litematica.printer.printer.action.Action;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 可可豆放置指南。
 * 注册到：CocoaBlock.class
 *
 * <p>规则：点击 HORIZONTAL_FACING 方向的面放置。
 */
public class CocoaGuide extends Guide {

    public CocoaGuide(SchematicBlockContext context) {
        super(context);
    }

    @Override
    protected Optional<Action> onBuildActionMissingBlock(BlockMatchResult state, AtomicReference<Boolean> skipOtherGuide) {
        var cocoaFacing = getProperty(requiredState, BlockStateProperties.HORIZONTAL_FACING).orElse(null);
        if (cocoaFacing == null) return Optional.empty();
        return Optional.of(new Action().setSides(cocoaFacing));
    }
}
