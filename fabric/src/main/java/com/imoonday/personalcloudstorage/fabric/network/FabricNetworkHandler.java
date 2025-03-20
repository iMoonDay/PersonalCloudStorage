package com.imoonday.personalcloudstorage.fabric.network;

import com.imoonday.personalcloudstorage.PersonalCloudStorage;
import com.imoonday.personalcloudstorage.network.*;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class FabricNetworkHandler {

    private static final Map<Class<?>, BiConsumer<?, FriendlyByteBuf>> ENCODERS = new ConcurrentHashMap<>();
    private static final Map<Class<?>, ResourceLocation> PACKET_IDS = new ConcurrentHashMap<>();

    public static void init() {
        PersonalCloudStorage.LOGGER.info(String.format("Initializing %s network...", PersonalCloudStorage.MOD_ID));
        registerMessage("open_cloud_storage", OpenCloudStorageC2SRequest.class, OpenCloudStorageC2SRequest::write, OpenCloudStorageC2SRequest::new, OpenCloudStorageC2SRequest::handle);
        registerMessage("sync_cloud_storage", SyncCloudStorageS2CPacket.class, SyncCloudStorageS2CPacket::write, SyncCloudStorageS2CPacket::new, SyncCloudStorageS2CPacket::handle);
        registerMessage("request_sync", RequestSyncC2SRequest.class, RequestSyncC2SRequest::write, RequestSyncC2SRequest::new, RequestSyncC2SRequest::handle);
        registerMessage("sync_config", SyncConfigS2CPacket.class, SyncConfigS2CPacket::write, SyncConfigS2CPacket::new, SyncConfigS2CPacket::handle);
        registerMessage("sync_settings", SyncSettingsPacket.class, SyncSettingsPacket::write, SyncSettingsPacket::new, SyncSettingsPacket::handle);
        registerMessage("sync_current_page", SyncCurrentPageS2CPacket.class, SyncCurrentPageS2CPacket::write, SyncCurrentPageS2CPacket::new, SyncCurrentPageS2CPacket::handle);
        PersonalCloudStorage.LOGGER.info(String.format("Initialized %s network!", PersonalCloudStorage.MOD_ID));
    }

    private static <T> void registerMessage(String id, Class<T> clazz, BiConsumer<T, FriendlyByteBuf> encode, Function<FriendlyByteBuf, T> decode, BiConsumer<T, Player> handler) {
        ENCODERS.put(clazz, encode);
        PACKET_IDS.put(clazz, PersonalCloudStorage.id(id));
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            ClientProxy.registerClientReceiver(id, decode, handler);
        }
        ServerProxy.registerServerReceiver(id, decode, handler);
    }

    public static <MSG> void sendToServer(MSG packet) {
        ResourceLocation packetId = PACKET_IDS.get(packet.getClass());
        @SuppressWarnings("unchecked")
        BiConsumer<MSG, FriendlyByteBuf> encoder = (BiConsumer<MSG, FriendlyByteBuf>) ENCODERS.get(packet.getClass());
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        encoder.accept(packet, buf);
        ClientPlayNetworking.send(packetId, buf);
    }

    public static <MSG> void sendToPlayer(ServerPlayer player, MSG packet) {
        ResourceLocation packetId = PACKET_IDS.get(packet.getClass());
        @SuppressWarnings("unchecked")
        BiConsumer<MSG, FriendlyByteBuf> encoder = (BiConsumer<MSG, FriendlyByteBuf>) ENCODERS.get(packet.getClass());
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        encoder.accept(packet, buf);
        ServerPlayNetworking.send(player, packetId, buf);
    }

    public static <MSG> void sendToAllPlayers(List<ServerPlayer> players, MSG packet) {
        ResourceLocation packetId = PACKET_IDS.get(packet.getClass());
        @SuppressWarnings("unchecked")
        BiConsumer<MSG, FriendlyByteBuf> encoder = (BiConsumer<MSG, FriendlyByteBuf>) ENCODERS.get(packet.getClass());
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        encoder.accept(packet, buf);
        players.forEach(player -> ServerPlayNetworking.send(player, packetId, buf));
    }

    public static class ClientProxy {

        public static <T> void registerClientReceiver(String id, Function<FriendlyByteBuf, T> decode, BiConsumer<T, Player> handler) {
            ClientPlayNetworking.registerGlobalReceiver(PersonalCloudStorage.id(id), (client, listener, buf, responseSender) -> {
                buf.retain();
                client.execute(() -> {
                    T packet = decode.apply(buf);
                    try {
                        handler.accept(packet, client.player);
                    } catch (Throwable throwable) {
                        PersonalCloudStorage.LOGGER.error("Packet failed: ", throwable);
                        throw throwable;
                    }
                    buf.release();
                });
            });
        }
    }

    public static class ServerProxy {

        public static <T> void registerServerReceiver(String id, Function<FriendlyByteBuf, T> decode, BiConsumer<T, Player> handler) {
            ServerPlayNetworking.registerGlobalReceiver(PersonalCloudStorage.id(id), (server, player, handler1, buf, responseSender) -> {
                buf.retain();
                server.execute(() -> {
                    T packet = decode.apply(buf);
                    try {
                        handler.accept(packet, player);
                    } catch (Throwable throwable) {
                        PersonalCloudStorage.LOGGER.error("Packet failed: ", throwable);
                        throw throwable;
                    }
                    buf.release();
                });
            });
        }
    }
}
