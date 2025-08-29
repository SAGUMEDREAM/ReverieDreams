package cc.thonly.mystias_izakaya.item.base;

import cc.thonly.mystias_izakaya.component.DrinkProperty;
import cc.thonly.mystias_izakaya.component.MIDataComponentTypes;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ConsumableComponents;
import net.minecraft.component.type.TooltipDisplayComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.HungerManager;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public class DrinkItem extends Item {
    public static final Map<Item, Set<DrinkProperty>> ITEM_DRINK_CACHED = new HashMap<>();
    public static final Map<Item, Integer> PRICE_CALCULATION_TABLE = new Object2ObjectOpenHashMap<>();

    public DrinkItem(Settings settings) {
        super(settings.maxCount(16)
                .component(DataComponentTypes.CONSUMABLE, ConsumableComponents.DRINK)
                .useRemainder(Items.GLASS_BOTTLE));
    }

    public DrinkItem(List<DrinkProperty> drinkProperties, Settings settings) {
        super(settings.maxCount(16)
                .component(DataComponentTypes.CONSUMABLE, ConsumableComponents.DRINK)
                .component(MIDataComponentTypes.DRINK_PROPERTIES, drinkProperties.stream().map(DrinkProperty::getId).map(Identifier::toString).toList())
                .useRemainder(Items.GLASS_BOTTLE));
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        if (!world.isClient && world instanceof ServerWorld serverWorld) {
            List<DrinkProperty> allProperties = DrinkProperty.getAllProperties(stack);
            for (DrinkProperty property : allProperties) {
                property.use(serverWorld, user);
            }
            if (user instanceof ServerPlayerEntity player) {
                HungerManager hungerManager = player.getHungerManager();
                hungerManager.add(1, allProperties.size() % 3);
            }
        }
        return super.finishUsing(stack, world, user);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, TooltipDisplayComponent displayComponent, Consumer<Text> textConsumer, TooltipType type) {
        super.appendTooltip(stack, context, displayComponent, textConsumer, type);
        List<DrinkProperty> allProperties = DrinkProperty.getAllProperties(stack);
        if (!allProperties.isEmpty()) {
            textConsumer.accept(Text.empty().append(Text.translatable("item.tooltip.food_properties")));
        }
        for (DrinkProperty property : allProperties) {
            textConsumer.accept(Text.empty().append("§b+").append(Text.translatable(property.translateKey())));
        }
    }
}
