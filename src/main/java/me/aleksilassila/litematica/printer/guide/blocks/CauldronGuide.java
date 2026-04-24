package me.aleksilassila.litematica.printer.guide.blocks;

import me.aleksilassila.litematica.printer.config.Configs;
import me.aleksilassila.litematica.printer.enums.BlockMatchResult;
import me.aleksilassila.litematica.printer.guide.Guide;
import me.aleksilassila.litematica.printer.guide.Result;
import me.aleksilassila.litematica.printer.printer.SchematicBlockContext;
import me.aleksilassila.litematica.printer.printer.action.ClickAction;
import me.aleksilassila.litematica.printer.utils.InteractionUtils;
import me.aleksilassila.litematica.printer.utils.InventoryUtils;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.item.Items;

import java.util.Optional;

/**
 * 炼药锅
 */
public class CauldronGuide extends Guide {

    public CauldronGuide(SchematicBlockContext context) {
        super(context);
    }

    @Override
    protected Result onBuildActionWrongState(BlockMatchResult state) {
        Optional<Integer> currentLevel = getProperty(currentState, LayeredCauldronBlock.LEVEL);
        Optional<Integer> requiredLevel = getProperty(requiredState, LayeredCauldronBlock.LEVEL);

        if (currentLevel.isEmpty() || requiredLevel.isEmpty()) {
            return Result.SKIP;
        }

        if (currentLevel.get() > requiredLevel.get()) {
            if (InventoryUtils.playerHasAccessToItem(client.player, Items.GLASS_BOTTLE)) {
                return Result.success(new ClickAction().setItem(Items.GLASS_BOTTLE));
            }
        }
        if (currentLevel.get() < requiredLevel.get()) {
            if (InventoryUtils.playerHasAccessToItem(client.player, Items.POTION)) {
                return Result.success(new ClickAction().setItem(Items.POTION));
            }
        }
        return Result.SKIP;
    }

    @Override
    protected Result onBuildActionWrongBlock(BlockMatchResult state) {
        if (Configs.Print.BREAK_WRONG_BLOCK.getBooleanValue()
                && InteractionUtils.canBreakBlock(blockPos)) {
            InteractionUtils.INSTANCE.add(context);
        }
        return Result.SKIP;
    }
}
