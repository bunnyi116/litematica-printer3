package me.aleksilassila.litematica.printer.printer.guide;

import lombok.Getter;
import lombok.Setter;
import me.aleksilassila.litematica.printer.printer.action.Action;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Guide 动作执行结果。
 */
@Getter
@Setter
public final class Result {
    public static final Result PASS = new Result(null, true, false);
    public static final Result SKIP = new Result(null, false, true);

    private final @Nullable Action action;
    private final boolean passToNext;
    private final boolean skipOtherGuide;
    private @Nullable BlockPos iterationNextBlockPos;

    public Result(@Nullable Action action, boolean passToNext, boolean skipOtherGuide) {
        this.action = action;
        this.passToNext = passToNext;
        this.skipOtherGuide = skipOtherGuide;
    }

    public static Result success(Action action) {
        return new Result(action, false, false);
    }

    public static Result success() {
        return new Result(null, false, false);
    }

    /**
     * 条件成立时返回成功结果，否则交给下一个 Guide。
     *
     * @param condition 条件
     * @param action    要返回的动作
     * @return 条件成立则返回 {@link #success(Action)}，否则返回 {@link #PASS}
     */
    public static Result resultIf(boolean condition, Action action) {
        return condition ? success(action) : PASS;
    }

    /**
     * 条件成立时返回成功结果，否则交给下一个 Guide。
     *
     * @param condition 条件
     * @param supplier  动作供应者（延迟执行）
     * @return 条件成立则返回成功结果，否则返回 {@link #PASS}
     */
    public static Result resultIf(boolean condition, Supplier<Action> supplier) {
        return condition ? success(supplier.get()) : PASS;
    }

    /**
     * 将 Result 转换为 Optional。
     */
    public Optional<Action> toOptional() {
        return Optional.ofNullable(action);
    }

    /**
     * 检查是否有动作。
     */
    public boolean hasAction() {
        return action != null;
    }

    /**
     * 如果有动作则执行 consumer。
     */
    public void ifHasAction(Consumer<Action> consumer) {
        if (action != null) {
            consumer.accept(action);
        }
    }

    // ==================== 便捷的流式方法 ====================

    /**
     * 如果当前是 PASS，则返回 other。
     */
    public Result or(Result other) {
        return passToNext ? other : this;
    }

    /**
     * 如果当前是 PASS，则使用 supplier 生成结果。
     */
    public Result or(Supplier<Result> supplier) {
        return passToNext ? supplier.get() : this;
    }

    public Result setIterationNextBlockPos(@Nullable BlockPos iterationNextBlockPos) {
        this.iterationNextBlockPos = iterationNextBlockPos;
        return this;
    }
}
