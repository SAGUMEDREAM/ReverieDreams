package cc.thonly.reverie_dreams.fabric.datagen.generator;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.data.FoodProperty;
import cc.thonly.reverie_dreams.registry.content.FoodProperties;
import com.google.common.hash.HashCode;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Slf4j
@SuppressWarnings("rawTypes")
public abstract class AbstractFoodIngredientProvider implements DataProvider {
    public final FabricDataOutput output;
    public final CompletableFuture<HolderLookup.Provider> future;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final Map<Identifier, Factory> id2Builder = new Object2ObjectOpenHashMap<>();

    public AbstractFoodIngredientProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> future) {
        this.output = output;
        this.future = future;
        this.configured();
    }

    public Factory createFactory(FoodProperty property) {
        Identifier id = property.getId();
        if (this.id2Builder.containsKey(id)) {
            return this.id2Builder.get(id);
        }
        Factory factory = new Factory(id, property);
        this.id2Builder.put(id, factory);
        return factory;
    }

    public Factory createFactory(FoodProperty property, Item... items) {
        Factory factory = createFactory(property);
        factory.getList().addAll(Arrays.stream(items).toList());
        return factory;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput writer) {
        return CompletableFuture.runAsync(() -> {
            this.configured();
            this.export(writer);
            for (Factory factory : this.id2Builder.values()) {
                FoodProperties.registerByPair(factory.buildForProvider());
            }
        });
    }

    protected abstract void configured();

    public void export(CachedOutput writer) {
        Path path = Paths.get(DataGeneratorUtil.OUTPUT_DIR);
        try {
            for (var entry : this.id2Builder.entrySet()) {
                Identifier identifier = entry.getKey();
                Factory factory = entry.getValue();
                FoodProperty.Data data = new FoodProperty.Data(identifier, factory.getList());
                Path generatePath = DataGeneratorUtil.getData(path, ReverieDreams.MOD_ID, "food_property", null);

                DataResult<JsonElement> result = FoodProperty.Data.CODEC.encodeStart(JsonOps.INSTANCE, data);
                Optional<JsonElement> optional = result.result();

                if (optional.isPresent()) {
                    JsonElement element = optional.get();
                    Path output = generatePath.resolve(identifier.getPath() + ".json");
                    String jsonString = this.gson.toJson(element);
                    byte[] bytes = jsonString.getBytes(StandardCharsets.UTF_8);
                    Files.createDirectories(output.getParent());

                    writer.writeIfNeeded(output, bytes, HashCode.fromBytes(bytes));
                }
            }
        } catch (Exception err) {
            log.error("Error: ", err);
        }
    }

    @Setter
    @Getter
    public static class Factory {
        private final Identifier id;
        private final FoodProperty property;
        private final List<Item> list = new LinkedList<>();

        protected Factory(Identifier id, FoodProperty property) {
            this.id = id;
            this.property = property;
        }

        public Factory add(ItemLike item) {
            this.list.add(item.asItem());
            return this;
        }

        public Factory add(Item item) {
            this.list.add(item);
            return this;
        }

        public Factory add(Item... item) {
            this.list.addAll(Arrays.stream(item).toList());
            return this;
        }

        public void build() {
        }

        public Pair<FoodProperty, Collection<Item>> buildForProvider() {
            return Pair.of(this.property, new HashSet<>(this.list));
        }
    }


}
