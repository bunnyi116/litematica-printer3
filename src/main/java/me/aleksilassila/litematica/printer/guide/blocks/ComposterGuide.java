package me.aleksilassila.litematica.printer.guide.blocks;

import me.aleksilassila.litematica.printer.Reference;
import me.aleksilassila.litematica.printer.config.Configs;
import me.aleksilassila.litematica.printer.enums.BlockMatchResult;
import me.aleksilassila.litematica.printer.guide.Guide;
import me.aleksilassila.litematica.printer.printer.SchematicBlockContext;
import me.aleksilassila.litematica.printer.printer.action.Action;
import me.aleksilassila.litematica.printer.printer.action.ClickAction;
import me.aleksilassila.litematica.printer.utils.FilterUtils;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * 堆肥桶
 */
public class ComposterGuide extends Guide {
    private static List<String> compostWhitelistCache = new ArrayList<>();
    private static Item[] whitelistItemsCache = new Item[0];

    public ComposterGuide(SchematicBlockContext context) {
        super(context);
    }

    @Override
    protected java.util.Optional<Action> onBuildActionWrongState(BlockMatchResult state, java.util.concurrent.atomic.AtomicReference<Boolean> skipOtherGuide) {
        if (!Configs.Print.FILL_COMPOSTER.getBooleanValue()) return java.util.Optional.empty();
        if (!currentState.hasProperty(ComposterBlock.LEVEL) || !requiredState.hasProperty(ComposterBlock.LEVEL)) {
            return java.util.Optional.empty();
        }

        int currentLevel = currentState.getValue(ComposterBlock.LEVEL);
        int requiredLevel = requiredState.getValue(ComposterBlock.LEVEL);

        if (currentLevel >= requiredLevel) return java.util.Optional.empty();

        List<String> whitelist = Configs.Print.FILL_COMPOSTER_WHITELIST.getStrings();
        if (!whitelist.equals(compostWhitelistCache)) {
            compostWhitelistCache = new ArrayList<>(whitelist);
            List<Item> whitelistItems = new ArrayList<>();
            for (Item item : Reference.COMPOSTABLE_ITEMS) {
                for (String rule : whitelist) {
                    if (FilterUtils.matchName(rule, new ItemStack(item))) {
                        whitelistItems.add(item);
                        break;
                    }
                }
            }
            whitelistItemsCache = whitelistItems.toArray(Item[]::new);
        }

        Item[] finalItems = whitelistItemsCache.length > 0 ? whitelistItemsCache : Reference.COMPOSTABLE_ITEMS;
        if (finalItems.length > 0) {
            return java.util.Optional.of(new ClickAction().setItems(finalItems));
        }
        return java.util.Optional.empty();
    }
}
