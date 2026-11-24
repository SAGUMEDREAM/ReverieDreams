package cc.thonly.reverie_dreams.world.moon;

import cc.thonly.reverie_dreams.world.dimension.RDBuiltInDimensionTypes;
import cc.thonly.reverie_dreams.world.gen.biome.TheMoonBiomeCreator;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.minecraft.SharedConstants;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.registries.RegistryPatchGenerator;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.attribute.EnvironmentAttributeSystem;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.timeline.Timelines;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class MoonEnvironmentTest {
    private static HolderLookup.Provider registries;
    private static DimensionType moon;

    @BeforeAll static void bootstrap() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        registries = RegistryPatchGenerator.createLookup(
                CompletableFuture.completedFuture(VanillaRegistries.createLookup()),
                new RegistrySetBuilder().add(Registries.DIMENSION_TYPE, RDBuiltInDimensionTypes::bootstrap)).join().full();
        moon = registries.lookupOrThrow(Registries.DIMENSION_TYPE).getOrThrow(RDBuiltInDimensionTypes.THE_MOON).value();
    }

    private static Biome profile(int index) {
        return TheMoonBiomeCreator.createMoonBiome(new BiomeGenerationSettings.Builder(
                registries.lookupOrThrow(Registries.PLACED_FEATURE), registries.lookupOrThrow(Registries.CONFIGURED_CARVER)),
                index == 1, index == 2, index == 3);
    }

    @Test void neutralLightingSurvivesEveryMoonBiomeLayer() {
        for (int index = 0; index < 4; index++) {
            var environment = EnvironmentAttributeSystem.builder().addConstantLayer(moon.attributes())
                    .addConstantLayer(profile(index).getAttributes()).build();
            assertEquals(0xFFFFFF, environment.getValue(EnvironmentAttributes.SKY_LIGHT_COLOR, Vec3.ZERO, null) & 0xFFFFFF);
            int fog = environment.getValue(EnvironmentAttributes.FOG_COLOR, Vec3.ZERO, null);
            assertEquals((fog >> 16) & 255, (fog >> 8) & 255);
            assertEquals((fog >> 8) & 255, fog & 255);
            assertEquals(0, environment.getValue(EnvironmentAttributes.SKY_COLOR, Vec3.ZERO, null) & 0xFFFFFF);
            assertEquals(index == 2 ? 0.12f : 0.5f,
                    environment.getValue(EnvironmentAttributes.SKY_LIGHT_FACTOR, Vec3.ZERO, null), 0.00001f);
        }
        assertEquals(-128, moon.minY());
        assertEquals(352, moon.height());
        assertEquals(15f, moon.attributes().applyModifier(EnvironmentAttributes.SKY_LIGHT_LEVEL, 0f));
    }

    @Test void lunarPhasesAndSchedulesRemainWithoutAnyTimeDependentColorTrack() {
        assertEquals(Set.of(Timelines.MOON, Timelines.VILLAGER_SCHEDULE, Timelines.EARLY_GAME),
                moon.timelines().stream().map(holder -> holder.unwrapKey().orElseThrow()).collect(Collectors.toSet()));
        for (var timeline : moon.timelines()) {
            assertFalse(timeline.value().attributes().contains(EnvironmentAttributes.SKY_LIGHT_COLOR));
            assertFalse(timeline.value().attributes().contains(EnvironmentAttributes.FOG_COLOR));
            assertFalse(timeline.value().attributes().contains(EnvironmentAttributes.SKY_LIGHT_FACTOR));
        }
    }

    @Test void shippedDimensionAndFourBiomesMatchTheirActualBootstrapCodecs() throws Exception {
        var ops = registries.createSerializationContext(JsonOps.INSTANCE);
        var data = Path.of("src/generated/resources/data/reverie_dreams");
        var actual = JsonParser.parseString(Files.readString(data.resolve("dimension_type/the_moon.json")));
        var expected = DimensionType.DIRECT_CODEC.encodeStart(ops, moon).getOrThrow();
        assertEquals(JsonParser.parseString(expected.toString()), actual);
        String[] names = {"the_moon", "basalt_mare", "shadow_ice_crater", "moon_sea"};
        for (int index = 0; index < names.length; index++) {
            var biome = Biome.DIRECT_CODEC.encodeStart(ops, profile(index)).getOrThrow();
            assertEquals(JsonParser.parseString(biome.toString()),
                    JsonParser.parseString(Files.readString(data.resolve("worldgen/biome/" + names[index] + ".json"))));
        }
    }
}
