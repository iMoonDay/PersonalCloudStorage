package com.imoonday.personalcloudstorage.fabric.client;

import com.imoonday.personalcloudstorage.client.ClientHandler;
import com.imoonday.personalcloudstorage.client.KeyBinding;
import com.imoonday.personalcloudstorage.client.ModKeys;
import com.imoonday.personalcloudstorage.client.PersonalCloudStorageClient;
import com.imoonday.personalcloudstorage.client.screen.CloudStorageScreen;
import com.imoonday.personalcloudstorage.init.ModMenuType;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenKeyboardEvents;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;

public final class PersonalCloudStorageFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        PersonalCloudStorageClient.initClient();
        registerKeys();
        registerMenuScreens();
        registerScreenEvents();
        ClientTickEvents.END_CLIENT_TICK.register(minecraft -> {
            ClientHandler.onClientTick(minecraft.player);
        });
        ClientPlayConnectionEvents.DISCONNECT.register((listener, minecraft) -> {
            ClientHandler.onDisconnect();
        });
    }

    private void registerKeys() {
        for (KeyBinding key : ModKeys.KEYS) {
            KeyBindingHelper.registerKeyBinding(key.getKeyMapping());
        }
        ClientTickEvents.END_CLIENT_TICK.register(minecraft -> {
            for (KeyBinding key : ModKeys.KEYS) {
                key.tick();
            }
        });
    }

    private void registerMenuScreens() {
        MenuScreens.register(ModMenuType.CLOUD_STORAGE.get(), CloudStorageScreen::new);
    }

    private void registerScreenEvents() {
        ScreenEvents.BEFORE_INIT.register((minecraft, screen, scaledWidth, scaledHeight) -> {
            if (screen instanceof EffectRenderingInventoryScreen<?> inventoryScreen) {
                ScreenKeyboardEvents.afterKeyPress(inventoryScreen).register((screen1, key, scancode, modifiers) -> {
                    if (ModKeys.OPEN_CLOUD_STORAGE_INVENTORY.matches(key, scancode)) {
                        ClientHandler.openCloudStorage();
                    }
                });
            }
        });
    }
}
