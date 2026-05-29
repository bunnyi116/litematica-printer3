package me.aleksilassila.litematica.printer.module.modules;

import me.aleksilassila.litematica.printer.utils.mods.BedrockUtils;
import me.aleksilassila.litematica.printer.config.Configs;
import me.aleksilassila.litematica.printer.config.enums.PrintModeType;
import me.aleksilassila.litematica.printer.module.Module;
import me.aleksilassila.litematica.printer.I18n;
import me.aleksilassila.litematica.printer.utils.minecraft.MessageUtils;
import me.aleksilassila.litematica.printer.utils.mods.ModLoadUtils;
import net.minecraft.core.BlockPos;

import java.util.concurrent.atomic.AtomicReference;

public class BedrockModule extends Module {
    public BedrockModule() {
        super("bedrock", PrintModeType.BEDROCK, Configs.Hotkeys.BEDROCK, null, true);
    }

    @Override
    protected int getTickWorkInterval() {
        return Configs.Break.BREAK_INTERVAL.getIntegerValue();
    }

    @Override
    protected int getMaxEffectiveExecutionsPerTick() {
        return Configs.Break.BREAK_BLOCKS_PER_TICK.getIntegerValue();
    }

    @Override
    protected boolean canExecute() {
        if (player.isCreative()) {
            MessageUtils.setOverlayMessage(I18n.BEDROCK_CREATIVE_MODE.getName());
            return false;
        }
        if (!ModLoadUtils.isBedrockMinerLoaded() && !ModLoadUtils.isBlockMinerLoaded()) {
            MessageUtils.setOverlayMessage(I18n.BEDROCK_MOD_NOT_LOADED.getName());
            return false;
        }
        if (!BedrockUtils.isWorking()) {
            BedrockUtils.setWorking(true);
        }
        if (BedrockUtils.isBedrockMinerFeatureEnable()) {   // 限制原功能(手动点击或使用方块：添加、开关)
            BedrockUtils.setBedrockMinerFeatureEnable(false);
        }
        return true;
    }

    @Override
    protected void executeIteration(BlockPos blockPos, AtomicReference<Boolean> skipIteration) {
        BedrockUtils.addToBreakList(blockPos, client.level);
        setBlockPosCooldown(blockPos, 100);
    }
}
