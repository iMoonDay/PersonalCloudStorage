package com.imoonday.personalcloudstorage.forge.client;

import com.imoonday.personalcloudstorage.client.ClientHandler;
import com.imoonday.personalcloudstorage.client.ModKeys;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ClientForgeEventHandler {

    @SubscribeEvent
    public static void onDisconnect(ClientPlayerNetworkEvent.LoggingOut event) {
        ClientHandler.onDisconnect();
    }

    @SubscribeEvent
    public static void onKeyPressed(ScreenEvent.KeyPressed.Post event) {
        if (event.getScreen() instanceof EffectRenderingInventoryScreen<?>) {
            int keyCode = event.getKeyCode();
            int scanCode = event.getScanCode();
            if (ModKeys.OPEN_CLOUD_STORAGE_INVENTORY.matches(keyCode, scanCode)) {
                ClientHandler.openCloudStorage();
            }
        }
    }
}
