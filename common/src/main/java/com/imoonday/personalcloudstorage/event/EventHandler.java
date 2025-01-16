package com.imoonday.personalcloudstorage.event;

import com.imoonday.personalcloudstorage.component.CloudStorage;
import com.imoonday.personalcloudstorage.config.ServerConfig;
import com.imoonday.personalcloudstorage.network.SyncConfigS2CPacket;
import com.imoonday.personalcloudstorage.platform.Services;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public class EventHandler {

    public static void syncToClient(Player player) {
        CloudStorage.of(player).syncToClient(player);
        if (player instanceof ServerPlayer serverPlayer) {
            Services.PLATFORM.sendToPlayer(serverPlayer, new SyncConfigS2CPacket(ServerConfig.get().save(new CompoundTag())));
        }
    }

    public static void loadConfig() {
        ServerConfig.load();
    }
}
