package cc.thonly.reverie_dreams.fabric.datagen;

import cc.thonly.reverie_dreams.data.danmaku.DanmakuType;
import cc.thonly.reverie_dreams.fabric.datagen.generator.AbstractRecipeTypeProvider;
import cc.thonly.reverie_dreams.item.IngredientStack;
import cc.thonly.reverie_dreams.recipe.RecipeManager;
import cc.thonly.reverie_dreams.recipe.entry.DanmakuRecipe;
import cc.thonly.reverie_dreams.registry.BuiltInRegistryProviders;
import cc.thonly.reverie_dreams.registry.content.item.RDItems;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;

import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public class DanmakuRecipeProvider extends AbstractRecipeTypeProvider {
    public final Factory<DanmakuRecipe> factory = this.getOrCreateFactory(RecipeManager.DANMAKU, DanmakuRecipe.class);

    public DanmakuRecipeProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> future) {
        super(output, future);
    }

    @Override
    public void configured(HolderLookup.Provider provider) {
        Stream<DanmakuType> stream = BuiltInRegistryProviders.DANMAKU_TYPE.stream();
        stream.forEach(value -> {
            if (!value.isDeleteFromList()) {
                for (Tuple<Item, ItemStackTemplate> pair : value.getColorPairs().get()) {
                    Item dye = pair.getA();
                    ItemStackTemplate result = pair.getB();
                    Item item = result.item().value();
                    Identifier itemId = BuiltInRegistries.ITEM.getKey(item);
                    Identifier dyeId = BuiltInRegistries.ITEM.getKey(dye);
                    String path = itemId.getPath().replaceAll("/", "_").replaceFirst("danmaku_", "") + "_dye_by_" + dyeId.getPath();
                    Identifier registryKey = Identifier.fromNamespaceAndPath(itemId.getNamespace(), path);
                    DanmakuRecipe recipe = new DanmakuRecipe(
                            new IngredientStack(new ItemStackTemplate(dye, 4)),
                            new IngredientStack(new ItemStackTemplate(RDItems.DANMAKU_CORE.asItem(), 4)),
                            new IngredientStack(new ItemStackTemplate(RDItems.POWER.asItem(), 35)),
                            new IngredientStack(new ItemStackTemplate(RDItems.POINT.asItem(), 35)),
                            new IngredientStack(value.toShape().getItemStackTemplate()),
                            new IngredientStack(result)
                    );
                    this.factory.register(registryKey, recipe);
                }
            }
        });
    }

    @Override
    public String getName() {
        return "Danmaku Recipe Provider";
    }
}
