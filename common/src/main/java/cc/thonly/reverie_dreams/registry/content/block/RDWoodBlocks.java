package cc.thonly.reverie_dreams.registry.content.block;

import cc.thonly.keine.api.KeineRegistries;
import cc.thonly.keine.api.registry.StrippableBlockRegistry;
import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.block.base.FertilizableFlowerBlock;
import cc.thonly.reverie_dreams.block.base.FruitLeavesBlock;
import cc.thonly.reverie_dreams.block.bundle.WoodBundle;
import cc.thonly.reverie_dreams.registry.content.item.RDIngredientItems;
import cc.thonly.reverie_dreams.world.sapling.SaplingGeneratorInit;
import net.blay09.mods.balm.world.level.block.BalmBlockRegistrar;
import net.blay09.mods.balm.world.level.block.DeferredBlock;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class RDWoodBlocks {
    public static WoodBundle SPIRITUAL_BUNDLE;
    public static DeferredBlock BLESSED_SPIRITUAL_LOG;
    public static WoodBundle LEMON_BUNDLE;
    public static DeferredBlock LEMON_FRUIT_LEAVES;
    public static WoodBundle GINKGO_BUNDLE;
    public static DeferredBlock GINKGO_FRUIT_LEAVES;
    public static WoodBundle PEACH_BUNDLE;
    public static DeferredBlock PEACH_FRUIT_LEAVES;
    public static DeferredBlock UDUMBARA_FLOWER;
    public static DeferredBlock TREMELLA;

    public static void initialize(BalmBlockRegistrar registrar) {
        SPIRITUAL_BUNDLE = WoodBundle.create("spiritual", SaplingGeneratorInit.SPIRITUAL_TREE).build(registrar);
        BLESSED_SPIRITUAL_LOG = RDBlocks.registerBlock(registrar, "blessed_spiritual_log", RotatedPillarBlock::new, Blocks.OAK_LOG.properties());
        LEMON_BUNDLE = WoodBundle.create("lemon", SaplingGeneratorInit.LEMON_TREE).build(registrar);
        LEMON_FRUIT_LEAVES = RDBlocks.registerSimpleBlock(registrar, "lemon_fruit_leaves", (settings) -> new FruitLeavesBlock(RDIngredientItems.LEMON, LEMON_BUNDLE.leaves(), settings), BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_LEAVES));
        LEMON_BUNDLE.setFruitLeaves(LEMON_FRUIT_LEAVES);
        GINKGO_BUNDLE = WoodBundle.create("ginkgo", SaplingGeneratorInit.GINKGO_TREE).build(registrar);
        GINKGO_FRUIT_LEAVES = RDBlocks.registerSimpleBlock(registrar, "ginkgo_fruit_leaves", (settings) -> new FruitLeavesBlock(RDIngredientItems.GINKGO, GINKGO_BUNDLE.leaves(), settings), BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_LEAVES));
        GINKGO_BUNDLE.setFruitLeaves(GINKGO_FRUIT_LEAVES);
        PEACH_BUNDLE = WoodBundle.create("peach", SaplingGeneratorInit.PEACH_TREE).build(registrar);
        PEACH_FRUIT_LEAVES = RDBlocks.registerSimpleBlock(registrar, "peach_fruit_leaves", (settings) -> new FruitLeavesBlock(RDIngredientItems.PEACH, PEACH_BUNDLE.leaves(), settings), BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_LEAVES));
        PEACH_BUNDLE.setFruitLeaves(PEACH_FRUIT_LEAVES);
        UDUMBARA_FLOWER = RDBlocks.registerSimpleBlock(registrar, "udumbara_flower", (settings) -> new FertilizableFlowerBlock(MobEffects.REGENERATION, 3f, settings), RDPlantBlocks.createPlantSettings());
        TREMELLA = RDBlocks.registerSimpleBlock(registrar, "tremella", (settings) -> new FlowerBlock(MobEffects.REGENERATION, 3f, settings), RDPlantBlocks.createPlantSettings());
        KeineRegistries registries = ReverieDreams.getKeineRegistries();
        StrippableBlockRegistry strippableBlockRegistry = registries.strippableBlockRegistry();
        strippableBlockRegistry.register(context -> {
            context.add(RDWoodBlocks.BLESSED_SPIRITUAL_LOG, RDWoodBlocks.SPIRITUAL_BUNDLE.strippedLog());
        });
    }
}
