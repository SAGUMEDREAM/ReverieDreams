package cc.thonly.reverie_dreams.registry.content.block;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.api.registry.block.FlammableBlockRegistry;
import cc.thonly.reverie_dreams.registry.delegate.BlockDelegate;
import dev.architectury.registry.fuel.FuelRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.Arrays;
import java.util.List;

public final class RDWoolBlocks {
    public static final List<Family> FAMILIES = Arrays.stream(DyeColor.values())
            .map(RDWoolBlocks::register)
            .toList();

    private RDWoolBlocks() {
    }

    public static void initialize() {
    }

    private static Family register(DyeColor color) {
        String path = color.getName() + "_wool";
        Block wool = BuiltInRegistries.BLOCK.getValue(Identifier.withDefaultNamespace(path));
        BlockDelegate slab = RDBlocks.registerSimpleBlock(path + "_slab", SlabBlock::new,
                BlockBehaviour.Properties.ofFullCopy(wool));
        BlockDelegate stairs = RDBlocks.registerSimpleBlock(path + "_stairs",
                properties -> new StairBlock(wool.defaultBlockState(), properties),
                BlockBehaviour.Properties.ofFullCopy(wool));
        FlammableBlockRegistry.register(slab, 30, 60);
        FlammableBlockRegistry.register(stairs, 30, 60);
        ReverieDreams.COMMON_LATE_INIT.add(() -> {
            FuelRegistry.register(100, slab);
            FuelRegistry.register(100, stairs);
        });
        return new Family(color, wool, slab, stairs);
    }

    public record Family(DyeColor color, Block wool, BlockDelegate slab, BlockDelegate stairs) {
    }
}
