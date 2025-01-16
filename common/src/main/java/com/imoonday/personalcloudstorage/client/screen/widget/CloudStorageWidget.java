package com.imoonday.personalcloudstorage.client.screen.widget;

import com.imoonday.personalcloudstorage.PersonalCloudStorage;
import com.imoonday.personalcloudstorage.client.ClientUtils;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class CloudStorageWidget extends AbstractWidget {

    public static final ResourceLocation TEXTURE = PersonalCloudStorage.id("textures/gui/cloud_storage.png");
    public static final int TEXTURE_WIDTH = 64;
    public static final int TEXTURE_HEIGHT = 64;

    public CloudStorageWidget(int x, int y, int width, int height) {
        super(x, y, width, height, Component.empty());
        setTooltip(Tooltip.create(Component.translatable("widget.personalcloudstorage.tooltip")));
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        RenderSystem.enableBlend();
        guiGraphics.blit(TEXTURE, this.getX(), this.getY(), this.getWidth(), this.getHeight(), 0.0F, 0.0F, TEXTURE_WIDTH, TEXTURE_HEIGHT, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        RenderSystem.disableBlend();
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        super.onClick(mouseX, mouseY);
        ClientUtils.openCloudStorage();
    }
}
