package me.aleksilassila.litematica.printer.guide.blocks;

import me.aleksilassila.litematica.printer.config.Configs;
import me.aleksilassila.litematica.printer.enums.BlockMatchResult;
import me.aleksilassila.litematica.printer.guide.Guide;
import me.aleksilassila.litematica.printer.guide.Result;
import me.aleksilassila.litematica.printer.printer.SchematicBlockContext;
import me.aleksilassila.litematica.printer.printer.action.ClickAction;
import me.aleksilassila.litematica.printer.utils.InteractionUtils;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.ComparatorBlock;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.List;

/**
 * 红石比较器
 */
public class ComparatorGuide extends Guide {

    public ComparatorGuide(SchematicBlockContext context) {
        super(context);
    }

    @Override
    protected Result onBuildActionWrongState(BlockMatchResult state) {
        if (!getProperty(requiredState, ComparatorBlock.MODE).equals(getProperty(currentState, ComparatorBlock.MODE))) {
            return Result.success(new ClickAction());
        }
        // 模式相同但状态不对 → 检查信号和输入端
        Direction requiredFacing = getProperty(requiredState, ComparatorBlock.FACING).orElse(null);
        if (Configs.Print.BREAK_WRONG_STATE_BLOCK.getBooleanValue() && requiredFacing != null) {
            Direction currentFacing = getProperty(currentState, BlockStateProperties.FACING).orElse(null);
            if (requiredFacing == currentFacing) {
                SchematicBlockContext facingFirstBlockCtx = context.offset(requiredFacing);
                // 检验输出信号
                if (level.getSignal(blockPos, requiredFacing) != schematic.getSignal(blockPos, requiredFacing)) {
                    // 检验输入端是否为"能输出比较器信号方块"
                    if (facingFirstBlockCtx.requiredState.hasAnalogOutputSignal()) {
                        return Result.PASS;
                    }
                    // 检验输入端非透明方块
                    if (facingFirstBlockCtx.requiredState.isRedstoneConductor(facingFirstBlockCtx.level, facingFirstBlockCtx.blockPos)) {
                        SchematicBlockContext facingSecondBlockCtx = facingFirstBlockCtx.offset(requiredFacing);
                        // 仿照原版检验物品展示框
                        net.minecraft.core.BlockPos blockPos2 = facingSecondBlockCtx.blockPos;
                        List<net.minecraft.world.entity.decoration.ItemFrame> itemFrameList = facingSecondBlockCtx.schematic.getEntitiesOfClass(
                                net.minecraft.world.entity.decoration.ItemFrame.class,
                                new net.minecraft.world.phys.AABB(blockPos2),
                                (itemFrame) -> itemFrame.getDirection() == requiredFacing
                        );
                        // 隔非透明方块检验容器
                        if (facingSecondBlockCtx.requiredState.hasAnalogOutputSignal()) {
                            return Result.PASS;
                        }
                        // 隔非透明方块检验物品展示框
                        if (!itemFrameList.isEmpty()) {
                            return Result.PASS;
                        }
                    }
                }
            }
            InteractionUtils.INSTANCE.add(context);
        }
        return Result.SKIP;
    }
}
