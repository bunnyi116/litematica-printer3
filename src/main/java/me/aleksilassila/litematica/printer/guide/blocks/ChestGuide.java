package me.aleksilassila.litematica.printer.guide.blocks;

import me.aleksilassila.litematica.printer.config.Configs;
import me.aleksilassila.litematica.printer.enums.BlockMatchResult;
import me.aleksilassila.litematica.printer.guide.Guide;
import me.aleksilassila.litematica.printer.printer.SchematicBlockContext;
import me.aleksilassila.litematica.printer.printer.action.Action;
import me.aleksilassila.litematica.printer.utils.InteractionUtils;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 箱子放置指南。
 * 注册到：ChestBlock.class, TrappedChestBlock.class
 *
 * <p>放置规则：
 * <ul>
 *   <li>单箱子 → 如果周围有箱子，需要潜行放置；否则直接放置</li>
 *   <li>双箱子（LEFT/RIGHT）→ 先确保另一半已放置，点击对应方向，潜行/不潜行取决于是否有其他箱子</li>
 * </ul>
 */
public class ChestGuide extends Guide {

    public ChestGuide(SchematicBlockContext context) {
        super(context);
    }

    @Override
    protected Optional<Action> onBuildActionMissingBlock(BlockMatchResult state, AtomicReference<Boolean> skipOtherGuide) {
        if (facing == null) return Optional.empty();

        Direction facingOpposite = facing.getOpposite();
        ChestType chestType = getProperty(requiredState, BlockStateProperties.CHEST_TYPE).orElse(ChestType.SINGLE);

        // 收集所有不与其他箱子相邻的面
        Map<Direction, Vec3> noChestSides = new HashMap<>();
        for (Direction side : Direction.values()) {
            if (level.getBlockState(blockPos.relative(side)).getBlock() instanceof ChestBlock) {
                continue;
            }
            noChestSides.put(side, Vec3.ZERO);
        }

        if (chestType == ChestType.SINGLE) {
            // 有水平方向的箱子邻居 → 潜行放置（防止自动合并）
            boolean hasChestNeighbor = Direction.Plane.HORIZONTAL.stream()
                    .anyMatch(s -> !noChestSides.containsKey(s));
            if (hasChestNeighbor) {
                return Optional.of(new Action().setLookDirection(facingOpposite).setShift());
            }
            return Optional.of(new Action().setSides(noChestSides).setLookDirection(facingOpposite));
        }

        // 双箱子：不潜行放置，让 Minecraft 自动合并
        // 无论另一半是否已放，都不能潜行，否则会阻止合并
        Direction partnerDir = chestType == ChestType.LEFT
                ? facing.getCounterClockWise()
                : facing.getClockWise();

        Map<Direction, Vec3> clickSides = new HashMap<>(noChestSides);
        clickSides.put(partnerDir, Vec3.ZERO);  // 也允许从另一半方向点击

        return Optional.of(new Action()
                .setSides(clickSides)
                .setLookDirection(facingOpposite)
                .setShift(false));
    }

    @Override
    protected Optional<Action> onBuildActionWrongState(BlockMatchResult state, AtomicReference<Boolean> skipOtherGuide) {
        ChestType requiredType = getProperty(requiredState, BlockStateProperties.CHEST_TYPE).orElse(ChestType.SINGLE);

        // 只处理双箱子状态不匹配（当前 SINGLE 但需要 LEFT/RIGHT，或反过来）
        if (requiredType == ChestType.SINGLE) {
            // 目标是单箱但当前不是 → 破坏重放（潜行放单箱）
            if (Configs.Print.BREAK_WRONG_STATE_BLOCK.getBooleanValue()) {
                InteractionUtils.INSTANCE.add(context);
            }
            return Optional.empty();
        }

        // 目标是双箱子：当前箱子类型不对，需要破坏后重新放置来触发自动合并
        if (Configs.Print.BREAK_WRONG_STATE_BLOCK.getBooleanValue()) {
            InteractionUtils.INSTANCE.add(context);
        }
        return Optional.empty();
    }
}
