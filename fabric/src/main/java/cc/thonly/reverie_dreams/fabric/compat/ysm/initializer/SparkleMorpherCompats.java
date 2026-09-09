package cc.thonly.reverie_dreams.fabric.compat.ysm.initializer;

import cc.thonly.reverie_dreams.client.networking.ClientNetworkingHandlers;
import cc.thonly.reverie_dreams.fabric.compat.ysm.network.C2SSetNPCModelPacket;
import cc.thonly.reverie_dreams.fabric.compat.ysm.network.YsmScreenPacket;
import cc.thonly.reverie_dreams.util.YsmHolder;
import com.micaftic.morpher.core.api.network.PacketDirection;
import com.micaftic.morpher.core.api.network.YSMChannel;
import dev.architectury.networking.NetworkManager;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SparkleMorpherCompats {
    static final int packet_offset = 64;

    public static void bootstrap() {
        try {
            YsmHolder.setInitialized();
            NetworkManager.registerReceiver(NetworkManager.Side.S2C, YsmScreenPacket.PACKET_ID, YsmScreenPacket.CODEC, (packet, context) -> {
                ClientNetworkingHandlers.safeHandleClient(() -> ClientSparkleMorpherCompats.openScreen(packet.entityId()));
            });
            YSMChannel.register(getPacket_offset(24), C2SSetNPCModelPacket.class, C2SSetNPCModelPacket::encode, C2SSetNPCModelPacket::decode, C2SSetNPCModelPacket::handle, PacketDirection.PLAY_TO_SERVER);
        } catch (Exception e) {
            log.error("Error: ", e);
        }
    }

    static int getPacket_offset(int discriminator) {
        int i = packet_offset + discriminator;
        if (i > 256) {
            throw new IllegalArgumentException("%s > 256".formatted(i));
        }
        return i;
    }
}
