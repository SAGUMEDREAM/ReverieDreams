package cc.thonly.reverie_dreams.fabric.compat.ysm.network;

import com.micaftic.morpher.core.api.network.PacketContext;
import com.micaftic.morpher.core.compat.touhoulittlemaid.MaidModelSync;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

@SuppressWarnings("resource")
public record C2SSetNPCModelPacket(int maidId, String modelId, String textureId) {
    public C2SSetNPCModelPacket(int maidId, String modelId, String textureId) {
        this.maidId = maidId;
        this.modelId = modelId;
        this.textureId = textureId == null ? "" : textureId;
    }

    public static void encode(C2SSetNPCModelPacket message, FriendlyByteBuf buf) {
        buf.writeVarInt(message.maidId);
        buf.writeUtf(message.modelId);
        buf.writeUtf(message.textureId);
    }

    public static C2SSetNPCModelPacket decode(FriendlyByteBuf buf) {
        return new C2SSetNPCModelPacket(buf.readVarInt(), buf.readUtf(), buf.readUtf());
    }

    public static void handle(C2SSetNPCModelPacket message, PacketContext ctx) {
        if (ctx.isServerSide()) {
            ctx.enqueueWork(() -> {
                ServerPlayer sender = ctx.getSender();
                if (sender != null) {
                    Entity maid = sender.level().getEntity(message.maidId());
                    MaidModelSync.applySelectedModel(maid, sender, message.modelId(), message.textureId());
                }
            });
        }
    }

}