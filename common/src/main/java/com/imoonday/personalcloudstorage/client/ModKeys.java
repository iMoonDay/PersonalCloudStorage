package com.imoonday.personalcloudstorage.client;

import org.lwjgl.glfw.GLFW;

public class ModKeys {

    private static final String CATEGORY = "key.categories.personalcloudstorage";

    public static final KeyBinding OPEN_CLOUD_STORAGE_INVENTORY = new KeyBinding("key.personalcloudstorage.inventory", GLFW.GLFW_KEY_B, CATEGORY, mc -> ClientHandler.openCloudStorage());
    public static final KeyBinding PREVIOUS_PAGE = new KeyBinding("key.personalcloudstorage.previous_page", GLFW.GLFW_KEY_A, CATEGORY);
    public static final KeyBinding NEXT_PAGE = new KeyBinding("key.personalcloudstorage.next_page", GLFW.GLFW_KEY_D, CATEGORY);

    public static KeyBinding[] KEYS = {OPEN_CLOUD_STORAGE_INVENTORY, PREVIOUS_PAGE, NEXT_PAGE};
}