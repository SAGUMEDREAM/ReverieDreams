package cc.thonly.reverie_dreams.compat.rei;

import cc.thonly.reverie_dreams.block.kitchen.AbstractKitchenwareBlock;
import cc.thonly.reverie_dreams.compat.ItemViewItemInfo;
import cc.thonly.reverie_dreams.compat.rei.category.*;
import cc.thonly.reverie_dreams.registry.content.block.RDBlocks;
import cc.thonly.reverie_dreams.registry.content.item.RDItems;
import cc.thonly.reverie_dreams.util.item.REIItemUtils;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import me.shedaniel.rei.plugin.common.displays.DefaultInformationDisplay;
import net.minecraft.world.level.ItemLike;

public class ClientREIPlugin implements REIClientPlugin {
    @Override
    public void registerCategories(CategoryRegistry registry) {
        registry.add(new DanmakuCraftingTableRecipeCategory());
        registry.add(new DanmakuShapeDrawRecipeCategory());
        registry.add(new GensokyoAltarRecipeCategory());
        registry.add(new StrengthTableRecipeCategory());
        registry.add(new KitchenRecipeCategory());

        registry.addWorkstations(REICategoryIdentifiers.DANMAKU_CRAFTING_TABLE, REIItemUtils.getItem(RDBlocks.DANMAKU_CRAFTING_TABLE));
        registry.addWorkstations(REICategoryIdentifiers.DANMAKU_SHAPE_DRAW, REIItemUtils.getItem(RDItems.DANMAKU_SHAPE_CREATOR));
        registry.addWorkstations(REICategoryIdentifiers.GENSOKYO_ALTAR, REIItemUtils.getItem(RDBlocks.GENSOKYO_ALTAR));
        registry.addWorkstations(REICategoryIdentifiers.STRENGTH_TABLE, REIItemUtils.getItem(RDBlocks.STRENGTH_TABLE));
        for (AbstractKitchenwareBlock block : AbstractKitchenwareBlock.KITCHENWARE_BLOCKS) {
            registry.addWorkstations(REICategoryIdentifiers.KITCHEN, REIItemUtils.getItem(block));
        }
    }

    @Override
    public void registerDisplays(DisplayRegistry registry) {
//        IDisplayRegisterView view = IDisplayRegisterView.getClientRecipeRegisters(registry);
//        DisplayImpls.register(view);

        ItemViewItemInfo.registerItemInfo((items, component) -> {
            registry.add(DefaultInformationDisplay.createFromEntries(
                    EntryIngredients.ofItems(items.stream().map(item -> (ItemLike) item).toList()),
                    component
            ).lines(component));
        });
    }
}
