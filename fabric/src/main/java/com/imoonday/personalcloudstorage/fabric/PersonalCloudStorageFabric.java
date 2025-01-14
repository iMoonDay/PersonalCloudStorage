package com.imoonday.personalcloudstorage.fabric;

import com.imoonday.personalcloudstorage.PersonalCloudStorage;
import com.imoonday.personalcloudstorage.fabric.network.FabricNetworkHandler;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

public final class PersonalCloudStorageFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        PersonalCloudStorage.init();
        FabricNetworkHandler.init();
    }
}
