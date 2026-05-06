package me.aleksilassila.litematica.printer.mixin;

import me.aleksilassila.litematica.printer.handler.ClientPlayerTickManager;
import me.aleksilassila.litematica.printer.mixin_extension.MultiPlayerGameModeExtension;
import me.aleksilassila.litematica.printer.printer.zxy.inventory.InventoryUtils;
import me.aleksilassila.litematica.printer.printer.zxy.utils.ZxyUtils;
import me.aleksilassila.litematica.printer.utils.CooldownUtils;
import me.aleksilassila.litematica.printer.utils.InteractionUtils;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftMixin {
    @Inject(at = @At("HEAD"), method = "tick")
    public void tick(CallbackInfo ci) {
        CooldownUtils.INSTANCE.tick();
        InventoryUtils.tick();
        ZxyUtils.tick();
        Minecraft mc = (Minecraft) ((Object) this);
        if (mc.gameMode instanceof MultiPlayerGameModeExtension extension) {
            extension.litematica_printer$handleDelayedDestroy();
        }
        InteractionUtils.INSTANCE.preprocess();
        if (InteractionUtils.INSTANCE.isNeedHandle()) {
            InteractionUtils.INSTANCE.onTick();
        } else {
            ClientPlayerTickManager.tick();
        }
    }
}
