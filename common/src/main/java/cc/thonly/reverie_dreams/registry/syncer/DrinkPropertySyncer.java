package cc.thonly.reverie_dreams.registry.syncer;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.api.registry.DrinkPropertiesLoaderCallback;
import cc.thonly.reverie_dreams.api.registry.FoodPropertiesLoaderCallback;
import cc.thonly.reverie_dreams.data.DrinkProperty;
import cc.thonly.reverie_dreams.data.FoodProperty;
import cc.thonly.reverie_dreams.registry.RegistryImpls;
import cc.thonly.reverie_dreams.registry.content.DrinkProperties;
import cc.thonly.reverie_dreams.registry.content.FoodProperties;
import cc.thonly.reverie_dreams.registry.impl.RegistryImpl;
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
public class DrinkPropertySyncer implements Supplier<RegistrySyncer<DrinkProperty, DrinkProperty.Data>> {
    @Override
    public RegistrySyncer<DrinkProperty, DrinkProperty.Data> get() {
        return new Impl(
                RegistryImpls.DRINK_PROPERTY,
                DrinkProperty.Data.CODEC,
                new RegistrySyncer.ClientReloadListener<>() {
                    private final Map<DrinkProperty, Set<Item>> propertyItemMap =
                            new Object2ObjectLinkedOpenHashMap<>();

                    @Override
                    public void preProcessing(RegistryImpl<DrinkProperty> registry) {
                        RegistryImpls.DRINK_PROPERTY.clear();
                        DrinkProperties.unbound();
                        this.propertyItemMap.clear();
                    }

                    @Override
                    public DrinkProperty update(Identifier key,
                                                @Nullable DrinkProperty old,
                                                DrinkProperty.Data data) {

                        DrinkProperty property = RegistryImpls.DRINK_PROPERTY.getValue(key);

                        if (property == null) {
                            ReverieDreams.LOGGER.warn("Unknown food property {}", key);
                            return null;
                        }

                        Set<Item> callbackItems = new HashSet<>();

                        DrinkPropertiesLoaderCallback.EVENT.invoker().modify(new DrinkPropertiesLoaderCallback.Context() {
                            @Override
                            public DrinkProperty getProperty() {
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
                    public void afterProcessing(RegistryImpl<DrinkProperty> registry) {
                        propertyItemMap.forEach((property, items) -> {
                            for (Item item : items) {
                                DrinkProperties.ITEM_CACHE.computeIfAbsent(item, k -> new LinkedHashSet<>())
                                        .add(property);
                            }
                            DrinkProperties.PROPERTY_CACHE.computeIfAbsent(property, p -> new LinkedHashSet<>()).addAll(items);
                        });

                        ReverieDreams.LOGGER.info("Food TAG loading completed");
                    }

                    @Override
                    public RegistrySyncer<DrinkProperty, DrinkProperty.Data> getSyncer() {
                        return RegistryImpls.DRINK_PROPERTY.getSyncer();
                    }
                }
        );
    }

    public static class Impl extends RegistrySyncer<DrinkProperty, DrinkProperty.Data>{
        public Impl(RegistryImpl<DrinkProperty> registry, Codec<DrinkProperty.Data> dataCodec, ClientReloadListener<DrinkProperty, DrinkProperty.Data> clientReloadListener) {
            super(registry, dataCodec, clientReloadListener);
        }

        @Override
        public DrinkProperty.Data toD(DrinkProperty property) {
            return new DrinkProperty.Data(property.getId(), DrinkProperties.PROPERTY_CACHE.getOrDefault(property, new LinkedHashSet<>()).stream().toList());
        }

        @Override
        public DrinkProperty toT(DrinkProperty.Data data) {
            return RegistryImpls.DRINK_PROPERTY.getValue(data.id());
        }
    }
}