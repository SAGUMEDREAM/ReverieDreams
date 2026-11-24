package cc.thonly.reverie_dreams.registry.content.skin;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.data.skin.SkinType;
import cc.thonly.reverie_dreams.registry.impl.RegistryProvider;
import net.minecraft.resources.Identifier;

public class MobSkinTypes extends SkinTypes {
    public static final SkinType UNDEFINED = register(new SkinType(Identifier.withDefaultNamespace("undefined")));

    public static final SkinType DEFAULT = register(new SkinType(Identifier.withDefaultNamespace("default")));

    public static final SkinType GHOST = register(new SkinType(ReverieDreams.id("ghost")));
    public static final SkinType YOUSEI01 = register(new SkinType(ReverieDreams.id("yousei_01")));
    public static final SkinType YOUSEI02 = register(new SkinType(ReverieDreams.id("yousei_02")));
    public static final SkinType YOUSEI03 = register(new SkinType(ReverieDreams.id("yousei_03")));
    public static final SkinType SUNFLOWER_YOUSEI = register(new SkinType(ReverieDreams.id("sunflower_yousei")));
    public static final SkinType GOBLIN = register(new SkinType(ReverieDreams.id("goblin")));
    public static final SkinType WATER_ELEMENTAL = register(new SkinType(ReverieDreams.id("water_elemental")));
    public static final SkinType FIRE_ELEMENTAL = register(new SkinType(ReverieDreams.id("fire_elemental")));
    public static final SkinType ICE_ELEMENTAL = register(new SkinType(ReverieDreams.id("ice_elemental")));
    public static final SkinType RABBIT_UNIT_0 = register(new SkinType(ReverieDreams.id("rabbit_unit_0")));
    public static final SkinType RABBIT_UNIT_1 = register(new SkinType(ReverieDreams.id("rabbit_unit_1")));
    public static final SkinType RABBIT_UNIT_2 = register(new SkinType(ReverieDreams.id("rabbit_unit_2")));
    public static final SkinType ONI_GREEN = register(new SkinType(ReverieDreams.id("oni_green")));
    public static final SkinType ONI_ORANGE = register(new SkinType(ReverieDreams.id("oni_orange")));
    public static final SkinType ONI_RED = register(new SkinType(ReverieDreams.id("oni_red")));

    public static void bootstrap(RegistryProvider<SkinType> registry) {
        MobSkinTypes.DEFAULT.setSlim(false);
        MobSkinTypes.GOBLIN.setSlim(false);
        MobSkinTypes.WATER_ELEMENTAL.setSlim(false);
        MobSkinTypes.FIRE_ELEMENTAL.setSlim(false);
        MobSkinTypes.ICE_ELEMENTAL.setSlim(false);
        MobSkinTypes.ONI_GREEN.setSlim(false);
        MobSkinTypes.ONI_ORANGE.setSlim(false);
        MobSkinTypes.ONI_RED.setSlim(false);
    }
}
