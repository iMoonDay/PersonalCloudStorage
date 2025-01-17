package com.imoonday.personalcloudstorage.component;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class EmptyPagedList extends PagedList {

    public EmptyPagedList() {
        super(List.of(), 0, 0);
    }

    @Override
    public void clearContent() {
    }

    @Override
    public PagedList copy() {
        return new EmptyPagedList();
    }

    @Override
    public ItemStack replaceItem(int index, ItemStack item) {
        return item;
    }

    @Override
    public boolean add(@NotNull PagedSlot slot) {
        return false;
    }

    @Override
    public @NotNull PagedSlot get(int index) {
        return PagedSlot.empty(0, index);
    }

    @Override
    public PagedSlot set(int index, @NotNull PagedSlot slot) {
        return PagedSlot.empty(0, index);
    }

    @Override
    public void add(int index, @NotNull PagedSlot slot) {
    }

    @Override
    public PagedSlot remove(int index) {
        return PagedSlot.empty(0, index);
    }

    @Override
    public void clear() {
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
    public int getPage() {
        return 0;
    }

    @Override
    public int getSize() {
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
    public void setChanged() {
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
