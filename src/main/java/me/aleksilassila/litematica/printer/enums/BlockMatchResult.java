package me.aleksilassila.litematica.printer.enums;

import me.aleksilassila.litematica.printer.printer.SchematicBlockContext;
import me.aleksilassila.litematica.printer.utils.minecraft.BlockStateUtils;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.properties.Property;

public enum BlockMatchResult {
    /**
     * 缺失方块：实际位置为空，或当前方块在可替换列表中且启用了替换功能
     */
    MISSING,

    /**
     * 方块错误：方块类型完全不同，且不满足缺失/状态错误的条件
     */
    WRONG_BLOCK,

    /**
     * 状态错误：方块类型相同，但方块状态（如朝向、亮度等）不一致
     */
    WRONG_STATE,

    /**
     * 正确匹配：原理图方块与实际方块的类型和状态完全一致
     */
    CORRECT;


    public static BlockMatchResult compare(SchematicBlockContext context, Property<?>... propertiesToIgnore) {
        if (context.requiredState == context.currentState) {
            return CORRECT;
        }
        if (context.requiredState.getBlock().equals(context.currentState.getBlock())) {
            if (BlockStateUtils.statesEqualIgnoreProperties(context.requiredState, context.currentState, propertiesToIgnore)) {
                return CORRECT;
            }
            return WRONG_STATE;
        }
        // 液体（LiquidBlock）视为可替换，方块可直接放置其中
        if (!context.requiredState.isAir()
                && (context.currentState.isAir() || context.currentState.getBlock() instanceof LiquidBlock)) {
            return MISSING;
        }
        return WRONG_BLOCK;
    }
}

