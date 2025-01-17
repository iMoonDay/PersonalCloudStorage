package com.imoonday.personalcloudstorage.fabric;

import com.imoonday.personalcloudstorage.PersonalCloudStorage;
import com.imoonday.personalcloudstorage.command.CommandHandler;
import com.imoonday.personalcloudstorage.event.EventHandler;
import com.imoonday.personalcloudstorage.fabric.network.FabricNetworkHandler;
import com.imoonday.personalcloudstorage.init.ModItems;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.world.item.CreativeModeTabs;

public final class PersonalCloudStorageFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        PersonalCloudStorage.init();
        FabricNetworkHandler.init();
        registerEvents();
    }

    private void registerEvents() {
        ServerPlayConnectionEvents.JOIN.register((listener, sender, server) -> {
            EventHandler.onPlayerJoin(listener.player);
        });
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register(entries -> {
            entries.accept(ModItems.CLOUD_CORE.get());
            entries.accept(ModItems.PARTITION_NODE.get());
        });
        CommandRegistrationCallback.EVENT.register(CommandHandler::registerCommands);
        ServerLifecycleEvents.SERVER_STARTING.register(server -> EventHandler.loadConfig());
    }
}
