package cc.thonly.reverie_dreams.registry.content;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.api.item.DrinkPropertyItemUseCallback;
import cc.thonly.reverie_dreams.api.registry.DrinkPropertiesLoaderCallback;
import cc.thonly.reverie_dreams.data.DrinkProperty;
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
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
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
public class DrinkProperties {
    public static final Map<Item, Set<DrinkProperty>> ITEM_CACHE = new Object2ObjectLinkedOpenHashMap<>();
    public static final Map<DrinkProperty, Set<Item>> PROPERTY_CACHE = new Object2ObjectLinkedOpenHashMap<>();
    public static final Map<Item, Integer> PRICE_CALCULATION_TABLE = new Object2ObjectOpenHashMap<>();
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

    public static void registerDefaultItemUsingProperty() {
        DrinkPropertyItemUseCallback.EVENT.register((world, user, property) -> {
            if (world.isClientSide()) {
                return;
            }
            if (property.is(DrinkProperties.LOW_ALCOHOL)) {
                user.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 20));
            }
            if (property.is(DrinkProperties.MID_ALCOHOL)) {
                user.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 4 * 20));
            }
            if (property.is(DrinkProperties.HIGH_ALCOHOL)) {
                user.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 10 * 20));
            }
            if (property.is(DrinkProperties.CAN_ADD_ICE)) {
                user.setSharedFlagOnFire(false);
                user.setTicksFrozen(20);
            }
            if (property.is(DrinkProperties.SWEET)) {
                user.addEffect(new MobEffectInstance(MobEffects.SPEED, 10 * 20));
            }
            if (property.is(DrinkProperties.REFRESHING)) {
                user.addEffect(new MobEffectInstance(MobEffects.HASTE, 20 * 20));
                user.addEffect(new MobEffectInstance(MobEffects.SPEED, 20 * 20));
            }
            if (property.is(DrinkProperties.BITTER)) {
                user.addEffect(new MobEffectInstance(MobEffects.MINING_FATIGUE, 20));
            }
        });
    }

    public static List<DrinkProperty> get(ItemStack stack) {
        List<DrinkProperty> existing = stack.getOrDefault(RDDataComponents.DRINK_PROPERTIES.value(), new ArrayList<>());
        if (!existing.isEmpty()) {
            return existing;
        }

        Set<DrinkProperty> cached = ITEM_CACHE.get(stack.getItem());
        if (cached == null || cached.isEmpty()) {
            return List.of();
        }

        List<DrinkProperty> result = new ArrayList<>(cached);
        stack.set(RDDataComponents.DRINK_PROPERTIES.value(), result);

        return result;
    }

    public static Collection<DrinkProperty> get(ItemStackTemplate template) {
        Holder<Item> item = template.item();
        List<DrinkProperty> existing = template.getOrDefault(RDDataComponents.DRINK_PROPERTIES.value(), new ArrayList<>());
        if (!existing.isEmpty()) {
            return existing;
        }

        Set<DrinkProperty> cached = ITEM_CACHE.get(item.value());
        if (cached == null || cached.isEmpty()) {
            return List.of();
        }

        List<DrinkProperty> result = new ArrayList<>(cached);
        ItemStackTemplateHelper.modify(template, (source, modifier) -> {
            modifier.set(RDDataComponents.DRINK_PROPERTIES.value(), result);
        });
        return result;
    }

    public static Map<Item, Integer> getPriceCalculationTable() {
        return Map.copyOf(PRICE_CALCULATION_TABLE);
    }

    @SuppressWarnings("unchecked")
    private static <T extends DrinkProperty> T register(String name, Supplier<T> factory) {
        T property = factory.get();
        property.setId(ReverieDreams.id(name));
        return (T) RegistryImpls.registerForBuiltin(RegistryImpls.DRINK_PROPERTY, ReverieDreams.id(name), property);
    }

    public static void reload(ResourceManager manager) {
        unbound();
        List<DrinkProperty.Data> drinkPropertyData = new ArrayList<>();

        Map<Identifier, Resource> resources = manager.listResources("drink_property", id ->
                id.getNamespace().equals(ReverieDreams.MOD_ID) && id.getPath().endsWith(".json")
        );

        for (Map.Entry<Identifier, Resource> entry : resources.entrySet()) {
            Identifier resourceId = entry.getKey();
            Identifier key = Identifier.fromNamespaceAndPath(resourceId.getNamespace(), resourceId.getPath().replace("drink_property/", "").replace(".json", ""));
            Resource resource = entry.getValue();
            DrinkProperty property = RegistryImpls.DRINK_PROPERTY.getValue(key);

            if (property == null) {
                ReverieDreams.LOGGER.warn("Unknown DrinkProperty id: {}", resourceId);
                continue;
            }

            try (InputStream stream = resource.open()) {
                JsonElement json = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
                Dynamic<JsonElement> input = new Dynamic<>(JsonOps.INSTANCE, json);

                DataResult<DrinkProperty.Data> result = DrinkProperty.Data.CODEC.parse(input);

                result.resultOrPartial(error -> ReverieDreams.LOGGER.warn("Failed to parse tags for {}: {}", resourceId, error))
                        .ifPresent(drinkPropertyData::add);

            } catch (IOException e) {
                ReverieDreams.LOGGER.error("Failed to load drink_property {}: {}", resourceId, e.getMessage(), e);
            }
        }
        Map<DrinkProperty, Set<Item>> drinkPropertySetMap = new Object2ObjectLinkedOpenHashMap<>();
        for (DrinkProperty.Data drinkPropertyDatum : drinkPropertyData) {
            Identifier id = drinkPropertyDatum.id();
            DrinkProperty property = RegistryImpls.DRINK_PROPERTY.getValue(id);
            if (property == null) {
                log.warn("Unknown food property {}", id);
                continue;
            }
            HashSet<Item> callbackSets = new HashSet<>();
            DrinkPropertiesLoaderCallback.EVENT.invoker().modify(new DrinkPropertiesLoaderCallback.Context() {
                @Override
                public DrinkProperty getProperty() {
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
        ITEM_CACHE.clear();
        PROPERTY_CACHE.clear();
        PRICE_CALCULATION_TABLE.clear();
    }

    public static void registerByPair(Pair<DrinkProperty, Collection<Item>> pair) {
        DrinkProperty property = pair.key();
        Collection<Item> itemCollection = pair.value();
        itemCollection.forEach(item -> ITEM_CACHE.computeIfAbsent(item, x -> new LinkedHashSet<>()).add(property));
        for (Item item : itemCollection) {
            Set<DrinkProperty> drinkProperties = ITEM_CACHE.get(item);
            if (drinkProperties == null) {
                continue;
            }
            int cost = 8;
            cost += drinkProperties.size() * 2;
            PRICE_CALCULATION_TABLE.put(item, cost);
        }
    }

    public static void bootstrap(RegistryImpl<DrinkProperty> registry) {

    }
}
