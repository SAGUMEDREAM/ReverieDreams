package cc.thonly.reverie_dreams.registry.content;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.api.FoodPropertyLoaderCallback;
import cc.thonly.reverie_dreams.data.FoodProperty;
import cc.thonly.reverie_dreams.item.base.IngredientItem;
import cc.thonly.reverie_dreams.registry.RegistryHandlers;
import cc.thonly.reverie_dreams.registry.impl.RegistryHandler;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Item;

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

    public static void registerDefaultItemUsingProperty() {
        FoodPropertyLoaderCallback.EVENT.register((world, user, property) -> {
            if (world.isClientSide()) {
                return;
            }
            if (property.is(FoodProperties.COOL)) {
                user.setSharedFlagOnFire(false);
                user.setRemainingFireTicks(0);
            }
            if (property.is(FoodProperties.SPICY)) {
                user.setSharedFlagOnFire(true);
                user.setRemainingFireTicks(2 * 20);
            }
            if (property.is(FoodProperties.BIZARRE)) {
                user.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 2 * 20, 1));
            }
            if (property.is(FoodProperties.GOURMET)) {
                user.addEffect(new MobEffectInstance(MobEffects.HASTE, 25 * 20, 1));
            }
            if (property.is(FoodProperties.MOUNTAIN_DELICACY)) {
                user.addEffect(new MobEffectInstance(MobEffects.LUCK, 90 * 20, 1));
            }
            if (property.is(FoodProperties.PHOTOGENIC)) {
                user.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 60 * 20, 1));
            }
            if (property.is(FoodProperties.CURSE)) {
                user.addEffect(new MobEffectInstance(MobEffects.BAD_OMEN, 120 * 20, 1));
            }
            if (property.is(FoodProperties.TOXIC)) {
                user.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 60 * 20, 1));
            }
            if (property.is(FoodProperties.LARGE_PARTITION)) {
                user.addEffect(new MobEffectInstance(MobEffects.SATURATION, 1, 1));
            }
            if (property.is(FoodProperties.SWEET)) {
                user.addEffect(new MobEffectInstance(MobEffects.SPEED, 35 * 20));
            }
            if (property.is(FoodProperties.UNBELIEVABLE)) {
                user.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 35 * 20));
            }
            if (property.is(FoodProperties.LEGENDARY)) {
                user.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 35 * 20));
            }
            if (property.is(FoodProperties.FILLING)) {
                user.addEffect(new MobEffectInstance(MobEffects.SATURATION, 1, 1));
            }
            if (property.is(FoodProperties.POWER_SURGE)) {
                user.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 45 * 20));
            }
            if (property.is(FoodProperties.OCEAN_FLAVOR)) {
                user.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 30 * 20));
            }
            if (property.is(FoodProperties.DARK_CUISINE)) {
                user.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 15 * 20));
                user.addEffect(new MobEffectInstance(MobEffects.POISON, 10 * 20));
            }
        });
    }

    @SuppressWarnings("unchecked")
    private static <T extends FoodProperty> T register(String name, Supplier<T> factory) {
        T property = factory.get();
        property.setId(ReverieDreams.id(name));
        return (T) RegistryHandlers.registerForBuiltin(RegistryHandlers.FOOD_PROPERTY, ReverieDreams.id(name), property);
    }

    public static void bootstrap(RegistryHandler<FoodProperty> registry) {

    }

    public static void reload(ResourceManager manager) {
        Map<ResourceLocation, FoodProperty> map = RegistryHandlers.FOOD_PROPERTY.entrySet().stream()
                .collect(Collectors.toMap(
                        entry -> entry.getKey().location(),
                        Map.Entry::getValue
                ));
        Set<Map.Entry<ResourceLocation, FoodProperty>> entries = map.entrySet();
        entries.forEach((es) -> es.getValue().getItems().clear());

        Map<ResourceLocation, Resource> resources = manager.listResources("food_property", id ->
                id.getNamespace().equals(ReverieDreams.MOD_ID) && id.getPath().endsWith(".json")
        );

        for (Map.Entry<ResourceLocation, Resource> entry : resources.entrySet()) {
            ResourceLocation resourceId = entry.getKey();
            ResourceLocation key = ResourceLocation.fromNamespaceAndPath(resourceId.getNamespace(), resourceId.getPath().replace("food_property/", "").replace(".json", ""));
            Resource resource = entry.getValue();
            FoodProperty property = RegistryHandlers.FOOD_PROPERTY.getValue(key);

            if (property == null) {
                ReverieDreams.LOGGER.warn("Unknown FoodProperty id: {}", resourceId);
                continue;
            }

            try (InputStream stream = resource.open()) {
                JsonElement json = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
                Dynamic<JsonElement> input = new Dynamic<>(JsonOps.INSTANCE, json);

                DataResult<FoodProperty> result = FoodProperty.CODEC.parse(input);

                result.resultOrPartial(error -> ReverieDreams.LOGGER.warn("Failed to parse tags for {}: {}", resourceId, error))
                        .ifPresent(data -> {
                            property.getItems().addAll(data.getItems());
                        });

            } catch (IOException e) {
                ReverieDreams.LOGGER.error("Failed to load food_property {}: {}", resourceId, e.getMessage(), e);
            }
        }

        Map<Item, Set<FoodProperty>> itemIngredientCached = IngredientItem.ITEM_INGREDIENT_CACHED;
        itemIngredientCached.clear();
        for (Map.Entry<ResourceLocation, FoodProperty> entry : entries) {
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
