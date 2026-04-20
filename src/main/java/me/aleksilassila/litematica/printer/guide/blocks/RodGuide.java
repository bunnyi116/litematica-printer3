package me.aleksilassila.litematica.printer.guide.blocks;

import me.aleksilassila.litematica.printer.enums.BlockMatchResult;
import me.aleksilassila.litematica.printer.guide.Guide;
import me.aleksilassila.litematica.printer.printer.SchematicBlockContext;
import me.aleksilassila.litematica.printer.printer.action.Action;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.EndRodBlock;
import net.minecraft.world.level.block.RodBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 末地烛/避雷针放置
 */
public class RodGuide extends Guide {

    public RodGuide(SchematicBlockContext context) {
        super(context);
    }

    @Override
    protected Optional<Action> onBuildActionMissingBlock(BlockMatchResult state, AtomicReference<Boolean> skipOtherGuide) {
        if (facing == null) return Optional.empty();

        // 末地烛的特殊逻辑
        if (requiredBlock instanceof EndRodBlock) {
            BlockState forwardState = level.getBlockState(blockPos.relative(facing));
            // 前面有反向末地烛 → 点击 facing 方向
            if (forwardState.is(requiredBlock)
                    && forwardState.getValue(EndRodBlock.FACING) == facing.getOpposite()) {
                return Optional.of(new Action().setSides(facing));
            }
            // 投影中前面有同向末地烛 → 等待
            BlockState forwardSchematic = schematic.getBlockState(blockPos.relative(facing));
            if (forwardSchematic.is(requiredBlock)
                    && forwardSchematic.getValue(EndRodBlock.FACING) == facing) {
                if (forwardSchematic == forwardState) {
                    return Optional.of(new Action().setSides(facing.getOpposite()));
                }
                return Optional.empty();
            }
        }

        return Optional.of(new Action().setSides(facing.getOpposite()));
    }
}
