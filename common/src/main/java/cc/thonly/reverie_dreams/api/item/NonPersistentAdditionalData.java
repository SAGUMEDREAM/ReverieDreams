package cc.thonly.reverie_dreams.api.item;

import org.jetbrains.annotations.Nullable;

public interface NonPersistentAdditionalData {
    public @Nullable <Type> Type reverie_dreams$getNonPersistentAdditionalData(String name);

    public @Nullable <Type> Type reverie_dreams$getNonPersistentAdditionalData(String name, Class<Type> type);

    public @Nullable <Type> void reverie_dreams$setNonPersistentAdditionalData(String name, Type data);

}
