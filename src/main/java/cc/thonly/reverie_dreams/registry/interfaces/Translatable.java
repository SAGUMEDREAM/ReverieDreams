package cc.thonly.reverie_dreams.registry.interfaces;

import cc.thonly.reverie_dreams.registry.impl.RegistryHandler;
import net.minecraft.resources.Identifier;

@SuppressWarnings("unchecked")
public interface Translatable {
    default String translateKey() {
        Object object = this;
        if (object instanceof OwnerBinding<?> ownerBindingImpl) {
            RegistryHandler<Object> registryRef = (RegistryHandler<Object>) ownerBindingImpl.<Object>getOwner();
            Identifier id = registryRef.getKey(object);
            if (id == null) {
                return registryRef.key().identifier().getPath() + ".null";
            }
            return id.toLanguageKey(registryRef.key().identifier().getPath());
        }
        return "null";
    }
}
