package com.imoonday.personalcloudstorage.client;

import com.imoonday.personalcloudstorage.api.CloudStorageListener;
import com.imoonday.personalcloudstorage.config.ServerConfig;
import com.imoonday.personalcloudstorage.core.CloudStorage;
import com.imoonday.personalcloudstorage.network.OpenCloudStorageC2SRequest;
import com.imoonday.personalcloudstorage.network.RequestSyncC2SRequest;
import com.imoonday.personalcloudstorage.platform.Services;
import net.minecraft.client.Minecraft;
import net.minecraft.core.UUIDUtil;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;

public class ClientHandler {

    public static void openCloudStorage() {
        ClientCloudStorage.get().syncSettings();
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

    public static void onUpdate(Player player) {
        if (player.containerMenu instanceof CloudStorageListener listener) {
            listener.onUpdate();
        }
        if (Minecraft.getInstance().screen instanceof CloudStorageListener listener) {
            listener.onUpdate();
        }
    }

    public static void onDisconnect() {
        ServerConfig.getClientCache().reset();
    }

    public static UUID getOfflinePlayerUUID() {
        return UUIDUtil.createOfflinePlayerUUID(Minecraft.getInstance().getUser().getName());
    }
}
