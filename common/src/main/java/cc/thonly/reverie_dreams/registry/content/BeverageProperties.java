package cc.thonly.reverie_dreams.registry.content;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.api.item.callback.BeveragePropertyItemUseCallback;
import cc.thonly.reverie_dreams.api.registry.callback.BeveragePropertiesLoaderCallback;
import cc.thonly.reverie_dreams.data.BeverageProperty;
import cc.thonly.reverie_dreams.item.IngredientStack;
import cc.thonly.reverie_dreams.registry.BuiltInRegistryProviders;
import cc.thonly.reverie_dreams.registry.content.component.RDDataComponentTypes;
import cc.thonly.reverie_dreams.registry.content.effect.RDStatusEffects;
import cc.thonly.reverie_dreams.registry.content.item.RDBeverageItems;
import cc.thonly.reverie_dreams.registry.impl.RegistryProvider;
import cc.thonly.reverie_dreams.util.item.ItemStackTemplateHelper;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import cc.thonly.keine.item.ItemStackTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Supplier;

@SuppressWarnings("Convert2MethodRef")
@Slf4j
public class BeverageProperties {
    public static final Map<Item, Set<BeverageProperty>> ITEM_CACHE = new Object2ObjectLinkedOpenHashMap<>();
    public static final Map<BeverageProperty, Set<Item>> PROPERTY_CACHE = new Object2ObjectLinkedOpenHashMap<>();
    public static final Map<Item, Integer> PRICE_CALCULATION_TABLE = new Object2ObjectOpenHashMap<>();
    public static final BeverageProperty UNDEFINED = registerForBuiltIn("undefined", () -> new BeverageProperty());
    public static final BeverageProperty ALCOHOL_FREE = registerForBuiltIn("alcohol-free", () -> new BeverageProperty());
    public static final BeverageProperty LOW_ALCOHOL = registerForBuiltIn("low_alcohol", () -> new BeverageProperty());
    public static final BeverageProperty MID_ALCOHOL = registerForBuiltIn("mid_alcohol", () -> new BeverageProperty());
    public static final BeverageProperty HIGH_ALCOHOL = registerForBuiltIn("high_alcohol", () -> new BeverageProperty());
    public static final BeverageProperty CAN_ADD_ICE = registerForBuiltIn("can_add_ice", () -> new BeverageProperty());
    public static final BeverageProperty CAN_HEATED = registerForBuiltIn("can_heated", () -> new BeverageProperty());
    public static final BeverageProperty COCKTAIL = registerForBuiltIn("cocktail", () -> new BeverageProperty());
    public static final BeverageProperty WESTERN_WINE = registerForBuiltIn("western_wine", () -> new BeverageProperty());
    public static final BeverageProperty FRUIT = registerForBuiltIn("fruit", () -> new BeverageProperty());
    public static final BeverageProperty SWEET = registerForBuiltIn("sweet", () -> new BeverageProperty());
    public static final BeverageProperty BITTER = registerForBuiltIn("bitter", () -> new BeverageProperty());
    public static final BeverageProperty SOJU = registerForBuiltIn("soju", () -> new BeverageProperty());
    public static final BeverageProperty SAKE = registerForBuiltIn("sake", () -> new BeverageProperty());
    public static final BeverageProperty PUNGENT = registerForBuiltIn("pungent", () -> new BeverageProperty());
    public static final BeverageProperty BUBBLE = registerForBuiltIn("bubble", () -> new BeverageProperty());
    public static final BeverageProperty BEER = registerForBuiltIn("beer", () -> new BeverageProperty());
    public static final BeverageProperty DIRECT_DRINKING = registerForBuiltIn("direct_drinking", () -> new BeverageProperty());
    public static final BeverageProperty LIQUEUR = registerForBuiltIn("liqueur", () -> new BeverageProperty());
    public static final BeverageProperty REFRESHING = registerForBuiltIn("refreshing", () -> new BeverageProperty());
    public static final BeverageProperty CLASSICAL = registerForBuiltIn("classical", () -> new BeverageProperty());
    public static final BeverageProperty MODERN = registerForBuiltIn("modern", () -> new BeverageProperty());

    public static void registerDefaultItemUsingProperty() {
        BeveragePropertyItemUseCallback.EVENT.register((world, user, itemStack, property, effectInstances, negativeEffectInstances) -> {
            if (world.isClientSide()) {
                return;
            }
            RandomSource random = user.getRandom();
            if (itemStack.is(RDBeverageItems.MILK)) {
                effectInstances.add(new MobEffectInstance(RDStatusEffects.ANTI_ALCOHOL, 60 * 20 + 20 * 15, 0, false, true));
            }

            if (property.is(BeverageProperties.LOW_ALCOHOL)) {
                effectInstances.add(new MobEffectInstance(MobEffects.REGENERATION, 5 * 20, 0));
                negativeEffectInstances.add(new MobEffectInstance(MobEffects.NAUSEA, 5 * 20, 0));
            }

            if (property.is(BeverageProperties.MID_ALCOHOL)) {
                effectInstances.add(new MobEffectInstance(MobEffects.STRENGTH, 10 * 20, 0));
                negativeEffectInstances.add(new MobEffectInstance(MobEffects.NAUSEA, 10 * 20, 0));
                negativeEffectInstances.add(new MobEffectInstance(MobEffects.WEAKNESS, 5 * 20, 0));
            }

            if (property.is(BeverageProperties.HIGH_ALCOHOL)) {
                effectInstances.add(new MobEffectInstance(MobEffects.STRENGTH, 12 * 20, 1));
                effectInstances.add(new MobEffectInstance(MobEffects.RESISTANCE, 12 * 20, 0));
                negativeEffectInstances.add(new MobEffectInstance(MobEffects.NAUSEA, 15 * 20, 1));
                negativeEffectInstances.add(new MobEffectInstance(MobEffects.BLINDNESS, 5 * 20, 0));
            }

            if (property.is(BeverageProperties.CAN_ADD_ICE)) {
                user.clearFire();
                negativeEffectInstances.add(new MobEffectInstance(MobEffects.SLOWNESS, 5 * 20, 0));
            }

            if (property.is(BeverageProperties.SWEET)) {
                effectInstances.add(new MobEffectInstance(MobEffects.SPEED, 8 * 20, 1));
                negativeEffectInstances.add(new MobEffectInstance(MobEffects.HUNGER, 10 * 20, 0));
            }

            if (property.is(BeverageProperties.REFRESHING)) {
                effectInstances.add(new MobEffectInstance(MobEffects.SPEED, 10 * 20, 0));
                effectInstances.add(new MobEffectInstance(MobEffects.HASTE, 10 * 20, 0));
            }

            if (property.is(BeverageProperties.BITTER)) {
                effectInstances.add(new MobEffectInstance(MobEffects.HASTE, 10 * 20, 1));
                negativeEffectInstances.add(new MobEffectInstance(MobEffects.MINING_FATIGUE, 5 * 20, 0));
            }

            if (property.is(BeverageProperties.BUBBLE)) {
                effectInstances.add(new MobEffectInstance(MobEffects.JUMP_BOOST, 10 * 20, 1));
            }

            if (property.is(BeverageProperties.COCKTAIL)) {
                if (random.nextBoolean()) {
                    effectInstances.add(new MobEffectInstance(MobEffects.LUCK, 15 * 20, 0));
                } else {
                    negativeEffectInstances.add(new MobEffectInstance(MobEffects.UNLUCK, 10 * 20, 0));
                }
            }
        });
    }

    public static List<BeverageProperty> get(IngredientStack stack) {
        List<BeverageProperty> existing = stack.getOrDefault(RDDataComponentTypes.BEVERAGE_PROPERTIES.value(), new ArrayList<>());
        if (!existing.isEmpty()) {
            return existing;
        }

        Set<BeverageProperty> cached = ITEM_CACHE.get(stack.getItem());
        if (cached == null || cached.isEmpty()) {
            return List.of();
        }

        List<BeverageProperty> result = new ArrayList<>(cached);
        stack.set(RDDataComponentTypes.BEVERAGE_PROPERTIES.value(), result);

        return result;
    }

    public static List<BeverageProperty> get(ItemStack stack) {
        List<BeverageProperty> existing = stack.getOrDefault(RDDataComponentTypes.BEVERAGE_PROPERTIES.value(), new ArrayList<>());
        if (!existing.isEmpty()) {
            return existing;
        }

        Set<BeverageProperty> cached = ITEM_CACHE.get(stack.getItem());
        if (cached == null || cached.isEmpty()) {
            return List.of();
        }

        List<BeverageProperty> result = new ArrayList<>(cached);
        stack.set(RDDataComponentTypes.BEVERAGE_PROPERTIES.value(), result);

        return result;
    }

    public static Collection<BeverageProperty> get(ItemStackTemplate template) {
        Holder<Item> item = template.item();
        List<BeverageProperty> existing = template.get(RDDataComponentTypes.BEVERAGE_PROPERTIES.value());
        if (existing == null) {
            existing = new ArrayList<>();
        }
        if (!existing.isEmpty()) {
            return existing;
        }

        Set<BeverageProperty> cached = ITEM_CACHE.get(item.value());
        if (cached == null || cached.isEmpty()) {
            return List.of();
        }

        List<BeverageProperty> result = new ArrayList<>(cached);
        ItemStackTemplateHelper.modify(template, (source, modifier) -> {
            modifier.set(RDDataComponentTypes.BEVERAGE_PROPERTIES.value(), result);
        });
        return result;
    }

    public static Map<Item, Integer> getPriceCalculationTable() {
        return Map.copyOf(PRICE_CALCULATION_TABLE);
    }

    @SuppressWarnings("unchecked")
    private static <T extends BeverageProperty> T register(String name, Supplier<T> factory) {
        T property = factory.get();
        property.setId(ReverieDreams.id(name));
        return (T) BuiltInRegistryProviders.register(BuiltInRegistryProviders.BEVERAGE_PROPERTY, ReverieDreams.id(name), property);
    }

    @SuppressWarnings("unchecked")
    private static <T extends BeverageProperty> T registerForBuiltIn(String name, Supplier<T> factory) {
        T property = factory.get();
        property.setId(ReverieDreams.id(name));
        return (T) BuiltInRegistryProviders.registerForBuiltin(BuiltInRegistryProviders.BEVERAGE_PROPERTY, ReverieDreams.id(name), property);
    }

    public static void reload(ResourceManager manager) {
        unbound();
        List<BeverageProperty.Data> drinkPropertyData = new ArrayList<>();

        Map<Identifier, Resource> resources = manager.listResources("beverage_property", id ->
                id.getPath().endsWith(".json")
        );

        for (Map.Entry<Identifier, Resource> entry : resources.entrySet()) {
            Identifier resourceId = entry.getKey();
            Identifier key = Identifier.fromNamespaceAndPath(resourceId.getNamespace(), resourceId.getPath().replace("beverage_property/", "").replace(".json", ""));
            Resource resource = entry.getValue();
            BeverageProperty property = BuiltInRegistryProviders.BEVERAGE_PROPERTY.getValue(key);

            if (property == null) {
                ReverieDreams.LOGGER.warn("Unknown DrinkProperty id: {}", resourceId);
                continue;
            }

            try (InputStream stream = resource.open()) {
                JsonElement json = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
                Dynamic<JsonElement> input = new Dynamic<>(JsonOps.INSTANCE, json);

                DataResult<BeverageProperty.Data> result = BeverageProperty.Data.CODEC.parse(input);

                result.resultOrPartial(error -> log.warn("Failed to parse tags for {}: {}", resourceId, error))
                        .ifPresent(drinkPropertyData::add);

            } catch (IOException e) {
                log.error("Failed to load beverage_property {}: {}", resourceId, e.getMessage(), e);
            }
        }
        Map<BeverageProperty, Set<Item>> drinkPropertySetMap = new Object2ObjectLinkedOpenHashMap<>();
        for (BeverageProperty.Data drinkPropertyDatum : drinkPropertyData) {
            Identifier id = drinkPropertyDatum.id();
            BeverageProperty property = BuiltInRegistryProviders.BEVERAGE_PROPERTY.getValue(id);
            if (property == null) {
                log.warn("Unknown food property {}", id);
                continue;
            }
            HashSet<Item> callbackSets = new HashSet<>();
            BeveragePropertiesLoaderCallback.EVENT.invoker().modify(new BeveragePropertiesLoaderCallback.Context() {
                @Override
                public BeverageProperty getProperty() {
                    return property;
                }

                @Override
                public Set<Item> getItems() {
                    return callbackSets;
                }
            });
            Set<Item> items = drinkPropertySetMap.computeIfAbsent(property, x -> new LinkedHashSet<>());
            items.addAll(drinkPropertyDatum.items());
            items.addAll(callbackSets);
        }
        drinkPropertySetMap.forEach((property, items) -> {
            for (Item item : items) {
                ITEM_CACHE.computeIfAbsent(item, k -> new LinkedHashSet<>())
                        .add(property);
            }
            PROPERTY_CACHE.computeIfAbsent(property, p -> new LinkedHashSet<>()).addAll(items);
        });
        ITEM_CACHE.forEach((item, drinkProperties) -> {
            int cost = 8;
            cost += drinkProperties.size() * 2;
            PRICE_CALCULATION_TABLE.put(item, cost);
        });
        log.info("Ingredients TAG loading completed");
    }

    public static void unbound() {
        BuiltInRegistryProviders.BEVERAGE_PROPERTY.clear();
        ITEM_CACHE.clear();
        PROPERTY_CACHE.clear();
        PRICE_CALCULATION_TABLE.clear();
    }

    public static void registerByPair(Pair<BeverageProperty, Collection<Item>> pair) {
        BeverageProperty property = pair.key();
        Collection<Item> itemCollection = pair.value();
        itemCollection.forEach(item -> ITEM_CACHE.computeIfAbsent(item, x -> new LinkedHashSet<>()).add(property));
        for (Item item : itemCollection) {
            Set<BeverageProperty> drinkProperties = ITEM_CACHE.get(item);
            if (drinkProperties == null) {
                continue;
            }
            int cost = 8;
            cost += drinkProperties.size() * 2;
            PRICE_CALCULATION_TABLE.put(item, cost);
        }
    }

    public static void bootstrap(RegistryProvider<BeverageProperty> registry) {

    }
}
