package com.xh.easy.easycache.base;

import java.util.function.*;

public abstract class LambdaConverter {

    public static <T, R> R supplierToFunction(T t, Supplier<Function<T, R>> supplier) {
        return supplier.get().apply(t);
    }

    public static <T, R> Supplier<R> functionToSupplier(Function<? super T, ? extends R> function, T t) {
        return () -> function.apply(t);
    }

    public static <T, U, R> Supplier<R> functionToSupplier(BiFunction<? super T, ? super U, ? extends R> function, T t, U u) {
        return () -> function.apply(t, u);
    }

    public static <T> Runnable consumerToRunnable(Consumer<? super T> consumer, T t) {
        return () -> consumer.accept(t);
    }

    public static <T, U> Runnable consumerToRunnable(BiConsumer<? super T, ? super U> consumer, T t, U u) {
        return () -> consumer.accept(t, u);
    }
}
