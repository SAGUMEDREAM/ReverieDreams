package cc.thonly.reverie_dreams.neoforge.impl;

import cc.thonly.reverie_dreams.registry.RegistryBuilderFactory;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;

import java.util.Map;
import java.util.function.Supplier;

public class RegistryBuilderFactoryImpl implements RegistryBuilderFactory {
    public static final Map<Identifier, Supplier<Registry<?>>> BUILDERS = new Object2ObjectLinkedOpenHashMap<>(8);

    @Override
    public void addBuilder(Identifier key, Supplier<Registry<?>> factory) {
        BUILDERS.put(key, factory);
    }
}
