package cc.thonly.reverie_dreams.api;

import cc.thonly.reverie_dreams.recipe.BaseRecipe;
import cc.thonly.reverie_dreams.recipe.BaseRecipeType;
import cc.thonly.reverie_dreams.recipe.ItemStackWrapper;
import cc.thonly.reverie_dreams.recipe.RecipeItemTag;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

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

    public static synchronized void apply(BaseRecipeType<?> recipeType) {
        Builder<?> builder = getOrCreateBuilder(recipeType);
        Map<Identifier, ?> registries = builder.getRegistries();
        for (Map.Entry<Identifier, ?> registry : registries.entrySet()) {
            log.info("Registered compatibility recipe {}", registry.getKey().toString());
            recipeType.add(registry.getKey(), registry.getValue());
        }
    }

    @Accessors(chain = true)
    @Getter
    @Setter
    public static class Builder<R extends BaseRecipe> {
        public static final Map<BaseRecipeType<?>, Builder<?>> INSTANCE = new Object2ObjectOpenHashMap<>();
        public final BaseRecipeType<R> baseRecipeType;
        protected final Map<Identifier, BaseRecipe> registries = new Object2ObjectOpenHashMap<>();

        public Builder(BaseRecipeType<R> baseRecipeType) {
            this.baseRecipeType = baseRecipeType;
        }

        public Builder<R> add(Item targetItem, Item compatItem) {
            return this.add(new ItemPair(targetItem, compatItem));
        }

        public Builder<R> add(Item targetItem, List<Item> compatItems) {
            compatItems.forEach(item -> this.add(new ItemPair(targetItem, item)));
            return this;
        }

        public Builder<R> add(Item targetItem, Item... compatItems) {
            for (Item item : compatItems) {
                this.add(new ItemPair(targetItem, item));
            }
            return this;
        }

        public Builder<R> add(Item targetItem, RecipeItemTag tagEntry) {
            tagEntry.forEach((item -> this.add(targetItem, item)));
            return this;
        }

        public Builder<R> add(ItemPair itemPair) {
            try {
                Map<Identifier, R> registryView = this.baseRecipeType.getRegistryView();
                for (Map.Entry<Identifier, R> view : registryView.entrySet()) {
                    R value = view.getValue();

                    Object object = cloneWithLombokBuilder(value);
                    if (object instanceof BaseRecipe baseRecipe) {
                        Class<? extends BaseRecipe> brClass = baseRecipe.getClass();
                        Field[] declaredFields = brClass.getDeclaredFields();
                        boolean changed = false;
                        for (Field field : declaredFields) {
                            field.setAccessible(true);
                            Object fieldValue = field.get(value);

                            if (fieldValue instanceof ItemStackWrapper wrapper && wrapper.getItem().equals(itemPair.targetItem)) {
                                if (wrapper.getItem().equals(itemPair.compatItem)) {
                                    continue;
                                }
                                field.set(object, new ItemStackWrapper(new ItemStack(itemPair.targetItem, wrapper.getCount())));
                                changed = true;
                            }
                            if (fieldValue instanceof List<?> list) {
                                if (list.isEmpty()) {
                                    continue;
                                }
                                Object first = list.getFirst();
                                if (!(first instanceof ItemStackWrapper)) {
                                    continue;
                                }

                                List<ItemStackWrapper> wrappers = new ArrayList<>();
                                boolean listChanged = false;

                                for (ItemStackWrapper wrapper : (List<ItemStackWrapper>) list) {
                                    if (itemPair.targetItem.equals(itemPair.compatItem)) {
                                        wrappers.add(wrapper);
                                        continue;
                                    }

                                    if (wrapper.getItem().equals(itemPair.targetItem)) {
                                        wrappers.add(ItemStackWrapper.of(new ItemStack(itemPair.compatItem, wrapper.getCount())));
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
                            Identifier itemId = BuiltInRegistries.ITEM.getKey(itemPair.compatItem);
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

    public record ItemPair(Item targetItem, Item compatItem) {
    }

}
