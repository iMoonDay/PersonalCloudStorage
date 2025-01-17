package com.imoonday.personalcloudstorage.forge.client;

import com.imoonday.personalcloudstorage.client.*;
import com.imoonday.personalcloudstorage.client.screen.CloudStorageScreen;
import com.imoonday.personalcloudstorage.init.ModItems;
import com.imoonday.personalcloudstorage.init.ModMenuType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@SuppressWarnings("removal")
public class ClientEventHandler {

    public static void init() {
        PersonalCloudStorageClient.initClient();

        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(ClientEventHandler::registerKeys);
        modEventBus.addListener(ClientEventHandler::registerMenuScreens);
        modEventBus.addListener(ClientEventHandler::onBuildCreativeTabs);

        IEventBus eventBus = MinecraftForge.EVENT_BUS;
        eventBus.addListener(ClientEventHandler::onClientTick);
        eventBus.addListener(ClientEventHandler::onDisconnect);

        if (PersonalCloudStorageClient.clothConfig) {
            ModLoadingContext.get().registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class, () -> {
                return new ConfigScreenHandler.ConfigScreenFactory((minecraft, screen) -> {
                    return ModConfigScreenFactory.create(screen);
                });
            });
        }
    }

    private static void registerKeys(RegisterKeyMappingsEvent e) {
        for (KeyBinding key : ModKeys.KEYS) {
            e.register(key.getKeyMapping());
        }
    }

    private static void onClientTick(TickEvent.ClientTickEvent e) {
        if (e.phase == TickEvent.Phase.END) {
            for (KeyBinding key : ModKeys.KEYS) {
                if (key.hasPressAction()) {
                    while (key.getKeyMapping().consumeClick()) {
                        key.onPress(Minecraft.getInstance());
                    }
                }
            }
            ClientHandler.onClientTick(Minecraft.getInstance().player);
        }
    }

    private static void registerMenuScreens(FMLClientSetupEvent e) {
        MenuScreens.register(ModMenuType.CLOUD_STORAGE.get(), CloudStorageScreen::new);
    }

    private static void onBuildCreativeTabs(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(ModItems.CLOUD_CORE.get());
            event.accept(ModItems.PARTITION_NODE.get());
        }
    }

    private static void onDisconnect(ClientPlayerNetworkEvent.LoggingOut event) {
        ClientHandler.onDisconnect();
    }
}
