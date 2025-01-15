package com.imoonday.personalcloudstorage.client;

import com.imoonday.personalcloudstorage.network.OpenCloudStorageC2SRequest;
import com.imoonday.personalcloudstorage.network.PageC2SRequest;
import com.imoonday.personalcloudstorage.network.RequestUpdateC2SRequest;
import com.imoonday.personalcloudstorage.platform.Services;
import net.minecraft.client.Minecraft;

public class ClientUtils {

    public static void openCloudStorage(Minecraft mc) {
        Services.PLATFORM.sendToServer(new OpenCloudStorageC2SRequest(0));
    }

    public static void nextPage() {
        Services.PLATFORM.sendToServer(new PageC2SRequest(PageC2SRequest.Direction.NEXT));
    }

    public static void previousPage() {
        Services.PLATFORM.sendToServer(new PageC2SRequest(PageC2SRequest.Direction.PREVIOUS));
    }

    public static void requestUpdate() {
        Services.PLATFORM.sendToServer(new RequestUpdateC2SRequest());
    }
}
