package com.imoonday.personalcloudstorage.network;

import com.imoonday.personalcloudstorage.config.ServerConfig;
import com.imoonday.personalcloudstorage.core.CloudStorage;
import com.imoonday.personalcloudstorage.platform.Services;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public class RequestSyncC2SRequest implements NetworkPacket {

    public RequestSyncC2SRequest() {

    }

    public RequestSyncC2SRequest(FriendlyByteBuf buf) {

    }

    @Override
    public void write(FriendlyByteBuf buf) {

    }

    @Override
    public void handle(@Nullable Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            CloudStorage.of(serverPlayer).syncToPlayer(serverPlayer);
            Services.PLATFORM.sendToPlayer(serverPlayer, new SyncConfigS2CPacket(ServerConfig.get().save(new CompoundTag())));
        }
    }
}
