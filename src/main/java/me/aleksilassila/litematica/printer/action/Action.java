package me.aleksilassila.litematica.printer.action;

import lombok.Getter;
import me.aleksilassila.litematica.printer.Reference;
import me.aleksilassila.litematica.printer.config.Configs;
import me.aleksilassila.litematica.printer.printer.Look;
import me.aleksilassila.litematica.printer.utils.minecraft.BlockUtils;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

@SuppressWarnings("UnusedReturnValue")
public class Action {
    protected Map<Direction, Vec3> sides;
    @Getter
    protected @Nullable Look look = null;
    protected @Nullable Item[] useItems;
    protected boolean needSupportBlock = false;
    @Getter
    protected @Nullable Boolean sneak = null;

    public Action() {
        this.sides = new HashMap<>();
        for (Direction direction : Direction.values()) {
            sides.put(direction, new Vec3(0, 0, 0));
        }
    }

    public Action setLookRotation(int lookRotation) {
        this.look = new Look(lookRotation);
        return this;
    }

    public Action setLookDirection(Direction lookDirection) {
        this.look = new Look(lookDirection);
        return this;
    }

    public Action setLookDirection(Direction lookDirectionYaw, Direction lookDirectionPitch) {
        this.look = new Look(lookDirectionYaw, lookDirectionPitch);
        return this;
    }

    public @Nullable Item[] getRequiredItems(Block backup) {
        if (useItems == null) {
            if (backup.asItem() != Items.AIR) {
                return new Item[]{backup.asItem()};
            }
        }
        return useItems;
    }

    public @NotNull Map<Direction, Vec3> getSides() {
        if (this.sides == null) {
            this.sides = new HashMap<>();
            for (Direction d : Direction.values()) {
                this.sides.put(d, new Vec3(0, 0, 0));
            }
        }
        return this.sides;
    }

    public Action setSides(Direction.Axis... axis) {
        Map<Direction, Vec3> sides = new HashMap<>();
        for (Direction.Axis a : axis) {
            for (Direction d : Direction.values()) {
                if (d.getAxis() == a) {
                    sides.put(d, new Vec3(0, 0, 0));
                }
            }
        }
        this.sides = sides;
        return this;
    }

    public Action setSides(Map<Direction, Vec3> sides) {
        this.sides = sides;
        return this;
    }

    public Action setSides(Direction side, Vec3 offset) {
        this.sides = new HashMap<>();
        this.sides.put(side, offset);
        return this;
    }

    public Action setSides(Direction... directions) {
        Map<Direction, Vec3> sides = new HashMap<>();
        for (Direction d : directions) {
            sides.put(d, new Vec3(0, 0, 0));
        }
        this.sides = sides;
        return this;
    }

    @SuppressWarnings("SequencedCollectionMethodCanBeUsed")
    public @Nullable Direction getValidSide(ClientLevel world, BlockPos pos) {
        Map<Direction, Vec3> sides = getSides();
        List<Direction> validSides = new ArrayList<>();
        for (Direction side : sides.keySet()) {
            BlockPos neighborPos = pos.relative(side);
            BlockState neighborState = world.getBlockState(neighborPos);
            if (Configs.Print.PLACE_IN_AIR.getBooleanValue() && !this.needSupportBlock) {
                return side;
            }
            if (canBeClicked(world, neighborPos) && !BlockUtils.isReplaceable(neighborState)) {
                validSides.add(side);
            }
        }
        if (validSides.isEmpty()) {
            return null;
        }
        // 选择一个不需要潜行放置的面
        for (Direction validSide : validSides) {
            BlockState requiredState = world.getBlockState(pos);
            BlockState sideBlockState = world.getBlockState(pos.relative(validSide));
            if (!Reference.isInteractive(sideBlockState.getBlock()) && requiredState.canSurvive(world, pos)) {
                return validSide;
            }
        }
        return validSides.get(0);
    }

    public Action setItem(Item item) {
        return this.setItems(item);
    }

    public Action setItems(Item... items) {
        this.useItems = items;
        return this;
    }

    public Action setNeedSupportBlock(boolean needSupportBlock) {
        this.needSupportBlock = needSupportBlock;
        return this;
    }

    public Action setNeedSupportBlock() {
        return this.setNeedSupportBlock(true);
    }

    public Action setSneak(boolean useShift) {
        this.sneak = useShift;
        return this;
    }

    public Action setSneak() {
        return this.setSneak(true);
    }


    public Action queueAction(@NotNull BlockPos blockPos, @NotNull Direction side, boolean useShift, @NotNull LocalPlayer player) {
        if (Configs.Print.PLACE_IN_AIR.getBooleanValue() && !this.needSupportBlock) {
            ActionManager.INSTANCE.queueClick(
                    blockPos,
                    side.getOpposite(),
                    getSides().get(side),
                    useShift
            );
        } else {
            ActionManager.INSTANCE.queueClick(
                    blockPos.relative(side),
                    side.getOpposite(),
                    getSides().get(side),
                    useShift
            );
        }
        return this;
    }

    public static boolean canBeClicked(ClientLevel world, BlockPos pos) {
        return world.getBlockState(pos).getShape(world, pos) != Shapes.empty();
    }
}
