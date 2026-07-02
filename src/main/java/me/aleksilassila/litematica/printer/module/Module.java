package me.aleksilassila.litematica.printer.module;

import fi.dy.masa.malilib.config.options.ConfigBoolean;
import fi.dy.masa.malilib.config.options.ConfigOptionList;
import lombok.Getter;
import me.aleksilassila.litematica.printer.config.Configs;
import me.aleksilassila.litematica.printer.config.enums.AxisDirection;
import me.aleksilassila.litematica.printer.config.enums.WorkSingleMode;
import me.aleksilassila.litematica.printer.config.enums.WorkMode;
import me.aleksilassila.litematica.printer.printer.action.ActionManager;
import me.aleksilassila.litematica.printer.printer.WorkBox;
import me.aleksilassila.litematica.printer.utils.ConfigUtils;
import me.aleksilassila.litematica.printer.utils.BlockPosCooldownUtils;
import me.aleksilassila.litematica.printer.utils.mods.LitematicaUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.atomic.AtomicReference;

public abstract class Module extends ModuleGameVariables {
    @Getter
    protected final String id;
    protected final @Nullable AtomicReference<WorkBox> boxAtomicReference;
    @Getter
    protected final @Nullable ConfigBoolean enable;
    protected final @Nullable WorkSingleMode workSingleMode;
    protected final @Nullable ConfigOptionList selectionType;
    protected @Nullable BlockPos iterationNextBlockPos;

    private final AtomicReference<Boolean> skipOtherPosIteration = new AtomicReference<>(false);
    private long lastTickTime = -1L;

    protected Module(String id, @Nullable WorkSingleMode workSingleMode, @Nullable ConfigBoolean enable, @Nullable ConfigOptionList selectionType, boolean useBox) {
        this.id = id;
        this.workSingleMode = workSingleMode;
        this.enable = enable;
        this.selectionType = selectionType;
        this.boxAtomicReference = useBox ? new AtomicReference<>() : null;
        this.updateVariables();
    }

    public void tick() {
        int tickInterval = this.getTickWorkInterval();
        if (tickInterval > 0) {
            long currentTickTime = ModuleManager.INSTANCE.getClientTickCount();
            if (this.lastTickTime != -1L) {
                // 非首次执行
                if (currentTickTime - this.lastTickTime < tickInterval) {
                    return;
                }
            }
            this.lastTickTime = currentTickTime;
        }
        if (!ConfigUtils.isEnable()) {
            return;
        }
        if (!this.updateVariables()) {
            return;
        }
        this.onPreprocess();
        if (!this.isAllowConfigExecute()) {
            return;
        }
        if (!this.canExecute()) {
            return;
        }
        boolean interrupt = false;
        WorkBox workBox = updateWorkBox();
        if (workBox != null) {
            if (workBox.isIterationFinished()){
                workBox.resetIterator();
            }

            int maxEffectiveExec = this.getMaxEffectiveExecutionsPerTick();
            int maxTotalIter = this.getMaxTotalIterationsPerTick();
            int totalIterCount = 0;
            int effectiveExecCount = 0;
            this.skipOtherPosIteration.set(false);
            for (BlockPos blockPos : workBox) {
                if (maxTotalIter > 0 && ++totalIterCount >= maxTotalIter) {
                    interrupt = true;
                }
                if (this.skipOtherPosIteration.get() || ActionManager.INSTANCE.needWaitModifyLook) {
                    interrupt = true;
                }
                if (blockPos == null) {
                    continue;
                }
                if (isSchematicBlockHandler()) {
                    if (!LitematicaUtils.isSchematicBlock(blockPos)) {
                        continue;
                    }
                } else if (!LitematicaUtils.isWithinSelection1ModeRange(blockPos)) {
                    continue;
                }
                if (!ConfigUtils.canInteracted(blockPos)) {
                    continue;
                }
                if (selectionType != null && !ConfigUtils.isPositionInSelectionRange(player, blockPos, selectionType)) {
                    continue;
                }
                if (this.canIterationBlockPos(blockPos) && !isBlockPosOnCooldown(blockPos)) {
                    if (this.executeIterationBlockPos(blockPos, this.skipOtherPosIteration)){
                        effectiveExecCount++;
                    }
                    if (this.skipOtherPosIteration.get() || maxEffectiveExec > 0 && effectiveExecCount >= maxEffectiveExec) {
                        interrupt = true;
                    }
                }
                if (iterationNextBlockPos != null) {
                    workBox.setNextIterationPos(iterationNextBlockPos);
                    iterationNextBlockPos = null;
                    interrupt = true;
                }
                if (interrupt) {
                    break;
                }
            }
            this.onIterationEnd(workBox.isIterationFinished());
        }
    }

    protected boolean executeIterationBlockPos(BlockPos pos, AtomicReference<Boolean> skipIteration) {
        return false;
    }

    protected boolean canIterationBlockPos(BlockPos pos) {
        return true;
    }

    protected void onIterationEnd(boolean interrupt) {
    }

    protected boolean canExecute() {
        return true;
    }

    protected void onPreprocess() {
    }

    protected boolean isSchematicBlockHandler() {
        return false;
    }

    protected int getTickWorkInterval() {
        return -1;
    }

    protected int getMaxEffectiveExecutionsPerTick() {
        return -1;
    }

    protected int getMaxTotalIterationsPerTick() {
        return Configs.Core.ITERATOR_TOTAL_PER_TICK.getIntegerValue();
    }

    private @Nullable WorkBox updateWorkBox() {
        if (this.boxAtomicReference == null) {
            return null;
        }
        WorkBox box = boxAtomicReference.updateAndGet(v -> {
            if (v == null) {
                return new WorkBox(player, ConfigUtils.getWorkRange(), level);
            } else {
                v.setPlayerWorkBox(player, ConfigUtils.getWorkRange(), level);
                return v;
            }
        });
        box.setXDirection((AxisDirection) Configs.Core.AXIS_DIRECTION_X.getOptionListValue());
        box.setYDirection((AxisDirection) Configs.Core.AXIS_DIRECTION_Y.getOptionListValue());
        box.setZDirection((AxisDirection) Configs.Core.AXIS_DIRECTION_Z.getOptionListValue());
        return box;
    }

    private boolean isAllowConfigExecute() {
        if (!ConfigUtils.isEnable()) {
            return false;
        }
        if (this.workSingleMode != null && this.enable != null) {
            WorkMode modeType = (WorkMode) Configs.Core.WORK_MODE.getOptionListValue();
            return switch (modeType) {
                case SINGLE -> Configs.Core.WORK_MODE_TYPE.getOptionListValue().equals(this.workSingleMode);
                case MULTI -> this.enable.getBooleanValue();
            };
        }
        if (this.enable != null) {
            return this.enable.getBooleanValue();
        }
        return true;
    }


    public boolean isBlockPosOnCooldown(@Nullable BlockPos pos) {
        if (this.level == null || pos == null) return true;
        return BlockPosCooldownUtils.INSTANCE.isOnCooldown(this.level, this.id, pos);
    }

    public void setBlockPosCooldown(@Nullable BlockPos pos, int cooldownTicks) {
        if (this.level == null || pos == null || cooldownTicks < 1) return;
        BlockPosCooldownUtils.INSTANCE.setCooldown(this.level, this.id, pos, cooldownTicks);
    }

    protected Direction[] getPlayerOrderedByNearest() {
        return Direction.orderedByNearest(player);
    }

    protected Direction getPlayerPlacementDirection() {
        return getPlayerOrderedByNearest()[0].getOpposite();
    }
}