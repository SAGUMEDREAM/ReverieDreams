package cc.thonly.reverie_dreams.creative_tab.content;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.block.BlockTypeGroup;
import cc.thonly.reverie_dreams.block.creator.DecorativeBlockCreator;
import cc.thonly.reverie_dreams.registry.content.block.RDBlocks;
import cc.thonly.reverie_dreams.block.creator.WoodCreator;
import cc.thonly.reverie_dreams.block.base.FruitLeavesBlock;
import cc.thonly.reverie_dreams.registry.content.effect.RDPotions;
import cc.thonly.reverie_dreams.registry.content.item.RDItems;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;

public class ItemBlockCreativeTab implements ItemGroupContentHelper {
    public static final ResourceKey<CreativeModeTab> ITEM_GROUP_KEY = ResourceKey.create(Registries.CREATIVE_MODE_TAB, ReverieDreams.id("item_group"));
    public static final CreativeModeTab ITEM_GROUP = ItemGroupContentHelper.builder()
            .icon(() -> new ItemStack(RDItems.HAKUREI_CANE))
            .title(Component.translatable("item_group.touhou_block_and_item"))
            .build();

    public static void bootstrap() {
        ItemGroupEvents.modifyEntriesEvent(ItemBlockCreativeTab.ITEM_GROUP_KEY).register(itemGroup -> {
            itemGroup.acceptAll(RDItems.getItemView().stream().map(Item::getDefaultInstance).toList());
            for (ItemLike item : RDItems.getItemView()) {
                itemGroup.accept(item);
            }
            itemGroup.accept(RDItems.ROLE_CARD);
            itemGroup.accept(RDPotions.createStack(RDPotions.ELIXIR_OF_LIFE_POTION));
            itemGroup.accept(RDPotions.createStack(RDPotions.ELIXIR_OF_LIFE_POTION_INF));
            itemGroup.accept(RDPotions.createStack(RDPotions.MENTAL_DISORDER_POTION));
            itemGroup.accept(RDPotions.createStack(RDPotions.BACK_OF_LIFE_POTION));
            itemGroup.accept(RDPotions.createStack(RDPotions.KANJU_KUSURI_POTION));
            for (ItemLike item : RDBlocks.BLOCKS) {
                itemGroup.accept(item);
            }
            for (WoodCreator instance : WoodCreator.INSTANCES) {
                instance.stream().forEach(block -> itemGroup.accept(block.asItem()));
            }
            for (Block block : BlockTypeGroup.FRUIT_LEAVES.blocks()) {
                if (block instanceof FruitLeavesBlock fruitLeavesBlock) {
                    itemGroup.addAfter(fruitLeavesBlock.getEmptyLeavesBlock(), block);
                }
            }
            for (DecorativeBlockCreator instance : DecorativeBlockCreator.INSTANCES) {
                instance.stream().forEach(block -> itemGroup.accept(block.asItem()));
            }
            FruitLeavesBlock.FRUIT_LEAVES_BLOCKS.forEach(itemGroup::accept);
        });
        ItemGroupContentHelper.registerGroup(ItemBlockCreativeTab.ITEM_GROUP_KEY, ItemBlockCreativeTab.ITEM_GROUP);
    }
}
