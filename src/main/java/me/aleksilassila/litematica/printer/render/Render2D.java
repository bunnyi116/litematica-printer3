package me.aleksilassila.litematica.printer.render;

import com.mojang.blaze3d.vertex.PoseStack;
import me.aleksilassila.litematica.printer.config.Configs;
import me.aleksilassila.litematica.printer.enums.WorkingModeType;
import me.aleksilassila.litematica.printer.handler.ClientPlayerTickHandler;
import me.aleksilassila.litematica.printer.handler.ClientPlayerTickManager;
import me.aleksilassila.litematica.printer.handler.GuiBlockInfo;
import me.aleksilassila.litematica.printer.handler.handlers.GuiHandler;
import me.aleksilassila.litematica.printer.utils.ConfigUtils;
import me.aleksilassila.litematica.printer.utils.render.Render2DUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

import java.awt.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * 统一的 2D 渲染管理器，负责所有调试信息和 HUD 的绘制。
 * 由 MixinGui 在每帧调用 render() 方法触发。
 */
public class Render2D {
    public static final Render2D INSTANCE = new Render2D();

    private static final int DEBUG_PADDING = 4;
    private static final int DEBUG_LINE_HEIGHT = 12;
    private static final int MIN_COLUMN_WIDTH = 120;
    private static final int SIDE_MARGIN = 10;
    private static final int COLUMN_SPACING = DEBUG_PADDING * 3;
    private static final int COMMON_INFO_OFFSET_Y = 10;

    private Render2D() {
    }

    /**
     * 主渲染入口，由 Mixin 每帧调用。
     * 注意：调用前必须已通过 Render2DUtils.initGuiGraphics 或 initMatrix 设置好渲染上下文。
     */
    public void render(float scaledWidth, float scaledHeight) {
//        ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
//        sword.setDamageValue(100);
//        sword.setCount(64);

//        int y = 50;
//        // 绘制物品图标 + 装饰
//        Render2DUtils.drawItemWithDecorations(sword, 100, y);
//        y += 24;
//        // 如果你只想绘制物品图标本身（不显示数量、耐久条）
//        Render2DUtils.drawItem(sword, 100, y);
//        y += 24;
//        // 绘制方块图标本身
//        Render2DUtils.drawBlock(Blocks.DIAMOND_BLOCK, 100, y);
//        y += 24;
//        // 绘制方块图标，并自动显示数量、耐久条等装饰
//        Render2DUtils.drawBlockWithDecorations(Blocks.CHEST, 100, y);
//        y += 24;
//        // 组合方法
//        Render2DUtils.drawItemWithLabel(sword, 100, y, sword.getItemName().getString(), Color.WHITE, true);

        if (Configs.Core.DEBUG_OUTPUT.getBooleanValue()) {
            drawDebugInfo(scaledWidth, scaledHeight);
        }

        if (Configs.Core.RENDER_HUD.getBooleanValue()) {
            drawHudInfo(scaledWidth, scaledHeight);
        }
    }

    // ==================== 调试信息绘制 ====================

    private void drawDebugInfo(float scaledWidth, float scaledHeight) {
        Minecraft mc = Minecraft.getInstance();
        List<ClientPlayerTickHandler> validHandlers = new ArrayList<>();
        int globalMaxTextWidth = MIN_COLUMN_WIDTH;

        // 1. 收集有效 Handler 并计算全局最大宽度
        for (ClientPlayerTickHandler handler : ClientPlayerTickManager.VALUES) {
            GuiBlockInfo guiInfo = handler.getCurrentRenderGuiBlockInfo();
            if (guiInfo == null) continue;

            validHandlers.add(handler);
            List<String> lines = buildHandlerDebugLines(handler, guiInfo);
            for (String line : lines) {
                String cleanLine = line.replaceAll("§[0-9a-fA-Fklmnor]", "");
                globalMaxTextWidth = Math.max(globalMaxTextWidth, mc.font.width(cleanLine));
            }
        }

        if (validHandlers.isEmpty()) return;

        // 2. 绘制公共信息（左上角）
        int commonInfoBottomY = drawCommonDebugInfo(SIDE_MARGIN, SIDE_MARGIN);

        // 3. 计算布局参数（动态适配屏幕）
        int columnWidth = globalMaxTextWidth + DEBUG_PADDING * 2;
        int maxColumnsPerSide = calculateMaxColumnsPerSide(scaledWidth, columnWidth);
        int availableHeight = (int) (scaledHeight - commonInfoBottomY - COMMON_INFO_OFFSET_Y - SIDE_MARGIN);

        // 4. 优先绘制左侧面板，再绘制右侧
        int drawnHandlers = drawHandlerPanels(
                validHandlers, 0,
                SIDE_MARGIN, commonInfoBottomY + COMMON_INFO_OFFSET_Y,
                columnWidth, maxColumnsPerSide, availableHeight,
                scaledHeight
        );

        // 如果左侧绘制不完，绘制右侧面板
        if (drawnHandlers < validHandlers.size()) {
            int rightStartX = (int) (scaledWidth - SIDE_MARGIN - columnWidth);
            drawHandlerPanels(
                    validHandlers, drawnHandlers,
                    rightStartX, commonInfoBottomY + COMMON_INFO_OFFSET_Y,
                    columnWidth, maxColumnsPerSide, availableHeight,
                    scaledHeight
            );
        }
    }

    private int calculateMaxColumnsPerSide(float scaledWidth, int columnWidth) {
        float centerAreaWidth = scaledWidth * 0.5f;
        float sideAvailableWidth = (scaledWidth - centerAreaWidth) / 2 - SIDE_MARGIN * 2;
        int maxColumns = Math.max(1, (int) (sideAvailableWidth / (columnWidth + COLUMN_SPACING)));
        return Math.min(maxColumns, 3);
    }

    private int drawHandlerPanels(List<ClientPlayerTickHandler> handlers, int startIndex,
                                  int startX, int startY, int columnWidth,
                                  int maxColumns, int availableHeight, float scaledHeight) {
        int drawnCount = 0;
        int currentColumn = 0;
        int currentX = startX;
        int currentY = startY;

        for (int i = startIndex; i < handlers.size(); i++) {
            ClientPlayerTickHandler handler = handlers.get(i);
            GuiBlockInfo guiInfo = handler.getCurrentRenderGuiBlockInfo();
            if (guiInfo == null) continue;

            List<String> debugLines = buildHandlerDebugLines(handler, guiInfo);
            int panelHeight = debugLines.size() * DEBUG_LINE_HEIGHT + DEBUG_PADDING * 2;

            if (currentColumn >= maxColumns) {
                currentColumn = 0;
                currentX = startX;
                currentY += panelHeight + DEBUG_PADDING * 2;

                if (currentY + panelHeight > scaledHeight - SIDE_MARGIN) {
                    break;
                }
            }

            // 绘制面板背景
            Render2DUtils.fill(
                    currentX, currentY,
                    currentX + columnWidth, currentY + panelHeight,
                    new Color(0, 0, 0, 50)
            );

            // 绘制文本行
            int lineY = currentY + DEBUG_PADDING;
            for (String line : debugLines) {
                drawDebugLine(line, currentX + DEBUG_PADDING, lineY);
                lineY += DEBUG_LINE_HEIGHT;
            }

            drawnCount++;
            currentColumn++;
            currentX += columnWidth + COLUMN_SPACING;

            if (currentY + panelHeight > scaledHeight - SIDE_MARGIN) {
                break;
            }
        }

        return drawnCount;
    }

    private int drawCommonDebugInfo(int startX, int startY) {
        List<String> commonLines = new ArrayList<>();
        commonLines.add("全局Tick: " + ClientPlayerTickManager.getCurrentHandlerTime());
        commonLines.add("活跃Handler数: " + ClientPlayerTickManager.VALUES.size());

        Minecraft mc = Minecraft.getInstance();
        int maxWidth = 0;
        for (String line : commonLines) {
            String cleanLine = line.replaceAll("§[0-9a-fA-Fklmnor]", "");
            maxWidth = Math.max(maxWidth, mc.font.width(cleanLine));
        }

        int bgWidth = maxWidth + DEBUG_PADDING * 2;
        int bgHeight = commonLines.size() * DEBUG_LINE_HEIGHT + DEBUG_PADDING * 2;

        Render2DUtils.fill(
                startX, startY,
                startX + bgWidth, startY + bgHeight,
                new Color(0, 0, 0, 50)
        );

        int lineY = startY + DEBUG_PADDING;
        for (String line : commonLines) {
            drawDebugLine(line, startX + DEBUG_PADDING, lineY);
            lineY += DEBUG_LINE_HEIGHT;
        }

        return startY + bgHeight;
    }

    private List<String> buildHandlerDebugLines(ClientPlayerTickHandler handler, GuiBlockInfo guiInfo) {
        List<String> lines = new ArrayList<>();
        lines.add("处理类型: " + handler.getId());
        lines.add("当前位置: " + guiInfo.pos.toShortString());
        if (guiInfo.requiredState != null) {
            lines.add("投影方块: " + guiInfo.requiredState.getBlock().getName().getString());
        }
        lines.add("当前方块: " + guiInfo.currentState.getBlock().getName().getString());
        lines.add("交互范围: " + booleanToColoredString(guiInfo.interacted));
        lines.add("选区类型: " + booleanToColoredString(guiInfo.posInSelectionRange));
        lines.add("已经执行: " + booleanToColoredString(guiInfo.execute));

        int renderIndex = handler.getRenderIndex();
        int queueSize = handler.getGuiBlockInfoQueueSize();
        lines.add("同刻迭代(GUI): " + formatAlignedNumber(renderIndex, queueSize) + "/" + queueSize);

        return lines;
    }

    private void drawDebugLine(String text, int x, int y) {
        Render2DUtils.drawString(text, x, y, new Color(0, 255, 255, 255), true);
    }

    // ==================== HUD 进度条等信息绘制 ====================

    private void drawHudInfo(float scaledWidth, float scaledHeight) {
        int centerX = (int) (scaledWidth / 2);
        int centerY = (int) (scaledHeight / 2);
        GuiHandler guiHandler = ClientPlayerTickManager.GUI;

        // 延迟过大警告
        if (Configs.Core.LAG_CHECK.getBooleanValue() &&
                ClientPlayerTickManager.getPacketTick() > Configs.Core.LAG_CHECK_MAX.getIntegerValue()) {
            Render2DUtils.drawString("延迟过大，已暂停运行", centerX, centerY - 22, Color.ORANGE, true, true);
        }

        // 单模式进度条
        WorkingModeType workMode = (WorkingModeType) Configs.Core.WORK_MODE.getOptionListValue();
        if (workMode.equals(WorkingModeType.SINGLE)) {
            double progress = guiHandler.getTotalProgress().getProgress();
            Render2DUtils.drawString((int) (progress * 100) + "%", centerX, centerY + 22, Color.WHITE, true, true);
            drawProgressBar(centerX, centerY + 36, 40, 6, progress, new Color(0, 0, 0, 150), new Color(0, 255, 0, 255));
        }

        // 模式名称显示
        if (ConfigUtils.isSingleMode()) {
            String modeName = Configs.Core.WORK_MODE_TYPE.getOptionListValue().getDisplayName();
            Render2DUtils.drawString(modeName, centerX, centerY + 52, Color.WHITE, true, true);
        } else {
            HashSet<String> modeNames = new HashSet<>();
            for (ClientPlayerTickHandler handler : ClientPlayerTickManager.VALUES) {
                if (handler.getId().equals(GuiHandler.NAME) ||
                        handler.getEnableConfig() == null ||
                        !handler.getEnableConfig().getBooleanValue()) {
                    continue;
                }
                modeNames.add(handler.getEnableConfig().getPrettyName());
            }
            Render2DUtils.drawString(String.join(", ", modeNames), centerX, centerY + 52, Color.WHITE, true, true);
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

    // ==================== 静态工具方法 ====================

    private static String booleanToColoredString(boolean value) {
        return value ? "§atrue" : "§cfalse";
    }

    private static String formatAlignedNumber(int current, int total) {
        int totalDigits = total == 0 ? 1 : String.valueOf(total).length();
        DecimalFormat formatter = new DecimalFormat(String.format("%0" + totalDigits + "d", 0));
        return formatter.format(current);
    }
}