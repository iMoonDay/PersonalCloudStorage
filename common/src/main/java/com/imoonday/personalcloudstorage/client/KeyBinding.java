package com.imoonday.personalcloudstorage.client;

import net.minecraft.client.KeyMapping;
import org.jetbrains.annotations.Nullable;

public class KeyBinding {

    private final KeyMapping keyMapping;
    @Nullable
    private final Runnable onPress;

    public KeyBinding(String name, int key, String category) {
        this(name, key, category, null);
    }

    public KeyBinding(String name, int key, String category, @Nullable Runnable onPress) {
        this.keyMapping = new KeyMapping(name, key, category);
        this.onPress = onPress;
    }

    public void onPress() {
        if (onPress != null) {
            onPress.run();
        }
    }

    public boolean hasPressAction() {
        return onPress != null;
    }

    public KeyMapping getKeyMapping() {
        return keyMapping;
    }

    public boolean matches(int keysym, int scancode) {
        return keyMapping.matches(keysym, scancode);
    }

    public boolean matchesMouse(int key) {
        return keyMapping.matchesMouse(key);
    }

    public void tick() {
        if (hasPressAction()) {
            while (keyMapping.consumeClick()) {
                onPress();
            }
        }
    }
}
