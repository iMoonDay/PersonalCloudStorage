package com.imoonday.personalcloudstorage.forge.client;

import com.imoonday.personalcloudstorage.client.KeyBinding;
import com.imoonday.personalcloudstorage.client.ModKeys;
import com.imoonday.personalcloudstorage.client.PersonalCloudStorageClient;
import com.imoonday.personalcloudstorage.client.screen.CloudStorageScreen;
import com.imoonday.personalcloudstorage.event.EventHandler;
import com.imoonday.personalcloudstorage.init.ModMenuType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

public class ClientEventHandler {

    public static void init() {
        PersonalCloudStorageClient.initClient();

        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(ClientEventHandler::registerKeys);
        modEventBus.addListener(ClientEventHandler::registerMenuScreens);

        IEventBus eventBus = MinecraftForge.EVENT_BUS;
        eventBus.addListener(ClientEventHandler::onClientTick);
    }

    private static void registerKeys(RegisterKeyMappingsEvent e) {
        for (KeyBinding key : ModKeys.KEYS) {
            e.register(key.getKeyMapping());
        }
    }

    private static void onClientTick(TickEvent.ClientTickEvent e) {
        if (e.phase == TickEvent.Phase.END) {
            for (KeyBinding key : ModKeys.KEYS) {
                while (key.getKeyMapping().consumeClick()) {
                    key.onPress(Minecraft.getInstance());
                }
            }
            EventHandler.onClientTick(Minecraft.getInstance().player);
        }
    }

    private static void registerMenuScreens(FMLClientSetupEvent e) {
        MenuScreens.register(ModMenuType.CLOUD_STORAGE.get(), CloudStorageScreen::new);
    }
}
