package cc.thonly.reverie_dreams.compat.rrv;

import cc.cassian.rrv.api.ReliableRecipeViewerClientPlugin;
import cc.cassian.rrv.api.recipe.ItemView;
import cc.cassian.rrv.common.builtin.BuiltInReliableRecipeViewerIntegration;
import cc.cassian.rrv.common.builtin.info.InfoClientRecipe;
import cc.cassian.rrv.common.recipe.inventory.RecipeViewMenu;
import cc.cassian.rrv.common.recipe.inventory.SlotContent;
import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.compat.ItemViewItemInfo;
import cc.thonly.reverie_dreams.compat.rrv.brewing_barrel.BrewingBarrelRecipe;
import cc.thonly.reverie_dreams.compat.rrv.danmaku_crafting_table.DanmakuCraftingTableRecipe;
import cc.thonly.reverie_dreams.compat.rrv.danmaku_shape_draw.DanmakuShapeDrawRecipe;
import cc.thonly.reverie_dreams.compat.rrv.gensokyo_altar.GensokyoAltarRecipe;
import cc.thonly.reverie_dreams.compat.rrv.kitchen.BaseKitchenRecipe;
import cc.thonly.reverie_dreams.data.danmaku.DanmakuShape;
import cc.thonly.reverie_dreams.data.danmaku.DanmakuType;
import cc.thonly.reverie_dreams.item.base.RoleCard;
import cc.thonly.reverie_dreams.recipe.RecipeManager;
import cc.thonly.reverie_dreams.recipe.entry.StrengthTableRecipe;
import cc.thonly.reverie_dreams.registry.BuiltInRegistryProviders;
import cc.thonly.reverie_dreams.registry.content.item.RDEntityHolderItems;
import cc.thonly.reverie_dreams.registry.content.item.RDGuiPlaceholderItems;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import cc.thonly.keine.item.ItemStackTemplate;

import java.util.List;
import java.util.UUID;

@Slf4j
@SuppressWarnings("deprecation")
public class RRVPlugin implements ReliableRecipeViewerClientPlugin {
    public static final Identifier SLOT_TEXTURE = BuiltInReliableRecipeViewerIntegration.DEFAULT_SLOT_TEXTURE;
    public static final RecipeViewMenu.OptionalSlotRenderer RENDERER = RecipeViewMenu.OptionalSlotRenderer.DEFAULT;

    @Override
    public void onIntegrationInitialize() {
        ItemView.addClientReloadCallback(() -> {
            for (DanmakuType type : BuiltInRegistryProviders.DANMAKU_TYPE) {
//                if (type.isDeleteFromList()) {
//                    continue;
//                }
                List<Tuple<Item, ItemStackTemplate>> tuples = type.getColorPairs().get();
                for (Tuple<Item, ItemStackTemplate> tuple : tuples) {
                    ItemView.addStackSensitive(tuple.getB().create());
                }
            }
            for (DanmakuShape danmakuShape : BuiltInRegistryProviders.DANMAKU_SHAPE) {
                ItemStack itemStack = danmakuShape.getItemStackOrThrow();
                ItemView.addStackSensitive(itemStack);
            }
            for (RoleCard roleCard : BuiltInRegistryProviders.ROLE_CARD) {
                ItemStackTemplate template = roleCard.getTemplate();
                ItemView.addStackSensitive(template.create());
            }
            for (Holder<Item> holder : RDGuiPlaceholderItems.getGuiPlaceholderItemList()) {
                ItemView.excludeItem(holder.value());
            }
            for (Holder<Item> holder : RDEntityHolderItems.HOLDERS) {
                ItemView.excludeItem(holder.value());
            }
            ItemViewItemInfo.registerItemInfo((items, component) -> {
                SlotContent slotContent = SlotContent.ofItemList(items);
                Identifier id = ReverieDreams.id(UUID.randomUUID().toString());
                ItemView.addInfoRecipe(new InfoClientRecipe(id, slotContent, component));
            });
        });
        ItemView.addClientRecipeProvider(recipeList -> {
            try {
                RecipeManager.KITCHEN_TYPE.getRegistryView().forEach((id, recipe) -> {
                    recipeList.add(new BaseKitchenRecipe(id, recipe, RRVRecipeTypes.getTypeByRecipe(recipe)));
                });
                RecipeManager.DANMAKU.getRegistryView().forEach((id, recipe) -> {
                    recipeList.add(new DanmakuCraftingTableRecipe(id, recipe));
                });
                RecipeManager.DANMAKU_SHAPE_DRAW.getRegistryView().forEach((id, recipe) -> {
                    recipeList.add(new DanmakuShapeDrawRecipe(id, recipe));
                });
                RecipeManager.GENSOKYO_ALTAR.getRegistryView().forEach((id, recipe) -> {
                    recipeList.add(new GensokyoAltarRecipe(id, recipe));
                });
                StrengthTableRecipe.createRecipeList().forEach(recipe -> {
                    String idPath = "%s_by_%s_%s_u%s".formatted(
                            recipe.getMainItem().asItem().builtInRegistryHolder().key().identifier().getPath(),
                            recipe.getMainItem().asItem().builtInRegistryHolder().key().identifier().getPath(),
                            recipe.getMainItem().asItem().builtInRegistryHolder().key().identifier().getPath(),
                            UUID.randomUUID().toString().charAt(2)
                    ).replaceAll("/", "_");
                    recipeList.add(new cc.thonly.reverie_dreams.compat.rrv.strength_table.StrengthTableRecipe(ReverieDreams.id(idPath), recipe));
                });
                RecipeManager.BREWING_BARREL.getRegistryView().forEach((id, recipe) -> {
                    recipeList.add(new BrewingBarrelRecipe(id, recipe));
                });
            } catch (Exception e) {
                log.error("Can't load rrv client recipe provider", e);
            }
        });
    }
}
