package me.aleksilassila.litematica.printer.guide.blocks;

import me.aleksilassila.litematica.printer.config.Configs;
import me.aleksilassila.litematica.printer.enums.BlockMatchResult;
import me.aleksilassila.litematica.printer.guide.Guide;
import me.aleksilassila.litematica.printer.guide.Result;
import me.aleksilassila.litematica.printer.printer.SchematicBlockContext;
import me.aleksilassila.litematica.printer.printer.action.ClickAction;
import me.aleksilassila.litematica.printer.Reference;
import me.aleksilassila.litematica.printer.utils.InteractionUtils;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.item.Items;

/**
 * 篝火
 */
public class CampfireGuide extends Guide {

    public CampfireGuide(SchematicBlockContext context) {
        super(context);
    }

    @Override
    protected Result onBuildActionWrongState(BlockMatchResult state) {
        Direction facing = getProperty(requiredState, CampfireBlock.FACING).orElseThrow();
        boolean requiredLit = getProperty(requiredState, CampfireBlock.LIT).orElseThrow();
        boolean currentLit = getProperty(currentState, CampfireBlock.LIT).orElseThrow();

        if (!requiredLit && currentLit) {
            return Result.success(new ClickAction().setItems(Reference.SHOVEL_ITEMS).setSides(Direction.UP));
        }
        if (requiredLit && !currentLit) {
            return Result.success(new ClickAction().setItems(Items.FLINT_AND_STEEL, Items.FIRE_CHARGE));
        }

        // 朝向不对 → 破坏
        if (Configs.Print.BREAK_WRONG_STATE_BLOCK.getBooleanValue()
                && facing != getProperty(currentState, net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING).orElse(null)) {
            InteractionUtils.INSTANCE.add(context);
        }
        return Result.SKIP;
    }
}
