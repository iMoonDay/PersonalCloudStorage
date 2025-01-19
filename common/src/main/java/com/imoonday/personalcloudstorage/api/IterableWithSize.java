package com.imoonday.personalcloudstorage.api;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public interface IterableWithSize<T> {

    default void forEach(Consumer<@NotNull T> consumer, boolean createIfAbsent) {
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
    default <R> R findFirst(Function<@NotNull T, R> function) {
        return findFirstOrDefault(function, null);
    }

    default <R> R findFirstOrDefault(Function<@NotNull T, R> function, R defaultValue) {
        return findFirstOrElse(function, () -> defaultValue);
    }

    default <R> R findFirstOrElse(Function<@NotNull T, R> function, Supplier<R> defaultValue) {
        for (int i = 0; i < this.size(); i++) {
            R result = function.apply(this.get(i));
            if (result != null) {
                return result;
            }
        }
        return defaultValue.get();
    }
}
