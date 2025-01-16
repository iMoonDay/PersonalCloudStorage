package com.imoonday.personalcloudstorage.component;

import com.imoonday.personalcloudstorage.client.ClientCloudStorage;
import com.imoonday.personalcloudstorage.client.screen.menu.CloudStorageMenu;
import com.imoonday.personalcloudstorage.network.SyncCloudStorageS2CPacket;
import com.imoonday.personalcloudstorage.platform.Services;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CloudStorage {

    public static final int SLOTS_PER_ROW = 9;
    private static final int MAX_ROWS = 6;
    protected UUID playerUUID;
    protected final List<PagedList> pages;
    protected int pageSize;
    protected int totalPages;
    @Nullable
    protected Component playerName;

    public CloudStorage(UUID playerUUID) {
        this(playerUUID, 3);
    }

    public CloudStorage(UUID playerUUID, int rows) {
        this.playerUUID = playerUUID;
        this.pageSize = fixRows(rows) * SLOTS_PER_ROW;
        this.pages = new ArrayList<>();
        this.addNewPage();
    }

    protected CloudStorage(UUID playerUUID, int pageSize, int totalPages, List<PagedList> pages) {
        this.playerUUID = playerUUID;
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

    @Nullable
    public Component getPlayerName() {
        return playerName;
    }

    public void setPlayerName(@Nullable Component playerName) {
        this.playerName = playerName;
    }

    public void setPlayerNameIfAbsent(@Nullable Component playerName) {
        if (this.playerName == null) {
            this.setPlayerName(playerName);
        }
    }

    /**
     * Should call this method after modifying {@link #pageSize} or {@link #totalPages} on server
     */
    public void syncToClient(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            Services.PLATFORM.sendToPlayer(serverPlayer, new SyncCloudStorageS2CPacket(this));
            MinecraftServer server = serverPlayer.getServer();
            if (server != null) {
                List<ServerPlayer> players = server.getPlayerList().getPlayers();
                for (ServerPlayer otherPlayer : players) {
                    AbstractContainerMenu menu = otherPlayer.containerMenu;
                    if (menu instanceof CloudStorageMenu cloudStorageMenu && cloudStorageMenu.getCloudStorage() == this) {
                        Services.PLATFORM.sendToPlayer(otherPlayer, new SyncCloudStorageS2CPacket(this));
                        cloudStorageMenu.updateSlots();
                    }
                }
            }
        }
    }

    public boolean isSynced() {
        return true;
    }

    public void openMenu(ServerPlayer player) {
        syncToClient(player);
        player.openMenu(new SimpleMenuProvider((i, inventory, player1) -> new CloudStorageMenu(i, inventory, this), getMenuTitle(player)));
    }

    @NotNull
    public Component getMenuTitle(ServerPlayer player) {
        if (player.getUUID().equals(playerUUID)) {
            return Component.translatable("title.personalcloudstorage.own");
        }
        Component playerName = this.getPlayerName();
        if (playerName != null) {
            return Component.translatable("title.personalcloudstorage.other", playerName);
        }
        MinecraftServer server = player.getServer();
        if (server != null) {
            Player owner = server.getPlayerList().getPlayer(playerUUID);
            if (owner != null) {
                Component name = owner.getName();
                this.setPlayerName(name);
                return Component.translatable("title.personalcloudstorage.other", name);
            }
        }
        return Component.translatable("title.personalcloudstorage.common");
    }

    public static CloudStorage of(@NotNull Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            return CloudStorageData.get(serverPlayer);
        }
        return ClientCloudStorage.getOrCreate(player.getUUID());
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

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public int getPageSize() {
        return pageSize;
    }

    public boolean hasMaxPageSize() {
        return pageSize >= SLOTS_PER_ROW * MAX_ROWS;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public int getPageRows() {
        return (int) Math.ceil((double) pageSize / SLOTS_PER_ROW);
    }

    /**
     * Should call {@link #syncToClient(Player)} after calling this method.
     */
    public int addNewPage() {
        if (totalPages == Integer.MAX_VALUE) {
            return totalPages - 1;
        }
        pages.add(PagedList.create(totalPages, pageSize));
        return totalPages++;
    }

    /**
     * Should call {@link #syncToClient(Player)} after calling this method.
     */
    public int removeLastPage() {
        if (totalPages > 1) {
            PagedList list = pages.get(totalPages - 1);
            list.setRemoved(true);
            pages.remove(list);
            return totalPages-- - 1;
        }
        return -1;
    }

    /**
     * Should call {@link #syncToClient(Player)} after calling this method.
     */
    public int removeLastPageIfEmpty() {
        if (totalPages > 1) {
            PagedList list = pages.get(totalPages - 1);
            if (list.isEmpty()) {
                list.setRemoved(true);
                pages.remove(list);
                totalPages--;
                return 1;
            }
            return 0;
        }
        return -1;
    }

    /**
     * Should call {@link #syncToClient(Player)} after calling this method.
     */
    public void updatePageSize(int rows) {
        this.pageSize = fixRows(rows) * SLOTS_PER_ROW;
        for (PagedList page : pages) {
            page.setSize(this.pageSize);
        }
    }

    public boolean addRow() {
        int rows = getPageRows();
        if (rows < MAX_ROWS) {
            updatePageSize(rows + 1);
            return true;
        }
        return false;
    }

    public static boolean isValidRows(int rows) {
        return rows >= 1 && rows <= MAX_ROWS;
    }

    public static int fixRows(int rows) {
        if (rows < 1) {
            rows = 1;
        } else if (rows > MAX_ROWS) {
            rows = MAX_ROWS;
        }
        return rows;
    }

    public PagedList getPage(int page) {
        if (totalPages <= 0) {
            return PagedList.EMPTY;
        }
        if (page < 0) {
            page = 0;
        } else if (page >= totalPages) {
            page = totalPages - 1;
        }
        return pages.get(page);
    }

    public void updateTotalPages(int totalPages) {
        if (totalPages < 0) {
            totalPages = 0;
        }

        int size = this.totalPages;
        if (totalPages < size) {
            this.pages.subList(totalPages, size).clear();
            this.totalPages = totalPages;
        } else if (totalPages > size) {
            do {
                size = addNewPage() + 1;
            } while (size < totalPages);
        }
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

    public CompoundTag save(CompoundTag tag) {
        tag.putUUID("playerUUID", playerUUID);
        tag.putInt("pageSize", pageSize);
        tag.putInt("totalPages", totalPages);
        ListTag pagesTag = new ListTag();
        for (PagedList page : pages) {
            pagesTag.add(page.save(new CompoundTag()));
        }
        tag.put("pages", pagesTag);
        if (playerName != null) {
            tag.putString("playerName", Component.Serializer.toJson(playerName));
        }
        return tag;
    }

    @Nullable
    public static CloudStorage fromTag(@Nullable CompoundTag tag) {
        if (tag == null) return null;
        if (!tag.contains("playerUUID")) return null;
        UUID playerUUID = tag.getUUID("playerUUID");
        int pageSize = tag.getInt("pageSize");
        int totalPages = tag.getInt("totalPages");
        List<PagedList> pages = new ArrayList<>();
        ListTag pagesTag = tag.getList("pages", Tag.TAG_COMPOUND);
        for (int i = 0; i < pagesTag.size(); i++) {
            pages.add(PagedList.fromTag(pagesTag.getCompound(i)));
        }
        CloudStorage storage = new CloudStorage(playerUUID, pageSize, totalPages, pages);
        if (tag.contains("playerName")) {
            storage.setPlayerName(Component.Serializer.fromJson(tag.getString("playerName")));
        }
        return storage;
    }

    public void load(@Nullable CompoundTag tag) {
        if (tag == null) return;
        if (tag.contains("pageSize")) {
            pageSize = tag.getInt("pageSize");
        }
        if (tag.contains("totalPages")) {
            totalPages = tag.getInt("totalPages");
        }
        if (tag.contains("pages")) {
            pages.clear();
            ListTag pagesTag = tag.getList("pages", Tag.TAG_COMPOUND);
            for (int i = 0; i < pagesTag.size(); i++) {
                pages.add(PagedList.fromTag(pagesTag.getCompound(i)));
            }
        }
        if (tag.contains("playerName")) {
            playerName = Component.Serializer.fromJson(tag.getString("playerName"));
        }
    }
}