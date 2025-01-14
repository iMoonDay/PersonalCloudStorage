package com.imoonday.personalcloudstorage.network;

import com.imoonday.personalcloudstorage.client.screen.menu.CloudStorageMenu;
import com.imoonday.personalcloudstorage.component.CloudStorage;
import com.imoonday.personalcloudstorage.component.PagedList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
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
            CloudStorage cloudStorage = CloudStorage.of(player);
            int actualPage = getActualPage(player, cloudStorage);
            if (actualPage < 0 || !cloudStorage.hasPage(actualPage)) return;
            serverPlayer.openMenu(new SimpleMenuProvider((i, inventory, player1) -> new CloudStorageMenu(i, inventory, actualPage), Component.literal("Cloud Storage - Page " + (actualPage + 1))));
        }
    }

    private int getActualPage(@NotNull Player player, CloudStorage cloudStorage) {
        if (page >= 0) {
            return page;
        }
        if (player.containerMenu instanceof CloudStorageMenu menu) {
            PagedList pagedList = menu.getPage();
            int totalPages = cloudStorage.getTotalPages();
            int currentPage = pagedList.getPage();
            if (page == -2) {
                return (currentPage + totalPages - 1) % totalPages;
            } else if (page == -1) {
                return (currentPage + 1) % totalPages;
            }
        }
        return page;
    }
}
