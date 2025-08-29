package cc.thonly.reverie_dreams.item;

import cc.thonly.reverie_dreams.Touhou;
import cc.thonly.reverie_dreams.block.BlockTypeGroup;
import cc.thonly.reverie_dreams.block.DecorativeBlockCreator;
import cc.thonly.reverie_dreams.block.ModBlocks;
import cc.thonly.reverie_dreams.block.WoodCreator;
import cc.thonly.reverie_dreams.block.base.FruitLeavesBlock;
import cc.thonly.reverie_dreams.danmaku.DanmakuTypes;
import cc.thonly.reverie_dreams.danmaku.SpellCardTemplates;
import cc.thonly.reverie_dreams.effect.ModPotions;
import cc.thonly.reverie_dreams.fumo.Fumo;
import cc.thonly.reverie_dreams.fumo.Fumos;
import cc.thonly.reverie_dreams.registry.RegistryManager;
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
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class ModCreativeTabs {
    public static final RegistryKey<ItemGroup> ITEM_GROUP_KEY = RegistryKey.of(RegistryKeys.ITEM_GROUP, Touhou.id("item_group"));
    public static final RegistryKey<ItemGroup> BULLET_ITEM_GROUP_KEY = RegistryKey.of(RegistryKeys.ITEM_GROUP, Touhou.id("item_group_bullet"));
    public static final RegistryKey<ItemGroup> TEMPLATE_ITEM_GROUP_KEY = RegistryKey.of(RegistryKeys.ITEM_GROUP, Touhou.id("item_group_template"));
    public static final RegistryKey<ItemGroup> FUMO_ITEM_GROUP_KEY = RegistryKey.of(RegistryKeys.ITEM_GROUP, Touhou.id("item_group_fumo"));
    public static final RegistryKey<ItemGroup> ROLE_CARD_ITEM_GROUP_KEY = RegistryKey.of(RegistryKeys.ITEM_GROUP, Touhou.id("item_group_role_card"));

    public static final ItemGroup ITEM_GROUP = PolymerItemGroupUtils.builder()
            .icon(() -> new ItemStack(ModItems.HAKUREI_CANE))
            .displayName(Text.translatable("item_group.touhou_block_and_item"))
            .build();
    public static final ItemGroup ITEM_GROUP_BULLET = PolymerItemGroupUtils.builder()
            .icon(() -> new ItemStack(ModItems.DANMAKU))
            .displayName(Text.translatable("item_group.touhou.bullet"))
            .build();
    public static final ItemGroup TEMPLATE_ITEM_GROUP_BULLET = PolymerItemGroupUtils.builder()
            .icon(() -> new ItemStack(ModItems.SPELL_CARD_TEMPLATE))
            .displayName(Text.translatable("item_group.touhou.template"))
            .build();
    public static final ItemGroup ITEM_GROUP_FUMO = PolymerItemGroupUtils.builder()
            .icon(() -> new ItemStack(ModItems.FUMO_ICON))
            .displayName(Text.translatable("item_group.touhou.fumo"))
            .build();
    public static final ItemGroup ITEM_GROUP_ROLE_CARD = PolymerItemGroupUtils.builder()
            .icon(() -> new ItemStack(ModItems.ROLE_CARD))
            .displayName(Text.translatable("item_group.touhou.role_card"))
            .build();

    static {
        PolymerItemGroupUtils.registerPolymerItemGroup(ITEM_GROUP_KEY, ITEM_GROUP);
        PolymerItemGroupUtils.registerPolymerItemGroup(BULLET_ITEM_GROUP_KEY, ITEM_GROUP_BULLET);
        PolymerItemGroupUtils.registerPolymerItemGroup(TEMPLATE_ITEM_GROUP_KEY, TEMPLATE_ITEM_GROUP_BULLET);
        PolymerItemGroupUtils.registerPolymerItemGroup(FUMO_ITEM_GROUP_KEY, ITEM_GROUP_FUMO);
        PolymerItemGroupUtils.registerPolymerItemGroup(ROLE_CARD_ITEM_GROUP_KEY, ITEM_GROUP_ROLE_CARD);
    }

    public static void registerItemGroups() {
        ItemGroupEvents.modifyEntriesEvent(ITEM_GROUP_KEY).register(itemGroup -> {
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
                if (block instanceof FruitLeavesBlock fruitLeavesBlock){
                    itemGroup.addAfter(fruitLeavesBlock.getEmptyLeavesBlock(), block);
                }
            }
            for (DecorativeBlockCreator instance : DecorativeBlockCreator.INSTANCES) {
                instance.stream().forEach(block -> itemGroup.add(block.asItem()));
            }
            FruitLeavesBlock.FRUIT_LEAVES_BLOCKS.forEach(itemGroup::add);
        });
        ItemGroupEvents.modifyEntriesEvent(BULLET_ITEM_GROUP_KEY).register(itemGroup -> {
            List<ItemStack> color = DanmakuTypes.allColor();
            color.forEach(itemGroup::add);
        });
        ItemGroupEvents.modifyEntriesEvent(TEMPLATE_ITEM_GROUP_KEY).register(itemGroup -> {
            Map<Identifier, ItemStack> registryView = SpellCardTemplates.getRegistryItemStackView();
            Set<Map.Entry<Identifier, ItemStack>> views = registryView.entrySet();
            for (Map.Entry<Identifier, ItemStack> view : views) {
                ItemStack stack = view.getValue();
                itemGroup.add(stack.copy());
            }
        });
        ItemGroupEvents.modifyEntriesEvent(FUMO_ITEM_GROUP_KEY).register(itemGroup -> {
            itemGroup.add(ModItems.FUMO_LICENSE);
            for (Fumo fumo : Fumos.getView()) {
                itemGroup.add(fumo.item());
            }
        });
        ItemGroupEvents.modifyEntriesEvent(ROLE_CARD_ITEM_GROUP_KEY).register(itemGroup -> {
            RegistryManager.ROLE_CARD.values().forEach(instance -> itemGroup.add(instance.itemStack()));
        });
    }
}
