package cc.thonly.reverie_dreams.creative_tab.content;

import cc.thonly.reverie_dreams.Touhou;
import cc.thonly.reverie_dreams.block.BlockTypeGroup;
import cc.thonly.reverie_dreams.block.DecorativeBlockCreator;
import cc.thonly.reverie_dreams.block.ModBlocks;
import cc.thonly.reverie_dreams.block.WoodCreator;
import cc.thonly.reverie_dreams.block.base.FruitLeavesBlock;
import cc.thonly.reverie_dreams.effect.ModPotions;
import cc.thonly.reverie_dreams.item.ModItems;
import eu.pb4.polymer.core.api.item.PolymerItemGroupUtils;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;

public class ItemBlockCreativeTab implements ItemGroupContent {
    public static final RegistryKey<ItemGroup> ITEM_GROUP_KEY = RegistryKey.of(RegistryKeys.ITEM_GROUP, Touhou.id("item_group"));
    public static final ItemGroup ITEM_GROUP = ItemGroupContent.builder()
            .icon(() -> new ItemStack(ModItems.HAKUREI_CANE))
            .displayName(Text.translatable("item_group.touhou_block_and_item"))
            .build();

    public static void bootstrap() {
        ItemGroupEvents.modifyEntriesEvent(ItemBlockCreativeTab.ITEM_GROUP_KEY).register(itemGroup -> {
            itemGroup.addAll(ModItems.getItemView().stream().map(Item::getDefaultStack).toList());
            for (ItemConvertible item : ModItems.getItemView()) {
                itemGroup.add(item);
            }
            itemGroup.add(ModItems.ROLE_CARD);
            itemGroup.add(ModPotions.createStack(ModPotions.ELIXIR_OF_LIFE_POTION));
            itemGroup.add(ModPotions.createStack(ModPotions.ELIXIR_OF_LIFE_POTION_INF));
            itemGroup.add(ModPotions.createStack(ModPotions.MENTAL_DISORDER_POTION));
            itemGroup.add(ModPotions.createStack(ModPotions.BACK_OF_LIFE_POTION));
            itemGroup.add(ModPotions.createStack(ModPotions.KANJU_KUSURI_POTION));
            for (ItemConvertible item : ModBlocks.BLOCKS) {
                itemGroup.add(item);
            }
            for (WoodCreator instance : WoodCreator.INSTANCES) {
                instance.stream().forEach(block -> itemGroup.add(block.asItem()));
            }
            for (Block block : BlockTypeGroup.FRUIT_LEAVES.blocks()) {
                if (block instanceof FruitLeavesBlock fruitLeavesBlock) {
                    itemGroup.addAfter(fruitLeavesBlock.getEmptyLeavesBlock(), block);
                }
            }
            for (DecorativeBlockCreator instance : DecorativeBlockCreator.INSTANCES) {
                instance.stream().forEach(block -> itemGroup.add(block.asItem()));
            }
            FruitLeavesBlock.FRUIT_LEAVES_BLOCKS.forEach(itemGroup::add);
        });
        ItemGroupContent.registerGroup(ItemBlockCreativeTab.ITEM_GROUP_KEY, ItemBlockCreativeTab.ITEM_GROUP);
    }
}
