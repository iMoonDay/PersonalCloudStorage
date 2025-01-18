package com.imoonday.personalcloudstorage.core;

import net.minecraft.nbt.CompoundTag;

public class CloudStorageSettings {

    public boolean autoDownload;
    public boolean autoUpload;
    public boolean cycleThroughPages;

    public CompoundTag save(CompoundTag tag) {
        tag.putBoolean("autoDownload", autoDownload);
        tag.putBoolean("autoUpload", autoUpload);
        tag.putBoolean("cycleThroughPages", cycleThroughPages);
        return tag;
    }

    public void load(CompoundTag tag) {
        if (tag.contains("autoDownload")) {
            autoDownload = tag.getBoolean("autoDownload");
        }
        if (tag.contains("autoUpload")) {
            autoUpload = tag.getBoolean("autoUpload");
        }
        if (tag.contains("cycleThroughPages")) {
            cycleThroughPages = tag.getBoolean("cycleThroughPages");
        }
    }

    @Override
    public String toString() {
        return "CloudStorageSettings{" +
               "autoDownload=" + autoDownload +
               ", autoUpload=" + autoUpload +
               ", cycleThroughPages=" + cycleThroughPages +
               '}';
    }
}
