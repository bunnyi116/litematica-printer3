package me.aleksilassila.litematica.printer.guide.guides;

import me.aleksilassila.litematica.printer.enums.BlockMatchResult;
import me.aleksilassila.litematica.printer.guide.Guide;
import me.aleksilassila.litematica.printer.guide.Result;
import me.aleksilassila.litematica.printer.printer.SchematicBlockContext;
import me.aleksilassila.litematica.printer.printer.action.Action;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.EndRodBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 末地烛/避雷针放置
 */
public class RodGuide extends Guide {

    public RodGuide(SchematicBlockContext context) {
        super(context);
    }

    @Override
    protected Result onBuildActionMissingBlock(BlockMatchResult state) {
        Direction facing = getProperty(requiredState, EndRodBlock.FACING).orElseThrow();

        // 末地烛的特殊逻辑
        if (requiredBlock instanceof EndRodBlock) {
            BlockState forwardState = level.getBlockState(blockPos.relative(facing));
            // 前面有反向末地烛 → 点击 facing 方向
            if (forwardState.is(requiredBlock)
                    && getProperty(forwardState, EndRodBlock.FACING).orElseThrow() == facing.getOpposite()) {
                return Result.success(new Action().setSides(facing));
            }
            // 投影中前面有同向末地烛 → 等待
            BlockState forwardSchematic = schematic.getBlockState(blockPos.relative(facing));
            if (forwardSchematic.is(requiredBlock)
                    && getProperty(forwardSchematic, EndRodBlock.FACING).orElseThrow() == facing) {
                if (forwardSchematic == forwardState) {
                    return Result.success(new Action().setSides(facing.getOpposite()));
                }
                return Result.SKIP;
            }
        }

        return Result.success(new Action().setSides(facing.getOpposite()));
    }
}
