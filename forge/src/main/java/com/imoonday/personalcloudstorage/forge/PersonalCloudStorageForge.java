package com.imoonday.personalcloudstorage.forge;

import com.imoonday.personalcloudstorage.PersonalCloudStorage;
import com.imoonday.personalcloudstorage.command.CommandHandler;
import com.imoonday.personalcloudstorage.event.EventHandler;
import com.imoonday.personalcloudstorage.forge.client.ClientEventHandler;
import com.imoonday.personalcloudstorage.forge.network.ForgeNetworkHandler;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

@SuppressWarnings("removal")
@Mod(PersonalCloudStorage.MOD_ID)
public final class PersonalCloudStorageForge {

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, PersonalCloudStorage.MOD_ID);
    public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(ForgeRegistries.MENU_TYPES, PersonalCloudStorage.MOD_ID);

    public PersonalCloudStorageForge() {
        PersonalCloudStorage.init();
        ForgeNetworkHandler.init();

        MinecraftForge.EVENT_BUS.register(this);

        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ITEMS.register(eventBus);
        MENU_TYPES.register(eventBus);

        if (FMLEnvironment.dist == Dist.CLIENT) {
            ClientEventHandler.init();
        }
    }

    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        EventHandler.onPlayerJoin(event.getEntity());
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        CommandHandler.registerCommands(event.getDispatcher(), event.getBuildContext(), event.getCommandSelection());
    }

    @SubscribeEvent
    public void onServerStating(ServerStartingEvent event) {
        EventHandler.loadConfig();
    }
}
