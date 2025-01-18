package com.imoonday.personalcloudstorage.network;

import com.imoonday.personalcloudstorage.core.CloudStorage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public class OpenCloudStorageC2SRequest implements NetworkPacket {

    public OpenCloudStorageC2SRequest() {

    }

    public OpenCloudStorageC2SRequest(FriendlyByteBuf buf) {

    }

    @Override
    public void write(FriendlyByteBuf buf) {

    }

    @Override
    public void handle(@Nullable Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            CloudStorage.of(serverPlayer).openMenu(serverPlayer);
        }
    }
}
