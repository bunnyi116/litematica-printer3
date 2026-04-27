package me.aleksilassila.litematica.printer.guide.guides;

import me.aleksilassila.litematica.printer.enums.BlockMatchResult;
import me.aleksilassila.litematica.printer.guide.Guide;
import me.aleksilassila.litematica.printer.guide.Result;
import me.aleksilassila.litematica.printer.printer.SchematicBlockContext;
import me.aleksilassila.litematica.printer.printer.action.Action;
import me.aleksilassila.litematica.printer.printer.action.ClickAction;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.TurtleEggBlock;
import net.minecraft.world.item.Items;

/**
 * 海龟蛋
 */
public class TurtleEggGuide extends Guide {

    public TurtleEggGuide(SchematicBlockContext context) {
        super(context);
    }

    @Override
    protected Result onBuildActionMissingBlock(BlockMatchResult state) {
        // 首次放置：从下方点击（支撑面）
        return Result.success(new Action()
                .setSides(Direction.DOWN)
                .setRequiresSupport());
    }

    @Override
    protected Result onBuildActionWrongState(BlockMatchResult state) {
        int currentEggs = getProperty(currentState, TurtleEggBlock.EGGS).orElseThrow();
        int requiredEggs = getProperty(requiredState, TurtleEggBlock.EGGS).orElseThrow();

        // 当前蛋数少于目标 → 点击放置更多蛋
        if (currentEggs < requiredEggs) {
            return Result.success(new ClickAction().setItem(Items.TURTLE_EGG));
        }

        // 当前蛋数多于目标（不应该发生，但处理一下）
        // 或者 HATCH 状态不对 → 这种情况无法交互修正，跳过
        return Result.SKIP;
    }
}
