package me.aleksilassila.litematica.printer.guide.blocks;

import me.aleksilassila.litematica.printer.enums.BlockMatchResult;
import me.aleksilassila.litematica.printer.guide.Guide;
import me.aleksilassila.litematica.printer.printer.SchematicBlockContext;
import me.aleksilassila.litematica.printer.printer.action.Action;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.portal.PortalShape;
import net.minecraft.world.item.Items;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 下界传送门放置指南。
 * 注册到：NetherPortalBlock.class
 *
 * <p>MISSING：检查是否有空传送门形状，有则用打火石/火焰弹点燃。
 */
public class NetherPortalGuide extends Guide {

    public NetherPortalGuide(SchematicBlockContext context) {
        super(context);
    }

    @Override
    protected Optional<Action> onBuildActionMissingBlock(BlockMatchResult state, AtomicReference<Boolean> skipOtherGuide) {
        boolean canCreatePortal = PortalShape.findEmptyPortalShape(level, blockPos, net.minecraft.core.Direction.Axis.X).isPresent();
        if (canCreatePortal) {
            return Optional.of(new Action()
                    .setItems(Items.FLINT_AND_STEEL, Items.FIRE_CHARGE)
                    .setRequiresSupport());
        }
        return Optional.empty();
    }
}
