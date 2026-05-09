//package cc.thonly.reverie_dreams.compat.rei.display;
//
//import cc.thonly.reverie_dreams.compat.rei.REICategoryIdentifiers;
//import cc.thonly.reverie_dreams.recipe.BaseRecipe;
//import cc.thonly.reverie_dreams.recipe.entry.StrengthTableRecipe;
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
//
//public class StrengthTableDisplay extends BasicDisplay {
//    public static final DisplaySerializer<StrengthTableDisplay> SERIALIZER =
//            DisplaySerializer.of(RecordCodecBuilder.mapCodec(instance -> instance.group(
//                    StrengthTableRecipe.CODEC.fieldOf("recipe").forGetter(StrengthTableDisplay::getRecipe)
//            ).apply(instance, StrengthTableDisplay::new)), StreamCodec.composite(
//                    BaseRecipe.forStreamCodec(StrengthTableRecipe.CODEC), StrengthTableDisplay::getRecipe,
//                    StrengthTableDisplay::new
//            ));
//    @Getter
//    StrengthTableRecipe recipe;
//
//    public StrengthTableDisplay(StrengthTableRecipe recipe) {
//        super(List.of(
//                REIItemUtils.getItem(recipe.getMainItem()),
//                REIItemUtils.getItem(recipe.getOffItem())
//        ), List.of(REIItemUtils.getItem(recipe.getOutput())));
//        this.recipe = recipe;
//    }
//
//    @Override
//    public CategoryIdentifier<?> getCategoryIdentifier() {
//        return REICategoryIdentifiers.STRENGTH_TABLE;
//    }
//
//    @Override
//    public @Nullable DisplaySerializer<? extends Display> getSerializer() {
//        return SERIALIZER;
//    }
//}
