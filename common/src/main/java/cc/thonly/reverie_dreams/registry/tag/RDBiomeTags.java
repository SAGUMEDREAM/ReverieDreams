package cc.thonly.reverie_dreams.registry.tag;

import cc.thonly.reverie_dreams.ReverieDreams;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;

public class RDBiomeTags {
    public static final TagKey<Biome> GENSOKYO = of("gensokyo");
    public static final TagKey<Biome> FOREST_YOUKAI_SPAWN_AREA = of("forest_youkai_spawn_area");
    public static final TagKey<Biome> MAGIC_FOREST = of("magic_forest");
    public static final TagKey<Biome> NORMAL_YOUSEI_SPAWN_AREA = of("normal_yousei_spawn_area");
    public static final TagKey<Biome> SUNFLOWER_YOUSEI_SPAWN_AREA = of("sunflower_yousei_spawn_area");
    public static final TagKey<Biome> MAID_YOUSEI_SPAWN_AREA = of("maid_yousei_spawn_area");
    public static final TagKey<Biome> HAIR_BALL_SPAWN_AREA = of("hairball_spawn_area");
    public static final TagKey<Biome> TENGU_SPAWN_AREA = of("tengu_spawn_area");
    public static final TagKey<Biome> YOUSEI_LOW_DENSITY_SPAWN_AREA = of("yousei_low_density_spawn_area");
    public static final TagKey<Biome> NORMAL_YOUSEI_PRIMARY_HABITAT = of("normal_yousei_primary_habitat");
    public static final TagKey<Biome> SUNFLOWER_YOUSEI_PRIMARY_HABITAT = of("sunflower_yousei_primary_habitat");
    public static final TagKey<Biome> MAID_YOUSEI_PRIMARY_HABITAT = of("maid_yousei_primary_habitat");
    public static final TagKey<Biome> HAIR_BALL_PRIMARY_HABITAT = of("hairball_primary_habitat");
    public static final TagKey<Biome> TENGU_PRIMARY_HABITAT = of("tengu_primary_habitat");
    public static final TagKey<Biome> YOUKAI_RABBIT_SPAWN_AREA = of("youkai_rabbit_spawn_area");
    public static final TagKey<Biome> YOUKAI_RABBIT_PRIMARY_HABITAT = of("youkai_rabbit_primary_habitat");
    public static final TagKey<Biome> WILD_PIG_SPAWN_AREA = of("wild_pig_spawn_area");
    public static final TagKey<Biome> KILLER_BEE_SPAWN_AREA = of("killer_bee_spawn_area");
    public static final TagKey<Biome> GOBLIN_SPAWN_AREA = of("goblin_spawn_area");
    public static final TagKey<Biome> ONI_SPAWN_AREA = of("oni_spawn_area");
    public static final TagKey<Biome> MUSHROOM_MONSTER_SPAWN_AREA = of("mushroom_monster_spawn_area");
    public static final TagKey<Biome> ICE_ELEMENTAL_SPAWN_AREA = of("ice_elemental_spawn_area");
    public static final TagKey<Biome> UFO_SPAWN_AREA = of("ufo_spawn_area");
    public static final TagKey<Biome> GHOST_SPAWN_AREA = of("ghost_spawn_area");

    private static TagKey<Biome> of(String id) {
        return TagKey.create(Registries.BIOME, ReverieDreams.id(id));
    }
}
