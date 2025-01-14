package com.imoonday.personalcloudstorage.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public interface NetworkPacket {

    void write(FriendlyByteBuf buf);

    void handle(@Nullable Player player);
}
