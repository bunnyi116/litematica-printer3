package me.aleksilassila.litematica.printer.event;

public enum EventResult {
    /**
     * 放行
     */
    PASS,

    /**
     * 取消
     */
    CANCEL;

    public boolean isCancelled() {
        return this == CANCEL;
    }
}