package me.aleksilassila.litematica.printer.module;

import com.google.common.collect.ImmutableList;
import lombok.Getter;
import lombok.Setter;
import me.aleksilassila.litematica.printer.TickContext;
import me.aleksilassila.litematica.printer.config.Configs;
import me.aleksilassila.litematica.printer.module.modules.*;
import me.aleksilassila.litematica.printer.printer.ActionManager;
import me.aleksilassila.litematica.printer.utils.BlockPosCooldownUtils;
import me.aleksilassila.litematica.printer.utils.InteractionUtils;
import net.minecraft.client.Minecraft;

import static me.aleksilassila.litematica.printer.printer.zxy.inventory.InventoryUtils.isOpenHandler;
import static me.aleksilassila.litematica.printer.printer.zxy.inventory.InventoryUtils.switchItem;

public class ModuleManager {
    public static final Minecraft mc = Minecraft.getInstance();

    public static final GuiModule GUI = new GuiModule();
    public static final PrintModule PRINT = new PrintModule();
    public static final FillModule FILL = new FillModule();
    public static final MineModule MINE = new MineModule();
    public static final FluidModule FLUID = new FluidModule();
    public static final BedrockModule BEDROCK = new BedrockModule();
    public static final ImmutableList<Module> VALUES = ImmutableList.of(GUI, MINE, FLUID, PRINT, FILL, BEDROCK);

    @Getter
    @Setter
    private static int packetTick;

    public static void tick() {
        // 本次TICK共享部分预先检查
        if (isOpenHandler || switchItem() || InteractionUtils.INSTANCE.isNeedHandle()) {
            return;
        }
        if (ActionManager.INSTANCE.needWaitModifyLook) {
            ActionManager.INSTANCE.sendQueue(mc.player);
            return;
        }
        if (Configs.Core.LAG_CHECK.getBooleanValue()) {
            if (packetTick > Configs.Core.LAG_CHECK_MAX.getIntegerValue()) {
                return;
            }
            packetTick++;
        }
        for (Module handler : VALUES) {
            boolean isGui = handler instanceof GuiModule;
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
            handler.tick();
        }
        BlockPosCooldownUtils.INSTANCE.tick();
    }

    public static long getCurrentHandlerTime() {
        return TickContext.INSTANCE.getClientTickCount();
    }
}
