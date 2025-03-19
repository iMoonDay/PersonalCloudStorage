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

public class PageContainer implements Container, IterableWithSize<PageSlot> {

    private final Map<Integer, PageSlot> slots;
    private final int page;
    private int size;
    private boolean removed;

    public PageContainer(int page, int size) {
        Validate.isTrue(size > 0, "Size must be greater than 0");
        this.page = page;
        this.size = size;
        this.slots = new HashMap<>(size);
    }

    public PageContainer(Map<Integer, PageSlot> slots, int page) {
        this(slots, page, slots.size());
    }

    public PageContainer(Map<Integer, PageSlot> slots, int page, int size) {
        this.slots = slots;
        this.page = page;
        this.size = size;
    }

    @Override
    public void clearContent() {
        forEach(PageSlot::takeItem);
    }

    public PageContainer copy() {
        Map<Integer, PageSlot> slots = new HashMap<>(this.slots.size());
        forEach(slot -> slots.put(slot.getPage(), slot.copy()));
        return new PageContainer(slots, page, size);
    }

    public boolean isRemoved() {
        return removed;
    }

    public void setRemoved(boolean removed) {
        this.removed = removed;
    }

    public ItemStack replaceItem(int slot, ItemStack stack) {
        return get(slot).replaceItem(stack);
    }

    @NotNull
    @Override
    public PageSlot get(int index) {
        if (size <= 0) {
            return PageSlot.empty();
        }
        index = Mth.clamp(index, 0, size - 1);
        return slots.computeIfAbsent(index, i -> new PageSlot(page, i));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PageContainer list)) return false;
        if (!super.equals(o)) return false;
        return page == list.page && size == list.size && removed == list.removed && Objects.equals(slots, list.slots);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), slots, page, size, removed);
    }

    @Override
    public @Nullable PageSlot getUnchecked(int index) {
        return slots.get(index);
    }

    public ItemStack insertItem(ItemStack item) {
        if (item.isEmpty()) {
            return ItemStack.EMPTY;
        }

        PageSlot[] emptySlot = {null};
        ItemStack result = findFirst(slot -> {
            if (slot.canMerge(item)) {
                if (slot.merge(item).isEmpty()) {
                    return ItemStack.EMPTY;
                }
            } else if (slot.isEmpty() && emptySlot[0] == null) {
                emptySlot[0] = slot;
            }
            return null;
        }, true);
        if (result != null) {
            return result;
        }

        if (!item.isEmpty()) {
            PageSlot slot = emptySlot[0];
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
        return findFirstOrDefault(slot -> !slot.isEmpty() ? false : null, true, false);
    }

    @Override
    public String toString() {
        return "PageContainer{" +
               "page=" + page +
               ", slots=" + slots +
               ", size=" + size +
               ", removed=" + removed +
               '}';
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
        }
    }

    public List<ItemStack> getItems() {
        List<ItemStack> list = new ArrayList<>(size);
        forEach(slot -> list.add(slot.getItem()));
        return list;
    }

    public List<PageSlot> getSlots() {
        List<PageSlot> list = new ArrayList<>(size);
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
        return this.size > 0 && !this.removed && !this.isEmptyList();
    }

    public ItemStack takeItem(int index) {
        return get(index).takeItem();
    }

    public boolean isValidIndex(int index) {
        return index >= 0 && index < size;
    }

    public CompoundTag save(CompoundTag tag) {
        tag.putInt("page", page);
        tag.putInt("size", size);
        tag.put("slots", toSlotsTag());
        return tag;
    }

    public CompoundTag toSlotsTag() {
        CompoundTag tag = new CompoundTag();
        forEach(slot -> {
            if (!slot.isEmpty()) {
                tag.put(String.valueOf(slot.getSlot()), slot.getItem().save(new CompoundTag()));
            }
        });
        return tag;
    }

    public static PageContainer fromTag(CompoundTag tag) {
        int page = tag.getInt("page");
        int size = tag.getInt("size");
        Map<Integer, PageSlot> slots;
        if (tag.contains("slots", Tag.TAG_LIST)) {
            slots = parseOldSlots(size, tag.getList("slots", Tag.TAG_COMPOUND));
        } else if (tag.contains("slots", Tag.TAG_COMPOUND)) {
            slots = parseSlotsFromTag(page, size, tag.getCompound("slots"));
        } else {
            slots = new HashMap<>();
        }
        return new PageContainer(slots, page, size);
    }

    public static Map<Integer, PageSlot> parseOldSlots(int size, ListTag tag) {
        Map<Integer, PageSlot> slots = new HashMap<>(size);
        for (int i = 0; i < size; i++) {
            slots.put(i, PageSlot.fromTag(tag.getCompound(i)));
        }
        return slots;
    }

    public static Map<Integer, PageSlot> parseSlotsFromTag(int page, int size, CompoundTag tag) {
        Map<Integer, PageSlot> slots = new HashMap<>(size);
        for (String key : tag.getAllKeys()) {
            try {
                int slot = Integer.parseInt(key);
                ItemStack stack = ItemStack.of(tag.getCompound(key));
                if (!stack.isEmpty()) {
                    slots.put(slot, new PageSlot(stack, page, slot));
                }
            } catch (NumberFormatException ignore) {

            }
        }
        return slots;
    }

    public static PageContainer empty() {
        return Empty.INSTANCE;
    }

    public static class Empty extends PageContainer {

        public static final Empty INSTANCE = new Empty();

        private Empty() {
            super(Map.of(), 0, 0);
        }

        @Override
        public void clearContent() {
        }

        @Override
        public PageContainer copy() {
            return new Empty();
        }

        @Override
        public ItemStack replaceItem(int slot, ItemStack stack) {
            return stack;
        }

        @Override
        public @NotNull PageSlot get(int index) {
            return PageSlot.empty();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            return o instanceof Empty;
        }

        @Override
        public int hashCode() {
            return 0;
        }

        @Override
        public @Nullable PageSlot getUnchecked(int index) {
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
            return "EmptyPageContainer";
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
        public List<PageSlot> getSlots() {
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
}
