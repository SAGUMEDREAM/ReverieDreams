package cc.thonly.reverie_dreams.fabric.compat;

import cc.thonly.reverie_dreams.api.registry.FoodPropertiesLoaderCallback;
import cc.thonly.reverie_dreams.data.FoodProperty;
import cc.thonly.reverie_dreams.registry.content.FoodProperties;
import net.macck209.fishing101.registries.ItemRegistry;
import net.minecraft.world.item.Item;

import java.util.List;
import java.util.Set;

@SuppressWarnings("unchecked")
public class Fishing101CompatImpl {
    public static void bootstrap() {
        FoodPropertiesLoaderCallback.EVENT.register(ctx -> {
            FoodProperty property = ctx.getProperty();
            Set<Item> items = ctx.getItems();
            if (property.is(FoodProperties.AQUATIC_PRODUCTS)) {
                items.addAll(List.of(
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
    }
}
