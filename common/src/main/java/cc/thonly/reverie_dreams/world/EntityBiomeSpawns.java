package cc.thonly.reverie_dreams.world;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.registry.content.entity.RDEntityTypes;
import cc.thonly.reverie_dreams.world.gen.RDBuiltinBiomes;
import cc.thonly.reverie_dreams.world.gensokyo.tree.GensokyoForestBootstrap;
import cc.thonly.reverie_dreams.world.gensokyo.tree.GensokyoForestType;
import dev.architectury.hooks.level.biome.SpawnProperties;
import dev.architectury.registry.level.biome.BiomeModifications;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.MobSpawnSettings;

import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static cc.thonly.reverie_dreams.registry.tag.RDBiomeTags.*;

/**
 * Biome additions for land mobs. Primary habitats replace the ordinary entry for tag-based rules.
 */
public final class EntityBiomeSpawns {
    // Select these by key: the NeoForge biome hook can run before biome tags are available.
    private static final Set<Identifier> WAGYU_HABITAT = Set.of(
            Biomes.TAIGA.identifier(), Biomes.SNOWY_TAIGA.identifier(),
            Biomes.OLD_GROWTH_PINE_TAIGA.identifier(), Biomes.OLD_GROWTH_SPRUCE_TAIGA.identifier(),
            GensokyoForestBootstrap.biome(GensokyoForestType.TAIGA).identifier()
    );

    public record Population(int weight, int min, int max) {
    }

    public record Rule(Supplier<? extends EntityType<?>> entity, MobCategory category, TagKey<Biome> area,
                       TagKey<Biome> primary, Population ordinary, Population resident, boolean fairy) {
        public boolean allows(Predicate<TagKey<Biome>> hasTag, boolean fairiesEnabled) {
            return (!fairy || fairiesEnabled) && hasTag.test(area);
        }

        public Population select(Predicate<TagKey<Biome>> hasTag, boolean fairiesEnabled, boolean dreamBiome) {
            if (!allows(hasTag, fairiesEnabled))
                return null;
            boolean primaryHabitat = primary != null && hasTag.test(primary);
            Population selected = primaryHabitat ? resident : ordinary;
            if (fairy && !dreamBiome && !hasTag.test(YOUSEI_LOW_DENSITY_SPAWN_AREA)) {
                return population(selected.weight() * (primaryHabitat ? 2 : 5), 2, 3);
            }
            return selected;
        }
    }

    private static Population population(int weight, int min, int max) {
        return new Population(weight, min, max);
    }

    public static final List<Rule> RULES = List.of(
            new Rule(RDEntityTypes.YOUKAI_CAT, MobCategory.MONSTER, FOREST_YOUKAI_SPAWN_AREA, null, population(1, 1, 2), null, false),
            new Rule(RDEntityTypes.YOUKAI_TANUKI, MobCategory.MONSTER, FOREST_YOUKAI_SPAWN_AREA, null, population(1, 1, 2), null, false),
            new Rule(RDEntityTypes.YOUKAI_FOX, MobCategory.MONSTER, FOREST_YOUKAI_SPAWN_AREA, null, population(1, 1, 2), null, false),
            new Rule(RDEntityTypes.YOUSEI, MobCategory.MONSTER, NORMAL_YOUSEI_SPAWN_AREA, NORMAL_YOUSEI_PRIMARY_HABITAT,
                    population(4, 1, 2), population(12, 1, 2), true),
            new Rule(RDEntityTypes.SUNFLOWER_YOUSEI, MobCategory.MONSTER, SUNFLOWER_YOUSEI_SPAWN_AREA, SUNFLOWER_YOUSEI_PRIMARY_HABITAT,
                    population(2, 1, 1), population(24, 1, 2), true),
            new Rule(RDEntityTypes.MAID_YOUSEI, MobCategory.MONSTER, MAID_YOUSEI_SPAWN_AREA, MAID_YOUSEI_PRIMARY_HABITAT,
                    population(2, 1, 1), population(8, 1, 2), true),
            new Rule(RDEntityTypes.HAIRBALL, MobCategory.MONSTER, HAIR_BALL_SPAWN_AREA, HAIR_BALL_PRIMARY_HABITAT,
                    population(2, 1, 1), population(6, 1, 2), false),
            new Rule(RDEntityTypes.WHITE_WOLF_TENGU, MobCategory.MONSTER, TENGU_SPAWN_AREA, TENGU_PRIMARY_HABITAT,
                    population(1, 1, 1), population(10, 1, 2), false),
            new Rule(RDEntityTypes.CROW_TENGU, MobCategory.MONSTER, TENGU_SPAWN_AREA, TENGU_PRIMARY_HABITAT,
                    population(1, 1, 1), population(8, 1, 2), false),
            new Rule(RDEntityTypes.YOUKAI_RABBIT, MobCategory.CREATURE, YOUKAI_RABBIT_SPAWN_AREA, YOUKAI_RABBIT_PRIMARY_HABITAT,
                    population(1, 1, 2), population(3, 1, 2), false),
            new Rule(RDEntityTypes.WILD_PIG, MobCategory.MONSTER, WILD_PIG_SPAWN_AREA, null, population(1, 1, 1), null, false),
            new Rule(RDEntityTypes.KILLER_BEE, MobCategory.MONSTER, KILLER_BEE_SPAWN_AREA, null, population(2, 1, 2), null, false),
            new Rule(RDEntityTypes.GOBLIN, MobCategory.MONSTER, GOBLIN_SPAWN_AREA, null, population(10, 1, 1), null, false),
            new Rule(RDEntityTypes.ONI, MobCategory.MONSTER, ONI_SPAWN_AREA, null, population(1, 1, 1), null, false),
            new Rule(RDEntityTypes.MUSHROOM_MONSTER, MobCategory.MONSTER, MUSHROOM_MONSTER_SPAWN_AREA, null, population(20, 1, 1), null, false),
            new Rule(RDEntityTypes.ICE_ELEMENTAL, MobCategory.MONSTER, ICE_ELEMENTAL_SPAWN_AREA, null, population(4, 1, 1), null, false),
            new Rule(RDEntityTypes.UFO, MobCategory.MONSTER, UFO_SPAWN_AREA, null, population(3, 1, 2), null, false),
            new Rule(RDEntityTypes.GHOST, MobCategory.MONSTER, GHOST_SPAWN_AREA, null, population(6, 1, 1), null, false));

    public static void initialize() {
        BiomeModifications.addProperties((context, mutable) -> {
            boolean dreamBiome = context.getKey().filter(RDBuiltinBiomes.DREAM.identifier()::equals).isPresent();
            SpawnProperties.Mutable spawns = mutable.getSpawnProperties();
            add(context::hasTag, spawns, ReverieDreams.config().enableYouseiSpawn, dreamBiome);
            if (context.getKey().filter(WAGYU_HABITAT::contains).isPresent()) {
                // Ordinary cows have weight 8 and groups of 4 in all five taigas.
                spawns.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(RDEntityTypes.WAGYU.value(), 2, 3), 4);
            }
        });
    }

    public static void add(Predicate<TagKey<Biome>> hasTag, SpawnProperties.Mutable spawns, boolean fairiesEnabled,
                           boolean dreamBiome) {
        for (Rule rule : RULES) {
            Population selected = rule.select(hasTag, fairiesEnabled, dreamBiome);
            if (selected != null)
                spawns.addSpawn(rule.category(),
                        new MobSpawnSettings.SpawnerData(rule.entity().get(), selected.min(), selected.max()), selected.weight());
        }
    }

    private EntityBiomeSpawns() {
    }
}
