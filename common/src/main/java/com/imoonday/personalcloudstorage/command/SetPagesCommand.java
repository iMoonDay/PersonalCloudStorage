package com.imoonday.personalcloudstorage.command;

import com.imoonday.personalcloudstorage.component.CloudStorage;
import com.imoonday.personalcloudstorage.config.ServerConfig;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public class SetPagesCommand {

    public static LiteralArgumentBuilder<CommandSourceStack> builder() {
        return literal("pages")
                .then(argument("uuid_or_name", StringArgumentType.string())
                              .suggests(CommandHandler::suggestNameAndUUID)
                              .then(argument("pages", IntegerArgumentType.integer(1, ServerConfig.DEFAULT_MAX_PAGES))
                                            .executes(SetPagesCommand::setPagesWithUUIDOrName)))
                .then(argument("player", EntityArgument.player())
                              .then(argument("pages", IntegerArgumentType.integer(1, ServerConfig.DEFAULT_MAX_PAGES))
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
            sendSuccess(context, cloudStorage);
            return 1;
        }

        context.getSource().sendFailure(Component.translatable("message.personalcloudstorage.not_found", input));
        return 0;
    }

    private static int setPagesWithPlayer(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer targetPlayer = EntityArgument.getPlayer(context, "player");
        int pages = IntegerArgumentType.getInteger(context, "pages");
        CloudStorage cloudStorage = CloudStorage.of(targetPlayer);
        cloudStorage.updateTotalPages(pages);
        cloudStorage.syncToClient(targetPlayer);
        sendSuccess(context, cloudStorage);
        return 1;
    }

    private static void sendSuccess(CommandContext<CommandSourceStack> context, CloudStorage cloudStorage) {
        int totalPages = cloudStorage.getTotalPages();
        context.getSource().sendSuccess(() -> Component.translatable("message.personalcloudstorage.set_pages", totalPages), true);
    }
}
