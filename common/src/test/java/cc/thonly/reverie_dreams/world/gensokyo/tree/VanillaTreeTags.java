package cc.thonly.reverie_dreams.world.gensokyo.tree;

import com.google.gson.JsonParser;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.TagLoader;
import net.minecraft.world.level.block.Block;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

/** Read the version's real, nested vanilla tags: never invent tag membership for fixtures. */
final class VanillaTreeTags {
    static void bind() {
        Map<TagKey<Block>, List<Holder<Block>>> tags = new LinkedHashMap<>();
        load(BlockTags.SUBSTRATE_OVERWORLD, tags);
        load(BlockTags.REPLACEABLE_BY_TREES, tags);
        load(BlockTags.PREVENTS_NEARBY_LEAF_DECAY, tags);
        load(BlockTags.SUPPORTS_VEGETATION, tags);
        load(BlockTags.OVERRIDES_MUSHROOM_LIGHT_REQUIREMENT, tags);
        load(BlockTags.LOGS, tags);
        load(BlockTags.LEAVES, tags);
        load(BlockTags.PLANKS, tags);
        MaplePreviewFixture.addBlockTags(tags);
        BuiltInRegistries.BLOCK.prepareTagReload(new TagLoader.LoadResult<>(Registries.BLOCK, tags)).apply();
    }

    private static List<Holder<Block>> load(TagKey<Block> tag, Map<TagKey<Block>, List<Holder<Block>>> tags) {
        if (tags.containsKey(tag)) return tags.get(tag);
        String path = "/data/" + tag.location().getNamespace() + "/tags/block/" + tag.location().getPath() + ".json";
        var stream = VanillaTreeTags.class.getResourceAsStream(path);
        if (stream == null) throw new IllegalStateException("Missing actual vanilla tag: " + path);
        try (var reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            var values = JsonParser.parseReader(reader).getAsJsonObject().getAsJsonArray("values");
            var result = new LinkedHashSet<Holder<Block>>();
            for (var value : values) {
                String id = value.isJsonPrimitive() ? value.getAsString() : value.getAsJsonObject().get("id").getAsString();
                if (id.startsWith("#")) result.addAll(load(TagKey.create(Registries.BLOCK, Identifier.parse(id.substring(1))), tags));
                else result.add(BuiltInRegistries.BLOCK.getOrThrow(ResourceKey.create(Registries.BLOCK, Identifier.parse(id))));
            }
            var resolved = List.copyOf(result);
            tags.put(tag, resolved);
            return resolved;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private VanillaTreeTags() {}
}
