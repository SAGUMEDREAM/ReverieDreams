package cc.thonly.reverie_dreams.neoforge.compat.jade;

import cc.thonly.reverie_dreams.data.npc.NPCState;
import cc.thonly.reverie_dreams.data.npc.NPCWorkMode;
import cc.thonly.reverie_dreams.entity.npc.NPCSimpleEntity;
import cc.thonly.reverie_dreams.entity.npc.container.NPCCustomerContainer;
import cc.thonly.reverie_dreams.entity.npc.container.NPCFoodDataContainer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import org.jspecify.annotations.Nullable;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.StreamServerDataProvider;

import java.util.List;

public class NPCServerDataProvider implements StreamServerDataProvider<EntityAccessor, NPCServerData> {
    public static final NPCServerDataProvider INSTANCE = new NPCServerDataProvider();

    @Override
    public @Nullable NPCServerData streamData(EntityAccessor accessor) {
        if (!(accessor.getEntity() instanceof NPCSimpleEntity npc)) {
            return null;
        }
        NPCFoodDataContainer foodData = npc.getFoodData();
        NPCCustomerContainer customerContainer = npc.getCustomerContainer();
        NPCState npcState = npc.getNpcState();
        NPCWorkMode workMode = npc.getWorkMode();
        LivingEntity owner = npc.getOwner();
        Component displayName = Component.empty();
        if (owner != null) {
            displayName = owner.getDisplayName();
        }
        return new NPCServerData(
                foodData.getNutrition(),
                foodData.getSaturation(),
                List.copyOf(customerContainer.getComponents()),
                npcState,
                workMode,
                displayName,
                npc.isTame()
        );
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, NPCServerData> streamCodec() {
        return NPCServerData.STREAM_CODEC;
    }

    @Override
    public Identifier getUid() {
        return JadePlugin.NPC_DESCRIPTION_PROVIDER;
    }
}
