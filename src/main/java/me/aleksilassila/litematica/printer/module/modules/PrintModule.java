package me.aleksilassila.litematica.printer.module.modules;

import fi.dy.masa.litematica.world.SchematicWorldHandler;
import fi.dy.masa.litematica.world.WorldSchematic;
import lombok.Getter;
import lombok.Setter;
import me.aleksilassila.litematica.printer.Reference;
import me.aleksilassila.litematica.printer.config.Configs;
import me.aleksilassila.litematica.printer.printer.guide.BlockMatchResult;
import me.aleksilassila.litematica.printer.config.enums.WorkSingleMode;
import me.aleksilassila.litematica.printer.printer.guide.Guide;
import me.aleksilassila.litematica.printer.printer.guide.Guides;
import me.aleksilassila.litematica.printer.module.Module;
import me.aleksilassila.litematica.printer.I18n;
import me.aleksilassila.litematica.printer.printer.SchematicBlockContext;
import me.aleksilassila.litematica.printer.printer.action.Action;
import me.aleksilassila.litematica.printer.printer.ActionManager;
import me.aleksilassila.litematica.printer.printer.action.ClickAction;
import me.aleksilassila.litematica.printer.printer.guide.Result;
import me.aleksilassila.litematica.printer.utils.*;
import me.aleksilassila.litematica.printer.utils.minecraft.MessageUtils;
import me.aleksilassila.litematica.printer.utils.mods.LitematicaUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.*;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

public class PrintModule extends Module {
    public final static String NAME = "print";

    @Getter
    @Setter
    private boolean pistonNeedFix;

    @Getter
    @Setter
    private boolean printerMemorySync;

    private SchematicBlockContext ctx;
    private Action action;

    public PrintModule() {
        super(NAME, WorkSingleMode.PRINT, Configs.Core.PRINT, Configs.Print.PRINT_SELECTION_TYPE, true);
    }

    public SchematicBlockContext getContext() {
        return ctx;
    }

    @Override
    protected int getTickWorkInterval() {
        return Configs.Placement.PLACE_INTERVAL.getIntegerValue();
    }

    @Override
    protected int getMaxEffectiveExecutionsPerTick() {
        return Configs.Placement.PLACE_BLOCKS_PER_TICK.getIntegerValue();
    }

    @Override
    protected boolean isSchematicBlockHandler() {
        return true;
    }

    @Override
    public boolean canIterationBlockPos(BlockPos blockPos) {
        WorldSchematic schematic = SchematicWorldHandler.getSchematicWorld();
        if (schematic == null) {
            return false;
        }
        SchematicBlockContext context = new SchematicBlockContext(client, level, schematic, blockPos);
        if (Configs.Print.PRINT_SKIP.getBooleanValue()) {
            Set<String> skipSet = new HashSet<>(Configs.Print.PRINT_SKIP_LIST.getStrings()); // 转换为 HashSet
            if (skipSet.stream().anyMatch(s -> FilterUtils.matchName(s, context.requiredState))) {
                return false;
            }
        }
        Optional<Action> action = Optional.empty();
        BlockMatchResult blockMatchResult = BlockMatchResult.compare(context);
        List<Guide> guides = Guides.INSTANCE.getGuides(context);
        for (Guide guide : guides) {
            Result result = guide.buildAction(blockMatchResult);
            if (result.getIterationNextBlockPos() != null) {
                this.iterationNextBlockPos = result.getIterationNextBlockPos();
            }
            if (result.hasAction()) {
                action = result.toOptional();
                break;
            }
            if (result.isSkipOtherGuide()) {
                break;
            }
        }

        if (action.isEmpty()) {
            this.ctx = null;
            this.action = null;
            return false;
        }
        this.action = action.get();
        this.ctx = context;
        return true;
    }

    @Override
    protected void executeIterationBlockPos(BlockPos blockPos, AtomicReference<Boolean> skipIteration) {
        if (Configs.Placement.FALLING_CHECK.getBooleanValue() && ctx.requiredState.getBlock() instanceof FallingBlock) {
            BlockPos downPos = blockPos.below();
            if (FallingBlock.isFree(level.getBlockState(downPos))) {
                MessageUtils.setOverlayMessage(I18n.FALLING_BLOCK_NO_SUPPORT.getName(ctx.requiredBlockName().getString()));
                return;
            } else if (level.getBlockState(downPos) != ctx.schematic.getBlockState(downPos)) {
                MessageUtils.setOverlayMessage(I18n.FALLING_BLOCK_MISMATCH.getName(ctx.requiredBlockName().getString()));
                return;
            }
        }
        Direction side = action.getValidSide(level, blockPos);
        if (side == null) return;

        @Nullable Item[] reqItems = action.getRequiredItems(ctx.requiredState.getBlock());
        if (reqItems == null || !InventoryUtils.switchToItems(player, reqItems)) return;

        boolean useShift;
        if (action.getShift() == null) {
            useShift = (Reference.isInteractive(level.getBlockState(blockPos.relative(side)).getBlock()) && !(action instanceof ClickAction))
                    || Configs.Print.PRINT_FORCED_SNEAK.getBooleanValue();
        } else {
            useShift = action.getShift();
        }
        action.queueAction(blockPos, side, useShift, player);
        Vec3 hitModifier = LitematicaUtils.usePrecisionPlacement(blockPos, ctx.requiredState);
        if (hitModifier != null) {
            ActionManager.INSTANCE.hitModifier = hitModifier;
            ActionManager.INSTANCE.useProtocol = true;
        }
        ActionManager.INSTANCE.setLook(action.getPlayerLook());
        if (ActionManager.INSTANCE.sendQueue(player).needWaitModifyLook) {
            skipIteration.set(true);
        }
        setBlockPosCooldown(blockPos, ConfigUtils.getPlaceCooldown());
    }
}

