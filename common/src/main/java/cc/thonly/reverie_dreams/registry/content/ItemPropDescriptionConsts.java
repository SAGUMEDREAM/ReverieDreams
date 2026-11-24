package cc.thonly.reverie_dreams.registry.content;

import cc.thonly.reverie_dreams.component.tooltip.InitTooltips;
import cc.thonly.reverie_dreams.registry.content.item.RDItems;
import cc.thonly.reverie_dreams.registry.content.block.RDBlocks;
import cc.thonly.reverie_dreams.registry.content.block.RDJapaneseLivingBlocks;
import cc.thonly.reverie_dreams.registry.content.block.RDJapaneseShrineBlocks;
import cc.thonly.reverie_dreams.registry.content.block.RDJapaneseSet2Blocks;
import cc.thonly.reverie_dreams.block.base.BaseFumoBlock;
import net.minecraft.world.level.ItemLike;

import java.util.List;

public class ItemPropDescriptionConsts {

    public static final String GUIDEBOOK_DESCRIPTION_0 = "item.reverie_dreams.guidebook.description.0";

    public static final String UPGRADED_HEALTH_DESCRIPTION_0 = "item.reverie_dreams.upgraded_health.description.0";

    public static final String BOMB_DESCRIPTION_0 = "item.reverie_dreams.bomb.description.0";
    public static final String BOMB_DESCRIPTION_1 = "item.reverie_dreams.bomb.description.1";

    public static final String YIN_YANG_ORB_DESCRIPTION_0 = "item.reverie_dreams.yin-yang_orb.description.0";
    public static final String YIN_YANG_ORB_DESCRIPTION_1 = "item.reverie_dreams.yin-yang_orb.description.1";

    public static final String SPEED_FEATHER_DESCRIPTION_0 = "item.reverie_dreams.speed_feather.description.0";

    public static final String CROSSING_CHISEL_DESCRIPTION_0 = "item.reverie_dreams.crossing_chisel.description.0";
    public static final String CROSSING_CHISEL_DESCRIPTION_1 = "item.reverie_dreams.crossing_chisel.description.1";

    public static final String GAP_BALL_DESCRIPTION_0 = "item.reverie_dreams.gap_ball.description.0";
    public static final String GAP_BALL_DESCRIPTION_1 = "item.reverie_dreams.gap_ball.description.1";

    public static final String BOUNDARY_OFUDA_DESCRIPTION_0 = "item.reverie_dreams.boundary_ofuda.description.0";
    public static final String BOUNDARY_OFUDA_DESCRIPTION_1 = "item.reverie_dreams.boundary_ofuda.description.1";
    public static final String BOUNDARY_OFUDA_DESCRIPTION_2 = "item.reverie_dreams.boundary_ofuda.description.2";

    public static final String TIME_STOP_CLOCK_DESCRIPTION_0 = "item.reverie_dreams.time_stop_clock.description.0";
    public static final String TIME_STOP_CLOCK_DESCRIPTION_1 = "item.reverie_dreams.time_stop_clock.description.1";
    public static final String TIME_STOP_CLOCK_DESCRIPTION_2 = "item.reverie_dreams.time_stop_clock.description.2";

    public static final String FUMO_LICENSE_DESCRIPTION_0 = "item.reverie_dreams.fumo_license.description.0";
    public static final String FUMO_LICENSE_DESCRIPTION_1 = "item.reverie_dreams.fumo_license.description.1";

    public static final String CURSED_DECOY_DOLL_DESCRIPTION_0 = "item.reverie_dreams.cursed_decoy_doll.description.0";
    public static final String CURSED_DECOY_DOLL_DESCRIPTION_1 = "item.reverie_dreams.cursed_decoy_doll.description.1";

    public static final String VAISRAVANAS_PAGODA_DESCRIPTION_0 = "item.reverie_dreams.vaisravanas_pagoda.description";
    public static final String VAISRAVANAS_PAGODA_DESCRIPTION_1 = "item.reverie_dreams.vaisravanas_pagoda.description.1";
    public static final String VAISRAVANAS_PAGODA_DESCRIPTION_2 = "item.reverie_dreams.vaisravanas_pagoda.description.2";

    public static final String DREAM_PILLOW_DESCRIPTION_0 = "item.reverie_dreams.dream_pillow.description.0";
    public static final String DREAM_PILLOW_DESCRIPTION_1 = "item.reverie_dreams.dream_pillow.description.1";
    public static final String DREAM_MIRROR_DESCRIPTION = "item.reverie_dreams.dream_mirror.description";
    public static final String DREAM_COCOON_DESCRIPTION = "item.reverie_dreams.dream_cocoon.description";
    public static final String DREAM_SPOOL_DESCRIPTION = "item.reverie_dreams.dream_spool.description";
    public static final String FOOTSTEP_MOSS_DESCRIPTION = "block.reverie_dreams.footstep_moss.description";

    public static final String TENGU_SHIELD_DESCRIPTION_0 = "item.reverie_dreams.tengu_shield.description.0";

    public static final String TENGU_CAMERA_DESCRIPTION_0 = "item.reverie_dreams.tengu_camera.description.0";
    public static final String TENGU_CAMERA_DESCRIPTION_1 = "item.reverie_dreams.tengu_camera.description.1";

    public static final String HIMEKAIDOU_HATATES_PHONE_DESCRIPTION_0 = "item.reverie_dreams.himekaidou_hatates_phone.description.0";
    public static final String HIMEKAIDOU_HATATES_PHONE_DESCRIPTION_1 = "item.reverie_dreams.himekaidou_hatates_phone.description.1";

    public static final String BAD_APPLE_DESCRIPTION_0 = "item.reverie_dreams.bad_apple.description.0";

    public static final String SCARECROW_DESCRIPTION_0 = "item.reverie_dreams.scarecrow.description.0";
    public static final String SCARECROW_DESCRIPTION_1 = "item.reverie_dreams.scarecrow.description.1";

    public static final String EXORCISM_PAPER_DESCRIPTION_0 = "item.reverie_dreams.exorcism_paper.description";
    public static final String EXORCISM_PAPER_DESCRIPTION_1 = "item.reverie_dreams.exorcism_paper.description.1";
    public static final String EXORCISM_PAPER_DESCRIPTION_2 = "item.reverie_dreams.exorcism_paper.description.2";

    public static final String SPELLCARD_DESCRIPTION_0 = "item.reverie_dreams.spellcard.description.0";

    public static final String SATORI_EYE_DESCRIPTION_0 = "item.reverie_dreams.satori_eye.description.0";
    public static final String SATORI_EYE_DESCRIPTION_1 = "item.reverie_dreams.satori_eye.description.1";

    public static final String WEAPON_OF_THE_MOON_DESCRIPTION_0 = "item.reverie_dreams.weapon_of_the_moon.description";
    public static final String WEAPON_OF_THE_MOON_DESCRIPTION_1 = "item.reverie_dreams.weapon_of_the_moon.description.1";

    public static final String MAGAZINE_DESCRIPTION_0 = "item.reverie_dreams.magazine.description";

    public static final String FAST_RECIPE_BOOK_DESCRIPTION_0 = "item.reverie_dreams.fast_book_item.description.0";
    public static final String FAST_RECIPE_BOOK_DESCRIPTION_1 = "item.reverie_dreams.fast_book_item.description.1";
    public static final String FAST_RECIPE_BOOK_DESCRIPTION_2 = "item.reverie_dreams.fast_book_item.description.2";

    public static final String SUNFLOWER_DESCRIPTION_0 = "item.reverie_dreams.sunflower.description.0";
    public static final String SUNFLOWER_DESCRIPTION_1 = "item.reverie_dreams.sunflower.description.1";

    public static final String CHEQUE_DESCRIPTION_0 = "item.reverie_dreams.cheque.description.0";
    public static final String CHEQUE_DESCRIPTION_1 = "item.reverie_dreams.cheque.description.1";

    public static final String EARPHONE_DESCRIPTION_0 = "item.reverie_dreams.earphone.description.0";

    public static final String KOISHI_HAT_DESCRIPTION_0 = "item.reverie_dreams.koishi_hat.description.0";

    public static final String ONI_MASK_DESCRIPTION_0 = "item.reverie_dreams.oni_mask.description.0";
    public static final String ONI_MASK_DESCRIPTION_1 = "item.reverie_dreams.oni_mask.description.1";

    public static final String LOW_GRAVITY_BOOT_DESCRIPTION_0 = "item.reverie_dreams.low_gravity_boot.description.0";

    public static final String CROWN_OF_THE_UNDERWORLD_DESCRIPTION_0 = "item.reverie_dreams.crown_of_the_underworld.description.0";
    public static final String CROWN_OF_THE_UNDERWORLD_DESCRIPTION_1 = "item.reverie_dreams.crown_of_the_underworld.description.1";

    public static final String DREAM_ARMOR_DESCRIPTION_0 = "item.reverie_dreams.dream_armor.description.0";

    public static final String WATERPROOF_ARMOR_DESCRIPTION_0 = "item.reverie_dreams.waterproof_armor.description.0";

    public static final String HAKUREI_CANE_DESCRIPTION_0 = "item.reverie_dreams.hakurei_cane.description.0";

    public static final String BAGUA_FURNACE_DESCRIPTION_0 = "item.reverie_dreams.bagua_furnace.description.0";
    public static final String BAGUA_FURNACE_DESCRIPTION_1 = "item.reverie_dreams.bagua_furnace.description.1";

    public static final String WIND_BLESSING_CANE_DESCRIPTION_0 = "item.reverie_dreams.wind_blessing_cane.description.0";
    public static final String WIND_BLESSING_CANE_DESCRIPTION_1 = "item.reverie_dreams.wind_blessing_cane.description.1";
    public static final String WIND_BLESSING_CANE_DESCRIPTION_2 = "item.reverie_dreams.wind_blessing_cane.description.2";

    public static final String MAGIC_BROOM_DESCRIPTION_0 = "item.reverie_dreams.magic_broom.description.0";
    public static final String MAGIC_BROOM_DESCRIPTION_1 = "item.reverie_dreams.magic_broom.description.1";

    public static final String GUNGNIR_DESCRIPTION_0 = "item.reverie_dreams.gungnir.description.0";

    public static final String LEVATIN_DESCRIPTION_0 = "item.reverie_dreams.levatin.description.0";
    public static final String LEVATIN_TOGGLE_HINT = "item.reverie_dreams.levatin.toggle_hint";

    public static final String ROKANKEN_DESCRIPTION_0 = "item.reverie_dreams.rokanken.description.0";
    public static final String ROKANKEN_DESCRIPTION_1 = "item.reverie_dreams.rokanken.description.1";

    public static final String HAKUROKEN_DESCRIPTION_0 = "item.reverie_dreams.hakuroken.description.0";
    public static final String HAKUROKEN_DESCRIPTION_1 = "item.reverie_dreams.hakuroken.description.1";

    public static final String PAPILIO_PATTERN_FAN_DESCRIPTION_0 = "item.reverie_dreams.papilio_pattern_fan.description.0";

    public static final String HORAI_DAMA_NO_EDA_DESCRIPTION_0 = "item.reverie_dreams.horai-dama_no_eda.description.0";
    public static final String HORAI_DAMA_NO_EDA_DESCRIPTION_1 = "item.reverie_dreams.horai-dama_no_eda.description.1";

    public static final String YUKA_FLOWER_UMBRELLA_DESCRIPTION_0 = "item.reverie_dreams.yuka_flower_umbrella.description";
    public static final String YUKA_FLOWER_UMBRELLA_DESCRIPTION_1 = "item.reverie_dreams.yuka_flower_umbrella.description.1";

    public static final String MAPLE_LEAF_FAN_DESCRIPTION_0 = "item.reverie_dreams.maple_leaf_fan.description.0";

    public static final String IBUKIHO_DESCRIPTION_0 = "item.reverie_dreams.ibukiho.description.0";
    public static final String IBUKIHO_DESCRIPTION_1 = "item.reverie_dreams.ibukiho.description.1";

    public static final String SWORD_OF_HISOU_DESCRIPTION_0 = "item.reverie_dreams.sword_of_hisou.description.0";
    public static final String SWORD_OF_HISOU_DESCRIPTION_1 = "item.reverie_dreams.sword_of_hisou.description.1";

    public static final String MANPOZUCHI_DESCRIPTION_0 = "item.reverie_dreams.manpozuchi.description.0";
    public static final String MANPOZUCHI_DESCRIPTION_1 = "item.reverie_dreams.manpozuchi.description.1";
    public static final String MANPOZUCHI_DESCRIPTION_2 = "item.reverie_dreams.manpozuchi.description.2";

    public static final String NUE_TRIDENT_DESCRIPTION_0 = "item.reverie_dreams.nue_trident.description.0";

    public static final String TRUMPET_GUN_DESCRIPTION_0 = "item.reverie_dreams.trumpet_gun.description.0";

    public static final String TREASURE_HUNTING_ROD_DESCRIPTION_0 = "item.reverie_dreams.treasure_hunting_rod.description";
    public static final String TREASURE_HUNTING_ROD_DESCRIPTION_1 = "item.reverie_dreams.treasure_hunting_rod.description.1";

    public static final String DEATH_SCYTHE_DESCRIPTION_0 = "item.reverie_dreams.death_scythe.description.0";

    public static final String KNIFE_DESCRIPTION_0 = "item.reverie_dreams.knife.description.0";

    public static final String IRON_BAR_DESCRIPTION_0 = "item.reverie_dreams.iron_bar.description.0";

    public static final String ONI_IRON_BAR_DESCRIPTION_0 = "item.reverie_dreams.oni_iron_bar.description.0";
    public static final String ONI_IRON_BAR_DESCRIPTION_1 = "item.reverie_dreams.oni_iron_bar.description.1";

    public static final String MUSICAL_INSTRUMENT_DESCRIPTION_0 = "item.reverie_dreams.musical_instrument.description.0";
    public static final String MUSICAL_INSTRUMENT_DESCRIPTION_1 = "item.reverie_dreams.musical_instrument.description.1";

    public static final String DANMAKU_SHAPE_CREATOR_DESCRIPTION_0 = "item.reverie_dreams.danmaku_shape_blueprint.description.0";

    public static final String ROLE_CARD_DESCRIPTION_0 = "item.reverie_dreams.role_card.description.0";

    public static final String ROLE_ARCHIVE_DESCRIPTION_0 = "item.reverie_dreams.role_archive.description.0";
    public static final String ROLE_ARCHIVE_DESCRIPTION_1 = "item.reverie_dreams.role_archive.description.1";

    public static final String SOUL_CARD_DESCRIPTION_0 = "item.reverie_dreams.soul_card.description.0";
    public static final String SOUL_CARD_DESCRIPTION_1 = "item.reverie_dreams.soul_card.description.1";


    public static final String DECOR_SEAT = "item.reverie_dreams.decoration.seat";
    public static final String DECOR_BENCH_CONNECT = "item.reverie_dreams.decoration.bench_connect";
    public static final String DECOR_STORAGE = "item.reverie_dreams.decoration.storage";
    public static final String DECOR_INSERT_ONE = "item.reverie_dreams.decoration.insert_one";
    public static final String DECOR_SORT = "item.reverie_dreams.decoration.sort";
    public static final String DECOR_BOOK_STORAGE = "item.reverie_dreams.decoration.book_storage";
    public static final String DECOR_TATAMI = "item.reverie_dreams.decoration.tatami";
    public static final String DECOR_TABLE_PLACE = "item.reverie_dreams.decoration.table_place";
    public static final String DECOR_TABLE_REMOVE = "item.reverie_dreams.decoration.table_remove";
    public static final String DECOR_BOARD_IDLE = "item.reverie_dreams.decoration.board_idle";
    public static final String DECOR_BOARD_PLAY = "item.reverie_dreams.decoration.board_play";
    public static final String DECOR_BOARD_PLACE = "item.reverie_dreams.decoration.board_place";
    public static final String DECOR_TEA = "item.reverie_dreams.decoration.tea";
    public static final String DECOR_SHRINE_BELL = "item.reverie_dreams.decoration.shrine_bell";
    public static final String DECOR_ROPE = "item.reverie_dreams.decoration.rope";
    public static final String DECOR_CHIME = "item.reverie_dreams.decoration.chime";
    public static final String DECOR_FUTON = "item.reverie_dreams.decoration.futon";
    public static final String DECOR_HEARTH_LIGHT = "item.reverie_dreams.decoration.hearth_light";
    public static final String DECOR_HEARTH_OUT = "item.reverie_dreams.decoration.hearth_out";
    public static final String DECOR_DOC_WRITE = "item.reverie_dreams.decoration.doc_write";
    public static final String DECOR_INK = "item.reverie_dreams.decoration.ink";
    public static final String DECOR_DESK_ACCESSORY = "item.reverie_dreams.decoration.desk_accessory";
    public static final String DECOR_DESK_REMOVE = "item.reverie_dreams.decoration.desk_remove";
    public static final String DECOR_DESK_DRAWER = "item.reverie_dreams.decoration.desk_drawer";
    public static final String DECOR_DESK_WRITE = "item.reverie_dreams.decoration.desk_write";
    public static final String DECOR_RECORD_PLAYER = "item.reverie_dreams.decoration.record_player";
    public static final String DECOR_RADIO_NETWORK = "item.reverie_dreams.decoration.radio_network";
    public static final String DECOR_RACK_JOIN = "item.reverie_dreams.decoration.rack_join";
    public static final String DECOR_RACK_TAKE = "item.reverie_dreams.decoration.rack_take";
    public static final String DECOR_EMA = "item.reverie_dreams.decoration.ema";
    public static final String DECOR_OFUDA = "item.reverie_dreams.decoration.ofuda";
    public static final String DECOR_READ_TAKE = "item.reverie_dreams.decoration.read_take";
    public static final String DECOR_BOOK_STACK = "item.reverie_dreams.decoration.book_stack";
    public static final String DECOR_YAGEN_INSERT = "item.reverie_dreams.decoration.yagen_insert";
    public static final String DECOR_YAGEN_WORK = "item.reverie_dreams.decoration.yagen_work";
    public static final String DECOR_WORKSTATION = "item.reverie_dreams.decoration.workstation";
    public static final String DECOR_DRYING = "item.reverie_dreams.decoration.drying";
    public static final String DECOR_DRYING_ENV = "item.reverie_dreams.decoration.drying_env";
    public static final String DECOR_DRYING_VIEW = "item.reverie_dreams.decoration.drying_view";
    public static final String DECOR_DRYING_TAKE = "item.reverie_dreams.decoration.drying_take";
    public static final String DECOR_FLOWER = "item.reverie_dreams.decoration.flower";
    public static final String DECOR_SAKE = "item.reverie_dreams.decoration.sake";
    public static final String DECOR_SAKE_STORAGE = "item.reverie_dreams.decoration.sake_storage";
    public static final String DECOR_COUNTER = "item.reverie_dreams.decoration.counter";
    public static final String DECOR_COUNTER_CONNECT = "item.reverie_dreams.decoration.counter_connect";
    public static final String DECOR_RAKE_CREATE = "item.reverie_dreams.decoration.rake_create";
    public static final String DECOR_RAKE_ROTATE = "item.reverie_dreams.decoration.rake_rotate";
    public static final String DECOR_RAKE_RESTORE = "item.reverie_dreams.decoration.rake_restore";
    public static final String DECOR_RAKE_AUTO = "item.reverie_dreams.decoration.rake_auto";
    public static final String DECOR_MUSIC = "item.reverie_dreams.decoration.music";
    public static final String DECOR_MUSIC_REDSTONE = "item.reverie_dreams.decoration.music_redstone";
    public static final String DECOR_CASH = "item.reverie_dreams.decoration.cash";
    public static final String DECOR_CASH_OPEN = "item.reverie_dreams.decoration.cash_open";
    public static final String DECOR_SAISEN_DEPOSIT = "item.reverie_dreams.decoration.saisen_deposit";
    public static final String DECOR_SAISEN_SIGNAL = "item.reverie_dreams.decoration.saisen_signal";
    public static final String DECOR_WHEEL = "item.reverie_dreams.decoration.wheel";
    public static final String DECOR_FUMO_TURN = "item.reverie_dreams.decoration.fumo_turn";
    public static final String DECOR_FUMO_CAKE = "item.reverie_dreams.decoration.fumo_cake";
    public static final String DECOR_PLATE_PLACE = "item.reverie_dreams.decoration.plate_place";
    public static final String DECOR_PLATE_EAT = "item.reverie_dreams.decoration.plate_eat";
    public static final String DECOR_BELL_BUSINESS = "item.reverie_dreams.decoration.bell_business";
    public static final String DECOR_MENU = "item.reverie_dreams.decoration.menu";
    public static final String DECOR_BELL_OWNER = "item.reverie_dreams.decoration.bell_owner";
    public static final String DECOR_HOOK = "item.reverie_dreams.decoration.hook";
    public static final String DECOR_INK_STORAGE = "item.reverie_dreams.decoration.ink_storage";

    public static void addItemForDetail(ItemLike item, String component) {
        InitTooltips.addItemForDetail(item, component);
    }

    public static void addItemForDetail(ItemLike item, List<String> components) {
        InitTooltips.addItemForDetail(item, components);
    }

    public static void initialize() {
        addItemForDetail(RDItems.GUIDEBOOK.asItem(), GUIDEBOOK_DESCRIPTION_0);
        addItemForDetail(RDItems.UPGRADED_HEALTH.asItem(), UPGRADED_HEALTH_DESCRIPTION_0);
        addItemForDetail(RDItems.BOMB.asItem(), List.of(
                BOMB_DESCRIPTION_0,
                BOMB_DESCRIPTION_1
        ));
        addItemForDetail(RDItems.YIN_YANG_ORB.asItem(), List.of(
                YIN_YANG_ORB_DESCRIPTION_0,
                YIN_YANG_ORB_DESCRIPTION_1
        ));
        addItemForDetail(RDItems.SPEED_FEATHER.asItem(), SPEED_FEATHER_DESCRIPTION_0);
        addItemForDetail(RDItems.CROSSING_CHISEL.asItem(), List.of(
                CROSSING_CHISEL_DESCRIPTION_0,
                CROSSING_CHISEL_DESCRIPTION_1
        ));
        addItemForDetail(RDItems.GAP_BALL.asItem(), List.of(
                GAP_BALL_DESCRIPTION_0,
                GAP_BALL_DESCRIPTION_1
        ));
        addItemForDetail(RDItems.BOUNDARY_OFUDA.asItem(), List.of(
                BOUNDARY_OFUDA_DESCRIPTION_0,
                BOUNDARY_OFUDA_DESCRIPTION_1,
                BOUNDARY_OFUDA_DESCRIPTION_2
        ));
        addItemForDetail(RDItems.TIME_STOP_CLOCK.asItem(), List.of(
                TIME_STOP_CLOCK_DESCRIPTION_0,
                TIME_STOP_CLOCK_DESCRIPTION_1,
                TIME_STOP_CLOCK_DESCRIPTION_2
        ));
        addItemForDetail(RDItems.FUMO_LICENSE.asItem(), List.of(
                FUMO_LICENSE_DESCRIPTION_0,
                FUMO_LICENSE_DESCRIPTION_1
        ));
        addItemForDetail(RDItems.CURSED_DECOY_DOLL.asItem(), List.of(
                CURSED_DECOY_DOLL_DESCRIPTION_0,
                CURSED_DECOY_DOLL_DESCRIPTION_1
        ));
        addItemForDetail(RDItems.VAISRAVANAS_PAGODA.asItem(), List.of(
                VAISRAVANAS_PAGODA_DESCRIPTION_0,
                VAISRAVANAS_PAGODA_DESCRIPTION_1,
                VAISRAVANAS_PAGODA_DESCRIPTION_2
        ));
        addItemForDetail(RDItems.DREAM_MIRROR.asItem(), DREAM_MIRROR_DESCRIPTION);
        addItemForDetail(RDItems.DREAM_COCOON.asItem(), DREAM_COCOON_DESCRIPTION);
        addItemForDetail(RDItems.DREAM_SPOOL.asItem(), DREAM_SPOOL_DESCRIPTION);
        addItemForDetail(RDBlocks.FOOTSTEP_MOSS.asItem(), FOOTSTEP_MOSS_DESCRIPTION);
        addItemForDetail(RDItems.DREAM_PILLOW.asItem(), List.of(
                DREAM_PILLOW_DESCRIPTION_0,
                DREAM_PILLOW_DESCRIPTION_1
        ));
        addItemForDetail(RDItems.TENGU_SHIELD.asItem(), TENGU_SHIELD_DESCRIPTION_0);
        addItemForDetail(RDItems.TENGU_CAMERA.asItem(), List.of(
                TENGU_CAMERA_DESCRIPTION_0,
                TENGU_CAMERA_DESCRIPTION_1
        ));
        addItemForDetail(RDItems.HIMEKAIDOU_HATATES_PHONE.asItem(), List.of(
                HIMEKAIDOU_HATATES_PHONE_DESCRIPTION_0,
                HIMEKAIDOU_HATATES_PHONE_DESCRIPTION_1
        ));
        addItemForDetail(RDItems.BAD_APPLE.asItem(), BAD_APPLE_DESCRIPTION_0);
        addItemForDetail(RDItems.SCARECROW.asItem(), List.of(
                SCARECROW_DESCRIPTION_0,
                SCARECROW_DESCRIPTION_1
        ));
        addItemForDetail(RDItems.EXORCISM_PAPER.asItem(), List.of(
                EXORCISM_PAPER_DESCRIPTION_0,
                EXORCISM_PAPER_DESCRIPTION_1,
                EXORCISM_PAPER_DESCRIPTION_2
        ));
        addItemForDetail(RDItems.SPELLCARD.asItem(), SPELLCARD_DESCRIPTION_0);
        addItemForDetail(RDItems.SATORI_EYE.asItem(), List.of(
                SATORI_EYE_DESCRIPTION_0,
                SATORI_EYE_DESCRIPTION_1
        ));
        for (var weapon : RDItems.LUNAR_WEAPONS) {
            String path = weapon.unwrapKey().orElseThrow().identifier().getPath();
            addItemForDetail(weapon.asItem(), List.of("item.reverie_dreams." + path + ".description",
                    "item.reverie_dreams.lunar_weapon.controls"));
        }
        for (var magazine : RDItems.LUNAR_MAGAZINES) {
            String path = magazine.unwrapKey().orElseThrow().identifier().getPath();
            addItemForDetail(magazine.asItem(), "item.reverie_dreams." + path + ".description");
        }
        addItemForDetail(RDItems.FAST_RECIPE_BOOK.asItem(), List.of(
                FAST_RECIPE_BOOK_DESCRIPTION_0,
                FAST_RECIPE_BOOK_DESCRIPTION_1,
                FAST_RECIPE_BOOK_DESCRIPTION_2
        ));
        addItemForDetail(RDItems.SUNFLOWER.asItem(), List.of(
                SUNFLOWER_DESCRIPTION_0,
                SUNFLOWER_DESCRIPTION_1
        ));
        addItemForDetail(RDItems.CHEQUE.asItem(), List.of(
                CHEQUE_DESCRIPTION_0,
                CHEQUE_DESCRIPTION_1
        ));
        addItemForDetail(RDItems.EARPHONE.asItem(), EARPHONE_DESCRIPTION_0);
        addItemForDetail(RDItems.KOISHI_HAT.asItem(), KOISHI_HAT_DESCRIPTION_0);
        addItemForDetail(RDItems.ONI_MASK.asItem(), List.of(
                ONI_MASK_DESCRIPTION_0,
                ONI_MASK_DESCRIPTION_1
        ));
        addItemForDetail(RDItems.LOW_GRAVITY_BOOT.asItem(), LOW_GRAVITY_BOOT_DESCRIPTION_0);
        addItemForDetail(RDItems.CROWN_OF_THE_UNDERWORLD.asItem(), List.of(
                CROWN_OF_THE_UNDERWORLD_DESCRIPTION_0,
                CROWN_OF_THE_UNDERWORLD_DESCRIPTION_1
        ));
        addItemForDetail(RDItems.DREAM_HELMET.asItem(), DREAM_ARMOR_DESCRIPTION_0);
        addItemForDetail(RDItems.DREAM_CHESTPLATE.asItem(), DREAM_ARMOR_DESCRIPTION_0);
        addItemForDetail(RDItems.DREAM_LEGGINGS.asItem(), DREAM_ARMOR_DESCRIPTION_0);
        addItemForDetail(RDItems.DREAM_BOOTS.asItem(), DREAM_ARMOR_DESCRIPTION_0);
        addItemForDetail(RDItems.WATER_PROOF_HAT.asItem(), WATERPROOF_ARMOR_DESCRIPTION_0);
        addItemForDetail(RDItems.WATER_PROOF_CLOTHING.asItem(), WATERPROOF_ARMOR_DESCRIPTION_0);
        addItemForDetail(RDItems.WATER_PROOF_LEGGINGS.asItem(), WATERPROOF_ARMOR_DESCRIPTION_0);
        addItemForDetail(RDItems.WATER_PROOF_BOOTS.asItem(), WATERPROOF_ARMOR_DESCRIPTION_0);
        addItemForDetail(RDItems.HAKUREI_CANE.asItem(), HAKUREI_CANE_DESCRIPTION_0);
        addItemForDetail(RDItems.BAGUA_FURNACE.asItem(), List.of(
                BAGUA_FURNACE_DESCRIPTION_0,
                BAGUA_FURNACE_DESCRIPTION_1
        ));
        addItemForDetail(RDItems.WIND_BLESSING_CANE.asItem(), List.of(
                WIND_BLESSING_CANE_DESCRIPTION_0,
                WIND_BLESSING_CANE_DESCRIPTION_1,
                WIND_BLESSING_CANE_DESCRIPTION_2
        ));
        addItemForDetail(RDItems.MAGIC_BROOM.asItem(), List.of(
                MAGIC_BROOM_DESCRIPTION_0,
                MAGIC_BROOM_DESCRIPTION_1
        ));
        addItemForDetail(RDItems.GUNGNIR.asItem(), GUNGNIR_DESCRIPTION_0);
        addItemForDetail(RDItems.LEVATIN.asItem(), List.of(LEVATIN_DESCRIPTION_0, LEVATIN_TOGGLE_HINT));
        addItemForDetail(RDItems.ROKANKEN.asItem(), List.of(
                ROKANKEN_DESCRIPTION_0,
                ROKANKEN_DESCRIPTION_1
        ));
        addItemForDetail(RDItems.HAKUROKEN.asItem(), List.of(HAKUROKEN_DESCRIPTION_0, HAKUROKEN_DESCRIPTION_1));
        addItemForDetail(RDItems.PAPILIO_PATTERN_FAN.asItem(), PAPILIO_PATTERN_FAN_DESCRIPTION_0);
        addItemForDetail(RDItems.HORAI_DAMA_NO_EDA.asItem(), List.of(
                HORAI_DAMA_NO_EDA_DESCRIPTION_0,
                HORAI_DAMA_NO_EDA_DESCRIPTION_1
        ));
        addItemForDetail(RDItems.YUKA_FLOWER_UMBRELLA.asItem(), List.of(
                YUKA_FLOWER_UMBRELLA_DESCRIPTION_0,
                YUKA_FLOWER_UMBRELLA_DESCRIPTION_1
        ));
        addItemForDetail(RDItems.MAPLE_LEAF_FAN.asItem(), MAPLE_LEAF_FAN_DESCRIPTION_0);
        addItemForDetail(RDItems.IBUKIHO.asItem(), List.of(
                IBUKIHO_DESCRIPTION_0,
                IBUKIHO_DESCRIPTION_1
        ));
        addItemForDetail(RDItems.SWORD_OF_HISOU.asItem(), List.of(
                SWORD_OF_HISOU_DESCRIPTION_0,
                SWORD_OF_HISOU_DESCRIPTION_1
        ));
        addItemForDetail(RDItems.MANPOZUCHI.asItem(), List.of(
                MANPOZUCHI_DESCRIPTION_0,
                MANPOZUCHI_DESCRIPTION_1,
                MANPOZUCHI_DESCRIPTION_2
        ));
        addItemForDetail(RDItems.NUE_TRIDENT.asItem(), NUE_TRIDENT_DESCRIPTION_0);
        addItemForDetail(RDItems.TRUMPET_GUN.asItem(), TRUMPET_GUN_DESCRIPTION_0);
        addItemForDetail(RDItems.TREASURE_HUNTING_ROD.asItem(), List.of(
                TREASURE_HUNTING_ROD_DESCRIPTION_0,
                TREASURE_HUNTING_ROD_DESCRIPTION_1
        ));
        addItemForDetail(RDItems.DEATH_SCYTHE.asItem(), DEATH_SCYTHE_DESCRIPTION_0);
        addItemForDetail(RDItems.KNIFE.asItem(), KNIFE_DESCRIPTION_0);
        addItemForDetail(RDItems.IRON_BAR.asItem(), IRON_BAR_DESCRIPTION_0);
        addItemForDetail(RDItems.ONI_IRON_BAR.asItem(), List.of(
                ONI_IRON_BAR_DESCRIPTION_0,
                ONI_IRON_BAR_DESCRIPTION_1
        ));
        addItemForDetail(RDItems.VIOLIN.asItem(), List.of(
                MUSICAL_INSTRUMENT_DESCRIPTION_0,
                MUSICAL_INSTRUMENT_DESCRIPTION_1
        ));
        addItemForDetail(RDItems.KEYBOARD.asItem(), List.of(
                MUSICAL_INSTRUMENT_DESCRIPTION_0,
                MUSICAL_INSTRUMENT_DESCRIPTION_1
        ));
        addItemForDetail(RDItems.TRUMPET.asItem(), List.of(
                MUSICAL_INSTRUMENT_DESCRIPTION_0,
                MUSICAL_INSTRUMENT_DESCRIPTION_1
        ));
        addItemForDetail(RDItems.DANMAKU_SHAPE_CREATOR.asItem(), DANMAKU_SHAPE_CREATOR_DESCRIPTION_0);
        addItemForDetail(RDItems.ROLE_CARD.asItem(), ROLE_CARD_DESCRIPTION_0);
        addItemForDetail(RDItems.ROLE_ARCHIVE.asItem(), List.of(
                ROLE_ARCHIVE_DESCRIPTION_0,
                ROLE_ARCHIVE_DESCRIPTION_1
        ));
        addItemForDetail(RDItems.SOUL_CARD.asItem(), List.of(
                SOUL_CARD_DESCRIPTION_0,
                SOUL_CARD_DESCRIPTION_1
        ));
        registerDecorationDetails();
    }

    private static void decorationDetails(List<String> details, ItemLike... items) {
        for (ItemLike item : items) addItemForDetail(item.asItem(), details);
    }

    private static void registerDecorationDetails() {
        decorationDetails(List.of(DECOR_RECORD_PLAYER, DECOR_RADIO_NETWORK), RDBlocks.RECORD_PLAYER);
        decorationDetails(List.of(DECOR_SEAT), RDBlocks.CHAIR, RDBlocks.ZABUTON);
        RDBlocks.ZABUTONS.values().forEach(block -> addItemForDetail(block.asItem(), DECOR_SEAT));
        decorationDetails(List.of(DECOR_STORAGE), RDBlocks.CUPBOARD, RDBlocks.WOODEN_BOX.chestBlock(),
                RDBlocks.SILVER_CHEST_BLOCK.chestBlock(), RDJapaneseLivingBlocks.OSHIIRE, RDJapaneseLivingBlocks.TANSU);
        decorationDetails(List.of(DECOR_TATAMI), RDBlocks.TATAMI, RDBlocks.TATAMI_BLOCK);
        decorationDetails(List.of(DECOR_TABLE_PLACE, DECOR_TABLE_REMOVE, DECOR_BOARD_IDLE, DECOR_BOARD_PLAY), RDBlocks.CHABUDAI);
        decorationDetails(List.of(DECOR_TEA, DECOR_TABLE_REMOVE), RDBlocks.TEA_SET);
        decorationDetails(List.of(DECOR_BOARD_PLACE, DECOR_BOARD_PLAY, DECOR_BOARD_IDLE), RDBlocks.XIANGQI_BOARD, RDBlocks.CHESS_BOARD, RDBlocks.GO_BOARD);
        decorationDetails(List.of(DECOR_SHRINE_BELL), RDJapaneseLivingBlocks.SHRINE_BELL);
        decorationDetails(List.of(DECOR_ROPE), RDJapaneseLivingBlocks.BELL_ROPE);
        decorationDetails(List.of(DECOR_CHIME), RDJapaneseLivingBlocks.WIND_CHIME);
        decorationDetails(List.of(DECOR_FUTON), RDJapaneseLivingBlocks.FUTON);
        decorationDetails(List.of(DECOR_HEARTH_LIGHT, DECOR_HEARTH_OUT), RDJapaneseLivingBlocks.IRORI);
        decorationDetails(List.of(DECOR_HOOK), RDJapaneseLivingBlocks.JIZAIKAGI);
        decorationDetails(List.of(DECOR_RAKE_CREATE, DECOR_RAKE_ROTATE, DECOR_RAKE_RESTORE, DECOR_RAKE_AUTO), RDBlocks.RAKED_GRAVEL);
        decorationDetails(List.of(DECOR_MUSIC, DECOR_MUSIC_REDSTONE), RDBlocks.MUSIC_BLOCK);
        decorationDetails(List.of(DECOR_CASH, DECOR_CASH_OPEN), RDBlocks.CASH_BOX_BLOCK);
        decorationDetails(List.of(DECOR_SAISEN_DEPOSIT, DECOR_SAISEN_SIGNAL), RDJapaneseShrineBlocks.SAISEN_BAKO);
        decorationDetails(List.of(DECOR_WHEEL), RDBlocks.WHEEL_CHAIR);
        decorationDetails(List.of(DECOR_PLATE_PLACE, DECOR_PLATE_EAT), RDBlocks.PLATE);
        decorationDetails(List.of(DECOR_BELL_BUSINESS, DECOR_BELL_OWNER), RDBlocks.MYSTIA_BELL);
        decorationDetails(List.of(DECOR_MENU), RDBlocks.MENU_BOOK);
        for (var entry : RDJapaneseSet2Blocks.ENTRIES.entrySet()) {
            List<String> details = switch (entry.getKey()) {
                case EMA_RACK -> List.of(DECOR_EMA, DECOR_RACK_JOIN, DECOR_RACK_TAKE);
                case OFUDA_RACK -> List.of(DECOR_OFUDA, DECOR_RACK_JOIN, DECOR_RACK_TAKE);
                case OMAMORI_CABINET, MEDICINE_CABINET, MEDICINE_JAR_RACK, MERCHANDISE_SHELF, INGREDIENT_BASKET -> List.of(DECOR_INSERT_ONE, DECOR_STORAGE, DECOR_SORT);
                case SCROLL_RACK, BOOK_BOX -> List.of(DECOR_INSERT_ONE, DECOR_STORAGE, DECOR_BOOK_STORAGE);
                case FUMIZUKUE -> List.of(DECOR_DESK_ACCESSORY, DECOR_DESK_REMOVE, DECOR_DESK_DRAWER, DECOR_DESK_WRITE, DECOR_INK);
                case SUZURI -> List.of(DECOR_DOC_WRITE, DECOR_INK, DECOR_INK_STORAGE);
                case BOOK_STACK -> List.of(DECOR_BOOK_STACK, DECOR_READ_TAKE);
                case YAGEN, STONE_MILL -> List.of(DECOR_YAGEN_INSERT, DECOR_YAGEN_WORK, DECOR_WORKSTATION);
                case HERB_DRYING_TRAY -> List.of(DECOR_DRYING, DECOR_DRYING_ENV, DECOR_DRYING_VIEW, DECOR_DRYING_TAKE);
                case FLOWER_VASE -> List.of(DECOR_FLOWER);
                case SAKE_CASK_RACK -> List.of(DECOR_SAKE, DECOR_SAKE_STORAGE);
                case CONNECTED_COUNTER -> List.of(DECOR_COUNTER, DECOR_STORAGE, DECOR_COUNTER_CONNECT);
                case SHOP_BENCH -> List.of(DECOR_SEAT, DECOR_BENCH_CONNECT);
                default -> List.of(); // Pure decorative models have no invented use action.
            };
            if (!details.isEmpty()) addItemForDetail(entry.getValue().asItem(), details);
        }
        for (var type : FumoTypes.getView()) {
            if (type.block() instanceof BaseFumoBlock fumo) {
                addItemForDetail(type.item(), DECOR_FUMO_TURN);
                if (fumo.supportsGrowth()) addItemForDetail(type.item(), DECOR_FUMO_CAKE);
            }
        }
    }

}
