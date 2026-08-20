package cc.thonly.reverie_dreams.server;

import cc.thonly.reverie_dreams.api.entity.PlayerEntityDataModifier;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.Map;

public class PlayerSettings {
    public static final Map<String, Object> DEFINES = new Object2ObjectLinkedOpenHashMap<>(8);
    public static final String DISABLE_CHAT_AI = "DisableChatAI";

    static {
        DEFINES.put(DISABLE_CHAT_AI, false);
    }

    private final Player player;

    private final Map<String, KeyValue<?>> values =
            new Object2ObjectLinkedOpenHashMap<>(8);

    public PlayerSettings(Player player) {
        this.player = player;

        DEFINES.forEach((key, value) -> {
            this.values.put(
                    key,
                    new KeyValue<>(
                            key,
                            ValueType.from(value),
                            value
                    )
            );
        });
    }

    public PlayerSettings(Player player, Map<String, KeyValue<?>> values) {
        this(player);
        this.values.putAll(values);
    }

    public static PlayerSettings get(Player player) {
        return ((PlayerEntityDataModifier) player).reverie_dreams$getPlayerSettings();
    }

    public <T> void set(String name, T value) {
        if (value == null) {
            this.values.remove(name);
            return;
        }

        this.values.put(name, new KeyValue<>(
                name,
                ValueType.from(value),
                value
        ));
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String name) {
        KeyValue<?> value = this.values.get(name);

        if (value == null) {
            return null;
        }

        return (T) value.get();
    }

    public void read(ValueInput view) {
        view = view.childOrEmpty("reverie_dreams$settings");

        for (Map.Entry<String, KeyValue<?>> entry : this.values.entrySet()) {
            String name = entry.getKey();
            KeyValue<?> keyValue = entry.getValue();

            Object value = keyValue.type().read(view, name);

            if (value != null) {
                set(name, value);
            }
        }
    }

    public void save(ValueOutput view) {
        ValueOutput settings =
                view.child("reverie_dreams$settings");

        for (KeyValue<?> value : this.values.values()) {
            value.type().write(
                    settings,
                    value.key(),
                    value.get()
            );
        }
    }

    public Player player() {
        return player;
    }

    public enum ValueType {
        NUMBER(Number.class) {
            @Override
            Object read(ValueInput view, String key) {
                return view.getDoubleOr(key, 0D);
            }

            @Override
            void write(ValueOutput view, String key, Object value) {
                view.putDouble(key, ((Number) value).doubleValue());
            }
        },

        STRING(String.class) {
            @Override
            Object read(ValueInput view, String key) {
                return view.getStringOr(key, "");
            }

            @Override
            void write(ValueOutput view, String key, Object value) {
                view.putString(key, (String) value);
            }
        },

        BOOL(Boolean.class) {
            @Override
            Object read(ValueInput view, String key) {
                return view.getBooleanOr(key, false);
            }

            @Override
            void write(ValueOutput view, String key, Object value) {
                view.putBoolean(key, (Boolean) value);
            }
        };

        private final Class<?> type;

        ValueType(Class<?> type) {
            this.type = type;
        }

        public Class<?> type() {
            return this.type;
        }

        public boolean matches(Object value) {
            return value != null && type.isInstance(value);
        }

        abstract Object read(
                ValueInput view,
                String key
        );

        abstract void write(
                ValueOutput view,
                String key,
                Object value
        );

        public static ValueType from(Object value) {
            if (value instanceof Number) {
                return NUMBER;
            }

            if (value instanceof String) {
                return STRING;
            }

            if (value instanceof Boolean) {
                return BOOL;
            }

            throw new IllegalArgumentException(
                    "Unsupported value type: "
                            + value.getClass().getName()
            );
        }
    }

    public static class KeyValue<T> {

        private final String key;
        private final ValueType type;
        private T value;

        public KeyValue(
                String key,
                ValueType type,
                T value
        ) {
            this.key = key;
            this.type = type;
            this.value = value;
        }

        public String key() {
            return this.key;
        }

        public ValueType type() {
            return this.type;
        }

        public T get() {
            return this.value;
        }

        public void set(T value) {
            this.value = value;
        }
    }
}