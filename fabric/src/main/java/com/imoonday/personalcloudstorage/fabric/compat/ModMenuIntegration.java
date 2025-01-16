package com.imoonday.personalcloudstorage.fabric.compat;

import com.imoonday.personalcloudstorage.client.ModConfigScreenFactory;
import com.imoonday.personalcloudstorage.client.PersonalCloudStorageClient;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

public class ModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> PersonalCloudStorageClient.clothConfig ? ModConfigScreenFactory.create(parent) : null;
    }
}
