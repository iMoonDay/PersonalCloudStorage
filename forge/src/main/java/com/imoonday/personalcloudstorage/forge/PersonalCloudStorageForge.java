package com.imoonday.personalcloudstorage.forge;

import com.imoonday.personalcloudstorage.PersonalCloudStorage;
import com.imoonday.personalcloudstorage.event.EventHandler;
import com.imoonday.personalcloudstorage.forge.client.ClientEventHandler;
import com.imoonday.personalcloudstorage.forge.network.ForgeNetworkHandler;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

@Mod(PersonalCloudStorage.MOD_ID)
public final class PersonalCloudStorageForge {

    public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(ForgeRegistries.MENU_TYPES, PersonalCloudStorage.MOD_ID);

    public PersonalCloudStorageForge() {
        PersonalCloudStorage.init();
        ForgeNetworkHandler.init();
        MinecraftForge.EVENT_BUS.register(this);
        MENU_TYPES.register(FMLJavaModLoadingContext.get().getModEventBus());
        DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> ClientEventHandler::init);
    }

    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();
        if (player instanceof ServerPlayer serverPlayer) {
            EventHandler.onPlayerJoin(serverPlayer);
        }
    }

    @SubscribeEvent
    public void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        Player player = event.getEntity();
        if (player instanceof ServerPlayer serverPlayer) {
            EventHandler.onPlayerJoin(serverPlayer);
        }
    }

    @SubscribeEvent
    public void onPlayerClone(PlayerEvent.Clone event) {
        Player oldPlayer = event.getOriginal();
        Player newPlayer = event.getEntity();
        if (oldPlayer instanceof ServerPlayer oldServerPlayer && newPlayer instanceof ServerPlayer newServerPlayer) {
            EventHandler.onPlayerCopy(oldServerPlayer, newServerPlayer);
        }
    }
}
