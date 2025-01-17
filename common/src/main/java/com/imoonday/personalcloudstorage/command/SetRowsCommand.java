package com.imoonday.personalcloudstorage.command;

import com.imoonday.personalcloudstorage.component.CloudStorage;
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

public class SetRowsCommand {

    public static LiteralArgumentBuilder<CommandSourceStack> builder() {
        return literal("rows")
                .then(argument("uuid_or_name", StringArgumentType.string())
                              .suggests(CommandHandler::suggestNameAndUUID)
                              .then(argument("rows", IntegerArgumentType.integer(1, 6))
                                            .executes(SetRowsCommand::setRowsWithUUIDOrName)))
                .then(argument("player", EntityArgument.player())
                              .then(argument("rows", IntegerArgumentType.integer(1, 6))
                                            .executes(SetRowsCommand::setRowsWithPlayer)));
    }

    private static int setRowsWithUUIDOrName(CommandContext<CommandSourceStack> context) {
        String input = StringArgumentType.getString(context, "uuid_or_name");
        int rows = IntegerArgumentType.getInteger(context, "rows");
        CloudStorage cloudStorage = CommandHandler.findCloudStorage(context, input);

        if (cloudStorage != null) {
            cloudStorage.updatePageSize(rows);
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

    private static int setRowsWithPlayer(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer targetPlayer = EntityArgument.getPlayer(context, "player");
        int rows = IntegerArgumentType.getInteger(context, "rows");
        CloudStorage cloudStorage = CloudStorage.of(targetPlayer);
        cloudStorage.updatePageSize(rows);
        cloudStorage.syncToClient(targetPlayer);
        sendSuccess(context, cloudStorage);
        return 1;
    }

    private static void sendSuccess(CommandContext<CommandSourceStack> context, CloudStorage cloudStorage) {
        int rows = cloudStorage.getPageRows();
        context.getSource().sendSuccess(() -> Component.translatable("message.personalcloudstorage.set_rows", rows), true);
    }
}
