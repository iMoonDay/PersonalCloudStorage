package com.imoonday.personalcloudstorage.client;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

import java.util.function.Consumer;

public class KeyBinding {

    private final KeyMapping keyMapping;
    private final Consumer<Minecraft> onPress;

    public KeyBinding(String name, int key, String category, Consumer<Minecraft> onPress) {
        this.keyMapping = new KeyMapping(name, key, category);
        this.onPress = onPress;
    }

    public void onPress(Minecraft mc) {
        onPress.accept(mc);
    }

    public KeyMapping getKeyMapping() {
        return keyMapping;
    }
}
