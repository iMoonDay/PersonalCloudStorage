package com.imoonday.personalcloudstorage.network;

import com.imoonday.personalcloudstorage.client.screen.menu.CloudStorageMenu;
import com.imoonday.personalcloudstorage.component.CloudStorage;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public class SyncCloudStorageS2CPacket implements NetworkPacket {

    private final CompoundTag tag;

    public SyncCloudStorageS2CPacket(CloudStorage cloudStorage) {
        this.tag = cloudStorage.toTag(new CompoundTag());
    }

    public SyncCloudStorageS2CPacket(FriendlyByteBuf buf) {
        this.tag = buf.readAnySizeNbt();
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeNbt(tag);
    }

    @Override
    public void handle(@Nullable Player player) {
        if (!(player instanceof ServerPlayer)) {
            CloudStorage.of(player).readFrom(tag);
            if (player != null && player.containerMenu instanceof CloudStorageMenu menu) {
                menu.updateRenderingSlots();
            }
        }
    }
}
