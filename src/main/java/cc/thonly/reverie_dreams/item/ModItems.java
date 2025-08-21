package cc.thonly.reverie_dreams.item;

import cc.thonly.reverie_dreams.Touhou;
import cc.thonly.reverie_dreams.armor.*;
import cc.thonly.reverie_dreams.component.ModDataComponentTypes;
import cc.thonly.reverie_dreams.item.armor.BasicArmorItem;
import cc.thonly.reverie_dreams.item.armor.EarphoneItem;
import cc.thonly.reverie_dreams.item.armor.KoishiHatItem;
import cc.thonly.reverie_dreams.item.base.BasicPolymerDiscItem;
import cc.thonly.reverie_dreams.item.base.BasicPolymerSpawnEggItem;
import cc.thonly.reverie_dreams.item.debug.BattleStickItem;
import cc.thonly.reverie_dreams.item.debug.OwnerStickItem;
import cc.thonly.reverie_dreams.item.tool.*;
import cc.thonly.reverie_dreams.item.weapon.*;
import cc.thonly.reverie_dreams.sound.JukeboxSongInit;
import cc.thonly.reverie_dreams.util.IdentifierGetter;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.block.enums.NoteBlockInstrument;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.DyedColorComponent;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.component.type.TooltipDisplayComponent;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.equipment.EquipmentType;
import net.minecraft.registry.*;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.tag.EntityTypeTags;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.world.ServerWorld;
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
    private static final List<Item> ICON_LIST = new ArrayList<>();
    private static final List<Item> ITEM_LIST = new ArrayList<>();
    private static final List<Item> DISC_LIST = new ArrayList<>();

    public static final ItemStack NOT_COMPLETED = ((Supplier<ItemStack>) () -> {
        ItemStack itemStack = new ItemStack(Items.BARRIER);
        itemStack.set(DataComponentTypes.ITEM_NAME, Text.literal("§cThis page is not completed"));
        return itemStack;
    }).get();

    // 调试
    public static final Item BATTLE_STICK = registerSimpleItem(new BattleStickItem("battle_stick", new Item.Settings()));
    public static final Item OWNER_STICK = registerSimpleItem(new OwnerStickItem("owner_stick", new Item.Settings()));

    // 图标
    public static final Item ICON = registerIconItem(new BasicItem("icon", new Item.Settings()));
    public static final Item FUMO_ICON = registerIconItem(new BasicItem("fumo_icon", new Item.Settings()));
    public static final Item ROLE_ICON = registerIconItem(new BasicItem("role_icon", new Item.Settings()));
    public static final Item SPAWN_EGG = registerIconItem(new BasicItem("spawn_egg", new Item.Settings().component(DataComponentTypes.DYED_COLOR, BasicPolymerSpawnEggItem.DEFAULT_COLOR)));
    public static final Item DANMAKU = registerIconItem(new BasicItem("danmaku", new Item.Settings()));

    // 材料
    public static final Item POINT = registerItem(new BasicItem("point", new Item.Settings()));
    public static final Item POWER = registerItem(new BasicItem("power", new Item.Settings()));
    public static final Item UPGRADED_HEALTH_FRAGMENT = registerItem(new BasicItem("upgraded_health_fragment", new Item.Settings()));
    public static final Item BOMB_FRAGMENT = registerItem(new BasicItem("bomb_fragment", new Item.Settings()));
    public static final Item RED_ORB = registerItem(new BasicItem("red_orb", new Item.Settings()));
    public static final Item BLUE_ORB = registerItem(new BasicItem("blue_orb", new Item.Settings()));
    public static final Item YELLOW_ORB = registerItem(new BasicItem("yellow_orb", new Item.Settings()));
    public static final Item GREEN_ORB = registerItem(new BasicItem("green_orb", new Item.Settings()));
    public static final Item PURPLE_ORB = registerItem(new BasicItem("purple_orb", new Item.Settings()));
    public static final Item YIN_YANG_ORB = registerItem(new BasicItem("yin-yang_orb", new Item.Settings()));
    public static final Item SPEED_FEATHER = registerItem(new BasicItem("speed_feather", new Item.Settings().component(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true)));

    // 道具
    public static final Item TOUHOU_HELPER = registerItem(new TouhouHelperItem("touhou_helper", new Item.Settings()));
    public static final Item UPGRADED_HEALTH = registerItem(new UpgradedHealthItem("upgraded_health", new Item.Settings()));
    public static final Item BOMB = registerItem(new BombItem("bomb", new Item.Settings().useCooldown(2.0f)));
    public static final Item CROSSING_CHISEL = registerItem(new CrossingChisel("crossing_chisel", new Item.Settings()));
    public static final Item GAP_BALL = registerItem(new GapBall("gap_ball", new Item.Settings()));
    public static final Item TIME_STOP_CLOCK = registerItem(new TimeStopClock("time_stop_clock", new Item.Settings()));
    public static final Item EARPHONE = registerItem(new EarphoneItem("earphone", new Item.Settings().maxDamage(EquipmentType.HELMET.getMaxDamage(EarphoneArmorMaterial.BASE_DURABILITY))));
    public static final Item KOISHI_HAT = registerItem(new KoishiHatItem("koishi_hat", new Item.Settings().maxDamage(EquipmentType.HELMET.getMaxDamage(KoishiHatArmorMaterial.BASE_DURABILITY))));
    public static final Item FUMO_LICENSE = registerItem(new FumoLicenseItem("fumo_license", new Item.Settings()));
    public static final Item CURSED_DECOY_DOLl = registerItem(new CursedDecoyDollItem("cursed_decoy_doll", new Item.Settings()));
    public static final Item VAISRAVANAS_PAGODA = registerItem(new VaisravanasPagodaItem("vaisravanas_pagoda", new Item.Settings()));
    public static final Item DREAM_PILLOW = registerItem(new DreamPillowItem("dream_pillow", new Item.Settings().maxDamage(4)));
    public static final Item TENGU_SHIELD = registerItem(new TenguShieldItem("tengu_shield", TenguShieldItem.createItemSettings()));
    public static final Item TENGU_CAMERA = registerItem(new TenguCameraItem("tengu_camera", new Item.Settings().repairable(ItemTags.REPAIRS_IRON_ARMOR).maxDamage(250).maxCount(1)));
    public static final Item BAD_APPLE = registerItem(new BadAppleItem("bad_apple", new Item.Settings().maxCount(16).rarity(Rarity.EPIC)));

    // 武器
    public static final Item HAKUREI_CANE = registerItem(new HakureiCane("hakurei_cane", 0, 0, new Item.Settings()));
    public static final Item BAGUA_FURNACE = registerItem(new BaguaFurnace("bagua_furnace", new Item.Settings()));
    public static final Item WIND_BLESSING_CANE = registerItem(new WindBlessingCane("wind_blessing_cane", 0, 0, new Item.Settings()));
    public static final Item MAGIC_BROOM = registerItem(new MagicBroom("magic_broom", 0, 0, new Item.Settings()));
    public static final Item GUNGNIR = registerItem(new Gungnir("gungnir", 0, 0, new Item.Settings()));
    public static final Item LEVATIN = registerItem(new Levatin("levatin", 0, 0, new Item.Settings()));
    public static final Item ROKANKEN = registerItem(new Rokanken("rokanken", 1f, 0.5f, new Item.Settings()));
    public static final Item HAKUROKEN = registerItem(new Hakuroken("hakuroken", 1f, 1f, new Item.Settings()));
    public static final Item PAPILIO_PATTERN_FAN = registerItem(new PapilioPatternFan("papilio_pattern_fan", 0f, 1f, new Item.Settings()));
    public static final Item HORAI_DAMA_NO_EDA = registerItem(new HoraiDamaNoEdaItem("horai-dama_no_eda", 0, 0, new Item.Settings()));
    public static final Item MAPLE_LEAF_FAN = registerItem(new MapleLeafFan("maple_leaf_fan", 0, 0, new Item.Settings()));
    public static final Item IBUKIHO = registerItem(new Ibukiho("ibukiho", 0, 0, new Item.Settings()));
    public static final Item SWORD_OF_HISOU = registerItem(new SwordOfHisou("sword_of_hisou", 0, 0, new Item.Settings()));
    public static final Item MANPOZUCHI = registerItem(new ManpozuchiItem("manpozuchi", 0, 0, new Item.Settings()));
    public static final Item NUE_TRIDENT = registerItem(new NueTrident("nue_trident", 0, 0, new Item.Settings()));
    public static final Item TRUMPET_GUN = registerItem(new TrumpetGun("trumpet_gun", new Item.Settings().repairable(Items.GOLD_BLOCK)));
    public static final Item TREASURE_HUNTING_ROD = registerItem(new TreasureHuntingRod("treasure_hunting_rod", 0, 0, new Item.Settings()));
    public static final Item VIOLIN = registerItem(new MusicalInstrumentItem("violin", new Item.Settings().component(ModDataComponentTypes.NOTE_TYPE, NoteBlockInstrument.FLUTE)));
    public static final Item KEYBOARD = registerItem(new MusicalInstrumentItem("keyboard", new Item.Settings().component(ModDataComponentTypes.NOTE_TYPE, NoteBlockInstrument.PLING)));
    public static final Item TRUMPET = registerItem(new MusicalInstrumentItem("trumpet", new Item.Settings().component(ModDataComponentTypes.NOTE_TYPE, NoteBlockInstrument.DIDGERIDOO)));
    public static final Item DEATH_SCYTHE = registerItem(new DeathScytheItem("death_scythe", 0, 0, new Item.Settings()));

    // 银装备
    public static final Item RAW_SILVER = registerItem(new BasicItem("raw_silver", new Item.Settings()));
    public static final Item SILVER_INGOT = registerItem(new BasicItem("silver_ingot", new Item.Settings()));
    public static final Item SILVER_NUGGET = registerItem(new BasicItem("silver_nugget", new Item.Settings()));
    public static final Item SILVER_SWORD = registerItem(new BasicSwordItem("silver_sword", SilverMaterial.INSTANCE, 3.0f, -2.4f, new Item.Settings().component(ModDataComponentTypes.SILVER_ITEM, Unit.INSTANCE)));
    public static final Item SILVER_AXE = registerItem(new BasicAxeItem("silver_axe", SilverMaterial.INSTANCE, 6.0f, -2.8f, new Item.Settings().component(ModDataComponentTypes.SILVER_ITEM, Unit.INSTANCE)));
    public static final Item SILVER_PICKAXE = registerItem(new BasicPickaxeItem("silver_pickaxe", SilverMaterial.INSTANCE, 1.0f, -2.8f, new Item.Settings().component(ModDataComponentTypes.SILVER_ITEM, Unit.INSTANCE)));
    public static final Item SILVER_SHOVEL = registerItem(new BasicShovelItem("silver_shovel", SilverMaterial.INSTANCE, 1.5f, -3.0f, new Item.Settings().component(ModDataComponentTypes.SILVER_ITEM, Unit.INSTANCE)));
    public static final Item SILVER_HOE = registerItem(new BasicHoeItem("silver_hoe", SilverMaterial.INSTANCE, -2.0f, -1.0f, new Item.Settings().component(ModDataComponentTypes.SILVER_ITEM, Unit.INSTANCE)));
    public static final Item SILVER_HELMET = registerItem(new BasicArmorItem("silver_helmet", SilverArmorMaterial.INSTANCE, EquipmentType.HELMET, new Item.Settings().component(ModDataComponentTypes.SILVER_ITEM, Unit.INSTANCE).maxDamage(EquipmentType.HELMET.getMaxDamage(SilverArmorMaterial.BASE_DURABILITY))));
    public static final Item SILVER_CHESTPLATE = registerItem(new BasicArmorItem("silver_chestplate", SilverArmorMaterial.INSTANCE, EquipmentType.CHESTPLATE, new Item.Settings().component(ModDataComponentTypes.SILVER_ITEM, Unit.INSTANCE).maxDamage(EquipmentType.CHESTPLATE.getMaxDamage(SilverArmorMaterial.BASE_DURABILITY))));
    public static final Item SILVER_LEGGINGS = registerItem(new BasicArmorItem("silver_leggings", SilverArmorMaterial.INSTANCE, EquipmentType.LEGGINGS, new Item.Settings().component(ModDataComponentTypes.SILVER_ITEM, Unit.INSTANCE).maxDamage(EquipmentType.LEGGINGS.getMaxDamage(SilverArmorMaterial.BASE_DURABILITY))));
    public static final Item SILVER_BOOTS = registerItem(new BasicArmorItem("silver_boots", SilverArmorMaterial.INSTANCE, EquipmentType.BOOTS, new Item.Settings().component(ModDataComponentTypes.SILVER_ITEM, Unit.INSTANCE).maxDamage(EquipmentType.BOOTS.getMaxDamage(SilverArmorMaterial.BASE_DURABILITY))));

    // 女仆装备
    public static final Item KNIFE = registerItem(new Knife("knife", 0f, 0f, new Item.Settings()));
    public static final Item MAID_HAIRBAND = registerItem(new BasicArmorItem("maid_hairband", MaidArmorMaterial.INSTANCE, EquipmentType.HELMET, new Item.Settings().maxDamage(EquipmentType.HELMET.getMaxDamage(MaidArmorMaterial.BASE_DURABILITY))));
    public static final Item MAID_UPPER_SKIRT = registerItem(new BasicArmorItem("maid_upper_skirt", MaidArmorMaterial.INSTANCE, EquipmentType.CHESTPLATE, new Item.Settings().maxDamage(EquipmentType.CHESTPLATE.getMaxDamage(MaidArmorMaterial.BASE_DURABILITY))));
    public static final Item MAID_LOWER_SKIRT = registerItem(new BasicArmorItem("maid_lowerband", MaidArmorMaterial.INSTANCE, EquipmentType.LEGGINGS, new Item.Settings().maxDamage(EquipmentType.LEGGINGS.getMaxDamage(MaidArmorMaterial.BASE_DURABILITY))));
    public static final Item MAID_SHOE = registerItem(new BasicArmorItem("maid_shoe", MaidArmorMaterial.INSTANCE, EquipmentType.BOOTS, new Item.Settings().maxDamage(EquipmentType.BOOTS.getMaxDamage(MaidArmorMaterial.BASE_DURABILITY))));

    // 魔法冰装备
    public static final Item ICE_SCALES = registerItem(new BasicItem("ice_scales", new Item.Settings()));
    public static final Item MAGIC_ICE_SWORD = registerItem(new BasicSwordItem("magic_ice_sword", MagicIceMaterial.INSTANCE, 3.0f, -2.4f, new Item.Settings()));
    public static final Item MAGIC_ICE_AXE = registerItem(new BasicAxeItem("magic_ice_axe", MagicIceMaterial.INSTANCE, 6.0f, -2.8f, new Item.Settings()));
    public static final Item MAGIC_ICE_PICKAXE = registerItem(new BasicPickaxeItem("magic_ice_pickaxe", MagicIceMaterial.INSTANCE, 1.0f, -2.8f, new Item.Settings()));
    public static final Item MAGIC_ICE_SHOVEL = registerItem(new BasicShovelItem("magic_ice_shovel", MagicIceMaterial.INSTANCE, 1.5f, -3.0f, new Item.Settings()));
    public static final Item MAGIC_ICE_HOE = registerItem(new BasicHoeItem("magic_ice_hoe", MagicIceMaterial.INSTANCE, -2.0f, -1.0f, new Item.Settings()));
    public static final Item MAGIC_ICE_HELMET = registerItem(new BasicArmorItem("magic_ice_helmet", MagicIceArmorMaterial.INSTANCE, EquipmentType.HELMET, new Item.Settings().maxDamage(EquipmentType.HELMET.getMaxDamage(MagicIceArmorMaterial.BASE_DURABILITY))));
    public static final Item MAGIC_ICE_CHESTPLATE = registerItem(new BasicArmorItem("magic_ice_chestplate", MagicIceArmorMaterial.INSTANCE, EquipmentType.CHESTPLATE, new Item.Settings().maxDamage(EquipmentType.CHESTPLATE.getMaxDamage(MagicIceArmorMaterial.BASE_DURABILITY))));
    public static final Item MAGIC_ICE_LEGGINGS = registerItem(new BasicArmorItem("magic_ice_leggings", MagicIceArmorMaterial.INSTANCE, EquipmentType.LEGGINGS, new Item.Settings().maxDamage(EquipmentType.LEGGINGS.getMaxDamage(MagicIceArmorMaterial.BASE_DURABILITY))));
    public static final Item MAGIC_ICE_BOOTS = registerItem(new BasicArmorItem("magic_ice_boots", MagicIceArmorMaterial.INSTANCE, EquipmentType.BOOTS, new Item.Settings().maxDamage(EquipmentType.BOOTS.getMaxDamage(MagicIceArmorMaterial.BASE_DURABILITY))));

    // 模板
    public static final Item SPELL_CARD_TEMPLATE = registerItem(new SpellCardTemplateItem("spell_card_template", new Item.Settings()));
    public static final Item ROLE_CARD = registerItem(new RoleCardItem("role_card", new Item.Settings().maxCount(1).component(DataComponentTypes.TOOLTIP_DISPLAY, TooltipDisplayComponent.DEFAULT.with(DataComponentTypes.DYED_COLOR, true)).component(DataComponentTypes.DYED_COLOR, new DyedColorComponent(RoleCard.DEFAULT_COLOR.intValue()))));
    public static final Item ROLE_ARCHIVE = registerItem(new RoleFollowerArchiveItem("role_archive", new Item.Settings().maxCount(1)));

    // DISC
    public static final Item HR01_01 = registerDiscItem(new BasicPolymerDiscItem("hr01_01", new Item.Settings().jukeboxPlayable(JukeboxSongInit.HR01_01.getJukeboxSongRegistryKey())));
    public static final Item HR02_08 = registerDiscItem(new BasicPolymerDiscItem("hr02_08", new Item.Settings().jukeboxPlayable(JukeboxSongInit.HR02_08.getJukeboxSongRegistryKey())));
    public static final Item HR03_01 = registerDiscItem(new BasicPolymerDiscItem("hr03_01", new Item.Settings().jukeboxPlayable(JukeboxSongInit.HR03_01.getJukeboxSongRegistryKey())));
    public static final Item MELODIC_TASTE_NIGHTMARE_BEFORE_CROSSROADS = registerDiscItem(new BasicPolymerDiscItem("melodic-taste-nightmare-before-crossroads", new Item.Settings().jukeboxPlayable(JukeboxSongInit.MELODIC_TASTE_NIGHTMARE_BEFORE_CROSSROADS.getJukeboxSongRegistryKey())));
    public static final Item YV_FLOWER_CLOCK_AND_DREAMS = registerDiscItem(new BasicPolymerDiscItem("yv_flower_clock_and_dreams", new Item.Settings().jukeboxPlayable(JukeboxSongInit.YV_FLOWER_CLOCK_AND_DREAMS.getJukeboxSongRegistryKey())));
    public static final Item GLOWING_NEEDLES_LITTLE_PEOPLE = registerDiscItem(new BasicPolymerDiscItem("glowing_needles_little_people", new Item.Settings().jukeboxPlayable(JukeboxSongInit.GLOWING_NEEDLES_LITTLE_PEOPLE.getJukeboxSongRegistryKey())));
    public static final Item COOKIE = registerDiscItem(new BasicPolymerDiscItem("cookie", new Item.Settings().jukeboxPlayable(JukeboxSongInit.COOKIE.getJukeboxSongRegistryKey())));
    public static final Item BADAPPLE = registerDiscItem(new BasicPolymerDiscItem("bad-apple", new Item.Settings().jukeboxPlayable(JukeboxSongInit.BAD_APPLE.getJukeboxSongRegistryKey())));

    // 测试物品
//    public static final Item TEST_COLOR_DANMAKU_ITEM = registerItem(new BasicItem("test_color_danmaku", new Item.Settings()));

    static {
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.OPERATOR).register(itemGroup -> {
            itemGroup.add(BATTLE_STICK);
            itemGroup.add(OWNER_STICK);
        });
    }

    // 符卡模板
//    public static final Item EMPTY_SPELL_CARD = registerItem(new BasicPolymerSpellCardItem("empty_spell_card", new Item.Settings()) {
//        @Override
//        public void spellCard(ServerWorld world, PlayerEntity user, Hand hand, ItemStack offHandStack) {
//        }
//
//        @Override
//        public int getBulletConsumption() {
//            return 0;
//        }
//    });

//    public static final Item DEBUG_SPELL_CARD_ITEM = registerItem(new BasicPolymerSpellCardItem("test_spell_card", new Item.Settings()) {
//        @Override
//        public void spellCard(ServerWorld world, PlayerEntity user, Hand hand, ItemStack offHandStack) {
//            float pitch = user.getPitch();
//            float yaw = user.getYaw();
//
//            spawnDanmakuEntity(world, user, hand, offHandStack, pitch, yaw - 25, 1.4f, 5.0f);
//            spawnDanmakuEntity(world, user, hand, offHandStack, pitch, yaw, 1.4f, 5.0f);
//            spawnDanmakuEntity(world, user, hand, offHandStack, pitch, yaw + 25.0f, 1.4f, 5.0f);
//        }
//
//        @Override
//        public int getBulletConsumption() {
//            return 3;
//        }
//    });

//    public static final Item DEBUG_SPELL_CARD_ITEM2 = registerItem(new BasicPolymerSpellCardItem("test_spell_card2", new Item.Settings()) {
//        @Override
//        public void spellCard(ServerWorld world, PlayerEntity user, Hand hand, ItemStack offHandStack) {
//            ItemStack main = user.getMainHandStack().copy();
//            ItemStack copy = offHandStack.copy();
//
//            SpellCardEntity spellCardEntity = new SpellCardEntity(user, main, offHandStack, hand, 20 * 20) {
//                @Override
//                public void apply() {
//                    Integer tickCount = this.getTickCount();
//
//                    // 控制参数
//                    int bulletCount = 7;  // 弹幕数量
//                    float speed = 1.0f;   // 子弹的发射速度
//                    float divergence = 0.05f; // 子弹的发射偏差
//                    double radius = 2.0; // 发射轨迹的半径
//                    double angularSpeed = 0.1;  // 旋转速度
//
//                    // 旋转一圈后再回到原位
//                    double totalRotation = 2 * Math.PI;  // 一圈的旋转角度
//                    double angularStep = totalRotation / bulletCount;  // 每个子弹之间的角度间隔
//
//                    // 计算当前旋转方向
//                    int cycleTicks = 20 * 5; // 一个周期的 tick 数（20秒）
//                    int halfCycleTicks = cycleTicks / 2; // 半周期 tick 数（10秒）
//
//                    // 根据 tickCount 来调整旋转方向
//                    double currentRotation;
//                    if (tickCount % cycleTicks < halfCycleTicks) {
//                        // 顺时针旋转
//                        currentRotation = angularSpeed * (tickCount % cycleTicks);
//                    } else {
//                        // 逆时针旋转
//                        currentRotation = angularSpeed * (cycleTicks - (tickCount % cycleTicks));
//                    }
//
//                    // 子弹发射
//                    for (int i = 0; i < bulletCount; i++) {
//                        // 计算当前子弹的角度，确保它顺时针旋转一圈后转回来
//                        double angle = i * angularStep + currentRotation;
//
//                        // 计算子弹的偏移量
//                        double xOffset = radius * Math.cos(angle);
//                        double zOffset = radius * Math.sin(angle);
//
//                        // 创建子弹实体
//                        DanmakuEntity danmaku = new DanmakuEntity(
//                                (LivingEntity) this.getOwner(),
//                                copy.copy(),
//                                Hand.MAIN_HAND,
//                                (BasicPolymerDanmakuItem) copy.getItem(),
//                                this.getPitch(),  // 获取物品的俯仰角（pitch）
//                                this.getYaw(),    // 获取物品的偏航角（yaw）
//                                speed,            // 发射速度
//                                divergence        // 发射偏差
//                        );
//
//                        // 设置子弹的初始位置
//                        danmaku.setPos(this.getX() + xOffset, this.getY(), this.getZ() + zOffset);
//                        double motionX = speed * Math.cos(angle);
//                        double motionZ = speed * Math.sin(angle);
//                        danmaku.setVelocity(motionX, 0, motionZ);
//
//                        // 向世界中发射子弹
//                        if (!this.getWorld().isClient) {
//                            this.getWorld().spawnEntity(danmaku);
//                        }
//                    }
//                }
//            };
//
//            world.spawnEntity(spellCardEntity);
//            world.playSound(null, user.getX(), user.getEyeY(), user.getZ(), SoundEventInit.SPELL_CARD, user.getSoundCategory(), 1.0f, 1.0f);
//        }
//
//        @Override
//        public int getBulletConsumption() {
//            return 0;
//        }
//    });

    // 弹幕

//    public static final IDanmakuItem AMULET = IDanmakuItem.createBuilder("amulet", DanmakuColor.ALL_COLOR, 2f, 1f, 1.0f, false, false).build();
//    public static final IDanmakuItem ARROWHEAD = IDanmakuItem.createBuilder("arrowhead", DanmakuColor.ALL_COLOR, 2f, 1f, 1.0f, false, false).build();
//    public static final IDanmakuItem BALL = IDanmakuItem.createBuilder("ball", DanmakuColor.ALL_COLOR, 2f, 1f, 1.0f, true, false).build();
//    public static final IDanmakuItem BUBBLE = IDanmakuItem.createBuilder("bubble", List.of(DanmakuColor.GREY, DanmakuColor.RED, DanmakuColor.PURPLE, DanmakuColor.DARK_BLUE, DanmakuColor.BLUE), 2.5f, 1f, 1.0f, true, false).build();
//    public static final IDanmakuItem BULLET = IDanmakuItem.createBuilder("bullet", DanmakuColor.ALL_COLOR, 2f, 1f, 1.0f, false, false).build();
//    public static final IDanmakuItem FIREBALL = IDanmakuItem.createBuilder("fireball", DanmakuColor.ALL_COLOR, 2f, 1f, 1.0f, true, false).build();
//    public static final IDanmakuItem FIREBALL_GLOWY = IDanmakuItem.createBuilder("fireball_glowy", DanmakuColor.ALL_COLOR, 1f, 1f, 1.0f, true, false).build();
//    public static final IDanmakuItem KUNAI = IDanmakuItem.createBuilder("kunai", DanmakuColor.ALL_COLOR, 2f, 1f, 1.0f, false, false).build();
//    //public static final IDanmakuItem MENTOS = IDanmakuItem.createBuilder("mentos", ALL_COLOR, 2f, 1f, 1.0f, false).build(); // 暂占位
//    public static final IDanmakuItem RICE = IDanmakuItem.createBuilder("rice", DanmakuColor.ALL_COLOR, 2f, 1f, 1.0f, false, false).build();
//    public static final IDanmakuItem STAR = IDanmakuItem.createBuilder("star", DanmakuColor.ALL_COLOR, 2f, 1f, 1.0f, true, false).build();

//    public static final List<IDanmakuItem> DANMAKU_ITEMS = List.of(
//            AMULET,
//            ARROWHEAD,
//            BALL,
//            BUBBLE,
//            BULLET,
//            FIREBALL,
//            FIREBALL_GLOWY,
//            KUNAI,
//            RICE,
//            STAR
//    );

//    public static final Item DEBUG_DANMAKU_ITEM = registerDanmakuItemNoList(new BasicDanmakuItemTypeItem(
//            "debug_danmaku",
//            new Item.Settings()
//                    .component(ModDataComponentTypes.Danmaku.DAMAGE, 2.0f)
//                    .component(ModDataComponentTypes.Danmaku.SCALE, 1.0f)
//                    .component(ModDataComponentTypes.Danmaku.SPEED, 1.0f)
//                    .component(ModDataComponentTypes.Danmaku.INFINITE, true)
//    ));

    public static void registerItems() {
        ArrayList<ItemStack> discStack = new ArrayList<>();
        for (var disc : DISC_LIST) {
            discStack.add(disc.getDefaultStack());
        }
        List<Item> silverItems = new ArrayList<>(List.of(SILVER_SWORD, SILVER_AXE, SILVER_PICKAXE, SILVER_HOE, SILVER_HOE));
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(itemGroup -> {
            itemGroup.addAfter(Items.MUSIC_DISC_PIGSTEP, discStack);
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

    public static Item registerItem(IdentifierGetter item) {
        Registry.register(Registries.ITEM, item.getIdentifier(), (Item) item);
        ITEM_LIST.add((Item) item);
        return (Item) item;
    }

    public static Item registerSimpleItem(IdentifierGetter item) {
        return Registry.register(Registries.ITEM, item.getIdentifier(), (Item) item);
    }

    public static Item registerIconItem(IdentifierGetter item) {
        Registry.register(Registries.ITEM, item.getIdentifier(), (Item) item);
        ICON_LIST.add((Item) item);
        return (Item) item;
    }

    public static Item registerDiscItem(IdentifierGetter item) {
        Registry.register(Registries.ITEM, item.getIdentifier(), (Item) item);
        DISC_LIST.add((Item) item);
        return (Item) item;
    }

    public static Item registerItem(String path, Function<Identifier, Item> itemFactory) {
        Identifier id = Touhou.id(path);
        RegistryKey<Item> itemKey = RegistryKey.of(RegistryKeys.ITEM, id);
        Item item = itemFactory.apply(id);
        Registry.register(Registries.ITEM, itemKey, item);
        ITEM_LIST.add(item);
        return item;
    }

    public static List<Item> getItemView() {
        return List.copyOf(ITEM_LIST);
    }

    public static List<Item> getDiscItemView() {
        return List.copyOf(DISC_LIST);
    }

    public static List<Item> getIconItems() {
        return List.copyOf(ICON_LIST);
    }
}
