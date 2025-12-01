package cc.thonly.reverie_dreams.entity.variant;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.registry.RegistryHandlers;
import cc.thonly.reverie_dreams.registry.content.skin.MobSkinTypes;
import cc.thonly.reverie_dreams.registry.impl.RegistryHandler;
import com.mojang.authlib.properties.Property;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class YouseiVariants {
    public static final RegistryHandler<YouseiVariant> REGISTRY = RegistryHandlers.YOUSEI_VARIANT;
    public static final ResourceLocation DEFAULT_ID = ReverieDreams.id("blue");
    public static final YouseiVariant BLUE = register(new YouseiVariant(ReverieDreams.id("blue"), MobSkinTypes.YOUSEI01));
    public static final YouseiVariant ORANGE = register(new YouseiVariant(ReverieDreams.id("orange"), MobSkinTypes.YOUSEI02));
    public static final YouseiVariant GREEN = register(new YouseiVariant(ReverieDreams.id("green"), MobSkinTypes.YOUSEI03));

    public static YouseiVariant register(YouseiVariant variant) {
        return register(variant.getId(), variant);
    }

    public static YouseiVariant register(String name, YouseiVariant variant) {
        return register(ReverieDreams.id(name), variant);
    }

    public static YouseiVariant register(ResourceLocation identifier, YouseiVariant variant) {
        return RegistryHandlers.register(REGISTRY, identifier, variant);
    }

    public static void bootstrap(RegistryHandler<YouseiVariant> registry) {

    }

    public static boolean isEmpty() {
        return RegistryHandlers.YOUSEI_VARIANT.values().isEmpty();
    }

    public static synchronized YouseiVariant random() {
        List<YouseiVariant> list = RegistryHandlers.YOUSEI_VARIANT.values().stream().toList();

        if (list.isEmpty()) return null;

        return list.get(ThreadLocalRandom.current().nextInt(list.size()));
    }

    public static YouseiVariant getFromProperty(Property property) {
        List<YouseiVariant> list = RegistryHandlers.YOUSEI_VARIANT.values().stream().filter(variant -> variant.getSkinType().get() == property).toList();
        return list.isEmpty() ? null : list.getFirst();
    }
}
