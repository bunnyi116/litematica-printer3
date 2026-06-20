package me.aleksilassila.litematica.printer;

import lombok.Getter;

@Getter
public final class TickContext {
    public static final TickContext INSTANCE = new TickContext();

    private long clientTickCount;
    private long localPlayerCount;

    public void clientTick() {
        this.clientTickCount++;
    }

    public void resetClientTickCount() {
        this.clientTickCount = 0;
    }

    public void localPlayerTick() {
        this.localPlayerCount++;
    }

    public void resetLocalPlayerTickCount() {
        this.localPlayerCount = 0;
    }
}
