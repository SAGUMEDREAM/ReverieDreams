package cc.thonly.mystias_izakaya.mixin;

import cc.thonly.mystias_izakaya.component.FoodProperty;
import cc.thonly.mystias_izakaya.item.base.FoodItem;
import cc.thonly.mystias_izakaya.item.base.IngredientItem;
import net.fabricmc.fabric.api.item.v1.FabricItem;
import net.minecraft.network.chat.Component;
import net.minecraft.world.flag.FeatureElement;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.ItemLike;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.function.Consumer;

@Mixin(Item.class)
public abstract class ItemMixin implements FeatureElement, ItemLike, FabricItem {

    @Inject(method = "appendHoverText", at = @At("HEAD"))
    public void appendTooltip(ItemStack stack, Item.TooltipContext context, TooltipDisplay displayComponent, Consumer<Component> textConsumer, TooltipFlag type, CallbackInfo ci) {
        Item item = this.asItem();
        if (item instanceof IngredientItem || item instanceof FoodItem) {
            return;
        }
        List<FoodProperty> foodProperties = FoodProperty.getIngredientProperties(item);
        if (!foodProperties.isEmpty()) {
            textConsumer.accept(Component.empty().append(Component.translatable("item.tooltip.food_properties")));
        }
        for (FoodProperty foodProperty : foodProperties) {
            textConsumer.accept(Component.empty().append(FoodProperty.getDisplayPrefix(stack, foodProperty)).append(foodProperty.getTooltip()));
        }
    }
}
