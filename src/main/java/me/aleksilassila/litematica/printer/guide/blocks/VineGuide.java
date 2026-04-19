package me.aleksilassila.litematica.printer.guide.blocks;

import me.aleksilassila.litematica.printer.config.Configs;
import me.aleksilassila.litematica.printer.enums.BlockMatchResult;
import me.aleksilassila.litematica.printer.guide.Guide;
import me.aleksilassila.litematica.printer.printer.SchematicBlockContext;
import me.aleksilassila.litematica.printer.printer.action.Action;
import me.aleksilassila.litematica.printer.printer.action.ClickAction;
import me.aleksilassila.litematica.printer.printer.PrinterUtils;
import me.aleksilassila.litematica.printer.utils.InteractionUtils;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.item.Items;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 藤蔓/发光地衣放置/交互指南。
 * 注册到：VineBlock.class, GlowLichenBlock.class
 *
 * <p>MISSING：找到第一个非 DOWN 的方向属性，点击对应面放置。
 * WRONG_STATE：同上逻辑放置来修正方向。
 */
public class VineGuide extends Guide {

    public VineGuide(SchematicBlockContext context) {
        super(context);
    }

    @Override
    protected Optional<Action> onBuildActionMissingBlock(BlockMatchResult state, AtomicReference<Boolean> skipOtherGuide) {
        for (Direction direction : Direction.values()) {
            if (direction == Direction.DOWN && requiredBlock instanceof net.minecraft.world.level.block.VineBlock) continue;
            Object value = PrinterUtils.getPropertyByName(requiredState, direction.name());
            if (value instanceof Boolean && (Boolean) value) {
                return Optional.of(new Action().setSides(direction));
            }
        }
        return Optional.empty();
    }

    @Override
    protected Optional<Action> onBuildActionWrongState(BlockMatchResult state, AtomicReference<Boolean> skipOtherGuide) {
        for (Direction direction : Direction.values()) {
            if (direction == Direction.DOWN && requiredBlock instanceof net.minecraft.world.level.block.VineBlock) continue;
            Object value = PrinterUtils.getPropertyByName(requiredState, direction.name());
            if (value instanceof Boolean && (Boolean) value) {
                return Optional.of(new Action().setSides(direction).setLookDirection(direction));
            }
        }
        if (Configs.Print.BREAK_WRONG_STATE_BLOCK.getBooleanValue()) {
            InteractionUtils.INSTANCE.add(context);
        }
        return Optional.empty();
    }
}
