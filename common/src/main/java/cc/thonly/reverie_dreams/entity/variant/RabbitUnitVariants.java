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

public class RabbitUnitVariants {
    public static final Identifier DEFAULT_ID = ReverieDreams.id("rabbit_unit_0");
    public static final RabbitUnitVariant RABBIT_UNIT_0 = register(new RabbitUnitVariant(ReverieDreams.id("rabbit_unit_0"), MobSkinTypes.RABBIT_UNIT_0));
    public static final RabbitUnitVariant RABBIT_UNIT_1 = register(new RabbitUnitVariant(ReverieDreams.id("rabbit_unit_1"), MobSkinTypes.RABBIT_UNIT_1));
    public static final RabbitUnitVariant RABBIT_UNIT_2 = register(new RabbitUnitVariant(ReverieDreams.id("rabbit_unit_2"), MobSkinTypes.RABBIT_UNIT_2));

    public static RabbitUnitVariant register(RabbitUnitVariant variant) {
        return register(variant.getId(), variant);
    }

    public static RabbitUnitVariant register(String name, RabbitUnitVariant variant) {
        return register(ReverieDreams.id(name), variant);
    }

    public static RabbitUnitVariant register(Identifier identifier, RabbitUnitVariant variant) {
        return BuiltInRegistryProviders.register(BuiltInRegistryProviders.RABBIT_UNIT_VARIANT, identifier, variant);
    }

    public static void bootstrap(RegistryProvider<RabbitUnitVariant> registry) {

    }

    public static boolean isEmpty() {
        return BuiltInRegistryProviders.RABBIT_UNIT_VARIANT.values().isEmpty();
    }

    public static synchronized RabbitUnitVariant random() {
        List<RabbitUnitVariant> list = BuiltInRegistryProviders.RABBIT_UNIT_VARIANT.values().stream().toList();

        if (list.isEmpty()) return null;

        return list.get(ThreadLocalRandom.current().nextInt(list.size()));
    }

    public static RabbitUnitVariant getFromSkinType(SkinType skinType) {
        List<RabbitUnitVariant> list = BuiltInRegistryProviders.RABBIT_UNIT_VARIANT.values().stream().filter(variant -> variant.getSkinType().equals(skinType)).toList();
        return list.isEmpty() ? null : list.getFirst();
    }

    public static RabbitUnitVariant getFromProperty(Property property) {
        List<RabbitUnitVariant> list = BuiltInRegistryProviders.RABBIT_UNIT_VARIANT.values().stream().filter(variant -> variant.getSkinType().getProperty() == property).toList();
        return list.isEmpty() ? null : list.getFirst();
    }
}
