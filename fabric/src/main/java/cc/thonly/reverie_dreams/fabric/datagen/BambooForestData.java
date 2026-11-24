package cc.thonly.reverie_dreams.fabric.datagen;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.block.nature.*;
import cc.thonly.reverie_dreams.registry.content.block.RDBambooForestBlocks;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.minecraft.advancements.criterion.StatePropertiesPredicate;
import net.minecraft.client.data.models.*;
import net.minecraft.client.data.models.blockstates.*;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.loot.*;
import net.minecraft.world.level.storage.loot.entries.*;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.*;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import java.util.function.BiConsumer;
import static net.minecraft.client.data.models.BlockModelGenerators.*;

final class BambooForestData {
    private static MultiVariant model(String name){return plainVariant(ReverieDreams.id("block/bamboo_forest/"+name));}
    private static MultiVariant rotate(MultiVariant model,Direction d){return switch(d){case EAST->model.with(Y_ROT_90);case SOUTH->model.with(Y_ROT_180);case WEST->model.with(Y_ROT_270);default->model;};}
    static void models(BlockModelGenerators g){
        var leaf=MultiPartGenerator.multiPart(RDBambooForestBlocks.LEAF_CLUSTER.asBlock());
        var debris=MultiPartGenerator.multiPart(RDBambooForestBlocks.DEBRIS.asBlock());
        var litter=MultiPartGenerator.multiPart(RDBambooForestBlocks.LEAF_LITTER.asBlock());
        for(Direction d:Direction.Plane.HORIZONTAL){
            leaf.with(condition().term(BambooLeafClusterBlock.FACING,d),rotate(model("leaf_cluster"),d));
            for(var f:BambooDebrisBlock.Form.values())debris.with(condition().term(BambooDebrisBlock.FACING,d).term(BambooDebrisBlock.FORM,f),rotate(model("debris_"+f.getSerializedName()),d));
            for(int amount=1;amount<=4;amount++)litter.with(condition().term(BlockStateProperties.HORIZONTAL_FACING,d).term(BlockStateProperties.SEGMENT_AMOUNT,amount),rotate(model("litter_"+amount),d));
        }
        g.blockStateOutput.accept(leaf);g.blockStateOutput.accept(debris);g.blockStateOutput.accept(litter);
        g.blockStateOutput.accept(MultiVariantGenerator.dispatch(RDBambooForestBlocks.LEAF_BUSH.asBlock(),model("leaf_bush")));
        g.registerSimpleItemModel(RDBambooForestBlocks.LEAF_CLUSTER.asBlock(),ReverieDreams.id("block/bamboo_forest/leaf_cluster"));
        g.registerSimpleItemModel(RDBambooForestBlocks.LEAF_BUSH.asBlock(),ReverieDreams.id("block/bamboo_forest/leaf_bush"));
        g.registerSimpleItemModel(RDBambooForestBlocks.LEAF_LITTER.asBlock(),ReverieDreams.id("block/bamboo_forest/litter_4"));
        g.registerSimpleItemModel(RDBambooForestBlocks.DEBRIS.asBlock(),ReverieDreams.id("block/bamboo_forest/debris_stump"));
    }

    static void loot(BiConsumer<Block,LootTable.Builder> output){
        for(var block:RDBambooForestBlocks.ALL){
            var pool=LootPool.lootPool().setRolls(ConstantValue.exactly(1)).when(ExplosionCondition.survivesExplosion());
            if(block==RDBambooForestBlocks.LEAF_LITTER){
                var alternatives=AlternativesEntry.alternatives();
                for(int amount=4;amount>=1;amount--)alternatives.otherwise(LootItem.lootTableItem(block).when(
                        LootItemBlockStatePropertyCondition.hasBlockStateProperties(block.asBlock()).setProperties(StatePropertiesPredicate.Builder.properties()
                                .hasProperty(BlockStateProperties.SEGMENT_AMOUNT,amount))).apply(SetItemCountFunction.setCount(ConstantValue.exactly(amount))));
                pool.add(alternatives);
            }else pool.add(LootItem.lootTableItem(block));
            output.accept(block.asBlock(),LootTable.lootTable().withPool(pool));
        }
    }
    private BambooForestData(){}
}
