package com.imoonday.personalcloudstorage.forge.client;

import com.imoonday.personalcloudstorage.client.KeyBinding;
import com.imoonday.personalcloudstorage.client.ModKeys;
import com.imoonday.personalcloudstorage.client.screen.CloudStorageScreen;
import com.imoonday.personalcloudstorage.init.ModItems;
import com.imoonday.personalcloudstorage.init.ModMenuType;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientModEventHandler {

    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent e) {
        for (KeyBinding key : ModKeys.KEYS) {
            e.register(key.getKeyMapping());
        }
    }

    @SubscribeEvent
    public static void registerMenuScreens(FMLClientSetupEvent e) {
        MenuScreens.register(ModMenuType.CLOUD_STORAGE.get(), CloudStorageScreen::new);
    }

    @SubscribeEvent
    public static void onBuildCreativeTabs(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(ModItems.CLOUD_CORE.get());
            event.accept(ModItems.PARTITION_NODE.get());
        }
    }
}
