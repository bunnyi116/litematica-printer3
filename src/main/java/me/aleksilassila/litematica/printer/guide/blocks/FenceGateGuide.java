package me.aleksilassila.litematica.printer.guide.blocks;

import me.aleksilassila.litematica.printer.enums.BlockMatchResult;
import me.aleksilassila.litematica.printer.guide.Guide;
import me.aleksilassila.litematica.printer.printer.SchematicBlockContext;
import me.aleksilassila.litematica.printer.printer.action.Action;
import me.aleksilassila.litematica.printer.printer.action.ClickAction;
import me.aleksilassila.litematica.printer.config.Configs;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 栅栏门放置 / 交互指南。
 * 注册到：FenceGateBlock.class
 *
 * <p>放置规则：栅栏门朝向需要反向（玩家朝 facing 看，门朝 facing.opposite 打开）
 *
 * <p>交互规则（WRONG_STATE）：
 * <ul>
 *   <li>朝向相反或开关状态不同时 → 发送点击交互</li>
 *   <li>其他情况按配置决定是否破坏</li>
 * </ul>
 */
public class FenceGateGuide extends Guide {

    public FenceGateGuide(SchematicBlockContext context) {
        super(context);
    }

    @Override
    protected Optional<Action> onBuildActionMissingBlock(BlockMatchResult state, AtomicReference<Boolean> skipOtherGuide) {
        if (facing == null) return Optional.empty();
        // 栅栏门：玩家朝 facing 看（反向）
        return Optional.of(new Action().setLookDirection(facing));
    }

    @Override
    protected Optional<Action> onBuildActionWrongState(BlockMatchResult state, AtomicReference<Boolean> skipOtherGuide) {
        if (facing == null) return Optional.empty();

        Direction currentFacing = getProperty(currentState, BlockStateProperties.HORIZONTAL_FACING).orElse(null);
        boolean openMismatch = getProperty(requiredState, BlockStateProperties.OPEN)
                .map(open -> !open.equals(getProperty(currentState, BlockStateProperties.OPEN).orElse(null)))
                .orElse(false);

        if (facing.getOpposite() == currentFacing || openMismatch) {
            return Optional.of(new ClickAction()
                    .setSides(facing.getOpposite())
                    .setLookDirection(facing));
        }
        if (Configs.Print.BREAK_WRONG_STATE_BLOCK.getBooleanValue()) {
            me.aleksilassila.litematica.printer.utils.InteractionUtils.INSTANCE.add(context);
        }
        return Optional.empty();
    }
}
