package me.aleksilassila.litematica.printer.mixin.printer.mc;

import me.aleksilassila.litematica.printer.render.Render2D;
import me.aleksilassila.litematica.printer.utils.render.Render2DUtils;
import me.aleksilassila.litematica.printer.utils.ConfigUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//#if MC <= 11904
//$$import com.mojang.blaze3d.vertex.PoseStack;
//#elseif MC > 12006
import net.minecraft.client.DeltaTracker;
//#endif

//#if MC < 260000
//$$ import net.minecraft.client.gui.GuiGraphics;
//#endif

@Mixin(Gui.class)
public abstract class MixinGui {
    // @formatter:off
    //#if MC >= 260100
    @Inject(method = "extractHotbarAndDecorations", at = @At("TAIL"))
    //#else
    //$$ @Inject(method = "renderItemHotbar", at = @At("TAIL"))
    //#endif

    //#if MC > 12006
    private void hookRenderItemHotbar(GuiGraphicsExtractor guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
    //#elseif MC >= 12006
    //$$ private void hookRenderItemHotbar(GuiGraphics guiGraphics, float f, CallbackInfo ci) {
    //#elseif MC > 11904
    //$$ private void hookRenderItemHotbar(float f, GuiGraphics guiGraphics, CallbackInfo ci) {
    //#else
    //$$ private void hookRenderItemHotbar(float f, PoseStack poseStack, CallbackInfo ci) {
    //#endif
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || mc.player.isSpectator() || !ConfigUtils.isEnable()) {
            return;
        }

        // 初始化渲染矩阵
        //#if MC > 11904
        Render2DUtils.initGuiGraphics(guiGraphics);
        //#else
        //$$ Render2DUtils.initMatrix(poseStack);
        //#endif

        float scaledWidth = mc.getWindow().getGuiScaledWidth();
        float scaledHeight = mc.getWindow().getGuiScaledHeight();
        Render2D.INSTANCE.render(scaledWidth, scaledHeight);
    }
    // @formatter:on
}