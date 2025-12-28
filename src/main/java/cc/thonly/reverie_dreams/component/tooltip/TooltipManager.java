package cc.thonly.reverie_dreams.component.tooltip;

import cc.thonly.minecraft.api.ItemStackTooltipCallback;
import cc.thonly.reverie_dreams.component.DanmakuProperties;
import cc.thonly.reverie_dreams.data.DrinkProperty;
import cc.thonly.reverie_dreams.data.FoodProperty;
import cc.thonly.reverie_dreams.item.base.DrinkItem;
import cc.thonly.reverie_dreams.item.base.FoodItem;
import cc.thonly.reverie_dreams.item.base.IngredientItem;
import cc.thonly.reverie_dreams.item.builder.RoleCard;
import cc.thonly.reverie_dreams.item.danmaku.AbstractDanmakuItem;
import cc.thonly.reverie_dreams.item.prop.FumoLicenseItem;
import cc.thonly.reverie_dreams.item.template.DanmakuShapeCreatorItem;
import cc.thonly.reverie_dreams.item.template.RoleCardItem;
import cc.thonly.reverie_dreams.item.template.SpellCardTemplateItem;
import cc.thonly.reverie_dreams.recipe.ItemStackWrapper;
import cc.thonly.reverie_dreams.registry.content.component.RDDataComponents;
import net.fabricmc.fabric.api.event.Event;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class TooltipManager {
    public static void bootstrap() {
        Event<ItemStackTooltipCallback> event = ItemStackTooltipCallback.EVENT;
        event.register((stack, context, displayComponent, player, textConsumer, type) -> {
            if (!(stack.getItem() instanceof AbstractDanmakuItem abstractDanmakuItem)) {
                return;
            }
            DanmakuProperties properties = stack.get(RDDataComponents.DANMAKU_PROPERTIES);
            if (properties != null) {
                textConsumer.accept(Component.empty().append(Component.translatable("item.tooltip.damage")).append(String.valueOf(properties.damage)));
                textConsumer.accept(Component.empty().append(Component.translatable("item.tooltip.speed")).append(String.valueOf(properties.speed)));
                textConsumer.accept(Component.empty().append(Component.translatable("item.tooltip.count")).append(String.valueOf(properties.count)));
                textConsumer.accept(Component.empty().append(Component.translatable("item.tooltip.base_type")).append(Component.translatable(properties.templateId.toLanguageKey())));
            }
        });
        event.register((stack, context, displayComponent, player, textConsumer, type) -> {
            if (!(stack.getItem() instanceof DanmakuShapeCreatorItem danmakuShapeCreatorItem)) {
                return;
            }
            ItemStackWrapper itemStackWrapper = stack.getOrDefault(RDDataComponents.DANMAKU_SHAPE, ItemStackWrapper.of(Items.AIR));
            ItemStack itemStack = itemStackWrapper.getItemStack();
            textConsumer.accept(Component.empty().append(Component.translatable("item.tooltip.shape")).append(itemStack.getHoverName()));
        });
        event.register((stack, context, displayComponent, player, textConsumer, type) -> {
            List<DrinkProperty> allProperties = DrinkProperty.getAllProperties(stack);
            if (!allProperties.isEmpty()) {
                textConsumer.accept(Component.empty().append(Component.translatable("item.tooltip.food_properties")));
            }
            for (DrinkProperty property : allProperties) {
                textConsumer.accept(Component.empty().append("§b+").append(Component.translatable(property.translateKey())));
            }
        });
        event.register((stack, context, displayComponent, player, textConsumer, type) -> {
            if (!(stack.getItem() instanceof FoodItem foodItem)) {
                return;
            }
            List<FoodProperty> foodProperties = FoodProperty.getFromItemStackComponent(stack);
            List<FoodProperty> foodIngredientProperties = FoodProperty.getIngredientProperties(stack.getItem());
            Set<FoodProperty> foodPropertyList = new HashSet<>();
            foodPropertyList.addAll(foodProperties);
            foodPropertyList.addAll(foodIngredientProperties);
            if (!foodPropertyList.isEmpty()) {
                textConsumer.accept(Component.empty().append(Component.translatable("item.tooltip.food_properties")));
            }
            for (FoodProperty foodProperty : foodPropertyList) {
                textConsumer.accept(Component.empty().append(FoodProperty.getDisplayPrefix(stack, foodProperty)).append(foodProperty.getTooltip()));
            }
        });
        event.register((stack, context, displayComponent, player, textConsumer, type) -> {
            if (!(stack.getItem() instanceof FumoLicenseItem fumoLicenseItem)) {
                return;
            }
            textConsumer.accept(Component.translatable("item.tooltip.use.villager"));
        });
        event.register((stack, context, displayComponent, player, textConsumer, type) -> {
            if (!(stack.getItem() instanceof IngredientItem ingredientItem)) {
                return;
            }
            List<FoodProperty> foodProperties = FoodProperty.getIngredientProperties(stack.getItem());
            if (!foodProperties.isEmpty()) {
                textConsumer.accept(Component.empty().append(Component.translatable("item.tooltip.food_properties")));
            }
            for (FoodProperty foodProperty : foodProperties) {
                textConsumer.accept(Component.empty().append(FoodProperty.getDisplayPrefix(stack, foodProperty)).append(foodProperty.getTooltip()));
            }
        });
        event.register((stack, context, displayComponent, player, textConsumer, type) -> {
            if (!(stack.getItem() instanceof RoleCardItem roleCardItem)) {
                return;
            }
            Optional<RoleCard> roleCardComponent = roleCardItem.getRoleCardComponent(stack);
            if (roleCardComponent.isEmpty()) {
                textConsumer.accept(Component.translatable("item.disabled"));
                return;
            }
            if (roleCardComponent.get().isEmpty()) {
                textConsumer.accept(Component.translatable("item.disabled"));
                return;
            }
            textConsumer.accept(Component.translatable("item.tooltip.use"));
        });
        event.register((stack, context, displayComponent, player, textConsumer, type) -> {
            if (!(stack.getItem() instanceof SpellCardTemplateItem spellCardTemplateItem)) {
                return;
            }
            DanmakuProperties properties = stack.get(RDDataComponents.DANMAKU_PROPERTIES);
            if (properties != null) {
                textConsumer.accept(Component.empty().append(Component.translatable("item.tooltip.base_type")).append(Component.translatable(properties.templateId.toLanguageKey())));
            }
        });
    }
}
