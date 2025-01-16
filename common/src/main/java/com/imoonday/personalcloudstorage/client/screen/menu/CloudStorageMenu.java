package com.imoonday.personalcloudstorage.client.screen.menu;

import com.imoonday.personalcloudstorage.component.CloudStorage;
import com.imoonday.personalcloudstorage.component.PagedList;
import com.imoonday.personalcloudstorage.config.ServerConfig;
import com.imoonday.personalcloudstorage.init.ModItems;
import com.imoonday.personalcloudstorage.init.ModMenuType;
import com.imoonday.personalcloudstorage.mixin.SlotAccessor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class CloudStorageMenu extends AbstractContainerMenu {

    public static final int PREVIOUS_PAGE_BUTTON_ID = 0;
    public static final int NEXT_PAGE_BUTTON_ID = 1;
    public static final int ADD_PAGE_BUTTON_ID = 2;
    public static final int REMOVE_PAGE_BUTTON_ID = 3;
    private final Level level;
    private final CloudStorage cloudStorage;
    private final int containerRows;
    private final List<MutableSlot> mutableSlots = new ArrayList<>();
    private PagedList page;
    private final DataSlot currentPage = DataSlot.standalone();

    public CloudStorageMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, CloudStorage.of(playerInventory.player));
    }

    public CloudStorageMenu(int containerId, Inventory playerInventory, CloudStorage cloudStorage) {
        super(ModMenuType.CLOUD_STORAGE.get(), containerId);

        Player player = playerInventory.player;
        this.level = player.level();
        this.cloudStorage = cloudStorage;
        this.page = this.cloudStorage.getPage(0);
        this.updateSlots();
        this.containerRows = this.cloudStorage.getPageRows();

        int i;
        int j;
        int k;
        for (i = 0; i < this.page.getContainerSize(); ++i) {
            j = i % 9;
            k = i / 9;
            MutableSlot mutableSlot = new MutableSlot(this.page, i, 8 + j * 18, 18 + k * 18);
            this.addSlot(mutableSlot);
            mutableSlots.add(mutableSlot);
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

        this.addDataSlot(currentPage).set(0);
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
            case REMOVE_PAGE_BUTTON_ID -> {
                if (disallowModification(player)) {
                    player.sendSystemMessage(Component.translatable("message.personalcloudstorage.cannot_modify"));
                    return false;
                }
                return removePage(player);
            }
        }
        return super.clickMenuButton(player, id);
    }

    public boolean disallowModification(Player player) {
        return !(player.level().isClientSide ? ServerConfig.getClientCache() : ServerConfig.get()).modifyStorageOfOthers && !player.getUUID().equals(cloudStorage.getPlayerUUID());
    }

    public boolean removePage(Player player) {
        int result = cloudStorage.removeLastPageIfEmpty();
        if (result == 1) {
            if (!player.getAbilities().instabuild) {
                ItemStack stack = new ItemStack(ModItems.PARTITION_NODE.get(), 1);
                if (!player.addItem(stack)) {
                    player.spawnAtLocation(stack);
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
        if (checkAddAvailable(player, true)) {
            int totalPages = cloudStorage.getTotalPages();
            int newTotalPages = cloudStorage.addNewPage() + 1;
            cloudStorage.syncToClient(player);
            updateSlots();
            return newTotalPages > totalPages;
        }
        return false;
    }

    public boolean checkAddAvailable(Player player, boolean shrink) {
        if (player.getAbilities().instabuild) {
            return true;
        }
        return player.getInventory().hasAnyMatching(stack -> {
            if (stack.is(ModItems.PARTITION_NODE.get())) {
                if (shrink) {
                    stack.shrink(1);
                }
                return true;
            }
            return false;
        });
    }

    @Override
    public boolean stillValid(Player player) {
        return page.stillValid(player);
    }

    public CloudStorage getCloudStorage() {
        return cloudStorage;
    }

    public int getCurrentPage() {
        return currentPage.get();
    }

    public void nextPage() {
        page = cloudStorage.getNextPage(page);
        updateSlots();
    }

    public void previousPage() {
        page = cloudStorage.getPreviousPage(page);
        updateSlots();
    }

    public void updateSlots() {
        page = cloudStorage.getPage(page.getPage());
        currentPage.set(page.getPage());
        for (MutableSlot mutableSlot : mutableSlots) {
            mutableSlot.updateContainer(page);
        }
        if (!level.isClientSide) {
            this.broadcastChanges();
        }
    }

    public int getContainerRows() {
        return this.containerRows;
    }

    private static class MutableSlot extends Slot {

        public MutableSlot(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        public void updateContainer(Container container) {
            ((SlotAccessor) this).setContainer(container);
        }
    }
}
