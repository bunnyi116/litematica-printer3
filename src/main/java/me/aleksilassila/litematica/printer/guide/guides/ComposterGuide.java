package me.aleksilassila.litematica.printer.guide.guides;

import me.aleksilassila.litematica.printer.Reference;
import me.aleksilassila.litematica.printer.config.Configs;
import me.aleksilassila.litematica.printer.enums.BlockMatchResult;
import me.aleksilassila.litematica.printer.guide.Guide;
import me.aleksilassila.litematica.printer.guide.Result;
import me.aleksilassila.litematica.printer.printer.SchematicBlockContext;
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
    protected Result onBuildActionWrongState(BlockMatchResult state) {
        if (!Configs.Print.FILL_COMPOSTER.getBooleanValue()) return Result.PASS;
        if (!currentState.hasProperty(ComposterBlock.LEVEL) || !requiredState.hasProperty(ComposterBlock.LEVEL)) {
            return Result.SKIP;
        }

        int currentLevel = getProperty(currentState, ComposterBlock.LEVEL).orElse(0);
        int requiredLevel = getProperty(requiredState, ComposterBlock.LEVEL).orElse(0);

        if (currentLevel >= requiredLevel) return Result.PASS;

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
            return Result.success(new ClickAction().setItems(finalItems));
        }
        return Result.SKIP;
    }
}
