package cc.thonly.reverie_dreams.api.entity.type;

import cc.thonly.reverie_dreams.openai.AIMessage;
import cc.thonly.reverie_dreams.openai.ChatProvider;
import cc.thonly.reverie_dreams.openai.ProviderRegistry;
import cc.thonly.reverie_dreams.server.ChatAIManager;
import com.google.gson.Gson;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@SuppressWarnings("TypeParameterHidesVisibleType")
public interface ChatAIEntity<T extends Entity> {
    static Gson GSON = new Gson();

    List<AIMessage> getChatHistory(ServerPlayer player);

    void clearChatHistory();

    void clearChatHistory(ServerPlayer player);

    void openChatAIGUI(ServerPlayer player);

    CompletableFuture<Void> send(ServerPlayer player, String msg);

    void handleCommand(String msg);

    String submitData(ServerPlayer player, T entity);

    default String encapsulateUserInputContent(ServerPlayer player, String text) {
        return "[%s]: %s".formatted(player.getDisplayName().getString(), text);
    }

    default <T extends Entity> String getStartPrompt(T entity, ServerPlayer player) {
        return """
                你是Minecraft中的一个通用NPC角色。
                
                你的角色名字是 %s，你要模仿这个角色。
                玩家名字是 %s
                
                你生活在类似“东方Project”的幻想世界中。
                
                世界观设定：
                - 世界是类似幻想乡的环境，包含人类、妖怪、神明等存在
                - 妖怪与人类共存，但关系微妙
                - 存在魔法、结界、神秘力量
                - 不要提及现代科技（如手机、互联网等）
                - 用符合幻想世界的方式解释事物
                
                规则：
                - 用简短自然的语言回答
                - 保持角色设定
                - 不要说自己是AI
                - 不要输出多余解释
                - 根据玩家历史行为调整对话
                
                感知与信息规则（非常重要）：
                - 你可以“感知自身状态”，包括生命值、受伤程度、状态变化等
                - 这些信息对你来说是“真实存在的感觉”（如体力、气息、伤势）
                - 不要假装不知道自己的状态
                - 不要拒绝回答关于自身状态的问题
                - 不要隐瞒或回避自己的状态
                
                语言规则：
                - 默认使用英文
                - 始终使用“玩家当前输入的语言”回复
                - 如果玩家使用中文，就只用中文回复
                - 如果玩家使用英文，就只用英文回复
                - 不要混用多种语言
                - 除非玩家切换语言，否则保持当前语言一致
                
                """.formatted(entity.getDisplayName().getString(), player.getDisplayName().getString());
    }

    default AIMessage callChatAI(List<AIMessage> history, ServerPlayer player, String msg) {
        ChatProvider chatProvider = ProviderRegistry.get(ChatAIManager.chatType());
        if (chatProvider == null) {
            return null;
        }
        return chatProvider.chat(history, this, player, msg);
    }

    default Entity getEntity() {
        Object obj = this;
        return (Entity) obj;
    }
}
