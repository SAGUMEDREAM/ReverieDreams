package cc.thonly.reverie_dreams.datagen;

import cc.thonly.reverie_dreams.registry.content.block.RDBlocks;
import cc.thonly.reverie_dreams.block.base.FruitLeavesBlock;
import cc.thonly.reverie_dreams.datagen.generator.AbstractJsonElementWriterProvider;
import cc.thonly.reverie_dreams.data.FumoType;
import cc.thonly.reverie_dreams.registry.content.FumoTypes;
import cc.thonly.reverie_dreams.registry.content.block.RDWoodBlocks;
import cc.thonly.reverie_dreams.state.SixteenDirection;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;

import java.util.concurrent.CompletableFuture;

public class JsonElementWriterProvider extends AbstractJsonElementWriterProvider {
    public JsonElementWriterProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> future) {
        super(output, future);
    }

    @Override
    protected void configured() {
        for (FumoType fumoType : FumoTypes.getView()) {
            Block block = fumoType.block();
            this.addSixteenDirectionBlockState(block);
        }
        this.addSixteenDirectionBlockState(RDBlocks.MARISA_HAT_BLOCK);
        this.addFruityLeavesBlockState(RDWoodBlocks.LEMON_FRUIT_LEAVES, RDWoodBlocks.LEMON.leaves());
        this.addFruityLeavesBlockState(RDWoodBlocks.GINKGO_FRUIT_LEAVES, RDWoodBlocks.GINKGO.leaves());
        this.addFruityLeavesBlockState(RDWoodBlocks.PEACH_FRUIT_LEAVES, RDWoodBlocks.PEACH.leaves());
    }

    void addSixteenDirectionBlockState(Block block) {
        ResourceLocation key = BuiltInRegistries.BLOCK.getKey(block);
        JsonElement blockState = this.buildSixteenDirectionBlockState(block);
        JsonElement items = this.buildBlockItem(block);
        this.addElement(Type.ASSETS, key, "blockstates", blockState);
        this.addElement(Type.ASSETS, key, "items", items);
    }

    void addFruityLeavesBlockState(Block block, Block rawBlock) {
        ResourceLocation key = BuiltInRegistries.BLOCK.getKey(block);
        JsonElement blockState = this.buildFruityLeavesBlock(block, rawBlock);
        JsonElement items = this.buildBlockItem(block);
        this.addElement(Type.ASSETS, key, "blockstates", blockState);
        this.addElement(Type.ASSETS, key, "items", items);
    }

    JsonElement buildFruityLeavesBlock(Block block, Block rawBlock) {
        ResourceLocation blockKey = BuiltInRegistries.BLOCK.getKey(block);
        ResourceLocation rawBlockKey = BuiltInRegistries.BLOCK.getKey(rawBlock);
        JsonObject object = new JsonObject();
        JsonObject variants = new JsonObject();
        for (int i = 0; i <= FruitLeavesBlock.MAX_AGE; i++) {
            for (Integer distance : LeavesBlock.DISTANCE.getPossibleValues()) {
                for (Boolean persistent : LeavesBlock.PERSISTENT.getPossibleValues()) {
                    for (Boolean waterlogged : LeavesBlock.WATERLOGGED.getPossibleValues()) {
                        JsonObject element = new JsonObject();
                        String modelId = i == FruitLeavesBlock.MAX_AGE ? "%s:block/%s".formatted(blockKey.getNamespace(), blockKey.getPath()) : "%s:block/%s".formatted(rawBlockKey.getNamespace(), rawBlockKey.getPath());
                        element.addProperty("model", modelId);
                        String keyName = "fruit_age=%s,distance=%s,persistent=%s,waterlogged=%s".formatted(i, distance, persistent, waterlogged);
                        variants.add(keyName, element);
                    }
                }

            }
        }
        object.add("variants", variants);
        return object;
    }

    JsonElement buildSixteenDirectionBlockState(Block block) {
        ResourceLocation key = BuiltInRegistries.BLOCK.getKey(block);
        JsonObject object = new JsonObject();
        JsonObject variants = new JsonObject();

        for (SixteenDirection direction : SixteenDirection.values()) {
            JsonObject element = new JsonObject();
            float y = direction.getYaw();
            String modelId = "%s:block/%s".formatted(key.getNamespace(), key.getPath());

            int snappedY;
            if (y >= 315 || y < 45) {
                snappedY = 0;
            } else if (y < 135) {
                snappedY = 90;
            } else if (y < 225) {
                snappedY = 180;
            } else {
                snappedY = 270;
            }

            element.addProperty("model", modelId);
            if (snappedY != 0) {
                element.addProperty("y", snappedY);
            }

            String keyName = "facing_16=" + direction.getSerializedName();
            variants.add(keyName, element);
        }

        object.add("variants", variants);
        return object;
    }

    JsonElement buildBlockItem(Block block) {
        ResourceLocation blockKey = BuiltInRegistries.BLOCK.getKey(block);
        JsonObject object = new JsonObject();
        JsonObject model = new JsonObject();
        model.addProperty("type", "minecraft:model");
        model.addProperty("model", "%s:block/%s".formatted(blockKey.getNamespace(), blockKey.getPath()));
        object.add("model", model);
        return object;
    }
}
