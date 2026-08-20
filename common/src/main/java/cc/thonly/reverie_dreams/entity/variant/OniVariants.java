package cc.thonly.reverie_dreams.entity.variant;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.data.skin.SkinType;
import cc.thonly.reverie_dreams.registry.BuiltInRegistryProviders;
import cc.thonly.reverie_dreams.registry.content.skin.MobSkinTypes;
import cc.thonly.reverie_dreams.registry.impl.RegistryProvider;
import com.mojang.authlib.properties.Property;
import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class OniVariants {
    public static final OniVariant GREEN = register(new OniVariant(ReverieDreams.id("oni_green"), MobSkinTypes.ONI_GREEN));
    public static final OniVariant ORANGE = register(new OniVariant(ReverieDreams.id("oni_orange"), MobSkinTypes.ONI_ORANGE));
    public static final OniVariant RED = register(new OniVariant(ReverieDreams.id("oni_red"), MobSkinTypes.ONI_RED));

    public static OniVariant register(OniVariant variant) {
        return register(variant.getId(), variant);
    }

    public static OniVariant register(String name, OniVariant variant) {
        return register(ReverieDreams.id(name), variant);
    }

    public static OniVariant register(Identifier identifier, OniVariant variant) {
        return BuiltInRegistryProviders.register(BuiltInRegistryProviders.ONI_VARIANT, identifier, variant);
    }

    public static void bootstrap(RegistryProvider<OniVariant> oniVariants) {

    }

    public static boolean isEmpty() {
        return BuiltInRegistryProviders.ONI_VARIANT.values().isEmpty();
    }

    public static synchronized OniVariant random() {
        List<OniVariant> list = BuiltInRegistryProviders.ONI_VARIANT.values().stream().toList();

        if (list.isEmpty()) return null;

        return list.get(ThreadLocalRandom.current().nextInt(list.size()));
    }

    public static OniVariant getFromSkinType(SkinType skinType) {
        List<OniVariant> list = BuiltInRegistryProviders.ONI_VARIANT.values().stream().filter(variant -> variant.getSkinType().equals(skinType)).toList();
        return list.isEmpty() ? null : list.getFirst();
    }

    public static OniVariant getFromProperty(Property property) {
        List<OniVariant> list = BuiltInRegistryProviders.ONI_VARIANT.values().stream().filter(variant -> variant.getSkinType().getProperty() == property).toList();
        return list.isEmpty() ? null : list.getFirst();
    }
}
