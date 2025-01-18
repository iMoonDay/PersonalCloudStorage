package com.imoonday.personalcloudstorage.event;

import com.imoonday.personalcloudstorage.config.ServerConfig;
import com.imoonday.personalcloudstorage.core.CloudStorage;
import com.imoonday.personalcloudstorage.network.SyncConfigS2CPacket;
import com.imoonday.personalcloudstorage.platform.Services;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class EventHandler {

    public static void syncToClient(Player player) {
        CloudStorage.of(player).syncToClient(player);
        if (player instanceof ServerPlayer serverPlayer) {
            Services.PLATFORM.sendToPlayer(serverPlayer, new SyncConfigS2CPacket(ServerConfig.get().save(new CompoundTag())));
        }
    }

    public static void loadConfig() {
        ServerConfig.load();
    }

    public static void onPlayerJoin(Player player) {
        syncToClient(player);
        CloudStorage.of(player).syncSettings(player);
    }

    public static void onPlayerTick(Player player) {
        CloudStorage.of(player).tick(player);
    }

    public static boolean onAddToInventoryFailed(Player player, ItemStack stack) {
        CloudStorage cloudStorage = CloudStorage.of(player);
        if (cloudStorage.getSettings().autoUpload) {
            int originalCount = stack.getCount();
            ItemStack added = cloudStorage.addItem(stack);
            return added.isEmpty() || added.getCount() < originalCount;
        }
        return false;
    }
}
