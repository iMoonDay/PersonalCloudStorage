package com.imoonday.personalcloudstorage.event;

import com.imoonday.personalcloudstorage.client.ClientUtils;
import com.imoonday.personalcloudstorage.component.CloudStorage;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public class EventHandler {

    public static void syncToClient(ServerPlayer player) {
        CloudStorage.of(player).syncToClient(player);
    }

    public static void onPlayerClone(ServerPlayer oldPlayer, ServerPlayer newPlayer) {
        CloudStorage.of(newPlayer).copyFrom(CloudStorage.of(oldPlayer));
    }

    public static void onClientTick(Player player) {
        if (player != null && player.level().isClientSide && !CloudStorage.of(player).isSynced()) {
            ClientUtils.requestUpdate();
        }
    }
}
