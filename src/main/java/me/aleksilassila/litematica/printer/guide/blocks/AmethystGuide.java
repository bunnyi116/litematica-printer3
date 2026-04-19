package me.aleksilassila.litematica.printer.guide.blocks;

import me.aleksilassila.litematica.printer.enums.BlockMatchResult;
import me.aleksilassila.litematica.printer.guide.Guide;
import me.aleksilassila.litematica.printer.printer.SchematicBlockContext;
import me.aleksilassila.litematica.printer.printer.action.Action;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 紫水晶芽放置指南。
 * 注册到：AmethystClusterBlock.class
 *
 * <p>规则：反向 facing 放置，需要支撑。
 * 使用 onBuildAction 拦截（在 canSurvive 检查之前），因为倒置放置时 canSurvive 可能返回 false。
 */
public class AmethystGuide extends Guide {

    public AmethystGuide(SchematicBlockContext context) {
        super(context);
    }

    @Override
    protected Optional<Action> onBuildAction(BlockMatchResult state, AtomicReference<Boolean> skipOtherGuide) {
        // 只处理 MISSING：WRONG_STATE / WRONG_BLOCK 交给后续逻辑
        if (state != BlockMatchResult.MISSING) return Optional.empty();

        Direction attachDirection = getProperty(requiredState, AmethystClusterBlock.FACING)
                .orElse(Direction.UP).getOpposite();;

        skipOtherGuide.set(true);
        return Optional.of(new Action()
                .setSides(attachDirection)
                .setRequiresSupport());
    }
}
