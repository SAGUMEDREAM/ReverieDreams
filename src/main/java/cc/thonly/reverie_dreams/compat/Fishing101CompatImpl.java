package cc.thonly.reverie_dreams.compat;

import cc.thonly.mystias_izakaya.component.FoodProperty;
import cc.thonly.mystias_izakaya.registry.FoodProperties;
import cc.thonly.mystias_izakaya.registry.MIRegistryManager;
import cc.thonly.reverie_dreams.api.RegistryManagerReloadCallback;
import cc.thonly.reverie_dreams.registry.StandaloneRegistry;
import net.macck209.fishing101.registries.ItemRegistry;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

@SuppressWarnings("unchecked")
public class Fishing101CompatImpl {
    public static void bootstrap() {
        RegistryManagerReloadCallback.EVENT.register(simpleRegistry -> {
            if (!simpleRegistry.equals(MIRegistryManager.FOOD_PROPERTY)) {
                return;
            }
            StandaloneRegistry<FoodProperty> registry = (StandaloneRegistry<FoodProperty>) simpleRegistry;
            Stream<Map.Entry<Identifier, FoodProperty>> stream = registry.stream();
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
