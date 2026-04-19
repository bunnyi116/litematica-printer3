package me.aleksilassila.litematica.printer.guide.blocks;

import me.aleksilassila.litematica.printer.enums.BlockMatchResult;
import me.aleksilassila.litematica.printer.guide.Guide;
import me.aleksilassila.litematica.printer.printer.SchematicBlockContext;
import me.aleksilassila.litematica.printer.printer.action.Action;
import me.aleksilassila.litematica.printer.printer.PrinterUtils;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.SoulFireBlock;
import net.minecraft.world.item.Items;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 火焰放置指南。
 * 注册到：FireBlock.class, SoulFireBlock.class
 *
 * <p>MISSING：找到方向属性确定放置面。
 * WRONG_STATE：AGE 不同则跳过（等待自然扩散）。
 */
public class FireGuide extends Guide {

    public FireGuide(SchematicBlockContext context) {
        super(context);
    }

    @Override
    protected Optional<Action> onBuildActionMissingBlock(BlockMatchResult state, AtomicReference<Boolean> skipOtherGuide) {
        if (requiredBlock instanceof SoulFireBlock) {
            return Optional.of(new Action()
                    .setItems(Items.FLINT_AND_STEEL, Items.FIRE_CHARGE)
                    .setRequiresSupport());
        }

        for (Direction direction : Direction.values()) {
            if (direction == Direction.DOWN) continue;
            Object value = PrinterUtils.getPropertyByName(requiredState, direction.name());
            if (value instanceof Boolean && (Boolean) value) {
                return Optional.of(new Action()
                        .setSides(direction)
                        .setItems(Items.FLINT_AND_STEEL, Items.FIRE_CHARGE)
                        .setRequiresSupport());
            }
        }
        return Optional.of(new Action()
                .setSides(Direction.DOWN)
                .setItems(Items.FLINT_AND_STEEL, Items.FIRE_CHARGE)
                .setRequiresSupport());
    }

    @Override
    protected Optional<Action> onBuildActionWrongState(BlockMatchResult state, AtomicReference<Boolean> skipOtherGuide) {
        // AGE 不同 → 无法通过交互修正，跳过
        if (!requiredState.getValue(FireBlock.AGE).equals(currentState.getValue(FireBlock.AGE))) {
            return Optional.empty();
        }
        if (requiredBlock instanceof SoulFireBlock) return Optional.empty();

        for (Direction direction : Direction.values()) {
            if (direction == Direction.DOWN) continue;
            Object value = PrinterUtils.getPropertyByName(requiredState, direction.name());
            if (value instanceof Boolean && (Boolean) value) {
                return Optional.of(new Action()
                        .setSides(direction)
                        .setItems(Items.FLINT_AND_STEEL, Items.FIRE_CHARGE)
                        .setRequiresSupport());
            }
        }
        return Optional.of(new Action()
                .setSides(Direction.DOWN)
                .setItems(Items.FLINT_AND_STEEL, Items.FIRE_CHARGE)
                .setRequiresSupport());
    }
}
