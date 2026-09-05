package cc.thonly.reverie_dreams.api.recipe;

import cc.thonly.reverie_dreams.item.IngredientStack;
import cc.thonly.reverie_dreams.recipe.BaseRecipe;
import cc.thonly.reverie_dreams.recipe.BaseRecipeType;
import cc.thonly.reverie_dreams.recipe.RecipeItemTag;
import cc.thonly.reverie_dreams.registry.delegate.ItemDelegate;
import cc.thonly.reverie_dreams.util.item.CompatItemTuple;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import cc.thonly.keine.item.ItemStackTemplate;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@SuppressWarnings("unchecked")
@Accessors(chain = true)
@Getter
@Setter
public class PatchBuilder<Recipe extends BaseRecipe> {
    public static final Map<BaseRecipeType<?>, PatchBuilder<?>> INSTANCE = new Object2ObjectOpenHashMap<>();
    protected final BaseRecipeType<Recipe> baseRecipeType;
    protected final Map<Identifier, BaseRecipe> registries = new Object2ObjectOpenHashMap<>();

    public PatchBuilder(BaseRecipeType<Recipe> baseRecipeType) {
        this.baseRecipeType = baseRecipeType;
    }

    public PatchBuilder<Recipe> add(Item targetItem, ItemDelegate compatItem) {
        return this.add(new CompatItemTuple(targetItem, compatItem.asItem()));
    }

    public PatchBuilder<Recipe> add(ItemDelegate targetItem, Item compatItem) {
        return this.add(new CompatItemTuple(targetItem.asItem(), compatItem));
    }

    public PatchBuilder<Recipe> add(ItemDelegate targetItem, ItemDelegate compatItem) {
        return this.add(new CompatItemTuple(targetItem.asItem(), compatItem.asItem()));
    }

    public PatchBuilder<Recipe> add(Item targetItem, Item compatItem) {
        return this.add(new CompatItemTuple(targetItem, compatItem));
    }

    public PatchBuilder<Recipe> add(Item targetItem, List<Item> compatItems) {
        compatItems.forEach(item -> this.add(new CompatItemTuple(targetItem, item)));
        return this;
    }

    public PatchBuilder<Recipe> add(Item targetItem, Item... compatItems) {
        for (Item item : compatItems) {
            this.add(new CompatItemTuple(targetItem, item));
        }
        return this;
    }

    public PatchBuilder<Recipe> add(Item targetItem, RecipeItemTag tagEntry) {
        tagEntry.forEach((item -> this.add(targetItem, item)));
        return this;
    }

    public PatchBuilder<Recipe> add(CompatItemTuple compatItemTuple) {
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

                    if (fieldValue instanceof IngredientStack wrapper && wrapper.getItem().equals(compatItemTuple.targetItem())) {
                        if (wrapper.getItem().equals(compatItemTuple.compatItem())) {
                            continue;
                        }
                        field.set(object, new IngredientStack(new ItemStack(compatItemTuple.targetItem(), wrapper.getCount())));
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
                            if (compatItemTuple.targetItem().equals(compatItemTuple.compatItem())) {
                                wrappers.add(wrapper);
                                continue;
                            }

                            if (wrapper.getItem().equals(compatItemTuple.targetItem())) {
                                wrappers.add(IngredientStack.of(new ItemStackTemplate(compatItemTuple.compatItem(), wrapper.getCount())));
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
                    Identifier itemId = BuiltInRegistries.ITEM.getKey(compatItemTuple.compatItem());
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
