package cc.thonly.reverie_dreams.networking;

import io.netty.channel.ChannelFutureListener;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.jspecify.annotations.Nullable;

@SuppressWarnings("resource")
public final class FakePlayerNetworkHandler extends ServerGamePacketListenerImpl {
    private static final Connection FAKE_CONNECTION = new FakeClientConnection();

    public FakePlayerNetworkHandler(ServerPlayer player) {
        super(player.level().getServer(), FAKE_CONNECTION, player, CommonListenerCookie.createInitial(player.getGameProfile(), false));
    }

    public void send(Packet<?> packet, @Nullable ChannelFutureListener callbacks) {
    }

    private static final class FakeClientConnection extends Connection {
        private FakeClientConnection() {
            super(PacketFlow.CLIENTBOUND);
        }
    }
}
