package com.imoonday.personalcloudstorage.network;

import com.imoonday.personalcloudstorage.config.ServerConfig;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public record SyncConfigS2CPacket(CompoundTag tag) implements NetworkPacket {

    public SyncConfigS2CPacket(FriendlyByteBuf buf) {
        this(buf.readNbt());
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeNbt(tag);
    }

    @Override
    public void handle(@Nullable Player player) {
        if (player != null && player.level().isClientSide) {
            ServerConfig.getClientCache().load(tag);
        }
    }
}
