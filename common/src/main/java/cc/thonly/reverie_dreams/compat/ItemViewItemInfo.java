package cc.thonly.reverie_dreams.compat;

import cc.thonly.reverie_dreams.block.bundle.CropBlockBundle;
import cc.thonly.reverie_dreams.data.FumoType;
import cc.thonly.reverie_dreams.registry.BuiltInRegistryProviders;
import cc.thonly.reverie_dreams.registry.content.block.RDCropBlocks;
import cc.thonly.reverie_dreams.registry.content.item.RDBeverageItems;
import cc.thonly.reverie_dreams.registry.content.item.RDIngredientItems;
import cc.thonly.reverie_dreams.registry.delegate.RegistryDelegate;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;

import java.util.List;
import java.util.function.BiConsumer;

public class ItemViewItemInfo {
    public static void registerItemInfo(BiConsumer<List<Item>, Component> callback) {
        List<Item> chestDropItems = RDCropBlocks.CHEST_DROPS.stream().map(CropBlockBundle.Entry::getSeed).map(ItemLike::asItem).toList();
        List<Item> grassDropItems = RDCropBlocks.GRASS_DROPS.stream().map(CropBlockBundle.Entry::getSeed).map(ItemLike::asItem).toList();
        List<Item> fumos = BuiltInRegistryProviders.FUMO.values().stream().map(FumoType::block).map(Block::asItem).toList();
        List<Item> drinks = RDBeverageItems.BEVERAGE_ITEMS.stream().map(ItemLike::asItem).toList();
        List<Item> fishing = RDIngredientItems.FISHING.stream().map(RegistryDelegate::get).map(ItemLike::asItem).toList();
        List<Item> truffle = List.of(RDIngredientItems.TRUFFLE.asItem());

        callback.accept(chestDropItems, Component.translatable("item_view.information.desc.chest_drop_items"));
        callback.accept(grassDropItems, Component.translatable("item_view.information.desc.grass_drop_items"));
        callback.accept(fumos, Component.translatable("item_view.information.desc.fumos"));
        callback.accept(drinks, Component.translatable("item_view.information.desc.drinks"));
        callback.accept(fishing, Component.translatable("item_view.information.desc.fishing"));
        callback.accept(truffle, Component.translatable("item_view.information.desc.truffle"));
    }
}
