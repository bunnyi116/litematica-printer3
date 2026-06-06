package me.aleksilassila.litematica.printer.render;

import me.aleksilassila.litematica.printer.config.Configs;

/**
 * 统一的 2D 渲染管理器，负责所有调试信息和 HUD 的绘制。
 * 由 GuiMixin 在每帧调用 render() 方法触发。
 */
public class Render2D {
    public static final Render2D INSTANCE = new Render2D();

    private static final int DEBUG_PADDING = 4;

    private Render2D() {
    }

    /**
     * 主渲染入口，由 Mixin 每帧调用。
     * 注意：调用前必须已通过 Render2DUtils.initGuiGraphics 或 initMatrix 设置好渲染上下文。
     */
    public void render(float scaledWidth, float scaledHeight) {
        if (Configs.Core.RENDER_HUD.getBooleanValue()) {
            RenderHud.INSTANCE.render(scaledWidth, scaledHeight);
        }
    }
}