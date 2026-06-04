package cc.thonly.reverie_dreams.entity.variant;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.registry.RegistryImpls;
import cc.thonly.reverie_dreams.registry.content.skin.MobSkinTypes;
import cc.thonly.reverie_dreams.registry.impl.RegistryImpl;
import com.mojang.authlib.properties.Property;
import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class OniVariants {
    public static final RegistryImpl<OniVariant> REGISTRY = RegistryImpls.ONI_VARIANT;
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
        return RegistryImpls.register(REGISTRY, identifier, variant);
    }

    public static void bootstrap(RegistryImpl<OniVariant> oniVariants) {

    }

    public static boolean isEmpty() {
        return RegistryImpls.ONI_VARIANT.values().isEmpty();
    }

    public static synchronized OniVariant random() {
        List<OniVariant> list = RegistryImpls.ONI_VARIANT.values().stream().toList();

        if (list.isEmpty()) return null;

        return list.get(ThreadLocalRandom.current().nextInt(list.size()));
    }

    public static OniVariant getFromProperty(Property property) {
        List<OniVariant> list = RegistryImpls.ONI_VARIANT.values().stream().filter(variant -> variant.getSkinType().getProperty() == property).toList();
        return list.isEmpty() ? null : list.getFirst();
    }
}
