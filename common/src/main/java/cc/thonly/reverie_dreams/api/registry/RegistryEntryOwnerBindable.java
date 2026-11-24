package cc.thonly.reverie_dreams.api.registry;

import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;

import java.util.Optional;

@SuppressWarnings({"unchecked", "ConstantValue"})
public interface RegistryEntryOwnerBindable<T> {
    void setOwner(RegistryProviderLookup<T> register);

    RegistryProviderLookup<T> getOwner();

    default Holder.Reference<T> builtInRegistryHolder() {
        RegistryProviderLookup<T> owner = this.getOwner();
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
