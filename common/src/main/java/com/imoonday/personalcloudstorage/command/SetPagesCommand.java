package com.imoonday.personalcloudstorage.command;

import com.imoonday.personalcloudstorage.config.ServerConfig;
import com.imoonday.personalcloudstorage.core.CloudStorage;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class SetPagesCommand {

    public static LiteralArgumentBuilder<CommandSourceStack> builder() {
        return literal("pages")
                .then(argument("uuid_or_name", StringArgumentType.string())
                              .suggests(CommandHandler::suggestNameAndUUID)
                              .then(argument("pages", IntegerArgumentType.integer(1, ServerConfig.MAX_PAGES))
                                            .executes(SetPagesCommand::setPagesWithUUIDOrName)))
                .then(argument("player", EntityArgument.player())
                              .then(argument("pages", IntegerArgumentType.integer(1, ServerConfig.MAX_PAGES))
                                            .executes(SetPagesCommand::setPagesWithPlayer)));
    }

    private static int setPagesWithUUIDOrName(CommandContext<CommandSourceStack> context) {
        String input = StringArgumentType.getString(context, "uuid_or_name");
        int pages = IntegerArgumentType.getInteger(context, "pages");
        CloudStorage cloudStorage = CommandHandler.findCloudStorage(context, input);

        if (cloudStorage != null) {
            cloudStorage.updateTotalPages(pages);
            ServerPlayer onlinePlayer = cloudStorage.findOnlinePlayer(context.getSource().getServer());
            if (onlinePlayer != null) {
                cloudStorage.syncToClient(onlinePlayer);
            }
            sendSuccess(context, cloudStorage, onlinePlayer);
            return 1;
        }

        context.getSource().sendFailure(Component.translatable("message.personalcloudstorage.not_found", input));
        return 0;
    }

    private static void sendSuccess(CommandContext<CommandSourceStack> context, CloudStorage cloudStorage, @Nullable Player player) {
        int totalPages = cloudStorage.getTotalPages();
        Component component;
        Component playerName = cloudStorage.getPlayerName();
        UUID playerUUID = cloudStorage.getPlayerUUID();
        ServerPlayer sourcePlayer = context.getSource().getPlayer();
        if (sourcePlayer != null && sourcePlayer.getUUID().equals(playerUUID)) {
            component = Component.translatable("message.personalcloudstorage.set_pages", totalPages);
        } else if (player != null || playerName != null) {
            component = Component.translatable("message.personalcloudstorage.set_pages_with_name", player != null ? player.getName() : playerName, totalPages);
        } else {
            component = Component.translatable("message.personalcloudstorage.set_pages_with_uuid", playerUUID, totalPages);
        }
        context.getSource().sendSuccess(() -> component, true);
    }

    private static int setPagesWithPlayer(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer targetPlayer = EntityArgument.getPlayer(context, "player");
        int pages = IntegerArgumentType.getInteger(context, "pages");
        CloudStorage cloudStorage = CloudStorage.of(targetPlayer);
        cloudStorage.updateTotalPages(pages);
        cloudStorage.syncToClient(targetPlayer);
        sendSuccess(context, cloudStorage, targetPlayer);
        return 1;
    }
}
