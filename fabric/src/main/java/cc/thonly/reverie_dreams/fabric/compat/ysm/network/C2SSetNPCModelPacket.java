package cc.thonly.reverie_dreams.fabric.compat.ysm.network;

import cc.thonly.reverie_dreams.entity.npc.AbstractNPCEntity;
import cc.thonly.reverie_dreams.entity.npc.BaseNPCLikeEntity;
import com.micaftic.morpher.core.api.network.PacketContext;
import com.micaftic.morpher.core.compat.api.CompatServices;
import com.micaftic.morpher.core.compat.touhoulittlemaid.MaidModelSync;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

import java.util.List;

@SuppressWarnings("resource")
public record C2SSetNPCModelPacket(int npcId, String modelId, String textureId) {
    public C2SSetNPCModelPacket(int npcId, String modelId, String textureId) {
        this.npcId = npcId;
        this.modelId = modelId;
        this.textureId = textureId == null ? "" : textureId;
    }

    public static void encode(C2SSetNPCModelPacket message, FriendlyByteBuf buf) {
        buf.writeVarInt(message.npcId);
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
                    Entity entity = sender.level().getEntity(message.npcId());
                    String modelId = message.modelId;
                    String textureId = message.textureId;
                    if (entity instanceof BaseNPCLikeEntity npc) {
                        npc.reverie_dreams$setModelId(modelId);
                        npc.reverie_dreams$setTexture(textureId);
                    }
                }
            });
        }
    }

}