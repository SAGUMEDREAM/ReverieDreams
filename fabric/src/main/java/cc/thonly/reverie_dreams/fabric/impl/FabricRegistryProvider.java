package cc.thonly.reverie_dreams.fabric.impl;

import cc.thonly.reverie_dreams.registry.RegistryExtension;
import cc.thonly.reverie_dreams.registry.impl.RegistryProvider;
import com.mojang.serialization.Lifecycle;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class FabricRegistryProvider<T> extends RegistryProvider<T> implements RegistryExtension<T> {
    final Map<Identifier, Identifier> aliases = new HashMap<>();

    public FabricRegistryProvider(ResourceKey<? extends Registry<T>> key) {
        super(key);
    }

    public FabricRegistryProvider(ResourceKey<? extends Registry<T>> key, Lifecycle lifecycle) {
        super(key, lifecycle);
    }

    public FabricRegistryProvider(ResourceKey<? extends Registry<T>> key, @Nullable RegistryProvider<T> parent) {
        super(key, parent);
    }

    public FabricRegistryProvider(ResourceKey<? extends Registry<T>> key, Lifecycle lifecycle, @Nullable RegistryProvider<T> parent) {
        super(key, lifecycle, parent);
    }

    @Override
    public void addAliaName(Identifier from, Identifier to) {
        if (from.equals(to))
            return;
        if (this.aliases.containsKey(from)) {
            Identifier old = this.aliases.get(from);
            if (!old.equals(to))
                throw new IllegalStateException("Duplicate alias with key \"" + from + "\" attempting to map to \"" + to + "\", found existing mapping \"" + old + "\"");
        }
        if (resolveId(from).equals(to))
            throw new IllegalStateException("Infinite alias loop detected: from " + from + " to " + to);
        this.aliases.put(from, to);
    }

    @Override
    public ResourceKey<T> resolveKey(ResourceKey<T> key) {
        Identifier resolvedName = resolveId(key.identifier());
        // Try to reuse the key if possible
        return resolvedName == key.identifier() ? key : ResourceKey.create(this.key(), resolvedName);
    }

    @Override
    public Identifier resolveId(Identifier name) {
        if (this.containsKey(name))
            return name;

        Identifier alias = this.aliases.get(name);
        if (alias == null)
            return name;

        return resolveId(alias);
    }
}
