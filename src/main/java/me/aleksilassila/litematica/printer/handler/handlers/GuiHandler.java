package me.aleksilassila.litematica.printer.handler.handlers;

import fi.dy.masa.litematica.world.SchematicWorldHandler;
import fi.dy.masa.litematica.world.WorldSchematic;
import fi.dy.masa.malilib.config.options.ConfigBase;
import lombok.Getter;
import me.aleksilassila.litematica.printer.config.Configs;
import me.aleksilassila.litematica.printer.enums.BlockMatchResult;
import me.aleksilassila.litematica.printer.handler.ClientPlayerTickHandler;
import me.aleksilassila.litematica.printer.printer.SchematicBlockContext;
import me.aleksilassila.litematica.printer.utils.ConfigUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.LiquidBlock;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.atomic.AtomicReference;

public class GuiHandler extends ClientPlayerTickHandler {
    public final static String NAME = "gui";

    @Getter
    private final Progress totalProgress = new Progress(Configs.Core.PRINT);
    @Getter
    private final Progress printProgress = new Progress(Configs.Core.PRINT);
    @Getter
    private final Progress fluidProgress = new Progress(Configs.Core.FLUID);
    @Getter
    private final Progress fillProgress = new Progress(Configs.Core.FILL);
    @Getter
    private final Progress mineProgress = new Progress(Configs.Core.MINE);

    @Getter
    private @Nullable BlockPos blockPos = null;

    private final Progress[] progresses = new Progress[]{totalProgress, printProgress, fluidProgress, fillProgress, mineProgress};
    private boolean lastTickInterrupted = false;

    public GuiHandler() {
        super(NAME, null, Configs.Core.RENDER_HUD, null, true);
    }

    @Override
    protected boolean isSchematicBlockHandler() {
        return ConfigUtils.isPrintMode();
    }

    @Override
    protected void preprocess() {
        // 只有上一帧正常完成了（没中断），这一帧才重置统计
        if (!lastTickInterrupted) {
            for (Progress progress : progresses) {
                progress.reset();
            }
        }
    }

    @Override
    protected void executeIteration(BlockPos blockPos, AtomicReference<Boolean> skipIteration) {
        this.blockPos = blockPos;
        if (ConfigUtils.isPrintMode()) {
            WorldSchematic schematic = SchematicWorldHandler.getSchematicWorld();
            if (schematic != null) {
                SchematicBlockContext context = new SchematicBlockContext(client, level, schematic, blockPos);
                boolean isDone = BlockMatchResult.compare(context) != BlockMatchResult.MISSING;
                printProgress.add(isDone);
                totalProgress.add(isDone);
            }
        }
        if (isFluidMode()) {
            boolean isDone = level.getBlockState(blockPos).getBlock() instanceof LiquidBlock;
            fluidProgress.add(!isDone);
            totalProgress.add(!isDone);
        }
        if (isFillMode()) {
            boolean isDone = !level.getBlockState(blockPos).isAir();
            fillProgress.add(isDone);
            totalProgress.add(isDone);
        }
        if (isMineMode()) {
            boolean isDone = level.getBlockState(blockPos).isAir();
            mineProgress.add(isDone);
            totalProgress.add(isDone);
        }
    }

    @Override
    protected void stopIteration(boolean interrupt) {
        this.lastTickInterrupted = interrupt;
        if (!interrupt) {
            for (Progress progress : progresses) {
                progress.calculateProgress();
            }
        }
    }

    @Getter
    public static class Progress {
        private final ConfigBase<?> config;
        private long total;
        private long finished;
        private double progress;
        private double lastProgress;

        public Progress(ConfigBase<?> config) {
            this.config = config;
            this.total = 0;
            this.finished = 0;
            this.progress = 0.0;
        }

        public void add(boolean finished) {
            this.total++;
            if (finished) this.finished++;
            calculateProgress(); // 实时计算，保持平滑
        }

        public double getProgress() {
            return progress <= 0 ? lastProgress : progress;
        }

        public void calculateProgress() {
            if (total <= 0) {
                progress = lastProgress;
                return;
            }
            progress = (double) finished / total;
            lastProgress = progress;
        }

        public void reset() {
            this.total = 0;
            this.finished = 0;
        }
    }
}