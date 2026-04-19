package me.aleksilassila.litematica.printer.guide.blocks;

import me.aleksilassila.litematica.printer.enums.BlockMatchResult;
import me.aleksilassila.litematica.printer.guide.Guide;
import me.aleksilassila.litematica.printer.printer.SchematicBlockContext;
import me.aleksilassila.litematica.printer.printer.action.Action;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 床放置指南。
 * 注册到：BedBlock.class
 *
 * <p>放置规则：
 * <ul>
 *   <li>只放置 FOOT（床尾）部分，HEAD 由游戏自动在 facing 方向生成</li>
 *   <li>玩家朝 facing 方向看（背对放置面）</li>
 * </ul>
 */
public class BedGuide extends Guide {

    /** 床的部分（床头/床尾）：BED_PART */
    private final @Nullable BedPart bedPart;

    public BedGuide(SchematicBlockContext context) {
        super(context);
        this.bedPart = getProperty(requiredState, BlockStateProperties.BED_PART).orElse(null);
    }

    @Override
    protected Optional<Action> onBuildActionMissingBlock(BlockMatchResult state, AtomicReference<Boolean> skipOtherGuide) {
        if (facing == null || bedPart == null) return Optional.empty();

        // 只放置床尾，床头自动生成
        if (bedPart == BedPart.HEAD) return Optional.empty();

        return Optional.of(new Action().setLookDirection(facing));
    }
}
