//package cc.thonly.reverie_dreams.compat.rei.display;
//
//import cc.thonly.reverie_dreams.compat.rei.REICategoryIdentifiers;
//import cc.thonly.reverie_dreams.recipe.BaseRecipe;
//import cc.thonly.reverie_dreams.item.ItemStackWrapper;
//import cc.thonly.reverie_dreams.recipe.entry.KitchenRecipe;
//import cc.thonly.reverie_dreams.util.item.REIItemUtils;
//import com.mojang.serialization.codecs.RecordCodecBuilder;
//import lombok.Getter;
//import me.shedaniel.rei.api.common.category.CategoryIdentifier;
//import me.shedaniel.rei.api.common.display.Display;
//import me.shedaniel.rei.api.common.display.DisplaySerializer;
//import me.shedaniel.rei.api.common.display.basic.BasicDisplay;
//import net.minecraft.network.codec.StreamCodec;
//import org.jetbrains.annotations.Nullable;
//
//import java.util.List;
//import java.util.Objects;
//
//public class KitchenDisplay extends BasicDisplay {
//    public static final DisplaySerializer<KitchenDisplay> SERIALIZER =
//            DisplaySerializer.of(RecordCodecBuilder.mapCodec(instance -> instance.group(
//                    KitchenRecipe.CODEC.fieldOf("recipe").forGetter(KitchenDisplay::getRecipe)
//            ).apply(instance, KitchenDisplay::new)), StreamCodec.composite(
//                    BaseRecipe.forStreamCodec(KitchenRecipe.CODEC), KitchenDisplay::getRecipe,
//                    KitchenDisplay::new
//            ));
//    @Getter
//    KitchenRecipe recipe;
//
//    public KitchenDisplay(KitchenRecipe recipe) {
//        super(recipe.getIngredients()
//                .stream()
//                .map(ItemStackWrapper::getItemStack)
//                .filter(Objects::nonNull)
//                .filter(itemStack -> !itemStack.isEmpty())
//                .map(REIItemUtils::getItem)
//                .toList(), List.of(REIItemUtils.getItem(recipe.getOutput())));
//        this.recipe = recipe;
//    }
//
//    @Override
//    public CategoryIdentifier<?> getCategoryIdentifier() {
//        return REICategoryIdentifiers.KITCHEN;
//    }
//
//    @Override
//    public @Nullable DisplaySerializer<? extends Display> getSerializer() {
//        return SERIALIZER;
//    }
//}
