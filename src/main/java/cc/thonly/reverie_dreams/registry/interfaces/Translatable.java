package cc.thonly.reverie_dreams.registry.interfaces;

import cc.thonly.reverie_dreams.registry.impl.RegistryHandler;
import net.minecraft.resources.ResourceLocation;

@SuppressWarnings("unchecked")
public interface Translatable {
    default String translateKey() {
        Object object = this;
        if (object instanceof OwnerBinding<?> ownerBindingImpl) {
            RegistryHandler<Object> registryRef = (RegistryHandler<Object>) ownerBindingImpl.<Object>getOwner();
            ResourceLocation id = registryRef.getKey(object);
            if (id == null) {
                return registryRef.key().location().getPath() + ".null";
            }
            return id.toLanguageKey(registryRef.key().location().getPath());
        }
        return "null";
    }
}
