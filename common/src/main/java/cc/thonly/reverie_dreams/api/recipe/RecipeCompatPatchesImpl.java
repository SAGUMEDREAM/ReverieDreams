package cc.thonly.reverie_dreams.api.recipe;

import cc.thonly.reverie_dreams.item.IngredientStack;
import cc.thonly.reverie_dreams.recipe.BaseRecipe;
import cc.thonly.reverie_dreams.recipe.BaseRecipeType;
import cc.thonly.reverie_dreams.recipe.RecipeItemTag;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import net.blay09.mods.balm.world.item.DeferredItem;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@SuppressWarnings("unchecked")
public class RecipeCompatPatchesImpl {

    public static synchronized <R extends BaseRecipe> Builder<R> getOrCreateBuilder(BaseRecipeType<R> baseRecipeType) {
        return (Builder<R>) Builder.INSTANCE.computeIfAbsent(baseRecipeType, (x) -> new Builder<>(baseRecipeType));
    }

    public static synchronized void removeAll(BaseRecipeType<?> recipeType) {
        Builder<?> builder = getOrCreateBuilder(recipeType);
        Map<Identifier, BaseRecipe> registries = builder.getRegistries();
        registries.clear();
    }

    public static synchronized void apply(BaseRecipeType<?> recipeType) {
        Builder<?> builder = getOrCreateBuilder(recipeType);
        Map<Identifier, BaseRecipe> registries = builder.getRegistries();
        for (Map.Entry<Identifier, BaseRecipe> registry : registries.entrySet()) {
            log.info("Registered compatibility recipe {}", registry.getKey().toString());
            recipeType.add(registry.getKey(), registry.getValue());
        }
    }

    @Accessors(chain = true)
    @Getter
    @Setter
    public static class Builder<Recipe extends BaseRecipe> {
        public static final Map<BaseRecipeType<?>, Builder<?>> INSTANCE = new Object2ObjectOpenHashMap<>();
        protected final BaseRecipeType<Recipe> baseRecipeType;
        protected final Map<Identifier, BaseRecipe> registries = new Object2ObjectOpenHashMap<>();

        public Builder(BaseRecipeType<Recipe> baseRecipeType) {
            this.baseRecipeType = baseRecipeType;
        }

        public Builder<Recipe> add(Item targetItem, DeferredItem compatItem) {
            return this.add(new ItemTuple(targetItem, compatItem.asItem()));
        }

        public Builder<Recipe> add(DeferredItem targetItem, Item compatItem) {
            return this.add(new ItemTuple(targetItem.asItem(), compatItem));
        }

        public Builder<Recipe> add(DeferredItem targetItem, DeferredItem compatItem) {
            return this.add(new ItemTuple(targetItem.asItem(), compatItem.asItem()));
        }

        public Builder<Recipe> add(Item targetItem, Item compatItem) {
            return this.add(new ItemTuple(targetItem, compatItem));
        }

        public Builder<Recipe> add(Item targetItem, List<Item> compatItems) {
            compatItems.forEach(item -> this.add(new ItemTuple(targetItem, item)));
            return this;
        }

        public Builder<Recipe> add(Item targetItem, Item... compatItems) {
            for (Item item : compatItems) {
                this.add(new ItemTuple(targetItem, item));
            }
            return this;
        }

        public Builder<Recipe> add(Item targetItem, RecipeItemTag tagEntry) {
            tagEntry.forEach((item -> this.add(targetItem, item)));
            return this;
        }

        public Builder<Recipe> add(ItemTuple itemTuple) {
            try {
                Map<Identifier, Recipe> registryView = this.baseRecipeType.getRegistryView();
                for (Map.Entry<Identifier, Recipe> view : registryView.entrySet()) {
                    Recipe recipe = view.getValue();

                    Object object = this.cloneWithLombokBuilder(recipe);
                    if (!(object instanceof BaseRecipe baseRecipe)) {
                        continue;
                    }

                    Class<? extends BaseRecipe> brClass = baseRecipe.getClass();
                    Field[] declaredFields = brClass.getDeclaredFields();
                    boolean changed = false;
                    for (Field field : declaredFields) {
                        field.setAccessible(true);
                        Object fieldValue = field.get(recipe);

                        if (fieldValue instanceof IngredientStack wrapper && wrapper.getItem().equals(itemTuple.targetItem)) {
                            if (wrapper.getItem().equals(itemTuple.compatItem)) {
                                continue;
                            }
                            field.set(object, new IngredientStack(new ItemStack(itemTuple.targetItem, wrapper.getCount())));
                            changed = true;
                        }
                        if (fieldValue instanceof List<?> list) {
                            if (list.isEmpty()) {
                                continue;
                            }
                            Object first = list.getFirst();
                            if (!(first instanceof IngredientStack)) {
                                continue;
                            }

                            List<IngredientStack> wrappers = new ArrayList<>();
                            boolean listChanged = false;

                            for (IngredientStack wrapper : (List<IngredientStack>) list) {
                                if (itemTuple.targetItem.equals(itemTuple.compatItem)) {
                                    wrappers.add(wrapper);
                                    continue;
                                }

                                if (wrapper.getItem().equals(itemTuple.targetItem)) {
                                    wrappers.add(IngredientStack.of(new ItemStackTemplate(itemTuple.compatItem, wrapper.getCount())));
                                    listChanged = true;
                                } else {
                                    wrappers.add(wrapper);
                                }
                            }

                            if (listChanged) {
                                field.set(object, wrappers);
                                changed = true;
                            }
                        }
                    }
                    if (changed) {
                        Identifier itemId = BuiltInRegistries.ITEM.getKey(itemTuple.compatItem);
                        Identifier oldId = view.getKey();
                        Identifier newIdentifier = null;
                        if (oldId == null) {
                            oldId = Identifier.parse("unknown_recipe_" + UUID.randomUUID());
                            newIdentifier = oldId;
                        } else {
                            newIdentifier = Identifier.parse(oldId.getNamespace() + ":" + oldId.getPath() + "_" + itemId.toString().replaceAll(":", "_"));
                        }
                        this.registries.put(newIdentifier, baseRecipe);
                    }
                }
            } catch (Exception e) {
                log.error("Can't add recipe patches: ", e);
            }
            return this;
        }

        private <T> T cloneWithLombokBuilder(T object) {
            try {
                Method toBuilder = object.getClass().getMethod("toBuilder");
                Object builder = toBuilder.invoke(object);
                Method build = builder.getClass().getMethod("build");
                return (T) build.invoke(builder);
            } catch (Exception e) {
                log.error("Can't clone with builder", e);
                return null;
            }
        }
    }

    public record ItemTuple(Item targetItem, Item compatItem) {
    }

}
