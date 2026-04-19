package me.aleksilassila.litematica.printer.guide.blocks;

import me.aleksilassila.litematica.printer.enums.BlockMatchResult;
import me.aleksilassila.litematica.printer.guide.Guide;
import me.aleksilassila.litematica.printer.printer.SchematicBlockContext;
import me.aleksilassila.litematica.printer.printer.action.Action;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 漏斗放置指南。
 * 注册到：HopperBlock.class
 *
 * <p>规则：点击 facing 方向的面放置。
 */
public class HopperGuide extends Guide {

    public HopperGuide(SchematicBlockContext context) {
        super(context);
    }

    @Override
    protected Optional<Action> onBuildActionMissingBlock(BlockMatchResult state, AtomicReference<Boolean> skipOtherGuide) {
        var hopperFacing = getProperty(requiredState, HopperBlock.FACING).orElse(null);
        if (hopperFacing == null) return Optional.empty();
        return Optional.of(new Action().setSides(hopperFacing));
    }
}
