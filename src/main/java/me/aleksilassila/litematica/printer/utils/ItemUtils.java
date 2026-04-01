package me.aleksilassila.litematica.printer.utils;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;

public class ItemUtils {
    public static Component getNameFromItem(Item item) {
        //#if MC >= 260001
        return item.getName(item.getDefaultInstance());
        //#elseif MC > 12101
        //$$ return item.getName();
        //#else
        //$$ return item.getDescription();
        //#endif
    }
}
