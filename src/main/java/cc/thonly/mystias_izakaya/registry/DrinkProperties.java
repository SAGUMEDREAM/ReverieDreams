package cc.thonly.mystias_izakaya.registry;

import cc.thonly.mystias_izakaya.MystiasIzakaya;
import cc.thonly.mystias_izakaya.component.DrinkProperty;
import cc.thonly.mystias_izakaya.component.FoodProperty;
import cc.thonly.mystias_izakaya.item.base.DrinkItem;
import cc.thonly.mystias_izakaya.item.base.IngredientItem;
import cc.thonly.reverie_dreams.registry.RegistryManager;
import cc.thonly.reverie_dreams.registry.StandaloneRegistry;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.item.Item;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

@SuppressWarnings("Convert2MethodRef")
@Slf4j
public class DrinkProperties {
    public static final DrinkProperty UNDEFINED = register("undefined", () -> new DrinkProperty());
    public static final DrinkProperty ALCOHOL_FREE = register("alcohol-free", () -> new DrinkProperty());
    public static final DrinkProperty LOW_ALCOHOL = register("low_alcohol", () -> new DrinkProperty());
    public static final DrinkProperty MID_ALCOHOL = register("mid_alcohol", () -> new DrinkProperty());
    public static final DrinkProperty HIGH_ALCOHOL = register("high_alcohol", () -> new DrinkProperty());
    public static final DrinkProperty CAN_ADD_ICE = register("can_add_ice", () -> new DrinkProperty());
    public static final DrinkProperty CAN_HEATED = register("can_heated", () -> new DrinkProperty());
    public static final DrinkProperty COCKTAIL = register("cocktail", () -> new DrinkProperty());
    public static final DrinkProperty WESTERN_WINE = register("western_wine", () -> new DrinkProperty());
    public static final DrinkProperty FRUIT = register("fruit", () -> new DrinkProperty());
    public static final DrinkProperty SWEET = register("sweet", () -> new DrinkProperty());
    public static final DrinkProperty BITTER = register("bitter", () -> new DrinkProperty());
    public static final DrinkProperty SOJU = register("soju", () -> new DrinkProperty());
    public static final DrinkProperty SAKE = register("sake", () -> new DrinkProperty());
    public static final DrinkProperty PUNGENT = register("pungent", () -> new DrinkProperty());
    public static final DrinkProperty BUBBLE = register("bubble", () -> new DrinkProperty());
    public static final DrinkProperty BEER = register("beer", () -> new DrinkProperty());
    public static final DrinkProperty DIRECT_DRINKING = register("direct_drinking", () -> new DrinkProperty());
    public static final DrinkProperty LIQUEUR = register("liqueur", () -> new DrinkProperty());
    public static final DrinkProperty REFRESHING = register("refreshing", () -> new DrinkProperty());
    public static final DrinkProperty CLASSICAL = register("classical", () -> new DrinkProperty());
    public static final DrinkProperty MODERN = register("modern", () -> new DrinkProperty());

    @SuppressWarnings("unchecked")
    private static <T extends DrinkProperty> T register(String name, Supplier<T> factory) {
        return (T) RegistryManager.registerFinal(MIRegistryManager.DRINK_PROPERTY, MystiasIzakaya.id(name), factory.get());
    }

    public static void reload(ResourceManager manager) {
        Set<Map.Entry<Identifier, DrinkProperty>> entries = MIRegistryManager.DRINK_PROPERTY.entrySet();
        entries.forEach((es) -> es.getValue().getItems().clear());

        Map<Identifier, Resource> resources = manager.findResources("drink_property", id ->
                id.getNamespace().equals(MystiasIzakaya.MOD_ID) && id.getPath().endsWith(".json")
        );

        for (Map.Entry<Identifier, Resource> entry : resources.entrySet()) {
            Identifier resourceId = entry.getKey();
            Identifier key = Identifier.of(resourceId.getNamespace(), resourceId.getPath().replace("drink_property/", "").replace(".json", ""));
            Resource resource = entry.getValue();
            DrinkProperty property = MIRegistryManager.DRINK_PROPERTY.get(key);

            if (property == null) {
                MystiasIzakaya.LOGGER.warn("Unknown DrinkProperty id: {}", resourceId);
                continue;
            }

            try (InputStream stream = resource.getInputStream()) {
                JsonElement json = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
                Dynamic<JsonElement> input = new Dynamic<>(JsonOps.INSTANCE, json);

                DataResult<DrinkProperty> result = DrinkProperty.CODEC.parse(input);

                result.resultOrPartial(error -> MystiasIzakaya.LOGGER.warn("Failed to parse tags for {}: {}", resourceId, error))
                        .ifPresent(data -> {
                            property.getItems().addAll(data.getItems());
                        });

            } catch (IOException e) {
                MystiasIzakaya.LOGGER.error("Failed to load drink_property {}: {}", resourceId, e.getMessage(), e);
            }
        }

        Map<Item, Set<DrinkProperty>> itemDrinkPropertyCached = DrinkItem.ITEM_DRINK_CACHED;
        itemDrinkPropertyCached.clear();
        for (Map.Entry<Identifier, DrinkProperty> entry : MIRegistryManager.DRINK_PROPERTY.entrySet()) {
            DrinkProperty property = entry.getValue();
            Set<Item> tags = property.getItems();
            for (Item item : tags) {
                itemDrinkPropertyCached
                        .computeIfAbsent(item, k -> new HashSet<>())
                        .add(property);
            }
        }
        log.info("Ingredients TAG loading completed");
    }

    public static void bootstrap(StandaloneRegistry<DrinkProperty> registry) {

    }
}
