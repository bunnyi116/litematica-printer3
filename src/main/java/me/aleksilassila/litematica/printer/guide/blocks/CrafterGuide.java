package me.aleksilassila.litematica.printer.guide.blocks;

import me.aleksilassila.litematica.printer.enums.BlockMatchResult;
import me.aleksilassila.litematica.printer.guide.Guide;
import me.aleksilassila.litematica.printer.guide.Result;
import me.aleksilassila.litematica.printer.printer.SchematicBlockContext;
import me.aleksilassila.litematica.printer.printer.action.Action;
import net.minecraft.core.Direction;
import net.minecraft.core.FrontAndTop;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.Nullable;

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
    protected Result onBuildActionMissingBlock(BlockMatchResult state) {
        if (orientation == null) return Result.PASS;

        FrontAndTop ft = orientation;
        Direction facingDir = ft.front().getOpposite();
        Direction topDir = ft.top().getOpposite();

        if (ft.front() == Direction.UP) {
            return Result.success(new Action().setLookDirection(topDir, Direction.UP));
        } else if (ft.front() == Direction.DOWN) {
            return Result.success(new Action().setLookDirection(topDir.getOpposite(), Direction.DOWN));
        } else {
            return Result.success(new Action().setLookDirection(facingDir, facingDir));
        }
    }

    @Override
    protected Result onBuildActionWrongState(BlockMatchResult state) {
        // CRAFTING/TRIGGERED 由合成器内部状态和红石决定，环境决定 → 跳过
        return Result.SKIP;
    }
}
