package com.imoonday.personalcloudstorage.mixin;

import com.imoonday.personalcloudstorage.client.ClientUtils;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InventoryScreen.class)
public abstract class InventoryScreenMixin extends EffectRenderingInventoryScreen<InventoryMenu> {

    private InventoryScreenMixin(InventoryMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        throw new IllegalStateException("Mixin constructor called");
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void init(CallbackInfo ci) {
        Button button = Button.builder(Component.literal("Cloud"), button1 -> ClientUtils.openCloudStorage(this.minecraft))
                              .bounds(this.leftPos + 104 + 30, this.height / 2 - 22, 50, 20)
                              .build();
        this.addRenderableWidget(button);
    }
}
