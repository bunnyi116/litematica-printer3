package me.aleksilassila.litematica.printer.render;

import me.aleksilassila.litematica.printer.config.Configs;
import me.aleksilassila.litematica.printer.config.enums.WorkMode;
import me.aleksilassila.litematica.printer.module.Module;
import me.aleksilassila.litematica.printer.module.ModuleManager;
import me.aleksilassila.litematica.printer.module.Modules;
import me.aleksilassila.litematica.printer.module.modules.GuiModule;
import me.aleksilassila.litematica.printer.module.modules.PrintModule;
import me.aleksilassila.litematica.printer.printer.SchematicBlockContext;
import me.aleksilassila.litematica.printer.utils.ConfigUtils;
import me.aleksilassila.litematica.printer.utils.render.Render2DUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.item.ItemStack;

import java.awt.*;
import java.util.HashSet;

public class RenderHud {
    public final static RenderHud INSTANCE = new RenderHud();

    public void render(float scaledWidth, float scaledHeight) {
        if (Configs.Core.RENDER_HUD.getBooleanValue()) {
            drawHudInfo(scaledWidth, scaledHeight);
        }
    }

    private void drawHudInfo(float scaledWidth, float scaledHeight) {
        int centerX = (int) (scaledWidth / 2);
        int centerY = (int) (scaledHeight / 2);
        GuiModule guiModule = Modules.GUI;

        // ====================== 统一 Y 基准（核心改动） ======================
        int y = centerY;

        // 1. 延迟过大警告（向上偏移）
        if (Configs.Core.LAG_CHECK.getBooleanValue() &&
                ModuleManager.INSTANCE.getReceivePacketCount() > Configs.Core.LAG_CHECK_MAX.getIntegerValue()) {
            y += 22;
            Render2DUtils.drawString("延迟过大，已暂停运行", centerX, y - 22, Color.ORANGE, true, true);
        }

        // 2. 单模式进度百分比（向下偏移）
        WorkMode workMode = (WorkMode) Configs.Core.WORK_MODE.getOptionListValue();
        if (workMode.equals(WorkMode.SINGLE)) {
            y += 22; // 百分比位置
            double progress = guiModule.getTotalProgress().getProgress();
            Render2DUtils.drawString((int) (progress * 100) + "%", centerX, y, Color.WHITE, true, true);

            y += 14; // 进度条偏移
            drawProgressBar(centerX, y, 40, 6, progress, new Color(0, 0, 0, 150), new Color(0, 255, 0, 255));
        }

        // 3. 模式名称（向下偏移）
        y += 16;
        if (ConfigUtils.isSingleMode()) {
            String modeName = Configs.Core.WORK_MODE_TYPE.getOptionListValue().getDisplayName();
            Render2DUtils.drawString(modeName, centerX, y, Color.WHITE, true, true);
        } else {
            HashSet<String> modeNames = new HashSet<>();
            for (Module handler : Modules.VALUES) {
                if (handler.getId().equals(GuiModule.NAME) ||
                        handler.getEnable() == null ||
                        !handler.getEnable().getBooleanValue()) {
                    continue;
                }
                modeNames.add(handler.getEnable().getPrettyName());
            }
            Render2DUtils.drawString(String.join(", ", modeNames), centerX, y, Color.WHITE, true, true);
        }


        PrintModule printModule = Modules.PRINT;
        SchematicBlockContext printContext = printModule.getContext();
        if (printContext != null) {
            Minecraft mc = Minecraft.getInstance();
            ClientLevel level = mc.level;
            if (level != null) {
                y += 24;
                ItemStack itemStack = printContext.requiredState.getBlock().asItem().getDefaultInstance();
                Render2DUtils.drawItem(itemStack, centerX - 8, y - 8);
            }
        }
    }

    private void drawProgressBar(int x, int y, int barWidth, int barHeight, double progress,
                                 Color bgColor, Color fgColor) {
        double clampedProgress = Math.clamp(progress, 0.0, 1.0);
        int barXStart = x - (barWidth / 2);
        int barXEnd = x + (barWidth / 2);
        int barYEnd = y + barHeight;
        int filledWidth = (int) (clampedProgress * barWidth);

        Render2DUtils.fill(barXStart, y, barXEnd, barYEnd, bgColor);
        if (filledWidth > 0) {
            Render2DUtils.fill(barXStart, y, barXStart + filledWidth, barYEnd, fgColor);
        }
    }
}
