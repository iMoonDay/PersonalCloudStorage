package com.imoonday.personalcloudstorage.fabric.network;

import com.imoonday.personalcloudstorage.PersonalCloudStorage;
import com.imoonday.personalcloudstorage.network.OpenCloudStorageC2SRequest;
import com.imoonday.personalcloudstorage.network.PageC2SRequest;
import com.imoonday.personalcloudstorage.network.RequestUpdateC2SRequest;
import com.imoonday.personalcloudstorage.network.UpdateCloudStorageS2CPacket;
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
        registerMessage("page", PageC2SRequest.class, PageC2SRequest::write, PageC2SRequest::new, PageC2SRequest::handle);
        registerMessage("update_cloud_storage", UpdateCloudStorageS2CPacket.class, UpdateCloudStorageS2CPacket::write, UpdateCloudStorageS2CPacket::new, UpdateCloudStorageS2CPacket::handle);
        registerMessage("request_update", RequestUpdateC2SRequest.class, RequestUpdateC2SRequest::write, RequestUpdateC2SRequest::new, RequestUpdateC2SRequest::handle);
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
