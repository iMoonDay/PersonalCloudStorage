package com.imoonday.personalcloudstorage.client;

import com.imoonday.personalcloudstorage.network.OpenCloudStorageC2SRequest;
import com.imoonday.personalcloudstorage.platform.Services;
import net.minecraft.client.Minecraft;

public class ClientUtils {

    public static void openCloudStorage(Minecraft mc) {
        Services.PLATFORM.sendToServer(new OpenCloudStorageC2SRequest(0));
    }
}
