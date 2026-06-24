package me.aleksilassila.litematica.printer.event;

import me.aleksilassila.litematica.printer.event.callbacks.ModIterationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.event.Event;

public class EventCallbacks {
    public final static Event<ClientTickEvents.StartTick> CLIENT_START_TICK = ClientTickEvents.START_CLIENT_TICK;
    public final static Event<ClientTickEvents.EndTick> CLIENT_END_TICK = ClientTickEvents.END_CLIENT_TICK;

    public final static Event<ClientTickEvents.StartLevelTick> CLIENT_LEVEL_START_TICK = ClientTickEvents.START_LEVEL_TICK;
    public final static Event<ClientTickEvents.EndLevelTick> CLIENT_LEVEL_END_TICK = ClientTickEvents.END_LEVEL_TICK;

    public final static Event<ClientPlayConnectionEvents.Join> CLIENT_PLAY_JOIN = ClientPlayConnectionEvents.JOIN;
    public final static Event<ClientPlayConnectionEvents.Disconnect> CLIENT_PLAY_DISCONNECT = ClientPlayConnectionEvents.DISCONNECT;

    public final static Event<ModIterationCallback> MOD_ITERATION = ModIterationCallback.EVENT;
}
