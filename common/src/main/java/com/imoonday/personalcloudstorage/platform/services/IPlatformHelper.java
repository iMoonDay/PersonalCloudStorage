package com.imoonday.personalcloudstorage.platform.services;

import com.imoonday.personalcloudstorage.init.ModMenuType;
import com.imoonday.personalcloudstorage.network.NetworkPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;

import java.nio.file.Path;
import java.util.List;
import java.util.function.Supplier;

public interface IPlatformHelper {

    Path getConfigDir();

    boolean isDevelopmentEnvironment();

    boolean isModLoaded(String modId);

    <T extends Item> Supplier<T> registerItem(String id, Supplier<T> supplier);

    <T extends AbstractContainerMenu> Supplier<MenuType<T>> registerMenu(String id, ModMenuType.MenuSupplier<T> supplier);

    <P extends NetworkPacket> void sendToServer(P packet);

    <P extends NetworkPacket> void sendToPlayer(ServerPlayer player, P packet);

    default <P extends NetworkPacket> void sendToAllPlayers(List<ServerPlayer> players, P packet) {
        for (ServerPlayer player : players) {
            sendToPlayer(player, packet);
        }
    }
}
