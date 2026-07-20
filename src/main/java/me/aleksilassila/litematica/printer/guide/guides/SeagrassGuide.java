package me.aleksilassila.litematica.printer.guide.guides;

import me.aleksilassila.litematica.printer.guide.BlockMatchResult;
import me.aleksilassila.litematica.printer.guide.Guide;
import me.aleksilassila.litematica.printer.guide.Result;
import me.aleksilassila.litematica.printer.printer.SchematicBlockContext;
import me.aleksilassila.litematica.printer.action.Action;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

/**
 * 海草
 */
public class SeagrassGuide extends Guide {

    public SeagrassGuide(SchematicBlockContext context) {
        super(context);
    }

    @Override
    protected Result onBuildActionMissingBlock(BlockMatchResult state) {
        FluidState realFluid = level.getFluidState(blockPos);
        // 流体
        boolean waterOk = realFluid.is(FluidTags.WATER) && realFluid.getAmount() == 8;

        // 下方方块校验基底
        BlockState belowRealState = level.getBlockState(blockPos.below());
        boolean baseOk = belowRealState.isFaceSturdy(level, blockPos.below(), Direction.UP)
                //#if MC >= 260100
                && !belowRealState.is(net.minecraft.tags.BlockTags.CANNOT_SUPPORT_SEAGRASS);
                //#else
                //$$ && !belowRealState.is(net.minecraft.world.level.block.Blocks.MAGMA_BLOCK);
                //#endif

        // 两项都满足才放置蓝图中的海草状态
        if (waterOk && baseOk) {
            return Result.success(new Action()
                    .setSides(Direction.DOWN)
                    .setNeedSupportBlock());
        }
        return Result.skipOtherGuide();
    }

    @Override
    protected Result onBuildActionWrongBlock(BlockMatchResult state) {
        return Result.skipOtherGuide();
    }


}
