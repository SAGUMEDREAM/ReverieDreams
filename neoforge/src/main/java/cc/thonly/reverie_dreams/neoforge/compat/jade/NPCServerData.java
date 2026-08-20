package cc.thonly.reverie_dreams.neoforge.compat.jade;

import cc.thonly.reverie_dreams.data.npc.NPCState;
import cc.thonly.reverie_dreams.data.npc.NPCWorkMode;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.List;

public record NPCServerData(int nutrition, int saturation, List<Component> foodTextComponents, NPCState state, NPCWorkMode workMode, Component owner, boolean tamed) {
    public static final StreamCodec<RegistryFriendlyByteBuf, NPCServerData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            NPCServerData::nutrition,
            ByteBufCodecs.VAR_INT,
            NPCServerData::saturation,
            ComponentSerialization.STREAM_CODEC.apply(
                    ByteBufCodecs.list()
            ),
            NPCServerData::foodTextComponents,
            ByteBufCodecs.fromCodec(NPCState.CODEC),
            NPCServerData::state,
            ByteBufCodecs.fromCodec(NPCWorkMode.CODEC),
            NPCServerData::workMode,
            ComponentSerialization.STREAM_CODEC,
            NPCServerData::owner,
            ByteBufCodecs.BOOL,
            NPCServerData::tamed,
            NPCServerData::new
    );
}
