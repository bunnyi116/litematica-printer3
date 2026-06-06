package me.aleksilassila.litematica.printer.config.enums;

import me.aleksilassila.litematica.printer.I18n;
import me.aleksilassila.litematica.printer.config.ConfigOptionListEntry;

/**
 * 工作单例模式
 */
public enum WorkSingleMode implements ConfigOptionListEntry<WorkSingleMode> {
    PRINT("workSingleMode.print"),
    MINE("workSingleMode.mine"),
    FLUID("workSingleMode.fluid"),
    FILL("workSingleMode.fill"),
    // REPLACE("workSingleMode.replace"),
    BEDROCK("workSingleMode.bedrock");

    private final I18n i18n;

    WorkSingleMode(String translateKey) {
        this.i18n = I18n.of(translateKey);
    }

    @Override
    public I18n getI18n() {
        return i18n;
    }
}
