package com.imoonday.personalcloudstorage.core;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class PageSlot {

    private final int page;
    private final int slot;
    private ItemStack item;

    public PageSlot(int page, int slot) {
        this(ItemStack.EMPTY, page, slot);
    }

    public PageSlot(ItemStack item, int page, int slot) {
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

    public PageSlot copy() {
        return new PageSlot(item.copy(), page, slot);
    }

    public PageSlot withPage(int page) {
        if (page == this.page) {
            return this;
        }
        return new PageSlot(item, page, slot);
    }

    public PageSlot withSlot(int slot) {
        if (slot == this.slot) {
            return this;
        }
        return new PageSlot(item, page, slot);
    }

    public ItemStack copyItem() {
        return item.copy();
    }

    public ItemStack copyWithCount(int count) {
        return item.copyWithCount(count);
    }

    public boolean isSamePosition(PageSlot other) {
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
        if (!(o instanceof PageSlot pageSlot)) return false;
        return page == pageSlot.page && slot == pageSlot.slot && isSameItemSameTags(pageSlot.item);
    }

    @Override
    public String toString() {
        return "PageSlot{" +
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

    public static PageSlot empty() {
        return Empty.INSTANCE;
    }

    public static PageSlot fromTag(CompoundTag tag) {
        int page = tag.getInt("page");
        int slot = tag.getInt("slot");
        ItemStack item = tag.contains("item") ? ItemStack.of(tag.getCompound("item")) : ItemStack.EMPTY;
        return new PageSlot(item, page, slot);
    }

    public static class Empty extends PageSlot {

        public static final Empty INSTANCE = new Empty();

        private Empty() {
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
        public PageSlot copy() {
            return new Empty();
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
        public boolean isSamePosition(PageSlot other) {
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
            return o instanceof Empty;
        }

        @Override
        public String toString() {
            return "EmptyPageSlot";
        }
    }
}
