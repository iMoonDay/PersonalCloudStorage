package com.imoonday.personalcloudstorage;

import com.imoonday.personalcloudstorage.init.ModMenuType;
import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;

public final class PersonalCloudStorage {

    public static final String MOD_ID = "personalcloudstorage";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static void init() {
        ModMenuType.init();
    }

    public static ResourceLocation id(String path) {
        return new ResourceLocation(MOD_ID, path);
    }
}
