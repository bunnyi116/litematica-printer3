package me.aleksilassila.litematica.printer.config.enums;

import lombok.Getter;
import me.aleksilassila.litematica.printer.I18n;
import me.aleksilassila.litematica.printer.config.ConfigOptionListEntry;

@Getter
public enum IterationOrder implements ConfigOptionListEntry<IterationOrder> {
    XYZ(I18n.of("iterationOrder.xyz"), Axis.X, Axis.Y, Axis.Z),
    XZY(I18n.of("iterationOrder.xzy"), Axis.X, Axis.Z, Axis.Y),
    YXZ(I18n.of("iterationOrder.yxz"), Axis.Y, Axis.X, Axis.Z),
    YZX(I18n.of("iterationOrder.yzx"), Axis.Y, Axis.Z, Axis.X),
    ZXY(I18n.of("iterationOrder.zxy"), Axis.Z, Axis.X, Axis.Y),
    ZYX(I18n.of("iterationOrder.zyx"), Axis.Z, Axis.Y, Axis.X);

    private final I18n i18n;

    /** 最外层轴（变化最慢） */
    private final Axis first;

    /** 中间层轴（变化中等） */
    private final Axis second;

    /** 最内层轴（变化最快） */
    private final Axis third;

    IterationOrder(I18n i18n, Axis first, Axis second, Axis third) {
        this.i18n = i18n;
        this.first = first;
        this.second = second;
        this.third = third;
    }

    @Override
    public I18n getI18n() {
        return i18n;
    }

    public enum Axis {
        X, Y, Z
    }
}