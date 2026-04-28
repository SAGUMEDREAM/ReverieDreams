package cc.thonly.reverie_dreams.mixin.accessor;

import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RecipeProvider.class)
public interface RecipeProviderAccessor {
    @Accessor("items")
    HolderGetter<Item> reverie_dreams$getItems();
}
