package cc.thonly.reverie_dreams.registry.impl;

import cc.thonly.reverie_dreams.api.registry.RawIdTypeRegistry;
import lombok.Getter;
import net.minecraft.resources.Identifier;

public class RawIdTypeRegistryImpl<T> implements RawIdTypeRegistry<T> {
    @Getter
    private final String namespace;
    @Getter
    private final IdTypeRegistry<T> registry;
    private int nextId = -1;

    public RawIdTypeRegistryImpl(String namespace) {
        this.namespace = namespace;
        this.registry = new IdTypeRegistry<>();
    }

    @Override
    public void register(T value) {
        ++this.nextId;
        this.registry.register(Identifier.fromNamespaceAndPath(this.namespace, String.valueOf(this.nextId)), value);
    }

    @Override
    public void register(String name, T value) {
        this.registry.register(Identifier.fromNamespaceAndPath(this.namespace, name), value);
    }

}
