package com.imoonday.personalcloudstorage.network;

import com.imoonday.personalcloudstorage.client.screen.menu.CloudStorageMenu;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public record PageC2SRequest(Direction direction) implements NetworkPacket {

    public PageC2SRequest(FriendlyByteBuf buf) {
        this(buf.readEnum(Direction.class));
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeEnum(direction);
    }

    @Override
    public void handle(@Nullable Player player) {
        if (player instanceof ServerPlayer && player.containerMenu instanceof CloudStorageMenu menu) {
            if (direction == Direction.PREVIOUS) {
                menu.previousPage();
            } else if (direction == Direction.NEXT) {
                menu.nextPage();
            }
        }
    }

    public enum Direction {
        PREVIOUS, NEXT
    }
}
