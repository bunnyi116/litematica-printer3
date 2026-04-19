package me.aleksilassila.litematica.printer.guide;

import me.aleksilassila.litematica.printer.config.Configs;
import me.aleksilassila.litematica.printer.enums.BlockMatchResult;
import me.aleksilassila.litematica.printer.printer.SchematicBlockContext;
import me.aleksilassila.litematica.printer.printer.action.Action;
import me.aleksilassila.litematica.printer.utils.InteractionUtils;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 通用兜底指南。
 * 处理所有没有被专用 Guide 接管的方块。
 *
 * <p>优先级最低，应最后注册。
 */
public class DefaultGuide extends Guide {
    /** WRONG_STATE 破坏跳过列表：这些方块的连接状态由环境决定，破坏无意义 */
    private static final Class<?>[] BREAK_SKIP_CLASSES = {
            FenceBlock.class, WallBlock.class, IronBarsBlock.class, PressurePlateBlock.class
    };

    public DefaultGuide(SchematicBlockContext context) {
        super(context);
    }

    @Override
    protected Optional<Action> onBuildActionMissingBlock(BlockMatchResult state, AtomicReference<Boolean> skipOtherGuide) {
        Action action = new Action();

        // 1. 附着面方块（按钮、拉杆等 FaceAttachedHorizontalDirectionalBlock）
        if (requiredBlock instanceof FaceAttachedHorizontalDirectionalBlock && facing != null && attachFace != null) {
            Direction sidePitch = attachFace == AttachFace.CEILING ? Direction.UP
                    : attachFace == AttachFace.FLOOR ? Direction.DOWN
                    : facing;
            Direction clickSide = attachFace == AttachFace.WALL ? facing : facing.getOpposite();
            return Optional.of(action.setSides(clickSide).setLookDirection(clickSide.getOpposite(), sidePitch));
        }

        // 2. 轴向方块（原木、锁链等）
        if (axis != null) {
            action.setSides(axis);
        } else if (horizontalAxis != null) {
            action.setSides(horizontalAxis);
        }

        // 3. 朝向方块
        if (facing != null && axis == null && horizontalAxis == null) {
            // 水平方向方块（HorizontalDirectionalBlock、石切机等）
            if (requiredBlock instanceof HorizontalDirectionalBlock
                    || requiredBlock instanceof StonecutterBlock
                    //#if MC >= 11904
                    || requiredBlock instanceof net.minecraft.world.level.block.FlowerBedBlock
                    //#endif
            ) {
                // 栅栏门已由 FenceGateGuide 处理，这里不再特殊反向
                action.setLookDirection(facing.getOpposite());
            }
            // BaseEntityBlock（营火、装饰盆等）
            if (requiredBlock instanceof BaseEntityBlock) {
                if (requiredState.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
                    Direction entityFacing = facing;
                    //#if MC >= 11904
                    if (requiredBlock instanceof DecoratedPotBlock || requiredBlock instanceof CampfireBlock) {
                        entityFacing = entityFacing.getOpposite();
                    }
                    //#endif
                    action.setSides(entityFacing).setLookDirection(entityFacing.getOpposite());
                }
                if (requiredState.hasProperty(BlockStateProperties.FACING)) {
                    Direction entityFacing = facing;
                    if (requiredBlock instanceof ShulkerBoxBlock) {
                        entityFacing = entityFacing.getOpposite();
                        action.setShift();
                    }
                    action.setSides(entityFacing).setLookDirection(entityFacing.getOpposite());
                }
            }
            // 普通观察器、楼梯、栅栏门：看正向
            if (requiredBlock instanceof ObserverBlock
                    || requiredBlock instanceof StairBlock
                    || requiredBlock instanceof FenceGateBlock) {
                action.setLookDirection(facing);
            } else if (!(requiredBlock instanceof HorizontalDirectionalBlock)
                    && !(requiredBlock instanceof BaseEntityBlock)) {
                // 其余方块反向放置
                action.setLookDirection(facing.getOpposite());
            }
        }

        // 4. Half 属性兜底
        if (half != null && facing == null) {
            action.setSides(half == net.minecraft.world.level.block.state.properties.Half.BOTTOM
                    ? Direction.DOWN : Direction.UP);
        }

        // 5. 需要支撑的方块
        if (requiredBlock instanceof RodBlock
                || requiredBlock instanceof TorchBlock
                || requiredBlock instanceof FlowerBlock) {
            action.setRequiresSupport();
        }

        // 6. 方块型珊瑚替换
        if (Configs.Print.REPLACE_CORAL.getBooleanValue()
                && requiredBlock.getDescriptionId().endsWith("_coral_block")) {
            String type = requiredBlock.getDescriptionId()
                    .replace("block.minecraft.dead_", "")
                    .replace("_coral_block", "");
            action.setRequiresSupport();
            switch (type) {
                case "tube" -> action.setItem(net.minecraft.world.item.Items.TUBE_CORAL_BLOCK);
                case "brain" -> action.setItem(net.minecraft.world.item.Items.BRAIN_CORAL_BLOCK);
                case "bubble" -> action.setItem(net.minecraft.world.item.Items.BUBBLE_CORAL_BLOCK);
                case "fire" -> action.setItem(net.minecraft.world.item.Items.FIRE_CORAL_BLOCK);
                case "horn" -> action.setItem(net.minecraft.world.item.Items.HORN_CORAL_BLOCK);
            }
        }

        return Optional.of(action);
    }

    @Override
    protected Optional<Action> onBuildActionWrongState(BlockMatchResult state, AtomicReference<Boolean> skipOtherGuide) {
        if (!Configs.Print.BREAK_WRONG_STATE_BLOCK.getBooleanValue()) {
            return Optional.empty();
        }
        if (Arrays.asList(BREAK_SKIP_CLASSES).contains(requiredBlock.getClass())) {
            return Optional.empty();
        }
        InteractionUtils.INSTANCE.add(context);
        return Optional.empty();
    }

    @Override
    protected Optional<Action> onBuildActionWrongBlock(BlockMatchResult state, AtomicReference<Boolean> skipOtherGuide) {
        if (Configs.Print.REPLACE_CORAL.getBooleanValue()
                && requiredBlock.getDescriptionId().contains("coral")) {
            return Optional.empty();
        }
        boolean printBreakWrongBlock = Configs.Print.BREAK_WRONG_BLOCK.getBooleanValue();
        boolean printBreakExtraBlock = Configs.Print.BREAK_EXTRA_BLOCK.getBooleanValue();
        if (printBreakWrongBlock || printBreakExtraBlock) {
            if (InteractionUtils.canBreakBlock(blockPos)) {
                if (printBreakWrongBlock && !requiredState.isAir()) {
                    InteractionUtils.INSTANCE.add(context);
                } else if (printBreakExtraBlock && requiredState.isAir()) {
                    InteractionUtils.INSTANCE.add(context);
                }
            }
        }
        return Optional.empty();
    }
}
