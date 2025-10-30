package cc.thonly.mystias_izakaya.component;

import cc.thonly.mystias_izakaya.MystiasIzakaya;
import com.mojang.serialization.Codec;
import eu.pb4.polymer.core.api.other.PolymerComponent;
import java.util.List;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;

public class MIDataComponentTypes {

    public static final DataComponentType<List<String>> FOOD_PROPERTIES = registerComponent("food_properties",
            DataComponentType.<List<String>>builder()
                    .persistent(Codec.list(Codec.STRING))
                    .build()
    );
    public static final DataComponentType<List<String>> DRINK_PROPERTIES = registerComponent("drink_properties",
            DataComponentType.<List<String>>builder()
                    .persistent(Codec.list(Codec.STRING))
                    .build()
    );
    public static final DataComponentType<Integer> FOOD_BONUS = registerComponent("food_bonus",
            DataComponentType.<Integer>builder().persistent(Codec.INT).build()
    );

    public static void init() {

    }

    public static <T> DataComponentType<T> registerComponent(String path, DataComponentType<T> componentType) {
        DataComponentType<T> value = Registry.register(BuiltInRegistries.DATA_COMPONENT_TYPE, MystiasIzakaya.id(path), componentType);
        PolymerComponent.registerDataComponent(value);
        return value;
    }
}
