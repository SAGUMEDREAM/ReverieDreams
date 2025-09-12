package cc.thonly.reverie_dreams.component.tooltip;

import cc.thonly.minecraft.api.ItemStackTooltipCallback;
import cc.thonly.mystias_izakaya.component.DrinkProperty;
import cc.thonly.mystias_izakaya.component.FoodProperty;
import cc.thonly.mystias_izakaya.item.base.DrinkItem;
import cc.thonly.mystias_izakaya.item.base.FoodItem;
import cc.thonly.mystias_izakaya.item.base.IngredientItem;
import cc.thonly.reverie_dreams.Touhou;
import cc.thonly.reverie_dreams.component.ModDataComponentTypes;
import cc.thonly.reverie_dreams.item.builder.RoleCard;
import cc.thonly.reverie_dreams.item.danmaku.AbstractDanmakuItem;
import cc.thonly.reverie_dreams.item.prop.FumoLicenseItem;
import cc.thonly.reverie_dreams.item.template.DanmakuShapeCreatorItem;
import cc.thonly.reverie_dreams.item.template.RoleCardItem;
import cc.thonly.reverie_dreams.item.template.SpellCardTemplateItem;
import cc.thonly.reverie_dreams.recipe.ItemStackWrapper;
import net.fabricmc.fabric.api.event.Event;
import net.minecraft.component.type.TooltipDisplayComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

public class ModTooltips {
    public static void bootstrap() {
        Event<ItemStackTooltipCallback> event = ItemStackTooltipCallback.EVENT;
        event.register((stack, context, displayComponent, player, textConsumer, type) -> {
            if (!(stack.getItem() instanceof AbstractDanmakuItem abstractDanmakuItem)) {
                return;
            }
            Float damage = stack.getOrDefault(ModDataComponentTypes.Danmaku.DAMAGE, null);
            Float scale = stack.getOrDefault(ModDataComponentTypes.Danmaku.SCALE, null);
            Float speed = stack.getOrDefault(ModDataComponentTypes.Danmaku.SPEED, null);
            Integer count = stack.getOrDefault(ModDataComponentTypes.Danmaku.COUNT, AbstractDanmakuItem.DEFAULT_COUNT);
            String templateType = stack.getOrDefault(ModDataComponentTypes.Danmaku.TEMPLATE, Touhou.id("single").toString());

            textConsumer.accept(Text.empty().append(Text.translatable("item.tooltip.damage")).append(String.valueOf(damage)));
            textConsumer.accept(Text.empty().append(Text.translatable("item.tooltip.speed")).append(String.valueOf(speed)));
            textConsumer.accept(Text.empty().append(Text.translatable("item.tooltip.count")).append(String.valueOf(count)));
            textConsumer.accept(Text.empty().append(Text.translatable("item.tooltip.base_type")).append(Text.translatable(Identifier.of(templateType).toTranslationKey())));
        });
        event.register((stack, context, displayComponent, player, textConsumer, type) -> {
            if (!(stack.getItem() instanceof DanmakuShapeCreatorItem danmakuShapeCreatorItem)) {
                return;
            }
            ItemStackWrapper itemStackWrapper = stack.getOrDefault(ModDataComponentTypes.Danmaku.SHAPE, ItemStackWrapper.of(Items.AIR));
            ItemStack itemStack = itemStackWrapper.getItemStack();
            textConsumer.accept(Text.empty().append(Text.translatable("item.tooltip.shape")).append(itemStack.getName()));
        });
        event.register((stack, context, displayComponent, player, textConsumer, type) -> {
            if (!(stack.getItem() instanceof DrinkItem drinkItem)) {
                return;
            }
            List<DrinkProperty> allProperties = DrinkProperty.getAllProperties(stack);
            if (!allProperties.isEmpty()) {
                textConsumer.accept(Text.empty().append(Text.translatable("item.tooltip.food_properties")));
            }
            for (DrinkProperty property : allProperties) {
                textConsumer.accept(Text.empty().append("§b+").append(Text.translatable(property.translateKey())));
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
                textConsumer.accept(Text.empty().append(Text.translatable("item.tooltip.food_properties")));
            }
            for (FoodProperty foodProperty : foodPropertyList) {
                textConsumer.accept(Text.empty().append(FoodProperty.getDisplayPrefix(stack, foodProperty)).append(foodProperty.getTooltip()));
            }
        });
        event.register((stack, context, displayComponent, player, textConsumer, type) -> {
            if (!(stack.getItem() instanceof FumoLicenseItem fumoLicenseItem)) {
                return;
            }
            textConsumer.accept(Text.translatable("item.tooltip.use.villager"));
        });
        event.register((stack, context, displayComponent, player, textConsumer, type) -> {
            if (!(stack.getItem() instanceof IngredientItem ingredientItem)) {
                return;
            }
            List<FoodProperty> foodProperties = FoodProperty.getIngredientProperties(stack.getItem());
            if (!foodProperties.isEmpty()) {
                textConsumer.accept(Text.empty().append(Text.translatable("item.tooltip.food_properties")));
            }
            for (FoodProperty foodProperty : foodProperties) {
                textConsumer.accept(Text.empty().append(FoodProperty.getDisplayPrefix(stack, foodProperty)).append(foodProperty.getTooltip()));
            }
        });
        event.register((stack, context, displayComponent, player, textConsumer, type) -> {
            if (!(stack.getItem() instanceof RoleCardItem roleCardItem)) {
                return;
            }
            Optional<RoleCard> roleCardComponent = roleCardItem.getRoleCardComponent(stack);
            if (roleCardComponent.isEmpty()) {
                textConsumer.accept(Text.translatable("item.disabled"));
                return;
            }
            if (roleCardComponent.get().isEmpty()) {
                textConsumer.accept(Text.translatable("item.disabled"));
                return;
            }
            textConsumer.accept(Text.translatable("item.tooltip.use"));
        });
        event.register((stack, context, displayComponent, player, textConsumer, type) -> {
            if (!(stack.getItem() instanceof SpellCardTemplateItem spellCardTemplateItem)) {
                return;
            }
            Float damage = stack.getOrDefault(ModDataComponentTypes.Danmaku.DAMAGE, null);
            Float scale = stack.getOrDefault(ModDataComponentTypes.Danmaku.SCALE, null);
            Float speed = stack.getOrDefault(ModDataComponentTypes.Danmaku.SPEED, null);
            Integer count = stack.getOrDefault(ModDataComponentTypes.Danmaku.COUNT, AbstractDanmakuItem.DEFAULT_COUNT);
            String templateType = stack.getOrDefault(ModDataComponentTypes.Danmaku.TEMPLATE, Touhou.id("single").toString());

            textConsumer.accept(Text.empty().append(Text.translatable("item.tooltip.base_type")).append(Text.translatable(Identifier.of(templateType).toTranslationKey())));

        });
    }
}
