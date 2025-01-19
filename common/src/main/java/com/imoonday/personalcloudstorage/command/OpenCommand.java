package com.imoonday.personalcloudstorage.command;

import com.imoonday.personalcloudstorage.core.CloudStorage;
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

public class OpenCommand {

    public static LiteralArgumentBuilder<CommandSourceStack> builder() {
        return literal("open").executes(OpenCommand::openOwnCloudStorage)
                              .then(argument("uuid_or_name", StringArgumentType.string())
                                            .suggests(CommandHandler::suggestNameAndUUID)
                                            .executes(OpenCommand::openWithUUIDOrName))
                              .then(argument("player", EntityArgument.player())
                                            .executes(OpenCommand::openOtherCloudStorage));
    }

    private static int openWithUUIDOrName(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = context.getSource().getPlayer();
        if (player == null) return 0;
        String input = StringArgumentType.getString(context, "uuid_or_name");
        CloudStorage cloudStorage = CommandHandler.findCloudStorage(context, input);

        if (cloudStorage != null) {
            cloudStorage.openMenu(player);
            return 1;
        }

        context.getSource().sendFailure(Component.translatable("message.personalcloudstorage.not_found", input));
        return 0;
    }

    private static int openOtherCloudStorage(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayer();
        if (player != null) {
            ServerPlayer targetPlayer = EntityArgument.getPlayer(context, "player");
            CloudStorage.of(targetPlayer).openMenu(player);
            return 1;
        }
        return 0;
    }

    private static int openOwnCloudStorage(CommandContext<CommandSourceStack> context) {
        ServerPlayer player = context.getSource().getPlayer();
        if (player != null) {
            CloudStorage.of(player).openMenu(player);
            return 1;
        }
        return 0;
    }
}
