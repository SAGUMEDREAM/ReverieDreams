package cc.thonly.reverie_dreams.block;

import cc.thonly.mystias_izakaya.item.MIItems;
import cc.thonly.reverie_dreams.Touhou;
import cc.thonly.reverie_dreams.block.base.BasicPolymerBlock;
import cc.thonly.reverie_dreams.block.base.BasicPolymerSlabBlock;
import cc.thonly.reverie_dreams.block.base.BasicPolymerStairsBlock;
import cc.thonly.reverie_dreams.block.base.BasicPolymerWallBlock;
import cc.thonly.reverie_dreams.util.IdentifierGetter;
import eu.pb4.factorytools.api.block.model.generic.BlockStateModelManager;
import eu.pb4.polymer.blocks.api.BlockModelType;
import lombok.Getter;
import net.minecraft.block.*;
import net.minecraft.data.family.BlockFamilies;
import net.minecraft.data.family.BlockFamily;
import net.minecraft.data.recipe.RecipeGenerator;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

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

    private DecorativeBlockCreator(Identifier id) {
        super(id.getPath(), id);
        INSTANCES.add(this);
    }

    private DecorativeBlockCreator(String name) {
        this(Touhou.id(name));
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

    public void offerRecipe(RecipeGenerator generator, Item material) {
        Identifier id = Registries.ITEM.getId(material);
        ItemConvertible start = this.base() == null ? material : this.base();
        if (start != this.block()) {
            generator.createShaped(RecipeCategory.DECORATIONS, this.block())
                    .pattern("XX")
                    .pattern("XX")
                    .input('X', start)
                    .criterion("has_" + id.getPath(), generator.conditionsFromItem(start))
                    .offerTo(generator.exporter, RecipeGenerator.getRecipeName(this.block()));
        }
        generator.offerSlabRecipe(RecipeCategory.BUILDING_BLOCKS, this.slab(), material);
        generator.offerStonecuttingRecipe(RecipeCategory.BUILDING_BLOCKS, this.slab(), this.block(), 2);
        generator.createStairsRecipe(this.stair(), Ingredient.ofItem(material))
                .criterion("has_"+ id.getPath(), generator.conditionsFromItem(material))
                .offerTo(generator.exporter, RecipeGenerator.getRecipeName(this.stair()));
        generator.offerStonecuttingRecipe(RecipeCategory.BUILDING_BLOCKS, this.stair(), this.block());
        generator.offerWallRecipe(RecipeCategory.BUILDING_BLOCKS, this.wall(), material);
    }

    @Override
    protected DecorativeBlockCreator build() {
        this.block = new BasicPolymerBlock(this.getId(), BlockModelType.FULL_BLOCK, AbstractBlock.Settings.copy(Blocks.STONE));
        this.stair = new BasicPolymerStairsBlock(suffix("stair"), this.block.getDefaultState(), AbstractBlock.Settings.copy(Blocks.OAK_STAIRS));
        this.slab = new BasicPolymerSlabBlock(suffix("slab"), this.block.getDefaultState(), AbstractBlock.Settings.copy(Blocks.OAK_SLAB));
        this.wall = new BasicPolymerWallBlock(suffix("wall"), AbstractBlock.Settings.copy(Blocks.STONE_BRICK_WALL));
        this.stream().forEach((block) -> {
            IdentifierGetter blockImpl = (IdentifierGetter) block;
            Block rb = this.register(blockImpl);
            BLOCK_ITEMS.add(rb.asItem());
        });
        this.blockFamily = BlockFamilies.register(this.block)
                .stairs(this.stair)
                .slab(this.slab)
                .wall(this.wall)
                .group(this.getId().getPath()).unlockCriterionName("has_" + this.getId().getPath())
                .build();
        return this;
    }

    public static DecorativeBlockCreator create(String name) {
        return new DecorativeBlockCreator(name);
    }

    public static DecorativeBlockCreator create(Identifier id) {
        return new DecorativeBlockCreator(id);
    }
}
