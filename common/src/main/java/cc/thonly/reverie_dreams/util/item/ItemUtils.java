package cc.thonly.reverie_dreams.util.item;

import cc.thonly.reverie_dreams.data.FoodProperty;
import cc.thonly.reverie_dreams.item.IngredientStack;
import cc.thonly.reverie_dreams.item.prop.FumoLicenseItem;
import cc.thonly.reverie_dreams.item.prop.SatoriEye;
import cc.thonly.reverie_dreams.recipe.entry.KitchenRecipe;
import cc.thonly.reverie_dreams.registry.content.FoodProperties;
import cc.thonly.reverie_dreams.registry.content.component.RDDataComponents;
import cc.thonly.reverie_dreams.registry.tag.RDItemTags;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Predicate;

@Slf4j
public class ItemUtils extends net.minecraft.world.item.ItemUtils {
    private static Field _FIELD_ITEM = null;
    private static Field _FIELD_COUNT = null;
    private static Field _FIELD_COMPONENTS = null;

    public static IngredientStack buildFoodTags(KitchenRecipe recipe,
                                                IngredientStack output,
                                                List<IngredientStack> inputs) {

        ItemStack base = output.build();
        FoodProperties.get(base);

        // ❗过滤掉“纯食材容器类”（你原本的逻辑保留）
        List<IngredientStack> filteredInputs = inputs.stream()
                .filter(ingredientStack -> !ingredientStack.is(RDItemTags.FOOD_ITEM))
                .toList();

        List<IngredientStack> ingredients = recipe.getIngredients();

        // =============================
        // ✅ 核心修复：使用“标记消耗”而不是 remove
        // =============================

        boolean[] used = new boolean[filteredInputs.size()];

        // 1️⃣ 标记哪些 input 被配方消耗
        for (IngredientStack ingredient : ingredients) {

            for (int i = 0; i < filteredInputs.size(); i++) {
                if (used[i]) continue;

                IngredientStack input = filteredInputs.get(i);

                // ✅ 关键：不要用 build() 做强匹配
                // 👉 这里只按“物品类型”匹配，避免误伤 black_pork
                if (input.asItem() == ingredient.asItem()) {
                    used[i] = true;
                    break;
                }
            }
        }

        // 2️⃣ 收集“剩余材料”（额外食材，比如 black_pork）
        List<IngredientStack> remainingInputs = new ArrayList<>();
        for (int i = 0; i < filteredInputs.size(); i++) {
            if (!used[i]) {
                remainingInputs.add(filteredInputs.get(i));
            }
        }

        // =============================
        // ✅ FoodProperty 合并逻辑
        // =============================

        Set<FoodProperty> temp = new LinkedHashSet<>();

        // base 原始 tag
        List<FoodProperty> baseTags = new ArrayList<>(
                base.getOrDefault(
                        RDDataComponents.FOOD_PROPERTIES.value(),
                        List.of()
                )
        );

        temp.addAll(baseTags);

        // 3️⃣ 合并“额外材料”的 food tag
        for (IngredientStack input : remainingInputs) {
            ItemStack stack = input.build();

            List<FoodProperty> props = stack.getOrDefault(
                    RDDataComponents.FOOD_PROPERTIES.value(),
                    List.of()
            );

            temp.addAll(props);
        }

        List<FoodProperty> resultTags = new ArrayList<>(temp);

        // 4️⃣ 只有变化时才写回
        if (!resultTags.equals(baseTags)) {
            base.set(RDDataComponents.FOOD_PROPERTIES.value(), resultTags);
        }

        return new IngredientStack(base.copy());
    }

    public static boolean isArmorItem(ItemStack stack) {
        return stack.get(DataComponents.EQUIPPABLE) != null;
    }

    public static ItemStack getHandItem(Player player, Predicate<ItemStack> predicate) {
        ItemStack main = player.getItemInHand(InteractionHand.MAIN_HAND);
        ItemStack off = player.getItemInHand(InteractionHand.OFF_HAND);
        if (!main.isEmpty() && predicate.test(main)) {
            return main;
        }
        if (!off.isEmpty() && predicate.test(off)) {
            return off;
        }
        return ItemStack.EMPTY;
    }

    public static boolean shouldPass(Player player, InteractionHand hand) {
        Level world = player.level();
        ItemStack itemStack = player.getItemInHand(hand);
        if (!world.isClientSide()) {
            if (itemStack.getItem() instanceof FumoLicenseItem) {
                return true;
            }
            if (itemStack.getItem() instanceof SatoriEye) {
                return true;
            }
            if (itemStack.getItem() == Items.BARREL) {
                return true;
            }
        }
        return false;
    }

    public static Field getItemFieldFromTemplate() {
        if (_FIELD_ITEM == null) {
            try {
                Field itemField = ItemStackTemplate.class.getDeclaredField("item");
                itemField.setAccessible(true);
                _FIELD_ITEM = itemField;
            } catch (Exception e) {
                log.error("Error: ", e);
            }
        }
        return _FIELD_ITEM;
    }

    public static Field getItemCountFromTemplate() {
        if (_FIELD_COUNT == null) {
            try {
                Field itemField = ItemStackTemplate.class.getDeclaredField("count");
                itemField.setAccessible(true);
                _FIELD_COUNT = itemField;
            } catch (Exception e) {
                log.error("Error: ", e);
            }
        }
        return _FIELD_COUNT;
    }

    public static Field getComponentsFieldFromTemplate() {
        if (_FIELD_COMPONENTS == null) {
            try {
                Field itemField = ItemStackTemplate.class.getDeclaredField("components");
                itemField.setAccessible(true);
                _FIELD_COMPONENTS = itemField;
            } catch (Exception e) {
                log.error("Error: ", e);
            }
        }
        return _FIELD_COMPONENTS;
    }
}
