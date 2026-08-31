package cc.thonly.reverie_dreams.registry.delegate;

import dev.architectury.impl.RegistrySupplierImpl;
import dev.architectury.registry.registries.Registrar;
import dev.architectury.registry.registries.RegistrarManager;
import dev.architectury.registry.registries.RegistrySupplier;
import lombok.experimental.Delegate;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings({"UnstableApiUsage", "PatternVariableHidesField", "rawtypes", "unchecked"})
public class HolderDelegate<T> implements Holder<T>, RegistrySupplierImpl<T> {
    private final Holder<T> holder;
    private RegistrarManager registrarManager;
    private Registrar<T> registrar;
    private Identifier registryId;
    private Identifier id;

    public HolderDelegate(Holder<T> holder) {
        this.holder = holder;
    }

    @Override
    public Holder<T> getHolder() {
        return this.holder;
    }

    @Override
    public RegistrarManager getRegistrarManager() {
        if (this.registrarManager == null && this.holder instanceof RegistrySupplier<T> supplier) {
            this.registrarManager = supplier.getRegistrarManager();
        }
        if (this.registrarManager == null) {
            this.holder.unwrapKey().ifPresent(key -> {
                this.registrarManager = RegistrarManager.get(key.identifier().getNamespace());
            });
        }
        return this.registrarManager;
    }

    @Override
    public Registrar<T> getRegistrar() {
        if (this.registrar == null && this.holder instanceof RegistrySupplier<T> supplier) {
            this.registrar = supplier.getRegistrar();
        }
        return this.registrar;
    }

    @Override
    public Identifier getRegistryId() {
        if (this.registryId == null && this.holder instanceof RegistrySupplier<T> supplier) {
            this.registryId = supplier.getRegistryId();
        }
        return this.registryId;
    }

    @Override
    public Identifier getId() {
        if (this.id == null && this.holder instanceof RegistrySupplier<T> supplier) {
            this.id = supplier.getId();
        }
        return this.id;
    }

    @Override
    public boolean isPresent() {
        return this.holder.isBound();
    }

    @Override
    public T get() {
        return this.holder.value();
    }

    public ResourceKey<T> getKey() {
        return this.holder.unwrapKey().orElse(null);
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj) || (obj instanceof Holder<?> holder && this.is((Holder) holder));
    }
}
