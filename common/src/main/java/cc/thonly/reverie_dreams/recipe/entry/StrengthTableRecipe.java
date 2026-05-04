package cc.thonly.reverie_dreams.recipe.entry;

import cc.thonly.reverie_dreams.recipe.BaseRecipe;
import cc.thonly.reverie_dreams.recipe.ItemStackWrapper;
import cc.thonly.reverie_dreams.recipe.type.StrengthTableRecipeType;
import cc.thonly.reverie_dreams.registry.content.danmaku.DanmakuTemplates;
import cc.thonly.reverie_dreams.registry.content.item.RDItems;
import cc.thonly.reverie_dreams.registry.tag.RDItemTags;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
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
                    ItemStackWrapper.CODEC.fieldOf("main_item").forGetter(StrengthTableRecipe::getMainItem),
                    ItemStackWrapper.CODEC.fieldOf("off_item").forGetter(StrengthTableRecipe::getOffItem),
                    ItemStackWrapper.CODEC.fieldOf("output").forGetter(StrengthTableRecipe::getOutput)
            ).apply(instance, StrengthTableRecipe::new)
    );
    private final ItemStackWrapper mainItem;
    private final ItemStackWrapper offItem;
    private final ItemStackWrapper output;

    public ItemStackWrapper getOutput() {
        return new ItemStackWrapper(this.output.getItemStack().copy());
    }

    @SuppressWarnings("deprecation")
    public static List<StrengthTableRecipe> createRecipeList() {
        List<StrengthTableRecipe> recipeList = new ArrayList<>();

        List<ItemStack> danmakuItems = BuiltInRegistries.ITEM.stream()
                .filter(item -> item.builtInRegistryHolder().is(RDItemTags.DANMAKU_ITEM))
                .map(Item::getDefaultInstance)
                .toList();

        List<ItemStack> materials = new ArrayList<>(DanmakuTemplates.getRegistryItemStackView()
                .values()
                .stream()
                .map(ItemStack::copy)
                .toList());

        materials.add(RDItems.SPEED_FEATHER.createStack());
        materials.add(Items.SLIME_BLOCK.getDefaultInstance());
        materials.add(Items.IRON_SWORD.getDefaultInstance());

        StrengthTableRecipeType type = StrengthTableRecipeType.getInstance();

        for (ItemStack main : danmakuItems) {
            for (ItemStack off : materials) {

                ItemStackWrapper mainWrapper = ItemStackWrapper.of(main.copy());
                ItemStackWrapper offWrapper = ItemStackWrapper.of(off.copy());

                ItemStackWrapper output = type.tryGetOutput(mainWrapper, offWrapper);

                if (output != null) {
                    ItemStackWrapper mainView = mainWrapper.clone();
                    ItemStackWrapper offView = offWrapper.clone();
                    ItemStackWrapper outputView = output.clone();

                    mainView.getItemStack().setCount(1);
                    offView.getItemStack().setCount(1);

                    recipeList.add(new StrengthTableRecipe(mainView, offView, outputView));
                }
            }
        }

        return recipeList;
    }
}
