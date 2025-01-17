package com.imoonday.personalcloudstorage.network;

import com.imoonday.personalcloudstorage.client.ClientCloudStorage;
import com.imoonday.personalcloudstorage.client.ClientHandler;
import com.imoonday.personalcloudstorage.component.CloudStorage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public record SyncCloudStorageS2CPacket(UUID playerUUID, int pageSize, int totalPages) implements NetworkPacket {

    public SyncCloudStorageS2CPacket(FriendlyByteBuf buf) {
        this(buf.readUUID(), buf.readInt(), buf.readInt());
    }

    public SyncCloudStorageS2CPacket(CloudStorage cloudStorage) {
        this(cloudStorage.getPlayerUUID(), cloudStorage.getPageSize(), cloudStorage.getTotalPages());
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeUUID(playerUUID);
        buf.writeInt(pageSize);
        buf.writeInt(totalPages);
    }

    @Override
    public void handle(@Nullable Player player) {
        if (player != null && player.level().isClientSide) {
            ClientCloudStorage cloudStorage = ClientCloudStorage.getOrCreate(playerUUID);
            cloudStorage.updateClient(playerUUID, pageSize, totalPages);
            ClientHandler.onUpdate(player);
        }
    }
}
