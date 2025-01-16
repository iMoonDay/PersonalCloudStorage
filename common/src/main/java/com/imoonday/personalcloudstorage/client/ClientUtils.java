package com.imoonday.personalcloudstorage.client;

import com.imoonday.personalcloudstorage.api.CloudStorageListener;
import com.imoonday.personalcloudstorage.component.CloudStorage;
import com.imoonday.personalcloudstorage.config.ServerConfig;
import com.imoonday.personalcloudstorage.network.OpenCloudStorageC2SRequest;
import com.imoonday.personalcloudstorage.network.RequestSyncC2SRequest;
import com.imoonday.personalcloudstorage.platform.Services;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

public class ClientUtils {

    public static void openCloudStorage() {
        Services.PLATFORM.sendToServer(new OpenCloudStorageC2SRequest());
    }

    public static void requestUpdate() {
        Services.PLATFORM.sendToServer(new RequestSyncC2SRequest());
    }

    public static void onClientTick(Player player) {
        if (player != null && player.level().isClientSide && !CloudStorage.of(player).isSynced()) {
            requestUpdate();
        }
    }

    public static void onUpdate(Player player, CloudStorage cloudStorage) {
        if (player.containerMenu instanceof CloudStorageListener listener) {
            listener.onUpdate(cloudStorage);
        }
        if (Minecraft.getInstance().screen instanceof CloudStorageListener listener) {
            listener.onUpdate(cloudStorage);
        }
    }

    public static void onDisconnect() {
        ServerConfig.getClientCache().reset();
    }
}
