package cc.thonly.reverie_dreams.registry;

import net.minecraft.util.Identifier;

@SuppressWarnings("unchecked")
public interface Translatable {
    default String translateKey() {
        Object object = this;
        if (object instanceof OwnerBinding<?> ownerBindingImpl) {
            IntrinsicalRegister<Object> registryRef = (IntrinsicalRegister<Object>) ownerBindingImpl.<Object>getOwner();
            Identifier id = registryRef.getId(object);
            if (id == null) {
                return registryRef.getKey().getValue().getPath() + ".null";
            }
            return id.toTranslationKey(registryRef.getKey().getValue().getPath());
        }
        return "null";
    }
}
