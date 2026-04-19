package me.aleksilassila.litematica.printer.guide.blocks;

import me.aleksilassila.litematica.printer.config.Configs;
import me.aleksilassila.litematica.printer.enums.BlockMatchResult;
import me.aleksilassila.litematica.printer.guide.Guide;
import me.aleksilassila.litematica.printer.printer.SchematicBlockContext;
import me.aleksilassila.litematica.printer.printer.action.Action;
import me.aleksilassila.litematica.printer.printer.action.ClickAction;
import me.aleksilassila.litematica.printer.Reference;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 去皮原木放置/交互指南。
 * 注册到：RotatedPillarBlock.class
 *
 * <p>放置：按轴方向放置，配置开启后可同时接受原版/去皮版本。
 * WRONG_BLOCK：当前方块可去皮成目标时，使用斧头右键。
 */
public class StripLogGuide extends Guide {

    @SuppressWarnings("all")
    private static final Map<Block, Block> STRIPPED_LOGS =
            net.fabricmc.fabric.mixin.content.registry.AxeItemAccessor.getStrippables();

    public StripLogGuide(SchematicBlockContext context) {
        super(context);
    }

    @Override
    protected Optional<Action> onBuildActionMissingBlock(BlockMatchResult state, AtomicReference<Boolean> skipOtherGuide) {
        if (axis == null) return Optional.empty();

        Action action = new Action().setSides(axis);

        // 配置启用去皮时，可接受原版或去皮版本
        if (Configs.Print.STRIP_LOGS.getBooleanValue()) {
            for (Map.Entry<Block, Block> entry : STRIPPED_LOGS.entrySet()) {
                if (requiredBlock == entry.getValue()) {
                    action.setItems(entry.getValue().asItem(), entry.getKey().asItem());
                    return Optional.of(action);
                }
            }
        }

        return Optional.of(action);
    }

    @Override
    protected Optional<Action> onBuildActionWrongBlock(BlockMatchResult state, AtomicReference<Boolean> skipOtherGuide) {
        // 当前方块可以去皮成目标方块 → 用斧头右键
        Block stripped = STRIPPED_LOGS.get(currentBlock);
        if (stripped != null && stripped == requiredBlock) {
            return Optional.of(new ClickAction().setItems(Reference.AXE_ITEMS));
        }
        return Optional.empty();
    }
}
