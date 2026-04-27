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
    public CrafterGuide(SchematicBlockContext context) {
        super(context);
    }

    @Override
    protected Result onBuildActionMissingBlock(BlockMatchResult state) {
        FrontAndTop  frontAndTop = getProperty(requiredState, BlockStateProperties.ORIENTATION).orElseThrow();
        Direction facing = frontAndTop.front().getOpposite();
        Direction rotation = frontAndTop.top().getOpposite();
        if (facing == Direction.UP) {
            return Result.success(new Action().setLookDirection(rotation, Direction.UP));
        } else if (facing == Direction.DOWN) {
            return Result.success(new Action().setLookDirection(rotation.getOpposite(), Direction.DOWN));
        } else {
            return Result.success(new Action().setLookDirection(facing, facing));
        }
    }

    @Override
    protected Result onBuildActionWrongState(BlockMatchResult state) {
        return Result.SKIP;
    }
}
