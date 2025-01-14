package com.imoonday.personalcloudstorage.component;

import com.imoonday.personalcloudstorage.api.CloudStorageContainer;
import com.imoonday.personalcloudstorage.network.SyncCloudStorageS2CPacket;
import com.imoonday.personalcloudstorage.platform.Services;
import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class CloudStorage {

    private static final Logger LOGGER = LogUtils.getLogger();
    private final UUID playerUUID;
    private final List<PagedList> pages;
    private int pageSize;
    private int totalPages;
    private boolean dirty;

    public CloudStorage(UUID playerUUID, int pageSize) {
        this.playerUUID = playerUUID;
        this.pageSize = pageSize;
        this.pages = new ArrayList<>();
        this.addNewPage();
    }

    protected CloudStorage(UUID playerUUID, int pageSize, int totalPages, List<PagedList> pages) {
        this.playerUUID = playerUUID;
        this.pageSize = pageSize;
        this.totalPages = totalPages;
        this.pages = pages;
    }

    public static CloudStorage of(Player player) {
        return ((CloudStorageContainer) player).getCloudStorage();
    }

    public List<PagedSlot> getAllSlots() {
        List<PagedSlot> items = new ArrayList<>();
        for (PagedList page : pages) {
            items.addAll(page.getSlots());
        }
        return items;
    }

    public List<ItemStack> getAllItems() {
        List<ItemStack> items = new ArrayList<>();
        for (PagedList page : pages) {
            items.addAll(page.getItems());
        }
        return items;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public void addNewPage() {
        PagedList list = PagedList.create(totalPages++, pageSize);
        list.setSaveAction(this::markDirty);
        pages.add(list);
        markDirty();
    }

    public void updatePageSize(int pageSize) {
        this.pageSize = pageSize;
        for (PagedList page : pages) {
            page.setSize(pageSize);
        }
        markDirty();
    }

    public PagedList getPage(int page) {
        if (!hasPage(page)) {
            return PagedList.EMPTY;
        }
        return pages.get(page);
    }

    public boolean hasPage(int page) {
        return page >= 0 && page < totalPages;
    }

    public PagedList getNextPage(PagedList page) {
        int index = pages.indexOf(page);
        if (index < 0 || index >= totalPages - 1) {
            return page;
        }
        return pages.get(index + 1);
    }

    public PagedList getPreviousPage(PagedList page) {
        int index = pages.indexOf(page);
        if (index < 1 || index >= totalPages) {
            return page;
        }
        return pages.get(index - 1);
    }

    public ItemStack addItem(ItemStack item) {
        if (item.isEmpty()) {
            return ItemStack.EMPTY;
        }
        markDirty();
        for (PagedList page : pages) {
            if (page.insertItem(item).isEmpty()) {
                return ItemStack.EMPTY;
            }
        }
        return item;
    }

    public ItemStack takeItem(int page, int slot) {
        if (page < 0 || page >= totalPages || slot < 0 || slot >= pageSize) {
            return ItemStack.EMPTY;
        }
        PagedSlot pagedSlot = pages.get(page).get(slot);
        if (pagedSlot.isEmpty()) {
            return ItemStack.EMPTY;
        }
        markDirty();
        return pagedSlot.takeItem();
    }

    public ItemStack replaceItem(int page, int slot, ItemStack item) {
        if (page < 0 || page >= totalPages || slot < 0 || slot >= pageSize) {
            return ItemStack.EMPTY;
        }
        PagedSlot pagedSlot = pages.get(page).get(slot);
        if (pagedSlot.isEmpty()) {
            return ItemStack.EMPTY;
        }
        markDirty();
        return pagedSlot.replaceItem(item);
    }

    public ItemStack removeItem(ItemStack item, int maxCount) {
        if (item.isEmpty() || maxCount <= 0) {
            return ItemStack.EMPTY;
        }
        markDirty();
        ItemStack result = item.copyWithCount(0);
        for (PagedList page : pages) {
            for (PagedSlot slot : page) {
                if (slot.isSameItemSameTags(item)) {
                    ItemStack taken = slot.split(maxCount);
                    if (!taken.isEmpty()) {
                        int takenCount = taken.getCount();
                        result.grow(takenCount);
                        maxCount -= takenCount;
                    }
                    if (maxCount <= 0) {
                        return result;
                    }
                }
            }
        }
        return result;
    }

    public List<PagedSlot> getFilteredSlots(String filter) {
        if (filter == null || filter.isEmpty()) {
            return getAllSlots();
        }
        filter = filter.toLowerCase();
        List<PagedSlot> slots = new ArrayList<>();
        for (PagedSlot slot : getAllSlots()) {
            ItemStack itemStack = slot.getItem();
            boolean matches = false;
            if (itemStack.getHoverName().getString().toLowerCase().contains(filter)) {
                matches = true;
            } else if (BuiltInRegistries.ITEM.getKey(itemStack.getItem()).toString().toLowerCase().contains(filter)) {
                matches = true;
            }
            if (matches) {
                slots.add(slot);
            }
        }
        slots.sort(Comparator.comparing((PagedSlot slot) -> !slot.getItem().hasTag()) // 排序：有标签的优先
                             .thenComparing(slot -> slot.getPage() * pageSize + slot.getSlot()) // 排序：按槽位顺序
                             .thenComparing(slot -> slot.getItem().getHoverName().getString()) // 排序：按名称
                             .thenComparing(slot -> BuiltInRegistries.ITEM.getId(slot.getItem().getItem())) // 排序：按物品注册 ID
        );
        return slots;
    }

    public void markDirty() {
        dirty = true;
    }

    public boolean isDirty() {
        return dirty;
    }

    public void sync(ServerPlayer player) {
        if (dirty) {
            Services.PLATFORM.sendToPlayer(player, new SyncCloudStorageS2CPacket(this));
            dirty = false;
        }
    }

    public void clear() {
        for (PagedList page : pages) {
            page.clear();
        }
    }

    public CompoundTag toTag(CompoundTag tag) {
        tag.putUUID("playerUUID", playerUUID);
        tag.putInt("pageSize", pageSize);
        tag.putInt("totalPages", totalPages);
        ListTag pagesTag = new ListTag();
        for (PagedList page : pages) {
            pagesTag.add(page.toTag(new CompoundTag()));
        }
        tag.put("pages", pagesTag);
        return tag;
    }

    @Nullable
    public static CloudStorage fromTag(@Nullable CompoundTag tag) {
        if (tag == null) return null;
        if (!tag.hasUUID("playerUUID")) {
            LOGGER.error("Missing playerUUID in CloudStorage tag");
            return null;
        }
        UUID playerUUID = tag.getUUID("playerUUID");
        int pageSize = tag.getInt("pageSize");
        int totalPages = tag.getInt("totalPages");
        List<PagedList> pages = new ArrayList<>();
        ListTag pagesTag = tag.getList("pages", Tag.TAG_COMPOUND);
        for (int i = 0; i < pagesTag.size(); i++) {
            pages.add(PagedList.fromTag(pagesTag.getCompound(i)));
        }
        return new CloudStorage(playerUUID, pageSize, totalPages, pages);
    }

    public void readFrom(@Nullable CompoundTag tag) {
        if (tag == null) return;
        if (!tag.hasUUID("playerUUID")) {
            LOGGER.warn("Missing playerUUID when reading CloudStorage tag");
        } else if (tag.getUUID("playerUUID") != playerUUID) {
            LOGGER.warn("Different playerUUID when reading CloudStorage tag");
        }
        if (tag.contains("pageSize")) {
            pageSize = tag.getInt("pageSize");
        }
        if (tag.contains("totalPages")) {
            totalPages = tag.getInt("totalPages");
        }
        if (tag.contains("pages")) {
            this.pages.clear();
            ListTag pagesTag = tag.getList("pages", Tag.TAG_COMPOUND);
            for (int i = 0; i < pagesTag.size(); i++) {
                this.pages.add(PagedList.fromTag(pagesTag.getCompound(i)));
            }
        }
    }
}