package com.imoonday.personalcloudstorage.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.imoonday.personalcloudstorage.PersonalCloudStorage;
import com.imoonday.personalcloudstorage.platform.Services;
import com.mojang.logging.LogUtils;
import net.minecraft.nbt.CompoundTag;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ServerConfig {

    public static final int DEFAULT_MAX_PAGES = 999;
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static ServerConfig instance;
    private static ServerConfig clientCache;
    private static File configFile;

    public int initialRows = 3;
    public int maxPages = DEFAULT_MAX_PAGES;
    public boolean modifyStorageOfOthers = true;

    public CompoundTag save(CompoundTag tag) {
        tag.putInt("initialRows", initialRows);
        tag.putInt("maxPages", maxPages);
        tag.putBoolean("modifyStorageOfOthers", modifyStorageOfOthers);
        return tag;
    }

    public void load(CompoundTag tag) {
        if (tag.contains("initialRows")) {
            initialRows = tag.getInt("initialRows");
        }
        if (tag.contains("maxPages")) {
            maxPages = tag.getInt("maxPages");
        }
        if (tag.contains("modifyStorageOfOthers")) {
            modifyStorageOfOthers = tag.getBoolean("modifyStorageOfOthers");
        }
    }

    public void reset() {
        initialRows = 3;
        maxPages = DEFAULT_MAX_PAGES;
        modifyStorageOfOthers = true;
    }

    public static ServerConfig get(boolean isClient) {
        return isClient ? getClientCache() : get();
    }

    public static ServerConfig get() {
        if (instance == null) {
            instance = new ServerConfig();
        }
        return instance;
    }

    public static ServerConfig getClientCache() {
        if (clientCache == null) {
            clientCache = new ServerConfig();
        }
        return clientCache;
    }

    public static void load() {
        LOGGER.info("Loading server config");
        try {
            File file = getConfigDir();
            if (file.exists()) {
                String json = Files.readString(Paths.get(file.toURI()));
                ServerConfig config = fromJson(json);
                if (config != null) {
                    instance = config;
                    LOGGER.info("Server config loaded");
                } else {
                    LOGGER.warn("Failed to parse config file, saving current config");
                    get().save();
                }
            } else {
                LOGGER.warn("Config file does not exist, creating new one");
                get().save();
            }
        } catch (Exception e) {
            LOGGER.error("Failed to read from config file", e);
        }
    }

    private static File getConfigDir() {
        if (configFile == null) {
            configFile = Services.PLATFORM.getConfigDir().resolve(PersonalCloudStorage.MOD_ID + "-server.json").toFile();
        }
        return configFile;
    }

    public void save() {
        File file = getConfigDir();
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(toJson());
        } catch (Exception e) {
            LOGGER.error("Failed to write to config file", e);
        }
    }

    public String toJson() {
        return GSON.toJson(this);
    }

    public static ServerConfig fromJson(String json) {
        return GSON.fromJson(json, ServerConfig.class);
    }
}
