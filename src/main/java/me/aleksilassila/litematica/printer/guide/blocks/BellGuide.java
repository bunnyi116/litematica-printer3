package me.aleksilassila.litematica.printer.guide.blocks;

import me.aleksilassila.litematica.printer.enums.BlockMatchResult;
import me.aleksilassila.litematica.printer.guide.Guide;
import me.aleksilassila.litematica.printer.printer.SchematicBlockContext;
import me.aleksilassila.litematica.printer.printer.action.Action;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.BellAttachType;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 铃铛放置指南。
 * 注册到：BellBlock.class
 *
 * <p>放置规则：
 * <ul>
 *   <li>FLOOR → 点击下方</li>
 *   <li>CEILING → 点击上方</li>
 *   <li>SINGLE_WALL / DOUBLE_WALL → 点击 facing 方向，玩家不需要特定朝向</li>
 * </ul>
 */
public class BellGuide extends Guide {

    /** 钟的附着类型：BELL_ATTACHMENT */
    private final @Nullable BellAttachType bellAttachment;

    public BellGuide(SchematicBlockContext context) {
        super(context);
        this.bellAttachment = getProperty(requiredState, BlockStateProperties.BELL_ATTACHMENT).orElse(null);
    }

    @Override
    protected Optional<Action> onBuildActionMissingBlock(BlockMatchResult state, AtomicReference<Boolean> skipOtherGuide) {
        if (bellAttachment == null || facing == null) return Optional.empty();

        Direction side = switch (bellAttachment) {
            case FLOOR -> Direction.DOWN;
            case CEILING -> Direction.UP;
            default -> facing;
        };

        // 墙挂铃铛需要朝向
        Direction look = (bellAttachment == BellAttachType.SINGLE_WALL || bellAttachment == BellAttachType.DOUBLE_WALL)
                ? null : facing;

        return Optional.of(new Action().setSides(side).setLookDirection(look));
    }
}
