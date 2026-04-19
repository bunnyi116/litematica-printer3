package me.aleksilassila.litematica.printer.guide.blocks;

import me.aleksilassila.litematica.printer.config.Configs;
import me.aleksilassila.litematica.printer.enums.BlockMatchResult;
import me.aleksilassila.litematica.printer.guide.Guide;
import me.aleksilassila.litematica.printer.printer.SchematicBlockContext;
import me.aleksilassila.litematica.printer.printer.action.Action;
import me.aleksilassila.litematica.printer.printer.action.ClickAction;
import me.aleksilassila.litematica.printer.utils.InteractionUtils;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 花簇交互指南（MC 1.19.4+）。
 * 注册到：FlowerBedBlock.class / PinkPetalsBlock.class
 *
 * <p>WRONG_STATE：花数量不够 → 右键添加。
 */
//#if MC >= 11904
public class FlowerBedGuide extends Guide {

    public FlowerBedGuide(SchematicBlockContext context) {
        super(context);
    }

    @Override
    protected Optional<Action> onBuildActionWrongState(BlockMatchResult state, AtomicReference<Boolean> skipOtherGuide) {
        int requiredAmount = requiredState.getValue(BlockStateProperties.FLOWER_AMOUNT);
        int currentAmount = currentState.getValue(BlockStateProperties.FLOWER_AMOUNT);
        if (currentAmount <= requiredAmount) {
            return Optional.of(new ClickAction().setItem(requiredBlock.asItem()));
        }
        if (Configs.Print.BREAK_WRONG_STATE_BLOCK.getBooleanValue()) {
            InteractionUtils.INSTANCE.add(context);
        }
        return Optional.empty();
    }
}
//#endif
