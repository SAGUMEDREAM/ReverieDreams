package cc.thonly.reverie_dreams.client;

import cc.thonly.reverie_dreams.registry.content.block.RDJapaneseLivingBlocks;
import cc.thonly.reverie_dreams.registry.content.block.RDJapaneseSet2Blocks;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Particle sampling only; never changes targeting, collision, drops or sound.
 */
public final class JapaneseFurnitureParticles {
    private static final class Furniture {
        private static final Set<Block> BLOCKS = Stream.concat(RDJapaneseLivingBlocks.ALL.stream(),
                RDJapaneseSet2Blocks.ALL.stream()).map(entry -> entry.value()).collect(Collectors.toUnmodifiableSet());
    }

    public static VoxelShape samplingShape(Block block, VoxelShape outline) {
        // Vanilla emits at least 2x2x2 particles PER outline box. Detailed furniture
        // outlines contain dozens of boxes: sample its envelope once, like a block.
        // Retain vanilla's factory and spacing, including ordinary small-block effects.
        if (!Furniture.BLOCKS.contains(block) || outline.isEmpty())
            return outline;
        return Shapes.create(outline.bounds());
    }

    private JapaneseFurnitureParticles() {
    }
}
