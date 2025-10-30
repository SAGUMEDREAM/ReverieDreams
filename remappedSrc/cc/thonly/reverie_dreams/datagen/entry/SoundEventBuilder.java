package cc.thonly.reverie_dreams.datagen.entry;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.resources.ResourceLocation;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Accessors(chain = true)
@Setter
@Getter
public class SoundEventBuilder {
    private final ResourceLocation key;
    private String subtitle;
    private List<Object> sounds = new LinkedList<>();

    public SoundEventBuilder(ResourceLocation key) {
        this.key = key;
    }

    public SoundEventBuilder setSubtitle(ResourceLocation key) {
        this.subtitle = key.toLanguageKey("sound");
        return this;
    }

    public SoundEventBuilder addSounds(String sound) {
        this.sounds.add(sound);
        return this;
    }

    public SoundEventBuilder addSounds(ResourceLocation key) {
        this.sounds.add(key.toString());
        return this;
    }

    public SoundEventBuilder addSoundsByName(String name) {
        Map<String, String> map = Map.of("name", name);
        this.sounds.add(map);
        return this;
    }

    public SoundEventBuilder addSoundsByName(ResourceLocation key) {
        return addSoundsByName(key.toString());
    }

    public SoundEventBuilder addSoundsByName(String name, boolean stream) {
        Map<String, Object> map = new HashMap<>();
        map.put("name", name);
        map.put("stream", stream);
        this.sounds.add(map);
        return this;
    }

    public SoundEventBuilder addSoundsByName(ResourceLocation key, boolean stream) {
        return addSoundsByName(key.toString(), stream);
    }

    public JsonElement toJsonElement() {
        JsonObject jsonObject = new JsonObject();

        if (this.subtitle != null && !this.subtitle.isEmpty()) {
            jsonObject.addProperty("subtitle", this.subtitle);
        }

        JsonArray soundsArray = new JsonArray();

        for (Object sound : this.sounds) {
            if (sound instanceof String) {
                soundsArray.add((String) sound);
            } else if (sound instanceof Map) {
                JsonObject soundObject = new JsonObject();
                Map<String, String> soundMap = (Map<String, String>) sound;
                soundMap.forEach(soundObject::addProperty);
                soundsArray.add(soundObject);
            }
        }

        jsonObject.add("sounds", soundsArray);

        return jsonObject;
    }
}
