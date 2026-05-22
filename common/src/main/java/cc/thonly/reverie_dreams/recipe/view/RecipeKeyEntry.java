package cc.thonly.reverie_dreams.recipe.view;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import net.minecraft.resources.Identifier;

@AllArgsConstructor
@Getter
@ToString(callSuper = true)
public class RecipeKeyEntry<T> {
    public final Identifier key;
    public final T value;

    public static <T> RecipeKeyEntry<T> of(Identifier key, T value) {
        return new RecipeKeyEntry<>(key, value);
    }
}
