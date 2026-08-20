package cc.thonly.reverie_dreams.fabric.datagen.generator;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.data.BeverageProperty;
import cc.thonly.reverie_dreams.fabric.util.DataGeneratorUtil;
import cc.thonly.reverie_dreams.fabric.util.DataProviderHelper;
import cc.thonly.reverie_dreams.registry.content.BeverageProperties;
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
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
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
public abstract class AbstractBeverageProvider implements DataProvider {
    public final FabricPackOutput output;
    public final CompletableFuture<HolderLookup.Provider> future;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final Map<Identifier, Factory> registries = new Object2ObjectOpenHashMap<>();

    public AbstractBeverageProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> future) {
        this.output = output;
        this.future = future;
    }

    public Factory createFactory(BeverageProperty property) {
        Identifier id = property.getId();
        if (this.registries.containsKey(id)) {
            return this.registries.get(id);
        }
        Factory factory = new Factory(id, property);
        this.registries.put(id, factory);
        return factory;
    }

    public Factory createFactory(BeverageProperty property, Item... items) {
        Factory factory = createFactory(property);
        factory.getList().addAll(Arrays.stream(items).toList());
        return factory;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput writer) {
        return this.future.thenAcceptAsync((provider) -> {
            this.configured(provider);
            for (Factory factory : this.registries.values()) {
                BeverageProperties.registerByPair(factory.buildForProvider());
            }
            DataProviderHelper.outputFile(writer, this.registries, BeverageProperty.Data.CODEC, (id, factory) -> new BeverageProperty.Data(id, factory.getList()), "beverage_property");
        });
    }

    protected abstract void configured(HolderLookup.Provider provider);

    public void export(CachedOutput writer) {
        Path path = Paths.get(DataGeneratorUtil.OUTPUT_DIR);
        try {
            for (var entry : this.registries.entrySet()) {
                Identifier identifier = entry.getKey();
                Factory factory = entry.getValue();
                BeverageProperty.Data data = new BeverageProperty.Data(identifier, factory.getList());
                Path generatePath = DataGeneratorUtil.getData(path, ReverieDreams.MOD_ID, "beverage_property", null);

                DataResult<JsonElement> result = BeverageProperty.Data.CODEC.encodeStart(JsonOps.INSTANCE, data);
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
        private final BeverageProperty property;
        private final List<Item> list = new LinkedList<>();

        protected Factory(Identifier id, BeverageProperty property) {
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

        public Pair<BeverageProperty, Collection<Item>> buildForProvider() {
            return Pair.of(this.property, new HashSet<>(this.list));
        }

    }


}
