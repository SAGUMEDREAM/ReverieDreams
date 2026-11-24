package cc.thonly.reverie_dreams.fabric.datagen;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.block.nature.LargeBambooBlock;
import cc.thonly.reverie_dreams.block.nature.LargeBambooShootBlock;
import cc.thonly.reverie_dreams.registry.content.block.RDBambooBlocks;
import cc.thonly.reverie_dreams.registry.content.item.RDIngredientItems;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.minecraft.advancements.criterion.StatePropertiesPredicate;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.blockstates.MultiPartGenerator;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BambooLeaves;
import net.minecraft.world.level.storage.loot.*;
import net.minecraft.world.level.storage.loot.entries.*;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.*;
import net.minecraft.world.level.storage.loot.providers.number.*;
import java.util.function.BiConsumer;
import static net.minecraft.client.data.models.BlockModelGenerators.*;

/** Data generation mirrors the authored resources; model geometry/paint is kept in tools/large-bamboo. */
final class LargeBambooData {
    static void models(BlockModelGenerators generator){
        var stem=MultiPartGenerator.multiPart(RDBambooBlocks.LARGE_BAMBOO.asBlock());
        for(var leaves:BambooLeaves.values())stem.with(condition().term(LargeBambooBlock.LEAVES,leaves),
                plainVariant(ReverieDreams.id("block/large_bamboo/"+leaves.getSerializedName())));
        generator.blockStateOutput.accept(stem);
        var shoot=MultiPartGenerator.multiPart(RDBambooBlocks.LARGE_BAMBOO_SHOOT.asBlock());
        for(int age=0;age<=2;age++)shoot.with(condition().term(LargeBambooShootBlock.AGE,age),plainVariant(ReverieDreams.id("block/large_bamboo/shoot_"+age)));
        generator.blockStateOutput.accept(shoot);
        generator.registerSimpleItemModel(RDBambooBlocks.LARGE_BAMBOO.asBlock(),ReverieDreams.id("block/large_bamboo/large"));
        generator.registerSimpleItemModel(RDBambooBlocks.LARGE_BAMBOO_SHOOT.asBlock(),ReverieDreams.id("block/large_bamboo/shoot_2"));
    }

    static void loot(BiConsumer<Block,LootTable.Builder> output){
        output.accept(RDBambooBlocks.LARGE_BAMBOO.asBlock(),LootTable.lootTable().withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1))
                .when(ExplosionCondition.survivesExplosion()).add(LootItem.lootTableItem(RDBambooBlocks.LARGE_BAMBOO))));
        output.accept(RDBambooBlocks.LARGE_BAMBOO_SHOOT.asBlock(),LootTable.lootTable().withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1))
                .when(ExplosionCondition.survivesExplosion()).add(AlternativesEntry.alternatives(
                        LootItem.lootTableItem(RDIngredientItems.BAMBOO_SHOOTS).when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(RDBambooBlocks.LARGE_BAMBOO_SHOOT.asBlock())
                                .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(LargeBambooShootBlock.AGE,2)))
                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(1,2))),
                        LootItem.lootTableItem(RDBambooBlocks.LARGE_BAMBOO_SHOOT)))));
    }
    private LargeBambooData(){}
}
