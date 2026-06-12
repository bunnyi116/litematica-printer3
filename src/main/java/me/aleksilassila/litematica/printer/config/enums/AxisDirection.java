package me.aleksilassila.litematica.printer.config.enums;

import me.aleksilassila.litematica.printer.I18n;
import me.aleksilassila.litematica.printer.config.ConfigOptionListEntry;

public enum AxisDirection implements ConfigOptionListEntry<IterationOrder> {
    /*** 正序：min → max ***/
    POSITIVE(I18n.of("axisDirection.positive")),

    /*** 反序：max → min ***/
    NEGATIVE(I18n.of("axisDirection.negative"));

    private final I18n i18n;

    AxisDirection(I18n i18n) {
        this.i18n = i18n;
    }

    @Override
    public I18n getI18n() {
        return i18n;
    }
}
