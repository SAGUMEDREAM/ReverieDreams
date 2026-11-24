package cc.thonly.reverie_dreams.fabric.datagen.lang;

import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider.TranslationBuilder;

public interface LandmarkLang {
    static void add(TranslationBuilder builder) {
        builder.add("landmark_kind.reverie_dreams.realm_region", "异界区域");
        builder.add("landmark_kind.reverie_dreams.water_region", "水域");
        builder.add("landmark_kind.reverie_dreams.region", "区域");
        builder.add("landmark_kind.reverie_dreams.terrain", "地形");
        builder.add("landmark_kind.reverie_dreams.site", "地点");
        builder.add("landmark_kind.reverie_dreams.waterfall", "瀑布");
        builder.add("landmark_kind.reverie_dreams.uncertain_label", "待核地名");
        builder.add("landmark_kind.reverie_dreams.entrance", "入口");
        builder.add("landmark_kind.reverie_dreams.water_site", "临水地点");
        builder.add("landmark_kind.reverie_dreams.subterranean", "地下");
        builder.add("landmark_kind.reverie_dreams.realm_portal", "异界入口");
        builder.add("landmark_kind.reverie_dreams.route_region", "道路区域");
        builder.add("landmark_kind.reverie_dreams.height_unknown_site", "高度未知地点");
        builder.add("landmark_kind.reverie_dreams.site_label", "文字标注");
        builder.add("landmark_kind.reverie_dreams.aerial", "空中");

        builder.add("landmark.reverie_dreams.higan", "彼岸");
        builder.add("landmark.reverie_dreams.higan.note", "异界区域示意，不能据此设置主地图实体落点。");

        builder.add("landmark.reverie_dreams.sanzu_river", "三途河");
        builder.add("landmark.reverie_dreams.sanzu_river.note", "代表水域的大致位置，不是河岸边界。");

        builder.add("landmark.reverie_dreams.sai_no_kawara", "赛之河原");
        builder.add("landmark.reverie_dreams.sai_no_kawara.note", "区域代表点；文字覆盖范围不等于区域范围。");

        builder.add("landmark.reverie_dreams.youkai_mountain_peak", "妖怪之山主峰");
        builder.add("landmark.reverie_dreams.youkai_mountain_peak.note", "山地逆投影存在遮挡歧义，必须实地复核。");

        builder.add("landmark.reverie_dreams.tengu_settlement", "天狗聚落");
        builder.add("landmark.reverie_dreams.tengu_settlement.note", "建筑范围和朝向均未知。");

        builder.add("landmark.reverie_dreams.lake_suwako", "风神之湖");
        builder.add("landmark.reverie_dreams.lake_suwako.note", "采用当前地图已有湖瀑锚点，图像推断坐标另存。");

        builder.add("landmark.reverie_dreams.moriya_shrine", "守矢神社");
        builder.add("landmark.reverie_dreams.moriya_shrine.note", "与湖的文字合并说明，建筑锚点独立保留。");

        builder.add("landmark.reverie_dreams.nine_heaven_waterfall", "九天瀑布");
        builder.add("landmark.reverie_dreams.nine_heaven_waterfall.note", "采用当前地图已建两级瀑布的实测数据锚点。");

        builder.add("landmark.reverie_dreams.tenjo_roof", "天棚");
        builder.add("landmark.reverie_dreams.tenjo_roof.note", "原图首字待核；与秘天崖共用附近图示。");

        builder.add("landmark.reverie_dreams.rainbow_dragon_cave", "虹龙洞");
        builder.add("landmark.reverie_dreams.rainbow_dragon_cave.note", "洞口只作位置提示，地下深度未知。");

        builder.add("landmark.reverie_dreams.secret_heaven_cliff", "秘天崖");
        builder.add("landmark.reverie_dreams.secret_heaven_cliff.note", "地形区域代表点，不是崖壁精确边界。");

        builder.add("landmark.reverie_dreams.giant_toad_pond", "大蛤蟆之池");
        builder.add("landmark.reverie_dreams.giant_toad_pond.note", "原图池岸不清，不能把附近溪流整体当池面。");

        builder.add("landmark.reverie_dreams.scarlet_devil_mansion", "红魔馆");
        builder.add("landmark.reverie_dreams.scarlet_devil_mansion.note", "图标遮挡岸线，建筑底部和范围未知。");

        builder.add("landmark.reverie_dreams.cirno_house", "琪露诺家");
        builder.add("landmark.reverie_dreams.cirno_house.note", "雾之湖北岸的琪露诺冰屋；琪露诺家与冰屋是同一地点，北门连接湖畔小路。");

        builder.add("landmark.reverie_dreams.youkai_mountain_geyser", "间歇泉（妖怪之山）");
        builder.add("landmark.reverie_dreams.youkai_mountain_geyser.note", "与地狱谷／幻想风穴使用同一图示，不能分出精确洞口。");

        builder.add("landmark.reverie_dreams.hell_valley_entrance", "地狱谷、幻想风穴入口");
        builder.add("landmark.reverie_dreams.hell_valley_entrance.note", "与妖怪之山间歇泉为共用符号，不表示两个独立实测落点。");

        builder.add("landmark.reverie_dreams.road_of_the_middle", "中有之道");
        builder.add("landmark.reverie_dreams.road_of_the_middle.note", "图中注明贯穿地下，地下 Y 与实际空间路线未知。");

        builder.add("landmark.reverie_dreams.abandoned_mansion", "废弃洋馆");
        builder.add("landmark.reverie_dreams.abandoned_mansion.note", "图标前短接路仅有弱证据。");

        builder.add("landmark.reverie_dreams.nameless_hill", "无名之丘");
        builder.add("landmark.reverie_dreams.nameless_hill.note", "丘地代表点。");

        builder.add("landmark.reverie_dreams.lily_of_the_valley_field", "铃兰花田");
        builder.add("landmark.reverie_dreams.lily_of_the_valley_field.note", "不推断花田边界。");

        builder.add("landmark.reverie_dreams.sunflower_field", "太阳花田");
        builder.add("landmark.reverie_dreams.sunflower_field.note", "不推断花田边界。");

        builder.add("landmark.reverie_dreams.senkai_entrance_ibara", "仙界入口（茨华仙邸）");
        builder.add("landmark.reverie_dreams.senkai_entrance_ibara.note", "未画明确入口符号；目的地与空中高度不能由地形读取。");

        builder.add("landmark.reverie_dreams.sacred_asanagi_mountain", "浅间净秽山");
        builder.add("landmark.reverie_dreams.sacred_asanagi_mountain.note", "圣域中的金字塔与地下旧社，地下结构位于原地表下方。");

        builder.add("landmark.reverie_dreams.youkai_tree_sea", "妖怪树海");
        builder.add("landmark.reverie_dreams.youkai_tree_sea.note", "山前森林代表点。");

        builder.add("landmark.reverie_dreams.lost_house", "迷途之家");
        builder.add("landmark.reverie_dreams.lost_house.note", "林中建筑的位置候选。");

        builder.add("landmark.reverie_dreams.human_village", "人类村落");
        builder.add("landmark.reverie_dreams.human_village.note", "大图标覆盖范围广，坐标不能直接作为建筑中心。");

        builder.add("landmark.reverie_dreams.myouren_temple", "命莲寺");
        builder.add("landmark.reverie_dreams.myouren_temple.note", "棕色支路靠近此图标，遮挡末端未补画。");

        builder.add("landmark.reverie_dreams.senkai_entrance_shinreibyou", "仙界入口（神灵庙）");
        builder.add("landmark.reverie_dreams.senkai_entrance_shinreibyou.note", "入口图示不明确，不推断异界高度。");

        builder.add("landmark.reverie_dreams.youkai_tanuki_forest", "妖怪狸森林");
        builder.add("landmark.reverie_dreams.youkai_tanuki_forest.note", "森林名称保留原图写法，不由文字推断边界。");

        builder.add("landmark.reverie_dreams.human_village_plains", "人类村落平原");
        builder.add("landmark.reverie_dreams.human_village_plains.note", "平原代表点。");

        builder.add("landmark.reverie_dreams.willow_canal", "柳之运河");
        builder.add("landmark.reverie_dreams.willow_canal.note", "水道只作地标代表点，中心线另见已有水系数据。");

        builder.add("landmark.reverie_dreams.beast_path", "兽道");
        builder.add("landmark.reverie_dreams.beast_path.note", "名称标注关联 D03，不是另一条独立新增道路。");

        builder.add("landmark.reverie_dreams.bamboo_forest_of_the_lost", "迷途竹林");
        builder.add("landmark.reverie_dreams.bamboo_forest_of_the_lost.note", "示意范围很大，不按图标尺寸测量竹林边界。");

        builder.add("landmark.reverie_dreams.shining_needle_castle", "辉针城");
        builder.add("landmark.reverie_dreams.shining_needle_castle.note", "空中倒立的城郭，结构底部 Y1228，保留原稿倒立方向。");

        builder.add("landmark.reverie_dreams.hakurei_shrine", "博丽神社");
        builder.add("landmark.reverie_dreams.hakurei_shrine.note", "山脚路回转至图标附近，最后一段被遮挡。");

        builder.add("landmark.reverie_dreams.hakurei_shrine_back_mountain", "博丽神社后山");
        builder.add("landmark.reverie_dreams.hakurei_shrine_back_mountain.note", "后山代表点。");

        builder.add("landmark.reverie_dreams.hakurei_geyser", "间歇泉（博丽神社）");
        builder.add("landmark.reverie_dreams.hakurei_geyser.note", "没有独立清晰洞口符号，位置误差较大。");

        builder.add("landmark.reverie_dreams.kourindou", "香霖堂");
        builder.add("landmark.reverie_dreams.kourindou.note", "人类村落通往魔法森林途中道路旁的旧道具店，南侧接入道路，收藏外来品与奇特道具。");

        builder.add("landmark.reverie_dreams.three_fairies_new_house", "三月精新家");
        builder.add("landmark.reverie_dreams.three_fairies_new_house.note", "没有独立明确图标，不作为精确房基点。");

        builder.add("landmark.reverie_dreams.ghost_mansion", "幽灵洋馆");
        builder.add("landmark.reverie_dreams.ghost_mansion.note", "水网边缘的建筑示意。");

        builder.add("landmark.reverie_dreams.genbu_ravine", "玄武涧");
        builder.add("landmark.reverie_dreams.genbu_ravine.note", "峡谷代表点，不能由文字测量谷界。");

        builder.add("landmark.reverie_dreams.house_of_eternity", "永远亭");
        builder.add("landmark.reverie_dreams.house_of_eternity.note", "迷途竹林深处的宅邸与药局，北向小径连接竹林入口。");
        builder.add("landmark.reverie_dreams.kirisame_residence", "雾雨邸");
        builder.add("landmark.reverie_dreams.kirisame_residence.note", "林中建筑示意。");

        builder.add("landmark.reverie_dreams.okihori", "置行堀");
        builder.add("landmark.reverie_dreams.okihori.note", "原图写法保留，位置并非测得的独立池岸。");

        builder.add("landmark.reverie_dreams.margatroid_residence", "玛格特罗伊德邸");
        builder.add("landmark.reverie_dreams.margatroid_residence.note", "林中建筑示意。");

        builder.add("landmark.reverie_dreams.earthly_rainbow_cave", "地灵虹洞");
        builder.add("landmark.reverie_dreams.earthly_rainbow_cave.note", "原图没有独立洞口图标，不推断地下深度。");

        builder.add("landmark.reverie_dreams.forest_of_magic", "魔法森林");
        builder.add("landmark.reverie_dreams.forest_of_magic.note", "森林／湿地复合区域代表点。");

        builder.add("landmark.reverie_dreams.road_of_rethinking", "再思之道");
        builder.add("landmark.reverie_dreams.road_of_rethinking.note", "附近蓝线是水系；不把它自动识别为陆路。");

        builder.add("landmark.reverie_dreams.muenzuka", "无缘塚");
        builder.add("landmark.reverie_dreams.muenzuka.note", "红色区域代表点，不推断墓地地块边界。");

        builder.add("landmark.reverie_dreams.netherworld_barrier", "幽冥结界（空中）");
        builder.add("landmark.reverie_dreams.netherworld_barrier.note", "图注明空中，实际 Y 未知；不使用地表高度冒充。");

        builder.add("landmark.reverie_dreams.phantom_meadow", "幻草原");
        builder.add("landmark.reverie_dreams.phantom_meadow.note", "草原代表点，不推断完整边界。");
    }
}
