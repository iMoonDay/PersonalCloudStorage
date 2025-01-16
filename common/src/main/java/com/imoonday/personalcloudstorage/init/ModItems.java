package com.imoonday.personalcloudstorage.init;

import com.imoonday.personalcloudstorage.item.CloudCoreItem;
import com.imoonday.personalcloudstorage.item.PartitionNodeItem;
import com.imoonday.personalcloudstorage.platform.Services;
import net.minecraft.world.item.Item;

import java.util.function.Supplier;

public class ModItems {

    public static final Supplier<CloudCoreItem> CLOUD_CORE = register("cloud_core", () -> new CloudCoreItem(new Item.Properties().stacksTo(1)));
    public static final Supplier<PartitionNodeItem> PARTITION_NODE = register("partition_node", () -> new PartitionNodeItem(new Item.Properties().stacksTo(1)));

    private static <T extends Item> Supplier<T> register(String name, Supplier<T> item) {
        return Services.PLATFORM.registerItem(name, item);
    }

    public static void init() {

    }
}
