package me.aleksilassila.litematica.printer.guide.blocks;

import me.aleksilassila.litematica.printer.config.Configs;
import me.aleksilassila.litematica.printer.enums.BlockMatchResult;
import me.aleksilassila.litematica.printer.guide.Guide;
import me.aleksilassila.litematica.printer.printer.SchematicBlockContext;
import me.aleksilassila.litematica.printer.printer.action.Action;
import me.aleksilassila.litematica.printer.printer.action.ClickAction;
import me.aleksilassila.litematica.printer.Reference;
import me.aleksilassila.litematica.printer.utils.InteractionUtils;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.item.Items;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 篝火
 */
public class CampfireGuide extends Guide {

    public CampfireGuide(SchematicBlockContext context) {
        super(context);
    }

    @Override
    protected Optional<Action> onBuildActionWrongState(BlockMatchResult state, AtomicReference<Boolean> skipOtherGuide) {
        boolean requiredLit = requiredState.getValue(CampfireBlock.LIT);
        boolean currentLit = currentState.getValue(CampfireBlock.LIT);

        if (!requiredLit && currentLit) {
            return Optional.of(new ClickAction().setItems(Reference.SHOVEL_ITEMS).setSides(Direction.UP));
        }
        if (requiredLit && !currentLit) {
            return Optional.of(new ClickAction().setItems(Items.FLINT_AND_STEEL, Items.FIRE_CHARGE));
        }

        // 朝向不对 → 破坏
        if (Configs.Print.BREAK_WRONG_STATE_BLOCK.getBooleanValue()
                && facing != null
                && facing != getProperty(currentState, net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING).orElse(null)) {
            InteractionUtils.INSTANCE.add(context);
        }
        return Optional.empty();
    }
}
