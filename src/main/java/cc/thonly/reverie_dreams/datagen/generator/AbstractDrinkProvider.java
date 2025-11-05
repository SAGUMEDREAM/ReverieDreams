package cc.thonly.reverie_dreams.datagen.generator;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.data.DrinkProperty;
import com.google.common.hash.HashCode;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Slf4j
@SuppressWarnings("rawTypes")
public abstract class AbstractDrinkProvider implements DataProvider {
    public final FabricDataOutput output;
    public final CompletableFuture<HolderLookup.Provider> future;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final Map<ResourceLocation, AbstractDrinkProvider.Factory> identifier2BuilderListMap = new Object2ObjectOpenHashMap<>();

    public AbstractDrinkProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> future) {
        this.output = output;
        this.future = future;
        this.configured();
    }

    public AbstractDrinkProvider.Factory createFactory(DrinkProperty property) {
        ResourceLocation id = property.getId();
        if (this.identifier2BuilderListMap.containsKey(id)) {
            return this.identifier2BuilderListMap.get(id);
        }
        AbstractDrinkProvider.Factory factory = new AbstractDrinkProvider.Factory(id, property);
        this.identifier2BuilderListMap.put(id, factory);
        return factory;
    }

    public AbstractDrinkProvider.Factory createFactory(DrinkProperty property, Item... items) {
        AbstractDrinkProvider.Factory factory = createFactory(property);
        factory.getList().addAll(Arrays.stream(items).toList());
        return factory;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput writer) {
        return CompletableFuture.runAsync(() -> {
            this.configured();
            this.export(writer);
        });
    }

    protected abstract void configured();

    public void export(CachedOutput writer) {
        Path path = Paths.get(DataGeneratorUtil.OUTPUT_DIR);
        try {
            for (var entry : this.identifier2BuilderListMap.entrySet()) {
                ResourceLocation identifier = entry.getKey();
                AbstractDrinkProvider.Factory factory = entry.getValue();
                factory.getProperty().setId(identifier);
                Path generatePath = DataGeneratorUtil.getData(path, ReverieDreams.MOD_ID, "drink_property", null);

                DataResult<JsonElement> result = DrinkProperty.CODEC.encodeStart(JsonOps.INSTANCE, factory.getProperty());
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
        private final ResourceLocation id;
        private final DrinkProperty property;
        private final List<Item> list = new LinkedList<>();
        private boolean done = false;

        protected Factory(ResourceLocation id, DrinkProperty property) {
            this.id = id;
            this.property = property;
        }

        public AbstractDrinkProvider.Factory add(Item item) {
            this.list.add(item);
            return this;
        }

        public AbstractDrinkProvider.Factory add(Item... item) {
            this.list.addAll(Arrays.stream(item).toList());
            return this;
        }

        public void build() {
            this.property.getItems().addAll(this.list);
            this.done = true;
        }

    }


}
