package me.aleksilassila.litematica.printer.guide.blocks;

import me.aleksilassila.litematica.printer.enums.BlockMatchResult;
import me.aleksilassila.litematica.printer.guide.Guide;
import me.aleksilassila.litematica.printer.printer.SchematicBlockContext;
import me.aleksilassila.litematica.printer.printer.action.Action;
import me.aleksilassila.litematica.printer.printer.action.ClickAction;
import me.aleksilassila.litematica.printer.utils.InteractionUtils;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.item.Items;
import net.minecraft.network.chat.Component;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 炼药锅
 */
public class CauldronGuide extends Guide {

    public CauldronGuide(SchematicBlockContext context) {
        super(context);
    }

    @Override
    protected Optional<Action> onBuildActionWrongState(BlockMatchResult state, AtomicReference<Boolean> skipOtherGuide) {
        if (!currentState.hasProperty(LayeredCauldronBlock.LEVEL)
                || !requiredState.hasProperty(LayeredCauldronBlock.LEVEL)) {
            return Optional.empty();
        }

        int currentLevel = currentState.getValue(LayeredCauldronBlock.LEVEL);
        int requiredLevel = requiredState.getValue(LayeredCauldronBlock.LEVEL);

        if (currentLevel > requiredLevel) {
            if (me.aleksilassila.litematica.printer.utils.InventoryUtils.playerHasAccessToItem(client.player, Items.GLASS_BOTTLE)) {
                return Optional.of(new ClickAction().setItem(Items.GLASS_BOTTLE));
            }
        }
        if (currentLevel < requiredLevel) {
            if (me.aleksilassila.litematica.printer.utils.InventoryUtils.playerHasAccessToItem(client.player, Items.POTION)) {
                return Optional.of(new ClickAction().setItem(Items.POTION));
            }
        }
        return Optional.empty();
    }

    @Override
    protected Optional<Action> onBuildActionWrongBlock(BlockMatchResult state, AtomicReference<Boolean> skipOtherGuide) {
        if (me.aleksilassila.litematica.printer.config.Configs.Print.BREAK_WRONG_BLOCK.getBooleanValue()
                && InteractionUtils.canBreakBlock(blockPos)) {
            InteractionUtils.INSTANCE.add(context);
        }
        return Optional.empty();
    }
}
