package com.imoonday.personalcloudstorage.core;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class PagedSlot {

    private final int page;
    private final int slot;
    private ItemStack item;

    public PagedSlot(int page, int slot) {
        this(ItemStack.EMPTY, page, slot);
    }

    public PagedSlot(ItemStack item, int page, int slot) {
        this.item = item;
        this.page = page;
        this.slot = slot;
    }

    public ItemStack getItem() {
        return item;
    }

    public int getPage() {
        return page;
    }

    public int getSlot() {
        return slot;
    }

    public boolean isEmpty() {
        return item.isEmpty();
    }

    public boolean isEmptySlot() {
        return false;
    }

    public int getCount() {
        return item.getCount();
    }

    public ItemStack split(int amount) {
        ItemStack itemStack = item.split(amount);
        if (itemStack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        return itemStack;
    }

    public ItemStack merge(ItemStack stack) {
        int newCount = this.item.getCount() + stack.getCount();
        int maxSize = this.item.getMaxStackSize();
        if (newCount > maxSize) {
            newCount = maxSize;
        }
        int increment = newCount - this.item.getCount();
        this.item.setCount(newCount);
        stack.shrink(increment);
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        return stack;
    }

    public boolean canMerge(ItemStack stack) {
        return isSameItemSameTags(stack) && this.item.getCount() < this.item.getMaxStackSize();
    }

    public boolean isSameItemSameTags(ItemStack stack) {
        return ItemStack.isSameItemSameTags(this.item, stack);
    }

    public ItemStack takeItem() {
        return replaceItem(ItemStack.EMPTY);
    }

    public ItemStack replaceItem(ItemStack stack) {
        ItemStack oldItem = this.item;
        this.item = stack;
        if (oldItem.isEmpty()) {
            return ItemStack.EMPTY;
        }
        return oldItem;
    }

    public boolean isSameItem(ItemStack stack) {
        return ItemStack.isSameItem(this.item, stack);
    }

    public PagedSlot copy() {
        return new PagedSlot(item.copy(), page, slot);
    }

    public ItemStack copyItem() {
        return item.copy();
    }

    public ItemStack copyWithCount(int count) {
        return item.copyWithCount(count);
    }

    public boolean isSamePosition(PagedSlot other) {
        return page == other.page && slot == other.slot;
    }

    public CompoundTag save(CompoundTag tag) {
        tag.putInt("page", page);
        tag.putInt("slot", slot);
        if (!item.isEmpty()) {
            tag.put("item", item.save(new CompoundTag()));
        }
        return tag;
    }

    @Override
    public int hashCode() {
        return Objects.hash(hashStackAndTag(item), page, slot);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PagedSlot pagedSlot)) return false;
        return page == pagedSlot.page && slot == pagedSlot.slot && isSameItemSameTags(pagedSlot.item);
    }

    @Override
    public String toString() {
        return "PagedSlot{" +
               "item=" + item.getHoverName().getString() +
               ", page=" + page +
               ", slot=" + slot +
               '}';
    }

    private static int hashStackAndTag(@Nullable ItemStack stack) {
        if (stack != null) {
            CompoundTag compoundTag = stack.getTag();
            int i = 31 + stack.getItem().hashCode();
            return 31 * i + (compoundTag == null ? 0 : compoundTag.hashCode());
        } else {
            return 0;
        }
    }

    public static PagedSlot empty() {
        return new EmptyPagedSlot();
    }

    public static PagedSlot fromTag(CompoundTag tag) {
        int page = tag.getInt("page");
        int slot = tag.getInt("slot");
        ItemStack item = tag.contains("item") ? ItemStack.of(tag.getCompound("item")) : ItemStack.EMPTY;
        return new PagedSlot(item, page, slot);
    }
}
