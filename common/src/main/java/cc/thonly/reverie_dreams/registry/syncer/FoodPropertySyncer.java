package cc.thonly.reverie_dreams.registry.syncer;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.api.registry.callback.FoodPropertiesLoaderCallback;
import cc.thonly.reverie_dreams.data.FoodProperty;
import cc.thonly.reverie_dreams.registry.BuiltInRegistryProviders;
import cc.thonly.reverie_dreams.registry.content.FoodProperties;
import cc.thonly.reverie_dreams.registry.impl.RegistryProvider;
import cc.thonly.reverie_dreams.registry.impl.RegistrySyncer;
import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import org.jspecify.annotations.Nullable;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

@Slf4j
public class FoodPropertySyncer implements Supplier<RegistrySyncer<FoodProperty, FoodProperty.Data>> {
    @Override
    public RegistrySyncer<FoodProperty, FoodProperty.Data> get() {
        return new Impl(
                BuiltInRegistryProviders.FOOD_PROPERTY,
                FoodProperty.Data.CODEC,
                new RegistrySyncer.ClientReloadListener<>() {
                    private final Map<FoodProperty, Set<Item>> propertyItemMap =
                            new Object2ObjectLinkedOpenHashMap<>();

                    @Override
                    public void preProcessing(RegistryProvider<FoodProperty> registry) {
                        BuiltInRegistryProviders.FOOD_PROPERTY.clear();
                        FoodProperties.unbound();
                        this.propertyItemMap.clear();
                    }

                    @Override
                    public FoodProperty update(Identifier key,
                                               @Nullable FoodProperty old,
                                               FoodProperty.Data data) {

                        FoodProperty property = BuiltInRegistryProviders.FOOD_PROPERTY.getValue(key);

                        if (property == null) {
                            log.warn("Unknown food property {}", key);
                            return null;
                        }

                        Set<Item> callbackItems = new HashSet<>();

                        FoodPropertiesLoaderCallback.EVENT.invoker().modify(new FoodPropertiesLoaderCallback.Context() {
                            @Override
                            public FoodProperty getProperty() {
                                return property;
                            }

                            @Override
                            public Set<Item> getItems() {
                                return callbackItems;
                            }
                        });

                        Set<Item> items = propertyItemMap.computeIfAbsent(
                                property,
                                p -> new LinkedHashSet<>()
                        );

                        items.addAll(data.items());
                        items.addAll(callbackItems);

                        return property;
                    }

                    @Override
                    public void afterProcessing(RegistryProvider<FoodProperty> registry) {
                        propertyItemMap.forEach((property, items) -> {
                            for (Item item : items) {
                                FoodProperties.ITEM_CACHE.computeIfAbsent(item, k -> new LinkedHashSet<>())
                                        .add(property);
                            }
                            FoodProperties.PROPERTY_CACHE.computeIfAbsent(property, p -> new LinkedHashSet<>()).addAll(items);
                        });
                        ReverieDreams.LOGGER.info("Food TAG loading completed");
                    }

                    @Override
                    public RegistrySyncer<FoodProperty, FoodProperty.Data> getSyncer() {
                        return BuiltInRegistryProviders.FOOD_PROPERTY.getSyncer();
                    }
                }
        );
    }
    public static class Impl extends RegistrySyncer<FoodProperty, FoodProperty.Data>{
        public Impl(RegistryProvider<FoodProperty> registry, Codec<FoodProperty.Data> dataCodec, ClientReloadListener<FoodProperty, FoodProperty.Data> clientReloadListener) {
            super(registry, dataCodec, clientReloadListener);
        }

        @Override
        public FoodProperty.Data toD(FoodProperty property) {
            return new FoodProperty.Data(property.getId(), FoodProperties.PROPERTY_CACHE.getOrDefault(property, new LinkedHashSet<>()).stream().toList());
        }

        @Override
        public FoodProperty toT(FoodProperty.Data data) {
            return BuiltInRegistryProviders.FOOD_PROPERTY.getValue(data.id());
        }
    }
}