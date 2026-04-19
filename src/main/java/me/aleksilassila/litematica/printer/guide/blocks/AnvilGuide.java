package me.aleksilassila.litematica.printer.guide.blocks;

import me.aleksilassila.litematica.printer.enums.BlockMatchResult;
import me.aleksilassila.litematica.printer.guide.Guide;
import me.aleksilassila.litematica.printer.printer.SchematicBlockContext;
import me.aleksilassila.litematica.printer.printer.action.Action;
import net.minecraft.world.level.block.AnvilBlock;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 铁砧放置指南。
 * 注册到：AnvilBlock.class
 *
 * <p>规则：玩家看逆时针方向放置（铁砧朝向 fancing.getCounterClockWise() 的反方向）。
 */
public class AnvilGuide extends Guide {

    public AnvilGuide(SchematicBlockContext context) {
        super(context);
    }

    @Override
    protected Optional<Action> onBuildActionMissingBlock(BlockMatchResult state, AtomicReference<Boolean> skipOtherGuide) {
        var anvilFacing = getProperty(requiredState, AnvilBlock.FACING).orElse(null);
        if (anvilFacing == null) return Optional.empty();
        return Optional.of(new Action().setLookDirection(anvilFacing.getCounterClockWise()));
    }
}
