//package cc.thonly.reverie_dreams.compat.rei.display;
//
//import cc.thonly.reverie_dreams.compat.rei.REICategoryIdentifiers;
//import cc.thonly.reverie_dreams.recipe.BaseRecipe;
//import cc.thonly.reverie_dreams.recipe.entry.DanmakuRecipe;
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
//public class DanmakuCraftingTableDisplay extends BasicDisplay {
//    public static final DisplaySerializer<DanmakuCraftingTableDisplay> SERIALIZER =
//            DisplaySerializer.of(RecordCodecBuilder.mapCodec(instance -> instance.group(
//                    DanmakuRecipe.CODEC.fieldOf("recipe").forGetter(DanmakuCraftingTableDisplay::getRecipe)
//            ).apply(instance, DanmakuCraftingTableDisplay::new)), StreamCodec.composite(
//                    BaseRecipe.forStreamCodec(DanmakuRecipe.CODEC), DanmakuCraftingTableDisplay::getRecipe,
//                    DanmakuCraftingTableDisplay::new
//            ));
//    @Getter
//    DanmakuRecipe recipe;
//
//    public DanmakuCraftingTableDisplay(DanmakuRecipe recipe) {
//        super(List.of(
//                REIItemUtils.getItem(recipe.getDye()),
//                REIItemUtils.getItem(recipe.getCore()),
//                REIItemUtils.getItem(recipe.getPower()),
//                REIItemUtils.getItem(recipe.getPoint()),
//                REIItemUtils.getItem(recipe.getMaterial())
//        ), List.of(REIItemUtils.getItem(recipe.getOutput())));
//        this.recipe = recipe;
//    }
//
//    @Override
//    public CategoryIdentifier<?> getCategoryIdentifier() {
//        return REICategoryIdentifiers.DANMAKU_CRAFTING_TABLE;
//    }
//
//    @Override
//    public @Nullable DisplaySerializer<? extends Display> getSerializer() {
//        return SERIALIZER;
//    }
//}
