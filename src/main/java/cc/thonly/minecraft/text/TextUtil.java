package cc.thonly.minecraft.text;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;

import java.util.Optional;

public class TextUtil {
    public static final Gson GSON = new Gson();

    public static String encode(Text text) {
        DataResult<JsonElement> dataResult = TextCodecs.CODEC.encodeStart(JsonOps.INSTANCE, text);
        Optional<JsonElement> nameResult = dataResult.result();
        return nameResult.map(GSON::toJson).orElse("");
    }

    public static Optional<Text> decode(String json) {
        if (json == null || json.isEmpty()) {
            return Optional.empty();
        }
        JsonElement jsonElement = JsonParser.parseString(json);
        Dynamic<JsonElement> input = new Dynamic<>(JsonOps.INSTANCE, jsonElement);
        DataResult<Text> parse = TextCodecs.CODEC.parse(input);
        return parse.result();
    }
}
