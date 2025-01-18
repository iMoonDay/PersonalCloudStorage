package com.imoonday.personalcloudstorage.client;

import com.imoonday.personalcloudstorage.core.CloudStorage;
import com.imoonday.personalcloudstorage.network.SyncSettingsPacket;
import com.imoonday.personalcloudstorage.platform.Services;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class ClientCloudStorage extends CloudStorage {

    private static ClientCloudStorage instance;
    private boolean synced;

    protected ClientCloudStorage(UUID playerUUID) {
        super(playerUUID);
    }

    public void updateClient(UUID playerUUID, int pageSize, int totalPages) {
        this.playerUUID = playerUUID;
        this.pageSize = pageSize;
        this.forEach(page -> page.setSize(this.pageSize), false);
        this.totalPages = totalPages;
        synced = true;
    }

    public void syncSettings() {
        syncSettings(null);
    }

    @Override
    public void syncSettings(@Nullable Player player) {
        Services.PLATFORM.sendToServer(new SyncSettingsPacket(this.settings.save(new CompoundTag())));
    }

    @Override
    public boolean isSynced() {
        return synced;
    }

    @Override
    public void syncToClient(Player player) {

    }

    @Override
    public UUID getPlayerUUID() {
        UUID uuid = super.getPlayerUUID();
        if (uuid == null) {
            uuid = ClientHandler.getOfflinePlayerUUID();
        }
        return uuid;
    }

    @NotNull
    public static ClientCloudStorage getOrCreate(UUID playerUUID) {
        if (instance == null) {
            instance = new ClientCloudStorage(playerUUID);
        }
        return instance;
    }

    @NotNull
    public static ClientCloudStorage get() {
        if (instance == null) {
            instance = new ClientCloudStorage(ClientHandler.getOfflinePlayerUUID());
        }
        return instance;
    }
}
