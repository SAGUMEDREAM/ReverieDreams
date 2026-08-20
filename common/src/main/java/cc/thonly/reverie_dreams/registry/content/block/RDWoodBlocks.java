package cc.thonly.reverie_dreams.registry.content.block;

import cc.thonly.keine.api.KeineRegistries;
import cc.thonly.keine.api.registry.impl.StrippableBlockRegistry;
import cc.thonly.reverie_dreams.block.base.FertilizableFlowerBlock;
import cc.thonly.reverie_dreams.block.base.FruitLeavesBlock;
import cc.thonly.reverie_dreams.block.bundle.WoodBundle;
import cc.thonly.reverie_dreams.registry.MCBuiltInRegistries;
import cc.thonly.reverie_dreams.registry.content.item.RDIngredientItems;
import cc.thonly.reverie_dreams.registry.delegate.BlockDelegate;
import cc.thonly.reverie_dreams.world.sapling.SaplingGeneratorInit;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class RDWoodBlocks {
    public static final WoodBundle SPIRITUAL_BUNDLE = WoodBundle.create("spiritual", SaplingGeneratorInit.SPIRITUAL_TREE).build();
    public static final BlockDelegate BLESSED_SPIRITUAL_LOG = RDBlocks.registerBlock("blessed_spiritual_log", RotatedPillarBlock::new, Blocks.OAK_LOG.properties());
    public static final WoodBundle LEMON_BUNDLE = WoodBundle.create("lemon", SaplingGeneratorInit.LEMON_TREE).build();
    public static final BlockDelegate LEMON_FRUIT_LEAVES = RDBlocks.registerSimpleBlock("lemon_fruit_leaves", (settings) -> new FruitLeavesBlock(RDIngredientItems.LEMON, LEMON_BUNDLE.leaves(), settings), BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_LEAVES));
    public static final WoodBundle GINKGO_BUNDLE = WoodBundle.create("ginkgo", SaplingGeneratorInit.GINKGO_TREE).build();
    public static final BlockDelegate GINKGO_FRUIT_LEAVES = RDBlocks.registerSimpleBlock("ginkgo_fruit_leaves", (settings) -> new FruitLeavesBlock(RDIngredientItems.GINKGO, GINKGO_BUNDLE.leaves(), settings), BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_LEAVES));
    public static final WoodBundle PEACH_BUNDLE = WoodBundle.create("peach", SaplingGeneratorInit.PEACH_TREE).build();
    public static final BlockDelegate PEACH_FRUIT_LEAVES = RDBlocks.registerSimpleBlock("peach_fruit_leaves", (settings) -> new FruitLeavesBlock(RDIngredientItems.PEACH, PEACH_BUNDLE.leaves(), settings), BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_LEAVES));
    public static final BlockDelegate UDUMBARA_FLOWER = RDBlocks.registerSimpleBlock("udumbara_flower", (settings) -> new FertilizableFlowerBlock(MobEffects.REGENERATION, 3f, settings), RDPlantBlocks.createPlantSettings());
    public static final BlockDelegate TREMELLA = RDBlocks.registerSimpleBlock("tremella", (settings) -> new FlowerBlock(MobEffects.REGENERATION, 3f, settings), RDPlantBlocks.createPlantSettings());

    static {
        LEMON_BUNDLE.setFruitLeaves(LEMON_FRUIT_LEAVES);
        GINKGO_BUNDLE.setFruitLeaves(GINKGO_FRUIT_LEAVES);
        PEACH_BUNDLE.setFruitLeaves(PEACH_FRUIT_LEAVES);
    }

    public static void initialize() {
        KeineRegistries registries = MCBuiltInRegistries.KEINE_REGISTRIES;
        StrippableBlockRegistry strippableBlockRegistry = registries.strippableBlockRegistry();
        strippableBlockRegistry.register(context -> {
            context.add(RDWoodBlocks.BLESSED_SPIRITUAL_LOG.asHolder(), RDWoodBlocks.SPIRITUAL_BUNDLE.strippedLog().asHolder());
        });
    }
}
