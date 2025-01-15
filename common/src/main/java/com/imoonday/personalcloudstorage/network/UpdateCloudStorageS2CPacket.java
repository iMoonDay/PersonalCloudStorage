package com.imoonday.personalcloudstorage.network;

import com.imoonday.personalcloudstorage.component.CloudStorage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public record UpdateCloudStorageS2CPacket(int pageSize, int totalPages) implements NetworkPacket {

    public UpdateCloudStorageS2CPacket(FriendlyByteBuf buf) {
        this(buf.readInt(), buf.readInt());
    }

    public UpdateCloudStorageS2CPacket(CloudStorage cloudStorage) {
        this(cloudStorage.getPageSize(), cloudStorage.getTotalPages());
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeInt(pageSize);
        buf.writeInt(totalPages);
    }

    @Override
    public void handle(@Nullable Player player) {
        CloudStorage.of(player).updateClient(pageSize, totalPages);
    }
}
