package cc.thonly.reverie_dreams.world;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.entity.Ghost;
import cc.thonly.reverie_dreams.entity.ForestYoukai;
import cc.thonly.reverie_dreams.entity.Tengu;
import cc.thonly.reverie_dreams.entity.WildPig;
import cc.thonly.reverie_dreams.entity.YoukaiRabbit;
import cc.thonly.reverie_dreams.entity.npc.BaseYousei;
import cc.thonly.reverie_dreams.registry.content.entity.RDEntityTypes;
import cc.thonly.reverie_dreams.world.dimension.RDBuiltinLevels;
import cc.thonly.reverie_dreams.world.gen.RDBuiltinBiomes;
import cc.thonly.reverie_dreams.world.gensokyo.tree.GensokyoForestType;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import cc.thonly.reverie_dreams.registry.tag.RDBiomeTags;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * Local spawn checks complement tag-selected biome tables; no blanket dimension restriction.
 */
public final class LandEntitySpawnRules {
    private static final int FAIRY_MIN_SKY_LIGHT = 8;
    private static final int BED_SUPPRESSION_RADIUS = 32;
    private static final long BED_CACHE_TTL = 100L;
    /**
     * POI lookups may load sections, and natural spawn checks run for every candidate position,
     * so bed presence is cached per chunk for a few seconds.
     */
    private static final Map<ServerLevel, Long2LongOpenHashMap> BED_CACHES = new WeakHashMap<>();

    public static boolean isNatural(EntitySpawnReason reason) {
        return reason == EntitySpawnReason.NATURAL || reason == EntitySpawnReason.CHUNK_GENERATION;
    }

    public static boolean forestYoukai(EntityType<ForestYoukai> type, ServerLevelAccessor level,
                                       EntitySpawnReason reason, BlockPos pos, RandomSource random) {
        if (!isNatural(reason))
            return Mob.checkMobSpawnRules(type, level, reason, pos, random);
        var dimension = level.getLevel().dimension();
        boolean gensokyo = dimension.equals(RDBuiltinLevels.GENSOKYO);
        if (!gensokyo && !dimension.equals(net.minecraft.world.level.Level.OVERWORLD))
            return false;
        var biome = level.getBiome(pos);
        if (gensokyo && (biome.is(RDBuiltinBiomes.MAPLE_FOREST) || biome.is(GensokyoForestType.MAPLE_FOREST.biomeKey())))
            return false;
        long time = Math.floorMod(level.getLevel().getOverworldClockTime(), 24000L);
        if (time < 13000 || time >= 23000 || level.getDifficulty() == Difficulty.PEACEFUL
                || !biome.is(RDBiomeTags.FOREST_YOUKAI_SPAWN_AREA) || !naturalSurface(level, pos)
                || !SpawnPlacementTypes.ON_GROUND.isSpawnPositionOk(level, pos, type))
            return false;
        return level.getEntitiesOfClass(ForestYoukai.class, new AABB(pos).inflate(64, 24, 64), Entity::isAlive).size() < 4;
    }

    public static boolean ghost(EntityType<Ghost> type, ServerLevelAccessor level,
                                EntitySpawnReason reason, BlockPos pos, RandomSource random) {
        if (!isNatural(reason)) {
            return Mob.checkMobSpawnRules(type, level, reason, pos, random);
        }
        return level.getLevel().getGameRules().get(RDBuiltInGameRules.DO_GHOST.value())
                && allows(type, level, pos)
                && SpawnPlacementTypes.ON_GROUND.isSpawnPositionOk(level, pos, type)
                && Monster.checkMonsterSpawnRules(type, level, reason, pos, random);
    }

    public static boolean allows(EntityType<?> type, ServerLevelAccessor level, BlockPos pos) {
        return EntityBiomeSpawns.RULES.stream().filter(rule -> rule.entity().get() == type)
                .anyMatch(rule -> rule.allows(level.getBiome(pos)::is, ReverieDreams.config().enableYouseiSpawn));
    }

    public static boolean naturalSurface(ServerLevelAccessor level, BlockPos pos) {
        return isNaturalGround(level, pos)
                && level.getBlockState(pos).isAir() && level.getBlockState(pos.above()).isAir();
    }

    private static boolean isNaturalGround(ServerLevelAccessor level, BlockPos pos) {
        var soil = level.getBlockState(pos.below());
        return (soil.is(BlockTags.DIRT) || soil.is(Blocks.GRASS_BLOCK) || soil.is(Blocks.PODZOL)
                || soil.is(Blocks.MYCELIUM) || soil.is(Blocks.MOSS_BLOCK))
                && pos.getY() >= level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, pos.getX(), pos.getZ()) - 8;
    }

    public static <T extends BaseYousei> boolean fairy(EntityType<T> type, ServerLevelAccessor level,
                                                       EntitySpawnReason reason, BlockPos pos, RandomSource random) {
        if (!isNatural(reason))
            return Mob.checkMobSpawnRules(type, level, reason, pos, random);
        if (level.getDifficulty() == Difficulty.PEACEFUL || !allows(type, level, pos))
            return false;
        if (reason == EntitySpawnReason.NATURAL && !playerSettlementAllows(level, pos))
            return false;
        boolean lowDensity = isLowDensity(level, pos);
        if (lowDensity || isDream(level)) {
            return naturalSurface(level, pos) && hasRoomForFairy(type, level, pos)
                    && random.nextFloat() < (lowDensity ? 0.43F : 0.6F);
        }
        return isNaturalGround(level, pos) && SpawnPlacementTypes.ON_GROUND.isSpawnPositionOk(level, pos, type)
                && hasRoomForFairy(type, level, pos);
    }

    public static boolean hasRoomForFairy(EntityType<?> type, ServerLevelAccessor level, BlockPos pos) {
        AABB area = new AABB(pos).inflate(10, 12, 10);
        boolean dream = isDream(level);
        if (dream) {
            // Flying between island tiers must not free another spawn slot in this column.
            area = new AABB(area.minX, level.getMinY(), area.minZ,
                    area.maxX, level.getMaxY() + 1, area.maxZ);
        }
        var nearby = level.getEntitiesOfClass(BaseYousei.class, area, Entity::isAlive);
        int specialLimit = dream || isLowDensity(level, pos) ? 2 : 3;
        return nearby.size() < 6 && (type == RDEntityTypes.YOUSEI.value()
                || nearby.stream().filter(entity -> entity.getType() != RDEntityTypes.YOUSEI.value()).count() < specialLimit);
    }

    private static boolean playerSettlementAllows(ServerLevelAccessor level, BlockPos pos) {
        if (level.dimensionType().hasSkyLight() && level.getBrightness(LightLayer.SKY, pos) < FAIRY_MIN_SKY_LIGHT)
            return false;
        return !isNearBed(level.getLevel(), pos);
    }

    private static boolean isNearBed(ServerLevel level, BlockPos pos) {
        Long2LongOpenHashMap cache = BED_CACHES.computeIfAbsent(level, ignored -> new Long2LongOpenHashMap());
        long chunkKey = ChunkPos.pack(pos);
        long now = level.getGameTime();
        long cached = cache.get(chunkKey);
        if (cached != 0 && now < cached >>> 1)
            return (cached & 1L) != 0;
        BlockPos center = ChunkPos.containing(pos).getMiddleBlockPosition(pos.getY());
        boolean nearBed = level.getPoiManager()
                .getInRange(poi -> poi.is(PoiTypes.HOME), center, BED_SUPPRESSION_RADIUS, PoiManager.Occupancy.ANY)
                .findAny().isPresent();
        if (cache.size() > 8192)
            cache.clear();
        cache.put(chunkKey, ((now + BED_CACHE_TTL) << 1) | (nearBed ? 1L : 0L));
        return nearBed;
    }

    private static boolean isDream(ServerLevelAccessor level) {
        return level.getLevel().dimension().equals(RDBuiltinLevels.DREAM_WORLD);
    }

    private static boolean isLowDensity(ServerLevelAccessor level, BlockPos pos) {
        return level.getBiome(pos).is(RDBiomeTags.YOUSEI_LOW_DENSITY_SPAWN_AREA);
    }

    public static <T extends Tengu> boolean tengu(EntityType<T> type, ServerLevelAccessor level,
                                                  EntitySpawnReason reason, BlockPos pos, RandomSource random) {
        if (!Mob.checkMobSpawnRules(type, level, reason, pos, random))
            return false;
        if (!isNatural(reason))
            return true;
        boolean primaryHabitat = level.getBiome(pos).is(RDBiomeTags.TENGU_PRIMARY_HABITAT);
        int radius = primaryHabitat ? 48 : 64;
        int limit = primaryHabitat ? 5 : 3;
        return allows(type, level, pos)
                && pos.getY() >= level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, pos.getX(), pos.getZ()) - 8
                && level.getBlockState(pos).isAir() && level.getBlockState(pos.above()).isAir()
                && SpawnPlacementTypes.ON_GROUND.isSpawnPositionOk(level, pos, type)
                && level.getEntitiesOfClass(Tengu.class, new AABB(pos).inflate(radius, 16, radius), Entity::isAlive).size() < limit;
    }

    public static boolean wildPig(EntityType<WildPig> type, ServerLevelAccessor level,
                                  EntitySpawnReason reason, BlockPos pos, RandomSource random) {
        if (!Animal.checkAnimalSpawnRules(type, level, reason, pos, random))
            return false;
        return !isNatural(reason) || level.getEntitiesOfClass(WildPig.class,
                new AABB(pos).inflate(40, 12, 40), Entity::isAlive).size() < 2;
    }

    /**
     * Shaded fungus groves are this mob's habitat; the glow of magic mushrooms must not veto it,
     * so the vanilla monster light roll is replaced by a simple shade limit. The level-adjusted
     * brightness keeps open daylight out while still allowing night and canopy cover.
     */
    public static boolean mushroomMonster(EntityType<? extends Mob> type, ServerLevelAccessor level,
                                          EntitySpawnReason reason, BlockPos pos, RandomSource random) {
        if (!isNatural(reason))
            return Mob.checkMobSpawnRules(type, level, reason, pos, random);
        return level.getDifficulty() != Difficulty.PEACEFUL
                && naturalSurface(level, pos)
                && level.getLevel().getMaxLocalRawBrightness(pos) <= 10;
    }

    public static boolean rabbit(EntityType<YoukaiRabbit> type, ServerLevelAccessor level,
                                 EntitySpawnReason reason, BlockPos pos, RandomSource random) {
        if (!Mob.checkMobSpawnRules(type, level, reason, pos, random)
                || !level.getBlockState(pos).isAir() || !level.getBlockState(pos.above()).isAir())
            return false;
        return !isNatural(reason) || allows(type, level, pos)
                && level.getEntitiesOfClass(YoukaiRabbit.class, new AABB(pos).inflate(16, 12, 16), Entity::isAlive).size() < 3;
    }

    private LandEntitySpawnRules() {
    }
}
