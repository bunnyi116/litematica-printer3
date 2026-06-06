package me.aleksilassila.litematica.printer.config.enums;

import me.aleksilassila.litematica.printer.I18n;
import me.aleksilassila.litematica.printer.config.ConfigOptionListEntry;

/**
 * 工作模式
 */
public enum WorkMode implements ConfigOptionListEntry<WorkMode> {
    MULTI("workMode.multi"),
    SINGLE("workMode.single");

    private final I18n i18n;

    WorkMode(String translateKey) {
        this.i18n = I18n.of(translateKey);
    }

    @Override
    public I18n getI18n() {
        return i18n;
    }
}
