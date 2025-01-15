package com.imoonday.personalcloudstorage.component;

import com.imoonday.personalcloudstorage.api.CloudStorageContainer;
import com.imoonday.personalcloudstorage.network.UpdateCloudStorageS2CPacket;
import com.imoonday.personalcloudstorage.platform.Services;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class CloudStorage {

    private static final int SLOTS_PER_ROW = 9;
    private final List<PagedList> pages;
    private int pageSize;
    private int totalPages;
    private boolean synced;

    public CloudStorage(int rows) {
        if (rows < 0) {
            rows = 0;
        }
        this.pageSize = rows * SLOTS_PER_ROW;
        this.pages = new ArrayList<>();
        this.addNewPage();
    }

    protected CloudStorage(int pageSize, int totalPages, List<PagedList> pages) {
        this.pageSize = pageSize;
        this.totalPages = totalPages;
        this.pages = pages;
    }

    public void copyFrom(CloudStorage other) {
        this.pageSize = other.pageSize;
        this.totalPages = other.totalPages;
        this.pages.clear();
        for (PagedList page : other.pages) {
            this.pages.add(page.copy());
        }
    }

    public boolean update(PagedSlot slot) {
        int page = slot.getPage();
        if (!hasPage(page)) return false;
        PagedList pagedList = getPage(page);
        int slotIndex = slot.getSlot();
        if (pagedList.isValidIndex(slotIndex)) {
            pagedList.setItem(slotIndex, slot.getItem().copy());
            return true;
        }
        return false;
    }

    public boolean update(PagedList page) {
        for (PagedList pagedList : pages) {
            if (pagedList.getPage() == page.getPage()) {
                pagedList.setSize(page.getSize());
                for (int i = 0; i < page.size(); i++) {
                    pagedList.setItem(i, page.get(i).getItem().copy());
                }
                return true;
            }
        }
        return false;
    }

    public void update(CloudStorage other) {
        other.pages.forEach(this::update);
    }

    /**
     * Should call this method after modifying {@link #pageSize} or {@link #totalPages} on server
     */
    public void syncToClient(ServerPlayer player) {
        Services.PLATFORM.sendToPlayer(player, new UpdateCloudStorageS2CPacket(this));
    }

    /**
     * Called from client
     */
    public void updateClient(int pageSize, int totalPages) {
        this.pageSize = pageSize;
        this.totalPages = totalPages;
        this.pages.clear();
        for (int i = 0; i < totalPages; i++) {
            this.pages.add(PagedList.create(i, pageSize));
        }
        synced = true;
    }

    public boolean isSynced() {
        return synced;
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

    public int getPageSize() {
        return pageSize;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public int getPageRows() {
        return (int) Math.ceil((double) pageSize / SLOTS_PER_ROW);
    }

    /**
     * Should call {@link #syncToClient(ServerPlayer)} after calling this method.
     */
    public void addNewPage() {
        pages.add(PagedList.create(totalPages++, pageSize));
    }

    /**
     * Should call {@link #syncToClient(ServerPlayer)} after calling this method.
     */
    public void updatePageSize(int rows) {
        if (rows < 0) {
            rows = 0;
        }
        this.pageSize = rows * 9;
        for (PagedList page : pages) {
            page.setSize(this.pageSize);
        }
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
        if (index < 0) {
            return page;
        }
        int next = (index + 1) % totalPages;
        return pages.get(next);
    }

    public PagedList getPreviousPage(PagedList page) {
        int index = pages.indexOf(page);
        if (index < 0) {
            return page;
        }
        int previous = (index + totalPages - 1) % totalPages;
        return pages.get(previous);
    }

    public ItemStack addItem(ItemStack item) {
        if (item.isEmpty()) {
            return ItemStack.EMPTY;
        }
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
        return pagedSlot.takeItem();
    }

    public ItemStack replaceItem(int page, int slot, ItemStack item) {
        if (page < 0 || page >= totalPages || slot < 0 || slot >= pageSize) {
            return ItemStack.EMPTY;
        }
        return pages.get(page).get(slot).replaceItem(item);
    }

    public ItemStack removeItem(ItemStack item, int maxCount) {
        if (item.isEmpty() || maxCount <= 0) {
            return ItemStack.EMPTY;
        }
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

    public void clear() {
        for (PagedList page : pages) {
            page.clear();
        }
    }

    public CompoundTag toTag(CompoundTag tag) {
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
        int pageSize = tag.getInt("pageSize");
        int totalPages = tag.getInt("totalPages");
        List<PagedList> pages = new ArrayList<>();
        ListTag pagesTag = tag.getList("pages", Tag.TAG_COMPOUND);
        for (int i = 0; i < pagesTag.size(); i++) {
            pages.add(PagedList.fromTag(pagesTag.getCompound(i)));
        }
        return new CloudStorage(pageSize, totalPages, pages);
    }

    public void readFrom(@Nullable CompoundTag tag) {
        if (tag == null) return;
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