package com.imoonday.personalcloudstorage.api;

import net.minecraft.world.item.ItemStack;

import java.util.function.Supplier;

public interface ItemStackData extends Supplier<ItemStack> {

    default void clear() {
        set(ItemStack.EMPTY);
    }

    void set(ItemStack stack);
}
