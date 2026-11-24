package cc.thonly.reverie_dreams.entity.npc.container;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.ai.AIMessage;
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
    public static final int MAX_PLAYERS = 32;
    public static final int MAX_MESSAGES = 80;
    public static final int MAX_MESSAGE_CHARS = 8192;
    public static final int MAX_HISTORY_CHARS = 16384;
    public static final int MAX_TOTAL_CHARS = 131072;
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
        this(old.histories);
    }

    public ChatAIContainer(Map<UUID, List<AIMessage>> histories) {
        this.histories = new LinkedHashMap<>(32, 0.75f, true);
        histories.forEach((k, v) -> {
            this.histories.put(k, AIMessage.copy(v));
            this.trim(k, MAX_MESSAGES);
        });
        this.enforceBounds();
    }

    public ChatAIContainer() {
        this.histories = new LinkedHashMap<>(32, 0.75f, true);
    }

    public void addMessage(UUID player, AIMessage message) {
        this.getOrCreateHistory(player).add(message);
        this.trim(player, ReverieDreams.config().maxChatHistory);
    }

    public List<AIMessage> getOrCreateHistory(UUID player) {
        var history = this.histories.computeIfAbsent(player, k -> new ArrayList<>());
        this.enforceBounds();
        return history;
    }

    public List<AIMessage> getHistory(UUID player) {
        return this.histories.getOrDefault(player, new ArrayList<>());
    }

    /**
     * Trims the conversation history down to {@code maxSize} messages.
     * The leading system prompt (index 0) is kept whenever possible.
     */
    public void trim(UUID player, int maxSize) {
        List<AIMessage> list = this.histories.get(player);
        if (list == null) {
            return;
        }
        int limit = Math.clamp(maxSize, 2, MAX_MESSAGES);
        list.removeIf(message -> message == null || message.getContent() == null || message.getRole() == null
                || !Set.of(AIMessage.SYSTEM, AIMessage.USER, AIMessage.ASSISTANT).contains(message.getRole()));
        for (var message : list)
            if (message.getContent().length() > MAX_MESSAGE_CHARS) {
                message.setContent(message.getContent().substring(0, MAX_MESSAGE_CHARS));
            }
        while (list.size() > limit || chars(list) > MAX_HISTORY_CHARS) {
            if (list.size() > 1 && AIMessage.SYSTEM.equals(list.getFirst().getRole())) {
                list.remove(1);
            } else {
                list.removeFirst();
            }
        }
        this.enforceBounds();
    }

    private static int chars(List<AIMessage> messages) {
        return messages.stream().mapToInt(m -> m.getContent().length()).sum();
    }

    private void enforceBounds() {
        while (this.histories.size() > MAX_PLAYERS
                || this.histories.values().stream().mapToInt(ChatAIContainer::chars).sum() > MAX_TOTAL_CHARS) {
            this.histories.remove(this.histories.keySet().iterator().next());
        }
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
            this.histories = new ChatAIContainer(instance).histories;
        });
    }

    //    @Override
    public void addAdditionalSaveData(ValueOutput view) {
        for (var player : List.copyOf(this.histories.keySet()))
            this.trim(player, MAX_MESSAGES);
        this.enforceBounds();
        view.store("ChatAIData", ChatAIContainer.CODEC, this);
    }

    //    @Override
    public Identifier getId() {
        return KEY;
    }
}
