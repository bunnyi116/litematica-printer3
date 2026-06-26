package me.aleksilassila.litematica.printer.printer.action;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ClickAction extends Action {
    @Override
    public Action queueAction(@NotNull BlockPos blockPos, @NotNull Direction side, boolean useShift, @NotNull LocalPlayer player) {
        ActionManager.INSTANCE.queueClick(blockPos, side, getSides().get(side), false);
        return this;
    }

    @Override
    public @Nullable Item[] getRequiredItems(Block backup) {
        return this.useItems;
    }

    @Override
    public @Nullable Direction getValidSide(ClientLevel world, BlockPos pos) {
        for (Direction side : getSides().keySet()) {
            return side;
        }
        return null;
    }
}
