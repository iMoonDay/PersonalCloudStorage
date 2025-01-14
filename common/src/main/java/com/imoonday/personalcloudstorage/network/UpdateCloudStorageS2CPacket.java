package com.imoonday.personalcloudstorage.network;

import com.imoonday.personalcloudstorage.client.screen.menu.CloudStorageMenu;
import com.imoonday.personalcloudstorage.component.CloudStorage;
import com.imoonday.personalcloudstorage.component.PagedList;
import com.imoonday.personalcloudstorage.component.PagedSlot;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public record UpdateCloudStorageS2CPacket(Type type, CompoundTag tag) implements NetworkPacket {

    public UpdateCloudStorageS2CPacket(FriendlyByteBuf buf) {
        this(buf.readEnum(Type.class), buf.readNbt());
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeEnum(type);
        buf.writeNbt(tag);
    }

    @Override
    public void handle(@Nullable Player player) {
        if (!(player instanceof ServerPlayer)) {
            CloudStorage cloudStorage = CloudStorage.of(player);
            switch (type) {
                case SLOT -> cloudStorage.update(PagedSlot.fromTag(tag));
                case PAGE -> cloudStorage.update(PagedList.fromTag(tag));
                case ALL -> {
                    CloudStorage storage = CloudStorage.fromTag(tag);
                    if (storage != null) {
                        cloudStorage.update(storage);
                    }
                }
            }
            CloudStorageMenu.refresh(player);
        }
    }

    public enum Type {
        SLOT, PAGE, ALL
    }
}
