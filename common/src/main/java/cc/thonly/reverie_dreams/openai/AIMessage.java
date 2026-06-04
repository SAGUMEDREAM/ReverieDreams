package cc.thonly.reverie_dreams.openai;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class AIMessage {
    public static final Codec<AIMessage> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.fieldOf("role").forGetter(AIMessage::getRole),
                    Codec.STRING.fieldOf("content").forGetter(AIMessage::getContent)
            ).apply(instance, AIMessage::new)
    );
    public static final String USER = "user";
    public static final String ASSISTANT = "assistant";
    public static final String SYSTEM = "system";

    private String role;    // "user" / "assistant" / "system"
    private String content;

    public static AIMessage copy(AIMessage message) {
        return new AIMessage(message.role, message.content);
    }

    public static List<AIMessage> copy(List<AIMessage> messages) {
        List<AIMessage> list = new ArrayList<>();
        for (AIMessage message : messages) {
            list.add(copy(message));
        }
        return list;
    }
}