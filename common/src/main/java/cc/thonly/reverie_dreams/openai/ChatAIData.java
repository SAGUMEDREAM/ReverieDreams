package cc.thonly.reverie_dreams.openai;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
@Getter
@Setter
public class ChatAIData {

    public static final Codec<ChatAIData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.unboundedMap(
                            Codec.STRING.xmap(UUID::fromString, UUID::toString),
                            Codec.list(AIMessage.CODEC)
                    ).fieldOf("histories").forGetter(ChatAIData::getHistories)
            ).apply(instance, ChatAIData::new)
    );

    private Map<UUID, List<AIMessage>> histories;

    public ChatAIData(Map<UUID, List<AIMessage>> histories) {
        this.histories = new HashMap<>();
        histories.forEach((k, v) -> this.histories.put(k, new ArrayList<>(v)));
    }

    public ChatAIData() {
        this.histories = new HashMap<>();
    }

    public void addMessage(UUID player, AIMessage message) {
        histories.computeIfAbsent(player, k -> new ArrayList<>()).add(message);

        List<AIMessage> list = histories.get(player);

        if (list.size() > 40) {
            list.remove(1);
        }
    }

    public List<AIMessage> getHistory(UUID player) {
        return histories.getOrDefault(player, new ArrayList<>());
    }

    public void clear(UUID player) {
        histories.remove(player);
    }

    public void clearAll() {
        histories.clear();
    }
}