package cc.thonly.mystias_izakaya.datagen.generator;

import cc.thonly.mystias_izakaya.MystiasIzakaya;
import cc.thonly.mystias_izakaya.component.FoodProperty;
import cc.thonly.reverie_dreams.datagen.generator.DataGeneratorUtil;
import com.google.common.hash.HashCode;
import com.google.gson.*;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.DataWriter;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Slf4j
@SuppressWarnings("rawTypes")
public abstract class IngredientProvider implements DataProvider {
    public final FabricDataOutput output;
    public final CompletableFuture<RegistryWrapper.WrapperLookup> future;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final Map<Identifier, Factory> identifier2BuilderListMap = new Object2ObjectOpenHashMap<>();

    public IngredientProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> future) {
        this.output = output;
        this.future = future;
        this.configured();
    }

    public Factory createFactory(FoodProperty property) {
        Identifier id = property.getId();
        if (this.identifier2BuilderListMap.containsKey(id)) {
            return this.identifier2BuilderListMap.get(id);
        }
        Factory factory = new Factory(id, property);
        this.identifier2BuilderListMap.put(id, factory);
        return factory;
    }

    public Factory createFactory(FoodProperty property, Item... items) {
        Factory factory = createFactory(property);
        factory.getList().addAll(Arrays.stream(items).toList());
        return factory;
    }

    @Override
    public CompletableFuture<?> run(DataWriter writer) {
        return CompletableFuture.runAsync(() -> {
            this.configured();
            this.export(writer);
        });
    }

    protected abstract void configured();

    public void export(DataWriter writer) {
        Path path = Paths.get(DataGeneratorUtil.OUTPUT_DIR);
        try {
            for (var entry : this.identifier2BuilderListMap.entrySet()) {
                Identifier identifier = entry.getKey();
                Factory factory = entry.getValue();
                factory.getProperty().setId(identifier);
                Path generatePath = DataGeneratorUtil.getData(path, MystiasIzakaya.MOD_ID, "food_property", null);

                DataResult<JsonElement> result = FoodProperty.CODEC.encodeStart(JsonOps.INSTANCE, factory.getProperty());
                Optional<JsonElement> optional = result.result();

                if (optional.isPresent()) {
                    JsonElement element = optional.get();
                    Path output = generatePath.resolve(identifier.getPath() + ".json");
                    String jsonString = this.gson.toJson(element);
                    byte[] bytes = jsonString.getBytes(StandardCharsets.UTF_8);
                    Files.createDirectories(output.getParent());

                    writer.write(output, bytes, HashCode.fromBytes(bytes));
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
        private boolean done = false;

        protected Factory(Identifier id, FoodProperty property) {
            this.id = id;
            this.property = property;
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
            this.property.getItems().addAll(this.list);
            this.done = true;
        }

    }


}
