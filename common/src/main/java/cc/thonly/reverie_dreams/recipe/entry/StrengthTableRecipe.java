package cc.thonly.reverie_dreams.recipe.entry;

import cc.thonly.reverie_dreams.item.IngredientStack;
import cc.thonly.reverie_dreams.recipe.BaseRecipe;
import cc.thonly.reverie_dreams.recipe.type.StrengthTableRecipeType;
import cc.thonly.reverie_dreams.registry.content.danmaku.DanmakuTemplates;
import cc.thonly.reverie_dreams.registry.content.item.RDItems;
import cc.thonly.reverie_dreams.registry.tag.RDItemTags;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor(access = AccessLevel.PUBLIC)
@Builder(toBuilder = true)
public class StrengthTableRecipe extends BaseRecipe {
    public static final Codec<StrengthTableRecipe> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    IngredientStack.CODEC.fieldOf("main_item").forGetter(StrengthTableRecipe::getMainItem),
                    IngredientStack.CODEC.fieldOf("off_item").forGetter(StrengthTableRecipe::getOffItem),
                    IngredientStack.CODEC.fieldOf("output").forGetter(StrengthTableRecipe::getOutput)
            ).apply(instance, StrengthTableRecipe::new)
    );
    private final IngredientStack mainItem;
    private final IngredientStack offItem;
    private final IngredientStack output;

    @Override
    public IngredientStack getOutput() {
        return this.output.copy();
    }

    @SuppressWarnings("deprecation")
    public static List<StrengthTableRecipe> createRecipeList() {
        List<StrengthTableRecipe> recipeList = new ArrayList<>();

        List<ItemStackTemplate> danmakuItems = BuiltInRegistries.ITEM.stream()
                .filter(item -> item.builtInRegistryHolder().is(RDItemTags.DANMAKU_ITEM))
                .map(ItemStackTemplate::new)
                .toList();

        List<ItemStackTemplate> materials = new ArrayList<>(DanmakuTemplates.getRegistryItemStackView()
                .values()
                .stream()
                .toList());

        materials.add(new ItemStackTemplate(RDItems.SPEED_FEATHER.asItem()));
        materials.add(new ItemStackTemplate(Items.SLIME_BLOCK));
        materials.add(new ItemStackTemplate(Items.IRON_SWORD));

        StrengthTableRecipeType type = StrengthTableRecipeType.getInstance();

        for (ItemStackTemplate main : danmakuItems) {
            for (ItemStackTemplate off : materials) {

                IngredientStack mainWrapper = IngredientStack.of(main);
                IngredientStack offWrapper = IngredientStack.of(off);

                IngredientStack output = type.tryGetOutput(mainWrapper, offWrapper);

                if (output != null) {
                    IngredientStack mainView = mainWrapper.clone();
                    IngredientStack offView = offWrapper.clone();
                    IngredientStack outputView = output.clone();

                    mainView.setCount(1);
                    offView.setCount(1);

                    recipeList.add(new StrengthTableRecipe(mainView, offView, outputView));
                }
            }
        }

        return recipeList;
    }
}
