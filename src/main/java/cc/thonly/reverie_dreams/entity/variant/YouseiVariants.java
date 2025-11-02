package cc.thonly.reverie_dreams.entity.variant;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.entity.skin.MobSkinTypes;
import cc.thonly.reverie_dreams.registry.IntrinsicalRegister;
import cc.thonly.reverie_dreams.registry.RegistryManager;
import com.mojang.authlib.properties.Property;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.resources.ResourceLocation;

public class YouseiVariants {
    public static final IntrinsicalRegister<YouseiVariant> REGISTRY = RegistryManager.YOUSEI_VARIANT;
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
        return RegistryManager.register(REGISTRY, identifier, variant);
    }

    public static void bootstrap(IntrinsicalRegister<YouseiVariant> registry) {

    }

    public static boolean isEmpty() {
        return RegistryManager.YOUSEI_VARIANT.values().isEmpty();
    }

    public static synchronized YouseiVariant random() {
        List<YouseiVariant> list = RegistryManager.YOUSEI_VARIANT.values().stream().toList();

        if (list.isEmpty()) return null;

        return list.get(ThreadLocalRandom.current().nextInt(list.size()));
    }

    public static YouseiVariant getFromProperty(Property property) {
        List<YouseiVariant> list = RegistryManager.YOUSEI_VARIANT.values().stream().filter(variant -> variant.getSkinType().get() == property).toList();
        return list.isEmpty() ? null : list.getFirst();
    }
}
