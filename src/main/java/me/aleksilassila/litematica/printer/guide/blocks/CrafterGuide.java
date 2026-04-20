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
 * 合成器
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

    @Override
    protected Optional<Action> onBuildActionWrongState(BlockMatchResult state, AtomicReference<Boolean> skipOtherGuide) {
        // CRAFTING/TRIGGERED 由合成器内部状态和红石决定，环境决定 → 跳过
        skipOtherGuide.set(true);
        return Optional.empty();
    }
}
