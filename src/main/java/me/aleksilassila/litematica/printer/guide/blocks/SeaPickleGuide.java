package me.aleksilassila.litematica.printer.guide.blocks;

import me.aleksilassila.litematica.printer.config.Configs;
import me.aleksilassila.litematica.printer.enums.BlockMatchResult;
import me.aleksilassila.litematica.printer.guide.Guide;
import me.aleksilassila.litematica.printer.printer.SchematicBlockContext;
import me.aleksilassila.litematica.printer.printer.action.Action;
import me.aleksilassila.litematica.printer.printer.action.ClickAction;
import me.aleksilassila.litematica.printer.utils.InteractionUtils;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.SeaPickleBlock;
import net.minecraft.world.item.Items;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 海泡菜
 */
public class SeaPickleGuide extends Guide {

    public SeaPickleGuide(SchematicBlockContext context) {
        super(context);
    }

    @Override
    protected Optional<Action> onBuildActionMissingBlock(BlockMatchResult state, AtomicReference<Boolean> skipOtherGuide) {
        // 海泡菜只能放在支撑方块上（珊瑚块/海晶石等），需要从下方点击放置
        return Optional.of(new Action()
                .setSides(Direction.DOWN)
                .setRequiresSupport());
    }

    @Override
    protected Optional<Action> onBuildActionWrongState(BlockMatchResult state, AtomicReference<Boolean> skipOtherGuide) {
        if (currentState.getBlock() instanceof SeaPickleBlock) {
            if (currentState.getValue(SeaPickleBlock.PICKLES) < requiredState.getValue(SeaPickleBlock.PICKLES)) {
                return Optional.of(new ClickAction().setItem(Items.SEA_PICKLE));
            }
            if (Configs.Print.BREAK_WRONG_STATE_BLOCK.getBooleanValue()) {
                InteractionUtils.INSTANCE.add(context);
            }
        }
        return Optional.empty();
    }
}
