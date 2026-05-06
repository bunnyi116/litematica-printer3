package me.aleksilassila.litematica.printer.enums;

import me.aleksilassila.litematica.printer.I18n;
import me.aleksilassila.litematica.printer.config.ConfigOptionListEntry;
import me.aleksilassila.litematica.printer.printer.WorkBox;

public enum IterationOrderType implements ConfigOptionListEntry<IterationOrderType> {
    XYZ(I18n.of("iterationOrder.xyz"), Axis.X, Axis.Y, Axis.Z),
    XZY(I18n.of("iterationOrder.xzy"), Axis.X, Axis.Z, Axis.Y),
    YXZ(I18n.of("iterationOrder.yxz"), Axis.Y, Axis.X, Axis.Z),
    YZX(I18n.of("iterationOrder.yzx"), Axis.Y, Axis.Z, Axis.X),
    ZXY(I18n.of("iterationOrder.zxy"), Axis.Z, Axis.X, Axis.Y),
    ZYX(I18n.of("iterationOrder.zyx"), Axis.Z, Axis.Y, Axis.X);

    private final I18n i18n;
    public final Axis[] axis;

    IterationOrderType(I18n i18n, Axis... axis) {
        this.i18n = i18n;
        this.axis = axis;
    }

    @Override
    public I18n getI18n() {
        return i18n;
    }

    // 封装轴的所有行为逻辑
    public enum Axis {
        X {
            @Override
            public int getCoord(WorkBox box, int x, int y, int z) {
                return x;
            }

            @Override
            public int increment(WorkBox box, int current) {
                return current + (box.isXIncrement() ? 1 : -1);
            }

            @Override
            public boolean isOverflow(WorkBox box, int value) {
                return box.isXIncrement() ? value > box.getMaxX() : value < box.getMinX();
            }

            @Override
            public int reset(WorkBox box) {
                return box.isXIncrement() ? box.getMinX() : box.getMaxX();
            }
        },
        Y {
            @Override
            public int getCoord(WorkBox box, int x, int y, int z) {
                return y;
            }

            @Override
            public int increment(WorkBox box, int current) {
                return current + (box.isYIncrement() ? 1 : -1);
            }

            @Override
            public boolean isOverflow(WorkBox box, int value) {
                return box.isYIncrement() ? value > box.getMaxY() : value < box.getMinY();
            }

            @Override
            public int reset(WorkBox box) {
                return box.isYIncrement() ? box.getMinY() : box.getMaxY();
            }
        },
        Z {
            @Override
            public int getCoord(WorkBox box, int x, int y, int z) {
                return z;
            }

            @Override
            public int increment(WorkBox box, int current) {
                return current + (box.isZIncrement() ? 1 : -1);
            }

            @Override
            public boolean isOverflow(WorkBox box, int value) {
                return box.isZIncrement() ? value > box.getMaxZ() : value < box.getMinZ();
            }

            @Override
            public int reset(WorkBox box) {
                return box.isZIncrement() ? box.getMinZ() : box.getMaxZ();
            }
        };

        // 获取当前轴的坐标值（x/y/z）
        public abstract int getCoord(WorkBox box, int x, int y, int z);

        // 对当前轴执行增量操作
        public abstract int increment(WorkBox box, int current);

        // 检查当前轴是否超出边界
        public abstract boolean isOverflow(WorkBox box, int value);

        // 重置当前轴到起始边界值
        public abstract int reset(WorkBox box);
    }
}