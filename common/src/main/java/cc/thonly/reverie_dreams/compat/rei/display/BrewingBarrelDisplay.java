package cc.thonly.reverie_dreams.compat.rei.display;

import cc.thonly.reverie_dreams.compat.rei.REICategoryIdentifiers;
import cc.thonly.reverie_dreams.recipe.BaseRecipe;
import cc.thonly.reverie_dreams.recipe.entry.BrewingBarrelRecipe;
import cc.thonly.reverie_dreams.util.item.REIItemUtils;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.display.DisplaySerializer;
import me.shedaniel.rei.api.common.display.basic.BasicDisplay;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BrewingBarrelDisplay extends BasicDisplay {
    public static final DisplaySerializer<BrewingBarrelDisplay> SERIALIZER =
            DisplaySerializer.of(RecordCodecBuilder.mapCodec(instance -> instance.group(
                    BrewingBarrelRecipe.CODEC.fieldOf("recipe").forGetter(BrewingBarrelDisplay::getRecipe)
            ).apply(instance, BrewingBarrelDisplay::new)), StreamCodec.composite(
                    BaseRecipe.forStreamCodec(BrewingBarrelRecipe.CODEC), BrewingBarrelDisplay::getRecipe,
                    BrewingBarrelDisplay::new
            ));
    @Getter
    BrewingBarrelRecipe recipe;

    public BrewingBarrelDisplay(BrewingBarrelRecipe recipe) {
        super(recipe.getMaterials().stream().map(REIItemUtils::getItem).toList(),
                List.of(REIItemUtils.getItem(recipe.getOutput()))
        );
        this.recipe = recipe;
    }

    @Override
    public CategoryIdentifier<?> getCategoryIdentifier() {
        return REICategoryIdentifiers.BREWING_BARREL;
    }

    @Override
    public @Nullable DisplaySerializer<? extends Display> getSerializer() {
        return SERIALIZER;
    }
}
