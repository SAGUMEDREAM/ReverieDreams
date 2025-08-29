package cc.thonly.reverie_dreams.item;

import cc.thonly.polymer.PolymerItemHelper;
import cc.thonly.reverie_dreams.Touhou;
import cc.thonly.reverie_dreams.armor.*;
import cc.thonly.reverie_dreams.component.BattleStickRecorder;
import cc.thonly.reverie_dreams.component.ModDataComponentTypes;
import cc.thonly.reverie_dreams.data.ModTags;
import cc.thonly.reverie_dreams.item.armor.EarphoneItem;
import cc.thonly.reverie_dreams.item.armor.KoishiHatItem;
import cc.thonly.reverie_dreams.item.base.SpawnEggItem;
import cc.thonly.reverie_dreams.item.base.*;
import cc.thonly.reverie_dreams.item.material.MagicIceMaterial;
import cc.thonly.reverie_dreams.item.material.SilverMaterial;
import cc.thonly.reverie_dreams.item.prop.*;
import cc.thonly.reverie_dreams.item.prop.debug.BattleStickItem;
import cc.thonly.reverie_dreams.item.prop.debug.OwnerStickItem;
import cc.thonly.reverie_dreams.item.weapon.*;
import cc.thonly.reverie_dreams.sound.JukeboxSongInit;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.block.Block;
import net.minecraft.block.enums.NoteBlockInstrument;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.*;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.*;
import net.minecraft.item.equipment.EquipmentType;
import net.minecraft.registry.*;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.tag.EntityTypeTags;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.util.Unit;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public class ModItems {
    public static final List<Item> ITEM_LIST = new ArrayList<>();

    public static final Supplier<ItemStack> NOT_COMPLETED = () -> {
        ItemStack itemStack = new ItemStack(Items.BARRIER);
        itemStack.set(DataComponentTypes.ITEM_NAME, Text.literal("§cThis page is not completed"));
        return itemStack;
    };

    // 调试
    public static final Item BATTLE_STICK = registerSimpleItem("battle_stick", BattleStickItem::new, new Item.Settings().
            maxCount(1)
            .component(ModDataComponentTypes.BATTLE_STICK_RECORDER, BattleStickRecorder.empty()));
    public static final Item OWNER_STICK = registerSimpleItem("owner_stick", OwnerStickItem::new, new Item.Settings()
            .maxCount(1));

    // 图标
    public static final Item ICON = registerCreativeTabIcon("icon", Item::new, new Item.Settings());
    public static final Item FUMO_ICON = registerCreativeTabIcon("fumo_icon", Item::new, new Item.Settings());
    public static final Item ROLE_ICON = registerCreativeTabIcon("role_icon", Item::new, new Item.Settings());
    public static final Item SPAWN_EGG = registerCreativeTabIcon("spawn_egg", Item::new, new Item.Settings()
            .component(DataComponentTypes.DYED_COLOR, SpawnEggItem.DEFAULT_COLOR));
    public static final Item DANMAKU = registerCreativeTabIcon("danmaku", Item::new, new Item.Settings());

    // 材料
    public static final Item POINT = registerItem("point", Item::new, new Item.Settings());
    public static final Item POWER = registerItem("power", Item::new, new Item.Settings());
    public static final Item UPGRADED_HEALTH_FRAGMENT = registerItem("upgraded_health_fragment", Item::new, new Item.Settings());
    public static final Item BOMB_FRAGMENT = registerItem("bomb_fragment", Item::new, new Item.Settings());
    public static final Item RED_ORB = registerItem("red_orb", Item::new, new Item.Settings());
    public static final Item BLUE_ORB = registerItem("blue_orb", Item::new, new Item.Settings());
    public static final Item YELLOW_ORB = registerItem("yellow_orb", Item::new, new Item.Settings());
    public static final Item GREEN_ORB = registerItem("green_orb", Item::new, new Item.Settings());
    public static final Item PURPLE_ORB = registerItem("purple_orb", Item::new, new Item.Settings());
    public static final Item YIN_YANG_ORB = registerItem("yin-yang_orb", Item::new, new Item.Settings());
    public static final Item SPEED_FEATHER = registerItem("speed_feather", Item::new, new Item.Settings()
            .component(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true));
    public static final Item DREAM_CRYSTAL_FRAGMENT = registerItem("dream_crystal_fragment", Item::new, new Item.Settings().component(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true));

    // 道具
    public static final Item TOUHOU_HELPER = registerItem("touhou_helper", TouhouHelperItem::new, new Item.Settings()
            .maxCount(1)
            .rarity(Rarity.EPIC)
            .component(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true));
    public static final Item UPGRADED_HEALTH = registerItem("upgraded_health", UpgradedHealthItem::new, new Item.Settings());
    public static final Item BOMB = registerItem("bomb", BombItem::new, new Item.Settings().useCooldown(2.0f));
    public static final Item CROSSING_CHISEL = registerItem("crossing_chisel", CrossingChisel::new, new Item.Settings().useCooldown(3.0f)
            .component(ModDataComponentTypes.MAX_DISTANCE, CrossingChisel.DEFAULT_VALUE)
            .maxCount(1)
            .maxDamage(150));
    public static final Item GAP_BALL = registerItem("gap_ball", GapBall::new, new Item.Settings().maxCount(1));
    public static final Item TIME_STOP_CLOCK = registerItem("time_stop_clock", TimeStopClock::new, new Item.Settings().maxCount(1).maxDamage(200).repairable(ItemTags.GOLD_TOOL_MATERIALS));
    public static final Item EARPHONE = registerItem("earphone", EarphoneItem::new, new Item.Settings().maxDamage(EquipmentType.HELMET.getMaxDamage(EarphoneArmorMaterial.BASE_DURABILITY)));
    public static final Item KOISHI_HAT = registerItem("koishi_hat", KoishiHatItem::new, new Item.Settings().maxDamage(EquipmentType.HELMET.getMaxDamage(KoishiHatArmorMaterial.BASE_DURABILITY)));
    public static final Item FUMO_LICENSE = registerItem("fumo_license", FumoLicenseItem::new, new Item.Settings());
    public static final Item CURSED_DECOY_DOLl = registerItem("cursed_decoy_doll", CursedDecoyDollItem::new, new Item.Settings());
    public static final Item VAISRAVANAS_PAGODA = registerItem("vaisravanas_pagoda", VaisravanasPagodaItem::new, new Item.Settings().maxCount(1).maxDamage(250).repairable(ModTags.ItemTypeTag.VAISRAVANAS_PAGODA));
    public static final Item DREAM_PILLOW = registerItem("dream_pillow", DreamPillowItem::new, new Item.Settings().maxDamage(4));
    public static final Item TENGU_SHIELD = registerItem("tengu_shield", TenguShieldItem::new, TenguShieldItem.createItemSettings());
    public static final Item TENGU_CAMERA = registerItem("tengu_camera", TenguCameraItem::new, new Item.Settings().maxCount(1).maxDamage(250).repairable(ItemTags.REPAIRS_IRON_ARMOR));
    public static final Item BAD_APPLE = registerItem("bad_apple", BadAppleItem::new, new Item.Settings().food(FoodComponents.GOLDEN_APPLE).maxCount(16).rarity(Rarity.EPIC));

    // 武器
    public static final Item HAKUREI_CANE = registerItem("hakurei_cane", (settings) -> new HakureiCane(1f, -2.4f, settings), new Item.Settings());
    public static final Item BAGUA_FURNACE = registerItem("bagua_furnace", BaguaFurnace::new, new Item.Settings().maxCount(1).maxDamage(200).repairable(Items.NETHERITE_INGOT));
    public static final Item WIND_BLESSING_CANE = registerItem("wind_blessing_cane", (settings) -> new WindBlessingCane(1f, -2.4f, settings), new Item.Settings());
    public static final Item MAGIC_BROOM = registerItem("magic_broom", (settings) -> new MagicBroom(1f, -2.4f, settings), new Item.Settings());
    public static final Item GUNGNIR = registerItem("gungnir", (settings) -> new Gungnir(1f, -2.4f, settings), new Item.Settings());
    public static final Item LEVATIN = registerItem("levatin", (settings) -> new Levatin(1f, -2.4f, settings), new Item.Settings());
    public static final Item ROKANKEN = registerItem("rokanken", (settings) -> new Rokanken(2f, 0.5f - 2.4f, settings), new Item.Settings());
    public static final Item HAKUROKEN = registerItem("hakuroken", (settings) -> new Hakuroken(2f, 1f - 2.4f, settings), new Item.Settings());
    public static final Item PAPILIO_PATTERN_FAN = registerItem("papilio_pattern_fan", (settings) -> new PapilioPatternFan(1f, 1f - 2.4f, settings), new Item.Settings());
    public static final Item HORAI_DAMA_NO_EDA = registerItem("horai-dama_no_eda", (settings) -> new HoraiDamaNoEdaItem(0, 0, settings), new Item.Settings());
    public static final Item MAPLE_LEAF_FAN = registerItem("maple_leaf_fan", (settings) -> new MapleLeafFan(1f, -2.4f, settings), new Item.Settings());
    public static final Item IBUKIHO = registerItem("ibukiho", (settings) -> new Ibukiho(1f, -2.4f, settings), new Item.Settings()
            .useCooldown(130f)
            .fireproof()
            .food(new FoodComponent.Builder()
                            .alwaysEdible()
                            .saturationModifier(-4f)
                            .build(),
                    ConsumableComponent.builder()
                            .consumeSeconds(3f)
                            .finishSound(SoundEvents.ENTITY_GENERIC_DRINK)
                            .build()
            ));
    public static final Item SWORD_OF_HISOU = registerItem("sword_of_hisou", (settings) -> new SwordOfHisou(1f, -2.4f, settings), new Item.Settings());
    public static final Item MANPOZUCHI = registerItem("manpozuchi", (settings) -> new ManpozuchiItem(3.5f, -2.5f, settings), new Item.Settings());
    public static final Item NUE_TRIDENT = registerItem("nue_trident", (settings) -> new NueTrident(3.5f, -2.8f, settings), new Item.Settings());
    public static final Item TRUMPET_GUN = registerItem("trumpet_gun", TrumpetGun::new, new Item.Settings().maxCount(1).maxDamage(150).repairable(Items.GOLD_BLOCK));
    public static final Item TREASURE_HUNTING_ROD = registerItem("treasure_hunting_rod", (settings) -> new TreasureHuntingRod(2f, -2.8f, settings), new Item.Settings());
    public static final Item DEATH_SCYTHE = registerItem("death_scythe", (settings) -> new DeathScytheItem(1f, -2.8f, settings), new Item.Settings());
    public static final Item VIOLIN = registerItem("violin", MusicalInstrumentItem::new, new Item.Settings().maxCount(1)
            .equipmentSlot((livingEntity, stack) -> EquipmentSlot.HEAD)
            .component(ModDataComponentTypes.NOTE_TYPE, NoteBlockInstrument.FLUTE));
    public static final Item KEYBOARD = registerItem("keyboard", MusicalInstrumentItem::new, new Item.Settings()
            .component(ModDataComponentTypes.NOTE_TYPE, NoteBlockInstrument.PLING));
    public static final Item TRUMPET = registerItem("trumpet", MusicalInstrumentItem::new, new Item.Settings()
            .component(ModDataComponentTypes.NOTE_TYPE, NoteBlockInstrument.DIDGERIDOO));

    // 银装备
    public static final Item RAW_SILVER = registerItem("raw_silver", Item::new, new Item.Settings());
    public static final Item SILVER_INGOT = registerItem("silver_ingot", Item::new, new Item.Settings());
    public static final Item SILVER_NUGGET = registerItem("silver_nugget", Item::new, new Item.Settings());
    public static final Item SILVER_SWORD = registerItem("silver_sword", (settings) -> new SwordItem(SilverMaterial.INSTANCE, 3.0f, -2.4f, settings), new Item.Settings().component(ModDataComponentTypes.SILVER_ITEM, Unit.INSTANCE));
    public static final Item SILVER_AXE = registerItem("silver_axe", (settings) -> new AxeItem(SilverMaterial.INSTANCE, 6.0f, -2.8f, settings), new Item.Settings().component(ModDataComponentTypes.SILVER_ITEM, Unit.INSTANCE));
    public static final Item SILVER_PICKAXE = registerItem("silver_pickaxe", (settings) -> new PickaxeItem(SilverMaterial.INSTANCE, 1.0f, -2.8f, settings), new Item.Settings().component(ModDataComponentTypes.SILVER_ITEM, Unit.INSTANCE));
    public static final Item SILVER_SHOVEL = registerItem("silver_shovel", (settings) -> new ShovelItem(SilverMaterial.INSTANCE, 1.5f, -3.0f, settings), new Item.Settings().component(ModDataComponentTypes.SILVER_ITEM, Unit.INSTANCE));
    public static final Item SILVER_HOE = registerItem("silver_hoe", (settings) -> new HoeItem(SilverMaterial.INSTANCE, -2.0f, -1.0f, settings), new Item.Settings().component(ModDataComponentTypes.SILVER_ITEM, Unit.INSTANCE));
    public static final Item SILVER_HELMET = registerItem("silver_helmet", (settings) -> new ArmorItem(SilverArmorMaterial.INSTANCE, EquipmentType.HELMET, settings), new Item.Settings().component(ModDataComponentTypes.SILVER_ITEM, Unit.INSTANCE).maxDamage(EquipmentType.HELMET.getMaxDamage(SilverArmorMaterial.BASE_DURABILITY)));
    public static final Item SILVER_CHESTPLATE = registerItem("silver_chestplate", (settings) -> new ArmorItem(SilverArmorMaterial.INSTANCE, EquipmentType.CHESTPLATE, settings), new Item.Settings().component(ModDataComponentTypes.SILVER_ITEM, Unit.INSTANCE).maxDamage(EquipmentType.CHESTPLATE.getMaxDamage(SilverArmorMaterial.BASE_DURABILITY)));
    public static final Item SILVER_LEGGINGS = registerItem("silver_leggings", (settings) -> new ArmorItem(SilverArmorMaterial.INSTANCE, EquipmentType.LEGGINGS, settings), new Item.Settings().component(ModDataComponentTypes.SILVER_ITEM, Unit.INSTANCE).maxDamage(EquipmentType.LEGGINGS.getMaxDamage(SilverArmorMaterial.BASE_DURABILITY)));
    public static final Item SILVER_BOOTS = registerItem("silver_boots", (settings) -> new ArmorItem(SilverArmorMaterial.INSTANCE, EquipmentType.BOOTS, settings), new Item.Settings().component(ModDataComponentTypes.SILVER_ITEM, Unit.INSTANCE).maxDamage(EquipmentType.BOOTS.getMaxDamage(SilverArmorMaterial.BASE_DURABILITY)));

    // 女仆装备
    public static final Item KNIFE = registerItem("knife", (settings) -> new Knife(0f, 0f, settings), new Item.Settings().maxCount(1)
            .component(ModDataComponentTypes.Danmaku.TEMPLATE, Touhou.id("single").toString())
            .component(ModDataComponentTypes.Danmaku.DAMAGE, 2.0f)
            .component(ModDataComponentTypes.Danmaku.SPEED, 0.5f)
            .component(ModDataComponentTypes.Danmaku.SCALE, 0.8f)
            .component(ModDataComponentTypes.Danmaku.COUNT, 1)
            .component(ModDataComponentTypes.Danmaku.TILE, false)
            .component(ModDataComponentTypes.Danmaku.INFINITE, false));
    public static final Item MAID_HAIRBAND = registerItem("maid_hairband", (settings) -> new ArmorItem(MaidArmorMaterial.INSTANCE, EquipmentType.HELMET, settings), new Item.Settings().maxDamage(EquipmentType.HELMET.getMaxDamage(MaidArmorMaterial.BASE_DURABILITY)));
    public static final Item MAID_UPPER_SKIRT = registerItem("maid_upper_skirt", (settings) -> new ArmorItem(MaidArmorMaterial.INSTANCE, EquipmentType.CHESTPLATE, settings), new Item.Settings().maxDamage(EquipmentType.CHESTPLATE.getMaxDamage(MaidArmorMaterial.BASE_DURABILITY)));
    public static final Item MAID_LOWER_SKIRT = registerItem("maid_lowerband", (settings) -> new ArmorItem(MaidArmorMaterial.INSTANCE, EquipmentType.LEGGINGS, settings), new Item.Settings().maxDamage(EquipmentType.LEGGINGS.getMaxDamage(MaidArmorMaterial.BASE_DURABILITY)));
    public static final Item MAID_SHOE = registerItem("maid_shoe", (settings) -> new ArmorItem(MaidArmorMaterial.INSTANCE, EquipmentType.BOOTS, settings), new Item.Settings().maxDamage(EquipmentType.BOOTS.getMaxDamage(MaidArmorMaterial.BASE_DURABILITY)));

    // 魔法冰装备
    public static final Item ICE_SCALES = registerItem("ice_scales", Item::new, new Item.Settings());
    public static final Item MAGIC_ICE_SWORD = registerItem("magic_ice_sword", (settings) -> new SwordItem(MagicIceMaterial.INSTANCE, 3.0f, -2.4f, settings), new Item.Settings());
    public static final Item MAGIC_ICE_AXE = registerItem("magic_ice_axe", (settings) -> new AxeItem(MagicIceMaterial.INSTANCE, 6.0f, -2.8f, settings), new Item.Settings());
    public static final Item MAGIC_ICE_PICKAXE = registerItem("magic_ice_pickaxe", (settings) -> new PickaxeItem(MagicIceMaterial.INSTANCE, 1.0f, -2.8f, settings), new Item.Settings());
    public static final Item MAGIC_ICE_SHOVEL = registerItem("magic_ice_shovel", (settings) -> new ShovelItem(MagicIceMaterial.INSTANCE, 1.5f, -3.0f, settings), new Item.Settings());
    public static final Item MAGIC_ICE_HOE = registerItem("magic_ice_hoe", (settings) -> new HoeItem(MagicIceMaterial.INSTANCE, -2.0f, -1.0f, settings), new Item.Settings());
    public static final Item MAGIC_ICE_HELMET = registerItem("magic_ice_helmet", (settings) -> new ArmorItem(MagicIceArmorMaterial.INSTANCE, EquipmentType.HELMET, settings), new Item.Settings().maxDamage(EquipmentType.HELMET.getMaxDamage(MagicIceArmorMaterial.BASE_DURABILITY)));
    public static final Item MAGIC_ICE_CHESTPLATE = registerItem("magic_ice_chestplate", (settings) -> new ArmorItem(MagicIceArmorMaterial.INSTANCE, EquipmentType.CHESTPLATE, settings), new Item.Settings().maxDamage(EquipmentType.CHESTPLATE.getMaxDamage(MagicIceArmorMaterial.BASE_DURABILITY)));
    public static final Item MAGIC_ICE_LEGGINGS = registerItem("magic_ice_leggings", (settings) -> new ArmorItem(MagicIceArmorMaterial.INSTANCE, EquipmentType.LEGGINGS, settings), new Item.Settings().maxDamage(EquipmentType.LEGGINGS.getMaxDamage(MagicIceArmorMaterial.BASE_DURABILITY)));
    public static final Item MAGIC_ICE_BOOTS = registerItem("magic_ice_boots", (settings) -> new ArmorItem(MagicIceArmorMaterial.INSTANCE, EquipmentType.BOOTS, settings), new Item.Settings().maxDamage(EquipmentType.BOOTS.getMaxDamage(MagicIceArmorMaterial.BASE_DURABILITY)));

    // 模板
    public static final Item SPELL_CARD_TEMPLATE = registerItem("spell_card_template", SpellCardTemplateItem::new, new Item.Settings());
    public static final Item ROLE_CARD = registerItem("role_card", RoleCardItem::new, new Item.Settings().maxCount(1)
            .component(DataComponentTypes.TOOLTIP_DISPLAY, TooltipDisplayComponent.DEFAULT.with(DataComponentTypes.DYED_COLOR, true))
            .component(DataComponentTypes.DYED_COLOR, new DyedColorComponent(RoleCard.DEFAULT_COLOR.intValue())));
    public static final Item ROLE_ARCHIVE = registerItem("role_archive", RoleFollowerArchiveItem::new, new Item.Settings().maxCount(1));

    // DISC
    public static final Item HR01_01 = registerAlbum("hr01_01", AlbumItem::new, new Item.Settings().jukeboxPlayable(JukeboxSongInit.HR01_01.getJukeboxSongRegistryKey()));
    public static final Item HR02_08 = registerAlbum("hr02_08", AlbumItem::new, new Item.Settings().jukeboxPlayable(JukeboxSongInit.HR02_08.getJukeboxSongRegistryKey()));
    public static final Item HR03_01 = registerAlbum("hr03_01", AlbumItem::new, new Item.Settings().jukeboxPlayable(JukeboxSongInit.HR03_01.getJukeboxSongRegistryKey()));
    public static final Item MELODIC_TASTE_NIGHTMARE_BEFORE_CROSSROADS = registerAlbum("melodic-taste-nightmare-before-crossroads", AlbumItem::new, new Item.Settings().jukeboxPlayable(JukeboxSongInit.MELODIC_TASTE_NIGHTMARE_BEFORE_CROSSROADS.getJukeboxSongRegistryKey()));
    public static final Item YV_FLOWER_CLOCK_AND_DREAMS = registerAlbum("yv_flower_clock_and_dreams", AlbumItem::new,new Item.Settings().jukeboxPlayable(JukeboxSongInit.YV_FLOWER_CLOCK_AND_DREAMS.getJukeboxSongRegistryKey()));
    public static final Item GLOWING_NEEDLES_LITTLE_PEOPLE = registerAlbum("glowing_needles_little_people", AlbumItem::new,new Item.Settings().jukeboxPlayable(JukeboxSongInit.GLOWING_NEEDLES_LITTLE_PEOPLE.getJukeboxSongRegistryKey()));
    public static final Item COOKIE = registerAlbum("cookie", AlbumItem::new,new Item.Settings().jukeboxPlayable(JukeboxSongInit.COOKIE.getJukeboxSongRegistryKey()));
    public static final Item BADAPPLE = registerAlbum("bad-apple",AlbumItem::new, new Item.Settings().jukeboxPlayable(JukeboxSongInit.BAD_APPLE.getJukeboxSongRegistryKey()));

    static {
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.OPERATOR).register(itemGroup -> {
            itemGroup.add(BATTLE_STICK);
            itemGroup.add(OWNER_STICK);
        });
    }

    public static void registerItems() {
        List<Item> silverItems = new ArrayList<>(List.of(SILVER_SWORD, SILVER_AXE, SILVER_PICKAXE, SILVER_HOE, SILVER_HOE));
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(itemGroup -> {
            itemGroup.addAfter(Items.MUSIC_DISC_PIGSTEP, AlbumItem.ITEMS.stream().map(Item::getDefaultStack).toList());
        });
        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (!world.isClient() && silverItems.contains(player.getStackInHand(hand).getItem())) {
                if (entity instanceof LivingEntity livingEntity) {
                    DynamicRegistryManager registryManager = world.getRegistryManager();
                    Registry<EntityType<?>> registry = registryManager.getOrThrow(RegistryKeys.ENTITY_TYPE);
                    Optional<RegistryEntryList.Named<EntityType<?>>> listOptional = registry.getOptional(EntityTypeTags.UNDEAD);
                    if (listOptional.isPresent()) {
                        RegistryEntryList.Named<EntityType<?>> list = listOptional.get();
                        boolean contains = list.contains(RegistryEntry.of(livingEntity.getType()));
                        if (contains) {
                            livingEntity.damage((ServerWorld) world, world.getDamageSources().magic(), 1.0F);
                        }
                    }
                }
            }
            return ActionResult.PASS;
        });
    }

    public static Item registerItem(String id, Function<Item.Settings, Item> factory, Item.Settings settings) {
        Item item = registerSimpleItem(id, factory, settings);
        ITEM_LIST.add(item);
        return item;
    }

    public static Item registerCreativeTabIcon(String id, Function<Item.Settings, Item> factory, Item.Settings settings) {
        return registerSimpleItem(id, factory, settings);
    }

    public static Item registerAlbum(String id, Function<Item.Settings, Item> factory, Item.Settings settings) {
        return registerSimpleItem(id, factory, settings);
    }

    public static Item registerSimpleItem(Identifier id, Function<Item.Settings, Item> factory, Item.Settings settings) {
        Item item = factory.apply(settings.registryKey(keyOf(id)));
        Registry.register(Registries.ITEM, id, item);
        ItemTypeGroup.join(item);
        PolymerItemHelper.registerOverlay(item);
        return item;
    }

    public static Item registerSimpleItem(String name, Function<Item.Settings, Item> factory, Item.Settings settings) {
        return registerSimpleItem(Touhou.id(name), factory, settings);
    }

    public static RegistryKey<Item> keyOf(Identifier id) {
        return RegistryKey.of(RegistryKeys.ITEM, id);
    }

    public static RegistryKey<Item> keyOf(String id) {
        return RegistryKey.of(RegistryKeys.ITEM, Touhou.id(id));
    }

    public static RegistryKey<Item> keyOf(RegistryKey<Block> blockKey) {
        return RegistryKey.of(RegistryKeys.ITEM, blockKey.getValue());
    }

    public static List<Item> getItemView() {
        return List.copyOf(ITEM_LIST);
    }
}
