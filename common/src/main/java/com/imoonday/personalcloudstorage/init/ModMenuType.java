package com.imoonday.personalcloudstorage.init;

import com.imoonday.personalcloudstorage.client.screen.menu.CloudStorageMenu;
import com.imoonday.personalcloudstorage.platform.Services;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;

import java.util.function.Supplier;

public class ModMenuType {

    private static <T extends AbstractContainerMenu> Supplier<MenuType<T>> register(String name, MenuSupplier<T> supplier) {
        return Services.PLATFORM.registerMenu(name, supplier);
    }

    public static void init() {

    }

    public interface MenuSupplier<T extends AbstractContainerMenu> {

        T create(int i, Inventory inventory);
    }    public static final Supplier<MenuType<CloudStorageMenu>> CLOUD_STORAGE = register("cloud_storage", CloudStorageMenu::new);




}
