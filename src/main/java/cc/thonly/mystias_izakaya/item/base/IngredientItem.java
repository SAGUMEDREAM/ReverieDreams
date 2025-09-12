package cc.thonly.mystias_izakaya.item.base;

import cc.thonly.mystias_izakaya.component.FoodProperty;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.component.type.TooltipDisplayComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public class IngredientItem extends Item {
    public static final Map<Item, Set<FoodProperty>> ITEM_INGREDIENT_CACHED = new HashMap<>();

    public IngredientItem(Settings settings) {
        super(settings.food(new FoodComponent.Builder().nutrition(2).saturationModifier(1).build()));
    }

    public IngredientItem(Integer nutrition, Float saturation, Settings settings) {
        super(settings.food(new FoodComponent.Builder().nutrition(nutrition + 2).saturationModifier(saturation + 1).build()));
    }

//    @Override
//    public void appendTooltip(ItemStack stack, TooltipContext context, TooltipDisplayComponent displayComponent, Consumer<Text> textConsumer, TooltipType type) {
//        super.appendTooltip(stack, context, displayComponent, textConsumer, type);
//        List<FoodProperty> foodProperties = FoodProperty.getIngredientProperties(this);
//        if (!foodProperties.isEmpty()) {
//            textConsumer.accept(Text.empty().append(Text.translatable("item.tooltip.food_properties")));
//        }
//        for (FoodProperty foodProperty : foodProperties) {
//            textConsumer.accept(Text.empty().append(FoodProperty.getDisplayPrefix(stack, foodProperty)).append(foodProperty.getTooltip()));
//        }
//    }

    public static boolean isIngredient(Item item) {
        return item instanceof IngredientItem || (!(item instanceof FoodItem) && !FoodProperty.getIngredientProperties(item).isEmpty());
    }

    public static boolean isIngredient(ItemStack item) {
        return item.getItem() instanceof IngredientItem || (!(item.getItem() instanceof FoodItem) && !FoodProperty.getIngredientProperties(item.getItem()).isEmpty());
    }
}
