package com.imoonday.personalcloudstorage.fabric;

import com.imoonday.personalcloudstorage.PersonalCloudStorage;
import com.imoonday.personalcloudstorage.event.EventHandler;
import com.imoonday.personalcloudstorage.fabric.network.FabricNetworkHandler;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

public final class PersonalCloudStorageFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        PersonalCloudStorage.init();
        FabricNetworkHandler.init();
        registerEvents();
    }

    private void registerEvents() {
        ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) -> {
            EventHandler.onPlayerCopy(oldPlayer, newPlayer);
        });
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            EventHandler.onPlayerJoin(newPlayer);
        });
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            EventHandler.onPlayerJoin(handler.player);
        });
    }
}
