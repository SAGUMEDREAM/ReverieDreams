package cc.thonly.reverie_dreams.openai;

import cc.thonly.reverie_dreams.openai.impl.ClaudeProvider;
import cc.thonly.reverie_dreams.openai.impl.GeminiProvider;
import cc.thonly.reverie_dreams.openai.impl.OpenAIProvider;

import java.util.HashMap;
import java.util.Map;

public class ProviderRegistry {
    private static final Map<String, ChatProvider> PROVIDERS = new HashMap<>();
    public static final String OPENAI = "openai";
    public static final String DEEPSEEK = "deepseek";
    public static final String GEMINI = "gemini";
    public static final String CLAUDE = "claude";

    static {
        PROVIDERS.put(OPENAI, new OpenAIProvider());
        PROVIDERS.put(DEEPSEEK, new OpenAIProvider());
        PROVIDERS.put(GEMINI, new GeminiProvider());
        PROVIDERS.put(CLAUDE, new ClaudeProvider());
    }

    public static ChatProvider get(String name) {
        name = name.toLowerCase();
        return PROVIDERS.get(name);
    }

    public static Map<String, ChatProvider> getProviders() {
        return Map.copyOf(PROVIDERS);
    }

}
