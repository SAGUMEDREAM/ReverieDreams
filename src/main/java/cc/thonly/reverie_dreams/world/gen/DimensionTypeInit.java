package cc.thonly.reverie_dreams.world.gen;

import cc.thonly.reverie_dreams.Touhou;
import net.minecraft.registry.Registerable;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.intprovider.UniformIntProvider;
import net.minecraft.world.dimension.DimensionType;

import java.util.Optional;
import java.util.OptionalLong;

public class DimensionTypeInit {
    public static final RegistryKey<DimensionType> DREAM_WORLD = getOrCreateRegistryKey("dream_world");
    public static final RegistryKey<DimensionType> THE_MOON = getOrCreateRegistryKey("the_moon");

    public static void bootstrap(Registerable<DimensionType> context) {
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
                TagKey.of(RegistryKeys.BLOCK, Identifier.of("minecraft", "infiniburn_overworld")), // infiniburn
                Identifier.of("minecraft", "overworld"),                                          // effects
                0.0f,                                   // ambientLight
                Optional.empty(),                       // cloudHeight
                new DimensionType.MonsterSettings(      // monsterSettings
                        true,                           // piglinSafe
                        true,                           // hasRaids
                        UniformIntProvider.create(0, 7),// monsterSpawnLightLevel (UniformIntProvider)
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
                TagKey.of(RegistryKeys.BLOCK, Identifier.of("minecraft", "infiniburn_overworld")), // infiniburn
                Identifier.of("minecraft", "the_end"), // effects
                0.0f,                 // ambientLight
                Optional.empty(),     // cloudHeight
                new DimensionType.MonsterSettings(
                        false,                           // piglinSafe
                        true,                            // hasRaids
                        UniformIntProvider.create(0, 7), // monsterSpawnLightLevel
                        0                                // monsterSpawnBlockLightLimit
                )
        ));
    }

    public static void init() {

    }

    public static RegistryKey<DimensionType> getOrCreateRegistryKey(String name) {
        return RegistryKey.of(RegistryKeys.DIMENSION_TYPE, Identifier.of(Touhou.MOD_ID, name));
    }

}
