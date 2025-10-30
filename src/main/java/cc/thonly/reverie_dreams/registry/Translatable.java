package cc.thonly.reverie_dreams.registry;

import net.minecraft.resources.ResourceLocation;

@SuppressWarnings("unchecked")
public interface Translatable {
    default String translateKey() {
        Object object = this;
        if (object instanceof OwnerBinding<?> ownerBindingImpl) {
            IntrinsicalRegister<Object> registryRef = (IntrinsicalRegister<Object>) ownerBindingImpl.<Object>getOwner();
            ResourceLocation id = registryRef.getKey(object);
            if (id == null) {
                return registryRef.key().location().getPath() + ".null";
            }
            return id.toLanguageKey(registryRef.key().location().getPath());
        }
        return "null";
    }
}
