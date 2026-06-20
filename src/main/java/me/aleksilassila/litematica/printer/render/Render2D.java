package me.aleksilassila.litematica.printer.render;

import me.aleksilassila.litematica.printer.config.Configs;

public class Render2D {
    public static final Render2D INSTANCE = new Render2D();

    private static final int DEBUG_PADDING = 4;

    private Render2D() {
    }

    public void render(float scaledWidth, float scaledHeight) {
        if (Configs.Core.RENDER_HUD.getBooleanValue()) {
            RenderHud.INSTANCE.render(scaledWidth, scaledHeight);
        }
    }
}