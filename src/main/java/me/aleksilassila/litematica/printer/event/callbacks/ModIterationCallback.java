package me.aleksilassila.litematica.printer.event.callbacks;

import me.aleksilassila.litematica.printer.event.EventResult;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;

@FunctionalInterface
public interface ModIterationCallback {
    EventResult handler(ClientLevel level, BlockPos blockPos);

    Event<ModIterationCallback> EVENT = EventFactory.createArrayBacked(
            ModIterationCallback.class,
            listeners -> (level, blockPos) -> {
                for (ModIterationCallback listener : listeners) {
                    EventResult result = listener.handler(level, blockPos);
                    if (result.isCancelled()) {
                        return result;
                    }
                }
                return EventResult.PASS;
            }
    );
}
