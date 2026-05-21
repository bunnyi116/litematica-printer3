package me.aleksilassila.litematica.printer.utils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * 通用类型冷却工具
 * 仅根据 类型字符串 标记冷却，无位置/维度限制
 * 例：setCooldown("PRINTER_ACTION", 20) → 20tick内无法再次触发
 */
public class SimpleCooldownUtils {
    // 标准单例
    public static final SimpleCooldownUtils INSTANCE = new SimpleCooldownUtils();
    private SimpleCooldownUtils() {}

    // Key: 冷却类型 / Value: 剩余刻数
    private final Map<String, Integer> cooldownMap = new HashMap<>();

    /**
     * 冷却刻数递减（必须每tick调用）
     */
    public void tick() {
        if (!ConfigUtils.isEnable()) {
            cooldownMap.clear();
            return;
        }

        Iterator<Map.Entry<String, Integer>> iterator = cooldownMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Integer> entry = iterator.next();
            int remaining = entry.getValue() - 1;
            if (remaining <= 0) {
                iterator.remove();
            } else {
                entry.setValue(remaining);
            }
        }
    }

    /**
     * 设置类型冷却
     * @param type 冷却标识（如 "PRINTER", "RIGHT_CLICK"）
     * @param cooldownTicks 冷却刻数
     */
    public void setCooldown(String type, int cooldownTicks) {
        if (type == null || cooldownTicks <= 0) return;
        cooldownMap.put(type, cooldownTicks);
    }

    /**
     * 检查是否处于冷却中
     */
    public boolean isOnCooldown(String type) {
        return type != null && cooldownMap.containsKey(type);
    }

    /**
     * 获取剩余冷却刻数
     */
    public int getRemainingCooldown(String type) {
        return cooldownMap.getOrDefault(type, 0);
    }

    /**
     * 手动移除指定类型冷却
     */
    public void removeCooldown(String type) {
        if (type == null) return;
        cooldownMap.remove(type);
    }

    /**
     * 清空所有冷却
     */
    public void clearAll() {
        cooldownMap.clear();
    }
}