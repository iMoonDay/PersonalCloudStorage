package com.imoonday.personalcloudstorage.command;

import com.imoonday.personalcloudstorage.core.CloudStorage;
import com.imoonday.personalcloudstorage.core.CloudStorageData;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static net.minecraft.commands.Commands.literal;

public class CommandHandler {

    public static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext registryAccess, Commands.CommandSelection environment) {
        dispatcher.register(
                literal("cloudstorage")
                        .requires(source -> source.hasPermission(2))
                        .then(OpenCommand.builder())
                        .then(SetRowsCommand.builder())
                        .then(SetPagesCommand.builder())
                        .then(ReloadCommand.builder())
        );
    }

    public static CompletableFuture<Suggestions> suggestNameAndUUID(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        CloudStorageData data = CloudStorageData.get(context.getSource().getServer());
        Map<UUID, CloudStorage> cloudStorages = data.getCloudStorages();
        String remaining = builder.getRemaining();
        for (CloudStorage cloudStorage : cloudStorages.values()) {
            Component playerName = cloudStorage.getPlayerName();
            if (playerName != null) {
                String name = playerName.getString();
                if (remaining.isEmpty() || name.startsWith(remaining)) {
                    builder.suggest(name);
                }
            }
        }
        for (Map.Entry<UUID, CloudStorage> entry : cloudStorages.entrySet()) {
            UUID uuid = entry.getKey();
            CloudStorage cloudStorage = entry.getValue();
            String uuidString = uuid.toString();
            if (remaining.isEmpty() || uuidString.startsWith(remaining)) {
                builder.suggest(uuidString, cloudStorage.getPlayerName());
            }
        }
        return builder.buildFuture();
    }

    @Nullable
    public static CloudStorage findCloudStorage(CommandContext<CommandSourceStack> context, String input) {
        CloudStorageData data = CloudStorageData.get(context.getSource().getServer());

        CloudStorage cloudStorage = null;
        try {
            UUID uuid = UUID.fromString(input);
            cloudStorage = data.get(uuid);
        } catch (IllegalArgumentException ignored) {

        }

        if (cloudStorage == null) {
            cloudStorage = data.byName(input);
        }
        return cloudStorage;
    }
}
