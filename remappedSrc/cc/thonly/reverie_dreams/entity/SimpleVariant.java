package cc.thonly.reverie_dreams.entity;

import cc.thonly.reverie_dreams.entity.skin.SkinType;
import com.mojang.authlib.properties.Property;

import java.util.function.Supplier;

public interface SimpleVariant {
    void setSkinType(SkinType skinType);

    SkinType getSkinType();
}
