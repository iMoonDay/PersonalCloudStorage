package com.imoonday.personalcloudstorage.client.screen.widget;

import com.google.gson.annotations.SerializedName;
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

    public CloudStorageButton(int x, int y, boolean animated, AdhesiveEdge adhesiveEdge) {
        super(x, y, 20 + adhesiveEdge.getWidthOffset(), 18 + adhesiveEdge.getHeightOffset(), adhesiveEdge.getUOffset(), adhesiveEdge.getVOffset(), 19, TEXTURE, button -> ClientHandler.openCloudStorage());
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
        CloudStorageButton button = new CloudStorageButton(calculateXForInventory(leftPos, imageWidth, offsetX), topPos - 16 + offsetY, config.hideButton, config.buttonAdhesiveEdge);
        button.setTooltip(Tooltip.create(Component.translatable("widget.personalcloudstorage.open_button.tooltip")));
        return button;
    }

    public enum AdhesiveEdge {

        @SerializedName("none")
        NONE(0, 0, 0, 0),
        @SerializedName("left")
        LEFT(-2, 0, 2, 0),
        @SerializedName("right")
        RIGHT(-2, 0, 0, 0),
        @SerializedName("top")
        TOP(0, -2, 0, 2),
        @SerializedName("bottom")
        BOTTOM(0, -2, 0, 0);

        private final Component displayName = Component.translatable("widget.personalcloudstorage.edge_direction." + name().toLowerCase());
        private final int widthOffset;
        private final int heightOffset;
        private final int uOffset;
        private final int vOffset;

        AdhesiveEdge(int widthOffset, int heightOffset, int uOffset, int vOffset) {
            this.widthOffset = widthOffset;
            this.heightOffset = heightOffset;
            this.uOffset = uOffset;
            this.vOffset = vOffset;
        }


        public Component getDisplayName() {
            return displayName;
        }

        public int getWidthOffset() {
            return widthOffset;
        }

        public int getHeightOffset() {
            return heightOffset;
        }

        public int getUOffset() {
            return uOffset;
        }

        public int getVOffset() {
            return vOffset;
        }
    }
}
