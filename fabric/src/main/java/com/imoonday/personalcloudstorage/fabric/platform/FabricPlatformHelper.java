package com.imoonday.personalcloudstorage.fabric.platform;

import com.imoonday.personalcloudstorage.PersonalCloudStorage;
import com.imoonday.personalcloudstorage.fabric.network.FabricNetworkHandler;
import com.imoonday.personalcloudstorage.init.ModMenuType;
import com.imoonday.personalcloudstorage.network.NetworkPacket;
import com.imoonday.personalcloudstorage.platform.services.IPlatformHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;

import java.nio.file.Path;
import java.util.List;
import java.util.function.Supplier;

public class FabricPlatformHelper implements IPlatformHelper {

    @Override
    public Path getConfigDir() {
        return FabricLoader.getInstance().getConfigDir();
    }

    @Override
    public <T extends AbstractContainerMenu> Supplier<MenuType<T>> registerMenu(String id, ModMenuType.MenuSupplier<T> supplier) {
        MenuType<T> type = Registry.register(BuiltInRegistries.MENU, PersonalCloudStorage.id(id), new MenuType<>(supplier::create, FeatureFlags.VANILLA_SET));
        return () -> type;
    }

    @Override
    public <P extends NetworkPacket> void sendToServer(P packet) {
        FabricNetworkHandler.sendToServer(packet);
    }

    @Override
    public <P extends NetworkPacket> void sendToPlayer(ServerPlayer player, P packet) {
        FabricNetworkHandler.sendToPlayer(player, packet);
    }

    @Override
    public <P extends NetworkPacket> void sendToAllPlayers(List<ServerPlayer> players, P packet) {
        FabricNetworkHandler.sendToAllPlayers(players, packet);
    }
}
