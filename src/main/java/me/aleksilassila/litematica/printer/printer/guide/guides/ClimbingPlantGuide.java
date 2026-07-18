package me.aleksilassila.litematica.printer.printer.guide.guides;

import me.aleksilassila.litematica.printer.printer.guide.BlockMatchResult;
import me.aleksilassila.litematica.printer.printer.guide.Guide;
import me.aleksilassila.litematica.printer.printer.guide.Result;
import me.aleksilassila.litematica.printer.printer.SchematicBlockContext;
import me.aleksilassila.litematica.printer.printer.action.Action;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.*;

/**
 * 攀爬植物
 */
public class ClimbingPlantGuide extends Guide {

    public ClimbingPlantGuide(SchematicBlockContext context) {
        super(context);
    }

    @Override
    protected Result onBuildActionMissingBlock(BlockMatchResult state) {
        if (requiredBlock instanceof BigDripleafStemBlock) {
            return Result.success(new Action().setItem(Items.BIG_DRIPLEAF));
        }
        if (requiredBlock instanceof CaveVinesBlock
                || requiredBlock instanceof CaveVinesPlantBlock) {
            return Result.success(new Action().setItem(Items.GLOW_BERRIES).setNeedSupportBlock());
        }
        if (requiredBlock instanceof WeepingVinesBlock
                || requiredBlock instanceof WeepingVinesPlantBlock) {
            return Result.success(new Action().setItem(Items.WEEPING_VINES).setNeedSupportBlock());
        }
        if (requiredBlock instanceof TwistingVinesBlock
                || requiredBlock instanceof TwistingVinesPlantBlock) {
            return Result.success(new Action().setItem(Items.TWISTING_VINES).setNeedSupportBlock());
        }
        return Result.skipOtherGuide();
    }

    @Override
    protected Result onBuildActionWrongState(BlockMatchResult state) {
        return Result.skipOtherGuide();
    }
}
