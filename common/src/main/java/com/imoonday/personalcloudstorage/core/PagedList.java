package com.imoonday.personalcloudstorage.core;

import com.imoonday.personalcloudstorage.api.IterableWithSize;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class PagedList extends AbstractList<PagedSlot> implements Container, IterableWithSize<PagedSlot> {

    private final Map<Integer, PagedSlot> slots;
    private final int page;
    private int size;
    private boolean removed;

    public PagedList(int page, int size) {
        Validate.isTrue(size > 0, "Size must be greater than 0");
        this.page = page;
        this.size = size;
        this.slots = new HashMap<>(size);
    }

    public PagedList(Map<Integer, PagedSlot> slots, int page) {
        this(slots, page, slots.size());
    }

    public PagedList(Map<Integer, PagedSlot> slots, int page, int size) {
        this.slots = slots;
        this.page = page;
        this.size = size;
    }

    @Override
    public void clearContent() {
        clear();
    }

    public PagedList copy() {
        Map<Integer, PagedSlot> slots = new HashMap<>(this.slots.size());
        forEach(slot -> slots.put(slot.getPage(), slot.copy()), false);
        return new PagedList(slots, page, size);
    }

    public boolean isRemoved() {
        return removed;
    }

    public void setRemoved(boolean removed) {
        this.removed = removed;
    }

    public ItemStack replaceItem(int index, ItemStack item) {
        return get(index).replaceItem(item);
    }

    @Override
    public boolean add(@NotNull PagedSlot slot) {
        Validate.notNull(slot);
        return slots.put(slot.getSlot(), slot) != null;
    }

    @NotNull
    @Override
    public PagedSlot get(int index) {
        if (size <= 0) {
            return PagedSlot.empty();
        }
        index = Mth.clamp(index, 0, size - 1);
        return slots.computeIfAbsent(index, i -> new PagedSlot(page, i));
    }

    @Override
    public @Nullable PagedSlot getUnchecked(int index) {
        return slots.get(index);
    }

    @Override
    public PagedSlot set(int index, @NotNull PagedSlot slot) {
        Validate.notNull(slot);
        return slots.put(index, slot);
    }

    @Override
    public void add(int index, @NotNull PagedSlot slot) {
        Validate.notNull(slot);
        slots.put(index, slot);
    }

    @Override
    public PagedSlot remove(int index) {
        return slots.remove(index);
    }

    @Override
    public void clear() {
        forEach(PagedSlot::takeItem, false);
    }

    public ItemStack insertItem(ItemStack item) {
        if (item.isEmpty()) {
            return ItemStack.EMPTY;
        }

        PagedSlot[] emptySlot = {null};
        ItemStack result = findFirst(slot -> {
            if (slot.canMerge(item)) {
                slot.merge(item);
                if (item.isEmpty()) {
                    return ItemStack.EMPTY;
                }
            } else if (slot.isEmpty() && emptySlot[0] == null) {
                emptySlot[0] = slot;
            }
            return null;
        });
        if (result != null) {
            return result;
        }

        if (!item.isEmpty()) {
            PagedSlot slot = emptySlot[0];
            if (slot != null) {
                slot.replaceItem(item.copyAndClear());
                return ItemStack.EMPTY;
            }
        }
        return item;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return findFirstOrDefault(slot -> !slot.isEmpty() ? false : null, true);
    }

    public boolean isEmptyList() {
        return false;
    }

    public int getPage() {
        return page;
    }

    public void setSize(int size) {
        this.size = size;
        int slotSize = slots.size();
        if (size < slotSize) {
            for (int i = size; i < slotSize; i++) {
                slots.remove(i);
            }
        } else if (size > slotSize) {
            for (int i = slotSize; i < size; i++) {
                slots.put(i, new PagedSlot(page, i));
            }
        }
    }

    public List<ItemStack> getItems() {
        List<ItemStack> list = new ArrayList<>(slots.size());
        forEach(slot -> list.add(slot.getItem()), false);
        return list;
    }

    public List<PagedSlot> getSlots() {
        List<PagedSlot> list = new ArrayList<>(size);
        forEach(list::add, true);
        return list;
    }

    @Override
    public int getContainerSize() {
        return size;
    }

    public @NotNull ItemStack getItem(int index) {
        return get(index).getItem();
    }

    @Override
    public @NotNull ItemStack removeItem(int slot, int amount) {
        return get(slot).split(amount);
    }

    @Override
    public @NotNull ItemStack removeItemNoUpdate(int slot) {
        return takeItem(slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        get(slot).replaceItem(stack);
    }

    @Override
    public void setChanged() {

    }

    @Override
    public boolean stillValid(Player player) {
        return this.size > 0 && !this.removed;
    }

    public ItemStack takeItem(int index) {
        return get(index).takeItem();
    }

    public boolean isValidIndex(int index) {
        return index >= 0 && index < size;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PagedList list)) return false;
        if (!super.equals(o)) return false;
        return page == list.page && size == list.size && removed == list.removed && Objects.equals(slots, list.slots);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), slots, page, size, removed);
    }

    @Override
    public String toString() {
        return "PagedList{" +
               "page=" + page +
               ", slots=" + slots +
               ", size=" + size +
               ", removed=" + removed +
               '}';
    }

    public CompoundTag save(CompoundTag tag) {
        tag.putInt("page", page);
        tag.putInt("size", size);
        ListTag slotTags = new ListTag();
        forEach(slot -> slotTags.add(slot.save(new CompoundTag())), false);
        tag.put("slots", slotTags);
        return tag;
    }

    public static PagedList fromTag(CompoundTag tag) {
        int page = tag.getInt("page");
        int size = tag.getInt("size");
        ListTag slotTags = tag.getList("slots", Tag.TAG_COMPOUND);
        Map<Integer, PagedSlot> slots = new HashMap<>(size);
        for (int i = 0; i < size; i++) {
            slots.put(i, PagedSlot.fromTag(slotTags.getCompound(i)));
        }
        return new PagedList(slots, page, size);
    }

    public static PagedList empty() {
        return new EmptyPagedList();
    }
}
