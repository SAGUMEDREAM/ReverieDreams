package cc.thonly.reverie_dreams.ai.provider;

import cc.thonly.reverie_dreams.api.entity.type.ChatAIEntity;
import cc.thonly.reverie_dreams.ai.AIMessage;
import cc.thonly.reverie_dreams.ai.ChatProvider;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

import java.util.List;

public class CustomProvider implements ChatProvider {

    @Override
    public <T extends Entity> AIMessage chat(List<AIMessage> history, ChatAIEntity<T> entity, ServerPlayer player, String npcState) {
        return null;
    }
}
