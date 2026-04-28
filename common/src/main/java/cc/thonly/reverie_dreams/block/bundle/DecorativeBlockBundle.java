package cc.thonly.reverie_dreams.block.bundle;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.registry.content.block.RDBlocks;
import lombok.Getter;
import net.blay09.mods.balm.world.level.block.BalmBlockRegistrar;
import net.blay09.mods.balm.world.level.block.DeferredBlock;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.BlockFamilies;
import net.minecraft.data.BlockFamily;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DecorativeBlockBundle extends AbstractBlockBundle {
    public static final List<DecorativeBlockBundle> INSTANCES = new ArrayList<>();
    private DeferredBlock base;
    private DeferredBlock block;
    private DeferredBlock stair;
    private DeferredBlock slab;
    private DeferredBlock wall;

    private DecorativeBlockBundle(Identifier id) {
        super(id.getPath(), id);
        INSTANCES.add(this);
    }

    private DecorativeBlockBundle(String name) {
        this(ReverieDreams.id(name));
    }

    public void base(DeferredBlock base) {
        this.base = base;
    }

    public DeferredBlock base() {
        return this.base;
    }

    public DeferredBlock block() {
        return this.block;
    }

    public DeferredBlock stair() {
        return this.stair;
    }

    public DeferredBlock slab() {
        return this.slab;
    }

    public DeferredBlock wall() {
        return this.wall;
    }

    @Override
    public Collection<DeferredBlock> stream() {
        return List.of(
                this.block,
                this.stair,
                this.slab,
                this.wall
        );
    }

    @Override
    public DecorativeBlockBundle build(BalmBlockRegistrar registrar) {
        this.block = RDBlocks.registerSimpleBlock(registrar, this.getId(), Block::new, BlockBehaviour.Properties.ofFullCopy(Blocks.STONE));
        this.stair = RDBlocks.registerSimpleBlock(registrar, suffix("stairs"), (settings) -> new StairBlock(this.block.defaultBlockState(), settings), BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_STAIRS));
        this.slab = RDBlocks.registerSimpleBlock(registrar, suffix("slab"), SlabBlock::new, BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_SLAB).noOcclusion());
        this.wall = RDBlocks.registerSimpleBlock(registrar, suffix("wall"), WallBlock::new, BlockBehaviour.Properties.ofFullCopy(Blocks.STONE_BRICK_WALL).noOcclusion());
        return this;
    }

    public static DecorativeBlockBundle create(String name) {
        return new DecorativeBlockBundle(name);
    }

    public static DecorativeBlockBundle create(Identifier id) {
        return new DecorativeBlockBundle(id);
    }
}
