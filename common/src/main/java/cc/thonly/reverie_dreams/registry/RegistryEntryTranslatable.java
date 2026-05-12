package cc.thonly.reverie_dreams.registry;

import cc.thonly.reverie_dreams.registry.impl.RegistryImpl;
import net.minecraft.resources.Identifier;

@SuppressWarnings("unchecked")
public interface RegistryEntryTranslatable {
    default String translateKey() {
        Object object = this;
        if (object instanceof RegistryEntryOwnerBindable<?> ownerBindingImpl) {
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
