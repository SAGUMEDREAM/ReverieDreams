package cc.thonly.reverie_dreams.recipe;

import cc.thonly.reverie_dreams.item.IngredientStack;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.Item;

import java.util.*;
import java.util.stream.Stream;

@SuppressWarnings("UnusedReturnValue")
@Slf4j
public abstract class BaseRecipeType<R extends BaseRecipe> {
    protected final Map<Identifier, R> registries = new Object2ObjectLinkedOpenHashMap<>();
    private boolean acceptNetworking = false;
    private int nextRawId = 0;

    public abstract void reload(ResourceManager manager);

    public abstract void bootstrap();

    public abstract List<R> getMatches(List<IngredientStack> stackList);

    public abstract Boolean isMatch(IngredientStack input, IngredientStack recipe);

    public abstract Codec<R> getCodec();

    public abstract String getTypeId();

    public abstract Identifier getId();

    public synchronized void setAcceptNetworking(boolean acceptNetworking) {
        this.acceptNetworking = acceptNetworking;
    }

    public synchronized boolean isAcceptNetworking() {
        return this.acceptNetworking;
    }

    public BaseRecipeType<R> add(Identifier id, R recipe) {
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
    public void add(Identifier key, Object value) {
        this.add(key, (R) value);
    }

    public void sort() {
        Map<Item, LinkedList<R>> sign = new LinkedHashMap<>();
        Map<Identifier, R> all = new LinkedHashMap<>();
        for (Map.Entry<Identifier, R> recipeEntry : this.registries.entrySet()) {
            R recipe = recipeEntry.getValue();
            Item item = recipe.getOutput().getItem();
            LinkedList<R> list = sign.computeIfAbsent(item, _ -> new LinkedList<>());
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
        for (Map.Entry<Identifier, R> next : this.registries.entrySet()) {
            R recipeEntry = next.getValue();
            recipeEntry.setRawId(nextId++);
        }
    }

    public Identifier getRecipeKey(R recipe) {
        for (Map.Entry<Identifier, R> entry : this.registries.entrySet()) {
            if (Objects.equals(entry.getValue(), recipe)) {
                return entry.getKey();
            }
        }
        return null;
    }

    public R getRecipeById(Identifier id) {
        return this.registries.get(id);
    }

    public Map<Identifier, R> getRegistryView() {
        return new LinkedHashMap<>(this.registries);
    }

    public List<Identifier> keys() {
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

    public BaseRecipeType<R> remove(Identifier id) {
        this.registries.remove(id);
        return this;
    }

    public BaseRecipeType<R> removeAll() {
        this.registries.clear();
        this.nextRawId = 0;
        return this;
    }

    public BaseRecipeType<R> clear() {
        return this.removeAll();
    }

    @Override
    public String toString() {
        return "BaseRecipeType{" + "size=" + this.size() + ", acceptNetworking=" + this.acceptNetworking + '}';
    }

    public static <R extends BaseRecipe> CompoundTag writeForTag(BaseRecipeType<R> recipeType) {
        Identifier id = recipeType.getId();

        CompoundTag root = new CompoundTag();
        root.putString("type", id.toString());

        CompoundTag recipesTag = recipeType.encodeTags();
        root.put("data", recipesTag);

        return root;
    }

    public static <R extends BaseRecipe> Pair<Identifier, List<Pair<Identifier, R>>> readFromTag(BaseRecipeType<R> recipeType, CompoundTag tag) {
        String typeStr = tag.getStringOr("type", "null");
        if (typeStr.equals("null")) {
            return null;
        }
        Identifier typeId = Identifier.tryParse(typeStr);

        if (typeId == null) {
            throw new IllegalArgumentException("Invalid recipe type id: " + typeStr);
        }

        CompoundTag data = tag.getCompoundOrEmpty("data");

        List<Pair<Identifier, R>> recipes = recipeType.decodeTags(data);

        return Pair.of(typeId, recipes);
    }

    public CompoundTag encodeTags() {
        CompoundTag tag = new CompoundTag();
        ListTag list = new ListTag();

        for (Map.Entry<Identifier, R> entry : this.registries.entrySet()) {
            Identifier id = entry.getKey();
            R recipe = entry.getValue();

            CompoundTag wrapper = new CompoundTag();
            wrapper.putString("id", id.toString());

            CompoundTag data = encodeTag(recipe);
            wrapper.put("data", data);

            list.add(wrapper);
        }

        tag.put("recipes", list);
        return tag;
    }

    public List<Pair<Identifier, R>> decodeTags(CompoundTag tag) {
        List<Pair<Identifier, R>> result = new ArrayList<>();

        if (!tag.contains("recipes")) {
            return result;
        }

        Optional<ListTag> listOptional = tag.getList("recipes");
        if (listOptional.isEmpty()) {
            return result;
        }
        ListTag list = listOptional.get();
        for (int i = 0; i < list.size(); i++) {
            try {
                CompoundTag wrapper = list.getCompoundOrEmpty(i);

                String idStr = wrapper.getStringOr("id", "null");
                if (idStr.equals("null")) {
                    continue;
                }
                Identifier id = Identifier.tryParse(idStr);
                if (id == null) {
                    log.error("Invalid recipe id: {}", idStr);
                    continue;
                }

                CompoundTag data = wrapper.getCompoundOrEmpty("data");
                R recipe = decodeTag(data);

                if (recipe != null) {
                    result.add(Pair.of(id, recipe));
                }
            } catch (Exception e) {
                log.error("Error: ",e);
            }
        }

        return result;
    }

    public CompoundTag encodeTag(R recipe) {
        Codec<R> codec = this.getCodec();

        DataResult<Tag> result = codec.encodeStart(NbtOps.INSTANCE, recipe);

        return result.resultOrPartial(error -> {
            log.error("Failed to encode recipe: {}", error);
        }).map(tag -> {
            if (tag instanceof CompoundTag compound) {
                return compound;
            } else {
                CompoundTag wrapper = new CompoundTag();
                wrapper.put("data", tag);
                return wrapper;
            }
        }).orElse(new CompoundTag());
    }

    public R decodeTag(CompoundTag tag) {
        Codec<R> codec = this.getCodec();

        DataResult<R> result = codec.parse(NbtOps.INSTANCE, tag);

        return result.resultOrPartial(error -> {
            log.error("Failed to decode recipe: {}", error);
        }).orElse(null);
    }

    public JsonElement encodes() {
        JsonObject element = new JsonObject();
        Object2ObjectOpenHashMap<Identifier, R> registries = new Object2ObjectOpenHashMap<>(this.registries);
        Set<Map.Entry<Identifier, R>> entries = registries.entrySet();
        Codec<R> codec = this.getCodec();
        if (codec == null) {
            return element;
        }
        for (Map.Entry<Identifier, R> entry : entries) {
            R value = entry.getValue();
            DataResult<JsonElement> dataResult = codec.encodeStart(JsonOps.INSTANCE, value);
            Optional<JsonElement> result = dataResult.result();
            result.ifPresent((e) -> {
                element.add(value.getId().toString(), e);
            });
        }
        return element;
    }

    public List<BaseRecipe> decodes(JsonElement element) {
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

            Identifier id;
            try {
                id = Identifier.parse(key);
            } catch (Exception e) {
                log.error("Can't parse Identifier {}", key, e);
                continue;
            }

            Dynamic<JsonElement> dynamic = new Dynamic<>(JsonOps.INSTANCE, value);
            DataResult<R> parseResult = codec.parse(dynamic);

            parseResult.resultOrPartial(error -> {
                log.error("Can't parse {} -> {}", key, error);
            }).ifPresent(r -> {
                r.setId(id);
                list.add(r);
            });
        }

        return list;
    }

}
