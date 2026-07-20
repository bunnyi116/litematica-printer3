package me.aleksilassila.litematica.printer.mixin.mixins.printer.mc;

import me.aleksilassila.litematica.printer.action.ActionManager;
import me.aleksilassila.litematica.printer.printer.Look;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@SuppressWarnings("ModifyVariableMayUseName")
@Mixin(value = ServerboundMovePlayerPacket.class, priority = 1010)
public class ServerboundMovePlayerPacketMixin {
    //#if MC > 12101
    @ModifyVariable(method = "<init>(DDDFFZZZZ)V", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    //#else
    //$$ @ModifyVariable(method = "<init>(DDDFFZZZ)V", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    //#endif
    private static float modifyLookYaw(float yaw) {
        Look look = ActionManager.INSTANCE.look;
        if (look != null) {
            return look.yaw;
        }
        return yaw;
    }

    //#if MC > 12101
    @ModifyVariable(method = "<init>(DDDFFZZZZ)V", at = @At("HEAD"), ordinal = 1, argsOnly = true)
    //#else
    //$$ @ModifyVariable(method = "<init>(DDDFFZZZ)V", at = @At("HEAD"), ordinal = 1, argsOnly = true)
    //#endif
    private static float modifyLookPitch(float pitch) {
        Look look = ActionManager.INSTANCE.look;
        if (look != null) {
            return look.pitch;
        }
        return pitch;
    }
}
