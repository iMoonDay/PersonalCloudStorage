package com.imoonday.personalcloudstorage.network;

import com.imoonday.personalcloudstorage.component.CloudStorage;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public record SyncSettingsPacket(CompoundTag tag) implements NetworkPacket {

    public SyncSettingsPacket(FriendlyByteBuf buf) {
        this(buf.readNbt());
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeNbt(tag);
    }

    @Override
    public void handle(@Nullable Player player) {
        if (player instanceof ServerPlayer) {
            CloudStorage.of(player).loadSettings(tag);
        }
    }
}
