package cc.thonly.mystias_izakaya.registry;

import cc.thonly.mystias_izakaya.MystiasIzakaya;
import cc.thonly.mystias_izakaya.component.FoodProperty;
import cc.thonly.mystias_izakaya.item.base.IngredientItem;
import cc.thonly.reverie_dreams.registry.IntrinsicalRegister;
import cc.thonly.reverie_dreams.registry.RegistryManager;
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
import java.util.stream.Collectors;

@SuppressWarnings("Convert2MethodRef")
@Slf4j
public class FoodProperties {
    public static final FoodProperty UNDEFINED = register("undefined", () -> new FoodProperty());
    public static final FoodProperty MEAT = register("meat", () -> new FoodProperty());
    public static final FoodProperty AQUATIC_PRODUCTS = register("aquatic_products", () -> new FoodProperty());
    public static final FoodProperty VEGETARIAN = register("vegetarian", () -> new FoodProperty());
    public static final FoodProperty HOMESTYLE = register("homestyle", () -> new FoodProperty());
    public static final FoodProperty GOURMET = register("gourmet", () -> new FoodProperty());
    public static final FoodProperty LEGENDARY = register("legendary", () -> new FoodProperty());
    public static final FoodProperty GREASY = register("greasy", () -> new FoodProperty());
    public static final FoodProperty LIGHT = register("light", () -> new FoodProperty());
    public static final FoodProperty GOOD_WITH_ALCOHOL = register("good_with_alcohol", () -> new FoodProperty());
    public static final FoodProperty FILLING = register("filling", () -> new FoodProperty());
    public static final FoodProperty MOUNTAIN_DELICACY = register("mountain_delicacy", () -> new FoodProperty());
    public static final FoodProperty OCEAN_FLAVOR = register("ocean_flavor", () -> new FoodProperty());
    public static final FoodProperty JAPANESE_STYLE = register("japanese_style", () -> new FoodProperty());
    public static final FoodProperty WESTERN_STYLE = register("western_style", () -> new FoodProperty());
    public static final FoodProperty CHINESE_STYLE = register("chinese_style", () -> new FoodProperty());
    public static final FoodProperty SALTY = register("salty", () -> new FoodProperty());
    public static final FoodProperty UMAMI = register("umami", () -> new FoodProperty());
    public static final FoodProperty SWEET = register("sweet", () -> new FoodProperty());
    public static final FoodProperty RAW = register("raw", () -> new FoodProperty());
    public static final FoodProperty PHOTOGENIC = register("photogenic", () -> new FoodProperty());
    public static final FoodProperty COOL = register("cool", () -> new FoodProperty());
    public static final FoodProperty FIERY = register("fiery", () -> new FoodProperty());
    public static final FoodProperty POWER_SURGE = register("power_surge", () -> new FoodProperty());
    public static final FoodProperty BIZARRE = register("bizarre", () -> new FoodProperty());
    public static final FoodProperty CULTURAL_DEPTH = register("cultural_depth", () -> new FoodProperty());
    public static final FoodProperty MUSHROOMS = register("mushrooms", () -> new FoodProperty());
    public static final FoodProperty UNBELIEVABLE = register("unbelievable", () -> new FoodProperty());
    public static final FoodProperty PETITE = register("petite", () -> new FoodProperty());
    public static final FoodProperty DREAMLIKE = register("dreamlike", () -> new FoodProperty());
    public static final FoodProperty LOCAL_SPECIALTY = register("local_specialty", () -> new FoodProperty());
    public static final FoodProperty FRUITY = register("fruity", () -> new FoodProperty());
    public static final FoodProperty SOUP_AND_STEW = register("soup_and_stew", () -> new FoodProperty());
    public static final FoodProperty GRILLED = register("grilled", () -> new FoodProperty());
    public static final FoodProperty SPICY = register("spicy", () -> new FoodProperty());
    public static final FoodProperty FLAMING = register("flaming", () -> new FoodProperty());
    public static final FoodProperty SOUR = register("sour", () -> new FoodProperty());
    public static final FoodProperty TOXIC = register("toxic", () -> new FoodProperty());
    public static final FoodProperty DARK_CUISINE = register("dark_cuisine", () -> new FoodProperty());
    public static final FoodProperty ECONOMICAL = register("economical", () -> new FoodProperty());
    public static final FoodProperty EXPENSIVE = register("expensive", () -> new FoodProperty());
    public static final FoodProperty LARGE_PARTITION = register("large_partition", () -> new FoodProperty());
    public static final FoodProperty POPULAR_NEGATIVE = register("popular_pegative", () -> new FoodProperty());
    public static final FoodProperty POPULAR_POSITIVE = register("popular_positive", () -> new FoodProperty());
    public static final FoodProperty SIGNATURE = register("signature", () -> new FoodProperty());
    public static final FoodProperty CURSE = register("curse", () -> new FoodProperty());

    @SuppressWarnings("unchecked")
    private static <T extends FoodProperty> T register(String name, Supplier<T> factory) {
        T property = factory.get();
        property.setId(MystiasIzakaya.id(name));
        return (T) RegistryManager.registerForBuiltin(MIRegistryManager.FOOD_PROPERTY, MystiasIzakaya.id(name), property);
    }

    public static void bootstrap(IntrinsicalRegister<FoodProperty> registry) {

    }

    public static void reload(ResourceManager manager) {
        Map<Identifier, FoodProperty> map = MIRegistryManager.FOOD_PROPERTY.getEntrySet().stream()
                .collect(Collectors.toMap(
                        entry -> entry.getKey().getValue(),
                        Map.Entry::getValue
                ));
        Set<Map.Entry<Identifier, FoodProperty>> entries = map.entrySet();
        entries.forEach((es) -> es.getValue().getItems().clear());

        Map<Identifier, Resource> resources = manager.findResources("food_property", id ->
                id.getNamespace().equals(MystiasIzakaya.MOD_ID) && id.getPath().endsWith(".json")
        );

        for (Map.Entry<Identifier, Resource> entry : resources.entrySet()) {
            Identifier resourceId = entry.getKey();
            Identifier key = Identifier.of(resourceId.getNamespace(), resourceId.getPath().replace("food_property/", "").replace(".json", ""));
            Resource resource = entry.getValue();
            FoodProperty property = MIRegistryManager.FOOD_PROPERTY.get(key);

            if (property == null) {
                MystiasIzakaya.LOGGER.warn("Unknown FoodProperty id: {}", resourceId);
                continue;
            }

            try (InputStream stream = resource.getInputStream()) {
                JsonElement json = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
                Dynamic<JsonElement> input = new Dynamic<>(JsonOps.INSTANCE, json);

                DataResult<FoodProperty> result = FoodProperty.CODEC.parse(input);

                result.resultOrPartial(error -> MystiasIzakaya.LOGGER.warn("Failed to parse tags for {}: {}", resourceId, error))
                        .ifPresent(data -> {
                            property.getItems().addAll(data.getItems());
                        });

            } catch (IOException e) {
                MystiasIzakaya.LOGGER.error("Failed to load food_property {}: {}", resourceId, e.getMessage(), e);
            }
        }

        Map<Item, Set<FoodProperty>> itemIngredientCached = IngredientItem.ITEM_INGREDIENT_CACHED;
        itemIngredientCached.clear();
        for (Map.Entry<Identifier, FoodProperty> entry : entries) {
            FoodProperty property = entry.getValue();
            Set<Item> tags = property.getItems();
            for (Item item : tags) {
                itemIngredientCached
                        .computeIfAbsent(item, k -> new HashSet<>())
                        .add(property);
            }
        }
        log.info("Ingredients TAG loading completed");
    }
}
