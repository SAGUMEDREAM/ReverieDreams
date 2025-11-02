package cc.thonly.reverie_dreams.world.dimension;

import cc.thonly.reverie_dreams.ReverieDreams;
import java.util.Optional;
import java.util.OptionalLong;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.dimension.DimensionType;

public class DimensionTypeInit {
    public static final ResourceKey<DimensionType> DREAM_WORLD = getOrCreateRegistryKey("dream_world");
    public static final ResourceKey<DimensionType> THE_MOON = getOrCreateRegistryKey("the_moon");
    public static final ResourceKey<DimensionType> GENSOKYO = getOrCreateRegistryKey("gensokyo");

    public static void bootstrap(BootstrapContext<DimensionType> context) {
        context.register(DREAM_WORLD, new DimensionType(
                OptionalLong.empty(),                   // fixedTime
                true,                                   // hasSkyLight
                false,                                  // hasCeiling
                false,                                  // ultrawarm
                true,                                   // natural
                1.0,                                    // coordinateScale
                true,                                   // bedWorks
                true,                                   // respawnAnchorWorks
                0,                                      // minY
                256,                                    // height
                256,                                    // logicalHeight
                TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("minecraft", "infiniburn_overworld")), // infiniburn
                ResourceLocation.fromNamespaceAndPath("minecraft", "overworld"),                                          // effects
                0.0f,                                   // ambientLight
                Optional.empty(),                       // cloudHeight
                new DimensionType.MonsterSettings(      // monsterSettings
                        true,                           // piglinSafe
                        true,                           // hasRaids
                        UniformInt.of(0, 7),// monsterSpawnLightLevel (UniformIntProvider)
                        0                               // monsterSpawnBlockLightLimit
                )
        ));
        context.register(THE_MOON, new DimensionType(
                OptionalLong.empty(), // fixedTime 缺省
                true,                 // hasSkyLight
                false,                // hasCeiling
                false,                // ultrawarm
                true,                 // natural
                1.0,                  // coordinateScale
                true,                 // bedWorks
                false,                // respawnAnchorWorks
                0,                    // minY
                256,                  // height
                256,                  // logicalHeight
                TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("minecraft", "infiniburn_overworld")), // infiniburn
                ResourceLocation.fromNamespaceAndPath("minecraft", "the_end"), // effects
                0.0f,                 // ambientLight
                Optional.empty(),     // cloudHeight
                new DimensionType.MonsterSettings(
                        false,                           // piglinSafe
                        true,                            // hasRaids
                        UniformInt.of(0, 7), // monsterSpawnLightLevel
                        0                                // monsterSpawnBlockLightLimit
                )
        ));
    }

    public static void init() {

    }

    public static ResourceKey<DimensionType> getOrCreateRegistryKey(String name) {
        return ResourceKey.create(Registries.DIMENSION_TYPE, ResourceLocation.fromNamespaceAndPath(ReverieDreams.MOD_ID, name));
    }

}
