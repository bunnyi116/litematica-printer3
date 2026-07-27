package me.aleksilassila.litematica.printer;

import me.aleksilassila.litematica.printer.config.Configs;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * 调试日志输出类
 */
public class Debug {
    public static void alwaysWrite(String var1, Object... var2) {
        Reference.LOGGER.info(var1, var2);
    }

    public static void alwaysWrite(@Nullable Object obj) {
        alwaysWrite("{}", obj == null ? "NULL" : obj.toString());
    }

    public static void write(String var1, Object... var2) {
        if (Configs.Core.DEBUG_OUTPUT.getBooleanValue()) {
            Reference.LOGGER.info(var1, var2);
        }
    }

    public static void write(@Nullable Object obj) {
        write("{}", obj == null ? "NULL" : obj.toString());
    }

    public static void write() {
        write("");
    }

    public static String pos(BlockPos pos) {
        if (pos == null) {
            return "null";
        }
        return pos.getX() + "," + pos.getY() + "," + pos.getZ();
    }

    public static String describeState(BlockState state) {
        return state.getBlock() + " " + state;
    }
}
