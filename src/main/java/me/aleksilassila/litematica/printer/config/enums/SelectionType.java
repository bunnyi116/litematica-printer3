package me.aleksilassila.litematica.printer.config.enums;

import me.aleksilassila.litematica.printer.I18n;
import me.aleksilassila.litematica.printer.config.ConfigOptionListEntry;

/**
 * 选区模式
 */
public enum SelectionType implements ConfigOptionListEntry<SelectionType> {
    /**
     * 投影选区范围
     */
    LITEMATICA_SELECTION("selectionType.litematica.selection"),

    /**
     * 投影渲染层
     */
    LITEMATICA_RENDER_LAYER("selectionType.litematica.renderLayer"),

    /**
     * 玩家下方的部分
     */
    LITEMATICA_SELECTION_BELOW_PLAYER("selectionType.litematica.selection.belowPlayer"),

    /**
     * 玩家上方的部分
     */
    LITEMATICA_SELECTION_ABOVE_PLAYER("selectionType.litematica.selection.abovePlayer");

    private final I18n i18n;

    SelectionType(String translateKey) {
        this.i18n = I18n.of(translateKey);
    }

    @Override
    public I18n getI18n() {
        return i18n;
    }
}
