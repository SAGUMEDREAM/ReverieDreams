package cc.thonly.reverie_dreams.fabric.datagen.generator;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.fabric.util.DataGeneratorUtil;
import cc.thonly.reverie_dreams.fabric.util.DataProviderHelper;
import cc.thonly.reverie_dreams.item.IngredientStack;
import cc.thonly.reverie_dreams.recipe.BaseRecipe;
import cc.thonly.reverie_dreams.recipe.BaseRecipeType;
import cc.thonly.reverie_dreams.registry.delegate.ItemDelegate;
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
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Slf4j
@SuppressWarnings({"unchecked", "rawtypes"})
public abstract class AbstractRecipeTypeProvider implements DataProvider {
    public final FabricPackOutput output;
    public final CompletableFuture<HolderLookup.Provider> future;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final Map<Identifier, Factory<?>> registries = new Object2ObjectOpenHashMap<>();
    private HolderLookup.Provider provider;

    public AbstractRecipeTypeProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> future) {
        this.output = output;
        this.future = future;
    }

    public HolderLookup.Provider provider() {
        if (this.provider == null) {
            try {
                this.provider = this.future.get();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return this.provider;
    }

    public IngredientStack ofEmpty() {
        return IngredientStack.empty();
    }

    public IngredientStack ofItem(ItemStack item) {
        return IngredientStack.of(item.getItem());
    }

    public IngredientStack ofItem(ItemStackTemplate item) {
        return IngredientStack.of(item);
    }

    public IngredientStack ofItem(ItemLike item) {
        return IngredientStack.of(item.asItem());
    }

    public IngredientStack ofItem(ItemDelegate item) {
        return IngredientStack.of(item.asItem());
    }

    public IngredientStack ofItem(Item item) {
        return IngredientStack.of(item);
    }

    public IngredientStack ofItem(Block block) {
        return IngredientStack.of(block.asItem());
    }

    public IngredientStack ofItem(Block block, int amount) {
        return IngredientStack.of(block.asItem(), amount);
    }

    public IngredientStack ofItem(Item item, int amount) {
        return IngredientStack.of(item, amount);
    }

    public IngredientStack ofItem(ItemLike item, int amount) {
        return IngredientStack.of(item.asItem(), amount);
    }

    public IngredientStack ofItem(Item item, int amount, DataComponentPatch components) {
        return IngredientStack.of(item, amount, components);
    }

    public List<IngredientStack> ofList(Item... items) {
        LinkedList<IngredientStack> wrappers = new LinkedList<>();
        for (Item item : items) {
            wrappers.add(this.ofItem(item));
        }
        return wrappers;
    }

    public List<IngredientStack> ofList(ItemLike... items) {
        LinkedList<IngredientStack> wrappers = new LinkedList<>();
        for (ItemLike item : items) {
            wrappers.add(this.ofItem(item.asItem()));
        }
        return wrappers;
    }

    public List<IngredientStack> ofList(Holder<Item>... items) {
        LinkedList<IngredientStack> wrappers = new LinkedList<>();
        for (Holder<Item> item : items) {
            wrappers.add(this.ofItem(item.value()));
        }
        return wrappers;
    }

    public List<IngredientStack> ofList(ItemDelegate... items) {
        LinkedList<IngredientStack> wrappers = new LinkedList<>();
        for (ItemDelegate item : items) {
            wrappers.add(this.ofItem(item.asItem()));
        }
        return wrappers;
    }

    public List<IngredientStack> ofList(ItemStack... items) {
        LinkedList<IngredientStack> wrappers = new LinkedList<>();
        for (ItemStack stack : items) {
            wrappers.add(this.ofItem(stack));
        }
        return wrappers;
    }

    public List<IngredientStack> ofList(IngredientStack... stackRecipeWrappers) {
        return new LinkedList<>(Arrays.asList(stackRecipeWrappers));
    }

    public synchronized <R extends BaseRecipe> Factory<R> getOrCreateFactory(BaseRecipeType<R> recipeType, Class<R> rClass) {
        Identifier id = recipeType.getId();
        if (this.registries.containsKey(id)) {
            return (Factory<R>) this.registries.get(id);
        }
        Factory<R> factory = new Factory<>(recipeType, rClass);
        this.registries.put(id, factory);
        return factory;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput writer) {
        return this.future.thenAcceptAsync(provider -> {
            this.configured(provider);

            for (Factory<?> factory : this.registries.values()) {
                DataProviderHelper.outputFile(
                        writer,
                        (Map) factory.getRegistries(),
                        (Codec) factory.getCodec(),
                        factory.getRecipeType().getTypeId() + "_recipe"
                );
            }
        });
    }

    public abstract void configured(HolderLookup.Provider provider);

    public void export(CachedOutput cachedOutput) {
        try {
            Path path = Paths.get(DataGeneratorUtil.OUTPUT_DIR);
            for (Map.Entry<Identifier, Factory<?>> entry : this.registries.entrySet()) {
                Factory<?> factory = entry.getValue();
                Codec codec = factory.getCodec();
                BaseRecipeType<?> recipeType = factory.getRecipeType();
                Map<Identifier, ?> registries = factory.getRegistries();
                Path generatePath = DataGeneratorUtil.getData(path, ReverieDreams.MOD_ID, recipeType.getTypeId() + "_recipe", null);

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

                        cachedOutput.writeIfNeeded(output, bytes, HashCode.fromBytes(bytes));
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

        public Factory<R> register(ItemLike output, R recipe) {
            return this.register(BuiltInRegistries.ITEM.getKey(output.asItem()), recipe);
        }

        public Factory<R> register(Item output, R recipe) {
            return this.register(BuiltInRegistries.ITEM.getKey(output), recipe);
        }

        public Factory<R> register(Block output, R recipe) {
            Identifier id = BuiltInRegistries.BLOCK.getKey(output);
            if (output.asItem() == Items.AIR) {
                log.error("Found unknown BlockItem {} in {}", id, id + ".json");
                return this;
            }
            return this.register(id, recipe);
        }

        public Factory<R> register(String name, R recipe) {
            name = name.toLowerCase();
            Identifier identifier = Identifier.tryParse(name);
            if (identifier == null) {
                throw new IllegalArgumentException("%s is an invalid string".formatted(name));
            }
            return this.register(identifier, recipe);
        }

        public Factory<R> register(Identifier id, R recipe) {
            Identifier identifier = Identifier.fromNamespaceAndPath(id.getNamespace(), id.getPath().replaceAll("/", "-"));
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
