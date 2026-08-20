package cc.thonly.reverie_dreams.block.bundle;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.registry.content.block.RDBlocks;
import cc.thonly.reverie_dreams.registry.delegate.BlockDelegate;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

public class DecorativeBlockBundle extends AbstractBlockBundle {
    public static final List<DecorativeBlockBundle> INSTANCES = new ArrayList<>();
    private BlockDelegate base;
    private BlockDelegate block;
    private BlockDelegate stair;
    private BlockDelegate slab;
    private BlockDelegate wall;
    private final List<Function<BlockBehaviour.Properties, BlockBehaviour.Properties>> factories = new ArrayList<>();

    private DecorativeBlockBundle(Identifier id) {
        super(id.getPath(), id);
        INSTANCES.add(this);
    }

    private DecorativeBlockBundle(String name) {
        this(ReverieDreams.id(name));
    }


    @SafeVarargs
    public final DecorativeBlockBundle map(Function<BlockBehaviour.Properties, BlockBehaviour.Properties>... functions) {
        this.factories.addAll(Arrays.asList(functions));
        return this;
    }

    public DecorativeBlockBundle base(BlockDelegate base) {
        this.base = base;
        return this;
    }

    public BlockDelegate base() {
        return this.base;
    }

    public BlockDelegate block() {
        return this.block;
    }

    public BlockDelegate stair() {
        return this.stair;
    }

    public BlockDelegate slab() {
        return this.slab;
    }

    public BlockDelegate wall() {
        return this.wall;
    }

    @Override
    public Collection<BlockDelegate> stream() {
        return List.of(
                this.block,
                this.stair,
                this.slab,
                this.wall
        );
    }

    private BlockBehaviour.Properties apply(BlockBehaviour.Properties properties) {
        BlockBehaviour.Properties prev = properties;
        for (Function<BlockBehaviour.Properties, BlockBehaviour.Properties> factory : this.factories) {
            prev = factory.apply(prev);
        }
        return prev;
    }

    @Override
    public DecorativeBlockBundle build() {
        this.block = RDBlocks.registerSimpleBlock(this.getId(), Block::new, BlockBehaviour.Properties.ofFullCopy(Blocks.STONE));
        this.stair = RDBlocks.registerSimpleBlock(suffix("stairs"), (settings) -> new StairBlock(this.block.defaultBlockState(), settings), this.apply(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_STAIRS)));
        this.slab = RDBlocks.registerSimpleBlock(suffix("slab"), SlabBlock::new, this.apply(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_SLAB).noOcclusion()));
        this.wall = RDBlocks.registerSimpleBlock(suffix("wall"), WallBlock::new, this.apply(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE_BRICK_WALL).noOcclusion()));
        return this;
    }

    public static DecorativeBlockBundle create(String name) {
        return new DecorativeBlockBundle(name);
    }

    public static DecorativeBlockBundle create(Identifier id) {
        return new DecorativeBlockBundle(id);
    }
}
