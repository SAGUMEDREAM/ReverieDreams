package cc.thonly.reverie_dreams.world.gen;

import cc.thonly.reverie_dreams.Touhou;
import cc.thonly.reverie_dreams.block.ModBlocks;
import cc.thonly.reverie_dreams.mixin.accessor.GenerationShapeConfigAccessor;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.registry.Registerable;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.YOffset;
import net.minecraft.world.gen.chunk.ChunkGeneratorSettings;
import net.minecraft.world.gen.chunk.GenerationShapeConfig;
import net.minecraft.world.gen.densityfunction.DensityFunction;
import net.minecraft.world.gen.densityfunction.DensityFunctionTypes;
import net.minecraft.world.gen.densityfunction.DensityFunctions;
import net.minecraft.world.gen.noise.NoiseRouter;
import net.minecraft.world.gen.surfacebuilder.MaterialRules;

import java.util.ArrayList;

public class ChunkGeneratorSettingsInit {
    public static final RegistryKey<ChunkGeneratorSettings> MOON = getOrCreateRegistryKey("moon");

    public static void bootstrap(Registerable<ChunkGeneratorSettings> context) {
        RegistryEntryLookup<DensityFunction> densityFunctionLookup = context.getRegistryLookup(RegistryKeys.DENSITY_FUNCTION);
        ChunkGeneratorSettings moon = new ChunkGeneratorSettings(
                GenerationShapeConfig.create(0, 128, 2, 1),
                ModBlocks.MOON_STONE.block().getDefaultState(),
                Blocks.AIR.getDefaultState(),
                new NoiseRouter(
                        DensityFunctionTypes.zero(),
                        DensityFunctionTypes.zero(),
                        DensityFunctionTypes.zero(),
                        DensityFunctionTypes.zero(),
                        DensityFunctionTypes.zero(),
                        DensityFunctionTypes.zero(),
                        DensityFunctionTypes.zero(),
                        DensityFunctionTypes.zero(),
                        DensityFunctionTypes.zero(),
                        DensityFunctionTypes.zero(),
                        DensityFunctions.applySlides(DensityFunctionTypes.constant(-0.703125), 0, 128, 80, 64, -0.1, 0, 24, 0.1),
                        DensityFunctions.applyBlendDensity(method_50924(DensityFunctions.entryHolder(densityFunctionLookup, DensityFunctions.DEPTH_OVERWORLD))),
                        DensityFunctionTypes.zero(),
                        DensityFunctionTypes.zero(),
                        DensityFunctionTypes.zero()
                ),
                MaterialRules.sequence(
                        MaterialRules.condition(MaterialRules.verticalGradient(
                                        "bedrock_floor",
                                        YOffset.getBottom(), YOffset.aboveBottom(5)),
                                block(Blocks.BEDROCK)
                        ),
                        block(ModBlocks.MOON_STONE.block())),
                new ArrayList<>(),
                10,
                false,
                false,
                true,
                false

        );
        context.register(MOON, moon);
    }

    private static DensityFunction method_50924(DensityFunction densityFunction) {
        return DensityFunctions.applySlides(densityFunction, 0, 128, 80, 64, -0.1, 0, 24, 0.1);
    }

    private static MaterialRules.MaterialRule block(Block block) {
        return MaterialRules.block(block.getDefaultState());
    }

    public static void init() {

    }

    public static RegistryKey<ChunkGeneratorSettings> getOrCreateRegistryKey(String name) {
        return RegistryKey.of(RegistryKeys.CHUNK_GENERATOR_SETTINGS, Identifier.of(Touhou.MOD_ID, name));
    }
}
