package me.aleksilassila.litematica.printer.printer;

import lombok.Getter;
import lombok.Setter;
import me.aleksilassila.litematica.printer.config.enums.AxisDirection;
import me.aleksilassila.litematica.printer.config.enums.IterationOrder;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;

public class WorkBox implements Iterable<BlockPos> {
    @Getter
    protected int minX, minY, minZ;

    @Getter
    protected int maxX, maxY, maxZ;

    @Getter
    protected int centerX, centerY, centerZ;

    @Getter
    @Setter
    protected IterationOrder iterationOrder = IterationOrder.XYZ;

    @Getter
    @Setter
    protected AxisDirection xDirection = AxisDirection.POSITIVE;

    @Getter
    @Setter
    protected AxisDirection yDirection = AxisDirection.POSITIVE;

    @Getter
    @Setter
    protected AxisDirection zDirection = AxisDirection.POSITIVE;

    @Getter
    protected BoxIterator iterator;

    @Getter
    @Setter
    protected @Nullable BlockPos nextIterationPos;

    // @formatter:on

    public WorkBox(LocalPlayer player, int radius, ClientLevel level) {
        this.setPlayerWorkBox(player, radius, level);
    }

    public WorkBox(Vec3i pos1, Vec3i pos2) {
        this.setBounds(pos1.getX(), pos1.getY(), pos1.getZ(), pos2.getX(), pos2.getY(), pos2.getZ());
    }

    public WorkBox(int x1, int y1, int z1, int x2, int y2, int z2) {
        this.setBounds(x1, y1, z1, x2, y2, z2);
    }

    public WorkBox(int centerX, int centerY, int centerZ, int radius) {
        this.setCenterAndRadius(centerX, centerY, centerZ, radius);
    }

    public WorkBox(int centerX, int centerY, int centerZ, int radius, Level level) {
        this.setCenterAndRadiusWithLevel(centerX, centerY, centerZ, radius, level);
    }

    public void setPlayerWorkBox(LocalPlayer player, int radius, ClientLevel level) {
        BlockPos blockPos = player.blockPosition();
        this.setCenterAndRadiusWithLevel(blockPos.getX(), blockPos.getY(), blockPos.getZ(), radius, level);
    }

    public void setBounds(int x1, int y1, int z1, int x2, int y2, int z2) {
        this.minX = Math.min(x1, x2);
        this.minY = Math.min(y1, y2);
        this.minZ = Math.min(z1, z2);

        this.maxX = Math.max(x1, x2);
        this.maxY = Math.max(y1, y2);
        this.maxZ = Math.max(z1, z2);

        updateCenter();
        // 范围变更后重置迭代器状态
//        resetIterator();
    }

    public void setCenterAndRadius(int cx, int cy, int cz, int radius) {
        this.minX = cx - radius;
        this.minY = cy - radius;
        this.minZ = cz - radius;

        this.maxX = cx + radius;
        this.maxY = cy + radius;
        this.maxZ = cz + radius;

        updateCenter();
//        resetIterator();
    }

    public void setCenterAndRadiusWithLevel(int cx, int cy, int cz, int radius, Level level) {
        this.minX = cx - radius;
        this.minY = cy - radius;
        this.minY = Math.max(this.minY, level.getMinY());
        this.minZ = cz - radius;

        this.maxX = cx + radius;
        this.maxY = cy + radius;
        this.maxY = Math.min(this.maxY, level.getMaxY());
        this.maxZ = cz + radius;

        updateCenter();
//        resetIterator();
    }

    public void setCenter(int cx, int cy, int cz) {
        setCenterAndRadius(cx, cy, cz, getRadius());
    }

    public void setRadius(int radius) {
        setCenterAndRadius(centerX, centerY, centerZ, radius);
    }

    public void setRadiusWithLevel(int radius, Level level) {
        setCenterAndRadiusWithLevel(centerX, centerY, centerZ, radius, level);
    }

    private void updateCenter() {
        this.centerX = (minX + maxX) / 2;
        this.centerY = (minY + maxY) / 2;
        this.centerZ = (minZ + maxZ) / 2;
    }

    public long getSize() {
        return (long) getSizeX() * getSizeY() * getSizeZ();
    }

    public int getSizeX() {
        return maxX - minX + 1;
    }

    public int getSizeY() {
        return maxY - minY + 1;
    }

    public int getSizeZ() {
        return maxZ - minZ + 1;
    }

    public boolean contains(Vec3i vec3i) {
        return contains(vec3i.getX(), vec3i.getY(), vec3i.getZ());
    }

    public boolean contains(int x, int y, int z) {
        return x >= minX && x <= maxX
                && y >= minY && y <= maxY
                && z >= minZ && z <= maxZ;
    }

    public int getRadius() {
        return (maxX - minX) / 2;
    }

    @Override
    public @NotNull Iterator<BlockPos> iterator() {
        if (this.iterator == null) {
            this.iterator = new BoxIterator();
        }
        return this.iterator;
    }

    // ===================== 新增对外公开方法 =====================

    /**
     * 手动重置迭代器，从头开始遍历
     */
    public void resetIterator() {
        if (this.iterator == null) {
            this.iterator = new BoxIterator();
        }
        this.iterator.reset();
    }

    /**
     * 判断当前迭代是否已经全部遍历完毕
     */
    public boolean isIterationFinished() {
        if (iterator == null) return false;
        return iterator.isCompleted;
    }

    protected class BoxIterator implements Iterator<BlockPos> {
        private int currX;
        private int currY;
        private int currZ;

        private boolean initialized = false;
        private boolean isCompleted = false;

        private void init() {
            currX = xDirection == AxisDirection.POSITIVE ? minX : maxX;
            currY = yDirection == AxisDirection.POSITIVE ? minY : maxY;
            currZ = zDirection == AxisDirection.POSITIVE ? minZ : maxZ;
            initialized = true;
            isCompleted = false;
        }

        private void reset() {
            initialized = false;
            isCompleted = false;
        }

        private int get(IterationOrder.Axis axis) {
            return switch (axis) {
                case X -> currX;
                case Y -> currY;
                case Z -> currZ;
            };
        }

        private void set(IterationOrder.Axis axis, int value) {
            switch (axis) {
                case X -> currX = value;
                case Y -> currY = value;
                case Z -> currZ = value;
            }
        }

        private AxisDirection getDir(IterationOrder.Axis axis) {
            return switch (axis) {
                case X -> xDirection;
                case Y -> yDirection;
                case Z -> zDirection;
            };
        }

        private int getMin(IterationOrder.Axis axis) {
            return switch (axis) {
                case X -> minX;
                case Y -> minY;
                case Z -> minZ;
            };
        }

        private int getMax(IterationOrder.Axis axis) {
            return switch (axis) {
                case X -> maxX;
                case Y -> maxY;
                case Z -> maxZ;
            };
        }

        private boolean inRange(IterationOrder.Axis axis) {
            AxisDirection dir = getDir(axis);
            int value = get(axis);
            return dir == AxisDirection.POSITIVE ? value <= getMax(axis) : value >= getMin(axis);
        }

        private boolean step(IterationOrder.Axis axis) {
            AxisDirection dir = getDir(axis);
            int next = get(axis) + (dir == AxisDirection.POSITIVE ? 1 : -1);
            set(axis, next);

            if (inRange(axis)) return true;
            // 重置轴
            set(axis, dir == AxisDirection.POSITIVE ? getMin(axis) : getMax(axis));
            return false;
        }

        @Override
        public boolean hasNext() {
            if (nextIterationPos != null) {
                return true;
            }
            if (isCompleted) {
                return false;
            }
            if (!initialized) {
                init();
            }
            boolean hasNext = inRange(IterationOrder.Axis.X) && inRange(IterationOrder.Axis.Y) && inRange(IterationOrder.Axis.Z);
            if (!hasNext) {
                isCompleted = true;
                return false;
            }
            return true;
        }

        @Override
        public @Nullable BlockPos next() {
            if (nextIterationPos != null) {
                BlockPos blockPos = nextIterationPos;
                nextIterationPos = null;
                return blockPos;
            }

            if (!hasNext()) {
                return null;
            }

            BlockPos pos = new BlockPos(currX, currY, currZ);
            if (!step(iterationOrder.getThird())) {
                if (!step(iterationOrder.getSecond())) {
                    if (!step(iterationOrder.getFirst())) {
                        isCompleted = true;
                    }
                }
            }
            return pos;
        }
    }
}