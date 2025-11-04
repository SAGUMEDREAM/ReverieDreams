package cc.thonly.reverie_dreams.item;

import cc.thonly.polymer.PolymerItemHelper;
import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.armor.*;
import cc.thonly.reverie_dreams.component.BattleStickRecorder;
import cc.thonly.reverie_dreams.component.DanmakuProperties;
import cc.thonly.reverie_dreams.component.ModDataComponentTypes;
import cc.thonly.reverie_dreams.data.ModTags;
import cc.thonly.reverie_dreams.item.armor.DreamArmorItem;
import cc.thonly.reverie_dreams.item.armor.EarphoneItem;
import cc.thonly.reverie_dreams.item.armor.KoishiHatItem;
import cc.thonly.reverie_dreams.item.base.*;
import cc.thonly.reverie_dreams.item.builder.RoleCard;
import cc.thonly.reverie_dreams.item.danmaku.SpellcardItem;
import cc.thonly.reverie_dreams.item.material.DreamMaterial;
import cc.thonly.reverie_dreams.item.material.MagicIceMaterial;
import cc.thonly.reverie_dreams.item.material.SilverMaterial;
import cc.thonly.reverie_dreams.item.prop.*;
import cc.thonly.reverie_dreams.item.prop.debug.BattleStickItem;
import cc.thonly.reverie_dreams.item.prop.debug.OwnerStickItem;
import cc.thonly.reverie_dreams.item.template.DanmakuShapeCreatorItem;
import cc.thonly.reverie_dreams.item.template.RoleCardItem;
import cc.thonly.reverie_dreams.item.template.RoleFollowerArchiveItem;
import cc.thonly.reverie_dreams.item.template.SpellCardTemplateItem;
import cc.thonly.reverie_dreams.item.weapon.*;
import cc.thonly.reverie_dreams.sound.JukeboxSongInit;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Unit;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.food.Foods;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.ShovelItem;
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

public class ModItems {
    public static final List<Item> ITEM_LIST = new ArrayList<>();

    public static final Supplier<ItemStack> NOT_COMPLETED = () -> {
        ItemStack itemStack = new ItemStack(Items.BARRIER);
        itemStack.set(DataComponents.ITEM_NAME, Component.literal("§cThis page is not completed"));
        return itemStack;
    };

    // 调试
    public static final Item BATTLE_STICK = registerSimpleItem("battle_stick", BattleStickItem::new, new Item.Properties().
            stacksTo(1)
            .component(ModDataComponentTypes.BATTLE_STICK_RECORDER, BattleStickRecorder.empty()));
    public static final Item OWNER_STICK = registerSimpleItem("owner_stick", OwnerStickItem::new, new Item.Properties()
            .stacksTo(1));

    // 图标
    public static final Item ICON = registerCreativeTabIcon("icon", Item::new, new Item.Properties());
    public static final Item FUMO_ICON = registerCreativeTabIcon("fumo_icon", Item::new, new Item.Properties());
    public static final Item ROLE_ICON = registerCreativeTabIcon("role_icon", Item::new, new Item.Properties());
    public static final Item SPAWN_EGG = registerCreativeTabIcon("spawn_egg", Item::new, new Item.Properties()
            .component(DataComponents.DYED_COLOR, SpawnEggItem.DEFAULT_COLOR));
    public static final Item DANMAKU = registerCreativeTabIcon("danmaku", Item::new, new Item.Properties());

    // 材料
    public static final Item POINT = registerItem("point", Item::new, new Item.Properties());
    public static final Item POWER = registerItem("power", Item::new, new Item.Properties());
    public static final Item DANMAKU_CORE = registerItem("danmaku_core", Item::new, new Item.Properties()
            .component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true));
    public static final Item UPGRADED_HEALTH_FRAGMENT = registerItem("upgraded_health_fragment", Item::new, new Item.Properties());
    public static final Item BOMB_FRAGMENT = registerItem("bomb_fragment", Item::new, new Item.Properties());
    public static final Item RED_ORB = registerItem("red_orb", Item::new, new Item.Properties());
    public static final Item BLUE_ORB = registerItem("blue_orb", Item::new, new Item.Properties());
    public static final Item YELLOW_ORB = registerItem("yellow_orb", Item::new, new Item.Properties());
    public static final Item GREEN_ORB = registerItem("green_orb", Item::new, new Item.Properties());
    public static final Item PURPLE_ORB = registerItem("purple_orb", Item::new, new Item.Properties());
    public static final Item YIN_YANG_ORB = registerItem("yin-yang_orb", Item::new, new Item.Properties());
    public static final Item SPEED_FEATHER = registerItem("speed_feather", Item::new, new Item.Properties()
            .component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true));
    public static final Item DREAM_CRYSTAL_FRAGMENT = registerItem("dream_crystal_fragment", Item::new, new Item.Properties().component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true));
    public static final Item COPPER_COIN = registerItem("copper_coin", Item::new, new Item.Properties().stacksTo(96));
    public static final Item SILVER_COIN = registerItem("silver_coin", Item::new, new Item.Properties().stacksTo(96));
    public static final Item GOLD_COIN = registerItem("gold_coin", Item::new, new Item.Properties().stacksTo(96));

    // 道具
    public static final Item TOUHOU_HELPER = registerItem("touhou_helper", TouhouHelperItem::new, new Item.Properties()
            .stacksTo(1)
            .rarity(Rarity.EPIC)
            .component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true));
    public static final Item UPGRADED_HEALTH = registerItem("upgraded_health", UpgradedHealthItem::new, new Item.Properties());
    public static final Item BOMB = registerItem("bomb", BombItem::new, new Item.Properties().useCooldown(2.0f));
    public static final Item CROSSING_CHISEL = registerItem("crossing_chisel", CrossingChisel::new, new Item.Properties().useCooldown(3.0f)
            .component(ModDataComponentTypes.MAX_DISTANCE, CrossingChisel.DEFAULT_VALUE)
            .stacksTo(1)
            .durability(150));
    public static final Item GAP_BALL = registerItem("gap_ball", GapBall::new, new Item.Properties().stacksTo(1));
    public static final Item TIME_STOP_CLOCK = registerItem("time_stop_clock", TimeStopClock::new, new Item.Properties().stacksTo(1).durability(200).repairable(ItemTags.GOLD_TOOL_MATERIALS));
    public static final Item EARPHONE = registerItem("earphone", EarphoneItem::new, new Item.Properties().durability(ArmorType.HELMET.getDurability(EarphoneArmorMaterial.BASE_DURABILITY)));
    public static final Item KOISHI_HAT = registerItem("koishi_hat", KoishiHatItem::new, new Item.Properties().durability(ArmorType.HELMET.getDurability(KoishiHatArmorMaterial.BASE_DURABILITY)));
    public static final Item FUMO_LICENSE = registerItem("fumo_license", FumoLicenseItem::new, new Item.Properties());
    public static final Item CURSED_DECOY_DOLl = registerItem("cursed_decoy_doll", CursedDecoyDollItem::new, new Item.Properties());
    public static final Item VAISRAVANAS_PAGODA = registerItem("vaisravanas_pagoda", VaisravanasPagodaItem::new, new Item.Properties().stacksTo(1).durability(250).repairable(ModTags.ItemTypeTag.VAISRAVANAS_PAGODA));
    public static final Item DREAM_PILLOW = registerItem("dream_pillow", DreamPillowItem::new, new Item.Properties().durability(4));
    public static final Item TENGU_SHIELD = registerItem("tengu_shield", TenguShieldItem::new, TenguShieldItem.createItemSettings());
    public static final Item TENGU_CAMERA = registerItem("tengu_camera", TenguCameraItem::new, new Item.Properties().stacksTo(1).durability(250).repairable(ItemTags.REPAIRS_IRON_ARMOR));
    public static final Item BAD_APPLE = registerItem("bad_apple", BadAppleItem::new, new Item.Properties().food(Foods.GOLDEN_APPLE).stacksTo(16).rarity(Rarity.EPIC));
    public static final Item EXORCISM_PAPER = registerItem("exorcism_paper", ExorcismPaperItem::new, new Item.Properties().stacksTo(16));
    public static final Item SPELLCARD = registerItem("spellcard", SpellcardItem::new, new Item.Properties().stacksTo(1).durability(50));

    // 武器
    public static final Item HAKUREI_CANE = registerItem("hakurei_cane", (settings) -> new HakureiCane(1f, -2.4f, settings), new Item.Properties());
    public static final Item BAGUA_FURNACE = registerItem("bagua_furnace", BaguaFurnace::new, new Item.Properties().stacksTo(1).durability(200).component(DataComponents.CONSUMABLE, new Consumable(5, ItemUseAnimation.BLOCK, Holder.direct(SoundEvents.FIRECHARGE_USE), false, new ArrayList<>())).repairable(Items.NETHERITE_INGOT));
    public static final Item WIND_BLESSING_CANE = registerItem("wind_blessing_cane", (settings) -> new WindBlessingCane(1f, -2.4f, settings), new Item.Properties());
    public static final Item MAGIC_BROOM = registerItem("magic_broom", (settings) -> new MagicBroom(1f, -2.4f, settings), new Item.Properties());
    public static final Item GUNGNIR = registerItem("gungnir", (settings) -> new Gungnir(1f, -2.4f, settings), new Item.Properties());
    public static final Item LEVATIN = registerItem("levatin", (settings) -> new Levatin(1f, -2.4f, settings), new Item.Properties());
    public static final Item ROKANKEN = registerItem("rokanken", (settings) -> new Rokanken(2f, 0.5f - 2.4f, settings), new Item.Properties());
    public static final Item HAKUROKEN = registerItem("hakuroken", (settings) -> new Hakuroken(2f, 1f - 2.4f, settings), new Item.Properties());
    public static final Item PAPILIO_PATTERN_FAN = registerItem("papilio_pattern_fan", (settings) -> new PapilioPatternFan(1f - 4f, 1f - 2.4f, settings), new Item.Properties());
    public static final Item HORAI_DAMA_NO_EDA = registerItem("horai-dama_no_eda", (settings) -> new HoraiDamaNoEdaItem(0, 0, settings), new Item.Properties());
    public static final Item MAPLE_LEAF_FAN = registerItem("maple_leaf_fan", (settings) -> new MapleLeafFan(1f, -2.4f, settings), new Item.Properties());
    public static final Item IBUKIHO = registerItem("ibukiho", (settings) -> new Ibukiho(1f, -2.4f, settings), new Item.Properties()
            .useCooldown(130f)
            .fireResistant()
            .food(new FoodProperties.Builder()
                            .alwaysEdible()
                            .saturationModifier(-4f)
                            .build(),
                    Consumable.builder()
                            .consumeSeconds(3f)
                            .soundAfterConsume(SoundEvents.GENERIC_DRINK)
                            .build()
            ));
    public static final Item SWORD_OF_HISOU = registerItem("sword_of_hisou", (settings) -> new SwordOfHisou(1f, -2.4f, settings), new Item.Properties());
    public static final Item MANPOZUCHI = registerItem("manpozuchi", (settings) -> new ManpozuchiItem(3.5f, -2.5f, settings), new Item.Properties());
    public static final Item NUE_TRIDENT = registerItem("nue_trident", (settings) -> new NueTrident(3.5f, -2.8f, settings), new Item.Properties());
    public static final Item TRUMPET_GUN = registerItem("trumpet_gun", TrumpetGun::new, new Item.Properties().stacksTo(1).durability(150).repairable(Items.GOLD_BLOCK));
    public static final Item TREASURE_HUNTING_ROD = registerItem("treasure_hunting_rod", (settings) -> new TreasureHuntingRod(2f, -2.8f, settings), new Item.Properties());
    public static final Item DEATH_SCYTHE = registerItem("death_scythe", (settings) -> new DeathScytheItem(1f, -2.8f, settings), new Item.Properties());
    public static final Item VIOLIN = registerItem("violin", MusicalInstrumentItem::new, new Item.Properties()
            .stacksTo(1)
            .equipmentSlot((livingEntity, stack) -> EquipmentSlot.HEAD)
            .component(ModDataComponentTypes.NOTE_TYPE, NoteBlockInstrument.FLUTE));
    public static final Item KEYBOARD = registerItem("keyboard", MusicalInstrumentItem::new, new Item.Properties()
            .stacksTo(1)
            .equipmentSlot((livingEntity, stack) -> EquipmentSlot.HEAD)
            .component(ModDataComponentTypes.NOTE_TYPE, NoteBlockInstrument.PLING));
    public static final Item TRUMPET = registerItem("trumpet", MusicalInstrumentItem::new, new Item.Properties()
            .stacksTo(1)
            .equipmentSlot((livingEntity, stack) -> EquipmentSlot.HEAD)
            .component(ModDataComponentTypes.NOTE_TYPE, NoteBlockInstrument.DIDGERIDOO));

    // 银装备
    public static final Item RAW_SILVER = registerItem("raw_silver", Item::new, new Item.Properties());
    public static final Item SILVER_INGOT = registerItem("silver_ingot", Item::new, new Item.Properties());
    public static final Item SILVER_NUGGET = registerItem("silver_nugget", Item::new, new Item.Properties());
    public static final Item SILVER_SWORD = registerItem("silver_sword", (settings) -> new SwordItem(SilverMaterial.INSTANCE, 3.0f, -2.4f, settings), new Item.Properties().component(ModDataComponentTypes.SILVER_ITEM, Unit.INSTANCE));
    public static final Item SILVER_AXE = registerItem("silver_axe", (settings) -> new AxeItem(SilverMaterial.INSTANCE, 6.0f, -2.8f, settings), new Item.Properties().component(ModDataComponentTypes.SILVER_ITEM, Unit.INSTANCE));
    public static final Item SILVER_PICKAXE = registerItem("silver_pickaxe", (settings) -> new PickaxeItem(SilverMaterial.INSTANCE, 1.0f, -2.8f, settings), new Item.Properties().component(ModDataComponentTypes.SILVER_ITEM, Unit.INSTANCE));
    public static final Item SILVER_SHOVEL = registerItem("silver_shovel", (settings) -> new ShovelItem(SilverMaterial.INSTANCE, 1.5f, -3.0f, settings), new Item.Properties().component(ModDataComponentTypes.SILVER_ITEM, Unit.INSTANCE));
    public static final Item SILVER_HOE = registerItem("silver_hoe", (settings) -> new HoeItem(SilverMaterial.INSTANCE, -2.0f, -1.0f, settings), new Item.Properties().component(ModDataComponentTypes.SILVER_ITEM, Unit.INSTANCE));
    public static final Item SILVER_HELMET = registerItem("silver_helmet", (settings) -> new ArmorItem(SilverArmorMaterial.INSTANCE, ArmorType.HELMET, settings), new Item.Properties().component(ModDataComponentTypes.SILVER_ITEM, Unit.INSTANCE).durability(ArmorType.HELMET.getDurability(SilverArmorMaterial.BASE_DURABILITY)));
    public static final Item SILVER_CHESTPLATE = registerItem("silver_chestplate", (settings) -> new ArmorItem(SilverArmorMaterial.INSTANCE, ArmorType.CHESTPLATE, settings), new Item.Properties().component(ModDataComponentTypes.SILVER_ITEM, Unit.INSTANCE).durability(ArmorType.CHESTPLATE.getDurability(SilverArmorMaterial.BASE_DURABILITY)));
    public static final Item SILVER_LEGGINGS = registerItem("silver_leggings", (settings) -> new ArmorItem(SilverArmorMaterial.INSTANCE, ArmorType.LEGGINGS, settings), new Item.Properties().component(ModDataComponentTypes.SILVER_ITEM, Unit.INSTANCE).durability(ArmorType.LEGGINGS.getDurability(SilverArmorMaterial.BASE_DURABILITY)));
    public static final Item SILVER_BOOTS = registerItem("silver_boots", (settings) -> new ArmorItem(SilverArmorMaterial.INSTANCE, ArmorType.BOOTS, settings), new Item.Properties().component(ModDataComponentTypes.SILVER_ITEM, Unit.INSTANCE).durability(ArmorType.BOOTS.getDurability(SilverArmorMaterial.BASE_DURABILITY)));

    // 女仆装备
    public static final Item KNIFE = registerItem("knife", (settings) -> new Knife(0f, 0f, settings), new Item.Properties().stacksTo(1)
            .component(ModDataComponentTypes.DANMAKU_PROPERTIES, DanmakuProperties.ofDefault().withSpeed(0.5f).withScale(1.8f))
    );
    public static final Item MAID_HAIRBAND = registerItem("maid_hairband", (settings) -> new ArmorItem(MaidArmorMaterial.INSTANCE, ArmorType.HELMET, settings), new Item.Properties().durability(ArmorType.HELMET.getDurability(MaidArmorMaterial.BASE_DURABILITY)));
    public static final Item MAID_UPPER_SKIRT = registerItem("maid_upper_skirt", (settings) -> new ArmorItem(MaidArmorMaterial.INSTANCE, ArmorType.CHESTPLATE, settings), new Item.Properties().durability(ArmorType.CHESTPLATE.getDurability(MaidArmorMaterial.BASE_DURABILITY)));
    public static final Item MAID_LOWER_SKIRT = registerItem("maid_lowerband", (settings) -> new ArmorItem(MaidArmorMaterial.INSTANCE, ArmorType.LEGGINGS, settings), new Item.Properties().durability(ArmorType.LEGGINGS.getDurability(MaidArmorMaterial.BASE_DURABILITY)));
    public static final Item MAID_SHOE = registerItem("maid_shoe", (settings) -> new ArmorItem(MaidArmorMaterial.INSTANCE, ArmorType.BOOTS, settings), new Item.Properties().durability(ArmorType.BOOTS.getDurability(MaidArmorMaterial.BASE_DURABILITY)));

    // 魔法冰装备
    public static final Item ICE_SCALES = registerItem("ice_scales", Item::new, new Item.Properties());
    public static final Item MAGIC_ICE_SWORD = registerItem("magic_ice_sword", (settings) -> new SwordItem(MagicIceMaterial.INSTANCE, 3.0f, -2.4f, settings), new Item.Properties());
    public static final Item MAGIC_ICE_AXE = registerItem("magic_ice_axe", (settings) -> new AxeItem(MagicIceMaterial.INSTANCE, 6.0f, -2.8f, settings), new Item.Properties());
    public static final Item MAGIC_ICE_PICKAXE = registerItem("magic_ice_pickaxe", (settings) -> new PickaxeItem(MagicIceMaterial.INSTANCE, 1.0f, -2.8f, settings), new Item.Properties());
    public static final Item MAGIC_ICE_SHOVEL = registerItem("magic_ice_shovel", (settings) -> new ShovelItem(MagicIceMaterial.INSTANCE, 1.5f, -3.0f, settings), new Item.Properties());
    public static final Item MAGIC_ICE_HOE = registerItem("magic_ice_hoe", (settings) -> new HoeItem(MagicIceMaterial.INSTANCE, -2.0f, -1.0f, settings), new Item.Properties());
    public static final Item MAGIC_ICE_HELMET = registerItem("magic_ice_helmet", (settings) -> new ArmorItem(MagicIceArmorMaterial.INSTANCE, ArmorType.HELMET, settings), new Item.Properties().durability(ArmorType.HELMET.getDurability(MagicIceArmorMaterial.BASE_DURABILITY)));
    public static final Item MAGIC_ICE_CHESTPLATE = registerItem("magic_ice_chestplate", (settings) -> new ArmorItem(MagicIceArmorMaterial.INSTANCE, ArmorType.CHESTPLATE, settings), new Item.Properties().durability(ArmorType.CHESTPLATE.getDurability(MagicIceArmorMaterial.BASE_DURABILITY)));
    public static final Item MAGIC_ICE_LEGGINGS = registerItem("magic_ice_leggings", (settings) -> new ArmorItem(MagicIceArmorMaterial.INSTANCE, ArmorType.LEGGINGS, settings), new Item.Properties().durability(ArmorType.LEGGINGS.getDurability(MagicIceArmorMaterial.BASE_DURABILITY)));
    public static final Item MAGIC_ICE_BOOTS = registerItem("magic_ice_boots", (settings) -> new ArmorItem(MagicIceArmorMaterial.INSTANCE, ArmorType.BOOTS, settings), new Item.Properties().durability(ArmorType.BOOTS.getDurability(MagicIceArmorMaterial.BASE_DURABILITY)));

    // 梦境装备
    public static final Item DREAM_SWORD = registerItem("dream_sword", (settings) -> new SwordItem(DreamMaterial.INSTANCE, 3.0f, -2.4f, settings), new Item.Properties());
    public static final Item DREAM_AXE = registerItem("dream_axe", (settings) -> new AxeItem(DreamMaterial.INSTANCE, 6.0f, -2.8f, settings), new Item.Properties());
    public static final Item DREAM_PICKAXE = registerItem("dream_pickaxe", (settings) -> new PickaxeItem(DreamMaterial.INSTANCE, 1.0f, -2.8f, settings), new Item.Properties());
    public static final Item DREAM_SHOVEL = registerItem("dream_shovel", (settings) -> new ShovelItem(DreamMaterial.INSTANCE, 1.5f, -3.0f, settings), new Item.Properties());
    public static final Item DREAM_HOE = registerItem("dream_hoe", (settings) -> new HoeItem(DreamMaterial.INSTANCE, -2.0f, -1.0f, settings), new Item.Properties());
    public static final Item DREAM_HELMET = registerItem("dream_helmet", (settings) -> new DreamArmorItem(ArmorType.HELMET, settings), new Item.Properties().durability(ArmorType.HELMET.getDurability(DreamArmorMaterial.BASE_DURABILITY)));
    public static final Item DREAM_CHESTPLATE = registerItem("dream_chestplate", (settings) -> new DreamArmorItem(ArmorType.CHESTPLATE, settings), new Item.Properties().durability( ArmorType.CHESTPLATE.getDurability(DreamArmorMaterial.BASE_DURABILITY)));
    public static final Item DREAM_LEGGINGS = registerItem("dream_leggings", (settings) -> new DreamArmorItem(ArmorType.LEGGINGS, settings), new Item.Properties().durability(ArmorType.LEGGINGS.getDurability(DreamArmorMaterial.BASE_DURABILITY)));
    public static final Item DREAM_BOOTS = registerItem("dream_boots", (settings) -> new DreamArmorItem(ArmorType.BOOTS, settings), new Item.Properties().durability(ArmorType.BOOTS.getDurability(DreamArmorMaterial.BASE_DURABILITY)));

    // 模板
    public static final Item DANMAKU_SHAPE_CREATOR = registerItem("danmaku_recipe_creator", DanmakuShapeCreatorItem::new, new Item.Properties());
    public static final Item SPELL_CARD_TEMPLATE = registerItem("spell_card_template", SpellCardTemplateItem::new, new Item.Properties());
    public static final Item ROLE_CARD = registerItem("role_card", RoleCardItem::new, new Item.Properties().stacksTo(1)
            .component(DataComponents.TOOLTIP_DISPLAY, TooltipDisplay.DEFAULT.withHidden(DataComponents.DYED_COLOR, true))
            .component(DataComponents.DYED_COLOR, new DyedItemColor(RoleCard.DEFAULT_COLOR.intValue())));
    public static final Item ROLE_ARCHIVE = registerItem("role_archive", RoleFollowerArchiveItem::new, new Item.Properties().stacksTo(1));

    // DISC
    public static final Item HR01_01 = registerAlbum("hr01_01", AlbumItem::new, new Item.Properties().jukeboxPlayable(JukeboxSongInit.HR01_01.getJukeboxSongRegistryKey()));
    public static final Item HR02_08 = registerAlbum("hr02_08", AlbumItem::new, new Item.Properties().jukeboxPlayable(JukeboxSongInit.HR02_08.getJukeboxSongRegistryKey()));
    public static final Item HR03_01 = registerAlbum("hr03_01", AlbumItem::new, new Item.Properties().jukeboxPlayable(JukeboxSongInit.HR03_01.getJukeboxSongRegistryKey()));
    public static final Item MELODIC_TASTE_NIGHTMARE_BEFORE_CROSSROADS = registerAlbum("melodic-taste-nightmare-before-crossroads", AlbumItem::new, new Item.Properties().jukeboxPlayable(JukeboxSongInit.MELODIC_TASTE_NIGHTMARE_BEFORE_CROSSROADS.getJukeboxSongRegistryKey()));
    public static final Item YV_FLOWER_CLOCK_AND_DREAMS = registerAlbum("yv_flower_clock_and_dreams", AlbumItem::new, new Item.Properties().jukeboxPlayable(JukeboxSongInit.YV_FLOWER_CLOCK_AND_DREAMS.getJukeboxSongRegistryKey()));
    public static final Item GLOWING_NEEDLES_LITTLE_PEOPLE = registerAlbum("glowing_needles_little_people", AlbumItem::new, new Item.Properties().jukeboxPlayable(JukeboxSongInit.GLOWING_NEEDLES_LITTLE_PEOPLE.getJukeboxSongRegistryKey()));
    public static final Item COOKIE = registerAlbum("cookie", AlbumItem::new, new Item.Properties().jukeboxPlayable(JukeboxSongInit.COOKIE.getJukeboxSongRegistryKey()));
    public static final Item BADAPPLE = registerAlbum("bad-apple", AlbumItem::new, new Item.Properties().jukeboxPlayable(JukeboxSongInit.BAD_APPLE.getJukeboxSongRegistryKey()));

    static {
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.OP_BLOCKS).register(itemGroup -> {
            itemGroup.accept(BATTLE_STICK);
            itemGroup.accept(OWNER_STICK);
        });
    }

    public static void registerItems() {
        List<Item> silverItems = new ArrayList<>(List.of(SILVER_SWORD, SILVER_AXE, SILVER_PICKAXE, SILVER_HOE, SILVER_HOE));
        ItemGroupEvents.modifyEntriesEvent(CreativeModeTabs.TOOLS_AND_UTILITIES).register(itemGroup -> {
            itemGroup.addAfter(Items.MUSIC_DISC_PIGSTEP, AlbumItem.ITEMS.stream().map(Item::getDefaultInstance).toList());
        });
        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (!world.isClientSide() && silverItems.contains(player.getItemInHand(hand).getItem())) {
                if (entity instanceof LivingEntity livingEntity) {
                    RegistryAccess registryManager = world.registryAccess();
                    Registry<EntityType<?>> registry = registryManager.lookupOrThrow(Registries.ENTITY_TYPE);
                    Optional<HolderSet.Named<EntityType<?>>> listOptional = registry.get(EntityTypeTags.UNDEAD);
                    if (listOptional.isPresent()) {
                        HolderSet.Named<EntityType<?>> list = listOptional.get();
                        boolean contains = list.contains(Holder.direct(livingEntity.getType()));
                        if (contains) {
                            livingEntity.hurtServer((ServerLevel) world, world.damageSources().magic(), 1.0F);
                        }
                    }
                }
            }
            return InteractionResult.PASS;
        });
    }

    public static Item registerItem(String id, Function<Item.Properties, Item> factory, Item.Properties settings) {
        Item item = registerSimpleItem(id, factory, settings);
        ITEM_LIST.add(item);
        return item;
    }

    public static Item registerCreativeTabIcon(String id, Function<Item.Properties, Item> factory, Item.Properties settings) {
        return registerSimpleItem(id, factory, settings);
    }

    public static Item registerAlbum(String id, Function<Item.Properties, Item> factory, Item.Properties settings) {
        return registerSimpleItem(id, factory, settings);
    }

    public static Item registerSimpleItem(ResourceLocation id, Function<Item.Properties, Item> factory, Item.Properties settings) {
        Item item = factory.apply(settings.setId(keyOf(id)));
        Registry.register(BuiltInRegistries.ITEM, id, item);
        ItemTypeGroup.join(item);
        PolymerItemHelper.registerOverlay(item);
        return item;
    }

    public static Item registerSimpleItem(String name, Function<Item.Properties, Item> factory, Item.Properties settings) {
        return registerSimpleItem(ReverieDreams.id(name), factory, settings);
    }

    public static ResourceKey<Item> keyOf(ResourceLocation id) {
        return ResourceKey.create(Registries.ITEM, id);
    }

    public static ResourceKey<Item> keyOf(String id) {
        return ResourceKey.create(Registries.ITEM, ReverieDreams.id(id));
    }

    public static ResourceKey<Item> keyOf(ResourceKey<Block> blockKey) {
        return ResourceKey.create(Registries.ITEM, blockKey.location());
    }

    public static List<Item> getItemView() {
        return List.copyOf(ITEM_LIST);
    }
}
