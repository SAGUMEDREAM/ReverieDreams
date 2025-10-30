package cc.thonly.reverie_dreams.recipe;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import eu.pb4.polymer.core.api.utils.PolymerObject;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.Item;
import java.util.*;
import java.util.stream.Stream;

@Slf4j
@ToString
public abstract class BaseRecipeType<R extends BaseRecipe> {
    protected final Map<ResourceLocation, R> registries = new Object2ObjectLinkedOpenHashMap<>();
    private int nextRawId = 0;

    public abstract void reload(ResourceManager manager);

    public abstract void bootstrap();

    public abstract List<R> getMatches(List<ItemStackWrapper> wrappers);

    public abstract Boolean isMatch(ItemStackWrapper input, ItemStackWrapper recipe);

    public abstract Codec<R> getCodec();

    public abstract String getTypeId();

    public abstract ResourceLocation getId();

    public BaseRecipeType<R> add(ResourceLocation id, R recipe) {
        if (!this.registries.containsKey(id)) {
            recipe.setRawId(this.nextRawId++);
        } else {
            recipe.setRawId(this.registries.get(id).getRawId());
        }
        recipe.setId(id);
        this.registries.put(id, recipe);
        return this;
    }

    @SuppressWarnings("unchecked")
    public void add(ResourceLocation key, Object value) {
        this.add(key, (R) value);
    }

    public void sort() {
        Map<Item, LinkedList<R>> sign = new LinkedHashMap<>();
        Map<ResourceLocation, R> all = new LinkedHashMap<>();
        for (Map.Entry<ResourceLocation, R> recipeEntry : this.registries.entrySet()) {
            R recipe = recipeEntry.getValue();
            Item item = recipe.getOutput().getItem();
            LinkedList<R> list = sign.computeIfAbsent(item, i -> new LinkedList<>());
            list.add(recipe);
        }
        for (Map.Entry<Item, LinkedList<R>> linkEntry : sign.entrySet()) {
            LinkedList<R> list = linkEntry.getValue();
            for (R recipe : list) {
                all.put(recipe.getId(), recipe);
            }
        }
        this.registries.clear();
        this.registries.putAll(all);
    }

    public void assignRawId() {
        int nextId = 0;
        for (Map.Entry<ResourceLocation, R> next : this.registries.entrySet()) {
            R recipeEntry = next.getValue();
            recipeEntry.setRawId(nextId++);
        }
    }

    public R getRecipeById(ResourceLocation id) {
        return this.registries.get(id);
    }

    public Map<ResourceLocation, R> getRegistryView() {
        return new LinkedHashMap<>(this.registries);
    }

    public List<ResourceLocation> keys() {
        return new ArrayList<>(this.registries.keySet());
    }

    public List<R> values() {
        return new ArrayList<>(this.registries.values());
    }

    public Integer size() {
        return this.registries.size();
    }

    public Stream<R> stream() {
        return this.registries.values().stream();
    }

    public BaseRecipeType<R> remove(ResourceLocation id) {
        this.registries.remove(id);
        return this;
    }

    public BaseRecipeType<R> removeAll() {
        this.registries.clear();
        this.nextRawId = 0;
        return this;
    }

    public JsonElement encode() {
        JsonObject element = new JsonObject();
        Object2ObjectOpenHashMap<ResourceLocation, R> registries = new Object2ObjectOpenHashMap<>(this.registries);
        Set<Map.Entry<ResourceLocation, R>> entries = registries.entrySet();
        Codec<R> codec = this.getCodec();
        if (codec == null) {
            return element;
        }
        for (Map.Entry<ResourceLocation, R> entry : entries) {
            R value = entry.getValue();
            DataResult<JsonElement> dataResult = codec.encodeStart(JsonOps.INSTANCE, value);
            Optional<JsonElement> result = dataResult.result();
            result.ifPresent((e) -> {
                element.add(value.getId().toString(), e);
            });
        }
        return element;
    }

    public List<BaseRecipe> decode(JsonElement element) {
        List<BaseRecipe> list = new LinkedList<>();
        Codec<R> codec = this.getCodec();

        if (codec == null) {
            return list;
        }

        if (!(element instanceof JsonObject jsonObject)) {
            return list;
        }

        for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
            String key = entry.getKey();
            JsonElement value = entry.getValue();

            ResourceLocation id;
            try {
                id = ResourceLocation.parse(key);
            } catch (Exception e) {
                log.error("Can't parse Identifier {}", key, e);
                continue;
            }

            Dynamic<JsonElement> dynamic = new Dynamic<>(JsonOps.INSTANCE, value);
            DataResult<R> parseResult = codec.parse(dynamic);

            parseResult.resultOrPartial(error -> {
                log.error("Can't parse {} -> {}", key ,error);
            }).ifPresent(r -> {
                r.setId(id);
                list.add(r);
            });
        }

        return list;
    }

}
