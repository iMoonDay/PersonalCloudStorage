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
import net.minecraft.client.gui.screens.MenuScreens;

public final class PersonalCloudStorageFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        PersonalCloudStorageClient.initClient();
        registerKeys();
        registerMenuScreens();
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
                if (key.hasPressAction()) {
                    while (key.getKeyMapping().consumeClick()) {
                        key.onPress(minecraft);
                    }
                }
            }
        });
    }

    private void registerMenuScreens() {
        MenuScreens.register(ModMenuType.CLOUD_STORAGE.get(), CloudStorageScreen::new);
    }
}
