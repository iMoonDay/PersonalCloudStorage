package com.imoonday.personalcloudstorage.mixin;

import com.imoonday.personalcloudstorage.client.screen.widget.CloudStorageButton;
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
    public CloudStorageButton cloudStorageButton;

    private InventoryScreenMixin(InventoryMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        throw new IllegalStateException("Mixin constructor called");
    }

    @Inject(method = "init", at = @At("RETURN"))
    private void init(CallbackInfo ci) {
        this.cloudStorageButton = CloudStorageButton.createForInventory(this.leftPos, this.topPos, this.imageWidth, this.imageHeight);
        this.addRenderableWidget(cloudStorageButton);
    }

    @Inject(method = "method_19891", at = @At("RETURN"))
    private void method_19891(Button button, CallbackInfo ci) {
        if (this.cloudStorageButton != null) {
            this.cloudStorageButton.updateXForInventory(this.leftPos, this.imageWidth);
        }
    }
}
