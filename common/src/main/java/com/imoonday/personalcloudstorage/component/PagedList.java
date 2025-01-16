package com.imoonday.personalcloudstorage.component;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.NotNull;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class PagedList extends AbstractList<PagedSlot> implements Container {

    public static final PagedList EMPTY = new PagedList(List.of(), 0, 0);
    private final List<PagedSlot> slots;
    private final int page;
    private int size;
    private boolean removed;

    @Override
    public void clearContent() {
        clear();
    }

    public PagedList copy() {
        ArrayList<PagedSlot> slots = new ArrayList<>(this.slots.size());
        for (PagedSlot slot : this.slots) {
            slots.add(slot.copyWithItem());
        }
        return new PagedList(slots, page, size);
    }

    public static PagedList create(int page, int size) {
        Validate.isTrue(size > 0, "Size must be greater than 0");
        ArrayList<PagedSlot> items = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            items.add(PagedSlot.empty(page, i));
        }
        return new PagedList(items, page);
    }

    public static PagedList fromList(List<ItemStack> items, int page, int size) {
        Validate.notEmpty(items, "Items list cannot be empty");
        Validate.isTrue(size > 0, "Size must be greater than 0");
        int itemSize = items.size();
        ArrayList<PagedSlot> pagedSlots = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            if (i < itemSize) {
                pagedSlots.add(new PagedSlot(items.get(i), page, i));
            } else {
                pagedSlots.add(PagedSlot.empty(page, i));
            }
        }
        return new PagedList(pagedSlots, page);
    }

    public static PagedList fromList(List<ItemStack> items, int page) {
        Validate.notEmpty(items, "Items list cannot be empty");
        int size = items.size();
        ArrayList<PagedSlot> pagedSlots = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            pagedSlots.add(new PagedSlot(items.get(i), page, i));
        }
        return new PagedList(pagedSlots, page);
    }

    protected PagedList(List<PagedSlot> slots, int page, int size) {
        this.slots = slots;
        this.page = page;
        this.size = size;
    }

    protected PagedList(List<PagedSlot> slots, int page) {
        this(slots, page, slots.size());
    }

    @NotNull
    @Override
    public PagedSlot get(int index) {
        return slots.get(index);
    }

    public @NotNull ItemStack getItem(int index) {
        if (!isValidIndex(index)) return ItemStack.EMPTY;
        return get(index).getItem();
    }

    @Override
    public @NotNull ItemStack removeItem(int slot, int amount) {
        if (!isValidIndex(slot)) return ItemStack.EMPTY;
        return get(slot).split(amount);
    }

    @Override
    public @NotNull ItemStack removeItemNoUpdate(int slot) {
        return takeItem(slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (!isValidIndex(slot)) return;
        get(slot).replaceItem(stack);
    }

    @Override
    public void setChanged() {

    }

    public boolean isRemoved() {
        return removed;
    }

    public void setRemoved(boolean removed) {
        this.removed = removed;
    }

    @Override
    public boolean stillValid(Player player) {
        return this != EMPTY && this.size > 0 && !this.removed;
    }

    @Override
    public PagedSlot set(int index, @NotNull PagedSlot slot) {
        Validate.notNull(slot);
        return slots.set(index, slot);
    }

    public ItemStack replaceItem(int index, ItemStack item) {
        if (!isValidIndex(index)) return item;
        return get(index).replaceItem(item);
    }

    @Override
    public void add(int index, @NotNull PagedSlot slot) {
        Validate.notNull(slot);
        slots.add(index, slot);
    }

    @Override
    public boolean add(@NotNull PagedSlot slot) {
        Validate.notNull(slot);
        return insertItem(slot.getItem()).isEmpty();
    }

    public ItemStack insertItem(ItemStack item) {
        if (item.isEmpty()) {
            return ItemStack.EMPTY;
        }
        PagedSlot emptySlot = null;
        for (PagedSlot pagedSlot : slots) {
            if (pagedSlot.canMerge(item)) {
                pagedSlot.merge(item);
                if (item.isEmpty()) {
                    return ItemStack.EMPTY;
                }
            } else if (pagedSlot.isEmpty() && emptySlot == null) {
                emptySlot = pagedSlot;
            }
        }
        if (!item.isEmpty() && emptySlot != null) {
            emptySlot.replaceItem(item.copyAndClear());
            return ItemStack.EMPTY;
        }
        return item;
    }

    @Override
    public PagedSlot remove(int index) {
        return slots.remove(index);
    }

    public ItemStack takeItem(int index) {
        if (!isValidIndex(index)) return ItemStack.EMPTY;
        return get(index).takeItem();
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void clear() {
        for (PagedSlot item : slots) {
            item.takeItem();
        }
    }

    public int getPage() {
        return page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
        int itemSize = slots.size();
        if (size < itemSize) {
            slots.subList(size, itemSize).clear();
        } else if (size > itemSize) {
            for (int i = itemSize; i < size; i++) {
                slots.add(PagedSlot.empty(page, i));
            }
        }
    }

    public boolean isValidIndex(int index) {
        return index >= 0 && index < size;
    }

    public List<ItemStack> getItems() {
        List<ItemStack> list = new ArrayList<>();
        for (PagedSlot item : slots) {
            list.add(item.getItem());
        }
        return list;
    }

    public List<PagedSlot> getSlots() {
        return List.copyOf(slots);
    }

    @Override
    public int getContainerSize() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        for (PagedSlot item : slots) {
            if (!item.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public CompoundTag save(CompoundTag tag) {
        tag.putInt("page", page);
        tag.putInt("size", size);
        ListTag slotTags = new ListTag();
        for (PagedSlot slot : slots) {
            slotTags.add(slot.save(new CompoundTag()));
        }
        tag.put("slots", slotTags);
        return tag;
    }

    public static PagedList fromTag(CompoundTag tag) {
        int page = tag.getInt("page");
        int size = tag.getInt("size");
        ListTag slotTags = tag.getList("slots", Tag.TAG_COMPOUND);
        ArrayList<PagedSlot> slots = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            slots.add(PagedSlot.fromTag(slotTags.getCompound(i)));
        }
        slots.sort(Comparator.comparing(PagedSlot::getSlot));
        return new PagedList(slots, page, size);
    }
}
