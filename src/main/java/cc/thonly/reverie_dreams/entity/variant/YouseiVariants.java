package cc.thonly.reverie_dreams.entity.variant;

import cc.thonly.reverie_dreams.Touhou;
import cc.thonly.reverie_dreams.entity.skin.MobSkins;
import cc.thonly.reverie_dreams.registry.IntrinsicalRegister;
import cc.thonly.reverie_dreams.registry.RegistryManager;
import com.mojang.authlib.properties.Property;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class YouseiVariants {
    public static final IntrinsicalRegister<YouseiVariant> REGISTRY = RegistryManager.YOUSEI_VARIANT;
    public static final Identifier DEFAULT_ID = Touhou.id("blue");
    public static final YouseiVariant BLUE = register(new YouseiVariant(Touhou.id("blue"), MobSkins.YOUSEI01.get()));
    public static final YouseiVariant ORANGE = register(new YouseiVariant(Touhou.id("orange"), MobSkins.YOUSEI02.get()));
    public static final YouseiVariant GREEN = register(new YouseiVariant(Touhou.id("green"), MobSkins.YOUSEI03.get()));

    public static YouseiVariant register(YouseiVariant variant) {
        return register(variant.getId(), variant);
    }

    public static YouseiVariant register(String name, YouseiVariant variant) {
        return register(Touhou.id(name), variant);
    }

    public static YouseiVariant register(Identifier identifier, YouseiVariant variant) {
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
        List<YouseiVariant> list = RegistryManager.YOUSEI_VARIANT.values().stream().filter(variant -> variant.getProperty().equals(property)).toList();
        return list.isEmpty() ? null : list.getFirst();
    }
}
