package com.imoonday.personalcloudstorage.client;

import com.imoonday.personalcloudstorage.config.ServerConfig;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.gui.entries.IntegerListEntry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ModConfigScreenFactory {

    public static Screen create(Screen parent) {
        if (!PersonalCloudStorageClient.clothConfig) {
            return parent;
        }

        try {
            ClientConfig config = ClientConfig.get();
            ServerConfig serverConfig = ServerConfig.get();

            ConfigBuilder builder = ConfigBuilder.create()
                                                 .setParentScreen(parent)
                                                 .setTitle(Component.translatable("config.personalcloudstorage.title"))
                                                 .setSavingRunnable(() -> {
                                                     config.save();
                                                     serverConfig.save();
                                                 });
            builder.setGlobalized(true);
            builder.setGlobalizedExpanded(false);
            ConfigEntryBuilder entryBuilder = ConfigEntryBuilder.create();

            ConfigCategory clientCategory = builder.getOrCreateCategory(Component.translatable("config.personalcloudstorage.category.client"));

            clientCategory.addEntry(entryBuilder.startBooleanToggle(Component.translatable("config.personalcloudstorage.hideButton"), config.hideButton)
                                                .setDefaultValue(false)
                                                .setSaveConsumer(newValue -> config.hideButton = newValue)
                                                .build());

            clientCategory.addEntry(entryBuilder.startIntField(Component.translatable("config.personalcloudstorage.buttonOffsetX"), config.buttonOffsetX)
                                                .setDefaultValue(0)
                                                .setSaveConsumer(newValue -> config.buttonOffsetX = newValue)
                                                .build());

            clientCategory.addEntry(entryBuilder.startIntField(Component.translatable("config.personalcloudstorage.buttonOffsetY"), config.buttonOffsetY)
                                                .setDefaultValue(0)
                                                .setSaveConsumer(newValue -> config.buttonOffsetY = newValue)
                                                .build());

            clientCategory.addEntry(entryBuilder.startBooleanToggle(Component.translatable("config.personalcloudstorage.hidePageTurnKeyName"), config.hidePageTurnKeyName)
                                                .setDefaultValue(false)
                                                .setSaveConsumer(newValue -> config.hidePageTurnKeyName = newValue)
                                                .build());

            clientCategory.addEntry(entryBuilder.startBooleanToggle(Component.translatable("config.personalcloudstorage.hidePageTurnButton"), config.hidePageTurnButton)
                                                .setDefaultValue(false)
                                                .setSaveConsumer(newValue -> config.hidePageTurnButton = newValue)
                                                .build());

            clientCategory.addEntry(entryBuilder.startIntField(Component.translatable("config.personalcloudstorage.pageModificationButtonOffsetX"), config.pageModificationButtonOffsetX)
                                                .setDefaultValue(0)
                                                .setSaveConsumer(newValue -> config.pageModificationButtonOffsetX = newValue)
                                                .build());
            clientCategory.addEntry(entryBuilder.startIntField(Component.translatable("config.personalcloudstorage.pageModificationButtonOffsetY"), config.pageModificationButtonOffsetY)
                                                .setDefaultValue(0)
                                                .setSaveConsumer(newValue -> config.pageModificationButtonOffsetY = newValue)
                                                .build());

            ConfigCategory serverCategory = builder.getOrCreateCategory(Component.translatable("config.personalcloudstorage.category.server"));

            serverCategory.addEntry(entryBuilder.startIntSlider(Component.translatable("config.personalcloudstorage.initialRows"), serverConfig.initialRows, 1, 6)
                                                .setDefaultValue(3)
                                                .setSaveConsumer(newValue -> serverConfig.initialRows = newValue)
                                                .build());

            IntegerListEntry maxPagesEntry = entryBuilder.startIntField(Component.translatable("config.personalcloudstorage.maxPages"), serverConfig.maxPages)
                                                         .setDefaultValue(ServerConfig.DEFAULT_MAX_PAGES)
                                                         .setMin(1)
                                                         .setSaveConsumer(newValue -> serverConfig.maxPages = newValue)
                                                         .build();

            serverCategory.addEntry(entryBuilder.startTextDescription(Component.translatable("config.personalcloudstorage.maxPages.tip").withStyle(ChatFormatting.RED)).setDisplayRequirement(() -> {
                Integer integer = maxPagesEntry.getValue();
                return integer != null && integer >= 100000;
            }).build());

            serverCategory.addEntry(maxPagesEntry);

            serverCategory.addEntry(entryBuilder.startBooleanToggle(Component.translatable("config.personalcloudstorage.modifyStorageOfOthers"), serverConfig.modifyStorageOfOthers)
                                                .setDefaultValue(true)
                                                .setSaveConsumer(newValue -> serverConfig.modifyStorageOfOthers = newValue)
                                                .build());

            return builder.build();
        } catch (Exception e) {
            return null;
        }
    }
}
