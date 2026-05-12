package cc.thonly.reverie_dreams.registry.content.item;

import cc.thonly.reverie_dreams.RDMPHooks;
import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.armor.*;
import cc.thonly.reverie_dreams.component.BattleStickRecorder;
import cc.thonly.reverie_dreams.component.DanmakuProperties;
import cc.thonly.reverie_dreams.item.ItemTypeGroup;
import cc.thonly.reverie_dreams.item.armor.DreamArmorItem;
import cc.thonly.reverie_dreams.item.armor.EarphoneItem;
import cc.thonly.reverie_dreams.item.armor.KoishiHatItem;
import cc.thonly.reverie_dreams.item.armor.WaterproofArmor;
import cc.thonly.reverie_dreams.item.base.*;
import cc.thonly.reverie_dreams.item.danmaku.SpellcardItem;
import cc.thonly.reverie_dreams.item.debug.BattleStickItem;
import cc.thonly.reverie_dreams.item.debug.OwnerStickItem;
import cc.thonly.reverie_dreams.item.material.DreamMaterial;
import cc.thonly.reverie_dreams.item.material.MagicIceMaterial;
import cc.thonly.reverie_dreams.item.material.SilverMaterial;
import cc.thonly.reverie_dreams.item.prop.*;
import cc.thonly.reverie_dreams.item.template.DanmakuShapeCreatorItem;
import cc.thonly.reverie_dreams.item.template.RoleCardItem;
import cc.thonly.reverie_dreams.item.template.RoleFollowerArchiveItem;
import cc.thonly.reverie_dreams.item.template.SpellCardTemplateItem;
import cc.thonly.reverie_dreams.item.weapon.*;
import cc.thonly.reverie_dreams.registry.content.component.RDDataComponents;
import cc.thonly.reverie_dreams.registry.tag.RDItemTags;
import cc.thonly.reverie_dreams.sound.JukeboxSongInit;
import net.blay09.mods.balm.world.item.BalmItemRegistrar;
import net.blay09.mods.balm.world.item.BalmItemRegistration;
import net.blay09.mods.balm.world.item.DeferredItem;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.food.Foods;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.BlocksAttacks;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public class RDItems {
    public static final List<DeferredItem> CREATIVE_TAB_ITEM_LIST = new ArrayList<>(128);
    public static final List<Holder<Item>> LATE_POLYMERIFY_ITEM_LIST = new ArrayList<>(128);
    public static final Supplier<ItemStack> NOT_COMPLETED = () -> {
        ItemStack itemStack = new ItemStack(Items.BARRIER);
        itemStack.set(DataComponents.ITEM_NAME, Component.literal("§cThis page is not completed"));
        return itemStack;
    };

    // 调试
    public static DeferredItem BATTLE_STICK;
    public static DeferredItem OWNER_STICK;

    // 图标
    public static DeferredItem ICON;
    public static DeferredItem FUMO_ICON;
    public static DeferredItem ROLE_ICON;
    public static DeferredItem SPAWN_EGG;
    public static DeferredItem DANMAKU;
    public static DeferredItem MYSTIA_ICON;

    // 材料
    public static DeferredItem POINT;
    public static DeferredItem POWER;
    public static DeferredItem DANMAKU_CORE;
    public static DeferredItem UPGRADED_HEALTH_FRAGMENT;
    public static DeferredItem BOMB_FRAGMENT;
    public static DeferredItem RED_ORB;
    public static DeferredItem BLUE_ORB;
    public static DeferredItem YELLOW_ORB;
    public static DeferredItem GREEN_ORB;
    public static DeferredItem PURPLE_ORB;
    public static DeferredItem YIN_YANG_ORB;
    public static DeferredItem SPEED_FEATHER;
    public static DeferredItem DREAM_CRYSTAL_FRAGMENT;
    public static DeferredItem EMPTY_PHOTO;
    public static DeferredItem COPPER_COIN;
    public static DeferredItem SILVER_COIN;
    public static DeferredItem GOLD_COIN;

    // 道具
    public static DeferredItem GUIDEBOOK;
    public static DeferredItem UPGRADED_HEALTH;
    public static DeferredItem BOMB;
    public static DeferredItem CROSSING_CHISEL;
    public static DeferredItem GAP_BALL;
    public static DeferredItem TIME_STOP_CLOCK;
    public static DeferredItem EARPHONE;
    public static DeferredItem KOISHI_HAT;
    public static DeferredItem FUMO_LICENSE;
    public static DeferredItem CURSED_DECOY_DOLl;
    public static DeferredItem VAISRAVANAS_PAGODA;
    public static DeferredItem DREAM_PILLOW;
    public static DeferredItem TENGU_SHIELD;
    public static DeferredItem TENGU_CAMERA;
    public static DeferredItem HIMEKAIDOU_HATATES_PHONE;
    public static DeferredItem BAD_APPLE;
    public static DeferredItem SCARECROW;
    public static DeferredItem EXORCISM_PAPER;
    public static DeferredItem SPELLCARD;
    public static DeferredItem SATORI_EYE;
    public static DeferredItem FAST_RECIPE_BOOK;

    // 武器
    public static DeferredItem HAKUREI_CANE;
    public static DeferredItem BAGUA_FURNACE;
    public static DeferredItem WIND_BLESSING_CANE;
    public static DeferredItem MAGIC_BROOM;
    public static DeferredItem GUNGNIR;
    public static DeferredItem LEVATIN;
    public static DeferredItem ROKANKEN;
    public static DeferredItem HAKUROKEN;
    public static DeferredItem PAPILIO_PATTERN_FAN;
    public static DeferredItem HORAI_DAMA_NO_EDA;
    public static DeferredItem YUKA_FLOWER_UMBRELLA;
    public static DeferredItem MAPLE_LEAF_FAN;
    public static DeferredItem IBUKIHO;
    public static DeferredItem WEAPON_OF_THE_MOON;

    public static DeferredItem SWORD_OF_HISOU;
    public static DeferredItem MANPOZUCHI;
    public static DeferredItem NUE_TRIDENT;
    public static DeferredItem TRUMPET_GUN;
    public static DeferredItem TREASURE_HUNTING_ROD;
    public static DeferredItem DEATH_SCYTHE;
    public static DeferredItem VIOLIN;
    public static DeferredItem KEYBOARD;
    public static DeferredItem TRUMPET;

    // 银装备
    public static DeferredItem RAW_SILVER;
    public static DeferredItem SILVER_INGOT;
    public static DeferredItem SILVER_NUGGET;
    public static DeferredItem SILVER_SWORD;
    public static DeferredItem SILVER_AXE;
    public static DeferredItem SILVER_PICKAXE;
    public static DeferredItem SILVER_SHOVEL;
    public static DeferredItem SILVER_HOE;
    public static DeferredItem SILVER_SPEAR;
    public static DeferredItem SILVER_HELMET;
    public static DeferredItem SILVER_CHESTPLATE;
    public static DeferredItem SILVER_LEGGINGS;
    public static DeferredItem SILVER_BOOTS;

    // 女仆装备
    public static DeferredItem KNIFE;
    public static DeferredItem MAID_HAIRBAND;
    public static DeferredItem MAID_UPPER_SKIRT;
    public static DeferredItem MAID_LOWER_SKIRT;
    public static DeferredItem MAID_SHOE;

    // 魔法冰装备
    public static DeferredItem ICE_SCALES;
    public static DeferredItem MAGIC_ICE_SWORD;
    public static DeferredItem MAGIC_ICE_AXE;
    public static DeferredItem MAGIC_ICE_PICKAXE;
    public static DeferredItem MAGIC_ICE_SHOVEL;
    public static DeferredItem MAGIC_ICE_HOE;
    public static DeferredItem MAGIC_ICE_SPEAR;
    public static DeferredItem MAGIC_ICE_HELMET;
    public static DeferredItem MAGIC_ICE_CHESTPLATE;
    public static DeferredItem MAGIC_ICE_LEGGINGS;
    public static DeferredItem MAGIC_ICE_BOOTS;

    // 梦境装备
    public static DeferredItem DREAM_SWORD;
    public static DeferredItem DREAM_AXE;
    public static DeferredItem DREAM_PICKAXE;
    public static DeferredItem DREAM_SHOVEL;
    public static DeferredItem DREAM_HOE;
    public static DeferredItem DREAM_SPEAR;
    public static DeferredItem DREAM_HELMET;
    public static DeferredItem DREAM_CHESTPLATE;
    public static DeferredItem DREAM_LEGGINGS;
    public static DeferredItem DREAM_BOOTS;

    // 防水衣
    public static DeferredItem WATERPROOF_LEATHER;
    public static DeferredItem WATER_PROOF_HAT;
    public static DeferredItem WATER_PROOF_CLOTHING;
    public static DeferredItem WATER_PROOF_LEGGINGS;
    public static DeferredItem WATER_PROOF_BOOTS;

    // 模板
    public static DeferredItem DANMAKU_SHAPE_CREATOR;
    public static DeferredItem SPELL_CARD_TEMPLATE;
    public static DeferredItem ROLE_CARD;
    public static DeferredItem ROLE_ARCHIVE;

    // 唱片
    public static DeferredItem HR01_01;
    public static DeferredItem HR02_08;
    public static DeferredItem HR03_01;
    public static DeferredItem MELODIC_TASTE_NIGHTMARE_BEFORE_CROSSROADS;
    public static DeferredItem YV_FLOWER_CLOCK_AND_DREAMS;
    public static DeferredItem GLOWING_NEEDLES_LITTLE_PEOPLE;
    public static DeferredItem COOKIE;
    public static DeferredItem BADAPPLE;

    @SuppressWarnings({"Convert2MethodRef"})
    public static void initialize(BalmItemRegistrar balmItemRegistrar) {
        // 调试
        BATTLE_STICK = registerSimpleItem(balmItemRegistrar, "battle_stick", props -> new BattleStickItem(props.stacksTo(1).component(RDDataComponents.BATTLE_STICK_RECORDER.value(), BattleStickRecorder.empty())), new Item.Properties());
        OWNER_STICK = registerSimpleItem(balmItemRegistrar, "owner_stick", props -> new OwnerStickItem(props.stacksTo(1)), new Item.Properties());

        // 图标
        ICON = registerCreativeTabIcon(balmItemRegistrar, "icon", Item::new, new Item.Properties());
        FUMO_ICON = registerCreativeTabIcon(balmItemRegistrar, "fumo_icon", Item::new, new Item.Properties());
        ROLE_ICON = registerCreativeTabIcon(balmItemRegistrar, "role_icon", Item::new, new Item.Properties());
        SPAWN_EGG = registerCreativeTabIcon(balmItemRegistrar, "spawn_egg", Item::new, new Item.Properties().component(DataComponents.DYED_COLOR, ColoredSpawnEggItem.DEFAULT_COLOR));
        DANMAKU = registerCreativeTabIcon(balmItemRegistrar, "danmaku", Item::new, new Item.Properties());
        MYSTIA_ICON = registerCreativeTabIcon(balmItemRegistrar, "mystia_icon", Item::new, new Item.Properties().stacksTo(1));

        // 材料
        POINT = registerItem(balmItemRegistrar, "point", props -> new Item(props), new Item.Properties());
        POWER = registerItem(balmItemRegistrar, "power", props -> new Item(props), new Item.Properties());
        DANMAKU_CORE = registerItem(balmItemRegistrar, "danmaku_core", props -> new Item(props.component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true)), new Item.Properties());
        UPGRADED_HEALTH_FRAGMENT = registerItem(balmItemRegistrar, "upgraded_health_fragment", props -> new Item(props), new Item.Properties());
        BOMB_FRAGMENT = registerItem(balmItemRegistrar, "bomb_fragment", props -> new Item(props), new Item.Properties());
        RED_ORB = registerItem(balmItemRegistrar, "red_orb", props -> new Item(props), new Item.Properties());
        BLUE_ORB = registerItem(balmItemRegistrar, "blue_orb", props -> new Item(props), new Item.Properties());
        YELLOW_ORB = registerItem(balmItemRegistrar, "yellow_orb", props -> new Item(props), new Item.Properties());
        GREEN_ORB = registerItem(balmItemRegistrar, "green_orb", props -> new Item(props), new Item.Properties());
        PURPLE_ORB = registerItem(balmItemRegistrar, "purple_orb", props -> new Item(props), new Item.Properties());
        YIN_YANG_ORB = registerItem(balmItemRegistrar, "yin-yang_orb", props -> new YinYangOrbItem(props.stacksTo(1)), new Item.Properties());
        SPEED_FEATHER = registerItem(balmItemRegistrar, "speed_feather", props -> new SpeedFeatherItem(props.component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true)), new Item.Properties());
        DREAM_CRYSTAL_FRAGMENT = registerItem(balmItemRegistrar, "dream_crystal_fragment", props -> new Item(props.component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true)), new Item.Properties());
        EMPTY_PHOTO = registerItem(balmItemRegistrar, "empty_photo", props -> new Item(props), new Item.Properties());
        COPPER_COIN = registerItem(balmItemRegistrar, "copper_coin", props -> new Item(props.stacksTo(96)), new Item.Properties());
        SILVER_COIN = registerItem(balmItemRegistrar, "silver_coin", props -> new Item(props.stacksTo(96)), new Item.Properties());
        GOLD_COIN = registerItem(balmItemRegistrar, "gold_coin", props -> new Item(props.stacksTo(96)), new Item.Properties());

        // 道具
        GUIDEBOOK = registerItem(balmItemRegistrar, "guidebook", props -> RDMPHooks.GuidebookFactory.EVENT.invoker().create(props.stacksTo(1).rarity(Rarity.EPIC).component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true)), new Item.Properties());
        UPGRADED_HEALTH = registerItem(balmItemRegistrar, "upgraded_health", props -> new UpgradedHealthItem(props), new Item.Properties());
        BOMB = registerItem(balmItemRegistrar, "bomb", props -> new BombItem(props.useCooldown(2.0f)), new Item.Properties());
        CROSSING_CHISEL = registerItem(balmItemRegistrar, "crossing_chisel", props -> new CrossingChisel(props.useCooldown(3.0f).component(RDDataComponents.MAX_DISTANCE.value(), CrossingChisel.DEFAULT_VALUE).stacksTo(1).durability(150)), new Item.Properties());
        GAP_BALL = registerItem(balmItemRegistrar, "gap_ball", props -> new GapBall(props.stacksTo(1)), new Item.Properties());
        TIME_STOP_CLOCK = registerItem(balmItemRegistrar, "time_stop_clock", props -> new TimeStopClock(props.stacksTo(1).durability(200).repairable(ItemTags.GOLD_TOOL_MATERIALS)), new Item.Properties());
        EARPHONE = registerItem(balmItemRegistrar, "earphone", props -> new EarphoneItem(props.durability(ArmorType.HELMET.getDurability(EarphoneArmorMaterial.BASE_DURABILITY))), new Item.Properties());
        KOISHI_HAT = registerItem(balmItemRegistrar, "koishi_hat", props -> new KoishiHatItem(props.durability(ArmorType.HELMET.getDurability(KoishiHatArmorMaterial.BASE_DURABILITY))), new Item.Properties());
        FUMO_LICENSE = registerItem(balmItemRegistrar, "fumo_license", props -> new FumoLicenseItem(props), new Item.Properties());
        CURSED_DECOY_DOLl = registerItem(balmItemRegistrar, "cursed_decoy_doll", props -> new CursedDecoyDollItem(props), new Item.Properties());
        VAISRAVANAS_PAGODA = registerItem(balmItemRegistrar, "vaisravanas_pagoda", props -> new VaisravanasPagodaItem(props.stacksTo(1).durability(250).repairable(RDItemTags.VAISRAVANAS_PAGODA)), new Item.Properties());
        DREAM_PILLOW = registerItem(balmItemRegistrar, "dream_pillow", props -> new DreamPillowItem(props.durability(4)), new Item.Properties());
        TENGU_SHIELD = registerItem(balmItemRegistrar, "tengu_shield", props -> new TenguShieldItem(props.stacksTo(1).durability(600).repairable(ItemTags.IRON_TOOL_MATERIALS).equippableUnswappable(EquipmentSlot.OFFHAND)
                .delayedComponent(DataComponents.BLOCKS_ATTACKS, context -> new BlocksAttacks(
                        0.25F,
                        1.0F,
                        List.of(new BlocksAttacks.DamageReduction(90.0F,
                                Optional.empty()
                                , 0.0F, 1.0F)
                        ),
                        new BlocksAttacks.ItemDamageFunction(3.0F, 1.0F, 1.0F),
                        Optional.of(context.getOrThrow(DamageTypeTags.BYPASSES_SHIELD)),
                        Optional.of(SoundEvents.SHIELD_BLOCK),
                        Optional.of(SoundEvents.SHIELD_BREAK)))
                .component(DataComponents.BREAK_SOUND, SoundEvents.SHIELD_BREAK)), new Item.Properties());
        TENGU_CAMERA = registerItem(balmItemRegistrar, "tengu_camera", props -> new TenguCameraItem(props.stacksTo(1).durability(250).repairable(ItemTags.REPAIRS_IRON_ARMOR)), new Item.Properties());
        HIMEKAIDOU_HATATES_PHONE = registerItem(balmItemRegistrar, "himekaidou_hatates_phone", props -> new HimekaidouHatatesPhone(props.component(RDDataComponents.FOV.value(), 75).stacksTo(1).durability(250).repairable(ItemTags.REPAIRS_IRON_ARMOR)), new Item.Properties());
        BAD_APPLE = registerItem(balmItemRegistrar, "bad_apple", props -> new BadAppleItem(props.food(Foods.GOLDEN_APPLE).stacksTo(16).rarity(Rarity.EPIC)), new Item.Properties());
        SCARECROW = registerItem(balmItemRegistrar, "scarecrow", props -> new ScarecrowItem(props), new Item.Properties());
        EXORCISM_PAPER = registerItem(balmItemRegistrar, "exorcism_paper", props -> new ExorcismPaperItem(props.stacksTo(16)), new Item.Properties());
        SPELLCARD = registerItem(balmItemRegistrar, "spellcard", props -> new SpellcardItem(props.stacksTo(1).durability(50)), new Item.Properties());
        SATORI_EYE = registerItem(balmItemRegistrar, "satori_eye", props -> new SatoriEye(props.stacksTo(1)), new Item.Properties());
        WEAPON_OF_THE_MOON = registerItem(balmItemRegistrar, "weapon_of_the_moon", props -> new WeaponOfTheMoon(props.stacksTo(1)), new Item.Properties());
        FAST_RECIPE_BOOK = registerItem(balmItemRegistrar, "fast_book_item", FastRecipeBook::new, new Item.Properties());

        // 武器
        HAKUREI_CANE = registerItem(balmItemRegistrar, "hakurei_cane", props -> new HakureiCane(1f, -2.4f, props), new Item.Properties());
        BAGUA_FURNACE = registerItem(balmItemRegistrar, "bagua_furnace", props -> new BaguaFurnace(props.stacksTo(1).durability(200).component(DataComponents.CONSUMABLE, new Consumable(5, ItemUseAnimation.BLOCK, Holder.direct(SoundEvents.FIRECHARGE_USE), false, new ArrayList<>())).repairable(Items.NETHERITE_INGOT)), new Item.Properties());
        WIND_BLESSING_CANE = registerItem(balmItemRegistrar, "wind_blessing_cane", props -> new WindBlessingCane(1f, -2.4f, props), new Item.Properties());
        MAGIC_BROOM = registerItem(balmItemRegistrar, "magic_broom", props -> new MagicBroom(1f, -2.4f, props), new Item.Properties());
        GUNGNIR = registerItem(balmItemRegistrar, "gungnir", props -> new Gungnir(props), new Item.Properties());
        LEVATIN = registerItem(balmItemRegistrar, "levatin", props -> new Levatin(1f, -2.4f, props), new Item.Properties());
        ROKANKEN = registerItem(balmItemRegistrar, "rokanken", props -> new Rokanken(2f, 0.5f - 2.4f, props), new Item.Properties());
        HAKUROKEN = registerItem(balmItemRegistrar, "hakuroken", props -> new Hakuroken(2f, 1f - 2.4f, props), new Item.Properties());
        PAPILIO_PATTERN_FAN = registerItem(balmItemRegistrar, "papilio_pattern_fan", props -> new PapilioPatternFan(1f - 4f, 1f - 2.4f, props), new Item.Properties());
        HORAI_DAMA_NO_EDA = registerItem(balmItemRegistrar, "horai-dama_no_eda", props -> new HoraiDamaNoEdaItem(0, 0, props), new Item.Properties());
        YUKA_FLOWER_UMBRELLA = registerItem(balmItemRegistrar, "yuka_flower_umbrella", props -> new YukaFlowerUmbrella(1f, -2.4f, props), new Item.Properties());
        MAPLE_LEAF_FAN = registerItem(balmItemRegistrar, "maple_leaf_fan", props -> new MapleLeafFan(1f, -2.4f, props), new Item.Properties());
        IBUKIHO = registerItem(balmItemRegistrar, "ibukiho", props -> new Ibukiho(1f, -2.4f, props.useCooldown(130f).fireResistant().food(new FoodProperties.Builder().alwaysEdible().saturationModifier(-4f).build(), Consumable.builder().consumeSeconds(3f).soundAfterConsume(SoundEvents.GENERIC_DRINK).build())), new Item.Properties());
        SWORD_OF_HISOU = registerItem(balmItemRegistrar, "sword_of_hisou", props -> new SwordOfHisou(2f, -2.4f, props), new Item.Properties());
        MANPOZUCHI = registerItem(balmItemRegistrar, "manpozuchi", props -> new ManpozuchiItem(3.5f, -2.5f, props), new Item.Properties());
        NUE_TRIDENT = registerItem(balmItemRegistrar, "nue_trident", props -> new NueTrident(3.5f, -2.8f, props), new Item.Properties());
        TRUMPET_GUN = registerItem(balmItemRegistrar, "trumpet_gun", props -> new TrumpetGun(props.stacksTo(1).durability(150).repairable(Items.GOLD_BLOCK)), new Item.Properties());
        TREASURE_HUNTING_ROD = registerItem(balmItemRegistrar, "treasure_hunting_rod", props -> new TreasureHuntingRod(2f, -2.8f, props), new Item.Properties());
        DEATH_SCYTHE = registerItem(balmItemRegistrar, "death_scythe", props -> new DeathScytheItem(1f, -2.8f, props), new Item.Properties());
        VIOLIN = registerItem(balmItemRegistrar, "violin", props -> new MusicalInstrumentItem(props.stacksTo(1).equippable(EquipmentSlot.HEAD).component(RDDataComponents.NOTE_TYPE.value(), NoteBlockInstrument.FLUTE)), new Item.Properties());
        KEYBOARD = registerItem(balmItemRegistrar, "keyboard", props -> new MusicalInstrumentItem(props.stacksTo(1).equippable(EquipmentSlot.HEAD).component(RDDataComponents.NOTE_TYPE.value(), NoteBlockInstrument.PLING)), new Item.Properties());
        TRUMPET = registerItem(balmItemRegistrar, "trumpet", props -> new MusicalInstrumentItem(props.stacksTo(1).equippable(EquipmentSlot.HEAD).component(RDDataComponents.NOTE_TYPE.value(), NoteBlockInstrument.TRUMPET)), new Item.Properties());

        // 银装备
        RAW_SILVER = registerItem(balmItemRegistrar, "raw_silver", props -> new Item(props), new Item.Properties());
        SILVER_INGOT = registerItem(balmItemRegistrar, "silver_ingot", props -> new Item(props), new Item.Properties());
        SILVER_NUGGET = registerItem(balmItemRegistrar, "silver_nugget", props -> new Item(props), new Item.Properties());
        SILVER_SWORD = registerItem(balmItemRegistrar, "silver_sword", props -> new SwordItem(SilverMaterial.INSTANCE, 3.0f, -2.4f, props.component(RDDataComponents.SILVER_ITEM.value(), Unit.INSTANCE)), new Item.Properties());
        SILVER_AXE = registerItem(balmItemRegistrar, "silver_axe", props -> new AxeItem(SilverMaterial.INSTANCE, 6.0f, -2.8f, props.component(RDDataComponents.SILVER_ITEM.value(), Unit.INSTANCE)), new Item.Properties());
        SILVER_PICKAXE = registerItem(balmItemRegistrar, "silver_pickaxe", props -> new PickaxeItem(SilverMaterial.INSTANCE, 1.0f, -2.8f, props.component(RDDataComponents.SILVER_ITEM.value(), Unit.INSTANCE)), new Item.Properties());
        SILVER_SHOVEL = registerItem(balmItemRegistrar, "silver_shovel", props -> new ShovelItem(SilverMaterial.INSTANCE, 1.5f, -3.0f, props.component(RDDataComponents.SILVER_ITEM.value(), Unit.INSTANCE)), new Item.Properties());
        SILVER_HOE = registerItem(balmItemRegistrar, "silver_hoe", props -> new HoeItem(SilverMaterial.INSTANCE, -2.0f, -1.0f, props.component(RDDataComponents.SILVER_ITEM.value(), Unit.INSTANCE)), new Item.Properties());
        SILVER_SPEAR = registerItem(balmItemRegistrar, "silver_spear", props -> new CustomSpear(props.spear(SilverMaterial.INSTANCE, 0.95F, 0.95F, 0.6F, 2.5F, 8.0F, 6.75F, 5.1F, 11.25F, 4.6F)), new Item.Properties());
        SILVER_HELMET = registerItem(balmItemRegistrar, "silver_helmet", props -> new ArmorItem(SilverArmorMaterial.INSTANCE, ArmorType.HELMET, props.component(RDDataComponents.SILVER_ITEM.value(), Unit.INSTANCE).durability(ArmorType.HELMET.getDurability(SilverArmorMaterial.BASE_DURABILITY))), new Item.Properties());
        SILVER_CHESTPLATE = registerItem(balmItemRegistrar, "silver_chestplate", props -> new ArmorItem(SilverArmorMaterial.INSTANCE, ArmorType.CHESTPLATE, props.component(RDDataComponents.SILVER_ITEM.value(), Unit.INSTANCE).durability(ArmorType.CHESTPLATE.getDurability(SilverArmorMaterial.BASE_DURABILITY))), new Item.Properties());
        SILVER_LEGGINGS = registerItem(balmItemRegistrar, "silver_leggings", props -> new ArmorItem(SilverArmorMaterial.INSTANCE, ArmorType.LEGGINGS, props.component(RDDataComponents.SILVER_ITEM.value(), Unit.INSTANCE).durability(ArmorType.LEGGINGS.getDurability(SilverArmorMaterial.BASE_DURABILITY))), new Item.Properties());
        SILVER_BOOTS = registerItem(balmItemRegistrar, "silver_boots", props -> new ArmorItem(SilverArmorMaterial.INSTANCE, ArmorType.BOOTS, props.component(RDDataComponents.SILVER_ITEM.value(), Unit.INSTANCE).durability(ArmorType.BOOTS.getDurability(SilverArmorMaterial.BASE_DURABILITY))), new Item.Properties());

        // 女仆装备
        KNIFE = registerItem(balmItemRegistrar, "knife", props -> new Knife(0f, 0f, props.stacksTo(1).component(RDDataComponents.DANMAKU_PROPERTIES.value(), DanmakuProperties.ofDefault().withSpeed(1.4f).withScale(1.8f))), new Item.Properties());
        MAID_HAIRBAND = registerItem(balmItemRegistrar, "maid_hairband", props -> new ArmorItem(MaidArmorMaterial.INSTANCE, ArmorType.HELMET, props.durability(ArmorType.HELMET.getDurability(MaidArmorMaterial.BASE_DURABILITY))), new Item.Properties());
        MAID_UPPER_SKIRT = registerItem(balmItemRegistrar, "maid_upper_skirt", props -> new ArmorItem(MaidArmorMaterial.INSTANCE, ArmorType.CHESTPLATE, props.durability(ArmorType.CHESTPLATE.getDurability(MaidArmorMaterial.BASE_DURABILITY))), new Item.Properties());
        MAID_LOWER_SKIRT = registerItem(balmItemRegistrar, "maid_lowerband", props -> new ArmorItem(MaidArmorMaterial.INSTANCE, ArmorType.LEGGINGS, props.durability(ArmorType.LEGGINGS.getDurability(MaidArmorMaterial.BASE_DURABILITY))), new Item.Properties());
        MAID_SHOE = registerItem(balmItemRegistrar, "maid_shoe", props -> new ArmorItem(MaidArmorMaterial.INSTANCE, ArmorType.BOOTS, props.durability(ArmorType.BOOTS.getDurability(MaidArmorMaterial.BASE_DURABILITY))), new Item.Properties());

        // 魔法冰装备
        ICE_SCALES = registerItem(balmItemRegistrar, "ice_scales", props -> new Item(props), new Item.Properties());
        MAGIC_ICE_SWORD = registerItem(balmItemRegistrar, "magic_ice_sword", props -> new SwordItem(MagicIceMaterial.INSTANCE, 3.0f, -2.4f, props), new Item.Properties());
        MAGIC_ICE_AXE = registerItem(balmItemRegistrar, "magic_ice_axe", props -> new AxeItem(MagicIceMaterial.INSTANCE, 6.0f, -2.8f, props), new Item.Properties());
        MAGIC_ICE_PICKAXE = registerItem(balmItemRegistrar, "magic_ice_pickaxe", props -> new PickaxeItem(MagicIceMaterial.INSTANCE, 1.0f, -2.8f, props), new Item.Properties());
        MAGIC_ICE_SHOVEL = registerItem(balmItemRegistrar, "magic_ice_shovel", props -> new ShovelItem(MagicIceMaterial.INSTANCE, 1.5f, -3.0f, props), new Item.Properties());
        MAGIC_ICE_HOE = registerItem(balmItemRegistrar, "magic_ice_hoe", props -> new HoeItem(MagicIceMaterial.INSTANCE, -2.0f, -1.0f, props), new Item.Properties());
        MAGIC_ICE_SPEAR = registerItem(balmItemRegistrar, "magic_ice_spear", props -> new CustomSpear(props.spear(MagicIceMaterial.INSTANCE, 0.95F, 0.95F, 0.6F, 2.5F, 8.0F, 6.75F, 5.1F, 11.25F, 4.6F)), new Item.Properties());
        MAGIC_ICE_HELMET = registerItem(balmItemRegistrar, "magic_ice_helmet", props -> new ArmorItem(MagicIceArmorMaterial.INSTANCE, ArmorType.HELMET, props.durability(ArmorType.HELMET.getDurability(MagicIceArmorMaterial.BASE_DURABILITY))), new Item.Properties());
        MAGIC_ICE_CHESTPLATE = registerItem(balmItemRegistrar, "magic_ice_chestplate", props -> new ArmorItem(MagicIceArmorMaterial.INSTANCE, ArmorType.CHESTPLATE, props.durability(ArmorType.CHESTPLATE.getDurability(MagicIceArmorMaterial.BASE_DURABILITY))), new Item.Properties());
        MAGIC_ICE_LEGGINGS = registerItem(balmItemRegistrar, "magic_ice_leggings", props -> new ArmorItem(MagicIceArmorMaterial.INSTANCE, ArmorType.LEGGINGS, props.durability(ArmorType.LEGGINGS.getDurability(MagicIceArmorMaterial.BASE_DURABILITY))), new Item.Properties());
        MAGIC_ICE_BOOTS = registerItem(balmItemRegistrar, "magic_ice_boots", props -> new ArmorItem(MagicIceArmorMaterial.INSTANCE, ArmorType.BOOTS, props.durability(ArmorType.BOOTS.getDurability(MagicIceArmorMaterial.BASE_DURABILITY))), new Item.Properties());

        // 梦境装备
        DREAM_SWORD = registerItem(balmItemRegistrar, "dream_sword", props -> new SwordItem(DreamMaterial.INSTANCE, 3.0f, -2.4f, props), new Item.Properties());
        DREAM_AXE = registerItem(balmItemRegistrar, "dream_axe", props -> new AxeItem(DreamMaterial.INSTANCE, 6.0f, -2.8f, props), new Item.Properties());
        DREAM_PICKAXE = registerItem(balmItemRegistrar, "dream_pickaxe", props -> new PickaxeItem(DreamMaterial.INSTANCE, 1.0f, -2.8f, props), new Item.Properties());
        DREAM_SHOVEL = registerItem(balmItemRegistrar, "dream_shovel", props -> new ShovelItem(DreamMaterial.INSTANCE, 1.5f, -3.0f, props), new Item.Properties());
        DREAM_HOE = registerItem(balmItemRegistrar, "dream_hoe", props -> new HoeItem(DreamMaterial.INSTANCE, -2.0f, -1.0f, props), new Item.Properties());
        DREAM_SPEAR = registerItem(balmItemRegistrar, "dream_spear", props -> new CustomSpear(props.spear(DreamMaterial.INSTANCE, 0.95F, 0.95F, 0.6F, 2.5F, 8.0F, 6.75F, 5.1F, 11.25F, 4.6F)), new Item.Properties());
        DREAM_HELMET = registerItem(balmItemRegistrar, "dream_helmet", props -> new DreamArmorItem(ArmorType.HELMET, props.durability(ArmorType.HELMET.getDurability(DreamArmorMaterial.BASE_DURABILITY))), new Item.Properties());
        DREAM_CHESTPLATE = registerItem(balmItemRegistrar, "dream_chestplate", props -> new DreamArmorItem(ArmorType.CHESTPLATE, props.durability(ArmorType.CHESTPLATE.getDurability(DreamArmorMaterial.BASE_DURABILITY))), new Item.Properties());
        DREAM_LEGGINGS = registerItem(balmItemRegistrar, "dream_leggings", props -> new DreamArmorItem(ArmorType.LEGGINGS, props.durability(ArmorType.LEGGINGS.getDurability(DreamArmorMaterial.BASE_DURABILITY))), new Item.Properties());
        DREAM_BOOTS = registerItem(balmItemRegistrar, "dream_boots", props -> new DreamArmorItem(ArmorType.BOOTS, props.durability(ArmorType.BOOTS.getDurability(DreamArmorMaterial.BASE_DURABILITY))), new Item.Properties());

        // 防水衣
        WATERPROOF_LEATHER = registerItem(balmItemRegistrar, "waterproof_leather", props -> new Item(props), new Item.Properties());
        WATER_PROOF_HAT = registerItem(balmItemRegistrar, "waterproof_hat", props -> new WaterproofArmor(ArmorType.HELMET, props.component(DataComponents.DYED_COLOR, new DyedItemColor(0xFF4AA9FF)).durability(ArmorType.HELMET.getDurability(WaterproofArmorMaterial.BASE_DURABILITY))), new Item.Properties());
        WATER_PROOF_CLOTHING = registerItem(balmItemRegistrar, "waterproof_clothing", props -> new WaterproofArmor(ArmorType.CHESTPLATE, props.component(DataComponents.DYED_COLOR, new DyedItemColor(0xFF4AA9FF)).durability(ArmorType.CHESTPLATE.getDurability(WaterproofArmorMaterial.BASE_DURABILITY))), new Item.Properties());
        WATER_PROOF_LEGGINGS = registerItem(balmItemRegistrar, "waterproof_leggings", props -> new WaterproofArmor(ArmorType.LEGGINGS, props.component(DataComponents.DYED_COLOR, new DyedItemColor(0xFF4AA9FF)).durability(ArmorType.LEGGINGS.getDurability(WaterproofArmorMaterial.BASE_DURABILITY))), new Item.Properties());
        WATER_PROOF_BOOTS = registerItem(balmItemRegistrar, "waterproof_boots", props -> new WaterproofArmor(ArmorType.BOOTS, props.component(DataComponents.DYED_COLOR, new DyedItemColor(0xFF4AA9FF)).durability(ArmorType.BOOTS.getDurability(WaterproofArmorMaterial.BASE_DURABILITY))), new Item.Properties());

        // 模板
        DANMAKU_SHAPE_CREATOR = registerItem(balmItemRegistrar, "danmaku_recipe_creator", props -> new DanmakuShapeCreatorItem(props), new Item.Properties());
        SPELL_CARD_TEMPLATE = registerItem(balmItemRegistrar, "spell_card_template", props -> new SpellCardTemplateItem(props), new Item.Properties());
        ROLE_CARD = registerItem(balmItemRegistrar, "role_card", props -> new RoleCardItem(props.stacksTo(1).component(DataComponents.TOOLTIP_DISPLAY, TooltipDisplay.DEFAULT.withHidden(DataComponents.DYED_COLOR, true)).component(DataComponents.DYED_COLOR, new DyedItemColor(RoleCard.DEFAULT_COLOR.intValue()))), new Item.Properties());
        ROLE_ARCHIVE = registerItem(balmItemRegistrar, "role_archive", props -> new RoleFollowerArchiveItem(props.stacksTo(1)), new Item.Properties());

        // 唱片
        HR01_01 = registerAlbum(balmItemRegistrar, "hr01_01", props -> new AlbumItem(props.jukeboxPlayable(JukeboxSongInit.HR01_01.getJukeboxSongKey())), new Item.Properties());
        HR02_08 = registerAlbum(balmItemRegistrar, "hr02_08", props -> new AlbumItem(props.jukeboxPlayable(JukeboxSongInit.HR02_08.getJukeboxSongKey())), new Item.Properties());
        HR03_01 = registerAlbum(balmItemRegistrar, "hr03_01", props -> new AlbumItem(props.jukeboxPlayable(JukeboxSongInit.HR03_01.getJukeboxSongKey())), new Item.Properties());
        MELODIC_TASTE_NIGHTMARE_BEFORE_CROSSROADS = registerAlbum(balmItemRegistrar, "melodic-taste-nightmare-before-crossroads", props -> new AlbumItem(props.jukeboxPlayable(JukeboxSongInit.MELODIC_TASTE_NIGHTMARE_BEFORE_CROSSROADS.getJukeboxSongKey())), new Item.Properties());
        YV_FLOWER_CLOCK_AND_DREAMS = registerAlbum(balmItemRegistrar, "yv_flower_clock_and_dreams", props -> new AlbumItem(props.jukeboxPlayable(JukeboxSongInit.YV_FLOWER_CLOCK_AND_DREAMS.getJukeboxSongKey())), new Item.Properties());
        GLOWING_NEEDLES_LITTLE_PEOPLE = registerAlbum(balmItemRegistrar, "glowing_needles_little_people", props -> new AlbumItem(props.jukeboxPlayable(JukeboxSongInit.GLOWING_NEEDLES_LITTLE_PEOPLE.getJukeboxSongKey())), new Item.Properties());
        COOKIE = registerAlbum(balmItemRegistrar, "cookie", props -> new AlbumItem(props.jukeboxPlayable(JukeboxSongInit.COOKIE.getJukeboxSongKey())), new Item.Properties());
        BADAPPLE = registerAlbum(balmItemRegistrar, "bad-apple", props -> new AlbumItem(props.jukeboxPlayable(JukeboxSongInit.BAD_APPLE.getJukeboxSongKey())), new Item.Properties());
    }

    public static DeferredItem registerItem(BalmItemRegistrar balmItemRegistrar, String id, Function<Item.Properties, Item> factory, Item.Properties settings) {
        DeferredItem item = registerSimpleItem(balmItemRegistrar, id, factory, settings);
        CREATIVE_TAB_ITEM_LIST.add(item);
        return item;
    }

    public static DeferredItem registerCreativeTabIcon(BalmItemRegistrar balmItemRegistrar, String id, Function<Item.Properties, Item> factory, Item.Properties settings) {
        return registerSimpleItem(balmItemRegistrar, id, factory, settings);
    }

    public static DeferredItem registerAlbum(BalmItemRegistrar balmItemRegistrar, String id, Function<Item.Properties, Item> factory, Item.Properties settings) {
        return registerSimpleItem(balmItemRegistrar, id, factory, settings);
    }

    public static DeferredItem registerSimpleItem(BalmItemRegistrar balmItemRegistrar, String name, Function<Item.Properties, Item> factory, Item.Properties settings) {
        BalmItemRegistration balmItem = balmItemRegistrar.register(name, factory, settings.setId(keyOf(name)));
        DeferredItem item = balmItem.asDeferredItem();
        ReverieDreams.LATE_INIT.add(() -> ItemTypeGroup.join(item.asItem()));
        LATE_POLYMERIFY_ITEM_LIST.add(item);
        return item;
    }

    public static ResourceKey<Item> keyOf(Identifier id) {
        return ResourceKey.create(Registries.ITEM, id);
    }

    public static ResourceKey<Item> keyOf(String id) {
        return ResourceKey.create(Registries.ITEM, ReverieDreams.id(id));
    }

    public static ResourceKey<Item> keyOf(ResourceKey<Block> blockKey) {
        return ResourceKey.create(Registries.ITEM, blockKey.identifier());
    }

    public static List<DeferredItem> getItemView() {
        return List.copyOf(CREATIVE_TAB_ITEM_LIST);
    }

}
