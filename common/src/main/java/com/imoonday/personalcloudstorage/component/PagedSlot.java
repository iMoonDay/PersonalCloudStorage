package com.imoonday.personalcloudstorage.component;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class PagedSlot {

    private ItemStack item;
    private int page;
    private int slot;

    public PagedSlot(ItemStack item, int page, int slot) {
        this.item = item;
        this.page = page;
        this.slot = slot;
    }

    public static PagedSlot empty(int page, int slot) {
        return new PagedSlot(ItemStack.EMPTY, page, slot);
    }

    public ItemStack getItem() {
        return item;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSlot() {
        return slot;
    }

    public void setSlot(int slot) {
        this.slot = slot;
    }

    public void moveTo(int page, int slot) {
        this.setPage(page);
        this.setSlot(slot);
    }

    public boolean isEmpty() {
        return item.isEmpty();
    }

    public int getCount() {
        return item.getCount();
    }

    public ItemStack split(int amount) {
        ItemStack splitItem = item.split(amount);
        if (splitItem.isEmpty()) {
            return ItemStack.EMPTY;
        }
        return splitItem;
    }

    public ItemStack combine(ItemStack item) {
        if (!isSameItemSameTags(item)) {
            return item;
        }
        int newCount = this.item.getCount() + item.getCount();
        int maxSize = this.item.getMaxStackSize();
        if (newCount > maxSize) {
            newCount = maxSize;
        }
        int increment = newCount - this.item.getCount();
        this.item.setCount(newCount);
        return item.split(increment);
    }

    public boolean canCombine(ItemStack item) {
        return isSameItemSameTags(item) && this.item.getCount() < this.item.getMaxStackSize();
    }

    public ItemStack takeItem() {
        return replaceItem(ItemStack.EMPTY);
    }

    public ItemStack replaceItem(ItemStack item) {
        ItemStack oldItem = this.item;
        this.item = item;
        return oldItem;
    }

    public boolean isSameItem(ItemStack item) {
        return ItemStack.isSameItem(this.item, item);
    }

    public boolean isSameItemSameTags(ItemStack item) {
        return ItemStack.isSameItemSameTags(this.item, item);
    }

    public ItemStack copy() {
        return item.copy();
    }

    public CompoundTag toTag(CompoundTag tag) {
        tag.putInt("page", page);
        tag.putInt("slot", slot);
        if (!item.isEmpty()) {
            tag.put("item", item.save(new CompoundTag()));
        }
        return tag;
    }

    public static PagedSlot fromTag(CompoundTag tag) {
        int page = tag.getInt("page");
        int slot = tag.getInt("slot");
        ItemStack item = tag.contains("item") ? ItemStack.of(tag.getCompound("item")) : ItemStack.EMPTY;
        return new PagedSlot(item, page, slot);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PagedSlot pagedSlot)) return false;
        return page == pagedSlot.page && slot == pagedSlot.slot && isSameItemSameTags(pagedSlot.item);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hashStackAndTag(item), page, slot);
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

    @Override
    public String toString() {
        return "PagedItem{" +
               "item=" + item.getHoverName().getString() +
               ", page=" + page +
               ", slot=" + slot +
               '}';
    }
}
