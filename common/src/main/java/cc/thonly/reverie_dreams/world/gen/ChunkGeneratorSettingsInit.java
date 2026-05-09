package cc.thonly.reverie_dreams.world.gen;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.registry.content.block.RDBlocks;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.*;

import java.util.ArrayList;

public class ChunkGeneratorSettingsInit {
    public static final ResourceKey<NoiseGeneratorSettings> MOON = getOrCreateRegistryKey("moon");

    public static void bootstrap(BootstrapContext<NoiseGeneratorSettings> context) {
        HolderGetter<DensityFunction> densityFunctionLookup = context.lookup(Registries.DENSITY_FUNCTION);
        NoiseGeneratorSettings moon = new NoiseGeneratorSettings(
                NoiseSettings.create(0, 128, 2, 1),
                RDBlocks.MOON_STONE.block().defaultBlockState(),
                Blocks.AIR.defaultBlockState(),
                new NoiseRouter(
                        DensityFunctions.zero(),
                        DensityFunctions.zero(),
                        DensityFunctions.zero(),
                        DensityFunctions.zero(),
                        DensityFunctions.zero(),
                        DensityFunctions.zero(),
                        DensityFunctions.zero(),
                        DensityFunctions.zero(),
                        DensityFunctions.zero(),
                        DensityFunctions.zero(),
                        NoiseRouterData.slide(DensityFunctions.constant(-0.703125), 0, 128, 80, 64, -0.1, 0, 24, 0.1),
                        NoiseRouterData.postProcess(method_50924(NoiseRouterData.getFunction(densityFunctionLookup, NoiseRouterData.DEPTH))),
                        DensityFunctions.zero(),
                        DensityFunctions.zero(),
                        DensityFunctions.zero()
                ),
                SurfaceRules.sequence(
                        SurfaceRules.ifTrue(SurfaceRules.verticalGradient(
                                        "bedrock_floor",
                                        VerticalAnchor.bottom(), VerticalAnchor.aboveBottom(5)),
                                block(Blocks.BEDROCK)
                        ),
                        block(RDBlocks.MOON_STONE.block().asBlock())),
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
        return NoiseRouterData.slide(densityFunction, 0, 128, 80, 64, -0.1, 0, 24, 0.1);
    }

    private static SurfaceRules.RuleSource block(Block block) {
        return SurfaceRules.state(block.defaultBlockState());
    }

    public static void init() {

    }

    public static ResourceKey<NoiseGeneratorSettings> getOrCreateRegistryKey(String name) {
        return ResourceKey.create(Registries.NOISE_SETTINGS, Identifier.fromNamespaceAndPath(ReverieDreams.MOD_ID, name));
    }
}
