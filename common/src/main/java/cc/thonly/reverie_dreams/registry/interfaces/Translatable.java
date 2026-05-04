package cc.thonly.reverie_dreams.registry.interfaces;

import cc.thonly.reverie_dreams.registry.impl.RegistryImpl;
import net.minecraft.resources.Identifier;

@SuppressWarnings("unchecked")
public interface Translatable {
    default String translateKey() {
        Object object = this;
        if (object instanceof OwnerBinding<?> ownerBindingImpl) {
            RegistryImpl<Object> registryRef = (RegistryImpl<Object>) ownerBindingImpl.getOwner();
            Identifier id = registryRef.getKey(object);
            if (id == null) {
                return registryRef.key().identifier().getPath() + ".unregistered";
            }
            return id.toLanguageKey(registryRef.key().identifier().getPath());
        }
        return "unregistered";
    }
}
