package com.imoonday.personalcloudstorage.network;

import com.imoonday.personalcloudstorage.core.CloudStorage;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
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
        CloudStorage.of(player).loadSettings(tag);
    }
}
