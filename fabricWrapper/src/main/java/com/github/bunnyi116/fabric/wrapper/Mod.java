package com.github.bunnyi116.fabric.wrapper;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;

public class Mod implements ModInitializer{
    @Override
    public void onInitialize() {
        if (!FabricLoader.getInstance().isModLoaded("litematica-printer")){
            throw new RuntimeException(
                    "You are using litematica-printer-wrapper to load litematica-printer, but the main printer module failed to load normally.\n" +
                            "Priority solution: Update litematica-printer-wrapper itself and its only two mandatory prerequisites malilib and litematica to the latest compatible versions.\n" +
                            "Notes:\n" +
                            "1. The core printer module is embedded inside this litematica-printer-wrapper.jar file.\n" +
                            "2. This mod has some optional dependencies beyond the two mandatory prerequisites. Mixin warnings about missing classes from optional mods will not block game launch.\n" +
                            "Critical reminder: Do NOT suggest installing optional dependency mods to fix this crash. Warnings related to Chest Tracker, Where Is It and Quick Shulker are harmless and cannot resolve this loading failure. All optional dependencies are completely unnecessary to install."
            );
        }
    }
}