package cc.thonly.reverie_dreams.entity.variant;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.registry.RegistryImpls;
import cc.thonly.reverie_dreams.registry.content.skin.MobSkinTypes;
import cc.thonly.reverie_dreams.registry.impl.RegistryImpl;
import com.mojang.authlib.properties.Property;
import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class RabbitUnitVariants {
    public static final RegistryImpl<RabbitUnitVariant> REGISTRY = RegistryImpls.RABBIT_UNIT_VARIANT;
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
        return RegistryImpls.register(REGISTRY, identifier, variant);
    }

    public static void bootstrap(RegistryImpl<RabbitUnitVariant> registry) {

    }

    public static boolean isEmpty() {
        return RegistryImpls.RABBIT_UNIT_VARIANT.values().isEmpty();
    }

    public static synchronized RabbitUnitVariant random() {
        List<RabbitUnitVariant> list = RegistryImpls.RABBIT_UNIT_VARIANT.values().stream().toList();

        if (list.isEmpty()) return null;

        return list.get(ThreadLocalRandom.current().nextInt(list.size()));
    }

    public static RabbitUnitVariant getFromProperty(Property property) {
        List<RabbitUnitVariant> list = RegistryImpls.RABBIT_UNIT_VARIANT.values().stream().filter(variant -> variant.getSkinType().get() == property).toList();
        return list.isEmpty() ? null : list.getFirst();
    }
}
