package cc.thonly.reverie_dreams.registry.content.block;

import cc.thonly.reverie_dreams.block.base.FertilizableFlowerBlock;
import cc.thonly.reverie_dreams.block.base.FruitLeavesBlock;
import cc.thonly.reverie_dreams.block.creator.WoodCreator;
import cc.thonly.reverie_dreams.registry.content.item.RDIngredientItems;
import cc.thonly.reverie_dreams.world.sapling.SaplingGeneratorInit;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class RDWoodBlocks {
    public static final WoodCreator SPIRITUAL = WoodCreator.create("spiritual", SaplingGeneratorInit.SPIRITUAL_TREE).build();
    public static final WoodCreator LEMON = WoodCreator.create(
            "lemon", SaplingGeneratorInit.LEMON_TREE).build();
    public static final Block LEMON_FRUIT_LEAVES = RDBlocks.registerSimpleBlock(
            "lemon_fruit_leaves",
            (settings) -> new FruitLeavesBlock(RDIngredientItems.LEMON, LEMON.leaves(), settings), BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_LEAVES));
    public static final WoodCreator GINKGO = WoodCreator.create(
            "ginkgo", SaplingGeneratorInit.GINKGO_TREE).build();
    public static final Block GINKGO_FRUIT_LEAVES = RDBlocks.registerSimpleBlock(
            "ginkgo_fruit_leaves",
            (settings) -> new FruitLeavesBlock(RDIngredientItems.GINKGO, GINKGO.leaves(), settings), BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_LEAVES));
    public static final WoodCreator PEACH = WoodCreator.create(
            "peach", SaplingGeneratorInit.PEACH_TREE).build();
    public static final Block PEACH_FRUIT_LEAVES = RDBlocks.registerSimpleBlock(
            "peach_fruit_leaves",
            (settings) -> new FruitLeavesBlock(RDIngredientItems.PEACH, PEACH.leaves(), settings), BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_LEAVES));
    public static final Block UDUMBARA_FLOWER = RDBlocks.registerSimpleBlock(
            "udumbara_flower",
            (settings) -> new FertilizableFlowerBlock(MobEffects.REGENERATION, 3f, settings), RDPlantBlocks.createPlantSettings());
    public static final Block TREMELLA = RDBlocks.registerSimpleBlock(
            "tremella",
            (settings) -> new FlowerBlock(MobEffects.REGENERATION, 3f, settings), RDPlantBlocks.createPlantSettings());
    public static void registerBlocks() {

    }
}
