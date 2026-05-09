package cc.thonly.reverie_dreams.creative_tab.content;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.block.base.FruitLeavesBlock;
import cc.thonly.reverie_dreams.block.bundle.DecorativeBlockBundle;
import cc.thonly.reverie_dreams.block.bundle.WoodBundle;
import cc.thonly.reverie_dreams.registry.content.RDEnchantments;
import cc.thonly.reverie_dreams.registry.content.block.RDBlocks;
import cc.thonly.reverie_dreams.registry.content.effect.RDPotions;
import cc.thonly.reverie_dreams.registry.content.item.RDItems;
import net.blay09.mods.balm.world.item.BalmCreativeModeTabRegistrar;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;

import java.util.List;

public class ItemBlockCreativeTab implements ItemGroupContentHelper {
    public static final ResourceKey<CreativeModeTab> ITEM_GROUP_KEY = ResourceKey.create(Registries.CREATIVE_MODE_TAB, ReverieDreams.id("01_item_group"));

    public static void bootstrap(BalmCreativeModeTabRegistrar registrar) {
        ItemGroupContentHelper.registerGroup(registrar, ItemBlockCreativeTab.ITEM_GROUP_KEY, builder -> new CreativeModeTab.Builder(CreativeModeTab.Row.TOP, 0)
                .icon(() -> new ItemStack(RDItems.HAKUREI_CANE.asItem()))
                .title(Component.translatable("item_group.touhou_block_and_item"))
                .displayItems((parameters, output) -> {
                    for (ItemLike item : RDItems.getItemView()) {
                        output.accept(item);
                    }
                    HolderLookup.Provider registryAccess = parameters.holders();
                    for (ResourceKey<Enchantment> key : RDEnchantments.KEYS) {
                        List<ItemStack> books = RDEnchantments.getEnchantmentBook(registryAccess, key);
                        books.forEach(output::accept);
                    }
                    output.accept(RDPotions.createStackTemplate(RDPotions.ELIXIR_OF_LIFE_POTION).create());
                    output.accept(RDPotions.createStackTemplate(RDPotions.ELIXIR_OF_LIFE_POTION_INF).create());
                    output.accept(RDPotions.createStackTemplate(RDPotions.MENTAL_DISORDER_POTION).create());
                    output.accept(RDPotions.createStackTemplate(RDPotions.BACK_OF_LIFE_POTION).create());
                    output.accept(RDPotions.createStackTemplate(RDPotions.KANJU_KUSURI_POTION).create());

                    // 方块
                    for (Holder<Block> blockHolder : RDBlocks.BLOCKS) {
                        output.accept(blockHolder.value().asItem());
                    }
                    for (WoodBundle instance : WoodBundle.INSTANCES) {
                        instance.stream().forEach(block -> output.accept(block.asItem()));
                    }
                    for (DecorativeBlockBundle instance : DecorativeBlockBundle.INSTANCES) {
                        instance.stream().forEach(block -> output.accept(block.asItem()));
                    }
                    FruitLeavesBlock.FRUIT_LEAVES_BLOCKS.forEach(output::accept);
                }));
    }
}
