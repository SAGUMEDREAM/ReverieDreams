package cc.thonly.mystias_izakaya.component;

import cc.thonly.mystias_izakaya.MystiasIzakaya;
import com.mojang.serialization.Codec;
import eu.pb4.polymer.core.api.other.PolymerComponent;
import net.minecraft.component.ComponentType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

import java.util.List;

public class MIDataComponentTypes {

    public static final ComponentType<List<String>> FOOD_PROPERTIES = registerComponent("food_properties",
            ComponentType.<List<String>>builder()
                    .codec(Codec.list(Codec.STRING))
                    .build()
    );
    public static final ComponentType<List<String>> DRINK_PROPERTIES = registerComponent("drink_properties",
            ComponentType.<List<String>>builder()
                    .codec(Codec.list(Codec.STRING))
                    .build()
    );
    public static final ComponentType<Integer> FOOD_BONUS = registerComponent("food_bonus",
            ComponentType.<Integer>builder().codec(Codec.INT).build()
    );

    public static void init() {

    }

    public static <T> ComponentType<T> registerComponent(String path, ComponentType<T> componentType) {
        ComponentType<T> value = Registry.register(Registries.DATA_COMPONENT_TYPE, MystiasIzakaya.id(path), componentType);
        PolymerComponent.registerDataComponent(value);
        return value;
    }
}
