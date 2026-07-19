package com.github.bunnyi116.fabric.wrapper;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;

public class Mod implements ModInitializer{
    @Override
    public void onInitialize() {
        if (!FabricLoader.getInstance().isModLoaded("litematica-printer")){
            throw new RuntimeException("Wrapper failed to load core printer module. Please update litematica-printer, malilib and litematica to versions compatible with your current game version.");
        }
    }
}
