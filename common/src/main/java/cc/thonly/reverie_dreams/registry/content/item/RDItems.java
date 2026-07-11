package cc.thonly.reverie_dreams.registry.content.item;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.armor.*;
import cc.thonly.reverie_dreams.component.BattleStickRecorder;
import cc.thonly.reverie_dreams.component.DanmakuProperties;
import cc.thonly.reverie_dreams.item.ItemTypeGroup;
import cc.thonly.reverie_dreams.item.armor.*;
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
import cc.thonly.reverie_dreams.proxy.GuidebookFactory;
import cc.thonly.reverie_dreams.registry.ReverieDreamsRegistries;
import cc.thonly.reverie_dreams.registry.content.component.RDDataComponents;
import cc.thonly.reverie_dreams.registry.impl.ItemDelegate;
import cc.thonly.reverie_dreams.registry.tag.RDItemTags;
import cc.thonly.reverie_dreams.sound.JukeboxSongInit;
import dev.architectury.registry.registries.RegistrySupplier;
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
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.food.Foods;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.*;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

@SuppressWarnings("Convert2MethodRef")
public class RDItems {
    public static final List<ItemDelegate> CREATIVE_TAB_ITEM_LIST = new ArrayList<>(128);
    public static final List<Holder<Item>> LATE_POLYMERIFY_ITEM_LIST = new ArrayList<>(128);
    public static final Supplier<ItemStack> NOT_COMPLETED = () -> {
        ItemStack itemStack = new ItemStack(Items.BARRIER);
        itemStack.set(DataComponents.ITEM_NAME, Component.literal("§cThis page is not completed"));
        return itemStack;
    };

    // 调试
    public static final ItemDelegate BATTLE_STICK = registerSimpleItem("battle_stick", props -> new BattleStickItem(props.stacksTo(1).component(RDDataComponents.BATTLE_STICK_RECORDER.value(), BattleStickRecorder.empty())), new Item.Properties());
    public static final ItemDelegate OWNER_STICK = registerSimpleItem("owner_stick", props -> new OwnerStickItem(props.stacksTo(1)), new Item.Properties());

    // 图标
    public static final ItemDelegate ICON = registerCreativeTabIcon("icon", Item::new, new Item.Properties());
    public static final ItemDelegate FUMO_ICON = registerCreativeTabIcon("fumo_icon", Item::new, new Item.Properties());
    public static final ItemDelegate ROLE_ICON = registerCreativeTabIcon("role_icon", Item::new, new Item.Properties());
    public static final ItemDelegate SPAWN_EGG = registerCreativeTabIcon("spawn_egg", Item::new, new Item.Properties()
            .component(DataComponents.DYED_COLOR, ColoredSpawnEggItem.DEFAULT_COLOR));
    public static final ItemDelegate DANMAKU = registerCreativeTabIcon("danmaku", Item::new, new Item.Properties());
    public static final ItemDelegate MYSTIA_ICON = registerCreativeTabIcon("mystia_icon", Item::new, new Item.Properties()
            .stacksTo(1));

    // 材料
    public static final ItemDelegate POINT = registerItem("point", props -> new Item(props), new Item.Properties());
    public static final ItemDelegate POWER = registerItem("power", props -> new Item(props), new Item.Properties());
    public static final ItemDelegate DANMAKU_CORE = registerItem("danmaku_core", props -> new Item(props
            .component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true)
    ), new Item.Properties());
    public static final ItemDelegate UPGRADED_HEALTH_FRAGMENT = registerItem("upgraded_health_fragment", props -> new Item(props), new Item.Properties());
    public static final ItemDelegate BOMB_FRAGMENT = registerItem("bomb_fragment", props -> new Item(props), new Item.Properties());
    public static final ItemDelegate RED_ORB = registerItem("red_orb", props -> new Item(props), new Item.Properties());
    public static final ItemDelegate BLUE_ORB = registerItem("blue_orb", props -> new Item(props), new Item.Properties());
    public static final ItemDelegate YELLOW_ORB = registerItem("yellow_orb", props -> new Item(props), new Item.Properties());
    public static final ItemDelegate GREEN_ORB = registerItem("green_orb", props -> new Item(props), new Item.Properties());
    public static final ItemDelegate PURPLE_ORB = registerItem("purple_orb", props -> new Item(props), new Item.Properties());
    public static final ItemDelegate YIN_YANG_ORB = registerItem("yin-yang_orb", props -> new YinYangOrbItem(props.stacksTo(1)), new Item.Properties());
    public static final ItemDelegate SPEED_FEATHER = registerItem("speed_feather", props -> new SpeedFeatherItem(props
            .component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true)), new Item.Properties());
    public static final ItemDelegate DREAM_CRYSTAL_FRAGMENT = registerItem("dream_crystal_fragment", props -> new Item(props
            .component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true)), new Item.Properties());
    public static final ItemDelegate EMPTY_PHOTO = registerItem("empty_photo", props -> new Item(props), new Item.Properties());
    public static final ItemDelegate COPPER_COIN = registerItem("copper_coin", props -> new Item(props.stacksTo(96)), new Item.Properties());
    public static final ItemDelegate SILVER_COIN = registerItem("silver_coin", props -> new Item(props.stacksTo(96)), new Item.Properties());
    public static final ItemDelegate GOLD_COIN = registerItem("gold_coin", props -> new Item(props.stacksTo(96)), new Item.Properties());

    // 道具
    public static final ItemDelegate GUIDEBOOK = registerItem("guidebook", props -> GuidebookFactory.EVENT.invoker().create(props.stacksTo(1).rarity(Rarity.EPIC).component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true)), new Item.Properties());
    public static final ItemDelegate UPGRADED_HEALTH = registerItem("upgraded_health", props -> new UpgradedHealthItem(props), new Item.Properties());
    public static final ItemDelegate BOMB = registerItem("bomb", props -> new BombItem(props.useCooldown(2.0f)), new Item.Properties());
    public static final ItemDelegate CROSSING_CHISEL = registerItem("crossing_chisel", props -> new CrossingChisel(props.useCooldown(3.0f).component(RDDataComponents.MAX_DISTANCE.value(), CrossingChisel.DEFAULT_VALUE).stacksTo(1).durability(150)), new Item.Properties());
    public static final ItemDelegate GAP_BALL = registerItem("gap_ball", props -> new GapBall(props.stacksTo(1)), new Item.Properties());
    public static final ItemDelegate TIME_STOP_CLOCK = registerItem("time_stop_clock", props -> new TimeStopClock(props.stacksTo(1).durability(200).repairable(ItemTags.GOLD_TOOL_MATERIALS)), new Item.Properties());
    public static final ItemDelegate EARPHONE = registerItem("earphone", props -> new EarphoneItem(props.durability(ArmorType.HELMET.getDurability(EarphoneArmorMaterial.BASE_DURABILITY))), new Item.Properties());
    public static final ItemDelegate KOISHI_HAT = registerItem("koishi_hat", props -> new KoishiHatItem(props.durability(ArmorType.HELMET.getDurability(KoishiHatArmorMaterial.BASE_DURABILITY))), new Item.Properties());
    public static final ItemDelegate FUMO_LICENSE = registerItem("fumo_license", props -> new FumoLicenseItem(props), new Item.Properties());
    public static final ItemDelegate CURSED_DECOY_DOLl = registerItem("cursed_decoy_doll", props -> new CursedDecoyDollItem(props), new Item.Properties());
    public static final ItemDelegate VAISRAVANAS_PAGODA = registerItem("vaisravanas_pagoda", props -> new VaisravanasPagodaItem(props
            .stacksTo(1)
            .durability(250).repairable(RDItemTags.VAISRAVANAS_PAGODA)), new Item.Properties());
    public static final ItemDelegate DREAM_PILLOW = registerItem("dream_pillow", props -> new DreamPillowItem(props
            .durability(4)), new Item.Properties());
    public static final ItemDelegate TENGU_SHIELD = registerItem("tengu_shield", props -> new TenguShieldItem(props
            .stacksTo(1)
            .durability(600)
            .repairable(ItemTags.IRON_TOOL_MATERIALS).equippableUnswappable(EquipmentSlot.OFFHAND)
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
    public static final ItemDelegate TENGU_CAMERA = registerItem("tengu_camera", props -> new TenguCameraItem(props.stacksTo(1).durability(250).repairable(ItemTags.REPAIRS_IRON_ARMOR)), new Item.Properties());
    public static final ItemDelegate HIMEKAIDOU_HATATES_PHONE = registerItem("himekaidou_hatates_phone", props -> new HimekaidouHatatesPhone(props.component(RDDataComponents.FOV.value(), 75).stacksTo(1).durability(250).repairable(ItemTags.REPAIRS_IRON_ARMOR)), new Item.Properties());
    public static final ItemDelegate BAD_APPLE = registerItem("bad_apple", props -> new BadAppleItem(props.food(Foods.GOLDEN_APPLE).stacksTo(16).rarity(Rarity.EPIC)), new Item.Properties());
    public static final ItemDelegate SCARECROW = registerItem("scarecrow", props -> new ScarecrowItem(props), new Item.Properties());
    public static final ItemDelegate EXORCISM_PAPER = registerItem("exorcism_paper", props -> new ExorcismPaperItem(props.stacksTo(16)), new Item.Properties());
    public static final ItemDelegate SPELLCARD = registerItem("spellcard", props -> new SpellcardItem(props.stacksTo(1).durability(50)), new Item.Properties());
    public static final ItemDelegate SATORI_EYE = registerItem("satori_eye", props -> new SatoriEye(props.stacksTo(1)), new Item.Properties());
    public static final ItemDelegate WEAPON_OF_THE_MOON = registerItem("weapon_of_the_moon", props -> new WeaponOfTheMoon(props.stacksTo(1)), new Item.Properties());
    public static final ItemDelegate FAST_RECIPE_BOOK = registerItem("fast_book_item", FastRecipeBook::new, new Item.Properties());
    public static final ItemDelegate LOW_GRAVITY_BOOT = registerItem("low_gravity_boot", LowGravityBootItem::new, new Item.Properties());
    public static final ItemDelegate CROWN_OF_THE_UNDERWORLD = registerItem("crown_of_the_underworld", CrownOfTheUnderworldItem::new, new Item.Properties());
    public static final ItemDelegate SUNFLOWER = registerItem("sunflower", props -> new Sunflower(props.stacksTo(1).durability(256).repairable(RDItemTags.POWER_BLOCK)), new Item.Properties());
    public static final ItemDelegate CUSTOM_SKIN_SELECTOR = registerSimpleItem("custom_skin_selector", props -> new CustomSkinSelectorItem(props.stacksTo(1)), new Item.Properties());

    // 武器
    public static final ItemDelegate HAKUREI_CANE = registerItem("hakurei_cane", props -> new HakureiCane(1f, -2.4f, props), new Item.Properties());
    public static final ItemDelegate BAGUA_FURNACE = registerItem("bagua_furnace", props -> new BaguaFurnace(props.stacksTo(1).durability(200).component(DataComponents.CONSUMABLE, new Consumable(5, ItemUseAnimation.BLOCK, Holder.direct(SoundEvents.FIRECHARGE_USE), false, new ArrayList<>())).repairable(Items.NETHERITE_INGOT)), new Item.Properties());
    public static final ItemDelegate WIND_BLESSING_CANE = registerItem("wind_blessing_cane", props -> new WindBlessingCane(1f, -2.4f, props), new Item.Properties());
    public static final ItemDelegate MAGIC_BROOM = registerItem("magic_broom", props -> new MagicBroom(1f, -2.4f, props), new Item.Properties());
    public static final ItemDelegate GUNGNIR = registerItem("gungnir", props -> new Gungnir(props), new Item.Properties());
    public static final ItemDelegate LEVATIN = registerItem("levatin", props -> new Levatin(1f, -2.4f, props), new Item.Properties());
    public static final ItemDelegate ROKANKEN = registerItem("rokanken", props -> new Rokanken(2f, 0.5f - 2.4f, props.attributes(ItemAttributeModifiers.builder().add(Attributes.ENTITY_INTERACTION_RANGE, new AttributeModifier(Identifier.withDefaultNamespace("weapon_range"), 4, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.HAND).add(Attributes.BLOCK_INTERACTION_RANGE, new AttributeModifier(Identifier.withDefaultNamespace("block_range"), 3, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.HAND).build())), new Item.Properties());
    public static final ItemDelegate HAKUROKEN = registerItem("hakuroken", props -> new Hakuroken(2f, 1f - 2.4f, props), new Item.Properties());
    public static final ItemDelegate PAPILIO_PATTERN_FAN = registerItem("papilio_pattern_fan", props -> new PapilioPatternFan(1f - 4f, 1f - 2.4f, props), new Item.Properties());
    public static final ItemDelegate HORAI_DAMA_NO_EDA = registerItem("horai-dama_no_eda", props -> new HoraiDamaNoEdaItem(0, 0, props), new Item.Properties());
    public static final ItemDelegate YUKA_FLOWER_UMBRELLA = registerItem("yuka_flower_umbrella", props -> new YukaFlowerUmbrella(1f, -2.4f, props), new Item.Properties());
    public static final ItemDelegate MAPLE_LEAF_FAN = registerItem("maple_leaf_fan", props -> new MapleLeafFan(1f, -2.4f, props), new Item.Properties());
    public static final ItemDelegate IBUKIHO = registerItem("ibukiho", props -> new Ibukiho(1f, -2.4f, props.useCooldown(130f).fireResistant().food(new FoodProperties.Builder().alwaysEdible().saturationModifier(-4f).build(), Consumable.builder().consumeSeconds(3f).soundAfterConsume(SoundEvents.GENERIC_DRINK).build())), new Item.Properties());
    public static final ItemDelegate SWORD_OF_HISOU = registerItem("sword_of_hisou", props -> new SwordOfHisou(2f, -2.4f, props), new Item.Properties());
    public static final ItemDelegate MANPOZUCHI = registerItem("manpozuchi", props -> new ManpozuchiItem(3.5f, -2.5f, props), new Item.Properties());
    public static final ItemDelegate NUE_TRIDENT = registerItem("nue_trident", props -> new NueTrident(3.5f, -2.8f, props), new Item.Properties());
    public static final ItemDelegate TRUMPET_GUN = registerItem("trumpet_gun", props -> new TrumpetGun(props.stacksTo(1).durability(150).repairable(Items.GOLD_BLOCK)), new Item.Properties());
    public static final ItemDelegate TREASURE_HUNTING_ROD = registerItem("treasure_hunting_rod", props -> new TreasureHuntingRod(2f, -2.8f, props), new Item.Properties());
    public static final ItemDelegate DEATH_SCYTHE = registerItem("death_scythe", props -> new DeathScytheItem(1f, -2.8f, props), new Item.Properties());
    public static final ItemDelegate VIOLIN = registerItem("violin", props -> new MusicalInstrumentItem(props.stacksTo(1).equippable(EquipmentSlot.HEAD).component(RDDataComponents.NOTE_TYPE.value(), NoteBlockInstrument.FLUTE)), new Item.Properties());
    public static final ItemDelegate KEYBOARD = registerItem("keyboard", props -> new MusicalInstrumentItem(props.stacksTo(1).equippable(EquipmentSlot.HEAD).component(RDDataComponents.NOTE_TYPE.value(), NoteBlockInstrument.PLING)), new Item.Properties());
    public static final ItemDelegate TRUMPET = registerItem("trumpet", props -> new MusicalInstrumentItem(props.stacksTo(1).equippable(EquipmentSlot.HEAD).component(RDDataComponents.NOTE_TYPE.value(), NoteBlockInstrument.TRUMPET)), new Item.Properties());
    public static final ItemDelegate IRON_BAR = registerItem("iron_bar", props -> new IronBarItem(props.stacksTo(1).component(DataComponents.TOOL, new Tool(List.of(), 1.0F, 2, false)).durability(400).repairable(RDItemTags.IRON_BAR_MATERIALS).attributes(ItemAttributeModifiers.builder().add(Attributes.ATTACK_DAMAGE, new AttributeModifier(Item.BASE_ATTACK_DAMAGE_ID, 8, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND).add(Attributes.ATTACK_SPEED, new AttributeModifier(Item.BASE_ATTACK_SPEED_ID, 1 - 1 - 3.3, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND).build()).enchantable(15)), new Item.Properties());

    // 银装备
    public static final ItemDelegate RAW_SILVER = registerItem("raw_silver", props -> new Item(props), new Item.Properties());
    public static final ItemDelegate SILVER_INGOT = registerItem("silver_ingot", props -> new Item(props), new Item.Properties());
    public static final ItemDelegate SILVER_NUGGET = registerItem("silver_nugget", props -> new Item(props), new Item.Properties());
    public static final ItemDelegate SILVER_SWORD = registerItem("silver_sword", props -> new SwordItem(SilverMaterial.INSTANCE, 3.0f, -2.4f, props.component(RDDataComponents.SILVER_ITEM.value(), Unit.INSTANCE)), new Item.Properties());
    public static final ItemDelegate SILVER_AXE = registerItem("silver_axe", props -> new AxeItem(SilverMaterial.INSTANCE, 6.0f, -2.8f, props.component(RDDataComponents.SILVER_ITEM.value(), Unit.INSTANCE)), new Item.Properties());
    public static final ItemDelegate SILVER_PICKAXE = registerItem("silver_pickaxe", props -> new PickaxeItem(SilverMaterial.INSTANCE, 1.0f, -2.8f, props.component(RDDataComponents.SILVER_ITEM.value(), Unit.INSTANCE)), new Item.Properties());
    public static final ItemDelegate SILVER_SHOVEL = registerItem("silver_shovel", props -> new ShovelItem(SilverMaterial.INSTANCE, 1.5f, -3.0f, props.component(RDDataComponents.SILVER_ITEM.value(), Unit.INSTANCE)), new Item.Properties());
    public static final ItemDelegate SILVER_HOE = registerItem("silver_hoe", props -> new HoeItem(SilverMaterial.INSTANCE, -2.0f, -1.0f, props.component(RDDataComponents.SILVER_ITEM.value(), Unit.INSTANCE)), new Item.Properties());
    public static final ItemDelegate SILVER_SPEAR = registerItem("silver_spear", props -> new CustomSpear(props.spear(SilverMaterial.INSTANCE, 0.95F, 0.95F, 0.6F, 2.5F, 8.0F, 6.75F, 5.1F, 11.25F, 4.6F)), new Item.Properties());
    public static final ItemDelegate SILVER_HELMET = registerItem("silver_helmet", props -> new ArmorItem(SilverArmorMaterial.INSTANCE, ArmorType.HELMET, props.component(RDDataComponents.SILVER_ITEM.value(), Unit.INSTANCE).durability(ArmorType.HELMET.getDurability(SilverArmorMaterial.BASE_DURABILITY))), new Item.Properties());
    public static final ItemDelegate SILVER_CHESTPLATE = registerItem("silver_chestplate", props -> new ArmorItem(SilverArmorMaterial.INSTANCE, ArmorType.CHESTPLATE, props.component(RDDataComponents.SILVER_ITEM.value(), Unit.INSTANCE).durability(ArmorType.CHESTPLATE.getDurability(SilverArmorMaterial.BASE_DURABILITY))), new Item.Properties());
    public static final ItemDelegate SILVER_LEGGINGS = registerItem("silver_leggings", props -> new ArmorItem(SilverArmorMaterial.INSTANCE, ArmorType.LEGGINGS, props.component(RDDataComponents.SILVER_ITEM.value(), Unit.INSTANCE).durability(ArmorType.LEGGINGS.getDurability(SilverArmorMaterial.BASE_DURABILITY))), new Item.Properties());
    public static final ItemDelegate SILVER_BOOTS = registerItem("silver_boots", props -> new ArmorItem(SilverArmorMaterial.INSTANCE, ArmorType.BOOTS, props.component(RDDataComponents.SILVER_ITEM.value(), Unit.INSTANCE).durability(ArmorType.BOOTS.getDurability(SilverArmorMaterial.BASE_DURABILITY))), new Item.Properties());

    // 女仆装备
    public static final ItemDelegate KNIFE = registerItem("knife", props -> new Knife(0f, 0f, props.stacksTo(1).component(RDDataComponents.DANMAKU_PROPERTIES.value(), DanmakuProperties.ofDefault().withSpeed(1.4f).withScale(1.8f))), new Item.Properties());
    public static final ItemDelegate MAID_HAIRBAND = registerItem("maid_hairband", props -> new ArmorItem(MaidArmorMaterial.INSTANCE, ArmorType.HELMET, props.durability(ArmorType.HELMET.getDurability(MaidArmorMaterial.BASE_DURABILITY))), new Item.Properties());
    public static final ItemDelegate MAID_UPPER_SKIRT = registerItem("maid_upper_skirt", props -> new ArmorItem(MaidArmorMaterial.INSTANCE, ArmorType.CHESTPLATE, props.durability(ArmorType.CHESTPLATE.getDurability(MaidArmorMaterial.BASE_DURABILITY))), new Item.Properties());
    public static final ItemDelegate MAID_LOWER_SKIRT = registerItem("maid_lowerband", props -> new ArmorItem(MaidArmorMaterial.INSTANCE, ArmorType.LEGGINGS, props.durability(ArmorType.LEGGINGS.getDurability(MaidArmorMaterial.BASE_DURABILITY))), new Item.Properties());
    public static final ItemDelegate MAID_SHOE = registerItem("maid_shoe", props -> new ArmorItem(MaidArmorMaterial.INSTANCE, ArmorType.BOOTS, props.durability(ArmorType.BOOTS.getDurability(MaidArmorMaterial.BASE_DURABILITY))), new Item.Properties());

    // 魔法冰装备
    public static final ItemDelegate ICE_SCALES = registerItem("ice_scales", props -> new Item(props), new Item.Properties());
    public static final ItemDelegate MAGIC_ICE_SWORD = registerItem("magic_ice_sword", props -> new SwordItem(MagicIceMaterial.INSTANCE, 3.0f, -2.4f, props), new Item.Properties());
    public static final ItemDelegate MAGIC_ICE_AXE = registerItem("magic_ice_axe", props -> new AxeItem(MagicIceMaterial.INSTANCE, 6.0f, -2.8f, props), new Item.Properties());
    public static final ItemDelegate MAGIC_ICE_PICKAXE = registerItem("magic_ice_pickaxe", props -> new PickaxeItem(MagicIceMaterial.INSTANCE, 1.0f, -2.8f, props), new Item.Properties());
    public static final ItemDelegate MAGIC_ICE_SHOVEL = registerItem("magic_ice_shovel", props -> new ShovelItem(MagicIceMaterial.INSTANCE, 1.5f, -3.0f, props), new Item.Properties());
    public static final ItemDelegate MAGIC_ICE_HOE = registerItem("magic_ice_hoe", props -> new HoeItem(MagicIceMaterial.INSTANCE, -2.0f, -1.0f, props), new Item.Properties());
    public static final ItemDelegate MAGIC_ICE_SPEAR = registerItem("magic_ice_spear", props -> new CustomSpear(props.spear(MagicIceMaterial.INSTANCE, 0.95F, 0.95F, 0.6F, 2.5F, 8.0F, 6.75F, 5.1F, 11.25F, 4.6F)), new Item.Properties());
    public static final ItemDelegate MAGIC_ICE_HELMET = registerItem("magic_ice_helmet", props -> new ArmorItem(MagicIceArmorMaterial.INSTANCE, ArmorType.HELMET, props.durability(ArmorType.HELMET.getDurability(MagicIceArmorMaterial.BASE_DURABILITY))), new Item.Properties());
    public static final ItemDelegate MAGIC_ICE_CHESTPLATE = registerItem("magic_ice_chestplate", props -> new ArmorItem(MagicIceArmorMaterial.INSTANCE, ArmorType.CHESTPLATE, props.durability(ArmorType.CHESTPLATE.getDurability(MagicIceArmorMaterial.BASE_DURABILITY))), new Item.Properties());
    public static final ItemDelegate MAGIC_ICE_LEGGINGS = registerItem("magic_ice_leggings", props -> new ArmorItem(MagicIceArmorMaterial.INSTANCE, ArmorType.LEGGINGS, props.durability(ArmorType.LEGGINGS.getDurability(MagicIceArmorMaterial.BASE_DURABILITY))), new Item.Properties());
    public static final ItemDelegate MAGIC_ICE_BOOTS = registerItem("magic_ice_boots", props -> new ArmorItem(MagicIceArmorMaterial.INSTANCE, ArmorType.BOOTS, props.durability(ArmorType.BOOTS.getDurability(MagicIceArmorMaterial.BASE_DURABILITY))), new Item.Properties());

    // 梦境装备
    public static final ItemDelegate DREAM_SWORD = registerItem("dream_sword", props -> new SwordItem(DreamMaterial.INSTANCE, 3.0f, -2.4f, props), new Item.Properties());
    public static final ItemDelegate DREAM_AXE = registerItem("dream_axe", props -> new AxeItem(DreamMaterial.INSTANCE, 6.0f, -2.8f, props), new Item.Properties());
    public static final ItemDelegate DREAM_PICKAXE = registerItem("dream_pickaxe", props -> new PickaxeItem(DreamMaterial.INSTANCE, 1.0f, -2.8f, props), new Item.Properties());
    public static final ItemDelegate DREAM_SHOVEL = registerItem("dream_shovel", props -> new ShovelItem(DreamMaterial.INSTANCE, 1.5f, -3.0f, props), new Item.Properties());
    public static final ItemDelegate DREAM_HOE = registerItem("dream_hoe", props -> new HoeItem(DreamMaterial.INSTANCE, -2.0f, -1.0f, props), new Item.Properties());
    public static final ItemDelegate DREAM_SPEAR = registerItem("dream_spear", props -> new CustomSpear(props.spear(DreamMaterial.INSTANCE, 0.95F, 0.95F, 0.6F, 2.5F, 8.0F, 6.75F, 5.1F, 11.25F, 4.6F)), new Item.Properties());
    public static final ItemDelegate DREAM_HELMET = registerItem("dream_helmet", props -> new DreamArmorItem(ArmorType.HELMET, props.durability(ArmorType.HELMET.getDurability(DreamArmorMaterial.BASE_DURABILITY))), new Item.Properties());
    public static final ItemDelegate DREAM_CHESTPLATE = registerItem("dream_chestplate", props -> new DreamArmorItem(ArmorType.CHESTPLATE, props.durability(ArmorType.CHESTPLATE.getDurability(DreamArmorMaterial.BASE_DURABILITY))), new Item.Properties());
    public static final ItemDelegate DREAM_LEGGINGS = registerItem("dream_leggings", props -> new DreamArmorItem(ArmorType.LEGGINGS, props.durability(ArmorType.LEGGINGS.getDurability(DreamArmorMaterial.BASE_DURABILITY))), new Item.Properties());
    public static final ItemDelegate DREAM_BOOTS = registerItem("dream_boots", props -> new DreamArmorItem(ArmorType.BOOTS, props.durability(ArmorType.BOOTS.getDurability(DreamArmorMaterial.BASE_DURABILITY))), new Item.Properties());

    // 防水衣
    public static final ItemDelegate WATERPROOF_LEATHER = registerItem("waterproof_leather", props -> new Item(props), new Item.Properties());
    public static final ItemDelegate WATER_PROOF_HAT = registerItem("waterproof_hat", props -> new WaterproofArmor(ArmorType.HELMET, props.component(DataComponents.DYED_COLOR, new DyedItemColor(0xFF4AA9FF)).durability(ArmorType.HELMET.getDurability(WaterproofArmorMaterial.BASE_DURABILITY))), new Item.Properties());
    public static final ItemDelegate WATER_PROOF_CLOTHING = registerItem("waterproof_clothing", props -> new WaterproofArmor(ArmorType.CHESTPLATE, props.component(DataComponents.DYED_COLOR, new DyedItemColor(0xFF4AA9FF)).durability(ArmorType.CHESTPLATE.getDurability(WaterproofArmorMaterial.BASE_DURABILITY))), new Item.Properties());
    public static final ItemDelegate WATER_PROOF_LEGGINGS = registerItem("waterproof_leggings", props -> new WaterproofArmor(ArmorType.LEGGINGS, props.component(DataComponents.DYED_COLOR, new DyedItemColor(0xFF4AA9FF)).durability(ArmorType.LEGGINGS.getDurability(WaterproofArmorMaterial.BASE_DURABILITY))), new Item.Properties());
    public static final ItemDelegate WATER_PROOF_BOOTS = registerItem("waterproof_boots", props -> new WaterproofArmor(ArmorType.BOOTS, props.component(DataComponents.DYED_COLOR, new DyedItemColor(0xFF4AA9FF)).durability(ArmorType.BOOTS.getDurability(WaterproofArmorMaterial.BASE_DURABILITY))), new Item.Properties());

    // 模板
    public static final ItemDelegate DANMAKU_SHAPE_CREATOR = registerItem("danmaku_recipe_creator", props -> new DanmakuShapeCreatorItem(props), new Item.Properties());
    public static final ItemDelegate SPELL_CARD_TEMPLATE = registerItem("spell_card_template", props -> new SpellCardTemplateItem(props), new Item.Properties());
    public static final ItemDelegate ROLE_CARD = registerItem("role_card", props -> new RoleCardItem(props.stacksTo(1).component(DataComponents.TOOLTIP_DISPLAY, TooltipDisplay.DEFAULT.withHidden(DataComponents.DYED_COLOR, true)).component(DataComponents.DYED_COLOR, new DyedItemColor(RoleCard.DEFAULT_COLOR.intValue()))), new Item.Properties());
    public static final ItemDelegate ROLE_ARCHIVE = registerItem("role_archive", props -> new RoleFollowerArchiveItem(props.stacksTo(1)), new Item.Properties());

    // 唱片
    public static final ItemDelegate HR01_01 = registerAlbum("hr01_01", props -> new AlbumItem(props.jukeboxPlayable(JukeboxSongInit.HR01_01.getJukeboxSongKey())), new Item.Properties());
    public static final ItemDelegate HR02_08 = registerAlbum("hr02_08", props -> new AlbumItem(props.jukeboxPlayable(JukeboxSongInit.HR02_08.getJukeboxSongKey())), new Item.Properties());
    public static final ItemDelegate HR03_01 = registerAlbum("hr03_01", props -> new AlbumItem(props.jukeboxPlayable(JukeboxSongInit.HR03_01.getJukeboxSongKey())), new Item.Properties());
    public static final ItemDelegate MELODIC_TASTE_NIGHTMARE_BEFORE_CROSSROADS = registerAlbum("melodic-taste-nightmare-before-crossroads", props -> new AlbumItem(props.jukeboxPlayable(JukeboxSongInit.MELODIC_TASTE_NIGHTMARE_BEFORE_CROSSROADS.getJukeboxSongKey())), new Item.Properties());
    public static final ItemDelegate YV_FLOWER_CLOCK_AND_DREAMS = registerAlbum("yv_flower_clock_and_dreams", props -> new AlbumItem(props.jukeboxPlayable(JukeboxSongInit.YV_FLOWER_CLOCK_AND_DREAMS.getJukeboxSongKey())), new Item.Properties());
    public static final ItemDelegate GLOWING_NEEDLES_LITTLE_PEOPLE = registerAlbum("glowing_needles_little_people", props -> new AlbumItem(props.jukeboxPlayable(JukeboxSongInit.GLOWING_NEEDLES_LITTLE_PEOPLE.getJukeboxSongKey())), new Item.Properties());
    public static final ItemDelegate COOKIE = registerAlbum("cookie", props -> new AlbumItem(props.jukeboxPlayable(JukeboxSongInit.COOKIE.getJukeboxSongKey())), new Item.Properties());
    public static final ItemDelegate BADAPPLE = registerAlbum("bad-apple", props -> new AlbumItem(props.jukeboxPlayable(JukeboxSongInit.BAD_APPLE.getJukeboxSongKey())), new Item.Properties());


    @SuppressWarnings({"Convert2MethodRef"})
    public static void initialize() {

    }

    public static ItemDelegate registerItem(String id, Function<Item.Properties, Item> factory, Item.Properties settings) {
        ItemDelegate item = registerSimpleItem(id, factory, settings);
        CREATIVE_TAB_ITEM_LIST.add(item);
        return item;
    }

    public static ItemDelegate registerCreativeTabIcon(String id, Function<Item.Properties, Item> factory, Item.Properties settings) {
        return registerSimpleItem(id, factory, settings);
    }

    public static ItemDelegate registerAlbum(String id, Function<Item.Properties, Item> factory, Item.Properties settings) {
        return registerSimpleItem(id, factory, settings);
    }

    public static ItemDelegate registerSimpleItem(String name, Function<Item.Properties, Item> factory, Item.Properties settings) {
        RegistrySupplier<Item> item = ReverieDreamsRegistries.ITEM.register(name, () -> factory.apply(settings.setId(keyOf(name))));
        ItemDelegate itemDelegate = ItemDelegate.of(item);
        ReverieDreams.COMMON_LATE_INIT.add(() -> ItemTypeGroup.join(itemDelegate.asItem()));
        LATE_POLYMERIFY_ITEM_LIST.add(item);
        return itemDelegate;
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

    public static List<ItemDelegate> getItemView() {
        return List.copyOf(CREATIVE_TAB_ITEM_LIST);
    }

}
