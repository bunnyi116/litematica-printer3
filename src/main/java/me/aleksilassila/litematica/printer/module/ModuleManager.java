package me.aleksilassila.litematica.printer.module;

import lombok.Getter;
import lombok.Setter;
import me.aleksilassila.litematica.printer.config.Configs;
import me.aleksilassila.litematica.printer.event.EventCallbacks;
import me.aleksilassila.litematica.printer.mixin.extension.MultiPlayerGameModeExtension;
import me.aleksilassila.litematica.printer.module.modules.*;
import me.aleksilassila.litematica.printer.action.ActionManager;
import me.aleksilassila.litematica.printer.printer.zxy.inventory.InventoryUtils;
import me.aleksilassila.litematica.printer.printer.zxy.utils.ZxyUtils;
import me.aleksilassila.litematica.printer.utils.BlockPosCooldownUtils;
import me.aleksilassila.litematica.printer.utils.InteractionUtils;
import net.minecraft.client.Minecraft;

import static me.aleksilassila.litematica.printer.printer.zxy.inventory.InventoryUtils.isOpenHandler;
import static me.aleksilassila.litematica.printer.printer.zxy.inventory.InventoryUtils.switchItem;


public class ModuleManager {
    public final static ModuleManager INSTANCE = new ModuleManager();

    @Getter
    private long clientTickCount;

    @Getter
    @Setter
    private int receivePacketCount;

    public void registerEvents() {
        EventCallbacks.CLIENT_START_TICK.register(ModuleManager.INSTANCE::onStartTick);
        EventCallbacks.CLIENT_END_TICK.register(ModuleManager.INSTANCE::onEndTick);
    }

    @SuppressWarnings("unused")
    public void onStartTick(Minecraft minecraft) {
        this.clientTickCount++;
    }

    public void onEndTick(Minecraft minecraft) {
        InventoryUtils.tick();
        ZxyUtils.tick();
        if (minecraft.gameMode instanceof MultiPlayerGameModeExtension extension) {
            extension.litematica_printer$handleDelayedDestroy();
        }
        InteractionUtils.INSTANCE.preprocess();
        InteractionUtils.INSTANCE.onTick();

        // 本次TICK共享部分预先检查
        if (isOpenHandler || switchItem() || InteractionUtils.INSTANCE.isNeedHandle()) {
            return;
        }
        if (ActionManager.INSTANCE.needWaitModifyLook) {
            ActionManager.INSTANCE.sendQueue(minecraft.player);
            return;
        }
        if (Configs.Core.LAG_CHECK.getBooleanValue()) {
            if (receivePacketCount > Configs.Core.LAG_CHECK_MAX.getIntegerValue()) {
                return;
            }
            receivePacketCount++;
        }
        for (Module module : Modules.VALUES) {
            boolean isGui = module instanceof GuiModule;
            // Gui无需等待处理
            if (!isGui) {
                // 同TICK不同处理程序进行二次迭代检查, 避免独立的处理程序修改了内容没有及时跳出导致出现资源抢占问题
                if (isOpenHandler || switchItem() || InteractionUtils.INSTANCE.isNeedHandle()) {
                    return;
                }
                // 有任务需要修改视角强制退出
                if (ActionManager.INSTANCE.needWaitModifyLook) {
                    return;
                }
            }
            module.tick();
        }
        BlockPosCooldownUtils.INSTANCE.tick();
    }
}
