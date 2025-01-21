package com.imoonday.personalcloudstorage.client;

import com.imoonday.personalcloudstorage.platform.Services;

public final class PersonalCloudStorageClient {

    public static boolean clothConfig = Services.PLATFORM.isModLoaded("cloth-config") || Services.PLATFORM.isModLoaded("cloth_config");
    public static boolean inventoryProfilesNext = Services.PLATFORM.isModLoaded("inventoryprofilesnext");

    public static void initClient() {
        ClientConfig.load();
    }
}
