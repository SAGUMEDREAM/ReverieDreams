package cc.thonly.reverie_dreams.entity;

import com.mojang.authlib.properties.Property;

import java.util.function.Supplier;

public interface SimpleVariant {
    void setPropertySupplier(Supplier<Property> propertySupplier);
    void setProperty(Property property);

    Supplier<Property> getPropertySupplier();
}
