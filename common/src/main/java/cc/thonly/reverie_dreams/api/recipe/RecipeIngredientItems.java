package cc.thonly.reverie_dreams.api.recipe;

import cc.thonly.reverie_dreams.mixin.accessor.RecipeManagerAccessor;
import cc.thonly.reverie_dreams.server.RecipeManagerHolder;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.ItemLike;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;

@Slf4j
@SuppressWarnings({"deprecation", "unchecked"})
public class RecipeIngredientItems {
    // template -> other mod item
    private static final Map<Holder<Item>, Holder<Item>> MAPPINGS = new Object2ObjectOpenHashMap<>();
    private static final Map<Holder<RecipeType<? extends Recipe<?>>>, List<Consumer<Recipe<?>>>> PROCESSOR = new Object2ObjectOpenHashMap<>();

    public static void create(Consumer<Context> function) {
        function.accept(createContextImpl());
    }

    private static Context createContextImpl() {
        return new Context() {
            @Override
            public <I extends RecipeInput, R extends Recipe<I>> Context addProcessor(Holder<RecipeType<R>> recipeType, Consumer<R> function) {
                List<Consumer<Recipe<?>>> functions = PROCESSOR.computeIfAbsent((Holder<RecipeType<? extends Recipe<?>>>) (Object) recipeType, _ -> new ArrayList<>());
                functions.add((Consumer<Recipe<?>>) function);
                return this;
            }

            @Override
            public Context add(Holder<Item> template, Holder<Item> item) {
                MAPPINGS.put(template, item);
                return this;
            }
        };
    }

    @SuppressWarnings("rawtypes")
    public static void reload() {
        RecipeManager recipeManager = RecipeManagerHolder.get();
        RecipeManagerAccessor recipeManagerAccessor = (RecipeManagerAccessor) recipeManager;
        RecipeMap recipeMap = recipeManagerAccessor.reverie_dreams$getRecipeMap();
        for (var entry : PROCESSOR.entrySet()) {
            Holder<RecipeType> keyHolder = (Holder<RecipeType>) (Object) entry.getKey();
            List<Consumer<Recipe>> list = (List<Consumer<Recipe>>) (Object) entry.getValue();
            Collection<? extends RecipeHolder<? extends Recipe<?>>> recipeHolders = recipeMap.byType(keyHolder.value());
            for (RecipeHolder recipeHolder : recipeHolders) {
                for (Consumer<Recipe> function : list) {
                    try {
                        function.accept(recipeHolder.value());
                    } catch (Exception e) {
                        log.debug("The following problem occurred while fixing Recipe: {}", recipeHolder.id(), e);
                    }

                }
            }
        }
    }

    public static boolean contains(Ingredient ingredient, Item item) {
        for (Holder<Item> holder : ingredient.values) {
            if (holder.is(item.builtInRegistryHolder())) {
                return true;
            }
        }
        return false;
    }

    public static boolean contains(Ingredient ingredient, Holder<Item> item) {
        for (Holder<Item> holder : ingredient.values) {
            if (holder.is(item)) {
                return true;
            }
        }
        return false;
    }

    public static boolean contains(Ingredient ingredient, Holder<Item>... items) {
        for (Holder<Item> holder : ingredient.values) {
            for (Holder<Item> item : items) {
                if (holder.is(item)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean contains(Ingredient ingredient, List<Holder<Item>> items) {
        for (Holder<Item> holder : ingredient.values) {
            for (Holder<Item> item : items) {
                if (holder.is(item)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static List<Holder<Item>> getItems(ItemLike item) {
        Set<Holder<Item>> items = new LinkedHashSet<>();
        MAPPINGS.forEach((templateHolder, modItemHolder) -> {
            if (templateHolder.is(item.asItem().builtInRegistryHolder())) {
                items.add(modItemHolder);
            }
        });
        return new ArrayList<>(items);
    }

    public static List<Holder<Item>> getItems(Holder<Item> item) {
        Set<Holder<Item>> items = new LinkedHashSet<>();
        MAPPINGS.forEach((templateHolder, modItemHolder) -> {
            if (templateHolder.is(item)) {
                items.add(modItemHolder);
            }
        });
        return new ArrayList<>(items);
    }

    public interface Modifier {
        default Ingredient append(Ingredient ingredient, ItemLike like) {
            Set<ItemLike> list = new LinkedHashSet<>();
            HolderSet<Item> holderSet = ingredient.values;

            for (Holder<Item> holder : holderSet) {
                list.add(holder.value());
            }

            list.add(like);
            return Ingredient.of(list.stream());
        }

        default Ingredient appendIf(Ingredient ingredient, Predicate<Ingredient> predicate) {
            HolderSet<Item> holderSet = ingredient.values;
            if (predicate.test(ingredient)) {
                Set<ItemLike> list = new LinkedHashSet<>();
                MAPPINGS.forEach((a, b) -> {
                    if (holderSet.contains(a)) {
                        list.add(b.value());
                    }
                });
                holderSet.forEach(holder -> {
                    list.add(holder.value());
                });
                if (list.isEmpty())
                    return ingredient;
                return Ingredient.of(list.stream());
            }
            return ingredient;
        }
    }

    @SuppressWarnings({"unchecked", "UnusedReturnValue"})
    public interface Context {
        Context add(Holder<Item> template, Holder<Item> item);

        <I extends RecipeInput, R extends Recipe<I>> Context addProcessor(Holder<RecipeType<R>> recipeType, Consumer<R> function);

        default Ingredient modify(Ingredient ingredient, BiFunction<Context, Modifier, Ingredient> function) {
            return function.apply(this, new Modifier() {
            });
        }

        default <I extends RecipeInput, R extends Recipe<I>> Context addProcessor(RecipeType<R> recipeType, Consumer<R> function) {
            Optional<ResourceKey<RecipeType<?>>> resourceKeyOptional = BuiltInRegistries.RECIPE_TYPE.getResourceKey(recipeType);
            if (resourceKeyOptional.isEmpty()) {
                return this;
            }
            ResourceKey<RecipeType<?>> resourceKey = resourceKeyOptional.get();
            Holder.Reference<RecipeType<?>> reference = BuiltInRegistries.RECIPE_TYPE.getOrThrow(resourceKey);
            return this.addProcessor((Holder<RecipeType<R>>) (Object) reference, function);
        }

        default Context add(ItemLike template, ItemLike item) {
            return this.add(template.asItem().builtInRegistryHolder(), item.asItem().builtInRegistryHolder());
        }

        default Context add(Holder<Item> template, Item item) {
            return this.add(template, item.builtInRegistryHolder());
        }

        default Context add(Item template, Holder<Item> item) {
            return this.add(template.builtInRegistryHolder(), item);
        }
    }

}
