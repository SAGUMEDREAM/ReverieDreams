package cc.thonly.reverie_dreams.fabric.datagen;

import cc.thonly.reverie_dreams.block.base.FruitLeavesBlock;
import cc.thonly.reverie_dreams.data.FumoType;
import cc.thonly.reverie_dreams.fabric.datagen.generator.AbstractJsonElementWriterProvider;
import cc.thonly.reverie_dreams.item.base.ColoredSpawnEggItem;
import cc.thonly.reverie_dreams.registry.content.FumoTypes;
import cc.thonly.reverie_dreams.registry.content.block.RDBlocks;
import cc.thonly.reverie_dreams.registry.content.block.RDWoodBlocks;
import cc.thonly.reverie_dreams.state.SixteenDirection;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;

import java.util.concurrent.CompletableFuture;

public class JsonElementWriterProvider extends AbstractJsonElementWriterProvider {
    public JsonElementWriterProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> future) {
        super(output, future);
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void configured() {
        for (FumoType fumoType : FumoTypes.getView()) {
            Block block = fumoType.block();
            this.addSixteenDirectionBlockState(block);
        }
        this.addSixteenDirectionBlockState(RDBlocks.MARISA_HAT_BLOCK.asBlock());
        this.addFruityLeavesBlockState(RDWoodBlocks.LEMON_FRUIT_LEAVES.asBlock(), RDWoodBlocks.LEMON_BUNDLE.leaves().asBlock());
        this.addFruityLeavesBlockState(RDWoodBlocks.GINKGO_FRUIT_LEAVES.asBlock(), RDWoodBlocks.GINKGO_BUNDLE.leaves().asBlock());
        this.addFruityLeavesBlockState(RDWoodBlocks.PEACH_FRUIT_LEAVES.asBlock(), RDWoodBlocks.PEACH_BUNDLE.leaves().asBlock());

        this.addElement(Type.ASSETS, RDWoodBlocks.BLESSED_SPIRITUAL_LOG.asBlock().builtInRegistryHolder().key().identifier(), "blockstates", strToJson(
                "{\n" +
                        "  \"variants\": {\n" +
                        "    \"axis=x\": {\n" +
                        "      \"model\": \"reverie_dreams:block/blessed_spiritual_log\",\n" +
                        "      \"x\": 90,\n" +
                        "      \"y\": 90\n" +
                        "    },\n" +
                        "    \"axis=y\": {\n" +
                        "      \"model\": \"reverie_dreams:block/blessed_spiritual_log\"\n" +
                        "    },\n" +
                        "    \"axis=z\": {\n" +
                        "      \"model\": \"reverie_dreams:block/blessed_spiritual_log\",\n" +
                        "      \"x\": 90\n" +
                        "    }\n" +
                        "  }\n" +
                        "}"
        ));
        this.addElement(Type.ASSETS, RDWoodBlocks.BLESSED_SPIRITUAL_LOG.asBlock().builtInRegistryHolder().key().identifier(), "items", strToJson(
                "{\n" +
                        "  \"model\": {\n" +
                        "    \"type\": \"minecraft:model\",\n" +
                        "    \"model\": \"reverie_dreams:block/blessed_spiritual_log\"\n" +
                        "  }\n" +
                        "}"
        ));
        for (Item spawnEgg : ColoredSpawnEggItem.SPAWN_EGGS) {
            ResourceKey<Item> key = spawnEgg.builtInRegistryHolder().key();
            Identifier location = key.identifier();
            JsonElement element = strToJson("{\n" +
                    "  \"model\": {\n" +
                    "    \"type\": \"minecraft:condition\",\n" +
                    "    \"component\": \"minecraft:dyed_color\",\n" +
                    "    \"on_false\": {\n" +
                    "      \"type\": \"minecraft:model\",\n" +
                    "      \"model\": \"reverie_dreams:item/spawn_egg\"\n" +
                    "    },\n" +
                    "    \"on_true\": {\n" +
                    "      \"type\": \"minecraft:model\",\n" +
                    "      \"model\": \"reverie_dreams:item/spawn_egg_dyed\",\n" +
                    "      \"tints\": [\n" +
                    "        {\n" +
                    "          \"type\": \"minecraft:constant\",\n" +
                    "          \"value\": -1\n" +
                    "        },\n" +
                    "        {\n" +
                    "          \"type\": \"minecraft:dye\",\n" +
                    "          \"default\": 0\n" +
                    "        }\n" +
                    "      ]\n" +
                    "    },\n" +
                    "    \"property\": \"minecraft:has_component\"\n" +
                    "  }\n" +
                    "}");
            this.addElement(Type.ASSETS, location, "items", element);
        }
    }

    void addSixteenDirectionBlockState(Block block) {
        Identifier key = BuiltInRegistries.BLOCK.getKey(block);
        JsonElement blockState = this.buildSixteenDirectionBlockState(block);
        JsonElement items = this.buildBlockItem(block);
        this.addElement(Type.ASSETS, key, "blockstates", blockState);
        this.addElement(Type.ASSETS, key, "items", items);
    }

    void addFruityLeavesBlockState(Block block, Block rawBlock) {
        Identifier key = BuiltInRegistries.BLOCK.getKey(block);
        JsonElement blockState = this.buildFruityLeavesBlock(block, rawBlock);
        JsonElement items = this.buildBlockItem(block);
        this.addElement(Type.ASSETS, key, "blockstates", blockState);
        this.addElement(Type.ASSETS, key, "items", items);
    }

    JsonElement buildFruityLeavesBlock(Block block, Block rawBlock) {
        Identifier blockKey = BuiltInRegistries.BLOCK.getKey(block);
        Identifier rawBlockKey = BuiltInRegistries.BLOCK.getKey(rawBlock);
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
        Identifier key = BuiltInRegistries.BLOCK.getKey(block);
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
        Identifier blockKey = BuiltInRegistries.BLOCK.getKey(block);
        JsonObject object = new JsonObject();
        JsonObject model = new JsonObject();
        model.addProperty("type", "minecraft:model");
        model.addProperty("model", "%s:block/%s".formatted(blockKey.getNamespace(), blockKey.getPath()));
        object.add("model", model);
        return object;
    }
}
