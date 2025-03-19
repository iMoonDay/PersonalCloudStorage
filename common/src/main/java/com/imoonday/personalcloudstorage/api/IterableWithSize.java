package com.imoonday.personalcloudstorage.api;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public interface IterableWithSize<T> {

    default void forEach(Consumer<@NotNull T> consumer) {
        forEach(consumer, false);
    }

    default void forEach(Consumer<@NotNull T> consumer, boolean createIfAbsent) {
        Objects.requireNonNull(consumer);
        for (int i = 0; i < this.size(); i++) {
            T page = createIfAbsent ? this.get(i) : this.getUnchecked(i);
            if (page != null) {
                consumer.accept(page);
            }
        }
    }

    int size();

    @NotNull
    T get(int index);

    @Nullable
    T getUnchecked(int index);

    @Nullable
    default <R> R findFirst(Function<@NotNull T, R> function, boolean createIfAbsent) {
        return findFirstOrDefault(function, null, createIfAbsent);
    }

    default <R> R findFirstOrDefault(Function<@NotNull T, R> function, R defaultValue, boolean createIfAbsent) {
        return findFirstOrElse(function, () -> defaultValue, createIfAbsent);
    }

    default <R> R findFirstOrElse(Function<@NotNull T, R> function, @NotNull Supplier<R> defaultValue, boolean createIfAbsent) {
        for (int i = 0; i < this.size(); i++) {
            T page = createIfAbsent ? this.get(i) : this.getUnchecked(i);
            if (page != null) {
                R result = function.apply(page);
                if (result != null) {
                    return result;
                }
            }
        }
        return defaultValue.get();
    }
}
