package cc.thonly.reverie_dreams.block.bundle;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.registry.content.block.RDBlocks;
import cc.thonly.reverie_dreams.registry.impl.BlockDelegate;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DecorativeBlockBundle extends AbstractBlockBundle {
    public static final List<DecorativeBlockBundle> INSTANCES = new ArrayList<>();
    private BlockDelegate base;
    private BlockDelegate block;
    private BlockDelegate stair;
    private BlockDelegate slab;
    private BlockDelegate wall;

    private DecorativeBlockBundle(Identifier id) {
        super(id.getPath(), id);
        INSTANCES.add(this);
    }

    private DecorativeBlockBundle(String name) {
        this(ReverieDreams.id(name));
    }

    public void base(BlockDelegate base) {
        this.base = base;
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

    @Override
    public DecorativeBlockBundle build() {
        this.block = RDBlocks.registerSimpleBlock(this.getId(), Block::new, BlockBehaviour.Properties.ofFullCopy(Blocks.STONE));
        this.stair = RDBlocks.registerSimpleBlock(suffix("stairs"), (settings) -> new StairBlock(this.block.defaultBlockState(), settings), BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_STAIRS));
        this.slab = RDBlocks.registerSimpleBlock(suffix("slab"), SlabBlock::new, BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_SLAB).noOcclusion());
        this.wall = RDBlocks.registerSimpleBlock(suffix("wall"), WallBlock::new, BlockBehaviour.Properties.ofFullCopy(Blocks.STONE_BRICK_WALL).noOcclusion());
        return this;
    }

    public static DecorativeBlockBundle create(String name) {
        return new DecorativeBlockBundle(name);
    }

    public static DecorativeBlockBundle create(Identifier id) {
        return new DecorativeBlockBundle(id);
    }
}
