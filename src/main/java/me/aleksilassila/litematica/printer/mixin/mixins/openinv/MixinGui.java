package me.aleksilassila.litematica.printer.mixin.mixins.openinv;

//#if MC >= 260200

import me.aleksilassila.litematica.printer.utils.mods.ModLoadUtils;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class MixinGui {

    @Inject(method = "setScreen", at = {@At(value = "HEAD")}, cancellable = true)
    public void setScreen(Screen screen, CallbackInfo ci) {
        if (ModLoadUtils.closeScreen > 0 && /*screen != null &&*/ screen instanceof AbstractContainerScreen<?>) {
            ModLoadUtils.closeScreen--;
            ci.cancel();
        }
    }
}

//#else
//$$ import me.aleksilassila.litematica.printer.mixin.extension.Pointless;
//$$ import org.spongepowered.asm.mixin.Mixin;
//$$ @Mixin(value = Pointless.class)
//$$ public class MixinGui { }
//#endif