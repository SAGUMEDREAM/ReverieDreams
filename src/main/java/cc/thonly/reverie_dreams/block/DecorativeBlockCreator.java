package cc.thonly.reverie_dreams.block;

import cc.thonly.reverie_dreams.ReverieDreams;
import lombok.Getter;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.BlockFamilies;
import net.minecraft.data.BlockFamily;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class DecorativeBlockCreator extends AbstractBlockCreator {
    public static final List<DecorativeBlockCreator> INSTANCES = new ArrayList<>();
    public static final List<Item> BLOCK_ITEMS = new ArrayList<>();
    @Getter
    private BlockFamily blockFamily;
    private Block base;
    private Block block;
    private Block stair;
    private Block slab;
    private Block wall;

    private DecorativeBlockCreator(ResourceLocation id) {
        super(id.getPath(), id);
        INSTANCES.add(this);
    }

    private DecorativeBlockCreator(String name) {
        this(ReverieDreams.id(name));
    }

    public void base(Block base) {
        this.base = base;
    }

    public Block base() {
        return this.base;
    }

    public Block block() {
        return this.block;
    }

    public Block stair() {
        return this.stair;
    }

    public Block slab() {
        return this.slab;
    }

    public Block wall() {
        return this.wall;
    }

    @Override
    public Stream<Block> stream() {
        return Stream.of(
                this.block,
                this.stair,
                this.slab,
                this.wall
        ).filter(Objects::nonNull);
    }

    public void offerRecipe(RecipeProvider generator, @NotNull Item material) {
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(material);
        ItemLike start = this.base() == null ? material : this.base();
        if (start != this.block()) {
            generator.shaped(RecipeCategory.DECORATIONS, this.block(), 2)
                    .pattern("XX")
                    .pattern("XX")
                    .define('X', start)
                    .unlockedBy("has_" + id.getPath(), generator.has(start))
                    .save(generator.output, RecipeProvider.getSimpleRecipeName(this.block()));
        }
        generator.slab(RecipeCategory.BUILDING_BLOCKS, this.slab(), material);
        generator.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, this.slab(), this.block(), 2);
        generator.stairBuilder(this.stair(), Ingredient.of(material))
                .unlockedBy("has_" + id.getPath(), generator.has(material))
                .save(generator.output, RecipeProvider.getSimpleRecipeName(this.stair()));
        generator.stonecutterResultFromBase(RecipeCategory.BUILDING_BLOCKS, this.stair(), this.block());
        generator.wall(RecipeCategory.BUILDING_BLOCKS, this.wall(), material);
    }

    @Override
    protected DecorativeBlockCreator build() {
        this.block = ModBlocks.registerSimpleBlock(this.getId(), Block::new, BlockBehaviour.Properties.ofFullCopy(Blocks.STONE));
        this.stair = ModBlocks.registerSimpleBlock(suffix("stairs"), (settings) -> new StairBlock(this.block.defaultBlockState(), settings), BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_STAIRS));
        this.slab = ModBlocks.registerSimpleBlock(suffix("slab"), SlabBlock::new, BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_SLAB).noOcclusion());
        this.wall = ModBlocks.registerSimpleBlock(suffix("wall"), WallBlock::new, BlockBehaviour.Properties.ofFullCopy(Blocks.STONE_BRICK_WALL).noOcclusion());

        BuiltInRegistries.ITEM.addAlias(suffix("stair"), suffix("stairs"));

        this.stream().forEach((block) -> {
            BLOCK_ITEMS.add(block.asItem());
        });

        this.blockFamily = BlockFamilies.familyBuilder(this.block)
                .stairs(this.stair)
                .slab(this.slab)
                .wall(this.wall)
                .recipeGroupPrefix(this.getId().getPath()).recipeUnlockedBy("has_" + this.getId().getPath())
                .getFamily();
        return this;
    }

    public static DecorativeBlockCreator create(String name) {
        return new DecorativeBlockCreator(name);
    }

    public static DecorativeBlockCreator create(ResourceLocation id) {
        return new DecorativeBlockCreator(id);
    }
}
