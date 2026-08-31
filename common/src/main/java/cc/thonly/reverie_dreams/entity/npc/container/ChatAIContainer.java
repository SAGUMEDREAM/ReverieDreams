package cc.thonly.reverie_dreams.entity.npc.container;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.openai.AIMessage;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.*;

@Slf4j
@Getter
@Setter
public class ChatAIContainer {
    public static final Identifier KEY = ReverieDreams.id("chat_ai");
    public static final Codec<ChatAIContainer> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.unboundedMap(
                            Codec.STRING.xmap(UUID::fromString, UUID::toString),
                            Codec.list(AIMessage.CODEC)
                    ).fieldOf("histories").forGetter(ChatAIContainer::getHistories)
            ).apply(instance, ChatAIContainer::new)
    );

    private Map<UUID, List<AIMessage>> histories;

    public ChatAIContainer(ChatAIContainer old) {
        this.histories = new HashMap<>();
        old.histories.forEach((k, v) -> this.histories.put(k, new ArrayList<>(v)));
    }

    public ChatAIContainer(Map<UUID, List<AIMessage>> histories) {
        this.histories = new HashMap<>();
        histories.forEach((k, v) -> this.histories.put(k, new ArrayList<>(v)));
    }

    public ChatAIContainer() {
        this.histories = new HashMap<>();
    }

    public void addMessage(UUID player, AIMessage message) {
        this.histories.computeIfAbsent(player, k -> new ArrayList<>()).add(message);

        List<AIMessage> list = histories.get(player);

        if (list.size() > 40) {
            list.remove(1);
        }
    }

    public List<AIMessage> getHistory(UUID player) {
        return this.histories.getOrDefault(player, new ArrayList<>());
    }

    public void clear(UUID player) {
        this.histories.remove(player);
    }

    public void clearAll() {
        this.histories.clear();
    }

    //    @Override
    public void readAdditionalSaveData(ValueInput view) {
        view.read("ChatAIData", ChatAIContainer.CODEC).ifPresent(instance -> {
            this.histories = instance.getHistories();
        });
    }

    //    @Override
    public void addAdditionalSaveData(ValueOutput view) {
        view.store("ChatAIData", ChatAIContainer.CODEC, this);
    }

    //    @Override
    public Identifier getId() {
        return KEY;
    }
}