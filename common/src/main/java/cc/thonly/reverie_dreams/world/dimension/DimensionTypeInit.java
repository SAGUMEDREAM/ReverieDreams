package cc.thonly.reverie_dreams.world.dimension;

import cc.thonly.reverie_dreams.ReverieDreams;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.biome.OverworldBiomes;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.Musics;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.TimelineTags;
import net.minecraft.util.ARGB;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.attribute.*;
import net.minecraft.world.clock.WorldClock;
import net.minecraft.world.clock.WorldClocks;
import net.minecraft.world.level.CardinalLighting;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.timeline.Timeline;

import java.util.Optional;

public class DimensionTypeInit {
    public static final ResourceKey<DimensionType> DREAM_WORLD = getOrCreateRegistryKey("dream_world");
    public static final ResourceKey<DimensionType> THE_MOON = getOrCreateRegistryKey("the_moon");
    public static final ResourceKey<DimensionType> GENSOKYO = getOrCreateRegistryKey("gensokyo");

    public static void bootstrap(BootstrapContext<DimensionType> context) {
        HolderGetter<Timeline> timeLines = context.lookup(Registries.TIMELINE);
        HolderGetter<WorldClock> clocks = context.lookup(Registries.WORLD_CLOCK);
        context.register(DREAM_WORLD, new DimensionType(
                true,                       // hasFixedTime
                true,                                   // hasSkyLight
                false,                                  // hasCeiling
                false,                                  // hasEnderDragonFight
                1.0,                                    // coordinateScale
                0,                                      // minY
                256,                                    // height
                256,                                    // logicalHeight
                TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath("minecraft", "infiniburn_overworld")), // infiniburn
                0.0f,                                   // ambientLight
                new DimensionType.MonsterSettings(      // monsterSettings
                        UniformInt.of(0, 7),        // monsterSpawnLightLevel (UniformIntProvider)
                        0                               // monsterSpawnBlockLightLimit
                ),
                DimensionType.Skybox.NONE,
                CardinalLighting.Type.DEFAULT,
                EnvironmentAttributeMap.builder()
                        .set(EnvironmentAttributes.FOG_COLOR, -4138753)
                        .set(EnvironmentAttributes.SKY_COLOR, OverworldBiomes.calculateSkyColor(0.8F))
                        .set(EnvironmentAttributes.CLOUD_COLOR, ARGB.white(0.8F))
                        .set(EnvironmentAttributes.CLOUD_HEIGHT, 192.33F)
                        .set(EnvironmentAttributes.BACKGROUND_MUSIC, BackgroundMusic.OVERWORLD)
                        .set(EnvironmentAttributes.AMBIENT_SOUNDS, AmbientSounds.LEGACY_CAVE_SETTINGS)
                        .set(EnvironmentAttributes.PIGLINS_ZOMBIFY, false)
                        .set(EnvironmentAttributes.CAN_START_RAID, true)
                        .set(EnvironmentAttributes.NETHER_PORTAL_SPAWNS_PIGLINS, false)
                        .set(EnvironmentAttributes.BED_RULE, new BedRule(BedRule.Rule.ALWAYS, BedRule.Rule.NEVER, false, Optional.empty()))
                        .set(EnvironmentAttributes.RESPAWN_ANCHOR_WORKS, true)
                        .build(),    // hasRaids,
                timeLines.getOrThrow(TimelineTags.IN_OVERWORLD),
                Optional.of(clocks.getOrThrow(WorldClocks.OVERWORLD))
        ));
        context.register(THE_MOON, new DimensionType(
                true,     // hasFixedTime
                true,                 // hasSkyLight
                false,                // hasCeiling
                false,                // hasEnderDragonFight
                1.0,                  // coordinateScale
                0,                    // minY
                256,                  // height
                256,                  // logicalHeight
                TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath("minecraft", "infiniburn_overworld")), // infiniburn
                0.25f,                 // ambientLight
                new DimensionType.MonsterSettings(
                        UniformInt.of(0, 7),        // monsterSpawnLightLevel
                        0                           // monsterSpawnBlockLightLimit
                ),
                DimensionType.Skybox.END,
                CardinalLighting.Type.NETHER,
                EnvironmentAttributeMap.builder()
                        .set(EnvironmentAttributes.FOG_COLOR, -15199464)
                        .set(EnvironmentAttributes.SKY_LIGHT_COLOR, -1736449)
                        .set(EnvironmentAttributes.SKY_COLOR, -16777216)
                        .set(EnvironmentAttributes.SKY_LIGHT_FACTOR, 0.0F)
                        .set(EnvironmentAttributes.BACKGROUND_MUSIC, new BackgroundMusic(Musics.END))
                        .set(EnvironmentAttributes.AMBIENT_SOUNDS, AmbientSounds.LEGACY_CAVE_SETTINGS)
                        .set(EnvironmentAttributes.PIGLINS_ZOMBIFY, false)
                        .set(EnvironmentAttributes.CAN_START_RAID, true)
                        .set(EnvironmentAttributes.NETHER_PORTAL_SPAWNS_PIGLINS, true)
                        .set(EnvironmentAttributes.BED_RULE, BedRule.CAN_SLEEP_WHEN_DARK)
                        .set(EnvironmentAttributes.RESPAWN_ANCHOR_WORKS, true)
                        .build(),
                timeLines.getOrThrow(TimelineTags.IN_OVERWORLD),
                Optional.of(clocks.getOrThrow(WorldClocks.OVERWORLD))
        ));
    }

    public static void init() {

    }

    public static ResourceKey<DimensionType> getOrCreateRegistryKey(String name) {
        return ResourceKey.create(Registries.DIMENSION_TYPE, Identifier.fromNamespaceAndPath(ReverieDreams.MOD_ID, name));
    }

}
