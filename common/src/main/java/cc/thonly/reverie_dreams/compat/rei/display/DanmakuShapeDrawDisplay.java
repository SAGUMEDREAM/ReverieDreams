package cc.thonly.reverie_dreams.compat.rei.display;

import cc.thonly.reverie_dreams.compat.rei.REICategoryIdentifiers;
import cc.thonly.reverie_dreams.recipe.BaseRecipe;
import cc.thonly.reverie_dreams.recipe.entry.DanmakuRecipe;
import cc.thonly.reverie_dreams.recipe.entry.DanmakuShapeDrawRecipe;
import cc.thonly.reverie_dreams.util.item.REIItemUtils;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.display.DisplaySerializer;
import me.shedaniel.rei.api.common.display.basic.BasicDisplay;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DanmakuShapeDrawDisplay extends BasicDisplay {
    public static final DisplaySerializer<DanmakuShapeDrawDisplay> SERIALIZER =
            DisplaySerializer.of(RecordCodecBuilder.mapCodec(instance -> instance.group(
                    DanmakuShapeDrawRecipe.CODEC.fieldOf("recipe").forGetter(DanmakuShapeDrawDisplay::getRecipe)
            ).apply(instance, DanmakuShapeDrawDisplay::new)), StreamCodec.composite(
                    BaseRecipe.forStreamCodec(DanmakuShapeDrawRecipe.CODEC), DanmakuShapeDrawDisplay::getRecipe,
                    DanmakuShapeDrawDisplay::new
            ));
    @Getter
    DanmakuShapeDrawRecipe recipe;

    public DanmakuShapeDrawDisplay(DanmakuShapeDrawRecipe recipe) {
        super(List.of(REIItemUtils.getItem(Items.BARRIER)), List.of(REIItemUtils.getItem(recipe.getOutput())));
        this.recipe = recipe;
    }

    @Override
    public CategoryIdentifier<?> getCategoryIdentifier() {
        return REICategoryIdentifiers.DANMAKU_SHAPE_DRAW;
    }

    @Override
    public @Nullable DisplaySerializer<? extends Display> getSerializer() {
        return SERIALIZER;
    }
}
