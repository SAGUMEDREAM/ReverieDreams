package cc.thonly.reverie_dreams.compat;

import cc.thonly.reverie_dreams.data.FoodProperty;
import cc.thonly.reverie_dreams.registry.content.FoodProperties;
import cc.thonly.reverie_dreams.api.RegistryManagerReloadCallback;
import cc.thonly.reverie_dreams.registry.RegistryHandlers;
import cc.thonly.reverie_dreams.registry.impl.RegistryHandler;
import draylar.gofish.registry.GoFishItems;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

@SuppressWarnings("unchecked")
public class GoFishingCompatImpl {
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
                            GoFishItems.BLIZZARD_BASS,
                            GoFishItems.MATRIX_FISH,
                            GoFishItems.CHARFISH,
                            GoFishItems.GILDED_BLACKSTONE_CARP,
                            GoFishItems.SMOKEY_SALMON,
                            GoFishItems.SOUL_SALMON,
                            GoFishItems.MAGMA_COD,
                            GoFishItems.BASALT_BASS,
                            GoFishItems.OBSIDIAN_HALIBUT,
                            GoFishItems.ENDFISH,
                            GoFishItems.CHORUS_COD,
                            GoFishItems.DRAGONFISH,
                            GoFishItems.OMEGA_FLOATER,
                            GoFishItems.PORTAL_PUFFER
                    ));
                }
            });
        });
    }
}
