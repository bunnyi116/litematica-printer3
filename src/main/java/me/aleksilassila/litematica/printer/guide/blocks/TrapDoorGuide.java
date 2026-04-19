package me.aleksilassila.litematica.printer.guide.blocks;

import me.aleksilassila.litematica.printer.enums.BlockMatchResult;
import me.aleksilassila.litematica.printer.guide.Guide;
import me.aleksilassila.litematica.printer.printer.SchematicBlockContext;
import me.aleksilassila.litematica.printer.printer.action.Action;
import me.aleksilassila.litematica.printer.printer.action.ClickAction;
import me.aleksilassila.litematica.printer.config.Configs;
import me.aleksilassila.litematica.printer.utils.InteractionUtils;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.Half;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 活板门放置指南。
 * 注册到：TrapDoorBlock.class
 *
 * <p>活板门放置规则：
 * <ul>
 *   <li>TOP — 点击上面，玩家朝 facing.opposite 看（方块朝 facing 打开）</li>
 *   <li>BOTTOM — 点击下面，玩家朝 facing.opposite 看</li>
 * </ul>
 */
public class TrapDoorGuide extends Guide {

    public TrapDoorGuide(SchematicBlockContext context) {
        super(context);
    }

    @Override
    protected Optional<Action> onBuildActionMissingBlock(BlockMatchResult state, AtomicReference<Boolean> skipOtherGuide) {
        if (facing == null || half == null) return Optional.empty();

        Direction side = half == Half.TOP ? Direction.UP : Direction.DOWN;
        return Optional.of(new Action()
                .setSides(side)
                .setLookDirection(facing.getOpposite()));
    }

    @Override
    protected Optional<Action> onBuildActionWrongState(BlockMatchResult state, AtomicReference<Boolean> skipOtherGuide) {
        // 铁活板门无法手动交互
        if (requiredState.is(net.minecraft.world.level.block.Blocks.IRON_TRAPDOOR)) {
            return Optional.empty();
        }
        // 开关状态不一致 → 右键点击切换
        if (requiredState.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.OPEN)
                != currentState.getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.OPEN)) {
            return Optional.of(new ClickAction());
        }
        // 朝向不一致 → 根据配置决定是否破坏
        if (Configs.Print.BREAK_WRONG_STATE_BLOCK.getBooleanValue()
                && facing != null
                && facing != getProperty(currentState, net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING)
                        .or(() -> getProperty(currentState, net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING))
                        .orElse(null)) {
            InteractionUtils.INSTANCE.add(context);
        }
        return Optional.empty();
    }
}
