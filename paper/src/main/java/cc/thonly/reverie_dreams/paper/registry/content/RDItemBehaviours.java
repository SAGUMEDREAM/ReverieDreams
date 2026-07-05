package cc.thonly.reverie_dreams.paper.registry.content;

import cc.thonly.reverie_dreams.paper.ReverieDreamsPlugin;
import cc.thonly.reverie_dreams.paper.item.armor.*;
import net.momirealms.craftengine.core.item.behavior.ItemBehaviors;
import net.momirealms.craftengine.core.util.Key;

public class RDItemBehaviours {
    // Danmaku
    public static final Key CROWN_OF_THE_UNDER_WORLD_ARMOR = ReverieDreamsPlugin.key("crown_of_the_underworld_armor");
    public static final Key DREAM_ARMOR = ReverieDreamsPlugin.key("dream_armor");
    public static final Key EARPHONE_ARMOR = ReverieDreamsPlugin.key("earphone_armor");
    public static final Key KOISHI_HAT_ARMOR = ReverieDreamsPlugin.key("koishi_hat_armor");
    public static final Key LOW_GRAVITY_BOOT_ARMOR = ReverieDreamsPlugin.key("low_gravity_boot_armor");
    public static final Key WATERPROOF_ARMOR = ReverieDreamsPlugin.key("waterproof_armor");
    public static final Key SILVER_ARMOR = ReverieDreamsPlugin.key("silver_armor");
    // Base
    public static final Key ALBUM_ITEM = ReverieDreamsPlugin.key("album_item");
    // Danmaku
    public static final Key DANMAKU_ITEM = ReverieDreamsPlugin.key("danmaku_item");
    // Other
    public static final Key GUIDE_BOOK_ITEM = ReverieDreamsPlugin.key("guide_book_item");
    // Prop
    public static final Key BAD_APPLE_ITEM = ReverieDreamsPlugin.key("bad_apple_item");
    public static final Key BOMB_ITEM = ReverieDreamsPlugin.key("bomb_item");
    public static final Key CROSSING_CHISEL = ReverieDreamsPlugin.key("crossing_chisel_item");
    public static final Key CURSED_DECOY_DOLL_ITEM = ReverieDreamsPlugin.key("cursed_decoy_doll_item");
    public static final Key DREAM_PILLOW_ITEM = ReverieDreamsPlugin.key("dream_pillow_item");
    public static final Key EXORCISM_PAPER_ITEM = ReverieDreamsPlugin.key("exorcism_paper_item");
    public static final Key FAST_RECIPE_BOOK = ReverieDreamsPlugin.key("fast_recipe_book");
    public static final Key FUMO_LICENSE_ITEM = ReverieDreamsPlugin.key("fumo_license_item");
    public static final Key GAP_BALL = ReverieDreamsPlugin.key("gap_ball");
    public static final Key HIMEKAIDOU_HATATES_PHONE = ReverieDreamsPlugin.key("himekaidou_hatates_phone");
    public static final Key KNIFE = ReverieDreamsPlugin.key("knife");
    public static final Key MUSICAL_INSTRUMENT_ITEM = ReverieDreamsPlugin.key("musical_instrument_item");
    public static final Key SATORI_EYE = ReverieDreamsPlugin.key("satori_eye");
    public static final Key SCARECROW_ITEM = ReverieDreamsPlugin.key("scarecrow_item");
    public static final Key SPEED_FEATHER_ITEM = ReverieDreamsPlugin.key("speed_feather_item");
    public static final Key SUNFLOWER = ReverieDreamsPlugin.key("sunflower");
    public static final Key TENGU_CAMERA_ITEM = ReverieDreamsPlugin.key("tengu_camera_item");
    public static final Key TENGU_SHIELD_ITEM = ReverieDreamsPlugin.key("tengu_shield_item");
    public static final Key TIME_STOP_CLOCK = ReverieDreamsPlugin.key("time_stop_clock");
    public static final Key UPGRADED_HEALTH_ITEM = ReverieDreamsPlugin.key("upgraded_health_item");
    public static final Key VAISRAVANAS_PAGODA_ITEM = ReverieDreamsPlugin.key("vaisravanas_pagoda_item");
    public static final Key YIN_YANG_ORB_ITEM = ReverieDreamsPlugin.key("yin_yang_orb_item");
    // Template
    public static final Key DANMAKU_SHAPE_CREATOR_ITEM = ReverieDreamsPlugin.key("danmaku_shape_creator_item");
    public static final Key ROLE_CARD_ITEM = ReverieDreamsPlugin.key("role_card_item");
    public static final Key ROLE_FOLLOWER_ARCHIVE_ITEM = ReverieDreamsPlugin.key("role_follower_archive_item");
    public static final Key SPELL_CARD_TEMPLATE_ITEM = ReverieDreamsPlugin.key("spell_card_template_item");
    // Weapon
    public static final Key BAGUA_FURNACE = ReverieDreamsPlugin.key("bagua_furnace_item");
    public static final Key DEATH_SCYTHE_ITEM = ReverieDreamsPlugin.key("death_scythe_item");
    public static final Key GUNGNIR = ReverieDreamsPlugin.key("gungnir_item");
    public static final Key HAKUREI_CANE = ReverieDreamsPlugin.key("hakurei_cane_item");
    public static final Key HAKUROKEN = ReverieDreamsPlugin.key("hakuroken_item");
    public static final Key HORAI_DAMA_NO_EDA_ITEM = ReverieDreamsPlugin.key("horai_dama_no_eda_item");
    public static final Key IBUKIHO = ReverieDreamsPlugin.key("ibukiho_item");
    public static final Key IRON_BAR_ITEM = ReverieDreamsPlugin.key("iron_bar_item");
    public static final Key LEVATIN = ReverieDreamsPlugin.key("levatin_item");
    public static final Key MAGIC_BROOM = ReverieDreamsPlugin.key("magic_broom_item");
    public static final Key MANPOZUCHI_ITEM = ReverieDreamsPlugin.key("manpozuchi_item");
    public static final Key MAPLE_LEAF_FAN = ReverieDreamsPlugin.key("maple_leaf_fan_item");
    public static final Key NUE_TRIDENT = ReverieDreamsPlugin.key("nue_trident_item");
    public static final Key PAPILIO_PATTERN_FAN = ReverieDreamsPlugin.key("papilio_pattern_fan_item");
    public static final Key ROKANKEN = ReverieDreamsPlugin.key("rokanken_item");
    public static final Key SWORD_OF_HISOU = ReverieDreamsPlugin.key("sword_of_hisou_item");
    public static final Key TREASURE_HUNTING_ROD = ReverieDreamsPlugin.key("treasure_hunting_rod_item");
    public static final Key TRUMPET_GUN = ReverieDreamsPlugin.key("trumpet_gun_item");
    public static final Key WEAPON_OF_THE_MOON = ReverieDreamsPlugin.key("weapon_of_the_moon_item");
    public static final Key WIND_BLESSING_CANE = ReverieDreamsPlugin.key("wind_blessing_cane_item");
    public static final Key YOUMU_SWORD_USING = ReverieDreamsPlugin.key("youmu_sword_using_item");
    public static final Key YUKA_FLOWER_UMBRELLA = ReverieDreamsPlugin.key("yuka_flower_umbrella_item");

    public static void initialize() {
        ItemBehaviors.register(CROWN_OF_THE_UNDER_WORLD_ARMOR, CrownOfTheUnderworldItem.FACTORY);
        ItemBehaviors.register(DREAM_ARMOR, DreamArmorItem.FACTORY);
        ItemBehaviors.register(EARPHONE_ARMOR, EarphoneItem.FACTORY);
        ItemBehaviors.register(KOISHI_HAT_ARMOR, KoishiHatItem.FACTORY);
        ItemBehaviors.register(LOW_GRAVITY_BOOT_ARMOR, LowGravityBootItem.FACTORY);
        ItemBehaviors.register(WATERPROOF_ARMOR, WaterproofArmor.FACTORY);
        ItemBehaviors.register(SILVER_ARMOR, SilverArmor.FACTORY);
    }
}
