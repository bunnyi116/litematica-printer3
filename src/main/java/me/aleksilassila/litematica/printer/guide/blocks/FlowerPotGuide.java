package me.aleksilassila.litematica.printer.guide.blocks;

import me.aleksilassila.litematica.printer.enums.BlockMatchResult;
import me.aleksilassila.litematica.printer.guide.Guide;
import me.aleksilassila.litematica.printer.guide.Result;
import me.aleksilassila.litematica.printer.printer.SchematicBlockContext;
import me.aleksilassila.litematica.printer.printer.action.Action;
import me.aleksilassila.litematica.printer.printer.action.ClickAction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.item.Items;

/**
 * 花盆
 */
public class FlowerPotGuide extends Guide {

    public FlowerPotGuide(SchematicBlockContext context) {
        super(context);
    }

    @Override
    protected Result onBuildActionMissingBlock(BlockMatchResult state) {
        return Result.success(new Action().setItem(Items.FLOWER_POT));
    }

    @Override
    protected Result onBuildActionWrongBlock(BlockMatchResult state) {
        if (requiredBlock instanceof FlowerPotBlock potBlock) {
            Block content = potBlock.getPotted();
            if (content != Blocks.AIR) {
                return Result.success(new ClickAction().setItem(content.asItem()));
            }
        }
        return Result.SKIP;
    }

    @Override
    protected Result onBuildActionWrongState(BlockMatchResult state) {
        return Result.SKIP;
    }
}
