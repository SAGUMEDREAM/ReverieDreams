package cc.thonly.reverie_dreams.registry.content;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.api.item.callback.FoodPropertyItemUseCallback;
import cc.thonly.reverie_dreams.api.registry.callback.FoodPropertiesLoaderCallback;
import cc.thonly.reverie_dreams.data.FoodProperty;
import cc.thonly.reverie_dreams.registry.RegistryImpls;
import cc.thonly.reverie_dreams.registry.content.component.RDDataComponents;
import cc.thonly.reverie_dreams.registry.impl.RegistryImpl;
import cc.thonly.reverie_dreams.util.item.ItemStackTemplateHelper;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Supplier;

@SuppressWarnings("Convert2MethodRef")
@Slf4j
public class FoodProperties {
    public static final Map<Item, Set<FoodProperty>> ITEM_CACHE = new Object2ObjectLinkedOpenHashMap<>();
    public static final Map<FoodProperty, Set<Item>> PROPERTY_CACHE = new Object2ObjectLinkedOpenHashMap<>();

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
        FoodPropertyItemUseCallback.EVENT.register((world, user, property) -> {
            if (world.isClientSide()) {
                return;
            }

            if (property.is(FoodProperties.COOL)) {
                user.clearFire();
                user.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 5 * 20, 0));
            }

            if (property.is(FoodProperties.SPICY)) {
                user.setRemainingFireTicks(3 * 20);
                user.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 10 * 20, 0));
                user.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 10 * 20, 0));
            }

            if (property.is(FoodProperties.BIZARRE)) {
                if (user.getRandom().nextBoolean()) {
                    user.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 20 * 20, 0));
                } else {
                    user.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 10 * 20, 0));
                }
                user.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 10 * 20, 0));
            }

            if (property.is(FoodProperties.GOURMET)) {
                user.addEffect(new MobEffectInstance(MobEffects.HASTE, 20 * 20, 0));
                user.addEffect(new MobEffectInstance(MobEffects.HUNGER, 15 * 20, 0));
            }

            if (property.is(FoodProperties.MOUNTAIN_DELICACY)) {
                user.addEffect(new MobEffectInstance(MobEffects.LUCK, 60 * 20, 0));
            }

            if (property.is(FoodProperties.PHOTOGENIC)) {
                user.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 45 * 20, 0));
                user.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 10 * 20, 0));
            }

            if (property.is(FoodProperties.CURSE)) {
                user.addEffect(new MobEffectInstance(MobEffects.BAD_OMEN, 90 * 20, 0));
            }

            if (property.is(FoodProperties.TOXIC)) {
                user.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 10 * 20, 0));
                user.addEffect(new MobEffectInstance(MobEffects.POISON, 8 * 20, 0));
            }

            if (property.is(FoodProperties.LARGE_PARTITION)) {
                user.addEffect(new MobEffectInstance(MobEffects.SATURATION, 1, 0));
                user.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 10 * 20, 0));
            }

            if (property.is(FoodProperties.SWEET)) {
                user.addEffect(new MobEffectInstance(MobEffects.SPEED, 15 * 20, 1));
                user.addEffect(new MobEffectInstance(MobEffects.HUNGER, 20 * 20, 0));
            }

            if (property.is(FoodProperties.UNBELIEVABLE)) {
                user.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 15 * 20, 1));
            }

            if (property.is(FoodProperties.LEGENDARY)) {
                user.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 60 * 20, 1));
                user.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 10 * 20, 1));
            }

            if (property.is(FoodProperties.FILLING)) {
                user.addEffect(new MobEffectInstance(MobEffects.SATURATION, 1, 0));
            }

            if (property.is(FoodProperties.POWER_SURGE)) {
                user.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 15 * 20, 1));
                user.addEffect(new MobEffectInstance(MobEffects.HASTE, 15 * 20, 0));
                user.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 20 * 20, 0));
            }

            if (property.is(FoodProperties.OCEAN_FLAVOR)) {
                user.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 30 * 20, 0));
                user.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, 10 * 20, 0));
            }

            if (property.is(FoodProperties.DARK_CUISINE)) {
                user.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 15 * 20, 1));
                user.addEffect(new MobEffectInstance(MobEffects.POISON, 10 * 20, 0));
                user.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 15 * 20, 0));
            }
        });
    }

    @SuppressWarnings("unchecked")
    private static <T extends FoodProperty> T register(String name, Supplier<T> factory) {
        T property = factory.get();
        property.setId(ReverieDreams.id(name));
        return (T) RegistryImpls.registerForBuiltin(RegistryImpls.FOOD_PROPERTY, ReverieDreams.id(name), property);
    }

    public static void bootstrap(RegistryImpl<FoodProperty> registry) {

    }

    public static Collection<FoodProperty> get(ItemStack stack) {
        List<FoodProperty> existing = stack.getOrDefault(RDDataComponents.FOOD_PROPERTIES.value(), new ArrayList<>());
        if (!existing.isEmpty()) {
            return existing;
        }

        Set<FoodProperty> cached = ITEM_CACHE.get(stack.getItem());
        if (cached == null || cached.isEmpty()) {
            return List.of();
        }

        List<FoodProperty> result = new ArrayList<>(cached);
        stack.set(RDDataComponents.FOOD_PROPERTIES.value(), result);

        return result;
    }

    public static Collection<FoodProperty> get(ItemStackTemplate template) {
        Holder<Item> item = template.item();
        List<FoodProperty> existing = ItemStackTemplateHelper.getOrDefault(template, RDDataComponents.FOOD_PROPERTIES.value(), new ArrayList<>());
        if (!existing.isEmpty()) {
            return existing;
        }

        Set<FoodProperty> cached = ITEM_CACHE.get(item.value());
        if (cached == null || cached.isEmpty()) {
            return List.of();
        }

        List<FoodProperty> result = new ArrayList<>(cached);
        ItemStackTemplateHelper.modify(template, (template1, modifier) -> {
            modifier.set(RDDataComponents.FOOD_PROPERTIES.value(), result);
        });
        return result;
    }

    public static void reload(ResourceManager manager) {
        unbound();
        Map<Identifier, Resource> resources = manager.listResources("food_property", id ->
                id.getNamespace().equals(ReverieDreams.MOD_ID) && id.getPath().endsWith(".json")
        );
        List<FoodProperty.Data> foodPropertyData = new ArrayList<>();
        for (Map.Entry<Identifier, Resource> entry : resources.entrySet()) {
            Resource resource = entry.getValue();
            try (InputStream stream = resource.open()) {
                JsonElement json = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
                Dynamic<JsonElement> input = new Dynamic<>(JsonOps.INSTANCE, json);
                DataResult<FoodProperty.Data> result = FoodProperty.Data.CODEC.parse(input);

                result.resultOrPartial(error -> ReverieDreams.LOGGER.warn("Failed to parse tags for {}: {}", entry.getKey(), error))
                        .ifPresent(foodPropertyData::add);
            } catch (IOException e) {
                ReverieDreams.LOGGER.error("Failed to load food_property {}: {}", entry.getKey(), e.getMessage(), e);
            }
        }
        Map<FoodProperty, Set<Item>> foodPropertySetMap = new Object2ObjectLinkedOpenHashMap<>();
        for (FoodProperty.Data foodPropertyDatum : foodPropertyData) {
            Identifier id = foodPropertyDatum.id();
            FoodProperty property = RegistryImpls.FOOD_PROPERTY.getValue(id);
            if (property == null) {
                log.warn("Unknown food property {}", id);
                continue;
            }
            HashSet<Item> callbackSets = new HashSet<>();
            FoodPropertiesLoaderCallback.EVENT.invoker().modify(new FoodPropertiesLoaderCallback.Context() {
                @Override
                public FoodProperty getProperty() {
                    return property;
                }

                @Override
                public Set<Item> getItems() {
                    return callbackSets;
                }
            });
            Set<Item> items = foodPropertySetMap.computeIfAbsent(property, x -> new LinkedHashSet<>());
            items.addAll(foodPropertyDatum.items());
            items.addAll(callbackSets);
        }
        foodPropertySetMap.forEach((property, items) -> {
            for (Item item : items) {
                ITEM_CACHE.computeIfAbsent(item, k -> new LinkedHashSet<>())
                        .add(property);
            }
            FoodProperties.PROPERTY_CACHE.computeIfAbsent(property, p -> new LinkedHashSet<>()).addAll(items);
        });
        log.info("Food TAG loading completed");
    }

    public static void registerByPair(Pair<FoodProperty, Collection<Item>> pair) {
        FoodProperty property = pair.key();
        Collection<Item> itemCollection = pair.value();
        itemCollection.forEach(item -> ITEM_CACHE.computeIfAbsent(item, x -> new LinkedHashSet<>()).add(property));
    }

    public static void unbound() {
        ITEM_CACHE.clear();
        PROPERTY_CACHE.clear();
    }
}
