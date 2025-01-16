package com.imoonday.personalcloudstorage.client;

import com.imoonday.personalcloudstorage.platform.Services;

public final class PersonalCloudStorageClient {

    public static boolean modMenu = Services.PLATFORM.isModLoaded("modmenu");
    public static boolean clothConfig = Services.PLATFORM.isModLoaded("cloth-config") || Services.PLATFORM.isModLoaded("cloth_config");

    public static void initClient() {
        ClientConfig.load();
    }
}
