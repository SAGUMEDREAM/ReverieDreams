package cc.thonly.reverie_dreams.api.registry;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;

@SuppressWarnings({"rawtypes", "unchecked"})
public class NetworkManager {
    public static final List<ClientboundEntry<?>> CLIENTBOUNDS = new CopyOnWriteArrayList<>();
    public static final List<ServerboundEntry<?>> SERVERBOUNDS = new CopyOnWriteArrayList<>();

    public static <T extends CustomPacketPayload> void registerClientboundPacket(CustomPacketPayload.Type<T> type, Class<T> clazz, StreamCodec<RegistryFriendlyByteBuf, T> codec, BiConsumer<Player, T> handler) {
        CLIENTBOUNDS.add(new ClientboundEntry<CustomPacketPayload>((CustomPacketPayload.Type) type, (Class) clazz, (StreamCodec) codec, (BiConsumer) handler));
    }

    public static <T extends CustomPacketPayload> void registerServerboundPacket(CustomPacketPayload.Type<T> type, Class<T> clazz, StreamCodec<RegistryFriendlyByteBuf, T> codec, BiConsumer<ServerPlayer, T> handler) {
        SERVERBOUNDS.add(new ServerboundEntry<CustomPacketPayload>((CustomPacketPayload.Type) type, (Class) clazz, (StreamCodec) codec, (BiConsumer) handler));
    }

    public record ClientboundEntry<T extends CustomPacketPayload>(CustomPacketPayload.Type<T> type, Class<T> clazz,
                                                                  StreamCodec<RegistryFriendlyByteBuf, T> codec,
                                                                  BiConsumer<Player, T> handler) {

    }

    public record ServerboundEntry<T extends CustomPacketPayload>(CustomPacketPayload.Type<T> type, Class<T> clazz,
                                                                  StreamCodec<RegistryFriendlyByteBuf, T> codec,
                                                                  BiConsumer<ServerPlayer, T> handler) {

    }
}
