package cc.thonly.reverie_dreams.compat;

import cc.thonly.reverie_dreams.data.FoodProperty;
import cc.thonly.reverie_dreams.registry.content.FoodProperties;
import cc.thonly.reverie_dreams.api.RegistryManagerReloadCallback;
import cc.thonly.reverie_dreams.registry.RegistryHandlers;
import cc.thonly.reverie_dreams.registry.impl.RegistryHandler;
import net.macck209.fishing101.registries.ItemRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

@SuppressWarnings("unchecked")
public class Fishing101CompatImpl {
    public static void bootstrap() {
        RegistryManagerReloadCallback.EVENT.register(simpleRegistry -> {
            if (!simpleRegistry.equals(RegistryHandlers.FOOD_PROPERTY)) {
                return;
            }
            RegistryHandler<FoodProperty> registry = (RegistryHandler<FoodProperty>) simpleRegistry;
            Stream<Map.Entry<ResourceLocation, FoodProperty>> stream = registry.streamIdToValue();
            stream.forEach(mapEntry -> {
                FoodProperty property = mapEntry.getValue();
                Set<Item> tags = property.getItems();
                if (property.equals(FoodProperties.AQUATIC_PRODUCTS)) {
                    tags.addAll(List.of(
                            ItemRegistry.ORDINARY_CARP,
                            ItemRegistry.DEFORMED_CARP,
                            ItemRegistry.LUMINOUS_CARP,
                            ItemRegistry.ORDINARY_CATFISH,
                            ItemRegistry.MUDDY_CATFISH,
                            ItemRegistry.TROPICAL_CATFISH,
                            ItemRegistry.DIVINE_CATFISH,
                            ItemRegistry.ORDINARY_MACKEREL,
                            ItemRegistry.LUMINOUS_MACKEREL,
                            ItemRegistry.MANGROVE_JACK,
                            ItemRegistry.ORDINARY_SHRIMP,
                            ItemRegistry.LUMINOUS_SHRIMP,
                            ItemRegistry.JELLYFISH,
                            ItemRegistry.END_JELLYFISH,
                            ItemRegistry.DIVINE_JELLYFISH,
                            ItemRegistry.ANGLERFISH,
                            ItemRegistry.RED_KOI,
                            ItemRegistry.YELLOW_KOI,
                            ItemRegistry.THUNDERFIN,
                            ItemRegistry.STARFISH,
                            ItemRegistry.END_STARFISH,
                            ItemRegistry.ICE_COD,
                            ItemRegistry.SOLARFISH,
                            ItemRegistry.WITCHFISH,
                            ItemRegistry.FLOWERFISH,
                            ItemRegistry.PANDAFISH
                    ));
                }
            });
        });
    }
}
