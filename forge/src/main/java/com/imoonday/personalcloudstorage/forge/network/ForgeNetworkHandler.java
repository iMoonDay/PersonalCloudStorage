package com.imoonday.personalcloudstorage.forge.network;

import com.imoonday.personalcloudstorage.PersonalCloudStorage;
import com.imoonday.personalcloudstorage.network.*;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.function.Supplier;

public class ForgeNetworkHandler {

    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel SIMPLE_CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(PersonalCloudStorage.MOD_ID, "network"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static void init() {
        PersonalCloudStorage.LOGGER.info(String.format("Initializing %s network...", PersonalCloudStorage.MOD_ID));
        SIMPLE_CHANNEL.registerMessage(0, OpenCloudStorageC2SRequest.class, OpenCloudStorageC2SRequest::write, OpenCloudStorageC2SRequest::new, ForgeNetworkHandler::handle);
        SIMPLE_CHANNEL.registerMessage(1, PageC2SRequest.class, PageC2SRequest::write, PageC2SRequest::new, ForgeNetworkHandler::handle);
        SIMPLE_CHANNEL.registerMessage(2, UpdateCloudStorageS2CPacket.class, UpdateCloudStorageS2CPacket::write, UpdateCloudStorageS2CPacket::new, ForgeNetworkHandler::handle);
        SIMPLE_CHANNEL.registerMessage(3, RequestUpdateC2SRequest.class, RequestUpdateC2SRequest::write, RequestUpdateC2SRequest::new, ForgeNetworkHandler::handle);
        PersonalCloudStorage.LOGGER.info(String.format("Initialized %s network!", PersonalCloudStorage.MOD_ID));
    }

    public static <T extends NetworkPacket> void sendToPlayer(ServerPlayer playerEntity, T packet) {
        SIMPLE_CHANNEL.sendTo(packet, playerEntity.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
    }

    public static void sendToServer(Object objectToSend) {
        SIMPLE_CHANNEL.sendToServer(objectToSend);
    }

    public static <T extends NetworkPacket> void handle(T packet, Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        if (context.getDirection().getReceptionSide().isClient()) {
            context.enqueueWork(() -> Client.clientHandle(packet));
        } else {
            context.enqueueWork(() -> packet.handle(context.getSender()));
        }
        context.setPacketHandled(true);
    }

    private static class Client {

        private static <T extends NetworkPacket> void clientHandle(T packet) {
            packet.handle(Minecraft.getInstance().player);
        }
    }
}
