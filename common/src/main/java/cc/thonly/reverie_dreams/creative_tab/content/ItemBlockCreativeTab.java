package cc.thonly.reverie_dreams.creative_tab.content;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.block.base.FruitLeavesBlock;
import cc.thonly.reverie_dreams.block.bundle.DecorativeBlockBundle;
import cc.thonly.reverie_dreams.block.bundle.WoodBundle;
import cc.thonly.reverie_dreams.registry.content.RDEnchantments;
import cc.thonly.reverie_dreams.registry.content.FumoTypes;
import cc.thonly.reverie_dreams.registry.content.block.RDBlocks;
import cc.thonly.reverie_dreams.registry.content.effect.RDPotions;
import cc.thonly.reverie_dreams.registry.content.item.RDItems;
import cc.thonly.reverie_dreams.registry.content.item.RDWorkshopItems;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class ItemBlockCreativeTab implements ItemGroupContentHelper {
    public static final ResourceKey<CreativeModeTab> ITEM_GROUP_KEY = ResourceKey.create(Registries.CREATIVE_MODE_TAB, ReverieDreams.id("01_item_group"));

    public static void bootstrap() {
        ItemGroupContentHelper.registerGroup(ItemBlockCreativeTab.ITEM_GROUP_KEY, builder -> new CreativeModeTab.Builder(CreativeModeTab.Row.TOP, 0)
                .icon(() -> new ItemStack(RDItems.HAKUREI_CANE.asItem()))
                .title(Component.translatable("item_group.touhou_block_and_item"))
                .displayItems(ItemBlockCreativeTab::displayItems));
    }

    private static void displayItems(CreativeModeTab.ItemDisplayParameters parameters, CreativeModeTab.Output output) {
        Set<Item> remaining = collectItems();
        List<Item> buckets = new ArrayList<>();
        displayMatching(remaining, buckets::add, BucketItem.class::isInstance);

        // Materials and ingredients, with color, ore and currency families kept together.
        display(remaining, output::accept, RDItems.POINT, RDItems.POWER, RDItems.UPGRADED_HEALTH_FRAGMENT, RDItems.BOMB_FRAGMENT,
                RDItems.DANMAKU_CORE, RDItems.FAIRY_DUST, RDItems.GHOST_VEIL, RDItems.ONI_HORN, RDItems.ICE_SCALES,
                RDItems.RED_ORB, RDItems.YELLOW_ORB, RDItems.BLUE_ORB, RDItems.GREEN_ORB, RDItems.PURPLE_ORB,
                RDItems.RAW_SILVER, RDItems.SILVER_INGOT, RDItems.SILVER_NUGGET, RDItems.WATERPROOF_LEATHER,
                RDItems.DREAM_CRYSTAL_FRAGMENT, RDItems.LUNAR_POWER_CELL, RDItems.LUNAR_SIGNAL_COIL, RDItems.LUNAR_SIGNAL_CRYSTAL,
                RDItems.COPPER_COIN, RDItems.SILVER_COIN, RDItems.GOLD_COIN,
                RDWorkshopItems.DRIED_HERBS, RDWorkshopItems.HERBAL_POWDER);

        // Props tied to a character are grouped by that character's source; mod systems follow.
        display(remaining, output::accept, RDItems.UPGRADED_HEALTH, RDItems.BOMB, RDItems.EXORCISM_PAPER,
                RDItems.TIME_STOP_CLOCK, RDItems.GAP_BALL, RDItems.BOUNDARY_OFUDA,
                RDItems.TENGU_CAMERA, RDItems.EMPTY_PHOTO, RDItems.SATORI_EYE, RDItems.HIMEKAIDOU_HATATES_PHONE,
                RDItems.GUIDEBOOK, RDItems.BAD_APPLE, RDItems.SPEED_FEATHER, RDItems.SCARECROW, RDItems.CHEQUE,
                RDItems.DREAM_PILLOW, RDItems.DREAM_MIRROR, RDItems.DREAM_COCOON, RDItems.DREAM_SPOOL,
                RDItems.DANMAKU_SHAPE_CREATOR, RDItems.SPELL_CARD_TEMPLATE, RDItems.ROLE_CARD, RDItems.ROLE_ARCHIVE, RDItems.SOUL_CARD);
        // Equipment follows verified item or spell-card references, or the associated character for mod adaptations.
        // https://omake.thwiki.cc/
        // https://www.thwiki.cc/原作物品列表
        List<Item> equipment = new ArrayList<>();
        display(remaining, equipment::add, RDItems.YIN_YANG_ORB, RDItems.HAKUREI_CANE, RDItems.MAGIC_BROOM,
                RDItems.YUKA_FLOWER_UMBRELLA, RDItems.KNIFE, RDItems.LEVATIN,
                RDItems.VIOLIN, RDItems.TRUMPET, RDItems.KEYBOARD, RDItems.ROKANKEN, RDItems.HAKUROKEN, RDItems.PAPILIO_PATTERN_FAN,
                RDItems.BAGUA_FURNACE, RDItems.HORAI_DAMA_NO_EDA,
                RDItems.GUNGNIR, RDItems.IBUKIHO, RDItems.MAPLE_LEAF_FAN, RDItems.DEATH_SCYTHE,
                RDItems.WIND_BLESSING_CANE, RDItems.SWORD_OF_HISOU, RDItems.TREASURE_HUNTING_ROD,
                RDItems.VAISRAVANAS_PAGODA, RDItems.NUE_TRIDENT, RDItems.MANPOZUCHI,
                RDItems.CROSSING_CHISEL, RDItems.TENGU_SHIELD, RDItems.SUNFLOWER, RDItems.TRUMPET_GUN,
                RDItems.IRON_BAR, RDItems.ONI_IRON_BAR,
                RDItems.SILVER_SWORD, RDItems.SILVER_SPEAR, RDItems.SILVER_AXE, RDItems.SILVER_PICKAXE, RDItems.SILVER_SHOVEL, RDItems.SILVER_HOE,
                RDItems.MAGIC_ICE_SWORD, RDItems.MAGIC_ICE_SPEAR, RDItems.MAGIC_ICE_AXE, RDItems.MAGIC_ICE_PICKAXE, RDItems.MAGIC_ICE_SHOVEL, RDItems.MAGIC_ICE_HOE,
                RDItems.DREAM_SWORD, RDItems.DREAM_SPEAR, RDItems.DREAM_AXE, RDItems.DREAM_PICKAXE, RDItems.DREAM_SHOVEL, RDItems.DREAM_HOE);
        RDItems.LUNAR_WEAPONS.forEach(item -> display(remaining, equipment::add, item));
        RDItems.LUNAR_MAGAZINES.forEach(item -> display(remaining, equipment::add, item));

        List<Item> armor = new ArrayList<>();
        display(remaining, armor::add, RDItems.MAID_HAIRBAND, RDItems.MAID_UPPER_SKIRT, RDItems.MAID_LOWER_SKIRT, RDItems.MAID_SHOE,
                RDItems.KOISHI_HAT, RDItems.EARPHONE, RDItems.ONI_MASK, RDItems.LOW_GRAVITY_BOOT, RDItems.CROWN_OF_THE_UNDERWORLD,
                RDItems.SILVER_HELMET, RDItems.SILVER_CHESTPLATE, RDItems.SILVER_LEGGINGS, RDItems.SILVER_BOOTS,
                RDItems.MAGIC_ICE_HELMET, RDItems.MAGIC_ICE_CHESTPLATE, RDItems.MAGIC_ICE_LEGGINGS, RDItems.MAGIC_ICE_BOOTS,
                RDItems.DREAM_HELMET, RDItems.DREAM_CHESTPLATE, RDItems.DREAM_LEGGINGS, RDItems.DREAM_BOOTS,
                RDItems.WATER_PROOF_HAT, RDItems.WATER_PROOF_CLOTHING, RDItems.WATER_PROOF_LEGGINGS, RDItems.WATER_PROOF_BOOTS);
        displayMatching(remaining, armor::add, item -> !(item instanceof BlockItem) && isArmor(item));
        displayMatching(remaining, equipment::add, item -> !(item instanceof BlockItem) && isEquipment(item));

        displayMatching(remaining, output::accept, item -> !(item instanceof BlockItem));
        equipment.forEach(output::accept);
        armor.forEach(output::accept);

        display(remaining, output::accept, RDBlocks.POINT_BLOCK, RDBlocks.POWER_BLOCK,
                RDBlocks.ORB_ORE, RDBlocks.DEEPSLATE_ORB_ORE, RDBlocks.RED_ORB_BLOCK, RDBlocks.YELLOW_ORB_BLOCK,
                RDBlocks.BLUE_ORB_BLOCK, RDBlocks.GREEN_ORB_BLOCK, RDBlocks.PURPLE_ORB_BLOCK,
                RDBlocks.SILVER_ORE, RDBlocks.DEEPSLATE_SILVER_ORE, RDBlocks.SILVER_BLOCK,
                RDBlocks.MOON_IRON_ORE, RDBlocks.MOON_GOLD_ORE, RDBlocks.MOON_DIAMOND_ORE, RDBlocks.MOON_QUARTZ_ORE,
                RDBlocks.DREAM_CRYSTAL_ORE, RDBlocks.DREAM_RED_BLOCK, RDBlocks.DREAM_BLUE_BLOCK,
                RDBlocks.BLUE_MUSHROOM_BLOCK, RDBlocks.PURPLE_MUSHROOM_BLOCK, RDBlocks.MAGIC_MUSHROOM_STEM,
                RDBlocks.DANMAKU_CRAFTING_TABLE, RDBlocks.STRENGTH_TABLE, RDBlocks.GENSOKYO_ALTAR);
        remaining.forEach(output::accept);

        buckets.forEach(output::accept);
        for (var potion : RDPotions.POTIONS) {
            output.accept(RDPotions.createStackTemplate(potion).create());
        }
        for (ResourceKey<Enchantment> key : RDEnchantments.KEYS) {
            RDEnchantments.getEnchantmentBook(parameters.holders(), key).forEach(output::accept);
        }
    }

    private static Set<Item> collectItems() {
        Set<Item> excluded = new HashSet<>(DecorationCreativeTab.getItems());
        excluded.removeAll(DecorationCreativeTab.getLeaves());
        excluded.addAll(KitchenwareCreativeTab.getItems());
        excluded.addAll(SeedCreativeTab.getItems());
        excluded.add(RDItems.SPELLCARD.asItem());
        FumoTypes.getView().forEach(fumo -> excluded.add(fumo.item()));

        Set<Item> items = new LinkedHashSet<>();
        RDItems.getItemView().forEach(item -> items.add(item.asItem()));
        Set<Item> woodItems = new LinkedHashSet<>();
        WoodBundle.INSTANCES.forEach(wood -> wood.stream().forEach(block -> woodItems.add(block.asItem())));
        for (Holder<Block> holder : RDBlocks.HOLDERS) {
            Item item = holder.value().asItem();
            if (!woodItems.contains(item)) items.add(item);
        }
        items.addAll(woodItems);
        DecorativeBlockBundle.INSTANCES.forEach(bundle -> bundle.stream().forEach(block -> items.add(block.asItem())));
        FruitLeavesBlock.FRUIT_LEAVES_BLOCKS.forEach(block -> items.add(block.asItem()));
        items.remove(Items.AIR);
        items.removeAll(excluded);
        return items;
    }

    private static boolean isArmor(Item item) {
        var equippable = item.components().get(DataComponents.EQUIPPABLE);
        return equippable != null && equippable.slot().isArmor();
    }

    private static boolean isEquipment(Item item) {
        var components = item.components();
        return components.has(DataComponents.WEAPON) || components.has(DataComponents.KINETIC_WEAPON)
                || components.has(DataComponents.TOOL) || components.has(DataComponents.BLOCKS_ATTACKS);
    }

    private static void display(Set<Item> remaining, Consumer<Item> output, ItemLike... entries) {
        for (ItemLike entry : entries) {
            Item item = entry.asItem();
            if (remaining.remove(item)) output.accept(item);
        }
    }

    private static void displayMatching(Set<Item> remaining, Consumer<Item> output, Predicate<Item> matches) {
        for (Iterator<Item> iterator = remaining.iterator(); iterator.hasNext(); ) {
            Item item = iterator.next();
            if (matches.test(item)) {
                output.accept(item);
                iterator.remove();
            }
        }
    }
}
