package cc.thonly.reverie_dreams.recipe.view;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import net.minecraft.resources.ResourceLocation;

@AllArgsConstructor
@Getter
@ToString(callSuper = true)
public class RecipeEntryWrapper<T> {
    public final ResourceLocation key;
    public final T value;

    public static <T> RecipeEntryWrapper<T> of(ResourceLocation key, T value) {
        return new RecipeEntryWrapper<>(key, value);
    }
}
