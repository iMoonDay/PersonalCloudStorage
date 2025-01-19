package com.imoonday.personalcloudstorage.core;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public class EmptyPagedSlot extends PagedSlot {

    public EmptyPagedSlot() {
        super(0, 0);
    }

    @Override
    public ItemStack getItem() {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public boolean isEmptySlot() {
        return true;
    }

    @Override
    public int getCount() {
        return 0;
    }

    @Override
    public ItemStack split(int amount) {
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack merge(ItemStack stack) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canMerge(ItemStack stack) {
        return false;
    }

    @Override
    public boolean isSameItemSameTags(ItemStack stack) {
        return false;
    }

    @Override
    public ItemStack takeItem() {
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack replaceItem(ItemStack stack) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean isSameItem(ItemStack stack) {
        return false;
    }

    @Override
    public PagedSlot copy() {
        return new EmptyPagedSlot();
    }

    @Override
    public ItemStack copyItem() {
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack copyWithCount(int count) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean isSamePosition(PagedSlot other) {
        return false;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        return tag;
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        return o instanceof EmptyPagedSlot;
    }

    @Override
    public String toString() {
        return "EmptyPagedSlot";
    }
}
