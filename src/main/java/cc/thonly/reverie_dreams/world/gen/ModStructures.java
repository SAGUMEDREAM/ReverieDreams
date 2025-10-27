package cc.thonly.reverie_dreams.world.gen;

import cc.thonly.reverie_dreams.Touhou;
import com.mojang.datafixers.util.Pair;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBiomeTags;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registerable;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.structure.pool.StructurePool;
import net.minecraft.structure.pool.StructurePoolElement;
import net.minecraft.world.Heightmap;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.StructureTerrainAdaptation;
import net.minecraft.world.gen.YOffset;
import net.minecraft.world.gen.heightprovider.ConstantHeightProvider;
import net.minecraft.world.gen.heightprovider.HeightProvider;
import net.minecraft.world.gen.heightprovider.UniformHeightProvider;
import net.minecraft.world.gen.structure.JigsawStructure;
import net.minecraft.world.gen.structure.Structure;

import java.util.Arrays;
import java.util.List;

public class ModStructures {

    public static final RegistryKey<Structure> ABANDONED_ALTAR = getOrCreateRegistryKey("abandoned_altar");
    public static final RegistryKey<Structure> ABANDONED_TORII = getOrCreateRegistryKey("abandoned_torii");
    public static final RegistryKey<Structure> MINI_BAR = getOrCreateRegistryKey("mini_bar");
    public static final RegistryKey<Structure> BAMBOO_FOREST_BBQ_STALL = getOrCreateRegistryKey("bamboo_forest_bbq_stall");
    public static final RegistryKey<Structure> BAMBOO_FOREST_HUT = getOrCreateRegistryKey("bamboo_forest_hut");
    public static final RegistryKey<Structure> SAKURAZUKA = getOrCreateRegistryKey("sakurazuka");
    public static final RegistryKey<Structure> OUTER_SHRINE = getOrCreateRegistryKey("outer_shrine");

    public static void init() {

    }

    public static void bootstrap(Registerable<Structure> context) {
        RegistryEntryLookup<Biome> biomeLookup = context.getRegistryLookup(RegistryKeys.BIOME);
        RegistryEntryLookup<StructurePool> structurePoolLookup = context.getRegistryLookup(RegistryKeys.TEMPLATE_POOL);
        Structure.Config. Builder forest = new Structure.Config.Builder(biomeLookup.getOrThrow(ConventionalBiomeTags.IS_FOREST));
        forest.step(GenerationStep.Feature.SURFACE_STRUCTURES)
                .terrainAdaptation(StructureTerrainAdaptation.BEARD_THIN);
        Structure.Config. Builder plains = new Structure.Config.Builder(biomeLookup.getOrThrow(ConventionalBiomeTags.IS_PLAINS));
        plains.step(GenerationStep.Feature.SURFACE_STRUCTURES)
                .terrainAdaptation(StructureTerrainAdaptation.BEARD_THIN);
        Structure.Config. Builder hill = new Structure.Config.Builder(biomeLookup.getOrThrow(ConventionalBiomeTags.IS_HILL));
        hill.step(GenerationStep.Feature.SURFACE_STRUCTURES)
                .terrainAdaptation(StructureTerrainAdaptation.BEARD_THIN);
        Structure.Config. Builder taiga = new Structure.Config.Builder(biomeLookup.getOrThrow(ConventionalBiomeTags.IS_TAIGA));
        taiga.step(GenerationStep.Feature.SURFACE_STRUCTURES)
                .terrainAdaptation(StructureTerrainAdaptation.BEARD_THIN);
        Structure.Config. Builder bambooForest = new Structure.Config.Builder(RegistryEntryList.of(biomeLookup.getOrThrow(BiomeKeys.BAMBOO_JUNGLE)));
        taiga.step(GenerationStep.Feature.SURFACE_STRUCTURES)
                .terrainAdaptation(StructureTerrainAdaptation.BEARD_THIN);
        Structure.Config. Builder cherryForest = new Structure.Config.Builder(RegistryEntryList.of(biomeLookup.getOrThrow(BiomeKeys.CHERRY_GROVE)));
        taiga.step(GenerationStep.Feature.SURFACE_STRUCTURES)
                .terrainAdaptation(StructureTerrainAdaptation.BEARD_THIN);

        context.register(
                ABANDONED_ALTAR,
                new JigsawStructure(forest.build(), structurePoolLookup.getOrThrow(ModTemplatePools.ABANDONED_ALTAR), 1, ConstantHeightProvider.create(YOffset.fixed(0)), false, Heightmap.Type.WORLD_SURFACE_WG)
        );
        context.register(
                ABANDONED_TORII,
                new JigsawStructure(taiga.build(), structurePoolLookup.getOrThrow(ModTemplatePools.ABANDONED_TORII), 1, ConstantHeightProvider.create(YOffset.fixed(0)), false, Heightmap.Type.WORLD_SURFACE_WG)
        );
        context.register(
                MINI_BAR,
                new JigsawStructure(plains.build(), structurePoolLookup.getOrThrow(ModTemplatePools.MINI_BAR), 1, ConstantHeightProvider.create(YOffset.fixed(0)), false, Heightmap.Type.WORLD_SURFACE_WG)
        );
        context.register(
                OUTER_SHRINE,
                new JigsawStructure(forest.build(), structurePoolLookup.getOrThrow(ModTemplatePools.OUTER_SHRINE), 1, ConstantHeightProvider.create(YOffset.fixed(0)), false, Heightmap.Type.WORLD_SURFACE_WG)
        );
        context.register(
                BAMBOO_FOREST_BBQ_STALL,
                new JigsawStructure(bambooForest.build(), structurePoolLookup.getOrThrow(ModTemplatePools.BAMBOO_FOREST_BBQ_STALL), 1, ConstantHeightProvider.create(YOffset.fixed(0)), false, Heightmap.Type.WORLD_SURFACE_WG)
        );
        context.register(
                BAMBOO_FOREST_HUT,
                new JigsawStructure(bambooForest.build(), structurePoolLookup.getOrThrow(ModTemplatePools.BAMBOO_FOREST_HUT), 1, ConstantHeightProvider.create(YOffset.fixed(0)), false, Heightmap.Type.WORLD_SURFACE_WG)
        );
        context.register(
                SAKURAZUKA,
                new JigsawStructure(cherryForest.build(), structurePoolLookup.getOrThrow(ModTemplatePools.SAKURAZUKA), 1, ConstantHeightProvider.create(YOffset.fixed(0)), false, Heightmap.Type.WORLD_SURFACE_WG)
        );
    }

    public static RegistryEntry.Reference<Structure> register(Registerable<Structure> registry, RegistryKey<Structure> registryKey, Structure structure) {
        return registry.register(registryKey, structure);
    }

    public static RegistryKey<Structure> getOrCreateRegistryKey(String name) {
        return RegistryKey.of(RegistryKeys.STRUCTURE, Touhou.id(name));
    }
}
