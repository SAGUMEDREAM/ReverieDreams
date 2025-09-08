package cc.thonly.mystias_izakaya.mixin;

import cc.thonly.mystias_izakaya.component.FoodProperty;
import cc.thonly.mystias_izakaya.item.base.FoodItem;
import cc.thonly.mystias_izakaya.item.base.IngredientItem;
import net.fabricmc.fabric.api.item.v1.FabricItem;
import net.minecraft.component.type.TooltipDisplayComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.resource.featuretoggle.ToggleableFeature;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.function.Consumer;

@Mixin(Item.class)
public abstract class ItemMixin implements ToggleableFeature, ItemConvertible, FabricItem {

    @Inject(method = "appendTooltip", at = @At("HEAD"))
    public void appendTooltip(ItemStack stack, Item.TooltipContext context, TooltipDisplayComponent displayComponent, Consumer<Text> textConsumer, TooltipType type, CallbackInfo ci) {
        Item item = this.asItem();
        if (item instanceof IngredientItem || item instanceof FoodItem) {
            return;
        }
        List<FoodProperty> foodProperties = FoodProperty.getIngredientProperties(item);
        if (!foodProperties.isEmpty()) {
            textConsumer.accept(Text.empty().append(Text.translatable("item.tooltip.food_properties")));
        }
        for (FoodProperty foodProperty : foodProperties) {
            textConsumer.accept(Text.empty().append(FoodProperty.getDisplayPrefix(stack, foodProperty)).append(foodProperty.getTooltip()));
        }
    }
}
