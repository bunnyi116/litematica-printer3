package me.aleksilassila.litematica.printer.guide.blocks;

import me.aleksilassila.litematica.printer.config.Configs;
import me.aleksilassila.litematica.printer.enums.BlockMatchResult;
import me.aleksilassila.litematica.printer.guide.Guide;
import me.aleksilassila.litematica.printer.printer.SchematicBlockContext;
import me.aleksilassila.litematica.printer.printer.action.Action;
import me.aleksilassila.litematica.printer.printer.action.ClickAction;
import me.aleksilassila.litematica.printer.utils.minecraft.BlockUtils;
import me.aleksilassila.litematica.printer.utils.InteractionUtils;
import me.aleksilassila.litematica.printer.utils.InventoryUtils;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.item.Items;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 农作物放置/交互指南。
 * 注册到：AttachedStemBlock.class, StemBlock.class, CropBlock.class, BeetrootBlock.class
 *
 * <p>MISSING：南瓜茎用南瓜种子，西瓜茎用西瓜种子。
 * WRONG_STATE：骨粉催熟（配置开启时）。
 */
public class CropsGuide extends Guide {

    public CropsGuide(SchematicBlockContext context) {
        super(context);
    }

    @Override
    protected Optional<Action> onBuildActionMissingBlock(BlockMatchResult state, AtomicReference<Boolean> skipOtherGuide) {
        String blockKey = BlockUtils.getKeyString(requiredBlock);
        if (blockKey.contains("pumpkin")) {
            return Optional.of(new Action().setItem(Items.PUMPKIN_SEEDS).setRequiresSupport());
        }
        if (blockKey.contains("melon")) {
            return Optional.of(new Action().setItem(Items.MELON_SEEDS).setRequiresSupport());
        }
        return Optional.of(new Action());
    }

    @Override
    protected Optional<Action> onBuildActionWrongState(BlockMatchResult state, AtomicReference<Boolean> skipOtherGuide) {
        if (!Configs.Print.BONEMEAL_CROPS.getBooleanValue()) return Optional.empty();

        if (currentBlock == requiredBlock
                && InventoryUtils.playerHasAccessToItem(client.player, Items.BONE_MEAL)) {
            int maxAge = requiredBlock instanceof BeetrootBlock ? 3 : 7;
            var ageProp = requiredBlock instanceof BeetrootBlock ? BeetrootBlock.AGE : StemBlock.AGE;
            int requiredAge = requiredState.getValue(ageProp);
            int currentAge = currentState.getValue(ageProp);
            if (requiredAge == maxAge && currentAge < maxAge) {
                return Optional.of(new ClickAction().setItem(Items.BONE_MEAL));
            }
        }
        return Optional.empty();
    }

    @Override
    protected Optional<Action> onBuildActionWrongBlock(BlockMatchResult state, AtomicReference<Boolean> skipOtherGuide) {
        String requiredKey = BlockUtils.getKeyString(requiredBlock);
        String currentKey = BlockUtils.getKeyString(currentBlock);
        if (requiredKey.contains("pumpkin_stem") && !currentKey.contains("pumpkin_stem")) {
            InteractionUtils.INSTANCE.add(context);
        } else if (requiredKey.contains("melon_stem") && !currentKey.contains("melon_stem")) {
            InteractionUtils.INSTANCE.add(context);
        }
        return Optional.empty();
    }
}
