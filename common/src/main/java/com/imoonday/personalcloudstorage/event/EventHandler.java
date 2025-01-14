package com.imoonday.personalcloudstorage.event;

import com.imoonday.personalcloudstorage.component.CloudStorage;
import net.minecraft.server.level.ServerPlayer;

public class EventHandler {

    public static void onPlayerTickEnd(ServerPlayer player) {
        CloudStorage.of(player).sync(player);
    }
}
