package me.aleksilassila.litematica.printer.guide;

import me.aleksilassila.litematica.printer.enums.BlockMatchResult;
import me.aleksilassila.litematica.printer.guide.blocks.*;
import me.aleksilassila.litematica.printer.printer.SchematicBlockContext;
import me.aleksilassila.litematica.printer.printer.action.Action;
import net.minecraft.world.level.block.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class Guides {
    public static final Guides INSTANCE = new Guides();
    private final List<GuideRegistration> registrations = new ArrayList<>();
    private final AtomicReference<Boolean> skipOtherGuide = new AtomicReference<>();

    private Guides() {
        // ============================================================
        // 1. 跳过指南（最高优先级）
        //    当没有需要跳过的方块时，请注释掉此行，否则会拦截所有方块
        // ============================================================
        register(SkipGuide.class,
                LiquidBlock.class,
                BubbleColumnBlock.class,
                LilyPadBlock.class
        );

        // ============================================================
        // 2. 放置指南（按特殊度排序，特殊方块优先）
        // ============================================================

        // 火把（附着面决定放置方向，需要支撑）
        register(TorchGuide.class,
                //#if MC > 12002
                BaseTorchBlock.class
                //#else
                //$$ TorchBlock.class
                //#endif
        );

        // 紫水晶芽（附着面 + 支撑）
        register(AmethystGuide.class, AmethystClusterBlock.class);

        // 台阶（上/下半面精确点击 + DOUBLE 合并）
        register(SlabGuide.class, SlabBlock.class);

        // 楼梯（Half + Facing）
        register(StairGuide.class, StairBlock.class);

        // 活板门（Half）
        register(TrapDoorGuide.class, TrapDoorBlock.class);

        // 门（双格方块 + 铰链偏移 + 开关状态修正）
        register(DoorGuide.class, DoorBlock.class);

        // 栅栏门（反向朝向 + 开关状态修正）
        register(FenceGateGuide.class, FenceGateBlock.class);

        // 床（只放床尾）
        register(BedGuide.class, BedBlock.class);

        // 铃铛（附着类型）
        register(BellGuide.class, BellBlock.class);

        // 侦测器（安全放置模式）
        register(ObserverGuide.class, ObserverBlock.class);

        // 活塞（安全放置检查侦测器链）
        register(PistonGuide.class, net.minecraft.world.level.block.piston.PistonBaseBlock.class);

        // 箱子（单/双，潜行控制）
        register(ChestGuide.class, ChestBlock.class, TrappedChestBlock.class);

        // 告示牌（四类子型）
        register(SignGuide.class,
                StandingSignBlock.class,
                WallSignBlock.class
                //#if MC >= 12002
                , WallHangingSignBlock.class
                , CeilingHangingSignBlock.class
                //#endif
        );

        // 旗帜（站立/墙壁）
        register(BannerGuide.class, AbstractBannerBlock.class);

        // 头颅（站立/墙壁）
        register(SkullGuide.class, SkullBlock.class, WallSkullBlock.class);

        // 下界传送门
        register(NetherPortalGuide.class, NetherPortalBlock.class);

        // 梯子
        register(LadderGuide.class, LadderBlock.class);

        // 灯笼
        register(LanternGuide.class, LanternBlock.class);

        // 末地烛/避雷针（反向检测）
        register(RodGuide.class, RodBlock.class);

        // 漏斗
        register(HopperGuide.class, HopperBlock.class);

        // 铁砧
        register(AnvilGuide.class, AnvilBlock.class);

        // 去皮原木（轴方向 + 斧头去皮交互）
        register(StripLogGuide.class, RotatedPillarBlock.class);

        // 可可豆
        register(CocoaGuide.class, CocoaBlock.class);

        // 绊线钩
        register(TripWireHookGuide.class, TripWireHookBlock.class);

        // 铁轨
        register(RailGuide.class, BaseRailBlock.class);

        // 合成器（MC 1.21+）
        //#if MC >= 12003
        register(CrafterGuide.class, CrafterBlock.class);
        //#endif

        // ============================================================
        // 3. 交互指南（WRONG_STATE 处理为主）
        // ============================================================

        // 蜡烛（添加/点燃/熄灭）
        register(CandleGuide.class, CandleBlock.class);

        // 海泡菜
        register(SeaPickleGuide.class, SeaPickleBlock.class);

        // 海龟蛋（叠加放置）
        register(TurtleEggGuide.class, TurtleEggBlock.class);

        // 红石中继器（延迟调整）
        register(RepeaterGuide.class, RepeaterBlock.class);

        // 红石比较器（模式切换）
        register(ComparatorGuide.class, net.minecraft.world.level.block.ComparatorBlock.class);

        // 红石线（点状/十字形）
        register(RedstoneWireGuide.class, RedStoneWireBlock.class);

        // 拉杆
        register(LeverGuide.class, LeverBlock.class);

        // 营火（点燃/熄灭）
        register(CampfireGuide.class, CampfireBlock.class);

        // 农作物（骨粉催熟）
        register(CropsGuide.class,
                AttachedStemBlock.class, StemBlock.class, CropBlock.class, BeetrootBlock.class);

        // 音符盒（调音）
        register(NoteBlockGuide.class, NoteBlock.class);

        // 雪层（叠加）
        register(SnowGuide.class, SnowLayerBlock.class);

        // 末地传送门框架（嵌入末影之眼）
        register(EndPortalFrameGuide.class, EndPortalFrameBlock.class);

        // 阳光探测器（反转切换）
        register(DaylightDetectorGuide.class, DaylightDetectorBlock.class);

        // 花簇（MC 1.19.4+）
        //#if MC >= 11904
        register(FlowerBedGuide.class,
                //#if MC >= 12105
                FlowerBedBlock.class
                //#else
                //$$ PinkPetalsBlock.class
                //#endif
        );
        //#endif

        // 藤蔓/发光地衣
        register(VineGuide.class, VineBlock.class, GlowLichenBlock.class);

        // 火/灵魂火
        register(FireGuide.class, FireBlock.class, SoulFireBlock.class);

        // 炼药锅
        register(CauldronGuide.class,
                CauldronBlock.class, LavaCauldronBlock.class, LayeredCauldronBlock.class);

        // 堆肥桶
        register(ComposterGuide.class, ComposterBlock.class);

        // ============================================================
        // 4. 混合指南（放置 + 交互 + 破坏）
        // ============================================================

        // 耕地/土径
        register(SoilGuide.class, FarmlandBlock.class, DirtPathBlock.class);

        // 花盆
        register(FlowerPotGuide.class, FlowerPotBlock.class);

        // 攀爬植物（洞穴藤蔓/垂泪藤/缠怨藤/大垂叶茎）
        register(ClimbingPlantGuide.class,
                BigDripleafStemBlock.class,
                CaveVinesBlock.class, CaveVinesPlantBlock.class,
                WeepingVinesBlock.class, WeepingVinesPlantBlock.class,
                TwistingVinesBlock.class, TwistingVinesPlantBlock.class);

        // 死珊瑚（需过滤非珊瑚方块）
        register(CoralGuide.class, Block.class);

        // ============================================================
        // 5. 默认指南（最低优先级，兜底所有未被上面接管的方块）
        // ============================================================
        register(DefaultGuide.class);
    }

    @SafeVarargs
    public final void register(Class<? extends Guide> guideClass, Class<? extends Block>... supportedBlocks) {
        registrations.add(new GuideRegistration(guideClass, supportedBlocks));
    }

    @SuppressWarnings("CallToPrintStackTrace")
    private List<Guide> getGuides(SchematicBlockContext context) {
        List<Guide> guides = new ArrayList<>();
        for (GuideRegistration reg : registrations) {
            boolean matches = reg.blockClass.length == 0;
            if (!matches) {
                for (Class<? extends Block> clazz : reg.blockClass) {
                    if (clazz.isInstance(context.requiredState.getBlock())) {
                        matches = true;
                        break;
                    }
                }
            }
            if (matches) {
                try {
                    Guide guide = reg.guideClass
                            .getConstructor(SchematicBlockContext.class)
                            .newInstance(context);
                    if (guide.canExecute(this.skipOtherGuide)) {
                        guides.add(guide);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return guides;
    }

    public final Optional<Action> buildAction(SchematicBlockContext context) {
        this.skipOtherGuide.set(false);
        BlockMatchResult blockMatchResult = BlockMatchResult.compare(context);
        List<Guide> guides = this.getGuides(context);
        for (Guide guide : guides) {
            Optional<Action> action = guide.buildAction(blockMatchResult, this.skipOtherGuide);
            if (action.isPresent()) {
                return action;
            }
            if (this.skipOtherGuide.get()) {    // 跳过其他指南
                break;
            }
        }
        return Optional.empty();
    }

    @SuppressWarnings("ClassCanBeRecord")
    private static class GuideRegistration {
        public final Class<? extends Guide> guideClass;
        public final Class<? extends Block>[] blockClass;

        public GuideRegistration(Class<? extends Guide> guideClass, Class<? extends Block>[] blockClass) {
            this.guideClass = guideClass;
            this.blockClass = blockClass;
        }
    }
}
