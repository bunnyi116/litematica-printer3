package me.aleksilassila.litematica.printer.mixin.mixins.printer.mc;

//#if MC>= 260200
import me.aleksilassila.litematica.printer.render.Render2D;
import me.aleksilassila.litematica.printer.utils.render.Render2DUtils;
import me.aleksilassila.litematica.printer.utils.ConfigUtils;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Hud.class)
public abstract class HudMixin {
    @Inject(method = "extractHotbarAndDecorations", at = @At("TAIL"))
    private void hookRenderItemHotbar(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || mc.player.isSpectator() || !ConfigUtils.isEnable()) {
            return;
        }
        Render2DUtils.initGuiGraphics(graphics);
        float scaledWidth = mc.getWindow().getGuiScaledWidth();
        float scaledHeight = mc.getWindow().getGuiScaledHeight();
        Render2D.INSTANCE.render(scaledWidth, scaledHeight);
    }
}

//#else
//$$ import me.aleksilassila.litematica.printer.mixin.extension.Pointless;
//$$ import org.spongepowered.asm.mixin.Mixin;
//$$ @Mixin(value = Pointless.class)
//$$ public class HudMixin { }
//#endif