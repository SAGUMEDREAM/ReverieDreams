package cc.thonly.reverie_dreams.world.gen;

import cc.thonly.reverie_dreams.ReverieDreams;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBiomeTags;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.heightproviders.ConstantHeight;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.TerrainAdjustment;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.structures.JigsawStructure;

public class ModStructures {

    public static final ResourceKey<Structure> ABANDONED_ALTAR = getOrCreateRegistryKey("abandoned_altar");
    public static final ResourceKey<Structure> ABANDONED_TORII = getOrCreateRegistryKey("abandoned_torii");
    public static final ResourceKey<Structure> MINI_BAR = getOrCreateRegistryKey("mini_bar");
    public static final ResourceKey<Structure> BAMBOO_FOREST_BBQ_STALL = getOrCreateRegistryKey("bamboo_forest_bbq_stall");
    public static final ResourceKey<Structure> BAMBOO_FOREST_HUT = getOrCreateRegistryKey("bamboo_forest_hut");
    public static final ResourceKey<Structure> SAKURAZUKA = getOrCreateRegistryKey("sakurazuka");
    public static final ResourceKey<Structure> OUTER_SHRINE = getOrCreateRegistryKey("outer_shrine");

    public static void init() {

    }

    public static void bootstrap(BootstrapContext<Structure> context) {
        HolderGetter<Biome> biomeLookup = context.lookup(Registries.BIOME);
        HolderGetter<StructureTemplatePool> structurePoolLookup = context.lookup(Registries.TEMPLATE_POOL);
        Structure.StructureSettings. Builder forest = new Structure.StructureSettings.Builder(biomeLookup.getOrThrow(ConventionalBiomeTags.IS_FOREST));
        forest.generationStep(GenerationStep.Decoration.SURFACE_STRUCTURES)
                .terrainAdapation(TerrainAdjustment.BEARD_THIN);
        Structure.StructureSettings. Builder plains = new Structure.StructureSettings.Builder(biomeLookup.getOrThrow(ConventionalBiomeTags.IS_PLAINS));
        plains.generationStep(GenerationStep.Decoration.SURFACE_STRUCTURES)
                .terrainAdapation(TerrainAdjustment.BEARD_THIN);
        Structure.StructureSettings. Builder hill = new Structure.StructureSettings.Builder(biomeLookup.getOrThrow(ConventionalBiomeTags.IS_HILL));
        hill.generationStep(GenerationStep.Decoration.SURFACE_STRUCTURES)
                .terrainAdapation(TerrainAdjustment.BEARD_THIN);
        Structure.StructureSettings. Builder taiga = new Structure.StructureSettings.Builder(biomeLookup.getOrThrow(ConventionalBiomeTags.IS_TAIGA));
        taiga.generationStep(GenerationStep.Decoration.SURFACE_STRUCTURES)
                .terrainAdapation(TerrainAdjustment.BEARD_THIN);
        Structure.StructureSettings. Builder bambooForest = new Structure.StructureSettings.Builder(HolderSet.direct(biomeLookup.getOrThrow(Biomes.BAMBOO_JUNGLE)));
        taiga.generationStep(GenerationStep.Decoration.SURFACE_STRUCTURES)
                .terrainAdapation(TerrainAdjustment.BEARD_THIN);
        Structure.StructureSettings. Builder cherryForest = new Structure.StructureSettings.Builder(HolderSet.direct(biomeLookup.getOrThrow(Biomes.CHERRY_GROVE)));
        taiga.generationStep(GenerationStep.Decoration.SURFACE_STRUCTURES)
                .terrainAdapation(TerrainAdjustment.BEARD_THIN);

        context.register(
                ABANDONED_ALTAR,
                new JigsawStructure(forest.build(), structurePoolLookup.getOrThrow(ModTemplatePools.ABANDONED_ALTAR), 1, ConstantHeight.of(VerticalAnchor.absolute(0)), false, Heightmap.Types.WORLD_SURFACE_WG)
        );
        context.register(
                ABANDONED_TORII,
                new JigsawStructure(taiga.build(), structurePoolLookup.getOrThrow(ModTemplatePools.ABANDONED_TORII), 1, ConstantHeight.of(VerticalAnchor.absolute(0)), false, Heightmap.Types.WORLD_SURFACE_WG)
        );
        context.register(
                MINI_BAR,
                new JigsawStructure(plains.build(), structurePoolLookup.getOrThrow(ModTemplatePools.MINI_BAR), 1, ConstantHeight.of(VerticalAnchor.absolute(0)), false, Heightmap.Types.WORLD_SURFACE_WG)
        );
        context.register(
                OUTER_SHRINE,
                new JigsawStructure(forest.build(), structurePoolLookup.getOrThrow(ModTemplatePools.OUTER_SHRINE), 1, ConstantHeight.of(VerticalAnchor.absolute(0)), false, Heightmap.Types.WORLD_SURFACE_WG)
        );
        context.register(
                BAMBOO_FOREST_BBQ_STALL,
                new JigsawStructure(bambooForest.build(), structurePoolLookup.getOrThrow(ModTemplatePools.BAMBOO_FOREST_BBQ_STALL), 1, ConstantHeight.of(VerticalAnchor.absolute(0)), false, Heightmap.Types.WORLD_SURFACE_WG)
        );
        context.register(
                BAMBOO_FOREST_HUT,
                new JigsawStructure(bambooForest.build(), structurePoolLookup.getOrThrow(ModTemplatePools.BAMBOO_FOREST_HUT), 1, ConstantHeight.of(VerticalAnchor.absolute(0)), false, Heightmap.Types.WORLD_SURFACE_WG)
        );
        context.register(
                SAKURAZUKA,
                new JigsawStructure(cherryForest.build(), structurePoolLookup.getOrThrow(ModTemplatePools.SAKURAZUKA), 1, ConstantHeight.of(VerticalAnchor.absolute(0)), false, Heightmap.Types.WORLD_SURFACE_WG)
        );
    }

    public static Holder.Reference<Structure> register(BootstrapContext<Structure> registry, ResourceKey<Structure> registryKey, Structure structure) {
        return registry.register(registryKey, structure);
    }

    public static ResourceKey<Structure> getOrCreateRegistryKey(String name) {
        return ResourceKey.create(Registries.STRUCTURE, ReverieDreams.id(name));
    }
}
