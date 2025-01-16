package com.imoonday.personalcloudstorage.command;

import com.imoonday.personalcloudstorage.config.ServerConfig;
import com.imoonday.personalcloudstorage.network.SyncConfigS2CPacket;
import com.imoonday.personalcloudstorage.platform.Services;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

import static net.minecraft.commands.Commands.literal;

public class ReloadCommand {

    public static LiteralArgumentBuilder<CommandSourceStack> builder() {
        return literal("reload").executes(context -> {
            ServerConfig.load();
            List<ServerPlayer> players = context.getSource().getServer().getPlayerList().getPlayers();
            Services.PLATFORM.sendToAllPlayers(players, new SyncConfigS2CPacket(ServerConfig.get().save(new CompoundTag())));
            context.getSource().sendSuccess(() -> Component.translatable("message.personalcloudstorage.reload"), true);
            return 1;
        });
    }
}
