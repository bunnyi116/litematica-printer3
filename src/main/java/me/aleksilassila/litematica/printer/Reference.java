package me.aleksilassila.litematica.printer;

import net.minecraft.client.Minecraft;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

/**
 * 模组核心常量引用类
 * 集中管理模组的全局固定值，避免硬编码和拼写错误
 */
public class Reference {
    public final static Minecraft MINECRAFT = Minecraft.getInstance();
    public final static String MOD_ID = "litematica-printer";
    public final static String MOD_NAME = "Litematica Printer";
    public final static Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public final static Item[] COMPOSTABLE_ITEMS = Arrays.stream(ComposterBlock.COMPOSTABLES.keySet().toArray(ItemLike[]::new)).map(ItemLike::asItem).toArray(Item[]::new);
    public final static Item[] HOE_ITEMS = {Items.DIAMOND_HOE, Items.IRON_HOE, Items.GOLDEN_HOE, Items.NETHERITE_HOE, Items.STONE_HOE, Items.WOODEN_HOE};
    public final static Item[] SHOVEL_ITEMS = {Items.DIAMOND_SHOVEL, Items.IRON_SHOVEL, Items.GOLDEN_SHOVEL, Items.NETHERITE_SHOVEL, Items.STONE_SHOVEL, Items.WOODEN_SHOVEL};
    public final static Item[] AXE_ITEMS = {Items.DIAMOND_AXE, Items.IRON_AXE, Items.GOLDEN_AXE, Items.NETHERITE_AXE, Items.STONE_AXE, Items.WOODEN_AXE};

    /**
     * 可以交互的方块类
     */
    public static Class<?>[] interactiveBlocks = {
            AbstractFurnaceBlock.class,     // 熔炉/烟熏炉/高炉
            CraftingTableBlock.class,       // 工作台
            ChestBlock.class,               // 箱子
            BarrelBlock.class,              // 木桶
            HopperBlock.class,              // 漏斗
            ShulkerBoxBlock.class,          // 潜影盒
            ComparatorBlock.class,          // 红石比较器
            RepeaterBlock.class,            // 红石中继器
            LeverBlock.class,               // 拉杆
            DoorBlock.class,                // 门
            TrapDoorBlock.class,            // 活板门
            BedBlock.class,                 // 床
            RedStoneWireBlock.class,        // 红石线
            ScaffoldingBlock.class,         // 脚手架
            EnchantingTableBlock.class,     // 附魔台
            NoteBlock.class,                // 音符盒
            JukeboxBlock.class,             // 唱片机
            CakeBlock.class,                // 蛋糕
            FenceGateBlock.class,           // 栅栏门
            BrewingStandBlock.class,        // 酿造台
            DragonEggBlock.class,           // 龙蛋
            CommandBlock.class,             // 命令方块
            BeaconBlock.class,              // 信标
            AnvilBlock.class,               // 铁砧
            DropperBlock.class,             // 投掷器
            DispenserBlock.class,           // 发射器
            LecternBlock.class,             // 讲台
            FlowerPotBlock.class,           // 花盆
            BellBlock.class,                // 钟
            SmithingTableBlock.class,       // 锻造台
            LoomBlock.class,                // 织布机
            CartographyTableBlock.class,    // 制图台
            GrindstoneBlock.class,          // 砂轮
            StonecutterBlock.class,         // 切石机
            SignBlock.class,                // 告示牌（右键打开编辑界面）
            //#if MC < 12109
            //$$ FletchingTableBlock.class, // 制箭台
            //#endif
            //#if MC >= 12003
            CrafterBlock.class,             // 合成器（自动合成台）
            //#endif
    };

    /**
     * 检查方块是否可以交互
     *
     * @param block 你传入的方块类
     * @return 是否可以交互
     */
    public static boolean isInteractive(Block block) {
        for (Class<?> clazz : interactiveBlocks) {
            if (clazz.isInstance(block)) {
                return true;
            }
        }
        return false;
    }
}
