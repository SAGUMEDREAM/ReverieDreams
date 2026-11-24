package cc.thonly.reverie_dreams.fabric.datagen;

import cc.thonly.reverie_dreams.registry.content.block.RDBlocks;
import com.google.gson.JsonObject;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.blockstates.MultiPartGenerator;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.PipeBlock;
import java.util.List;

import static net.minecraft.client.data.models.BlockModelGenerators.*;

/** States/items reference the editable Blockbench exports; datagen never overwrites their geometry. */
final class MagicMushroomModels {
    static void generate(BlockModelGenerators generator) {
        for (var entry : List.of(RDBlocks.BLUE_MUSHROOM, RDBlocks.PURPLE_MUSHROOM)) {
            Block block = entry.asBlock();
            var model = ModelLocationUtils.getModelLocation(block);
            generator.blockStateOutput.accept(MultiVariantGenerator.dispatch(block, plainVariant(model)));
            // The placed model uses a block atlas, but the inventory sprite lives in textures/item.
            var itemModel = model.withPath("item/" + model.getPath().substring("block/".length()));
            JsonObject flat = new JsonObject();
            flat.addProperty("parent", "minecraft:item/generated");
            JsonObject textures = new JsonObject();
            textures.addProperty("layer0", itemModel.toString());
            flat.add("textures", textures);
            generator.modelOutput.accept(itemModel, () -> flat);
            generator.registerSimpleItemModel(block, itemModel);
        }
        for (var entry : List.of(RDBlocks.BLUE_MUSHROOM_BLOCK, RDBlocks.PURPLE_MUSHROOM_BLOCK, RDBlocks.MAGIC_MUSHROOM_STEM)) {
            Block block = entry.asBlock();
            var model = ModelLocationUtils.getModelLocation(block);
            var parts = MultiPartGenerator.multiPart(block);
            for (Direction face : Direction.values()) for (boolean skin : new boolean[]{true, false}) {
                parts.with(condition().term(PipeBlock.PROPERTY_BY_DIRECTION.get(face), skin), plainVariant(
                        model.withPath(model.getPath() + (skin ? "_" : "_inside_") + face.getName())));
            }
            generator.blockStateOutput.accept(parts);
            generator.registerSimpleItemModel(block, model);
        }
    }
    private MagicMushroomModels() {}
}
