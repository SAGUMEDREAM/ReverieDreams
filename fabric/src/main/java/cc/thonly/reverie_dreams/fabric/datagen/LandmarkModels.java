package cc.thonly.reverie_dreams.fabric.datagen;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.registry.content.block.RDLandmarkBlocks;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.data.models.blockstates.MultiPartGenerator;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.data.models.model.TexturedModel;
import net.minecraft.client.renderer.block.dispatch.Variant;
import net.minecraft.core.Direction;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.core.registries.BuiltInRegistries;
import static net.minecraft.client.data.models.BlockModelGenerators.*;

final class LandmarkModels {
    static void generate(BlockModelGenerators generator) {
        for(var entry:RDLandmarkBlocks.ALL) {
            var block=entry.asBlock();String name=BuiltInRegistries.BLOCK.getKey(block).getPath();
            var id=ReverieDreams.id("block/landmarks/"+name);
            if(entry==RDLandmarkBlocks.PURPLE_CHERRY_PETALS) {
                createCherryPetalFlowerBed(generator, block);
            } else if(block.defaultBlockState().hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
                var parts=MultiPartGenerator.multiPart(block);
                for(Direction d:Direction.Plane.HORIZONTAL) {
                    if(entry==RDLandmarkBlocks.GARDEN_SUNFLOWER) {
                        var top=WeightedList.<Variant>builder();
                        for(int v=0;v<3;v++)top.add(new Variant(ReverieDreams.id("block/landmarks/garden_sunflower_upper_"+v)),v==0?6:2);
                        parts.with(condition().term(BlockStateProperties.HORIZONTAL_FACING,d).term(DoublePlantBlock.HALF,DoubleBlockHalf.UPPER),rotate(new MultiVariant(top.build()),d));
                        parts.with(condition().term(BlockStateProperties.HORIZONTAL_FACING,d).term(DoublePlantBlock.HALF,DoubleBlockHalf.LOWER),rotate(plainVariant(ReverieDreams.id("block/landmarks/garden_sunflower_lower")),d));
                    } else parts.with(condition().term(BlockStateProperties.HORIZONTAL_FACING,d),rotate(plainVariant(id),d));
                }
                generator.blockStateOutput.accept(parts);
                if(entry==RDLandmarkBlocks.RED_SPIDER_LILY)generator.registerSimpleFlatItemModel(block);
                else generator.registerSimpleItemModel(block,entry==RDLandmarkBlocks.GARDEN_SUNFLOWER?ReverieDreams.id("block/landmarks/garden_sunflower_upper_0"):id);
            } else {
                generator.blockStateOutput.accept(MultiVariantGenerator.dispatch(block,plainVariant(id)));
                if(entry==RDLandmarkBlocks.PURPLE_CHERRY_LEAVES)generator.registerSimpleItemModel(block,id);
                else generator.registerSimpleFlatItemModel(block);
            }
        }
    }
    private static void createCherryPetalFlowerBed(BlockModelGenerators generator, Block block) {
        String name = BuiltInRegistries.BLOCK.getKey(block).getPath();
        MultiVariant amount1 = plainVariant(TexturedModel.FLOWERBED_1.create(block, generator.modelOutput));
        MultiVariant amount2 = plainVariant(TexturedModel.FLOWERBED_2.create(block, generator.modelOutput));
        MultiVariant amount3 = plainVariant(TexturedModel.FLOWERBED_3.create(block, generator.modelOutput));
        MultiVariant amount4 = plainVariant(TexturedModel.FLOWERBED_4.create(block, generator.modelOutput));
        // Vanilla's flower bed item icon samples item/<name>; this landmark only ships a block sprite.
        var itemModel = ModelTemplates.FLAT_ITEM.create(ReverieDreams.id("item/" + name),
                new TextureMapping().put(TextureSlot.LAYER0, TextureMapping.getBlockTexture(block)),
                generator.modelOutput);
        generator.registerSimpleItemModel(block, itemModel);
        generator.createSegmentedBlock(block, amount1,
                condition -> condition,
                amount2, condition -> condition.term(BlockStateProperties.FLOWER_AMOUNT, 2, 3, 4),
                amount3, condition -> condition.term(BlockStateProperties.FLOWER_AMOUNT, 3, 4),
                amount4, condition -> condition.term(BlockStateProperties.FLOWER_AMOUNT, 4));
    }

    private static MultiVariant rotate(MultiVariant model,Direction d) {
        return switch(d){case NORTH->model.with(Y_ROT_270);case SOUTH->model.with(Y_ROT_90);case WEST->model.with(Y_ROT_180);default->model;};
    }
    private LandmarkModels() {}
}
