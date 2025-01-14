package com.imoonday.personalcloudstorage.client.screen;

import com.imoonday.personalcloudstorage.client.screen.menu.CloudStorageMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

public class CloudStorageScreen extends AbstractContainerScreen<CloudStorageMenu> {

    private static final ResourceLocation CONTAINER_BACKGROUND = new ResourceLocation("textures/gui/container/generic_54.png");
    private final int containerRows;
    private boolean dragged;

    public CloudStorageScreen(CloudStorageMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.containerRows = menu.getContainerRows();
        this.imageHeight = 114 + this.containerRows * 18;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void init() {
        super.init();
        Button prevPageButton = Button.builder(Component.literal("<"), button -> {
            menu.previousPage();
        }).bounds(this.leftPos - 20 - 10, (this.height - 20) / 2, 20, 20).build();
        this.addRenderableWidget(prevPageButton);

        Button nextPageButton = Button.builder(Component.literal(">"), button -> {
            menu.nextPage();
        }).bounds(this.leftPos + this.imageWidth + 10, (this.height - 20) / 2, 20, 20).build();
        this.addRenderableWidget(nextPageButton);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        for (CloudStorageMenu.PositionedSlot renderingSlot : this.menu.getRenderingSlots()) {
            this.renderSlot(guiGraphics, mouseX, mouseY, renderingSlot);
        }
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        guiGraphics.blit(CONTAINER_BACKGROUND, i, j, 0, 0, this.imageWidth, this.containerRows * 18 + 17);
        guiGraphics.blit(CONTAINER_BACKGROUND, i, j + this.containerRows * 18 + 17, 0, 126, this.imageWidth, 96);
        int page = menu.getCurrentPage().getPage();
        int maxPage = menu.getCloudStorage().getTotalPages();
        Component text = Component.literal("Page " + (page + 1) + "/" + maxPage);
        guiGraphics.drawString(font, text, (this.width - font.width(text)) / 2, this.topPos + this.inventoryLabelY, 4210752, false);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_A) {
            menu.previousPage();
            return true;
        } else if (keyCode == GLFW.GLFW_KEY_D) {
            menu.nextPage();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        dragged = true;
        if (this.menu.onClick(mouseX - this.leftPos, mouseY - this.topPos, button)) {
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        dragged = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (dragged && hasShiftDown() && this.menu.onShiftMouseDown(mouseX - this.leftPos, mouseY - this.topPos, button)) {
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    private void renderSlot(GuiGraphics guiGraphics, int mouseX, int mouseY, CloudStorageMenu.PositionedSlot slot) {
        ItemStack itemStack = slot.getItem();
        if (!itemStack.isEmpty()) {
            int x = this.leftPos + slot.x();
            int y = this.topPos + slot.y();
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(0, 0, 100);
            guiGraphics.renderItem(itemStack, x, y, x + y * this.imageWidth);
            guiGraphics.renderItemDecorations(this.font, itemStack, x, y, null);
            if (mouseX >= x && mouseY >= y && mouseX < x + 16 && mouseY < y + 16) {
                guiGraphics.fill(x, y, x + 16, y + 16, 0x80FFFFFF);
                guiGraphics.renderTooltip(font, itemStack, mouseX, mouseY);
            }
            guiGraphics.pose().popPose();
        }
    }
}
