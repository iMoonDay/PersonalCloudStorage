package com.imoonday.personalcloudstorage.client;

import org.lwjgl.glfw.GLFW;

public class ModKeys {

    public static final KeyBinding OPEN_CLOUD_STORAGE_INVENTORY = new KeyBinding("key.personalcloudstorage.inventory", GLFW.GLFW_KEY_O, "key.categories.inventory", ClientUtils::openCloudStorage);

    public static KeyBinding[] KEYS = {OPEN_CLOUD_STORAGE_INVENTORY};
}