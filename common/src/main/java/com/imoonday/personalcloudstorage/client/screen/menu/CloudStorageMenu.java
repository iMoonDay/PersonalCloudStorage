package com.imoonday.personalcloudstorage.client.screen.menu;

import com.imoonday.personalcloudstorage.config.ServerConfig;
import com.imoonday.personalcloudstorage.core.CloudStorage;
import com.imoonday.personalcloudstorage.core.PageContainer;
import com.imoonday.personalcloudstorage.init.ModItems;
import com.imoonday.personalcloudstorage.init.ModMenuType;
import com.imoonday.personalcloudstorage.mixin.SlotAccessor;
import com.imoonday.personalcloudstorage.network.SyncCurrentPageS2CPacket;
import com.imoonday.personalcloudstorage.platform.Services;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class CloudStorageMenu extends AbstractContainerMenu {

    public static final int PREVIOUS_PAGE_BUTTON_ID = 0;
    public static final int NEXT_PAGE_BUTTON_ID = 1;
    public static final int ADD_PAGE_BUTTON_ID = 2;
    public static final int REMOVE_PAGE_BUTTON_ID = 3;
    public static final int REMOVE_PAGE_FORCED_BUTTON_ID = 4;
    private final Player player;
    private final Level level;
    private final CloudStorage cloudStorage;
    private final int containerRows;
    private final List<CloudStorageSlot> cloudStorageSlots = new ArrayList<>();
    private int prevPage = -1;
    private int currentPage = 0;
    private PageContainer page;
    @Nullable
    private Runnable onUpdate;

    public CloudStorageMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, CloudStorage.of(playerInventory.player));
    }

    public CloudStorageMenu(int containerId, Inventory playerInventory, CloudStorage cloudStorage) {
        super(ModMenuType.CLOUD_STORAGE.get(), containerId);

        this.player = playerInventory.player;
        this.level = player.level();
        this.cloudStorage = cloudStorage;
        this.cloudStorage.startOpen(this.player);
        this.page = this.cloudStorage.getPage(0);
        this.updateSlots();
        this.containerRows = this.cloudStorage.getPageRows();

        int i;
        int j;
        int k;
        for (i = 0; i < this.page.getContainerSize(); ++i) {
            j = i % 9;
            k = i / 9;
            CloudStorageSlot slot = new CloudStorageSlot(this.page, i, 8 + j * 18, 18 + k * 18);
            this.addSlot(slot);
            cloudStorageSlots.add(slot);
        }

        int offset = (this.containerRows - 4) * 18;
        for (i = 0; i < 3; ++i) {
            for (j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 103 + i * 18 + offset));
            }
        }
        for (i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 161 + offset));
        }
    }

    public void updateSlots() {
        page = cloudStorage.getPage(page.getPage());
        currentPage = page.getPage();
        for (CloudStorageSlot slot : cloudStorageSlots) {
            slot.updateContainer(page);
        }
        if (!level.isClientSide) {
            this.broadcastChanges();
        }
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();
        syncCurrentPage();
    }

    @Override
    public void sendAllDataToRemote() {
        super.sendAllDataToRemote();
        syncCurrentPage();
    }

    public void syncCurrentPage() {
        if (player instanceof ServerPlayer serverPlayer && currentPage != prevPage) {
            Services.PLATFORM.sendToPlayer(serverPlayer, new SyncCurrentPageS2CPacket(currentPage));
            prevPage = currentPage;
        }
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        switch (id) {
            case PREVIOUS_PAGE_BUTTON_ID -> {
                previousPage();
                return true;
            }
            case NEXT_PAGE_BUTTON_ID -> {
                nextPage();
                return true;
            }
            case ADD_PAGE_BUTTON_ID -> {
                if (disallowModification(player)) {
                    player.sendSystemMessage(Component.translatable("message.personalcloudstorage.cannot_modify"));
                    return false;
                }
                return addPage(player);
            }
            case REMOVE_PAGE_BUTTON_ID, REMOVE_PAGE_FORCED_BUTTON_ID -> {
                if (disallowModification(player)) {
                    player.sendSystemMessage(Component.translatable("message.personalcloudstorage.cannot_modify"));
                    return false;
                }
                return removePage(player, id == REMOVE_PAGE_FORCED_BUTTON_ID);
            }
        }
        return super.clickMenuButton(player, id);
    }

    @NotNull
    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot.hasItem()) {
            ItemStack itemStack2 = slot.getItem();
            itemStack = itemStack2.copy();
            int size = this.page.getContainerSize();
            if (index < size) {
                if (!this.moveItemStackTo(itemStack2, size, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(itemStack2, 0, size, false)) {
                return ItemStack.EMPTY;
            }

            if (itemStack2.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return itemStack;
    }

    @Override
    public void setItem(int slotId, int stateId, ItemStack stack) {
        super.setItem(slotId, stateId, stack);
        if (this.onUpdate != null) {
            this.onUpdate.run();
        }
    }

    @Override
    public void setData(int id, int data) {
        super.setData(id, data);
        if (this.onUpdate != null) {
            this.onUpdate.run();
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return page.stillValid(player);
    }

    public boolean disallowModification(Player player) {
        return !ServerConfig.get(player.level().isClientSide).modifyStorageOfOthers && !player.getUUID().equals(cloudStorage.getPlayerUUID());
    }

    public boolean isOwnCloudStorage() {
        return player.getUUID().equals(cloudStorage.getPlayerUUID());
    }

    public boolean removePage(Player player, boolean forced) {
        int result;
        boolean ownCloudStorage = isOwnCloudStorage();
        if (forced) {
            PageContainer removed = cloudStorage.removeLastPage();
            if (removed != null) {
                removed.forEach(slot -> {
                    if (!slot.isEmpty()) {
                        ItemStack stack = slot.getItem().copy();
                        if (ownCloudStorage || !cloudStorage.addItem(stack).isEmpty()) {
                            player.getInventory().placeItemBackInInventory(stack);
                        }
                    }
                });
                result = 1;
            } else {
                result = -1;
            }
        } else {
            result = cloudStorage.removeLastPageIfEmpty();
        }

        if (result == 1) {
            if (!player.getAbilities().instabuild) {
                ItemStack stack = new ItemStack(Items.NETHERITE_SCRAP, 1);
                if (ownCloudStorage || !cloudStorage.addItem(stack).isEmpty()) {
                    player.getInventory().placeItemBackInInventory(stack);
                }
            }
            cloudStorage.syncToClient(player);
            updateSlots();
            return true;
        } else if (result == 0) {
            player.sendSystemMessage(Component.translatable("message.personalcloudstorage.not_empty"));
        } else {
            player.sendSystemMessage(Component.translatable("message.personalcloudstorage.at_least_one"));
        }
        return false;
    }

    public boolean addPage(Player player) {
        if (checkAddAvailable(player, false)) {
            int totalPages = cloudStorage.getTotalPages();
            int newTotalPages = cloudStorage.addNewPage() + 1;
            cloudStorage.syncToClient(player);
            updateSlots();
            return newTotalPages > totalPages;
        }
        return false;
    }

    public boolean checkAddAvailable(Player player, boolean simulation) {
        int totalPages = cloudStorage.getTotalPages();
        if (totalPages >= ServerConfig.get(player.level().isClientSide).maxPages) {
            if (!simulation) {
                player.sendSystemMessage(Component.translatable("message.personalcloudstorage.reach_upper_limit"));
            }
            return false;
        }
        if (player.getAbilities().instabuild) {
            return true;
        }
        return player.getInventory().hasAnyMatching(stack -> {
            if (stack.is(ModItems.PARTITION_NODE.get())) {
                if (!simulation) {
                    stack.shrink(1);
                }
                return true;
            }
            return false;
        });
    }

    public void nextPage() {
        page = cloudStorage.getNextPage(page, getPlayerCloudStorage().getSettings().cycleThroughPages);
        updateSlots();
    }

    public void previousPage() {
        page = cloudStorage.getPreviousPage(page, getPlayerCloudStorage().getSettings().cycleThroughPages);
        updateSlots();
    }

    public void setOnUpdate(@Nullable Runnable onUpdate) {
        this.onUpdate = onUpdate;
    }

    public CloudStorage getCloudStorage() {
        return cloudStorage;
    }

    private CloudStorage getPlayerCloudStorage() {
        return CloudStorage.of(player);
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int page) {
        this.currentPage = page;
        syncCurrentPage();
    }

    public void setPageNoUpdate(int page) {
        this.page = cloudStorage.getPage(page);
    }

    public int getContainerRows() {
        return this.containerRows;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.cloudStorage.stopOpen(player);
    }

    private class CloudStorageSlot extends Slot {

        public CloudStorageSlot(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        public void updateContainer(Container container) {
            ((SlotAccessor) this).setContainer(container);
        }

        @Override
        public boolean mayPickup(Player player) {
            if (cannotModify(player)) {
                return false;
            }
            return super.mayPickup(player);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            if (cannotModify(player)) {
                return false;
            }
            return super.mayPlace(stack);
        }

        public boolean cannotModify(Player player) {
            return !player.getUUID().equals(cloudStorage.getPlayerUUID()) && !ServerConfig.get(player.level().isClientSide).modifyStorageOfOthers;
        }
    }
}
