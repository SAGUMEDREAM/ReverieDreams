package cc.thonly.minecraft.text;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;

public class TextUtil {
    public static final Gson GSON = new Gson();

    public static String encode(Component text) {
        DataResult<JsonElement> dataResult = ComponentSerialization.CODEC.encodeStart(JsonOps.INSTANCE, text);
        Optional<JsonElement> nameResult = dataResult.result();
        return nameResult.map(GSON::toJson).orElse("");
    }

    public static Optional<Component> decode(String json) {
        if (json == null || json.isEmpty()) {
            return Optional.empty();
        }
        JsonElement jsonElement = JsonParser.parseString(json);
        Dynamic<JsonElement> input = new Dynamic<>(JsonOps.INSTANCE, jsonElement);
        DataResult<Component> parse = ComponentSerialization.CODEC.parse(input);
        return parse.result();
    }
}
