package com.imoonday.personalcloudstorage.client.screen.menu;

import com.imoonday.personalcloudstorage.component.CloudStorage;
import com.imoonday.personalcloudstorage.component.PagedList;
import com.imoonday.personalcloudstorage.init.ModMenuType;
import com.imoonday.personalcloudstorage.mixin.SlotAccessor;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

public class CloudStorageMenu extends AbstractContainerMenu {

    private final Level level;
    private final CloudStorage cloudStorage;
    private final int containerRows;
    private final List<MutableSlot> mutableSlots = new ArrayList<>();
    private PagedList page;

    public CloudStorageMenu(int containerId, Inventory playerInventory) {
        super(ModMenuType.CLOUD_STORAGE.get(), containerId);

        Player player = playerInventory.player;
        this.level = player.level();
        this.cloudStorage = CloudStorage.of(player);
        this.page = cloudStorage.getPage(0);
        this.updateSlots();
        this.containerRows = cloudStorage.getPageRows();

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

    public Level getLevel() {
        return level;
    }

    public CloudStorage getCloudStorage() {
        return cloudStorage;
    }

    public PagedList getPage() {
        return page;
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
        for (MutableSlot mutableSlot : mutableSlots) {
            mutableSlot.updateContainer(page);
        }
        if (!level.isClientSide) {
            this.broadcastChanges();
        }
    }

    public int getContainerRows() {
        return containerRows;
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
