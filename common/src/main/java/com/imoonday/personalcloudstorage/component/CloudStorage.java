package com.imoonday.personalcloudstorage.component;

import com.imoonday.personalcloudstorage.client.ClientCloudStorage;
import com.imoonday.personalcloudstorage.client.screen.menu.CloudStorageMenu;
import com.imoonday.personalcloudstorage.network.SyncCloudStorageS2CPacket;
import com.imoonday.personalcloudstorage.network.SyncSettingsPacket;
import com.imoonday.personalcloudstorage.platform.Services;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class CloudStorage {

    public static final int SLOTS_PER_ROW = 9;
    private static final int MAX_ROWS = 6;
    protected final Map<Integer, PagedList> pages;
    protected final CloudStorageSettings settings = new CloudStorageSettings();
    protected UUID playerUUID;
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
        this.pages = new HashMap<>();
        this.addNewPage();
    }

    public int addNewPage() {
        if (totalPages == Integer.MAX_VALUE) {
            return totalPages - 1;
        }
        pages.put(totalPages, PagedList.create(totalPages, pageSize));
        return totalPages++;
    }

    public static int fixRows(int rows) {
        return Mth.clamp(rows, 1, MAX_ROWS);
    }

    protected CloudStorage(UUID playerUUID, int pageSize, int totalPages, Map<Integer, PagedList> pages) {
        this.playerUUID = playerUUID;
        this.pageSize = pageSize;
        this.totalPages = totalPages;
        this.pages = pages;
    }

    @Nullable
    public <T> T findFirst(Function<PagedList, T> function) {
        return findFirstOrDefault(function, null);
    }

    public <T> T findFirstOrDefault(Function<PagedList, T> function, T defaultValue) {
        return findFirstOrElse(function, () -> defaultValue);
    }

    public <T> T findFirstOrElse(Function<PagedList, T> function, Supplier<T> defaultValue) {
        for (int i = 0; i < this.totalPages; i++) {
            T result = function.apply(this.getPage(i));
            if (result != null) {
                return result;
            }
        }
        return defaultValue.get();
    }

    public PagedList getPage(int page) {
        if (totalPages <= 0) {
            return PagedList.empty();
        }
        page = Mth.clamp(page, 0, totalPages - 1);
        return pages.computeIfAbsent(page, i -> PagedList.create(i, this.pageSize));
    }

    public void copyFrom(CloudStorage other) {
        this.pageSize = other.pageSize;
        this.totalPages = other.totalPages;
        this.pages.clear();
        other.pages.forEach((key, value) -> this.pages.put(key, value.copy()));
    }

    public void setPlayerNameIfAbsent(@Nullable Component playerName) {
        if (this.playerName == null) {
            this.setPlayerName(playerName);
        }
    }

    public void syncSettings(@Nullable Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            Services.PLATFORM.sendToPlayer(serverPlayer, new SyncSettingsPacket(this.settings.save(new CompoundTag())));
        }
    }

    public boolean isSynced() {
        return true;
    }

    public void openMenu(ServerPlayer player) {
        syncToClient(player);
        player.openMenu(new SimpleMenuProvider((i, inventory, player1) -> new CloudStorageMenu(i, inventory, this), getMenuTitle(player)));
    }

    public void syncToClient(Player player) {
        syncToClient(player, true);
    }

    public void syncToClient(Player player, boolean check) {
        if (player instanceof ServerPlayer serverPlayer) {
            AbstractContainerMenu menu = serverPlayer.containerMenu;
            if (!check || !(menu instanceof CloudStorageMenu cloudStorageMenu) || cloudStorageMenu.getCloudStorage() == this) {
                Services.PLATFORM.sendToPlayer(serverPlayer, new SyncCloudStorageS2CPacket(this));
            }
            MinecraftServer server = player.getServer();
            if (server != null) {
                syncToVisitors(server);
            }
        }
    }

    public void syncToVisitors(MinecraftServer server) {
        List<ServerPlayer> players = server.getPlayerList().getPlayers();
        for (ServerPlayer otherPlayer : players) {
            AbstractContainerMenu menu = otherPlayer.containerMenu;
            if (menu instanceof CloudStorageMenu cloudStorageMenu && cloudStorageMenu.getCloudStorage() == this) {
                Services.PLATFORM.sendToPlayer(otherPlayer, new SyncCloudStorageS2CPacket(this));
                cloudStorageMenu.updateSlots();
            }
        }
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

    @Nullable
    public Component getPlayerName() {
        return playerName;
    }

    public void setPlayerName(@Nullable Component playerName) {
        this.playerName = playerName;
    }

    public List<PagedSlot> getAllSlots() {
        List<PagedSlot> items = new ArrayList<>();
        forEachPage(page -> items.addAll(page.getSlots()), true);
        return items;
    }

    public void forEachPage(Consumer<PagedList> consumer, boolean createIfAbsent) {
        for (int i = 0; i < this.totalPages; i++) {
            PagedList page = createIfAbsent ? this.getPage(i) : this.pages.get(i);
            if (page != null) {
                consumer.accept(page);
            }
        }
    }

    public List<ItemStack> getAllItems() {
        List<ItemStack> items = new ArrayList<>();
        forEachPage(page -> items.addAll(page.getItems()), false);
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

    @Nullable
    public PagedList removeLastPage() {
        if (totalPages > 1) {
            PagedList list = getPage(totalPages-- - 1);
            list.setRemoved(true);
            pages.remove(list.getPage());
            return list;
        }
        return null;
    }

    public int removeLastPageIfEmpty() {
        if (totalPages > 1) {
            PagedList list = getPage(totalPages - 1);
            if (list.isEmpty()) {
                list.setRemoved(true);
                pages.remove(list.getPage());
                totalPages--;
                return 1;
            }
            return 0;
        }
        return -1;
    }

    @Nullable
    public ServerPlayer findOnlinePlayer(@NotNull MinecraftServer server) {
        ServerPlayer player = server.getPlayerList().getPlayer(playerUUID);
        if (player == null && playerName != null) {
            player = server.getPlayerList().getPlayerByName(playerName.getString());
        }
        return player;
    }

    public boolean addRow() {
        int rows = getPageRows();
        if (rows < MAX_ROWS) {
            updatePageSize(rows + 1);
            return true;
        }
        return false;
    }

    public int getPageRows() {
        return (int) Math.ceil((double) pageSize / SLOTS_PER_ROW);
    }

    public void updatePageSize(int rows) {
        this.pageSize = fixRows(rows) * SLOTS_PER_ROW;
        forEachPage(page -> page.setSize(this.pageSize), false);
    }

    public void updateTotalPages(int totalPages) {
        if (totalPages < 0) {
            totalPages = 0;
        }

        int size = this.totalPages;
        if (totalPages < size) {
            for (int i = totalPages; i < size; i++) {
                pages.remove(i);
            }
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
        if (page == null || totalPages <= 0) {
            return PagedList.empty();
        }
        int currentPage = page.getPage();
        int next = currentPage + 1;
        if (settings.cycleThroughPages) {
            next = next % totalPages;
        }
        return getPage(next);
    }

    public PagedList getPreviousPage(PagedList page) {
        if (page == null || totalPages <= 0) {
            return PagedList.empty();
        }
        int currentPage = page.getPage();
        int previous = currentPage - 1;
        if (settings.cycleThroughPages) {
            previous = (previous + totalPages) % totalPages;
        }
        return getPage(previous);
    }

    public ItemStack addItem(ItemStack item) {
        if (item == null || item.isEmpty()) {
            return ItemStack.EMPTY;
        }
        return findFirstOrDefault(page -> page.insertItem(item).isEmpty() ? ItemStack.EMPTY : null, item);
    }

    public ItemStack takeItem(int page, int slot) {
        if (page < 0 || page >= totalPages || slot < 0 || slot >= pageSize) {
            return ItemStack.EMPTY;
        }
        PagedSlot pagedSlot = getPage(page).get(slot);
        if (pagedSlot.isEmpty()) {
            return ItemStack.EMPTY;
        }
        return pagedSlot.takeItem();
    }

    public ItemStack replaceItem(int page, int slot, ItemStack item) {
        if (page < 0 || page >= totalPages || slot < 0 || slot >= pageSize) {
            return ItemStack.EMPTY;
        }
        return getPage(page).get(slot).replaceItem(item);
    }

    public int removeItem(Item item, int maxCount) {
        if (item == Items.AIR || maxCount <= 0) {
            return 0;
        }

        int[] remainingCount = {maxCount};
        return findFirstOrElse(page -> {
            for (PagedSlot slot : page) {
                if (slot != null && slot.getItem().is(item)) {
                    ItemStack taken = slot.split(remainingCount[0]);
                    if (!taken.isEmpty()) {
                        int takenCount = taken.getCount();
                        remainingCount[0] -= takenCount;
                    }
                    if (remainingCount[0] <= 0) {
                        return maxCount;
                    }
                }
            }
            return null;
        }, () -> maxCount - remainingCount[0]);
    }

    public boolean hasItem(Item item, int count) {
        if (item == Items.AIR || count <= 0) {
            return false;
        }

        int[] remainingCount = {count};
        return findFirstOrDefault(page -> {
            for (PagedSlot slot : page) {
                if (slot != null && slot.getItem().is(item)) {
                    remainingCount[0] -= slot.getItem().getCount();
                    if (remainingCount[0] <= 0) {
                        return true;
                    }
                }
            }
            return null;
        }, false);
    }

    public void clear() {
        forEachPage(PagedList::clear, false);
    }

    public void tick(Player player) {
        if (player != null && !player.level().isClientSide && settings.autoDownload) {
            Inventory inventory = player.getInventory();
            for (int i = 0; i < inventory.getContainerSize(); i++) {
                ItemStack stack = inventory.getItem(i);
                if (stack.isEmpty()) continue;
                int count = stack.getCount();
                int maxStackSize = stack.getMaxStackSize();
                if (count < maxStackSize) {
                    int remaining = maxStackSize - count;
                    stack.grow(this.removeItem(stack, remaining).getCount());
                }
            }
        }
    }

    public ItemStack removeItem(ItemStack item, int maxCount) {
        if (item == null || item.isEmpty() || maxCount <= 0) {
            return ItemStack.EMPTY;
        }

        ItemStack result = item.copyWithCount(0);
        int[] remainingCount = {maxCount};
        return findFirstOrDefault(page -> {
            for (PagedSlot slot : page) {
                if (slot != null && slot.isSameItemSameTags(item)) {
                    ItemStack taken = slot.split(remainingCount[0]);
                    if (!taken.isEmpty()) {
                        int takenCount = taken.getCount();
                        result.grow(takenCount);
                        remainingCount[0] -= takenCount;
                    }
                    if (remainingCount[0] <= 0) {
                        return result;
                    }
                }
            }
            return null;
        }, result);
    }

    public CloudStorageSettings getSettings() {
        return settings;
    }

    public CompoundTag save(CompoundTag tag) {
        if (tag == null) {
            tag = new CompoundTag();
        }
        tag.putUUID("playerUUID", playerUUID);
        tag.putInt("pageSize", pageSize);
        tag.putInt("totalPages", totalPages);
        ListTag pagesTag = new ListTag();
        forEachPage(page -> pagesTag.add(page.save(new CompoundTag())), false);
        tag.put("pages", pagesTag);
        if (playerName != null) {
            tag.putString("playerName", Component.Serializer.toJson(playerName));
        }
        tag.put("settings", settings.save(new CompoundTag()));
        return tag;
    }

    public void load(CompoundTag tag) {
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
                PagedList page = PagedList.fromTag(pagesTag.getCompound(i));
                pages.put(page.getPage(), page);
            }
        }
        if (tag.contains("playerName")) {
            playerName = Component.Serializer.fromJson(tag.getString("playerName"));
        }
        if (tag.contains("settings")) {
            settings.load(tag.getCompound("settings"));
        }
    }

    public void loadSettings(CompoundTag tag) {
        if (tag != null) {
            settings.load(tag);
        }
    }

    public static CloudStorage of(@NotNull Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            return CloudStorageData.get(serverPlayer);
        }
        return ClientCloudStorage.get();
    }

    public static boolean isValidRows(int rows) {
        return rows >= 1 && rows <= MAX_ROWS;
    }

    @Nullable
    public static CloudStorage fromTag(CompoundTag tag) {
        if (tag == null) return null;
        if (!tag.contains("playerUUID")) return null;
        UUID playerUUID = tag.getUUID("playerUUID");
        int pageSize = tag.getInt("pageSize");
        int totalPages = tag.getInt("totalPages");
        Map<Integer, PagedList> pages = new HashMap<>();
        ListTag pagesTag = tag.getList("pages", Tag.TAG_COMPOUND);
        for (int i = 0; i < pagesTag.size(); i++) {
            PagedList page = PagedList.fromTag(pagesTag.getCompound(i));
            pages.put(page.getPage(), page);
        }
        CloudStorage storage = new CloudStorage(playerUUID, pageSize, totalPages, pages);
        if (tag.contains("playerName")) {
            storage.setPlayerName(Component.Serializer.fromJson(tag.getString("playerName")));
        }
        if (tag.contains("settings")) {
            storage.settings.load(tag.getCompound("settings"));
        }
        return storage;
    }
}
