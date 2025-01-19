package com.imoonday.personalcloudstorage.client.screen.widget;

import com.imoonday.personalcloudstorage.PersonalCloudStorage;
import com.imoonday.personalcloudstorage.client.ClientConfig;
import com.imoonday.personalcloudstorage.client.ClientHandler;
import com.imoonday.personalcloudstorage.client.ModConfigScreenFactory;
import com.imoonday.personalcloudstorage.client.PersonalCloudStorageClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class CloudStorageButton extends ImageButton {

    public static final ResourceLocation TEXTURE = PersonalCloudStorage.id("textures/gui/cloud_storage.png");
    private boolean animated;
    private float offset;

    public CloudStorageButton(int x, int y, boolean animated) {
        super(x, y, 20, 16, 0, 0, 19, TEXTURE, button -> ClientHandler.openCloudStorage());
        this.animated = animated;
        this.offset = this.getMaxOffset();
    }

    public int getMaxOffset() {
        return animated ? Math.max(this.height - 3, 0) : 0;
    }

    public void updateXForInventory(int leftPos, int imageWidth) {
        int offsetX = ClientConfig.get().buttonOffsetX;
        this.setX(calculateXForInventory(leftPos, imageWidth, offsetX));
    }

    private static int calculateXForInventory(int leftPos, int imageWidth, int offsetX) {
        return leftPos + imageWidth - 20 - 3 + offsetX;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.offset += (isHovered ? -partialTick : partialTick) * 3;
        this.offset = Mth.clamp(this.offset, 0, this.getMaxOffset());
        int y = (int) (this.getY() + this.offset);
        int height = this.getY() + this.height - y;
        this.renderTexture(guiGraphics, this.resourceLocation, this.getX(), y, this.xTexStart, this.yTexStart, this.yDiffTex, this.width, height, this.textureWidth, this.textureHeight);
    }

    public boolean isAnimated() {
        return animated;
    }

    public void setAnimated(boolean animated) {
        this.animated = animated;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        } else if (PersonalCloudStorageClient.clothConfig && this.active && this.visible && button == 1 && this.clicked(mouseX, mouseY)) {
            Minecraft minecraft = Minecraft.getInstance();
            this.playDownSound(minecraft.getSoundManager());
            minecraft.setScreen(ModConfigScreenFactory.create(minecraft.screen));
            return true;
        }
        return false;
    }

    public static CloudStorageButton createForInventory(int leftPos, int topPos, int imageWidth, int imageHeight) {
        ClientConfig config = ClientConfig.get();
        int offsetX = config.buttonOffsetX;
        int offsetY = config.buttonOffsetY;
        CloudStorageButton button = new CloudStorageButton(calculateXForInventory(leftPos, imageWidth, offsetX), topPos - 16 + offsetY, config.hideButton);
        button.setTooltip(Tooltip.create(Component.translatable("widget.personalcloudstorage.open_button.tooltip")));
        return button;
    }
}
