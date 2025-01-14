package com.imoonday.personalcloudstorage.client.screen;

import com.imoonday.personalcloudstorage.client.ClientUtils;
import com.imoonday.personalcloudstorage.client.screen.menu.CloudStorageMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.lwjgl.glfw.GLFW;

public class CloudStorageScreen extends AbstractContainerScreen<CloudStorageMenu> {

    private static final ResourceLocation CONTAINER_BACKGROUND = new ResourceLocation("textures/gui/container/generic_54.png");
    private final int containerRows;

    public CloudStorageScreen(CloudStorageMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.containerRows = menu.getContainerRows();
        this.imageHeight = 114 + this.containerRows * 18;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void init() {
        super.init();
        ClientUtils.switchingPage = false;
        Button prevPageButton = Button.builder(Component.literal("<"), button -> {
            ClientUtils.openPreviousPage();
        }).bounds(this.leftPos - 20 - 10, (this.height - 20) / 2, 20, 20).build();
        this.addRenderableWidget(prevPageButton);

        Button nextPageButton = Button.builder(Component.literal(">"), button -> {
            ClientUtils.openNextPage();
        }).bounds(this.leftPos + this.imageWidth + 10, (this.height - 20) / 2, 20, 20).build();
        this.addRenderableWidget(nextPageButton);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        guiGraphics.blit(CONTAINER_BACKGROUND, i, j, 0, 0, this.imageWidth, this.containerRows * 18 + 17);
        guiGraphics.blit(CONTAINER_BACKGROUND, i, j + this.containerRows * 18 + 17, 0, 126, this.imageWidth, 96);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_A) {
            ClientUtils.openPreviousPage();
            return true;
        } else if (keyCode == GLFW.GLFW_KEY_D) {
            ClientUtils.openNextPage();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
