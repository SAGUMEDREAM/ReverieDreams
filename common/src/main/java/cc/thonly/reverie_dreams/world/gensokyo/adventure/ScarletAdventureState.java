package cc.thonly.reverie_dreams.world.gensokyo.adventure;

import com.mojang.serialization.Codec;
import net.minecraft.resources.Identifier;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.HashMap;
import java.util.Map;

/**
 * World-shared issued rewards and encounter ownership, independent of template/chunk repair.
 */
public final class ScarletAdventureState extends SavedData {
    public static final SavedDataType<ScarletAdventureState> TYPE = new SavedDataType<>(
            Identifier.parse("reverie_dreams:scarlet_adventure"), ScarletAdventureState::new,
            Codec.unboundedMap(Codec.STRING, Codec.STRING).xmap(ScarletAdventureState::new, s -> Map.copyOf(s.values)), DataFixTypes.LEVEL);
    private final Map<String, String> values = new HashMap<>();

    public ScarletAdventureState() {
    }

    private ScarletAdventureState(Map<String, String> values) {
        this.values.putAll(values);
    }

    public String get(String key) {
        return values.getOrDefault(key, "");
    }

    public long number(String key) {
        try {
            return Long.parseLong(get(key));
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }

    public boolean has(String key) {
        return values.containsKey(key);
    }

    public void put(String key, String value) {
        if (!value.equals(values.put(key, value)))
            setDirty();
    }

    public void put(String key, long value) {
        put(key, Long.toString(value));
    }

    public void remove(String key) {
        if (values.remove(key) != null)
            setDirty();
    }
}
