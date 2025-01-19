package com.imoonday.personalcloudstorage.core;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public class EmptyPagedList extends PagedList {

    public EmptyPagedList() {
        super(Map.of(), 0, 0);
    }

    @Override
    public void clearContent() {
    }

    @Override
    public PagedList copy() {
        return new EmptyPagedList();
    }

    @Override
    public ItemStack replaceItem(int slot, ItemStack stack) {
        return stack;
    }

    @Override
    public boolean add(@NotNull PagedSlot slot) {
        return false;
    }

    @Override
    public @NotNull PagedSlot get(int index) {
        return PagedSlot.empty();
    }

    @Override
    public PagedSlot set(int index, @NotNull PagedSlot slot) {
        return PagedSlot.empty();
    }

    @Override
    public void add(int index, @NotNull PagedSlot slot) {
    }

    @Override
    public PagedSlot remove(int index) {
        return PagedSlot.empty();
    }

    @Override
    public void clear() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        return o instanceof EmptyPagedList;
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public @Nullable PagedSlot getUnchecked(int index) {
        return null;
    }

    @Override
    public ItemStack insertItem(ItemStack item) {
        return item;
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public String toString() {
        return "EmptyPagedList";
    }

    @Override
    public boolean isEmptyList() {
        return true;
    }

    @Override
    public int getPage() {
        return 0;
    }

    @Override
    public void setSize(int size) {
    }

    @Override
    public List<ItemStack> getItems() {
        return List.of();
    }

    @Override
    public List<PagedSlot> getSlots() {
        return List.of();
    }

    @Override
    public int getContainerSize() {
        return 0;
    }

    @Override
    public @NotNull ItemStack getItem(int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public @NotNull ItemStack removeItem(int slot, int amount) {
        return ItemStack.EMPTY;
    }

    @Override
    public @NotNull ItemStack removeItemNoUpdate(int slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
    }

    @Override
    public boolean stillValid(Player player) {
        return false;
    }

    @Override
    public ItemStack takeItem(int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean isValidIndex(int index) {
        return false;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        return tag;
    }
}
