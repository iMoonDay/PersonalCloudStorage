package com.imoonday.personalcloudstorage.forge.platform;

import com.imoonday.personalcloudstorage.forge.PersonalCloudStorageForge;
import com.imoonday.personalcloudstorage.forge.network.ForgeNetworkHandler;
import com.imoonday.personalcloudstorage.init.ModMenuType;
import com.imoonday.personalcloudstorage.network.NetworkPacket;
import com.imoonday.personalcloudstorage.platform.services.IPlatformHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.fml.loading.FMLPaths;

import java.nio.file.Path;
import java.util.function.Supplier;

public class ForgePlatformHelper implements IPlatformHelper {

    @Override
    public Path getConfigDir() {
        return FMLPaths.CONFIGDIR.get();
    }

    @Override
    public <T extends AbstractContainerMenu> Supplier<MenuType<T>> registerMenu(String id, ModMenuType.MenuSupplier<T> supplier) {
        return PersonalCloudStorageForge.MENU_TYPES.register(id, () -> new MenuType<>(supplier::create, FeatureFlags.VANILLA_SET));
    }

    @Override
    public <P extends NetworkPacket> void sendToServer(P packet) {
        ForgeNetworkHandler.sendToServer(packet);
    }

    @Override
    public <P extends NetworkPacket> void sendToPlayer(ServerPlayer player, P packet) {
        ForgeNetworkHandler.sendToPlayer(player, packet);
    }
}
