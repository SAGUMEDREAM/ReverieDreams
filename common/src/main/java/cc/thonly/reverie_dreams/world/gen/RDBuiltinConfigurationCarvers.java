package cc.thonly.reverie_dreams.world.gen;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.registry.content.block.RDBlocks;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.valueproviders.ConstantFloat;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.carver.CaveCarverConfiguration;
import net.minecraft.world.level.levelgen.carver.ConfiguredWorldCarver;
import net.minecraft.world.level.levelgen.carver.WorldCarver;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.heightproviders.UniformHeight;

@SuppressWarnings("deprecation")
public class RDBuiltinConfigurationCarvers {

    public static final ResourceKey<ConfiguredWorldCarver<?>> MOON_CAVE =
            ResourceKey.create(
                    Registries.CONFIGURED_CARVER,
                    ReverieDreams.id("moon_cave")
            );

    public static void init() {

    }

    public static void bootstrap(BootstrapContext<ConfiguredWorldCarver<?>> context) {
        context.register(
                MOON_CAVE,
                new ConfiguredWorldCarver<>(
                        WorldCarver.CAVE,
                        new CaveCarverConfiguration(
                                0.08F, // 生成概率

                                UniformHeight.of(
                                        VerticalAnchor.absolute(-32),
                                        VerticalAnchor.absolute(90)
                                ),

                                ConstantFloat.of(0.5F), // 垂直缩放

                                VerticalAnchor.absolute(-54), // 岩浆高度（不用基本无影响）

                                HolderSet.direct(
                                        RDBlocks.MOON_STONE.block().asBlock().builtInRegistryHolder()
                                ),

                                ConstantFloat.of(1.0F), // 水平半径

                                ConstantFloat.of(0.8F), // 垂直半径

                                ConstantFloat.of(-0.7F) // 地板高度
                        )
                )
        );
    }

    private static ResourceKey<ConfiguredWorldCarver<?>> getOrCreateRegistryKey(String name) {
        return ResourceKey.create(Registries.CONFIGURED_CARVER, ReverieDreams.id(name));
    }
}
