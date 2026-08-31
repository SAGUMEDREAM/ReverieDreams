package cc.thonly.reverie_dreams.registry.syncer;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.api.registry.callback.BeveragePropertiesLoaderCallback;
import cc.thonly.reverie_dreams.data.BeverageProperty;
import cc.thonly.reverie_dreams.registry.BuiltInRegistryProviders;
import cc.thonly.reverie_dreams.registry.content.BeverageProperties;
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
public class BeveragePropertySyncer implements Supplier<RegistrySyncer<BeverageProperty, BeverageProperty.Data>> {
    @Override
    public RegistrySyncer<BeverageProperty, BeverageProperty.Data> get() {
        return new Impl(
                BuiltInRegistryProviders.BEVERAGE_PROPERTY,
                BeverageProperty.Data.CODEC,
                new RegistrySyncer.ClientReloadListener<>() {
                    private final Map<BeverageProperty, Set<Item>> propertyItemMap =
                            new Object2ObjectLinkedOpenHashMap<>();

                    @Override
                    public void preProcessing(RegistryProvider<BeverageProperty> registry) {
                        BuiltInRegistryProviders.BEVERAGE_PROPERTY.clear();
                        BeverageProperties.unbound();
                        this.propertyItemMap.clear();
                    }

                    @Override
                    public BeverageProperty update(Identifier key,
                                                   @Nullable BeverageProperty old,
                                                   BeverageProperty.Data data) {

                        BeverageProperty property = BuiltInRegistryProviders.BEVERAGE_PROPERTY.getValue(key);

                        if (property == null) {
                            log.warn("Unknown beverage property {}", key);
                            return null;
                        }

                        Set<Item> callbackItems = new HashSet<>();

                        BeveragePropertiesLoaderCallback.EVENT.invoker().modify(new BeveragePropertiesLoaderCallback.Context() {
                            @Override
                            public BeverageProperty getProperty() {
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
                    public void afterProcessing(RegistryProvider<BeverageProperty> registry) {
                        propertyItemMap.forEach((property, items) -> {
                            for (Item item : items) {
                                BeverageProperties.ITEM_CACHE.computeIfAbsent(item, k -> new LinkedHashSet<>())
                                                             .add(property);
                            }
                            BeverageProperties.PROPERTY_CACHE.computeIfAbsent(property, p -> new LinkedHashSet<>()).addAll(items);
                        });

                        ReverieDreams.LOGGER.info("Beverage TAG loading completed");
                    }

                    @Override
                    public RegistrySyncer<BeverageProperty, BeverageProperty.Data> getSyncer() {
                        return BuiltInRegistryProviders.BEVERAGE_PROPERTY.getSyncer();
                    }
                }
        );
    }

    public static class Impl extends RegistrySyncer<BeverageProperty, BeverageProperty.Data>{
        public Impl(RegistryProvider<BeverageProperty> registry, Codec<BeverageProperty.Data> dataCodec, ClientReloadListener<BeverageProperty, BeverageProperty.Data> clientReloadListener) {
            super(registry, dataCodec, clientReloadListener);
        }

        @Override
        public BeverageProperty.Data toD(BeverageProperty property) {
            return new BeverageProperty.Data(property.getId(), BeverageProperties.PROPERTY_CACHE.getOrDefault(property, new LinkedHashSet<>()).stream().toList());
        }

        @Override
        public BeverageProperty toT(BeverageProperty.Data data) {
            return BuiltInRegistryProviders.BEVERAGE_PROPERTY.getValue(data.id());
        }
    }
}