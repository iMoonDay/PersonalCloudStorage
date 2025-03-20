package com.imoonday.personalcloudstorage.network;

import com.imoonday.personalcloudstorage.client.ClientHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public record SyncCurrentPageS2CPacket(int page) implements NetworkPacket {

    public SyncCurrentPageS2CPacket(FriendlyByteBuf buf) {
        this(buf.readInt());
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeInt(page);
    }

    @Override
    public void handle(@Nullable Player player) {
        if (player != null && player.level().isClientSide) {
            ClientHandler.updateCurrentPage(page);
        }
    }
}
