package me.aleksilassila.litematica.printer;

import lombok.Getter;
import me.aleksilassila.litematica.printer.utils.minecraft.StringUtils;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.Nullable;

@Getter
public class I18n {
    public static final I18n MESSAGE_TOGGLED = of("message.toggled");
    public static final I18n MESSAGE_VALUE_OFF = of("message.value.off");
    public static final I18n MESSAGE_VALUE_ON = of("message.value.on");

    public static final I18n AUTO_DISABLE_NOTICE = of("auto_disable_notice");
    public static final I18n FREE_NOTICE = of("free_notice");

    public static final I18n UPDATE_AVAILABLE = of("update.available");
    public static final I18n UPDATE_DOWNLOAD = of("update.download");
    public static final I18n UPDATE_FAILED = of("update.failed");
    public static final I18n UPDATE_PASSWORD = of("update.password");
    public static final I18n UPDATE_RECOMMENDATION = of("update.recommendation");
    public static final I18n UPDATE_REPOSITORY = of("update.repository");

    // 下落方块检查提示
    public static final I18n FALLING_BLOCK_NO_SUPPORT = of("message.falling_block.no_support");
    public static final I18n FALLING_BLOCK_MISMATCH = of("message.falling_block.mismatch");

    // 破基岩模式提示
    public static final I18n BEDROCK_CREATIVE_MODE = of("message.bedrock.creative_mode");
    public static final I18n BEDROCK_MOD_NOT_LOADED = of("message.bedrock.mod_not_loaded");

    // 快捷潜影盒提示
    public static final I18n SHULKER_MOD_NOT_LOADED = of("message.shulker.mod_not_loaded");

    // 关闭全部模式提示
    public static final I18n CLOSE_ALL_MODE_NOTICE = of("message.close_all_mode");

    // 远程交互容器提示
    public static final I18n REMOTE_SERVER_ERROR = of("message.remote.server_error");
    public static final I18n REMOTE_AUTO_ENABLED = of("message.remote.auto_enabled");
    public static final I18n REMOTE_AUTO_DISABLED = of("message.remote.auto_disabled");

    // 库存/同步相关提示
    public static final I18n INVENTORY_ADD_COMPLETE = of("message.inventory.add_complete");
    public static final I18n INVENTORY_ADDING = of("message.inventory.adding");
    public static final I18n INVENTORY_CONTAINER_CANNOT_OPEN = of("message.inventory.container_cannot_open");
    public static final I18n INVENTORY_NOT_CONTAINER = of("message.inventory.not_container");
    public static final I18n INVENTORY_SYNC_CANCELLED = of("message.inventory.sync_cancelled");
    public static final I18n INVENTORY_TOO_FAR = of("message.inventory.too_far");
    public static final I18n INVENTORY_SYNC_PROGRESS = of("message.inventory.sync_progress");
    public static final I18n INVENTORY_SYNC_COMPLETE = of("message.inventory.sync_complete");
    public static final I18n INVENTORY_FULL = of("message.inventory.full");
    public static final I18n INVENTORY_RESTORE_FAILED = of("message.inventory.restore_failed");
    public static final I18n INVENTORY_SHULKER_OCCUPIED = of("message.inventory.shulker_occupied");
    public static final I18n INVENTORY_CLEARED = of("message.inventory.cleared");
    public static final I18n INVENTORY_OPEN_FAILED = of("message.inventory.open_failed");

    private static final String PREFIX_CONFIG = "config";
    private static final String PREFIX_COMMENT = "desc";

    private final @Nullable String prefix;
    private final String nameKey;
    private final String withPrefixNameKey;
    private final String descKey;
    private final String configNameKey;
    private final String configDescKey;

    private I18n(@Nullable String prefix, String nameKey) {
        this.prefix = prefix;
        this.nameKey = nameKey;
        this.withPrefixNameKey = prefix == null ? nameKey : prefix + "." + nameKey;
        this.descKey = withPrefixNameKey + "." + PREFIX_COMMENT;
        String configNameKey = prefix == null ? PREFIX_CONFIG : prefix + "." + PREFIX_CONFIG;
        this.configNameKey = configNameKey + "." + nameKey;
        this.configDescKey = configNameKey + "." + nameKey + "." + PREFIX_COMMENT;
    }

    public static I18n of(@Nullable String prefix, String key) {
        return new I18n(prefix, key);
    }

    public static I18n of(String key) {
        return new I18n(Reference.MOD_ID, key);
    }

    /*** 获取键名 ***/
    public MutableComponent getName() {
        return StringUtils.translatable(this.withPrefixNameKey);
    }

    /*** 获取键名(带参数) ***/
    public MutableComponent getName(Object... objects) {
        return StringUtils.translatable(this.withPrefixNameKey, objects);
    }

    /*** 获取描述 ***/
    public MutableComponent getDesc() {
        return StringUtils.translatable(this.descKey);
    }

    /*** 获取描述(带参数) ***/
    public MutableComponent getDesc(Object... objects) {
        return StringUtils.translatable(this.descKey, objects);
    }

    /*** 获取配置键名 ***/
    public MutableComponent getConfigName() {
        return StringUtils.translatable(this.configNameKey);
    }

    /*** 获取配置键名(带参数) ***/
    public MutableComponent getConfigName(Object... objects) {
        return StringUtils.translatable(this.configNameKey, objects);
    }

    /*** 获取配置描述 ***/
    public MutableComponent getConfigDesc() {
        return StringUtils.translatable(this.configDescKey);
    }

    /*** 获取配置描述(带参数) ***/
    public MutableComponent getConfigDesc(Object... objects) {
        return StringUtils.translatable(this.configDescKey, objects);
    }

    /*** 获取简易键名(一般用于枚举, 会取 "." 最后的文本) ***/
    public String getSimpleKey() {
        if (nameKey == null || nameKey.isEmpty()) {
            return nameKey == null ? "" : nameKey;
        }
        int lastDotIndex = nameKey.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return nameKey;
        }
        if (lastDotIndex == nameKey.length() - 1) {
            return "";
        }
        return nameKey.substring(lastDotIndex + 1);
    }
}