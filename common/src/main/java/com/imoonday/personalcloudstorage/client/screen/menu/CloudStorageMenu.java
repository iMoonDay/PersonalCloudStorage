package com.imoonday.personalcloudstorage.client.screen.menu;

import com.imoonday.personalcloudstorage.api.SlotAction;
import com.imoonday.personalcloudstorage.api.SlotActionListener;
import com.imoonday.personalcloudstorage.component.CloudStorage;
import com.imoonday.personalcloudstorage.component.PagedList;
import com.imoonday.personalcloudstorage.component.PagedSlot;
import com.imoonday.personalcloudstorage.init.ModMenuType;
import com.imoonday.personalcloudstorage.network.SlotActionC2SPacket;
import com.imoonday.personalcloudstorage.network.UpdateCloudStorageS2CPacket;
import com.imoonday.personalcloudstorage.platform.Services;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class CloudStorageMenu extends AbstractContainerMenu implements SlotActionListener {

    private final Player player;
    private final CloudStorage cloudStorage;
    private PagedList currentPage;
    private final List<PositionedSlot> renderingSlots = new ArrayList<>();
    @Nullable
    private PagedSlot lastDragged;

    public CloudStorageMenu(int containerId, Inventory playerInventory) {
        super(ModMenuType.CLOUD_STORAGE.get(), containerId);
        this.player = playerInventory.player;
        this.cloudStorage = CloudStorage.of(player);
        this.currentPage = cloudStorage.getPage(0);
        this.updateRenderingSlots();

        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 103 + i * 18));
            }
        }
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 161));
        }
    }

    public static void refresh(@Nullable Player player) {
        if (player != null && player.containerMenu instanceof CloudStorageMenu menu) {
            menu.updateRenderingSlots();
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot.hasItem()) {
            ItemStack itemStack2 = slot.getItem();
            itemStack = itemStack2.copy();
            if (!currentPage.insertItem(itemStack2).isEmpty()) {
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
        return getCurrentPage().stillValid(player);
    }

    public Player getPlayer() {
        return player;
    }

    public CloudStorage getCloudStorage() {
        return cloudStorage;
    }

    public PagedList getCurrentPage() {
        return currentPage;
    }

    public void nextPage() {
        currentPage = cloudStorage.getNextPage(currentPage);
        updateRenderingSlots();
    }

    public void previousPage() {
        currentPage = cloudStorage.getPreviousPage(currentPage);
        updateRenderingSlots();
    }

    public void updateRenderingSlots() {
        currentPage = cloudStorage.getPage(currentPage.getPage());
        renderingSlots.clear();
        for (int i = 0; i < currentPage.getSlots().size(); i++) {
            PagedSlot slot = currentPage.get(i);
            int x = (i % 9) * 18 + 8;
            int y = (i / 9) * 18 + 18;
            renderingSlots.add(new PositionedSlot(slot, x, y));
        }
    }

    public int getContainerRows() {
        return 4;
    }

    public List<PositionedSlot> getRenderingSlots() {
        return renderingSlots;
    }

    public boolean onClick(double mouseX, double mouseY, int button) {
        for (PositionedSlot slot : renderingSlots) {
            if (slot.isMouseOver(mouseX, mouseY)) {
                slot.onClick(button);
                return true;
            }
        }
        return false;
    }

    public boolean onShiftMouseDown(double mouseX, double mouseY, int button) {
        for (PositionedSlot slot : renderingSlots) {
            if (slot.isMouseOver(mouseX, mouseY)) {
                if (lastDragged != null && lastDragged.isSamePosition(slot.slot)) {
                    return false;
                }
                if (slot.slot.isEmpty()) {
                    return false;
                }
                slot.onClick(button);
                lastDragged = slot.slot;
                return true;
            }
        }
        return false;
    }

    @Override
    public void onSlotClicked(SlotAction slotAction, boolean hasShift, int page, int slot) {
        if (!cloudStorage.hasPage(page) || !cloudStorage.getPage(page).isValidIndex(slot)) return;

        PagedSlot clickedSlot = cloudStorage.getPage(page).get(slot);
        ItemStack clicked = clickedSlot.getItem();
        ItemStack carried = this.getCarried();
        switch (slotAction) {
            case LEFT_CLICK -> {
                if (!hasShift) {
                    if (!carried.isEmpty()) {
                        if (clicked.isEmpty()) {
                            this.setCarried(clickedSlot.replaceItem(carried));
                        } else {
                            this.setCarried(clickedSlot.combine(carried));
                        }
                    } else if (!clicked.isEmpty()) {
                        this.setCarried(clickedSlot.takeItem());
                    }
                } else if (!clicked.isEmpty()) {
                    this.moveItemStackTo(clicked, 0, slots.size(), false);
                }
                updateSlotToClient(clickedSlot);
            }
            case RIGHT_CLICK -> {
                if (carried.isEmpty()) {
                    if (!clicked.isEmpty()) {
                        this.setCarried(clickedSlot.split((clicked.getCount() + 1) / 2));
                    }
                } else if (clicked.isEmpty()) {
                    clickedSlot.replaceItem(carried.split(1));
                } else if (clickedSlot.combine(carried.copyWithCount(1)).isEmpty()) {
                    carried.split(1);
                }
                updateSlotToClient(clickedSlot);
            }
            case MIDDLE_CLICK -> {
                if (!clicked.isEmpty() && carried.isEmpty() && player.getAbilities().instabuild) {
                    this.setCarried(clickedSlot.copyWithCount(clicked.getMaxStackSize()));
                }
            }
        }
    }

    private void updateSlotToClient(PagedSlot slot) {
        if (player instanceof ServerPlayer serverPlayer) {
            Services.PLATFORM.sendToPlayer(serverPlayer, new UpdateCloudStorageS2CPacket(UpdateCloudStorageS2CPacket.Type.SLOT, slot.toTag(new CompoundTag())));
        }
    }

    public record PositionedSlot(PagedSlot slot, int x, int y) {

        public ItemStack getItem() {
            return slot.getItem();
        }

        public boolean isMouseOver(double x, double y) {
            return this.x <= x && x <= this.x + 16 && this.y <= y && y <= this.y + 16;
        }

        public void onClick(int button) {
            SlotAction slotAction = switch (button) {
                case 0 -> SlotAction.LEFT_CLICK;
                case 1 -> SlotAction.RIGHT_CLICK;
                case 2 -> SlotAction.MIDDLE_CLICK;
                default -> null;
            };
            if (slotAction != null) {
                Services.PLATFORM.sendToServer(new SlotActionC2SPacket(slotAction, Screen.hasShiftDown(), slot));
            }
        }
    }
}
