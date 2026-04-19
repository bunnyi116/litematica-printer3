package me.aleksilassila.litematica.printer.guide.blocks;

import me.aleksilassila.litematica.printer.enums.BlockMatchResult;
import me.aleksilassila.litematica.printer.guide.Guide;
import me.aleksilassila.litematica.printer.printer.SchematicBlockContext;
import me.aleksilassila.litematica.printer.printer.action.Action;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.RailBlock;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.RailShape;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 铁轨放置指南。
 * 注册到：BaseRailBlock.class
 *
 * <p>规则：根据铁轨形状确定玩家朝向。
 */
public class RailGuide extends Guide {

    /** 铁轨形状：RAIL_SHAPE / RAIL_SHAPE_STRAIGHT */
    private final @Nullable RailShape railShape;

    public RailGuide(SchematicBlockContext context) {
        super(context);
        this.railShape = getProperty(requiredState, BlockStateProperties.RAIL_SHAPE)
                .or(() -> getProperty(requiredState, BlockStateProperties.RAIL_SHAPE_STRAIGHT))
                .orElse(null);
    }

    @Override
    protected Optional<Action> onBuildActionMissingBlock(BlockMatchResult state, AtomicReference<Boolean> skipOtherGuide) {
        if (railShape == null) return Optional.empty();

        Action action = new Action();
        if (requiredBlock instanceof RailBlock) {
            // 普通铁轨
            switch (railShape) {
                case EAST_WEST, ASCENDING_EAST -> action.setLookDirection(Direction.EAST);
                case NORTH_SOUTH, ASCENDING_NORTH -> action.setLookDirection(Direction.NORTH);
                case ASCENDING_WEST -> action.setLookDirection(Direction.WEST);
                case ASCENDING_SOUTH -> action.setLookDirection(Direction.SOUTH);
                // SOUTH_EAST 等曲线铁轨暂不处理
                default -> {}
            }
        } else {
            // 充能铁轨/探测铁轨/激活铁轨
            switch (railShape) {
                case EAST_WEST, ASCENDING_EAST -> action.setLookDirection(Direction.EAST);
                case NORTH_SOUTH, ASCENDING_NORTH -> action.setLookDirection(Direction.NORTH);
                case ASCENDING_WEST -> action.setLookDirection(Direction.WEST);
                case ASCENDING_SOUTH -> action.setLookDirection(Direction.SOUTH);
                default -> {}
            }
        }
        return Optional.of(action);
    }
}
