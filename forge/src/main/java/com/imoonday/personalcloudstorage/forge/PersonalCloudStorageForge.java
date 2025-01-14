package com.imoonday.personalcloudstorage.forge;

import com.imoonday.personalcloudstorage.PersonalCloudStorage;
import com.imoonday.personalcloudstorage.forge.client.ClientEventHandler;
import com.imoonday.personalcloudstorage.forge.network.ForgeNetworkHandler;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.api.distmarker.Dist;
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
        MENU_TYPES.register(FMLJavaModLoadingContext.get().getModEventBus());
        DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> ClientEventHandler::init);
    }
}
