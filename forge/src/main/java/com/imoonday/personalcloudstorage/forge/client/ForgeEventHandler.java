package com.imoonday.personalcloudstorage.forge.client;

import com.imoonday.personalcloudstorage.client.ClientHandler;
import com.imoonday.personalcloudstorage.client.KeyBinding;
import com.imoonday.personalcloudstorage.client.ModKeys;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ForgeEventHandler {

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent e) {
        if (e.phase == TickEvent.Phase.END) {
            for (KeyBinding key : ModKeys.KEYS) {
                key.tick();
            }
        }
    }

    @SubscribeEvent
    public static void onClientPlayerTick(TickEvent.PlayerTickEvent e) {
        if (e.phase == TickEvent.Phase.END && e.side == LogicalSide.CLIENT) {
            ClientHandler.onClientTick(e.player);
        }
    }
}
