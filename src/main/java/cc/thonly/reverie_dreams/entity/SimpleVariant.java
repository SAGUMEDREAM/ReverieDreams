package cc.thonly.reverie_dreams.entity;

import com.mojang.authlib.properties.Property;

public interface SimpleVariant {
    void setProperty(Property property);

    Property getProperty();
}
