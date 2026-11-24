package cc.thonly.reverie_dreams.api.registry;

import net.minecraft.resources.Identifier;

@SuppressWarnings("unchecked")
public interface RegistryEntryTranslatable {
    default String translateKey() {
        Object object = this;
        if (object instanceof RegistryEntryOwnerBindable<?> ownerBindingImpl) {
            RegistryProviderLookup<Object> registryRef = (RegistryProviderLookup<Object>) ownerBindingImpl.getOwner();
            Identifier id = registryRef.getKey(object);
            if (id == null) {
                return registryRef.getRegistryKey().identifier().getPath() + ".unregistered";
            }
            return id.toLanguageKey(registryRef.getRegistryKey().identifier().getPath());
        }
        return "unregistered";
    }
}
