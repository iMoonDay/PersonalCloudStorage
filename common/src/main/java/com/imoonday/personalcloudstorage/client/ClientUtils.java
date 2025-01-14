package com.imoonday.personalcloudstorage.client;

import com.imoonday.personalcloudstorage.network.OpenCloudStorageC2SRequest;
import com.imoonday.personalcloudstorage.platform.Services;
import net.minecraft.client.Minecraft;

public class ClientUtils {

    public static boolean switchingPage;

    public static void openCloudStorage(Minecraft mc) {
        Services.PLATFORM.sendToServer(new OpenCloudStorageC2SRequest(0));
    }

    public static void openPreviousPage() {
        Services.PLATFORM.sendToServer(new OpenCloudStorageC2SRequest(-2));
        switchingPage = true;
    }

    public static void openNextPage() {
        Services.PLATFORM.sendToServer(new OpenCloudStorageC2SRequest(-1));
        switchingPage = true;
    }
}
