package com.imoonday.personalcloudstorage.client;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class KeyBinding {

    private final KeyMapping keyMapping;
    @Nullable
    private final Consumer<Minecraft> onPress;

    public KeyBinding(String name, int key, String category) {
        this(name, key, category, null);
    }

    public KeyBinding(String name, int key, String category, @Nullable Consumer<Minecraft> onPress) {
        this.keyMapping = new KeyMapping(name, key, category);
        this.onPress = onPress;
    }

    public void onPress(Minecraft mc) {
        if (onPress != null) {
            onPress.accept(mc);
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
}
