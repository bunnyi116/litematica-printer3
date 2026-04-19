package me.aleksilassila.litematica.printer.guide.blocks;

import me.aleksilassila.litematica.printer.config.Configs;
import me.aleksilassila.litematica.printer.enums.BlockMatchResult;
import me.aleksilassila.litematica.printer.guide.Guide;
import me.aleksilassila.litematica.printer.printer.SchematicBlockContext;
import me.aleksilassila.litematica.printer.printer.action.Action;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.ObserverBlock;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 活塞放置指南。
 * 注册到：PistonBaseBlock.class
 *
 * <p>安全放置模式下，检查四周侦测器的输入端方块状态。
 */
public class PistonGuide extends Guide {

    public PistonGuide(SchematicBlockContext context) {
        super(context);
    }

    @Override
    protected Optional<Action> onBuildActionMissingBlock(BlockMatchResult state, AtomicReference<Boolean> skipOtherGuide) {
        if (facing == null) return Optional.empty();

        if (!Configs.Print.SAFELY_OBSERVER.getBooleanValue()) {
            return Optional.of(new Action().setLookDirection(facing.getOpposite()));
        }

        // 检查活塞四周的侦测器链
        for (Direction direction : Direction.values()) {
            SchematicBlockContext temp = context.offset(direction);
            while (temp.requiredState.getBlock() instanceof ObserverBlock) {
                Direction tempObserverFacing = temp.requiredState.hasProperty(BlockStateProperties.FACING)
                        ? temp.requiredState.getValue(BlockStateProperties.FACING) : null;
                if (tempObserverFacing != null) {
                    SchematicBlockContext offset = temp.offset(tempObserverFacing);
                    if (tempObserverFacing == direction) {
                        if (BlockMatchResult.compare(offset) != BlockMatchResult.CORRECT) {
                            return Optional.empty();
                        }
                    }
                    temp = offset;
                } else {
                    break;
                }
            }
        }

        return Optional.of(new Action().setLookDirection(facing.getOpposite()));
    }
}
