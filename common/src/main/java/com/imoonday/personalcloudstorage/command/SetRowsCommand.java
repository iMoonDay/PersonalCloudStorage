package com.imoonday.personalcloudstorage.command;

import com.imoonday.personalcloudstorage.client.screen.menu.CloudStorageMenu;
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
            int originalPageSize = cloudStorage.getPageSize();
            cloudStorage.updatePageSize(rows);
            ServerPlayer onlinePlayer = cloudStorage.findOnlinePlayer(context.getSource().getServer());
            if (onlinePlayer != null) {
                if (originalPageSize != cloudStorage.getPageSize() && onlinePlayer.containerMenu instanceof CloudStorageMenu menu && menu.getCloudStorage() == cloudStorage) {
                    cloudStorage.openMenu(onlinePlayer);
                } else {
                    cloudStorage.syncToClient(onlinePlayer);
                }
            }
            sendSuccess(context, cloudStorage, onlinePlayer);
            return 1;
        }

        context.getSource().sendFailure(Component.translatable("message.personalcloudstorage.not_found", input));
        return 0;
    }

    private static void sendSuccess(CommandContext<CommandSourceStack> context, CloudStorage cloudStorage, @Nullable Player player) {
        int rows = cloudStorage.getPageRows();
        Component component;
        Component playerName = cloudStorage.getPlayerName();
        UUID playerUUID = cloudStorage.getPlayerUUID();
        ServerPlayer sourcePlayer = context.getSource().getPlayer();
        if (sourcePlayer != null && sourcePlayer.getUUID().equals(playerUUID)) {
            component = Component.translatable("message.personalcloudstorage.set_rows", rows);
        } else if (player != null || playerName != null) {
            component = Component.translatable("message.personalcloudstorage.set_rows_with_name", player != null ? player.getName() : playerName, rows);
        } else {
            component = Component.translatable("message.personalcloudstorage.set_rows_with_uuid", playerUUID, rows);
        }
        context.getSource().sendSuccess(() -> component, true);
    }

    private static int setRowsWithPlayer(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer targetPlayer = EntityArgument.getPlayer(context, "player");
        int rows = IntegerArgumentType.getInteger(context, "rows");
        CloudStorage cloudStorage = CloudStorage.of(targetPlayer);
        int originalPageSize = cloudStorage.getPageSize();
        cloudStorage.updatePageSize(rows);
        if (originalPageSize != cloudStorage.getPageSize() && targetPlayer.containerMenu instanceof CloudStorageMenu menu && menu.getCloudStorage() == cloudStorage) {
            cloudStorage.openMenu(targetPlayer);
        } else {
            cloudStorage.syncToClient(targetPlayer);
        }
        sendSuccess(context, cloudStorage, targetPlayer);
        return 1;
    }
}
