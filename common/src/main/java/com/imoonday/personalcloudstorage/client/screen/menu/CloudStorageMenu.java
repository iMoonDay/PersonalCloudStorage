package com.imoonday.personalcloudstorage.client.screen.menu;

import com.imoonday.personalcloudstorage.component.CloudStorage;
import com.imoonday.personalcloudstorage.component.PagedList;
import com.imoonday.personalcloudstorage.init.ModMenuType;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class CloudStorageMenu extends AbstractContainerMenu {

    private static final int SLOTS_PER_ROW = 9;
    private final Player player;
    private final CloudStorage cloudStorage;
    private final PagedList page;
    private final int containerRows;

    public CloudStorageMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, 0);
    }

    public CloudStorageMenu(int containerId, Inventory playerInventory, int page) {
        super(ModMenuType.CLOUD_STORAGE.get(), containerId);
        this.player = playerInventory.player;
        this.cloudStorage = CloudStorage.of(player);
        this.page = cloudStorage.getPage(page);
        this.containerRows = (int) Math.ceil((double) this.page.getContainerSize() / SLOTS_PER_ROW);

        int offset = (this.containerRows - 4) * 18;
        int i;
        int j;
        int k;
        int size = this.page.getContainerSize();
        for (i = 0; i < size; ++i) {
            j = i % 9;
            k = i / 9;
            this.addSlot(new Slot(this.page, i, 8 + j * 18, 18 + k * 18));
        }
        for (i = 0; i < 3; ++i) {
            for (j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 103 + i * 18 + offset));
            }
        }
        for (i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 161 + offset));
        }
    }

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
    public boolean stillValid(Player player) {
        return page.stillValid(player);
    }

    public Player getPlayer() {
        return player;
    }

    public CloudStorage getCloudStorage() {
        return cloudStorage;
    }

    public PagedList getPage() {
        return page;
    }

    public int getContainerRows() {
        return containerRows;
    }
}
