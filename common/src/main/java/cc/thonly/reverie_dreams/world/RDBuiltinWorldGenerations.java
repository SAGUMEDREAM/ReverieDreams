package cc.thonly.reverie_dreams.world;

import cc.thonly.reverie_dreams.world.dimension.RDBuiltInDimensions;
import cc.thonly.reverie_dreams.world.dimension.RDBuiltInDimensionTypes;
import cc.thonly.reverie_dreams.world.dimension.RDBuiltinLevels;
import cc.thonly.reverie_dreams.world.gen.*;

public class RDBuiltinWorldGenerations {
    public static void registerWorldGeneration() {
        RDBuiltinConfigurationFeatures.init();
        RDBuiltinPlacedFeatures.init();
        RDBuiltinChunkGenerations.init();
        RDBuiltinChunkGeneratorSettings.init();
        RDBuiltinLevels.init();
        RDBuiltinBiomes.init();
        RDBuiltinStructures.init();
        RDBuiltinStructureSets.init();
        RDBuiltinTemplatePools.init();
        RDBuiltinStructureTypes.init();
        RDBuiltInDimensionTypes.init();
        RDBuiltInDimensions.init();
    }
}
