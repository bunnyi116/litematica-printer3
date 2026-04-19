package me.aleksilassila.litematica.printer.guide.blocks;

import me.aleksilassila.litematica.printer.enums.BlockMatchResult;
import me.aleksilassila.litematica.printer.guide.Guide;
import me.aleksilassila.litematica.printer.printer.SchematicBlockContext;
import me.aleksilassila.litematica.printer.printer.action.Action;
import me.aleksilassila.litematica.printer.printer.action.ClickAction;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.TurtleEggBlock;
import net.minecraft.world.item.Items;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 海龟蛋放置/交互指南。
 * 注册到：TurtleEggBlock.class
 *
 * <p>海龟蛋需要支撑方块（沙子/红沙），可以叠加放置 1-4 个。
 * 放置时从下方（沙子面）点击，叠加时右键点击已有海龟蛋。
 */
public class TurtleEggGuide extends Guide {

    public TurtleEggGuide(SchematicBlockContext context) {
        super(context);
    }

    @Override
    protected Optional<Action> onBuildActionMissingBlock(BlockMatchResult state, AtomicReference<Boolean> skipOtherGuide) {
        // 首次放置：从下方点击（支撑面）
        return Optional.of(new Action()
                .setSides(Direction.DOWN)
                .setRequiresSupport());
    }

    @Override
    protected Optional<Action> onBuildActionWrongState(BlockMatchResult state, AtomicReference<Boolean> skipOtherGuide) {
        int currentEggs = currentState.getValue(TurtleEggBlock.EGGS);
        int requiredEggs = requiredState.getValue(TurtleEggBlock.EGGS);

        // 当前蛋数少于目标 → 点击放置更多蛋
        if (currentEggs < requiredEggs) {
            return Optional.of(new ClickAction().setItem(Items.TURTLE_EGG));
        }

        // 当前蛋数多于目标（不应该发生，但处理一下）
        // 或者 HATCH 状态不对 → 这种情况无法交互修正，跳过
        return Optional.empty();
    }
}
