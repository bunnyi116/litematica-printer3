package me.aleksilassila.litematica.printer.printer.guide;

import fi.dy.masa.litematica.world.WorldSchematic;
import me.aleksilassila.litematica.printer.printer.SchematicBlockContext;
import me.aleksilassila.litematica.printer.utils.BlockPosCooldownUtils;
import me.aleksilassila.litematica.printer.utils.minecraft.BlockStateUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public abstract class Guide extends BlockStateUtils {
    protected final SchematicBlockContext context;
    public final Minecraft client;
    public final ClientLevel level;
    public final WorldSchematic schematic;
    public final BlockPos blockPos;
    public final BlockState currentState;
    public final BlockState requiredState;
    protected final Block currentBlock;
    protected final Block requiredBlock;

    public Guide(SchematicBlockContext context) {
        this.context = context;
        this.client = context.client;
        this.level = context.level;
        this.schematic = context.schematic;
        this.blockPos = context.blockPos;
        this.currentBlock = context.currentState.getBlock();
        this.requiredBlock = context.requiredState.getBlock();
        this.currentState = context.currentState;
        this.requiredState = context.requiredState;
    }

    public final Result buildAction(BlockMatchResult state) {
        if (state == BlockMatchResult.CORRECT) {
            return this.onBuildActionCorrect(state);
        }

        // 方块在此位置无法自然存活（火把无附着面、植物在石头上等），跳过放置
        if (!requiredState.canSurvive(level, blockPos)) {
            return Result.passToNext();
        }

        Result result = this.onBuildAction(state);
        if (!result.isPassToNext() || result.isSkipOtherGuide()) {
            return result;
        }

        return switch (state) {
            case MISSING -> this.onBuildActionMissingBlock(state);
            case WRONG_BLOCK -> this.onBuildActionWrongBlock(state);
            case WRONG_STATE -> this.onBuildActionWrongState(state);
            default -> Result.passToNext();
        };
    }

    protected boolean canExecute() {
        return true;
    }

    // ==================== 子类可覆盖的钩子 ====================

    protected Result onBuildAction(BlockMatchResult state) {
        return Result.passToNext();
    }

    protected Result onBuildActionCorrect(BlockMatchResult state) {
        return Result.passToNext();
    }

    protected Result onBuildActionMissingBlock(BlockMatchResult state) {
        return Result.passToNext();
    }

    protected Result onBuildActionWrongBlock(BlockMatchResult state) {
        return Result.passToNext();
    }

    protected Result onBuildActionWrongState(BlockMatchResult state) {
        return Result.passToNext();
    }

    // ==================== 冷却 ====================

    protected boolean isOnCooldown() {
        return BlockPosCooldownUtils.INSTANCE.isOnCooldown(context.level, getClass().getSimpleName(), context.blockPos);
    }

    protected Guide setCooldown(int cooldown) {
        BlockPosCooldownUtils.INSTANCE.setCooldown(context.level, getClass().getSimpleName(), context.blockPos, cooldown);
        return this;
    }
}