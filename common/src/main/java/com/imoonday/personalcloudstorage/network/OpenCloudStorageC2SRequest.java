package com.imoonday.personalcloudstorage.network;

import com.imoonday.personalcloudstorage.client.screen.menu.CloudStorageMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public record OpenCloudStorageC2SRequest(int page) implements NetworkPacket {

    public OpenCloudStorageC2SRequest(FriendlyByteBuf buf) {
        this(buf.readInt());
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeInt(page);
    }

    @Override
    public void handle(@Nullable Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.openMenu(new SimpleMenuProvider((i, inventory, player1) -> new CloudStorageMenu(i, inventory), Component.literal("Cloud Storage")));
        }
    }
}
