package me.aleksilassila.litematica.printer.printer;

import lombok.Getter;
import lombok.Setter;
import me.aleksilassila.litematica.printer.enums.IterationOrderType;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Vec3i;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.Objects;

import net.minecraft.core.BlockPos;

@Getter
public class WorkBox implements Iterable<BlockPos> {
    public static final Minecraft client = Minecraft.getInstance();

    private int minX, minY, minZ, maxX, maxY, maxZ;
    private int centerX, centerY, centerZ;

    @Setter
    private boolean yIncrement = true;
    @Setter
    private boolean xIncrement = true;
    @Setter
    private boolean zIncrement = true;
    @Setter
    private IterationOrderType iterationMode = IterationOrderType.XZY;

    @Getter
    private Iterator<BlockPos> iterator;

    public WorkBox(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        this.update(minX, minY, minZ, maxX, maxY, maxZ);
    }

    public WorkBox(int centerX, int centerY, int centerZ, int radius) {
        this.update(centerX, centerY, centerZ, radius);
    }

    public WorkBox(Vec3i pos, int radius) {
        this.update(pos.getX(), pos.getY(), pos.getZ(), radius);
    }

    public WorkBox(Vec3i pos1, Vec3i pos2) {
        this(pos1.getX(), pos1.getY(), pos1.getZ(), pos2.getX(), pos2.getY(), pos2.getZ());
    }

    public void update(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        this.minX = Math.min(minX, maxX);
        this.minY = Math.min(minY, maxY);
        this.minZ = Math.min(minZ, maxZ);

        this.maxX = Math.max(minX, maxX);
        this.maxY = Math.max(minY, maxY);
        this.maxZ = Math.max(minZ, maxZ);

        // 根据当前世界对世界进行性能优化, 避免出现大范围无效位置迭代
        if (client.level != null) {
            this.minY = Math.max(client.level.getMinY(), this.minY);
            this.maxY = Math.min(client.level.getMaxY(), this.maxY);
        }

        this.centerX = (this.minX + this.maxX) / 2;
        this.centerY = (this.minY + this.maxY) / 2;
        this.centerZ = (this.minZ + this.maxZ) / 2;
    }

    public void update(int centerX, int centerY, int centerZ, int radius) {
        this.centerX = centerX;
        this.centerY = centerY;
        this.centerZ = centerZ;

        this.minX = centerX - radius;
        this.minY = centerY - radius;
        this.minZ = centerZ - radius;

        this.maxX = centerX + radius;
        this.maxY = centerY + radius;
        this.maxZ = centerZ + radius;

        // 根据当前世界对世界进行性能优化, 避免出现大范围无效位置迭代
        if (client.level != null) {
            this.minY = Math.max(client.level.getMinY(), this.minY);
            this.maxY = Math.min(client.level.getMaxY(), this.maxY);
        }
    }

    public boolean contains(int x, int y, int z) {
        return x >= this.minX && x <= this.maxX && y >= this.minY && y <= this.maxY && z >= this.minZ && z <= this.maxZ;
    }

    public boolean contains(Vec3i vec3i) {
        return vec3i.getX() >= this.minX && vec3i.getX() <= this.maxX && vec3i.getY() >= this.minY && vec3i.getY() <= this.maxY && vec3i.getZ() >= this.minZ && vec3i.getZ() <= this.maxZ;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        WorkBox box = (WorkBox) o;
        return minX == box.minX && minY == box.minY && minZ == box.minZ && maxX == box.maxX && maxY == box.maxY && maxZ == box.maxZ;
    }

    @Override
    public int hashCode() {
        return Objects.hash(minX, minY, minZ, maxX, maxY, maxZ);
    }


    @Override
    public @NotNull Iterator<BlockPos> iterator() {
        if (this.iterator == null) {
            this.iterator = new BoxIterator();
        }
        return this.iterator;
    }

    private class BoxIterator implements Iterator<BlockPos> {
        public BlockPos currPos;

        @Override
        public boolean hasNext() {
            if (currPos == null) return true;
            int x = currPos.getX();
            int y = currPos.getY();
            int z = currPos.getZ();

            int targetX = xIncrement ? maxX : minX;
            int targetY = yIncrement ? maxY : minY;
            int targetZ = zIncrement ? maxZ : minZ;

            if (x == targetX && y == targetY && z == targetZ) {
                currPos = new BlockPos(
                        IterationOrderType.Axis.X.reset(WorkBox.this),
                        IterationOrderType.Axis.Y.reset(WorkBox.this),
                        IterationOrderType.Axis.Z.reset(WorkBox.this)
                );
                return false;
            } else {
                return true;
            }
        }

        @Override
        public BlockPos next() {
            // 初始化起始位置
            if (currPos == null) {
                currPos = new BlockPos(
                        IterationOrderType.Axis.X.reset(WorkBox.this),
                        IterationOrderType.Axis.Y.reset(WorkBox.this),
                        IterationOrderType.Axis.Z.reset(WorkBox.this)
                );
                return currPos;
            }

            // 复制当前坐标，避免直接修改原对象
            int x = currPos.getX();
            int y = currPos.getY();
            int z = currPos.getZ();

            // 获取当前迭代模式的轴优先级，通用处理所有轴迭代（无任何switch）
            for (IterationOrderType.Axis axis : iterationMode.axis) {
                // 对当前轴执行增量
                int newValue = axis.increment(WorkBox.this, axis.getCoord(WorkBox.this, x, y, z));

                // 检查是否溢出
                if (axis.isOverflow(WorkBox.this, newValue)) {
                    // 溢出则重置当前轴，继续处理下一个轴
                    switch (axis) {
                        case X -> x = axis.reset(WorkBox.this);
                        case Y -> y = axis.reset(WorkBox.this);
                        case Z -> z = axis.reset(WorkBox.this);
                    }
                } else {
                    // 未溢出则更新当前轴坐标，终止循环
                    switch (axis) {
                        case X -> x = newValue;
                        case Y -> y = newValue;
                        case Z -> z = newValue;
                    }
                    break;
                }
            }

            // 更新当前位置并返回
            currPos = new BlockPos(x, y, z);
            return currPos;
        }
    }
}