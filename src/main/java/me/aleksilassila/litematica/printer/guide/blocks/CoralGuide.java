package me.aleksilassila.litematica.printer.guide.blocks;

import me.aleksilassila.litematica.printer.config.Configs;
import me.aleksilassila.litematica.printer.enums.BlockMatchResult;
import me.aleksilassila.litematica.printer.guide.Guide;
import me.aleksilassila.litematica.printer.printer.SchematicBlockContext;
import me.aleksilassila.litematica.printer.printer.action.Action;
import me.aleksilassila.litematica.printer.utils.minecraft.BlockUtils;
import me.aleksilassila.litematica.printer.utils.minecraft.IdentifierUtils;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.BaseCoralWallFanBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 死珊瑚
 */
public class CoralGuide extends Guide {

    private final Identifier blockId;

    public CoralGuide(SchematicBlockContext context) {
        super(context);
        this.blockId = BlockUtils.getKey(requiredBlock);
    }

    @Override
    public boolean canExecute(AtomicReference<Boolean> skipOtherGuide) {
        return blockId.toString().contains("coral");
    }

    @Override
    protected Optional<Action> onBuildActionMissingBlock(BlockMatchResult state, AtomicReference<Boolean> skipOtherGuide) {
        List<Item> items = new ArrayList<>();
        items.add(requiredBlock.asItem());

        if (Configs.Print.REPLACE_CORAL.getBooleanValue()) {
            Identifier coralId = IdentifierUtils.of(blockId.toString().replace("dead_", ""));
            if (!blockId.equals(coralId)) {
                items.add(BlockUtils.getBlock(coralId).asItem());
            }
        }

        Action action = new Action();
        action.setItems(items.toArray(new Item[0]));

        if (!blockId.getPath().contains("_block")) {
            getProperty(requiredState, BlockStateProperties.HORIZONTAL_FACING)
                    .ifPresent(facing -> action.setSides(facing.getOpposite()));
            action.setRequiresSupport();
        }

        return Optional.of(action);
    }
}
