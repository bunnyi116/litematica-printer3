package me.aleksilassila.litematica.printer.guide.blocks;

import me.aleksilassila.litematica.printer.enums.BlockMatchResult;
import me.aleksilassila.litematica.printer.guide.Guide;
import me.aleksilassila.litematica.printer.printer.SchematicBlockContext;
import me.aleksilassila.litematica.printer.printer.action.Action;
import me.aleksilassila.litematica.printer.printer.action.ClickAction;
import me.aleksilassila.litematica.printer.Reference;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.FlowerPotBlock;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 耕地/土径
 */
public class SoilGuide extends Guide {

    public SoilGuide(SchematicBlockContext context) {
        super(context);
    }

    @Override
    protected Optional<Action> onBuildActionMissingBlock(BlockMatchResult state, AtomicReference<Boolean> skipOtherGuide) {
        if (requiredBlock instanceof net.minecraft.world.level.block.FarmlandBlock) {
            return Optional.of(new Action().setItems(
                    Items.DIRT, Items.GRASS_BLOCK, Items.COARSE_DIRT));
        }
        if (requiredBlock instanceof net.minecraft.world.level.block.DirtPathBlock) {
            return Optional.of(new Action().setItems(
                    Items.DIRT, Items.GRASS_BLOCK, Items.COARSE_DIRT,
                    Items.ROOTED_DIRT, Items.MYCELIUM, Items.PODZOL));
        }
        return Optional.empty();
    }

    @Override
    protected Optional<Action> onBuildActionWrongBlock(BlockMatchResult state, AtomicReference<Boolean> skipOtherGuide) {
        if (requiredBlock instanceof net.minecraft.world.level.block.FarmlandBlock) {
            Block[] soilBlocks = {Blocks.GRASS_BLOCK, Blocks.DIRT, Blocks.DIRT_PATH, Blocks.COARSE_DIRT};
            for (Block soilBlock : soilBlocks) {
                if (currentBlock.equals(soilBlock)) {
                    return Optional.of(new ClickAction().setItems(Reference.HOE_ITEMS));
                }
            }
        }
        if (requiredBlock instanceof net.minecraft.world.level.block.DirtPathBlock) {
            Block[] soilBlocks = {Blocks.GRASS_BLOCK, Blocks.DIRT, Blocks.COARSE_DIRT, Blocks.ROOTED_DIRT, Blocks.MYCELIUM, Blocks.PODZOL};
            for (Block soilBlock : soilBlocks) {
                if (currentBlock.equals(soilBlock)) {
                    return Optional.of(new ClickAction().setItems(Reference.SHOVEL_ITEMS));
                }
            }
        }
        return Optional.empty();
    }

    @Override
    protected Optional<Action> onBuildActionWrongState(BlockMatchResult state, AtomicReference<Boolean> skipOtherGuide) {
        // MOISTURE 由灌溉/降雨决定，环境决定 → 跳过
        skipOtherGuide.set(true);
        return Optional.empty();
    }
}
