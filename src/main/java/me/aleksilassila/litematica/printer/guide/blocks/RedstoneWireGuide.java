package me.aleksilassila.litematica.printer.guide.blocks;

import me.aleksilassila.litematica.printer.enums.BlockMatchResult;
import me.aleksilassila.litematica.printer.guide.Guide;
import me.aleksilassila.litematica.printer.printer.SchematicBlockContext;
import me.aleksilassila.litematica.printer.printer.action.Action;
import me.aleksilassila.litematica.printer.printer.action.ClickAction;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.RedstoneSide;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 红石线交互指南。
 * 注册到：RedStoneWireBlock.class
 *
 * <p>WRONG_STATE：十字形红石线可点击变成点状。
 */
public class RedstoneWireGuide extends Guide {

    /** 红石线四方连接状态 */
    private final @Nullable RedstoneSide northRedstone;
    private final @Nullable RedstoneSide eastRedstone;
    private final @Nullable RedstoneSide southRedstone;
    private final @Nullable RedstoneSide westRedstone;

    public RedstoneWireGuide(SchematicBlockContext context) {
        super(context);
        this.northRedstone = getProperty(requiredState, BlockStateProperties.NORTH_REDSTONE).orElse(null);
        this.eastRedstone = getProperty(requiredState, BlockStateProperties.EAST_REDSTONE).orElse(null);
        this.southRedstone = getProperty(requiredState, BlockStateProperties.SOUTH_REDSTONE).orElse(null);
        this.westRedstone = getProperty(requiredState, BlockStateProperties.WEST_REDSTONE).orElse(null);
    }

    @Override
    protected Optional<Action> onBuildActionWrongState(BlockMatchResult state, AtomicReference<Boolean> skipOtherGuide) {
        boolean allNoneRequired = northRedstone == RedstoneSide.NONE
                && southRedstone == RedstoneSide.NONE
                && eastRedstone == RedstoneSide.NONE
                && westRedstone == RedstoneSide.NONE;

        boolean allSideCurrent = northRedstone == RedstoneSide.SIDE
                && southRedstone == RedstoneSide.SIDE
                && eastRedstone == RedstoneSide.SIDE
                && westRedstone == RedstoneSide.SIDE;

        if (allNoneRequired && allSideCurrent) {
            return Optional.of(new ClickAction().setItem(net.minecraft.world.item.Items.AIR));
        }
        return Optional.empty();
    }
}
