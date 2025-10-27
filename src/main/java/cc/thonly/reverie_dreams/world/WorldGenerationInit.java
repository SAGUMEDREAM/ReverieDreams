package cc.thonly.reverie_dreams.world;

import cc.thonly.reverie_dreams.world.dimension.DimensionInit;
import cc.thonly.reverie_dreams.world.dimension.DimensionTypeInit;
import cc.thonly.reverie_dreams.world.dimension.WorldInit;
import cc.thonly.reverie_dreams.world.gen.*;
import cc.thonly.reverie_dreams.world.gen.ModStructures;
import cc.thonly.reverie_dreams.world.gen.ModStructureSets;
import cc.thonly.reverie_dreams.world.gen.ModTemplatePools;

public class WorldGenerationInit {
    public static void registerWorldGeneration() {
        ConfigurationFeatureInit.init();
        PlacedFeaturesInit.init();
        ChunkGenerationInit.init();
        ChunkGeneratorSettingsInit.init();
        WorldInit.init();
        BiomeInit.init();
        ModStructures.init();
        ModStructureSets.init();
        ModTemplatePools.init();
        ModStructureTypes.init();
        DimensionTypeInit.init();
        DimensionInit.init();
    }
}
