package cc.thonly.reverie_dreams.registry.content;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.data.DrinkProperty;
import cc.thonly.reverie_dreams.api.DrinkPropertyLoaderCallback;
import cc.thonly.reverie_dreams.item.base.DrinkItem;
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

    public static void registerDefaultItemUsingProperty() {
        DrinkPropertyLoaderCallback.EVENT.register((world, user, property) -> {
            if (world.isClientSide) {
                return;
            }
            if (property.is(DrinkProperties.LOW_ALCOHOL)) {
                user.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 3 * 20));
            }
            if (property.is(DrinkProperties.MID_ALCOHOL)) {
                user.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 9 * 20));
            }
            if (property.is(DrinkProperties.HIGH_ALCOHOL)) {
                user.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 27 * 20));
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

    @SuppressWarnings("unchecked")
    private static <T extends DrinkProperty> T register(String name, Supplier<T> factory) {
        T property = factory.get();
        property.setId(ReverieDreams.id(name));
        return (T) RegistryHandlers.registerForBuiltin(RegistryHandlers.DRINK_PROPERTY, ReverieDreams.id(name), property);
    }

    public static void reload(ResourceManager manager) {
        Map<ResourceLocation, DrinkProperty> map = RegistryHandlers.DRINK_PROPERTY.entrySet().stream()
                .collect(Collectors.toMap(
                        entry -> entry.getKey().location(),
                        Map.Entry::getValue
                ));
        Set<Map.Entry<ResourceLocation, DrinkProperty>> entries = map.entrySet();
        entries.forEach((es) -> es.getValue().getItems().clear());

        Map<ResourceLocation, Resource> resources = manager.listResources("drink_property", id ->
                id.getNamespace().equals(ReverieDreams.MOD_ID) && id.getPath().endsWith(".json")
        );

        for (Map.Entry<ResourceLocation, Resource> entry : resources.entrySet()) {
            ResourceLocation resourceId = entry.getKey();
            ResourceLocation key = ResourceLocation.fromNamespaceAndPath(resourceId.getNamespace(), resourceId.getPath().replace("drink_property/", "").replace(".json", ""));
            Resource resource = entry.getValue();
            DrinkProperty property = RegistryHandlers.DRINK_PROPERTY.getValue(key);

            if (property == null) {
                ReverieDreams.LOGGER.warn("Unknown DrinkProperty id: {}", resourceId);
                continue;
            }

            try (InputStream stream = resource.open()) {
                JsonElement json = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
                Dynamic<JsonElement> input = new Dynamic<>(JsonOps.INSTANCE, json);

                DataResult<DrinkProperty> result = DrinkProperty.CODEC.parse(input);

                result.resultOrPartial(error -> ReverieDreams.LOGGER.warn("Failed to parse tags for {}: {}", resourceId, error))
                        .ifPresent(data -> {
                            property.getItems().addAll(data.getItems());
                        });

            } catch (IOException e) {
                ReverieDreams.LOGGER.error("Failed to load drink_property {}: {}", resourceId, e.getMessage(), e);
            }
        }

        Map<Item, Set<DrinkProperty>> itemDrinkPropertyCached = DrinkItem.ITEM_DRINK_CACHED;
        itemDrinkPropertyCached.clear();
        for (Map.Entry<ResourceLocation, DrinkProperty> entry : entries) {
            DrinkProperty property = entry.getValue();
            Set<Item> tags = property.getItems();
            for (Item item : tags) {
                itemDrinkPropertyCached.computeIfAbsent(item, k -> new HashSet<>())
                        .add(property);
            }
        }
        log.info("Ingredients TAG loading completed");

        Map<Item, Integer> priceCalculationTable = DrinkItem.PRICE_CALCULATION_TABLE;
        priceCalculationTable.clear();
        for (Map.Entry<Item, Set<DrinkProperty>> entry : itemDrinkPropertyCached.entrySet()) {
            int cost = 8;
            Item item = entry.getKey();
            Set<DrinkProperty> drinkProperties = entry.getValue();
            cost += drinkProperties.size() * 2;
            priceCalculationTable.put(item, cost);
        }
    }

    public static void bootstrap(RegistryHandler<DrinkProperty> registry) {

    }
}
