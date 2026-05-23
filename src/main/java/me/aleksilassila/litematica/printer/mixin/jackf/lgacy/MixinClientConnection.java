package me.aleksilassila.litematica.printer.mixin.jackf.lgacy;

//#if MC > 11904
import me.aleksilassila.litematica.printer.mixin_extension.Pointless;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Pointless.class)
public abstract class MixinClientConnection {
}
//#else
//$$ import me.aleksilassila.litematica.printer.config.Configs;
//$$ import me.aleksilassila.litematica.printer.printer.zxy.memory.MemoryDatabase;
//$$ import net.fabricmc.api.EnvType;
//$$ import net.fabricmc.api.Environment;
//$$ import net.minecraft.network.Connection;
//$$ import net.minecraft.network.chat.Component;
//$$ import org.spongepowered.asm.mixin.Mixin;
//$$ import org.spongepowered.asm.mixin.injection.At;
//$$ import org.spongepowered.asm.mixin.injection.Inject;
//$$ import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//$$
//$$ @Environment(EnvType.CLIENT)
//$$ @Mixin({Connection.class})
//$$ public abstract class MixinClientConnection {
//$$     @Inject(
//$$             method = {"disconnect"},
//$$             at = {@At("HEAD")}
//$$     )
//$$     public void chestTracker$onDisconnectHandler(Component ignored, CallbackInfo ci) {
//$$         if(!Configs.Core.CLOUD_INVENTORY.getBooleanValue()) return;
//$$         MemoryDatabase database = MemoryDatabase.getCurrent();
//$$         if (database != null) {
//$$             MemoryDatabase.clearCurrent();
//$$         }
//$$     }
//$$ }
//#endif
