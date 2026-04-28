package cc.thonly.reverie_dreams.recipe.view;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import net.minecraft.resources.Identifier;

@AllArgsConstructor
@Getter
@ToString(callSuper = true)
public class RecipeEntryWrapper<T> {
    public final Identifier key;
    public final T value;

    public static <T> RecipeEntryWrapper<T> of(Identifier key, T value) {
        return new RecipeEntryWrapper<>(key, value);
    }
}
