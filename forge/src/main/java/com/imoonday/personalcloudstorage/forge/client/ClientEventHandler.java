package com.imoonday.personalcloudstorage.forge.client;

import com.imoonday.personalcloudstorage.client.ModConfigScreenFactory;
import com.imoonday.personalcloudstorage.client.PersonalCloudStorageClient;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.fml.ModLoadingContext;

@SuppressWarnings("removal")
public class ClientEventHandler {

    public static void init() {
        PersonalCloudStorageClient.initClient();
        registerConfigScreen();
    }

    private static void registerConfigScreen() {
        if (PersonalCloudStorageClient.clothConfig) {
            ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class, () -> {
                return new ConfigScreenHandler.ConfigScreenFactory((minecraft, screen) -> {
                    return ModConfigScreenFactory.create(screen);
                });
            });
        }
    }
}
