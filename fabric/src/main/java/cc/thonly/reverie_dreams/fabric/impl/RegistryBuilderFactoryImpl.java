package cc.thonly.reverie_dreams.fabric.impl;

import cc.thonly.reverie_dreams.api.registry.SimpleRegistryFrozenModifier;
import cc.thonly.reverie_dreams.registry.RegistryBuilderFactory;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

import java.util.Map;
import java.util.function.Supplier;

@SuppressWarnings({"unchecked", "rawtypes"})
public class RegistryBuilderFactoryImpl implements RegistryBuilderFactory {
    @Override
    public void addBuilder(Identifier key, Supplier<Registry<?>> factory) {
        Registry<?> registry = factory.get();
        Registry registries = BuiltInRegistries.REGISTRY;
        if (registries instanceof SimpleRegistryFrozenModifier mappedRegistry) {
            boolean frozen = mappedRegistry.reverie_dreams$isFrozen();
            if (frozen) {
                return;
            }
            Registry.register(registries, key, registry);
        }
    }
}
