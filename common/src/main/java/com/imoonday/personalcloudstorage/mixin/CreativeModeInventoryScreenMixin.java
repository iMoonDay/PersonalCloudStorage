package com.imoonday.personalcloudstorage.mixin;

import com.imoonday.personalcloudstorage.client.ClientConfig;
import com.imoonday.personalcloudstorage.client.screen.widget.CloudStorageWidget;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.CreativeModeTab;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CreativeModeInventoryScreen.class)
public abstract class CreativeModeInventoryScreenMixin extends EffectRenderingInventoryScreen<CreativeModeInventoryScreen.ItemPickerMenu> {

    @Shadow
    private static CreativeModeTab selectedTab;
    @Unique
    public CloudStorageWidget cloudStorageWidget;

    private CreativeModeInventoryScreenMixin(CreativeModeInventoryScreen.ItemPickerMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        throw new IllegalStateException("Mixin constructor called");
    }

    @Inject(method = "init", at = @At("RETURN"))
    private void init(CallbackInfo ci) {
        ClientConfig config = ClientConfig.get();
        if (config.hideButton) return;
        int offsetX = config.buttonOffsetX;
        int offsetY = config.buttonOffsetY;
        this.cloudStorageWidget = new CloudStorageWidget(this.leftPos + this.imageWidth - 18 - 5 + offsetX, this.topPos + 5 + offsetY, 18, 18);
        this.updateVisibility();
        this.addRenderableWidget(cloudStorageWidget);
    }

    @Inject(method = "selectTab", at = @At("RETURN"))
    private void selectTab(CreativeModeTab tab, CallbackInfo ci) {
        this.updateVisibility();
    }

    @Unique
    private void updateVisibility() {
        if (this.cloudStorageWidget != null) {
            this.cloudStorageWidget.visible = selectedTab.getType() == CreativeModeTab.Type.INVENTORY;
        }
    }
}
