package me.aleksilassila.litematica.printer.module;

import fi.dy.masa.malilib.config.options.ConfigBoolean;
import fi.dy.masa.malilib.config.options.ConfigOptionList;
import lombok.Getter;
import me.aleksilassila.litematica.printer.config.Configs;
import me.aleksilassila.litematica.printer.config.enums.IterationOrderType;
import me.aleksilassila.litematica.printer.config.enums.WorkSingleMode;
import me.aleksilassila.litematica.printer.config.enums.WorkMode;
import me.aleksilassila.litematica.printer.printer.ActionManager;
import me.aleksilassila.litematica.printer.printer.WorkBox;
import me.aleksilassila.litematica.printer.utils.ConfigUtils;
import me.aleksilassila.litematica.printer.utils.BlockPosCooldownUtils;
import me.aleksilassila.litematica.printer.utils.mods.LitematicaUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.atomic.AtomicReference;

public abstract class Module extends ConfigUtils {
    // 为实现类预制了一些常用的MC变量
    protected Minecraft mc;
    protected ClientLevel level;
    protected LocalPlayer player;
    protected ClientPacketListener connection;
    protected MultiPlayerGameMode gameMode;
    protected GameType gameType;
    protected @Nullable HitResult hitResult;
    protected @Nullable BlockHitResult blockHitResult;

    @Getter
    protected final String id;

    @Getter
    protected final @Nullable AtomicReference<WorkBox> currentWorkBox;
    private @Nullable WorkBox lastWorkBox;
    private @Nullable BlockPos lastPlayerPos;

    @Getter
    protected final @Nullable ConfigBoolean enable;

    @Getter
    private final @Nullable WorkSingleMode workSingleMode;

    @Getter
    private final @Nullable ConfigOptionList selectionType;

    private final AtomicReference<Boolean> skipIteration = new AtomicReference<>(false);

    private long lastTickTime = -1L;

    protected Module(String id, @Nullable WorkSingleMode workSingleMode, @Nullable ConfigBoolean enable, @Nullable ConfigOptionList selectionType, boolean useBox) {
        this.id = id;
        this.workSingleMode = workSingleMode;
        this.enable = enable;
        this.selectionType = selectionType;
        this.currentWorkBox = useBox ? new AtomicReference<>() : null;
        this.updateVariables();
    }

    protected boolean updateVariables() {
        this.mc = Minecraft.getInstance();
        this.level = mc.level;
        this.player = mc.player;
        this.connection = mc.getConnection();
        this.gameMode = mc.gameMode;
        this.gameType = mc.gameMode == null ? null : mc.gameMode.getPlayerMode();
        this.hitResult = mc.hitResult;
        if (mc.hitResult != null && mc.hitResult.getType() == HitResult.Type.BLOCK) {
            this.blockHitResult = (BlockHitResult) mc.hitResult;
        } else {
            this.blockHitResult = null;
        }
        return this.mc != null && this.level != null && this.player != null && this.connection != null && this.gameMode != null && this.gameType != null;
    }

    public void tick() {
        int tickInterval = this.getTickWorkInterval(); // 工作间隔
        if (tickInterval > 0) {
            long currentTickTime = ModuleManager.getCurrentHandlerTime();
            if (this.lastTickTime != -1L) {
                // 非首次执行
                if (currentTickTime - this.lastTickTime < tickInterval) {
                    return;
                }
            }
            this.lastTickTime = currentTickTime; // 更新上次执行时间，首次执行也会初始化
        }
        if (!isEnable()) {
            this.lastPlayerPos = null;
            return;
        }
        if (!this.updateVariables()) {
            this.lastPlayerPos = null;
            return;
        }
        // 更新迭代范围
        if (this.currentWorkBox != null) {
            BlockPos playerPos = this.player.blockPosition();
            int workRange = getWorkRange();
            double threshold = workRange * 0.7; // 玩家移动阈值：工作范围的70%
            @Nullable WorkBox playerInteractionBox = this.currentWorkBox.get();
            if (playerInteractionBox == null
                    || !playerInteractionBox.equals(this.lastWorkBox)
                    || this.lastPlayerPos == null
                    || !this.lastPlayerPos.closerThan(playerPos, threshold)
            ) {
                this.lastPlayerPos = playerPos;
                playerInteractionBox = new WorkBox(playerPos, workRange);
                this.lastWorkBox = playerInteractionBox;
                this.currentWorkBox.set(playerInteractionBox);
            }
            // 同步交互盒的迭代配置：从全局配置读取迭代顺序、方向等
            playerInteractionBox.setIterationMode((IterationOrderType) Configs.Core.ITERATION_ORDER.getOptionListValue());
            playerInteractionBox.setXIncrement(!Configs.Core.X_REVERSE.getBooleanValue());
            playerInteractionBox.setYIncrement(!Configs.Core.Y_REVERSE.getBooleanValue());
            playerInteractionBox.setZIncrement(!Configs.Core.Z_REVERSE.getBooleanValue());
        }
        this.onPreprocess(); // 运行前处理的事情
        if (!this.isAllowConfigExecute()) {
            this.lastPlayerPos = null;
            return;
        }
        boolean interrupt = false;
        // 执行迭代业务任务：基于玩家交互盒的方块迭代处理（防主线程阻塞）
        if (this.currentWorkBox != null && this.canExecute()) {
            this.onIterationStart();
            WorkBox playerInteractionBox = this.currentWorkBox.get();
            // 交互盒非空且满足迭代执行条件时，执行迭代逻辑
            if (playerInteractionBox != null && canIterate()) {
                int maxEffectiveExec = this.getMaxEffectiveExecutionsPerTick();
                int maxTotalIter = this.getMaxTotalIterationsPerTick();
                int totalIterCount = 0;
                int effectiveExecCount = 0;
                this.skipIteration.set(false);
                // 开始迭代方块
                for (BlockPos pos : playerInteractionBox) {
                    // 单Tick迭代次数限制：达到最大次数则终止循环（防主线程阻塞）
                    if (maxTotalIter > 0 && ++totalIterCount >= maxTotalIter) {
                        interrupt = true;
                        break;
                    }
                    if (this.skipIteration.get() || ActionManager.INSTANCE.needWaitModifyLook) {
                        interrupt = true;
                        break;
                    }
                    if (pos == null) continue;
                    if (isSchematicBlockHandler()) {
                        if (!LitematicaUtils.isSchematicBlock(pos)) {
                            continue;
                        }
                    } else if (!LitematicaUtils.isWithinSelection1ModeRange(pos)) {
                        continue;
                    }
                    if (selectionType != null && !ConfigUtils.isPositionInSelectionRange(player, pos, selectionType)) {
                        continue;
                    }
                    // 方块迭代权限校验：子类可重写实现自定义过滤逻辑
                    if (this.canIterationBlockPos(pos) && !isBlockPosOnCooldown(pos)) {
                        this.executeIterationBlockPos(pos, this.skipIteration);
                        if (this.skipIteration.get() || maxEffectiveExec > 0 && ++effectiveExecCount >= maxEffectiveExec) {
                            interrupt = true;
                        }
                    }
                    if (interrupt) {
                        break;
                    }
                }
                this.onIterationEnd(interrupt);
            }
        }
        if (!interrupt) {
            this.lastPlayerPos = null;
        }
    }

    protected void onIterationEnd(boolean interrupt) {
    }

    protected void onIterationStart() {
    }

    protected void onPreprocess() {
    }

    protected boolean isSchematicBlockHandler() {
        return false;
    }

    private boolean isAllowConfigExecute() {
        // 全局打印机功能未启用，直接禁止所有处理器执行
        if (!ConfigUtils.isEnable()) {
            return false;
        }
        // 处理器绑定了模式和配置，按当前游戏模式校验
        if (this.workSingleMode != null && this.enable != null) {
            WorkMode modeType = (WorkMode) Configs.Core.WORK_MODE.getOptionListValue();
            return switch (modeType) {
                case SINGLE -> Configs.Core.WORK_MODE_TYPE.getOptionListValue().equals(this.workSingleMode);
                case MULTI -> this.enable.getBooleanValue();
            };
        }
        // 仅绑定了启用配置，直接校验配置是否启用
        if (this.enable != null) {
            return this.enable.getBooleanValue();
        }
        // 无任何配置绑定，默认允许执行（由全局配置控制）
        return true;
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

    protected boolean canExecute() {
        return true;
    }

    protected boolean canIterate() {
        return true;
    }

    public boolean canIterationBlockPos(BlockPos pos) {
        return true;
    }

    protected void executeIterationBlockPos(BlockPos pos, AtomicReference<Boolean> skipIteration) {
    }

    public boolean isBlockPosOnCooldown(@Nullable BlockPos pos) {
        if (this.level == null || pos == null) return true;
        return BlockPosCooldownUtils.INSTANCE.isOnCooldown(this.level, this.getId(), pos);
    }

    public boolean isBlockPosOnCooldown(String name, @Nullable BlockPos pos) {
        if (this.level == null || pos == null) return true;
        return BlockPosCooldownUtils.INSTANCE.isOnCooldown(this.level, this.getId() + "_" + name, pos);
    }

    public void setBlockPosCooldown(@Nullable BlockPos pos, int cooldownTicks) {
        if (this.level == null || pos == null || cooldownTicks < 1) return;
        BlockPosCooldownUtils.INSTANCE.setCooldown(this.level, this.getId(), pos, cooldownTicks);
    }

    public void setBlockPosCooldown(String name, @Nullable BlockPos pos, int cooldownTicks) {
        if (this.level == null || pos == null || cooldownTicks < 1) return;
        BlockPosCooldownUtils.INSTANCE.setCooldown(this.level, this.getId() + "_" + name, pos, cooldownTicks);
    }

    protected Direction[] getPlayerOrderedByNearest() {
        return Direction.orderedByNearest(player);
    }

    protected Direction getPlayerPlacementDirection() {
        return getPlayerOrderedByNearest()[0].getOpposite();
    }
}