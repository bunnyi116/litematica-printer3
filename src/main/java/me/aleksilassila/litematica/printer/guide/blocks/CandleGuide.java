package me.aleksilassila.litematica.printer.guide.blocks;

import me.aleksilassila.litematica.printer.config.Configs;
import me.aleksilassila.litematica.printer.enums.BlockMatchResult;
import me.aleksilassila.litematica.printer.guide.Guide;
import me.aleksilassila.litematica.printer.printer.SchematicBlockContext;
import me.aleksilassila.litematica.printer.printer.action.Action;
import me.aleksilassila.litematica.printer.printer.action.ClickAction;
import me.aleksilassila.litematica.printer.utils.InteractionUtils;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.item.Items;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 蜡烛放置/交互指南。
 * 注册到：CandleBlock.class
 *
 * <p>WRONG_STATE：
 * <ul>
 *   <li>数量不够 → 右键添加蜡烛</li>
 *   <li>需要点燃 → 打火石/火焰弹</li>
 *   <li>需要熄灭 → 右键</li>
 * </ul>
 */
public class CandleGuide extends Guide {

    public CandleGuide(SchematicBlockContext context) {
        super(context);
    }

    @Override
    protected Optional<Action> onBuildActionWrongState(BlockMatchResult state, AtomicReference<Boolean> skipOtherGuide) {
        int requiredCandles = requiredState.getValue(BlockStateProperties.CANDLES);
        int currentCandles = currentState.getValue(BlockStateProperties.CANDLES);
        boolean requiredLit = requiredState.getValue(CandleBlock.LIT);
        boolean currentLit = currentState.getValue(CandleBlock.LIT);

        // 添加蜡烛
        if (currentCandles < requiredCandles) {
            return Optional.of(new ClickAction().setItem(requiredBlock.asItem()));
        }
        // 点燃
        if (!currentLit && requiredLit) {
            return Optional.of(new ClickAction().setItems(Items.FLINT_AND_STEEL, Items.FIRE_CHARGE));
        }
        // 熄灭
        if (currentLit && !requiredLit) {
            return Optional.of(new ClickAction());
        }

        if (Configs.Print.BREAK_WRONG_STATE_BLOCK.getBooleanValue()) {
            InteractionUtils.INSTANCE.add(context);
        }
        return Optional.empty();
    }
}
