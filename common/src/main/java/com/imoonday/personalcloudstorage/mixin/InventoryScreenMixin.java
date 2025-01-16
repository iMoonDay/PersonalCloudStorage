package com.imoonday.personalcloudstorage.mixin;

import com.imoonday.personalcloudstorage.client.ClientConfig;
import com.imoonday.personalcloudstorage.client.screen.widget.CloudStorageWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InventoryScreen.class)
public abstract class InventoryScreenMixin extends EffectRenderingInventoryScreen<InventoryMenu> {

    @Unique
    public CloudStorageWidget cloudStorageWidget;

    private InventoryScreenMixin(InventoryMenu menu, Inventory playerInventory, Component title) {
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
        this.addRenderableWidget(cloudStorageWidget);
    }

    @Inject(method = "method_19891", at = @At("RETURN"))
    private void method_19891(Button button, CallbackInfo ci) {
        if (this.cloudStorageWidget != null) {
            int offsetX = ClientConfig.get().buttonOffsetX;
            this.cloudStorageWidget.setX(this.leftPos + this.imageWidth - 18 - 5 + offsetX);
        }
    }
}
