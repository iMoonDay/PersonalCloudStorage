package com.imoonday.personalcloudstorage.api;

import net.minecraft.world.item.ItemStack;

import java.util.function.Supplier;

public interface ItemStackData extends Supplier<ItemStack> {

    void set(ItemStack stack);

    default void clear() {
        set(ItemStack.EMPTY);
    }
}
