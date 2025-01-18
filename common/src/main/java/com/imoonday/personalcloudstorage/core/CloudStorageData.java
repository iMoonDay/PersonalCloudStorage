package com.imoonday.personalcloudstorage.core;

import com.google.common.collect.ImmutableMap;
import com.imoonday.personalcloudstorage.PersonalCloudStorage;
import com.imoonday.personalcloudstorage.config.ServerConfig;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class CloudStorageData extends SavedData {

    private final Map<UUID, CloudStorage> cloudStorages = new HashMap<>();

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag list = new ListTag();
        for (CloudStorage storage : cloudStorages.values()) {
            list.add(storage.save(new CompoundTag()));
        }
        tag.put("CloudStorages", list);
        return tag;
    }

    public Map<UUID, CloudStorage> getCloudStorages() {
        return ImmutableMap.copyOf(cloudStorages);
    }

    @Nullable
    public CloudStorage get(UUID playerUUID) {
        CloudStorage storage = cloudStorages.get(playerUUID);
        setDirty();
        return storage;
    }

    @Nullable
    public CloudStorage byName(String name) {
        for (CloudStorage storage : cloudStorages.values()) {
            Component playerName = storage.getPlayerName();
            if (playerName != null && playerName.getString().equals(name)) {
                return storage;
            }
        }
        return null;
    }

    @NotNull
    public static CloudStorage get(ServerPlayer player) {
        CloudStorage cloudStorage = get(Objects.requireNonNull(player.getServer())).getOrCreate(player.getUUID());
        cloudStorage.setPlayerNameIfAbsent(player.getName());
        return cloudStorage;
    }

    public static CloudStorageData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(CloudStorageData::fromTag, CloudStorageData::new, PersonalCloudStorage.MOD_ID);
    }

    public static CloudStorageData fromTag(CompoundTag tag) {
        CloudStorageData data = new CloudStorageData();
        for (Tag storageTag : tag.getList("CloudStorages", Tag.TAG_COMPOUND)) {
            CloudStorage storage = CloudStorage.fromTag((CompoundTag) storageTag);
            if (storage != null) {
                data.cloudStorages.put(storage.getPlayerUUID(), storage);
            }
        }
        return data;
    }

    @NotNull
    public CloudStorage getOrCreate(UUID playerUUID) {
        CloudStorage storage = cloudStorages.computeIfAbsent(playerUUID, uuid -> new CloudStorage(uuid, ServerConfig.get().initialRows));
        setDirty();
        return storage;
    }
}
