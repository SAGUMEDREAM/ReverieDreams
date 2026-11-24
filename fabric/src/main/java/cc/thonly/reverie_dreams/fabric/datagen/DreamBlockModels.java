package cc.thonly.reverie_dreams.fabric.datagen;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.block.dream.FootstepMossBlock;
import cc.thonly.reverie_dreams.block.dream.MirrorBridgeBlock;
import cc.thonly.reverie_dreams.registry.content.block.RDBlocks;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;

import java.util.List;

import static net.minecraft.client.data.models.BlockModelGenerators.plainVariant;

final class DreamBlockModels {
    private static final List<String> TRACE_SUFFIXES = List.of("", "_dim", "_lit");

    static void generate(BlockModelGenerators generator) {
        Block moss = RDBlocks.FOOTSTEP_MOSS.asBlock();
        List<Identifier> mossModels = TRACE_SUFFIXES.stream()
                .map(suffix -> ModelTemplates.CARPET.createWithSuffix(moss, suffix,
                        new TextureMapping().put(TextureSlot.WOOL, TextureMapping.getBlockTexture(moss, suffix)),
                        generator.modelOutput))
                .toList();
        generator.blockStateOutput.accept(MultiVariantGenerator.dispatch(moss)
                .with(PropertyDispatch.initial(FootstepMossBlock.TRACE).generate(trace -> plainVariant(mossModels.get(trace)))));
        generator.registerSimpleItemModel(moss, mossModels.getFirst());

        Identifier deck = ReverieDreams.id("block/mirror_bridge");
        Identifier warning = ReverieDreams.id("block/mirror_bridge_fading");
        generator.modelOutput.accept(deck, () -> mirrorDeck(deck));
        generator.modelOutput.accept(warning, () -> mirrorDeck(warning));
        generator.blockStateOutput.accept(MultiVariantGenerator.dispatch(RDBlocks.MIRROR_BRIDGE.get())
                .with(PropertyDispatch.initial(MirrorBridgeBlock.FADING)
                        .generate(fading -> plainVariant(fading ? warning : deck))));
    }

    private static JsonObject mirrorDeck(Identifier texture) {
        JsonObject model = new JsonObject();
        model.addProperty("ambientocclusion", false);
        JsonObject textures = new JsonObject();
        textures.addProperty("deck", texture.toString());
        textures.addProperty("particle", texture.toString());
        model.add("textures", textures);

        JsonObject deck = new JsonObject();
        deck.add("from", coordinates(0, 12, 0));
        deck.add("to", coordinates(16, 16, 16));
        JsonObject faces = new JsonObject();
        for (String direction : List.of("north", "east", "south", "west", "up", "down")) {
            JsonObject face = new JsonObject();
            face.addProperty("texture", "#deck");
            if (!direction.equals("up") && !direction.equals("down")) face.add("uv", coordinates(0, 0, 16, 4));
            faces.add(direction, face);
        }
        deck.add("faces", faces);
        JsonArray elements = new JsonArray();
        elements.add(deck);
        model.add("elements", elements);
        return model;
    }

    private static JsonArray coordinates(int... values) {
        JsonArray array = new JsonArray();
        for (int value : values) array.add(value);
        return array;
    }

    private DreamBlockModels() {
    }
}
