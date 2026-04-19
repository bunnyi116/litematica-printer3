package me.aleksilassila.litematica.printer.guide.blocks;

import me.aleksilassila.litematica.printer.enums.BlockMatchResult;
import me.aleksilassila.litematica.printer.guide.Guide;
import me.aleksilassila.litematica.printer.printer.SchematicBlockContext;
import me.aleksilassila.litematica.printer.printer.action.Action;
import net.minecraft.core.Direction;
import net.minecraft.core.FrontAndTop;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 合成器放置指南（MC 1.21+）。
 * 注册到：CrafterBlock.class
 *
 * <p>合成器使用 ORIENTATION（FrontAndTop），需要同时确定 yaw 和 pitch 朝向。
 * 放置规则：
 * <ul>
 *   <li>front 朝上 → 看反向 front 的水平方向，俯视</li>
 *   <li>front 朝下 → 看反向 top 的水平方向，仰视</li>
 *   <li>front 水平 → 直接看 front 方向</li>
 * </ul>
 */
public class CrafterGuide extends Guide {

    /** 前+上方向：ORIENTATION */
    private final @Nullable FrontAndTop orientation;

    public CrafterGuide(SchematicBlockContext context) {
        super(context);
        this.orientation = getProperty(requiredState, BlockStateProperties.ORIENTATION).orElse(null);
    }

    @Override
    protected Optional<Action> onBuildActionMissingBlock(BlockMatchResult state, AtomicReference<Boolean> skipOtherGuide) {
        if (orientation == null) return Optional.empty();

        FrontAndTop ft = orientation;
        Direction facingDir = ft.front().getOpposite();
        Direction topDir = ft.top().getOpposite();

        if (ft.front() == Direction.UP) {
            return Optional.of(new Action().setLookDirection(topDir, Direction.UP));
        } else if (ft.front() == Direction.DOWN) {
            return Optional.of(new Action().setLookDirection(topDir.getOpposite(), Direction.DOWN));
        } else {
            return Optional.of(new Action().setLookDirection(facingDir, facingDir));
        }
    }
}
