package com.imoonday.personalcloudstorage.client;

import com.imoonday.personalcloudstorage.component.CloudStorage;
import com.imoonday.personalcloudstorage.component.PagedList;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class ClientCloudStorage extends CloudStorage {

    private static ClientCloudStorage INSTANCE;
    private boolean synced;

    protected ClientCloudStorage(UUID playerUUID) {
        super(playerUUID);
    }

    @NotNull
    public static ClientCloudStorage getOrCreate(UUID playerUUID) {
        if (INSTANCE == null) {
            INSTANCE = new ClientCloudStorage(playerUUID);
        }
        return INSTANCE;
    }

    public void updateClient(UUID playerUUID, int pageSize, int totalPages) {
        this.playerUUID = playerUUID;
        this.pageSize = pageSize;
        this.totalPages = totalPages;
        this.pages.clear();
        for (int i = 0; i < totalPages; i++) {
            this.pages.add(PagedList.create(i, pageSize));
        }
        synced = true;
    }

    @Override
    public boolean isSynced() {
        return synced;
    }

    @Override
    public void syncToClient(Player player) {

    }
}
