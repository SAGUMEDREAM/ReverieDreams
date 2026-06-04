package cc.thonly.reverie_dreams.openai;

import cc.thonly.reverie_dreams.api.entity.type.ChatAIEntity;
import com.google.gson.Gson;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import okhttp3.OkHttpClient;

import java.util.List;

public interface ChatProvider {
    OkHttpClient CLIENT = new OkHttpClient();
    Gson GSON = new Gson();

    <T extends Entity> AIMessage chat(List<AIMessage> history, ChatAIEntity<T> entity, ServerPlayer player, String msg);
}
