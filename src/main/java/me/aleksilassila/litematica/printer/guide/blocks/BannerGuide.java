package me.aleksilassila.litematica.printer.guide.blocks;

import me.aleksilassila.litematica.printer.enums.BlockMatchResult;
import me.aleksilassila.litematica.printer.guide.Guide;
import me.aleksilassila.litematica.printer.printer.SchematicBlockContext;
import me.aleksilassila.litematica.printer.printer.action.Action;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.BannerBlock;
import net.minecraft.world.level.block.WallBannerBlock;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 旗帜放置指南。
 * 注册到：AbstractBannerBlock.class（覆盖站立/墙壁两类）
 *
 * <p>放置规则：
 * <ul>
 *   <li>站立旗帜（BannerBlock）→ 点击下方，按 rotation 设置偏转角</li>
 *   <li>墙壁旗帜（WallBannerBlock）→ 点击对应墙面，玩家朝向与面一致</li>
 * </ul>
 */
public class BannerGuide extends Guide {

    public BannerGuide(SchematicBlockContext context) {
        super(context);
    }

    @Override
    protected Optional<Action> onBuildActionMissingBlock(BlockMatchResult state, AtomicReference<Boolean> skipOtherGuide) {
        if (requiredBlock instanceof BannerBlock) {
            int rotation = requiredState.getValue(BannerBlock.ROTATION);
            return Optional.of(new Action()
                    .setSides(Direction.DOWN)
                    .setLookRotation(rotation)
                    .setRequiresSupport());
        }
        if (requiredBlock instanceof WallBannerBlock && facing != null) {
            return Optional.of(new Action()
                    .setSides(facing.getOpposite())
                    .setLookDirection(facing.getOpposite())
                    .setRequiresSupport());
        }
        return Optional.empty();
    }
}
