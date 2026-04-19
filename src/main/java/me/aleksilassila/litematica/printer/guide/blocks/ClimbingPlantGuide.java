package me.aleksilassila.litematica.printer.guide.blocks;

import me.aleksilassila.litematica.printer.enums.BlockMatchResult;
import me.aleksilassila.litematica.printer.guide.Guide;
import me.aleksilassila.litematica.printer.printer.SchematicBlockContext;
import me.aleksilassila.litematica.printer.printer.action.Action;
import net.minecraft.world.item.Items;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 攀爬植物放置指南。
 * 注册到：BigDripleafStemBlock, CaveVinesBlock, CaveVinesPlantBlock,
 *        WeepingVinesBlock, WeepingVinesPlantBlock,
 *        TwistingVinesBlock, TwistingVinesPlantBlock
 *
 * <p>MISSING：用对应物品放置。
 */
public class ClimbingPlantGuide extends Guide {

    public ClimbingPlantGuide(SchematicBlockContext context) {
        super(context);
    }

    @Override
    protected Optional<Action> onBuildActionMissingBlock(BlockMatchResult state, AtomicReference<Boolean> skipOtherGuide) {
        if (requiredBlock instanceof net.minecraft.world.level.block.BigDripleafStemBlock) {
            return Optional.of(new Action().setItem(Items.BIG_DRIPLEAF));
        }
        if (requiredBlock instanceof net.minecraft.world.level.block.CaveVinesBlock
                || requiredBlock instanceof net.minecraft.world.level.block.CaveVinesPlantBlock) {
            return Optional.of(new Action().setItem(Items.GLOW_BERRIES).setRequiresSupport());
        }
        if (requiredBlock instanceof net.minecraft.world.level.block.WeepingVinesBlock
                || requiredBlock instanceof net.minecraft.world.level.block.WeepingVinesPlantBlock) {
            return Optional.of(new Action().setItem(Items.WEEPING_VINES).setRequiresSupport());
        }
        if (requiredBlock instanceof net.minecraft.world.level.block.TwistingVinesBlock
                || requiredBlock instanceof net.minecraft.world.level.block.TwistingVinesPlantBlock) {
            return Optional.of(new Action().setItem(Items.TWISTING_VINES).setRequiresSupport());
        }
        return Optional.empty();
    }
}
