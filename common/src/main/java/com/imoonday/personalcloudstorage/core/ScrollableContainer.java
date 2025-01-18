package com.imoonday.personalcloudstorage.core;

import com.imoonday.personalcloudstorage.api.ItemStackData;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.ContainerListener;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.StackedContentsCompatible;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ScrollableContainer<T extends ItemStackData> implements Container, StackedContentsCompatible {

    private final List<T> data;
    private final int size;
    private int scrollIndex;
    @Nullable
    private List<ContainerListener> listeners;

    public ScrollableContainer(List<T> data, int size) {
        this.data = data;
        this.size = size;
    }

    public void addListener(ContainerListener listener) {
        if (this.listeners == null) {
            this.listeners = new ArrayList<>();
        }

        this.listeners.add(listener);
    }

    public void removeListener(ContainerListener listener) {
        if (this.listeners != null) {
            this.listeners.remove(listener);
        }
    }

    public List<ItemStack> getItems() {
        return data.stream().map(ItemStackData::get).collect(Collectors.toList());
    }

    public void scroll(int amount) {
        scrollIndex = Math.max(0, Math.min(scrollIndex + amount, data.size() - size));
    }

    @Override
    public void clearContent() {
        this.data.forEach(ItemStackData::clear);
        this.setChanged();
    }    public List<T> getDisplayedData() {
        return data.subList(scrollIndex, Math.min(scrollIndex + size, data.size()));
    }

    @Override
    public void fillStackedContents(StackedContents contents) {
        for (T t : this.data) {
            contents.accountStack(t.get());
        }
    }



    public List<ItemStack> getDisplayedItems() {
        return getDisplayedData().stream().map(ItemStackData::get).collect(Collectors.toList());
    }


    @Override
    public int getContainerSize() {
        return Math.min(size, data.size());
    }

    @Override
    public boolean isEmpty() {
        for (T t : data) {
            if (!t.get().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        return slot >= 0 && slot < getContainerSize() ? data.get(slot + scrollIndex).get() : ItemStack.EMPTY;
    }

    public Optional<T> getData(int slot) {
        return slot >= 0 && slot < getContainerSize() ? Optional.ofNullable(data.get(slot + scrollIndex)) : Optional.empty();
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack itemStack = ContainerHelper.removeItem(this.getDisplayedItems(), slot, amount);
        if (!itemStack.isEmpty()) {
            this.setChanged();
        }

        return itemStack;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        ItemStack itemStack = this.getItem(slot);
        if (itemStack.isEmpty()) {
            return ItemStack.EMPTY;
        } else {
            this.getData(slot).ifPresent(ItemStackData::clear);
            return itemStack;
        }
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        this.getData(slot).ifPresent(data -> data.set(stack));
        if (!stack.isEmpty() && stack.getCount() > this.getMaxStackSize()) {
            stack.setCount(this.getMaxStackSize());
        }

        this.setChanged();
    }

    @Override
    public void setChanged() {
        if (this.listeners != null) {
            for (ContainerListener containerListener : this.listeners) {
                containerListener.containerChanged(this);
            }
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }


}
