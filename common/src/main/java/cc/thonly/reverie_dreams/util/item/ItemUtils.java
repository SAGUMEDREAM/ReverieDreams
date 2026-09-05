package cc.thonly.reverie_dreams.util.item;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.api.item.IItemStack;
import cc.thonly.reverie_dreams.data.FoodProperty;
import cc.thonly.reverie_dreams.item.IngredientStack;
import cc.thonly.reverie_dreams.item.prop.FumoLicenseItem;
import cc.thonly.reverie_dreams.item.prop.SatoriEye;
import cc.thonly.reverie_dreams.recipe.entry.KitchenRecipe;
import cc.thonly.reverie_dreams.registry.content.BeverageProperties;
import cc.thonly.reverie_dreams.registry.content.FoodProperties;
import cc.thonly.reverie_dreams.registry.content.component.RDDataComponentTypes;
import cc.thonly.reverie_dreams.registry.content.item.RDItems;
import cc.thonly.reverie_dreams.registry.tag.RDItemTags;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import cc.thonly.keine.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Predicate;

@SuppressWarnings("deprecation")
@Slf4j
public class ItemUtils extends net.minecraft.world.item.ItemUtils {
    private static Field _FIELD_ITEM = null;
    private static Field _FIELD_COUNT = null;
    private static Field _FIELD_COMPONENTS = null;
    public static final String FOOD_TAG_FIELD_KEY = "FoodTagRead";
    public static final String BEVERAGE_TAG_FIELD_KEY = "BeverageTagRead";

    public static final Map<Holder<Item>, Integer> PRICE = new Object2ObjectOpenHashMap<>(Map.of(
            RDItems.COPPER_COIN, 1,
            RDItems.SILVER_COIN, 10,
            RDItems.GOLD_COIN, 100
    ));

    public static <T> void addComponentIfExist(
            DataComponentPatch.Builder builder,
            ItemStack itemStack,
            DataComponentType<T> componentType
    ) {
        if (itemStack.has(componentType)) {
            T value = itemStack.get(componentType);
            if (value != null) {
                builder.set(componentType, value);
            }
        }
    }
    public static <T> void addComponentIfExist(
            DataComponentPatch.Builder builder,
            ItemStack itemStack,
            Holder<DataComponentType<T>> componentType
    ) {
        if (itemStack.has(componentType.value())) {
            T value = itemStack.get(componentType.value());
            if (value != null) {
                builder.set(componentType.value(), value);
            }
        }
    }

    public static int getPlayerCoinValue(Player player) {
        Inventory inventory = player.getInventory();

        int total = 0;

        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);

            if (stack.isEmpty()) {
                continue;
            }

            Holder<Item> holder = stack.getItem().builtInRegistryHolder();

            for (Map.Entry<Holder<Item>, Integer> entry : PRICE.entrySet()) {
                if (holder.is(entry.getKey())) {
                    total += entry.getValue() * stack.getCount();
                    break;
                }
            }
        }

        return total;
    }

    public static boolean removeCoins(
            Player player,
            int amount
    ) {
        if (amount <= 0) {
            return true;
        }

        Inventory inventory = player.getInventory();

        int total = getPlayerCoinValue(player);

        if (total < amount) {
            return false;
        }

        int remaining = total - amount;

        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);

            if (stack.isEmpty()) {
                continue;
            }

            Holder<Item> holder = stack.getItem().builtInRegistryHolder();

            for (Holder<Item> coin : PRICE.keySet()) {
                if (holder.is(coin)) {
                    inventory.setItem(i, ItemStack.EMPTY);
                    break;
                }
            }
        }

        List<ItemStack> remainingCoins = calculateCoins(remaining);

        for (ItemStack stack : remainingCoins) {
            if (!inventory.add(stack)) {
                player.drop(stack, false);
            }
        }

        return true;
    }

    public static List<ItemStack> calculateCoins(int price) {
        List<ItemStack> result = new ArrayList<>();

        if (price <= 0) {
            return result;
        }

        List<Map.Entry<Holder<Item>, Integer>> entries =
                PRICE.entrySet()
                     .stream()
                     .sorted(Map.Entry.<Holder<Item>, Integer>comparingByValue().reversed())
                     .toList();

        int remaining = price;

        for (Map.Entry<Holder<Item>, Integer> entry : entries) {
            int coinValue = entry.getValue();

            if (coinValue <= 0) {
                continue;
            }

            int count = remaining / coinValue;

            if (count <= 0) {
                continue;
            }

            result.add(new ItemStack(entry.getKey(), count));

            remaining %= coinValue;

            if (remaining <= 0) {
                break;
            }
        }

        return result;
    }

    public static void updateIngredientTag(IngredientStack stack) {
        if (!ReverieDreams.config().autoUpdateItemTag) {
            return;
        }
        try {
            Byte foodTagBuild = stack.reverie_dreams$getNonPersistentAdditionalData(ItemUtils.FOOD_TAG_FIELD_KEY, Byte.class);
            if (foodTagBuild == null) {
                FoodProperties.get(stack);
                stack.reverie_dreams$setNonPersistentAdditionalData(ItemUtils.FOOD_TAG_FIELD_KEY, 1);
            }
            Byte beverageTagBuild = stack.reverie_dreams$getNonPersistentAdditionalData(ItemUtils.BEVERAGE_TAG_FIELD_KEY, Byte.class);
            if (beverageTagBuild == null) {
                BeverageProperties.get(stack);
                stack.reverie_dreams$setNonPersistentAdditionalData(ItemUtils.BEVERAGE_TAG_FIELD_KEY, 1);
            }
        } catch (Exception e) {
            log.error("Error on update item tag", e);
        }
    }

    public static void updateItemStackTag(ItemStack itemStack) {
        if (!ReverieDreams.config().autoUpdateItemTag) {
            return;
        }
        try {
            IItemStack mixinImpl = IItemStack.getMixinImpl(itemStack);
            Byte foodTagBuild = mixinImpl.reverie_dreams$getNonPersistentAdditionalData(ItemUtils.FOOD_TAG_FIELD_KEY, Byte.class);
            if (foodTagBuild == null) {
                FoodProperties.get(itemStack);
                mixinImpl.reverie_dreams$setNonPersistentAdditionalData(ItemUtils.FOOD_TAG_FIELD_KEY, 1);
            }
            Byte drinkTagBuild = mixinImpl.reverie_dreams$getNonPersistentAdditionalData(ItemUtils.BEVERAGE_TAG_FIELD_KEY, Byte.class);
            if (drinkTagBuild == null) {
                BeverageProperties.get(itemStack);
                mixinImpl.reverie_dreams$setNonPersistentAdditionalData(ItemUtils.BEVERAGE_TAG_FIELD_KEY, 1);
            }
        } catch (Exception e) {
            log.error("Error on update item tag");
        }
    }

    public static IngredientStack buildFoodTags(KitchenRecipe recipe,
                                                IngredientStack output,
                                                List<IngredientStack> inputs) {

        ItemStack base = output.build();
        FoodProperties.get(base);

        // ❗过滤掉“纯食材容器类”（你原本的逻辑保留）
        List<IngredientStack> filteredInputs = inputs.stream()
                                                     .filter(ingredientStack -> !ingredientStack.is(RDItemTags.CUISINE))
                                                     .toList();

        List<IngredientStack> ingredients = recipe.getIngredients();

        // =============================
        // ✅ 核心修复：使用“标记消耗”而不是 remove
        // =============================

        boolean[] used = new boolean[filteredInputs.size()];

        // 1️⃣ 标记哪些 input 被配方消耗
        for (IngredientStack ingredient : ingredients) {

            for (int i = 0; i < filteredInputs.size(); i++) {
                if (used[i])
                    continue;

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
                        RDDataComponentTypes.FOOD_PROPERTIES.value(),
                        List.of()
                )
        );

        temp.addAll(baseTags);

        // 3️⃣ 合并“额外材料”的 food tag
        for (IngredientStack input : remainingInputs) {
            ItemStack stack = input.build();

            List<FoodProperty> props = stack.getOrDefault(
                    RDDataComponentTypes.FOOD_PROPERTIES.value(),
                    List.of()
            );

            temp.addAll(props);
        }

        List<FoodProperty> resultTags = new ArrayList<>(temp);

        // 4️⃣ 只有变化时才写回
        if (!resultTags.equals(baseTags)) {
            base.set(RDDataComponentTypes.FOOD_PROPERTIES.value(), resultTags);
        }

        return new IngredientStack(base.copy());
    }

    public static Holder<Item> getHolderItem(Identifier itemId) {
        return BuiltInRegistries.ITEM.getValue(itemId).builtInRegistryHolder();
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
