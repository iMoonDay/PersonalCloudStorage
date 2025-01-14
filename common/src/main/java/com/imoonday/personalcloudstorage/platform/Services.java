package com.imoonday.personalcloudstorage.platform;

import com.imoonday.personalcloudstorage.PersonalCloudStorage;
import com.imoonday.personalcloudstorage.platform.services.IPlatformHelper;

import java.util.ServiceLoader;

public class Services {

    public static final IPlatformHelper PLATFORM = load(IPlatformHelper.class);

    public static <T> T load(Class<T> clazz) {
        final T service = ServiceLoader.load(clazz)
                                       .findFirst()
                                       .orElseThrow(() -> new NullPointerException("Failed to load service for " + clazz.getName()));
        PersonalCloudStorage.LOGGER.debug("Loaded {} for service {}", service, clazz);
        return service;
    }
}
