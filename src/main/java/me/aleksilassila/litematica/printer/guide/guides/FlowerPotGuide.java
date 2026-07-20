package me.aleksilassila.litematica.printer.guide.guides;

import me.aleksilassila.litematica.printer.config.Configs;
import me.aleksilassila.litematica.printer.guide.BlockMatchResult;
import me.aleksilassila.litematica.printer.guide.Guide;
import me.aleksilassila.litematica.printer.guide.Result;
import me.aleksilassila.litematica.printer.printer.SchematicBlockContext;
import me.aleksilassila.litematica.printer.action.Action;
import me.aleksilassila.litematica.printer.action.ClickAction;
import me.aleksilassila.litematica.printer.utils.InteractionUtils;
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
        if (requiredBlock instanceof FlowerPotBlock rPotBlock) {
            Block rPotted = rPotBlock.getPotted();
            if (rPotted != Blocks.AIR) {
                if (currentBlock instanceof FlowerPotBlock cPotBlock) {
                    Block cPotted = cPotBlock.getPotted();
                    if (cPotted == Blocks.AIR) {
                        return Result.success(new ClickAction().setItem(rPotted.asItem()).setNeedSupportBlock());
                    } else if (Configs.Print.BREAK_WRONG_BLOCK.getBooleanValue()
                            && rPotted != cPotted
                            && InteractionUtils.canBreakBlock(blockPos)) {
                        InteractionUtils.INSTANCE.add(context);
                    }
                }
            }
        }
        return Result.skipOtherGuide();
    }

    @Override
    protected Result onBuildActionWrongState(BlockMatchResult state) {
        return Result.skipOtherGuide();
    }
}
