package cc.thonly.reverie_dreams.registry;

import cc.thonly.reverie_dreams.registry.impl.RegistryProvider;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;

import java.util.Optional;

@SuppressWarnings({"unchecked", "ConstantValue"})
public interface RegistryEntryOwnerBindable<T> {
    void setOwner(RegistryProvider<T> register);

    RegistryProvider<T> getOwner();

    default Holder.Reference<T> builtInRegistryHolder() {
        RegistryProvider<T> owner = this.getOwner();
        if (owner == null) {
            return null;
        }
        T cast = (T) this;
        if (cast == null) {
            return null;
        }
        Identifier key = owner.getKey(cast);
        if (key == null) {
            return null;
        }
        Optional<Holder.Reference<T>> tReference = owner.get(key);
        return tReference.orElse(null);
    }
}
