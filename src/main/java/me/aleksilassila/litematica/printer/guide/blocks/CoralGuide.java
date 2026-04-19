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
import net.minecraft.world.level.block.BaseCoralWallFanBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 死珊瑚放置指南。
 * 注册到：Block.class（仅匹配珊瑚方块）
 *
 * <p>MISSING：
 * <ul>
 *   <li>配置开启替换时，可接受活珊瑚/死珊瑚两种版本</li>
 *   <li>墙扇珊瑚：点击 facing.opposite 面</li>
 *   <li>方块珊瑚：需要支撑</li>
 * </ul>
 *
 * <p>注意：由于注册到 Block.class，需要在 canExecute 中过滤非珊瑚方块。
 */
public class CoralGuide extends Guide {

    public CoralGuide(SchematicBlockContext context) {
        super(context);
    }

    @Override
    public boolean canExecute(AtomicReference<Boolean> skipOtherGuide) {
        Identifier blockId = BlockUtils.getKey(requiredBlock);
        if (!blockId.toString().contains("coral")) {
            // 不是珊瑚，传递给下一个 Guide
            return false;
        }
        return true;
    }

    @Override
    protected Optional<Action> onBuildActionMissingBlock(BlockMatchResult state, AtomicReference<Boolean> skipOtherGuide) {
        Identifier blockId = BlockUtils.getKey(requiredBlock);
        boolean isBlock = blockId.toString().contains("block");

        Action action = new Action();
        List<net.minecraft.world.item.Item> items = new ArrayList<>();
        items.add(requiredBlock.asItem());

        // 配置开启时接受活珊瑚
        if (Configs.Print.REPLACE_CORAL.getBooleanValue()) {
            Identifier liveCoralId = IdentifierUtils.of(blockId.toString().replace("dead_", ""));
            if (!blockId.equals(liveCoralId)) {
                Block liveCoral = BlockUtils.getBlock(liveCoralId);
                if (liveCoral != null) {
                    items.add(liveCoral.asItem());
                }
            }
        }

        action.setItems(items.toArray(new net.minecraft.world.item.Item[0]));

        if (!isBlock) {
            boolean isWallFan = requiredBlock instanceof BaseCoralWallFanBlock;
            Direction facing = isWallFan
                    ? getProperty(requiredState, BlockStateProperties.HORIZONTAL_FACING).orElse(Direction.DOWN)
                    : Direction.DOWN;
            action.setSides(facing).setRequiresSupport();
        }

        return Optional.of(action);
    }
}
