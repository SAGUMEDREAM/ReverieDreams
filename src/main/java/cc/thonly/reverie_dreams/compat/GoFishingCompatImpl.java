package cc.thonly.reverie_dreams.compat;

import cc.thonly.mystias_izakaya.component.FoodProperty;
import cc.thonly.mystias_izakaya.registry.FoodProperties;
import cc.thonly.mystias_izakaya.registry.MIRegistryManager;
import cc.thonly.reverie_dreams.api.RegistryManagerReloadCallback;
import cc.thonly.reverie_dreams.registry.StandaloneRegistry;
import draylar.gofish.registry.GoFishItems;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

@SuppressWarnings("unchecked")
public class GoFishingCompatImpl {
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
                            GoFishItems.ICICLE_FISH,
                            GoFishItems.SNOWBALL_FISH,
                            GoFishItems.SLIMEFISH,
                            GoFishItems.LILYFISH,
                            GoFishItems.SEAWEED_EEL,
                            GoFishItems.TERRAFISH,
                            GoFishItems.CARROT_CARP,
                            GoFishItems.OAKFISH,
                            GoFishItems.LUNARFISH,
                            GoFishItems.GALAXY_STARFISH,
                            GoFishItems.STARRY_SALMON,
                            GoFishItems.NEBULA_SWORDFISH,
                            GoFishItems.RAINY_BASS,
                            GoFishItems.THUNDERING_BASS,
                            GoFishItems.CLOUDY_CRAB,
                            GoFishItems.BLIZZARD_BASS
                    ));
                }
            });
        });
    }
}
