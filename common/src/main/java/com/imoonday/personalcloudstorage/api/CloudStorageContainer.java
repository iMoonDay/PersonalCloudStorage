package com.imoonday.personalcloudstorage.api;

import com.imoonday.personalcloudstorage.component.CloudStorage;

public interface CloudStorageContainer {

    default CloudStorage getCloudStorage() {
        return null;
    }

    default void setCloudStorage(CloudStorage cloudStorage) {

    }
}
