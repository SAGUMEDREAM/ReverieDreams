package cc.thonly.reverie_dreams.fabric.datagen;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.block.nature.SplinteredLogBlock;
import cc.thonly.reverie_dreams.registry.content.block.RDDeadwoodBlocks;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.blockstates.MultiPartGenerator;
import net.minecraft.core.Direction;

import static net.minecraft.client.data.models.BlockModelGenerators.*;

/** References the authored geometry in main/resources; datagen never replaces the split models. */
final class DeadwoodModels {
    static void generate(BlockModelGenerators generator) {
        for (var entry : RDDeadwoodBlocks.SPLINTERED_LOGS.entrySet()) {
            var block = entry.getValue().asBlock();
            var parts = MultiPartGenerator.multiPart(block);
            for (int v = 0; v < 3; v++) for (Direction direction : Direction.Plane.HORIZONTAL) {
                var model = plainVariant(ReverieDreams.id("block/deadwood/splintered_" + entry.getKey() + "_log_" + v));
                model = switch (direction) {
                    case EAST -> model.with(Y_ROT_90); case SOUTH -> model.with(Y_ROT_180);
                    case WEST -> model.with(Y_ROT_270); default -> model;
                };
                parts.with(condition().term(SplinteredLogBlock.FACING, direction).term(SplinteredLogBlock.VARIANT, v), model);
            }
            generator.blockStateOutput.accept(parts);
            generator.registerSimpleItemModel(block, ReverieDreams.id("block/deadwood/splintered_" + entry.getKey() + "_log_0"));
        }
    }
    private DeadwoodModels() {}
}
