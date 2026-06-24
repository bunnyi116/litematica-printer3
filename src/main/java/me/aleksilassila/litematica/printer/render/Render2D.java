package me.aleksilassila.litematica.printer.render;

import me.aleksilassila.litematica.printer.config.Configs;

public class Render2D {
    public final static Render2D INSTANCE = new Render2D();

    private final static int DEBUG_PADDING = 4;

    private Render2D() {
    }

    public void render(float scaledWidth, float scaledHeight) {
        if (Configs.Core.RENDER_HUD.getBooleanValue()) {
            RenderHud.INSTANCE.render(scaledWidth, scaledHeight);
        }
    }
}