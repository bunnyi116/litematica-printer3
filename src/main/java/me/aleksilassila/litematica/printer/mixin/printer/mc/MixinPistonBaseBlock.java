package me.aleksilassila.litematica.printer.mixin.printer.mc;

import me.aleksilassila.litematica.printer.printer.ActionManager;
import me.aleksilassila.litematica.printer.printer.PlayerLook;
import me.aleksilassila.litematica.printer.utils.minecraft.DirectionUtils;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PistonBaseBlock.class)
public abstract class MixinPistonBaseBlock extends DirectionalBlock {

    protected MixinPistonBaseBlock(Properties properties) {
        super(properties);
    }

    @Inject(at = @At("HEAD"), method = "getStateForPlacement", cancellable = true)
    public void getStateForPlacement(BlockPlaceContext context, CallbackInfoReturnable<BlockState> cir) {
        PlayerLook look = ActionManager.INSTANCE.look;
        if (look != null) {
            BlockState blockState = this.defaultBlockState()
                    .setValue(PistonBaseBlock.FACING, DirectionUtils.orderedByNearest(look.yaw, look.pitch)[0])
                    .setValue(PistonBaseBlock.EXTENDED, false);

            cir.setReturnValue(blockState);
        }
    }
}