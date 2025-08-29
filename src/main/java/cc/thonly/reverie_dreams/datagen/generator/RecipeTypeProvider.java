package cc.thonly.reverie_dreams.datagen.generator;

import cc.thonly.reverie_dreams.Touhou;
import cc.thonly.reverie_dreams.recipe.BaseRecipe;
import cc.thonly.reverie_dreams.recipe.BaseRecipeType;
import cc.thonly.reverie_dreams.recipe.ItemStackWrapper;
import com.google.common.hash.HashCode;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.block.Block;
import net.minecraft.component.ComponentChanges;
import net.minecraft.data.DataProvider;
import net.minecraft.data.DataWriter;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
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
@SuppressWarnings({"unchecked", "rawtypes"})
public abstract class RecipeTypeProvider implements DataProvider {
    public final FabricDataOutput output;
    public final CompletableFuture<RegistryWrapper.WrapperLookup> future;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final Map<Identifier, Factory<?>> identifierFactoryMap = new Object2ObjectOpenHashMap<>();

    public RecipeTypeProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> future) {
        this.output = output;
        this.future = future;
    }

    public ItemStackWrapper ofEmpty() {
        return ItemStackWrapper.empty();
    }

    public ItemStackWrapper ofItem(ItemStack item) {
        return ItemStackWrapper.of(item);
    }

    public ItemStackWrapper ofItem(Item item) {
        return ItemStackWrapper.of(item);
    }

    public ItemStackWrapper ofItem(Block block) {
        return ItemStackWrapper.of(block.asItem());
    }

    public ItemStackWrapper ofItem(Block block, int amount) {
        return ItemStackWrapper.of(block.asItem(), amount);
    }

    public ItemStackWrapper ofItem(Item item, int amount) {
        return ItemStackWrapper.of(item, amount);
    }

    public ItemStackWrapper ofItem(Item item, int amount, ComponentChanges components) {
        return ItemStackWrapper.of(item, amount, components);
    }

    public List<ItemStackWrapper> ofList(Item... items) {
        LinkedList<ItemStackWrapper> wrappers = new LinkedList<>();
        for (Item item : items) {
            wrappers.add(this.ofItem(item));
        }
        return wrappers;
    }

    public List<ItemStackWrapper> ofList(ItemStack... items) {
        LinkedList<ItemStackWrapper> wrappers = new LinkedList<>();
        for (ItemStack stack : items) {
            wrappers.add(this.ofItem(stack));
        }
        return wrappers;
    }

    public List<ItemStackWrapper> ofList(ItemStackWrapper... stackRecipeWrappers) {
        return new LinkedList<>(Arrays.asList(stackRecipeWrappers));
    }

    public synchronized <R extends BaseRecipe> Factory<R> getOrCreateFactory(BaseRecipeType<R> recipeType, Class<R> rClass) {
        Identifier id = recipeType.getId();
        if (this.identifierFactoryMap.containsKey(id)) {
            return (Factory<R>) this.identifierFactoryMap.get(id);
        }
        Factory<R> factory = new Factory<>(recipeType, rClass);
        this.identifierFactoryMap.put(id, factory);
        return factory;
    }

    @Override
    public CompletableFuture<?> run(DataWriter writer) {
        return CompletableFuture.runAsync(() -> {
            this.configured();
            this.export(writer);
        });
    }

    public abstract void configured();

    public void export(DataWriter writer) {
        try {
            Path path = Paths.get(DataGeneratorUtil.OUTPUT_DIR);
            for (Map.Entry<Identifier, Factory<?>> entry : identifierFactoryMap.entrySet()) {
                Factory<?> factory = entry.getValue();
                Codec codec = factory.getCodec();
                BaseRecipeType<?> recipeType = factory.getRecipeType();
                Map<Identifier, ?> registries = factory.getRegistries();
                Path generatePath = DataGeneratorUtil.getData(path, Touhou.MOD_ID, recipeType.getTypeId() + "_recipe", null);

                for (Map.Entry<Identifier, ?> registryEntry : registries.entrySet()) {
                    Identifier identifier = registryEntry.getKey();
                    Object value = registryEntry.getValue();
                    DataResult<JsonElement> result = codec.encodeStart(JsonOps.INSTANCE, value);
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
            }
        } catch (Exception err) {
            log.error("Error: ", err);
        }
    }

    @Getter
    public static class Factory<R extends BaseRecipe> {
        protected final Class<R> rClass;
        protected final BaseRecipeType<R> recipeType;
        protected final Codec<R> codec;
        protected final Map<Identifier, R> registries = new Object2ObjectOpenHashMap<>();

        protected Factory(BaseRecipeType<R> recipeType, Class<R> rClass) {
            this.recipeType = recipeType;
            this.codec = recipeType.getCodec();
            this.rClass = rClass;
        }

        public Factory<R> register(Item output, R recipe) {
            return this.register(Registries.ITEM.getId(output), recipe);
        }

        public Factory<R> register(Block output, R recipe) {
            Identifier id = Registries.BLOCK.getId(output);
            if (output.asItem() == Items.AIR) {
                log.error("Found unknown BlockItem {} in {}", id, id + ".json");
                return this;
            }
            return this.register(id, recipe);
        }

        public Factory<R> register(Identifier id, R recipe) {
            Identifier identifier = Identifier.of(id.getNamespace(), id.getPath().replaceAll("/", "-"));
            boolean contains = this.registries.containsKey(id);
            if (contains) {
                log.error("Duplicate recipe id found {} in {}", id, id + ".json");
            }
            this.registries.put(identifier, recipe);
            return this;
        }

        public void export(RegistryEntriesFactory<R> factory) {
            factory.apply(this.registries);
        }

        @FunctionalInterface
        public interface RegistryEntriesFactory<R> {
            void apply(Map<Identifier, R> registries);
        }
    }

}
