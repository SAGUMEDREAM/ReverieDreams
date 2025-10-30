package cc.thonly.mystias_izakaya.item.base;

import cc.thonly.mystias_izakaya.component.FoodProperty;
import cc.thonly.mystias_izakaya.component.MIDataComponentTypes;
import java.util.*;
import java.util.function.Consumer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class FoodItem extends Item {

    public FoodItem(Properties settings) {
        this(0, 0f, settings);
    }

    public FoodItem(List<FoodProperty> foodProperties, Properties settings) {
        super(settings.component(MIDataComponentTypes.FOOD_PROPERTIES, foodProperties.stream().map(FoodProperty::getId).map(ResourceLocation::toString).toList()));
    }

    public FoodItem(List<FoodProperty> foodProperties, Integer nutrition, Float saturation, Properties settings) {
        this(
                settings.food(new FoodProperties.Builder()
                                .nutrition(nutrition + 2)
                                .saturationModifier(saturation + 2)
                                .build()
                        )
                        .component(MIDataComponentTypes.FOOD_PROPERTIES, foodProperties.stream().map(FoodProperty::getId).map(ResourceLocation::toString).toList()));
    }

    public FoodItem(Integer nutrition, Float saturation, Properties settings) {
        super(settings
                .food(new FoodProperties.Builder()
                        .nutrition(nutrition + 2)
                        .saturationModifier(saturation + 2)
                        .build()
                ));
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level world, LivingEntity user) {
        if (!world.isClientSide() && world instanceof ServerLevel serverWorld) {
            Set<FoodProperty> foodProperties = new HashSet<>(FoodProperty.getFromItemStack(stack));
            Set<FoodProperty> foodPropertiesFromComponent = new HashSet<>(FoodProperty.getFromItemStackComponent(stack));

            Set<FoodProperty> allProperties = new HashSet<>(foodProperties);
            allProperties.addAll(foodPropertiesFromComponent);

            for (FoodProperty foodProperty : allProperties) {
                foodProperty.use(serverWorld, user);
            }
            if (user instanceof ServerPlayer player) {
                FoodData hungerManager = player.getFoodData();
                allProperties.forEach((property) -> hungerManager.eat(1, 2));
            }
        }
        return super.finishUsingItem(stack, world, user);
    }


//    @Override
//    public void appendTooltip(ItemStack stack, TooltipContext context, TooltipDisplayComponent displayComponent, Consumer<Text> textConsumer, TooltipType type) {
//        super.appendTooltip(stack, context, displayComponent, textConsumer, type);
//        List<FoodProperty> foodProperties = FoodProperty.getFromItemStackComponent(stack);
//        List<FoodProperty> foodIngredientProperties = FoodProperty.getIngredientProperties(this);
//        Set<FoodProperty> foodPropertyList = new HashSet<>();
//        foodPropertyList.addAll(foodProperties);
//        foodPropertyList.addAll(foodIngredientProperties);
//        if (!foodPropertyList.isEmpty()) {
//            textConsumer.accept(Text.empty().append(Text.translatable("item.tooltip.food_properties")));
//        }
//        for (FoodProperty foodProperty : foodPropertyList) {
//            textConsumer.accept(Text.empty().append(FoodProperty.getDisplayPrefix(stack, foodProperty)).append(foodProperty.getTooltip()));
//        }
//
//    }
}
