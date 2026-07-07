package me.aleksilassila.litematica.printer.printer.guide.guides;

import me.aleksilassila.litematica.printer.config.Configs;
import me.aleksilassila.litematica.printer.printer.guide.BlockMatchResult;
import me.aleksilassila.litematica.printer.printer.guide.Guide;
import me.aleksilassila.litematica.printer.printer.guide.Result;
import me.aleksilassila.litematica.printer.printer.SchematicBlockContext;
import me.aleksilassila.litematica.printer.printer.action.Action;
import me.aleksilassila.litematica.printer.printer.action.ClickAction;
import me.aleksilassila.litematica.printer.utils.minecraft.BlockUtils;
import me.aleksilassila.litematica.printer.utils.InteractionUtils;
import me.aleksilassila.litematica.printer.utils.InventoryUtils;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.item.Items;

import java.util.Optional;

/**
 * 农作物
 */
public class CropsGuide extends Guide {

    public CropsGuide(SchematicBlockContext context) {
        super(context);
    }

    @Override
    protected Result onBuildActionMissingBlock(BlockMatchResult state) {
        String blockKey = BlockUtils.getKeyString(requiredBlock);
        if (blockKey.contains("pumpkin")) {
            return Result.success(new Action().setItem(Items.PUMPKIN_SEEDS).setNeedSupportBlock());
        }
        if (blockKey.contains("melon")) {
            return Result.success(new Action().setItem(Items.MELON_SEEDS).setNeedSupportBlock());
        }
        return Result.success(new Action());
    }

    @Override
    protected Result onBuildActionWrongState(BlockMatchResult state) {
        if (!Configs.Print.BONEMEAL_CROPS.getBooleanValue()) {
            return Result.skipOtherGuide();
        }

        Direction facing = getProperty(requiredState, BlockStateProperties.HORIZONTAL_FACING).orElse(null);

        // 茎类（StemBlock/AttachedStemBlock）：AGE 是生长阶段，facing 朝向不对应破坏重放
        if (requiredBlock instanceof StemBlock || requiredBlock instanceof AttachedStemBlock) {
            if (facing != null
                    && currentState.hasProperty(BlockStateProperties.HORIZONTAL_FACING)
                    && !getProperty(currentState, BlockStateProperties.HORIZONTAL_FACING).equals(Optional.of(facing))) {
                return Result.passToNext(); // facing 不对 → 放置性错误，交给 DefaultGuide 破坏重放
            }
            // AGE 由生长决定，跳过
            return Result.skipOtherGuide();
        }

        // 农作物（CropBlock）和甜菜根（BeetrootBlock）：骨粉催熟
        if (currentBlock == requiredBlock
                && InventoryUtils.playerHasAccessToItem(client.player, Items.BONE_MEAL)) {
            IntegerProperty ageProp;
            int maxAge;
            if (requiredBlock instanceof BeetrootBlock) {
                ageProp = BeetrootBlock.AGE;
                maxAge = 3;
            } else if (requiredBlock instanceof CropBlock cropBlock) {
                ageProp = CropBlock.AGE;
                maxAge = cropBlock.getMaxAge();
            } else {
                return Result.skipOtherGuide();
            }
            int requiredAge = getProperty(requiredState, ageProp).orElse(0);
            int currentAge = getProperty(currentState, ageProp).orElse(0);
            if (requiredAge == maxAge && currentAge < maxAge) {
                return Result.success(new ClickAction().setItem(Items.BONE_MEAL));
            }
        }
        return Result.skipOtherGuide();
    }

    @Override
    protected Result onBuildActionWrongBlock(BlockMatchResult state) {
        if (!Configs.Print.BREAK_WRONG_BLOCK.getBooleanValue()) {
            return Result.skipOtherGuide();
        }
        String requiredKey = BlockUtils.getKeyString(requiredBlock);
        String currentKey = BlockUtils.getKeyString(currentBlock);
        if (requiredKey.contains("pumpkin_stem") && !currentKey.contains("pumpkin_stem")) {
            InteractionUtils.INSTANCE.add(context);
        } else if (requiredKey.contains("melon_stem") && !currentKey.contains("melon_stem")) {
            InteractionUtils.INSTANCE.add(context);
        }
        return Result.skipOtherGuide();
    }
}
