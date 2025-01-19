package com.imoonday.personalcloudstorage.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.imoonday.personalcloudstorage.PersonalCloudStorage;
import com.imoonday.personalcloudstorage.client.screen.widget.CloudStorageButton;
import com.imoonday.personalcloudstorage.platform.Services;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ClientConfig {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static ClientConfig instance;
    private static File configFile;

    public boolean hideButton;
    public int buttonOffsetX;
    public int buttonOffsetY;
    public CloudStorageButton.AdhesiveEdge buttonAdhesiveEdge = CloudStorageButton.AdhesiveEdge.BOTTOM;
    public boolean hidePageTurnKeyName;
    public boolean hidePageTurnButton;
    public int pageModificationButtonOffsetX;
    public int pageModificationButtonOffsetY;
    public int settingsComponentOffsetX;
    public int settingsComponentOffsetY;

    public static void load() {
        try {
            File file = getConfigDir();
            if (file.exists()) {
                String json = Files.readString(Paths.get(file.toURI()));
                ClientConfig config = fromJson(json);
                if (config != null) {
                    instance = config;
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

    public static ClientConfig get() {
        if (instance == null) {
            instance = new ClientConfig();
        }
        return instance;
    }

    private static File getConfigDir() {
        if (configFile == null) {
            configFile = Services.PLATFORM.getConfigDir().resolve(PersonalCloudStorage.MOD_ID + "-client.json").toFile();
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

    public static ClientConfig fromJson(String json) {
        return GSON.fromJson(json, ClientConfig.class);
    }
}
