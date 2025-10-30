package cc.thonly.mystias_izakaya.item.base;

import cc.thonly.mystias_izakaya.component.DrinkProperty;
import cc.thonly.mystias_izakaya.component.MIDataComponentTypes;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.level.Level;

public class DrinkItem extends Item {
    public static final Map<Item, Set<DrinkProperty>> ITEM_DRINK_CACHED = new HashMap<>();
    public static final Map<Item, Integer> PRICE_CALCULATION_TABLE = new Object2ObjectOpenHashMap<>();

    public DrinkItem(Properties settings) {
        super(settings.stacksTo(16)
                .component(DataComponents.CONSUMABLE, Consumables.DEFAULT_DRINK)
                .usingConvertsTo(Items.GLASS_BOTTLE));
    }

    public DrinkItem(List<DrinkProperty> drinkProperties, Properties settings) {
        super(settings.stacksTo(16)
                .component(DataComponents.CONSUMABLE, Consumables.DEFAULT_DRINK)
                .component(MIDataComponentTypes.DRINK_PROPERTIES, drinkProperties.stream().map(DrinkProperty::getId).map(ResourceLocation::toString).toList())
                .usingConvertsTo(Items.GLASS_BOTTLE));
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level world, LivingEntity user) {
        if (!world.isClientSide && world instanceof ServerLevel serverWorld) {
            List<DrinkProperty> allProperties = DrinkProperty.getAllProperties(stack);
            for (DrinkProperty property : allProperties) {
                property.use(serverWorld, user);
            }
            if (user instanceof ServerPlayer player) {
                FoodData hungerManager = player.getFoodData();
                hungerManager.eat(1, allProperties.size() % 3);
            }
        }
        return super.finishUsingItem(stack, world, user);
    }

//    @Override
//    public void appendTooltip(ItemStack stack, TooltipContext context, TooltipDisplayComponent displayComponent, Consumer<Text> textConsumer, TooltipType type) {
//        super.appendTooltip(stack, context, displayComponent, textConsumer, type);
//        List<DrinkProperty> allProperties = DrinkProperty.getAllProperties(stack);
//        if (!allProperties.isEmpty()) {
//            textConsumer.accept(Text.empty().append(Text.translatable("item.tooltip.food_properties")));
//        }
//        for (DrinkProperty property : allProperties) {
//            textConsumer.accept(Text.empty().append("§b+").append(Text.translatable(property.translateKey())));
//        }
//    }
}
