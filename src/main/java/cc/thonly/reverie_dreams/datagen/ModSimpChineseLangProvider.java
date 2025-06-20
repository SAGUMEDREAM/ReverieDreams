package cc.thonly.reverie_dreams.datagen;

import cc.thonly.mystias_izakaya.block.MIBlocks;
import cc.thonly.mystias_izakaya.item.MIItems;
import cc.thonly.mystias_izakaya.registry.FoodProperties;
import cc.thonly.reverie_dreams.Touhou;
import cc.thonly.reverie_dreams.block.FumoBlocks;
import cc.thonly.reverie_dreams.block.ModBlocks;
import cc.thonly.reverie_dreams.effect.ModPotions;
import cc.thonly.reverie_dreams.effect.ModStatusEffects;
import cc.thonly.reverie_dreams.entity.ModEntities;
import cc.thonly.reverie_dreams.item.BasicDanmakuItemTypeItem;
import cc.thonly.reverie_dreams.item.ModItems;
import cc.thonly.reverie_dreams.item.entry.DanmakuItemType;
import cc.thonly.reverie_dreams.lang.LanguageKeys;
import cc.thonly.reverie_dreams.sound.ModJukeboxSongs;
import cc.thonly.reverie_dreams.sound.ModSoundEvents;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class ModSimpChineseLangProvider extends FabricLanguageProvider implements LangGenerator {
    private static final Map<String, String> COLOR_NAME_TRANSLATION = new HashMap<>();

    static {
        COLOR_NAME_TRANSLATION.put("black", "黑色");
        COLOR_NAME_TRANSLATION.put("dark_red", "暗红色");
        COLOR_NAME_TRANSLATION.put("red", "红色");
        COLOR_NAME_TRANSLATION.put("dark_purple", "暗紫色");
        COLOR_NAME_TRANSLATION.put("purple", "紫色");
        COLOR_NAME_TRANSLATION.put("dark_blue", "暗蓝色");
        COLOR_NAME_TRANSLATION.put("blue", "蓝色");
        COLOR_NAME_TRANSLATION.put("dark_cyan", "暗青色");
        COLOR_NAME_TRANSLATION.put("cyan", "青色");
        COLOR_NAME_TRANSLATION.put("dark_green", "暗绿色");
        COLOR_NAME_TRANSLATION.put("green", "绿色");
        COLOR_NAME_TRANSLATION.put("dark_yellow_green", "暗黄绿色");
        COLOR_NAME_TRANSLATION.put("yellow_green", "黄绿色");
        COLOR_NAME_TRANSLATION.put("yellow", "黄色");
        COLOR_NAME_TRANSLATION.put("orange", "橙色");
        COLOR_NAME_TRANSLATION.put("grey", "灰色");
    }

    public ModSimpChineseLangProvider(FabricDataOutput dataOutput, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup) {
        super(dataOutput, "zh_cn", registryLookup);
    }

    @Override
    public void generateTranslations(RegistryWrapper.WrapperLookup wrapperLookup, TranslationBuilder translationBuilder) {
        translationBuilder.add("item_group.touhou", "幻想乡追忆录 - 物品/方块");
        translationBuilder.add("item_group.touhou.bullet", "幻想乡追忆录 - 子弹");
        translationBuilder.add("item_group.touhou.fumo", "幻想乡追忆录 - Fumo");
        translationBuilder.add("item_group.touhou.spawn_egg", "幻想乡追忆录 - 刷怪蛋");
        translationBuilder.add("item_group.touhou.role.spawn_egg", "幻想乡追忆录 - 东方角色刷怪蛋");
        translationBuilder.add("item.tooltip.color","颜色：");
        translationBuilder.add("item.tooltip.damage","伤害：");
        translationBuilder.add("item.tooltip.speed","速度：");
        translationBuilder.add("item.tooltip.count","数量：");
        translationBuilder.add("item.tooltip.base_type","弹幕轨迹：");
        translationBuilder.add(Touhou.id("recipe/danmaku_table").toTranslationKey(), "弹幕工作台");
        translationBuilder.add(Touhou.id("recipe/gensokyo_altar").toTranslationKey(), "幻想乡祭坛");
        translationBuilder.add(Touhou.id("recipe/strength_table").toTranslationKey(), "强化台");
        translationBuilder.add(Touhou.id("recipe/kitchen").toTranslationKey(), "夜雀食堂");


        this.generateItemTranslations(wrapperLookup, translationBuilder);
        this.generateBlockTranslations(wrapperLookup, translationBuilder);
        this.generateFumoTranslations(wrapperLookup, translationBuilder);
        this.generateSoundTranslations(wrapperLookup, translationBuilder);
        this.generateEntityTranslations(wrapperLookup, translationBuilder);
        this.generateEffectTranslations(wrapperLookup, translationBuilder);

        translationBuilder.add("gui.npc.info", "§d");
        translationBuilder.add("gui.npc.info.name", "§d名字: %s");
        translationBuilder.add("gui.npc.info.food", "§d食物信息: ");
        translationBuilder.add("gui.npc.info.food.nutrition", "§6饥饿度: %s");
        translationBuilder.add("gui.npc.info.food.saturation", "§6饱食度: %s");
        translationBuilder.add("gui.npc.info.uuid", "§d通用唯一识别码: %s");
        translationBuilder.add("gui.npc.info.health", "§d生命值: %s/%s");
        translationBuilder.add("gui.npc.info.armor", "§d护甲值: %s");
        translationBuilder.add("gui.npc.mode.0", "§b当前模式为: §a正常");
        translationBuilder.add("gui.npc.mode.1", "§b当前模式为: §a禁止移动");
        translationBuilder.add("gui.npc.mode.2", "§b当前模式为: §a潜行");
        translationBuilder.add("gui.npc.mode.3", "§b当前模式为: §a坐下");
        translationBuilder.add("gui.npc.mode.4", "§b当前模式为: §a工作中");

        translationBuilder.add("item.reverie_dreams.music.no_files", "§c未找到任何可用的音乐文件！");
        translationBuilder.add("item.reverie_dreams.music.switch_music", "§a切换音乐为：§f%s");
        translationBuilder.add("item.reverie_dreams.music.no_music_selected", "§e未选择任何音乐，潜行右键选择。");
        translationBuilder.add("item.reverie_dreams.music.playing_music", "§b播放音乐：§f%s §7[乐器: %s]");

        LanguageKeys.SIMP_CHINESE.build(translationBuilder);
        this.generateMITranslations(wrapperLookup, translationBuilder);
    }

    public void generateMITranslations(RegistryWrapper.WrapperLookup wrapperLookup, TranslationBuilder translationBuilder) {
        translationBuilder.add("item.tooltip.food_properties", "属性：");
        translationBuilder.add("item_group.kitchenware_item_group", "幻想乡追忆录 - 厨具");
        translationBuilder.add("item_group.Ingredients_item_group", "幻想乡追忆录 - 食材");
        translationBuilder.add("item_group.food_item_group", "幻想乡追忆录 - 食物");

        translationBuilder.add(MIBlocks.COOKING_POT, "煮锅");
        translationBuilder.add(MIBlocks.CUTTING_BOARD, "料理台");
        translationBuilder.add(MIBlocks.FRYING_PAN, "油锅");
        translationBuilder.add(MIBlocks.GRILL, "烧烤架");
        translationBuilder.add(MIBlocks.STEAMER, "蒸锅");

        translationBuilder.add(MIItems.BAMBOO_SHOOTS, "竹笋");
        translationBuilder.add(MIItems.BLACK_SALT, "黑盐");
        translationBuilder.add(MIItems.BLACK_PORK, "黑猪肉");
        translationBuilder.add(MIItems.BROCCOLI, "西兰花");
        translationBuilder.add(MIItems.VENISON, "鹿肉");
        translationBuilder.add(MIItems.BUTTER, "黄油");
        translationBuilder.add(MIItems.CAPSAICIN, "辣椒素");
        translationBuilder.add(MIItems.CHEESE, "奶酪");
        translationBuilder.add(MIItems.CHESTNUT, "栗子");
        translationBuilder.add(MIItems.CHILI, "辣椒");
        translationBuilder.add(MIItems.CRAB, "螃蟹");
        translationBuilder.add(MIItems.CREAM, "奶油");
        translationBuilder.add(MIItems.CUCUMBER, "黄瓜");
        translationBuilder.add(MIItems.DEW, "晨露");
        translationBuilder.add(MIItems.FLOUR, "面粉");
        translationBuilder.add(MIItems.FLOWERS, "食用花");
        translationBuilder.add(MIItems.FICUS_MICROCARPA, "薜茘");
        translationBuilder.add(MIItems.GINKGO, "白果");
        translationBuilder.add(MIItems.GRAPE, "葡萄");
        translationBuilder.add(MIItems.HAGFISH, "八目鳗");
        translationBuilder.add(MIItems.LEMON, "柠檬");
        translationBuilder.add(MIItems.LOTUS_NUTS, "莲子");
        translationBuilder.add(MIItems.MOONFLOWER, "月光花");
        translationBuilder.add(MIItems.OCTOPUS, "章鱼");
        translationBuilder.add(MIItems.ONION, "洋葱");
        translationBuilder.add(MIItems.PEACH, "桃子");
        translationBuilder.add(MIItems.PINE_NUT, "松子");
        translationBuilder.add(MIItems.PLUM, "李子");
        translationBuilder.add(MIItems.PUFF_YO_FRUIT, "噗噗哟果");
        translationBuilder.add(MIItems.RED_BEANS, "红豆");
        translationBuilder.add(MIItems.SALMON, "三文鱼");
        translationBuilder.add(MIItems.SEA_URCHIN, "海胆");
        translationBuilder.add(MIItems.SHRIMP, "虾");
        translationBuilder.add(MIItems.CICADA_SHELL, "蝉蜕");
        translationBuilder.add(MIItems.UDUMBARA, "幻昙华");
        translationBuilder.add(MIItems.STICKY_RICE, "糯米");
        translationBuilder.add(MIItems.SUPREME_TUNA, "极上金枪鱼");
        translationBuilder.add(MIItems.SWEET_POTATO, "红薯");
        translationBuilder.add(MIItems.TOFU, "豆腐");
        translationBuilder.add(MIItems.TOMATO, "番茄");
        translationBuilder.add(MIItems.TOON, "香椿");
        translationBuilder.add(MIItems.TREMELLA, "银耳");
        translationBuilder.add(MIItems.TROUT, "鳟鱼");
        translationBuilder.add(MIItems.TRUFFLE, "松露");
        translationBuilder.add(MIItems.TUNA, "金枪鱼");
        translationBuilder.add(MIItems.TWIN_LOTUS, "并蒂莲");
        translationBuilder.add(MIItems.WAGYU_BEEF, "和牛");
        translationBuilder.add(MIItems.WHITE_RADISH, "白萝卜");
        translationBuilder.add(MIItems.WILD_BOAR_MEAT, "野猪肉");

        translationBuilder.add(MIItems.ALL_MEAT_FEAST, "全肉盛宴");
        translationBuilder.add(MIItems.ARCTIC_SWEET_SHRIMP_AND_PEACH_SALAD, "北极甜虾蜜桃色拉");
        translationBuilder.add(MIItems.ASSORTED_TEMPURA, "什锦天妇罗");
        translationBuilder.add(MIItems.A_LITTLE_SWEET_POISON, "小小的甜蜜「毒药」");
        translationBuilder.add(MIItems.BAKED_CRAB_WITH_CREAM, "奶油焗蟹");
        translationBuilder.add(MIItems.BAKED_SWEET_POTATOES, "烤红薯");
        translationBuilder.add(MIItems.BAMBOO_SHOOTS_FRIED_MEAT, "竹笋炒肉");
        translationBuilder.add(MIItems.BAMBOO_SHOOTS_STEWED_IN_STONE_POT, "石锅竹笋炖肉");
        translationBuilder.add(MIItems.BAMBOO_STEAMED_EGG, "竹筒蒸蛋");
        translationBuilder.add(MIItems.BAMBOO_TUBE_ROASTED_DRUNKEN_SHRIMP, "竹筒烤醉虾");
        translationBuilder.add(MIItems.BAMBOO_TUBE_STEAMED_PORK, "竹筒蒸猪肉");
        translationBuilder.add(MIItems.BEAR_PAW, "熊掌");
        translationBuilder.add(MIItems.BEEF_HOT_POT, "牛肉火锅");
        translationBuilder.add(MIItems.BEEF_RICE, "牛肉盖浇饭");
        translationBuilder.add(MIItems.BEEF_WELLINGTON, "威灵顿牛排");
        translationBuilder.add(MIItems.BEETLE_STEAMED_CAKE, "兜甲蒸糕");
        translationBuilder.add(MIItems.BURNT_PUDDING, "燃烧布丁");
        translationBuilder.add(MIItems.BISCAY_BISCUITS, "比斯开饼干");
        translationBuilder.add(MIItems.BOILED_FISH, "白煮鱼");
        translationBuilder.add(MIItems.BRAISED_EEL, "红烧鳗鱼");
        translationBuilder.add(MIItems.BRAISED_PORK_WITH_PEACH, "桃子红烧肉");
        translationBuilder.add(MIItems.BUTTER_STEAK, "黄油牛排");
        translationBuilder.add(MIItems.CANDIED_CHESTNUTS, "糖栗子");
        translationBuilder.add(MIItems.CANDIED_SWEET_POTATO, "糖渍红薯");
        translationBuilder.add(MIItems.CATS_PLAYING_IN_WATER, "猫戏水");
        translationBuilder.add(MIItems.CAT_FOOD, "猫饭");
        translationBuilder.add(MIItems.CAT_KULULI, "猫咕噜哩");
        translationBuilder.add(MIItems.CAT_PIZZA, "猫咪披萨");
        translationBuilder.add(MIItems.CHEESE_EGG, "芝士蛋");
        translationBuilder.add(MIItems.COLD_DISH_CARVING, "凉菜雕花");
        translationBuilder.add(MIItems.COLD_TOFU, "凉拌豆腐");
        translationBuilder.add(MIItems.COLORFUL_JADE_FRIED_BUNS, "彩玉煎包");
        translationBuilder.add(MIItems.COOKING_TOFU, "正在煮的豆腐");
        translationBuilder.add(MIItems.CREAM_STEW, "奶油炖菜");
        translationBuilder.add(MIItems.CRISP_CYCLONE, "脆旋风");
        translationBuilder.add(MIItems.DARK_CUISINE, "黑暗物质");
        translationBuilder.add(MIItems.DEEP_FRIED_CICADA_SHELLS, "炸蝉蜕");
        translationBuilder.add(MIItems.DEPRESSED_CHEESE_STICKS, "忧郁芝士条");
        translationBuilder.add(MIItems.DEW_BOILED_EGGS, "晨露煮蛋");
        translationBuilder.add(MIItems.DORAYAKI, "铜锣烧");
        translationBuilder.add(MIItems.DUMPLING, "饺子");
        translationBuilder.add(MIItems.EEL_EGG_DONBURI, "鳗鱼蛋盖饭");
        translationBuilder.add(MIItems.EGGS_BENEDICT, "班尼迪克蛋");
        translationBuilder.add(MIItems.ENERGY_STRING, "能量串");
        translationBuilder.add(MIItems.FAILING_SAKURA_SNOW, "坠樱雪");
        translationBuilder.add(MIItems.FANTASY_IS_ALL_THE_RAGE, "幻想大流行");
        translationBuilder.add(MIItems.FISH_LEAPS_OVER_DRAGON_GATE, "鱼跃龙门");
        translationBuilder.add(MIItems.FLOWERS_BIRDS_WIND_AND_MOON, "花鸟风月");
        translationBuilder.add(MIItems.FLOWING_WATER_NOODLES, "流水素面");
        translationBuilder.add(MIItems.FRIED_HAGFISH, "炸八目鳗");
        translationBuilder.add(MIItems.FRIED_PORK_CUTLET, "炸猪排");
        translationBuilder.add(MIItems.FRIED_PORK_SHREDS, "炒肉丝");
        translationBuilder.add(MIItems.FRIED_SHRIMP_TEMPURA, "炸虾天妇罗");
        translationBuilder.add(MIItems.FRIED_TOFU, "炸豆腐");
        translationBuilder.add(MIItems.FRIED_TOMATO_STRIPS, "炒番茄条");
        translationBuilder.add(MIItems.FRIGHT_ADVENTURE, "惊吓！大冒险");
        translationBuilder.add(MIItems.GAME_SOUP, "野味杂烩汤");
        translationBuilder.add(MIItems.GENSOKYO_BUDDHA_JUMPS_OVER_THE_WALL, "幻想佛跳墙");
        translationBuilder.add(MIItems.GENSOKYO_STAR_LOTUS_SHIP, "幻想乡星莲船");
        translationBuilder.add(MIItems.GIANT_TAMAGOYAKI, "巨型玉子烧");
        translationBuilder.add(MIItems.GINKGO_AND_RADISH_PORK_RIB_SOUP, "银杏萝卜排骨汤");
        translationBuilder.add(MIItems.GLOOMY_FRUIT_PIE, "忧郁水果派");
        translationBuilder.add(MIItems.GLUTINOUS_RICE_BALLS, "汤圆");
        translationBuilder.add(MIItems.GOLDEN_CRISPY_FISH_CAKE, "金黄酥鱼饼");
        translationBuilder.add(MIItems.GRAND_BANQUET, "大奢宴");
        translationBuilder.add(MIItems.GREEN_BAMBOO_WELCOMES_SPRING, "青竹迎春");
        translationBuilder.add(MIItems.GREEN_FAIRY_MUSHROOM, "绿野仙菇");
        translationBuilder.add(MIItems.GRILLED_HAGFISH, "烤八目鳗");
        translationBuilder.add(MIItems.GRILLED_PORK_RICE_BALLS, "炙猪肉饭团");
        translationBuilder.add(MIItems.HEART_PORRIDGE_GRUEL, "养心粥");
        translationBuilder.add(MIItems.HELL_THRILL_WARNING, "地狱激辛警告！");
        translationBuilder.add(MIItems.HOLY_WHITE_LOTUS_SEED_CAKE, "圣白莲子糕");
        translationBuilder.add(MIItems.HONEY_BBQ_PORK, "蜜汁叉烧");
        translationBuilder.add(MIItems.HORAI_DAMA_NO_EDA, "蓬莱玉枝");
        translationBuilder.add(MIItems.HOT_WAFFLES, "热华夫饼");
        translationBuilder.add(MIItems.HULA_SOUP, "呼啦汤");
        translationBuilder.add(MIItems.LION_HEAD, "狮子头");
        translationBuilder.add(MIItems.LONGYIN_PEACH, "龙吟桃子");
        translationBuilder.add(MIItems.LOOKING_UP_AT_THE_CEILING_FRUIT_PIE, "仰望天花板派");
        translationBuilder.add(MIItems.LOTUS_FISH_RICE_BOWL, "荷花鱼米盏");
        translationBuilder.add(MIItems.LUOHAN_VEGETARIAN, "罗汉上素");
        translationBuilder.add(MIItems.MAD_HATTER_TEA_PARTY, "疯帽子茶会");
        translationBuilder.add(MIItems.MAGMA, "岩浆");
        translationBuilder.add(MIItems.MAOYU_LAVA_TOFU, "茂羽岩浆豆腐");
        translationBuilder.add(MIItems.MAOYU_TRICOLOR_ICE_CREAM, "茂羽三色冰淇淋");
        translationBuilder.add(MIItems.MAPO_TOFU, "麻婆豆腐");
        translationBuilder.add(MIItems.MILKY_MUSHROOM_SOUP, "奶香蘑菇汤");
        translationBuilder.add(MIItems.MOCHI, "年糕团子");
        translationBuilder.add(MIItems.MOLECULAR_EGG, "分子蛋");
        translationBuilder.add(MIItems.MOONLIGHT_DUMPLINGS, "月光团子");
        translationBuilder.add(MIItems.MOONLIGHT_OVER_LOTUS_POND, "荷塘月色");
        translationBuilder.add(MIItems.MOON_CAKE, "月饼");
        translationBuilder.add(MIItems.MOON_LOVERS, "月之恋人");
        translationBuilder.add(MIItems.MUSHROOM_GIRLS_DANCE_STEW, "蘑女的舞踏烩");
        translationBuilder.add(MIItems.MUSHROOM_MEAT_SLICES, "香菇肉片");
        translationBuilder.add(MIItems.NIGIRI_SUSHI, "握寿司");
        translationBuilder.add(MIItems.OEDO_BOAT_FESTIVAL, "大江户船祭");
        translationBuilder.add(MIItems.OKONOMIYAKI, "御好烧");
        translationBuilder.add(MIItems.ONE_HIT_KILL, "一击必杀");
        translationBuilder.add(MIItems.ORDINARY_SMALL_CAKE, "普通小蛋糕");
        translationBuilder.add(MIItems.PAN_FRIED_MUSHROOM_MEAT_ROLL, "煎香菇肉卷");
        translationBuilder.add(MIItems.PAN_FRIED_SALMON, "煎三文鱼");
        translationBuilder.add(MIItems.PEACH_BLOSSOM_GLAZE_ROLL, "桃花琉璃卷");
        translationBuilder.add(MIItems.PEACH_BLOSSOM_SOUP, "桃花羹");
        translationBuilder.add(MIItems.PHOENIX, "不死鸟");
        translationBuilder.add(MIItems.PICKLED_CUCUMBERS, "腌黄瓜");
        translationBuilder.add(MIItems.PIG_DEER_BUTTERFLY, "猪鹿蝶");
        translationBuilder.add(MIItems.PINE_NUT_CAKE, "松子糕");
        translationBuilder.add(MIItems.PIRATE_BACON, "海盗培根");
        translationBuilder.add(MIItems.PLUM_TEA_RICE, "梅子茶泡饭");
        translationBuilder.add(MIItems.POETRY_AND_GINKGO, "诗与银杏");
        translationBuilder.add(MIItems.POISONOUS_GARDEN, "毒瘴花园");
        translationBuilder.add(MIItems.PORK_AND_TROUT_SMOKED, "熏猪鳟双拼");
        translationBuilder.add(MIItems.PORK_RICE, "猪肉盖浇饭");
        translationBuilder.add(MIItems.POTATO_CROQUETTES, "土豆可乐饼");
        translationBuilder.add(MIItems.PSEUDO_JIRITAMA, "拟尻子玉");
        translationBuilder.add(MIItems.PUMPKIN_SHRIMP_CAKE, "南瓜虾盅");
        translationBuilder.add(MIItems.RAPUNZEL, "长发公主");
        translationBuilder.add(MIItems.REAL_SEAFOOD_MISO_SOUP, "真·海鲜味噌汤");
        translationBuilder.add(MIItems.RED_BEAN_DAIFUKU, "红豆大福");
        translationBuilder.add(MIItems.REFRESHING_PUDDING, "凉爽布丁");
        translationBuilder.add(MIItems.REVERSING_THE_WORLD, "逆转天地！");
        translationBuilder.add(MIItems.RICE_BALL, "饭团");
        translationBuilder.add(MIItems.RISOTTO, "烩饭");
        translationBuilder.add(MIItems.ROASTED_MUSHROOMS, "烤蘑菇");
        translationBuilder.add(MIItems.SAKURA_PUDDING, "樱花布丁");
        translationBuilder.add(MIItems.SALMON_TEMPURA, "三文鱼天妇罗");
        translationBuilder.add(MIItems.SASHIMI_PLATTER, "刺身拼盘");
        translationBuilder.add(MIItems.SCARLET_DEVILS_CAKE, "红魔蛋糕");
        translationBuilder.add(MIItems.SCONES, "司康饼");
        translationBuilder.add(MIItems.SCREAMING_ODEN, "绝叫关东煮");
        translationBuilder.add(MIItems.SEAFOOD_MISO_SOUP, "海鲜味噌汤");
        translationBuilder.add(MIItems.SEA_URCHIN_SHINGEN_PANCAKE, "海胆信玄饼");
        translationBuilder.add(MIItems.SEA_URCHIN_SASHIMI, "海胆刺身");
        translationBuilder.add(MIItems.SECRET_DRIED_FISH, "秘制小鱼干");
        translationBuilder.add(MIItems.SECRET_MUSHROOM_CASSEROLE, "秘制蘑菇煲");
        translationBuilder.add(MIItems.SEVEN_COLORED_YOKAN, "七彩羊羹");
        translationBuilder.add(MIItems.SHIRAGA_SADAMATSU, "白发定松");
        translationBuilder.add(MIItems.SKINNY_HORSE_DUMPLING, "瘦马团子");
        translationBuilder.add(MIItems.SNOW_WHITE, "白雪公主");
        translationBuilder.add(MIItems.STEAMED_EGG_WITH_SEA_URCHIN, "海胆蒸蛋");
        translationBuilder.add(MIItems.STINKY_TOFU, "臭豆腐");
        translationBuilder.add(MIItems.STRENGTH_SOUP, "力量汤");
        translationBuilder.add(MIItems.SUPERME_SEAFOOD_NOODLES, "至尊海鲜面");
        translationBuilder.add(MIItems.TAICHI_BAGUA_FISH_MAW, "太极八卦鱼肚");
        translationBuilder.add(MIItems.TAKETORIHIME, "辉夜姬");
        translationBuilder.add(MIItems.TAKOYAKI, "章鱼烧");
        translationBuilder.add(MIItems.THE_BEAUTY_OF_HAN_PALACE, "汉宫佳人");
        translationBuilder.add(MIItems.THE_DREAM, "梦想之宴");
        translationBuilder.add(MIItems.THE_MARS, "火星料理");
        translationBuilder.add(MIItems.THE_SOURCE_OF_LIFE, "生命之源");
        translationBuilder.add(MIItems.TIANSHI_BRAISED_CHESTNUT_MUSHROOMS, "天师板栗焖菇");
        translationBuilder.add(MIItems.TOFU_MISO, "味噌豆腐");
        translationBuilder.add(MIItems.TOFU_POT, "豆腐锅");
        translationBuilder.add(MIItems.TONKOTSU_RAMEN, "豚骨拉面");
        translationBuilder.add(MIItems.TOON_PANCAKES, "香椿煎饼");
        translationBuilder.add(MIItems.TWO_HEAVENS_ONE_STYLE, "双天一式");
        translationBuilder.add(MIItems.UDUMBARA_CAKE, "幻昙花糕");
        translationBuilder.add(MIItems.UNCONSCIOUS_MONSTER_MOUSSE, "无意识怪物慕斯");
        translationBuilder.add(MIItems.VEGETABLE_SPECIAL, "蔬菜特餐");
        translationBuilder.add(MIItems.WARM_RICE_BALL, "温热饭团");
        translationBuilder.add(MIItems.WHITE_PEACH_EIGHT_BRIDGE, "白桃八桥");
        translationBuilder.add(MIItems.YUNSHAN_COTTON_CANDY, "云山棉花糖");
        translationBuilder.add(MIItems.ZHAJI, "炸脊");

        translationBuilder.add(FoodProperties.MEAT.getTranslateKey(), "肉类");
        translationBuilder.add(FoodProperties.AQUATIC_PRODUCTS.getTranslateKey(), "水产");
        translationBuilder.add(FoodProperties.VEGETARIAN.getTranslateKey(), "素食");
        translationBuilder.add(FoodProperties.HOMESTYLE.getTranslateKey(), "家常");
        translationBuilder.add(FoodProperties.GOURMET.getTranslateKey(), "高级");
        translationBuilder.add(FoodProperties.LEGENDARY.getTranslateKey(), "传说");
        translationBuilder.add(FoodProperties.GREASY.getTranslateKey(), "重油");
        translationBuilder.add(FoodProperties.LIGHT.getTranslateKey(), "清淡");
        translationBuilder.add(FoodProperties.GOOD_WITH_ALCOHOL.getTranslateKey(), "下酒");
        translationBuilder.add(FoodProperties.FILLING.getTranslateKey(), "饱腹");
        translationBuilder.add(FoodProperties.MOUNTAIN_DELICACY.getTranslateKey(), "山珍");
        translationBuilder.add(FoodProperties.OCEAN_FLAVOR.getTranslateKey(), "海味");
        translationBuilder.add(FoodProperties.JAPANESE_STYLE.getTranslateKey(), "和风");
        translationBuilder.add(FoodProperties.WESTERN_STYLE.getTranslateKey(), "西式");
        translationBuilder.add(FoodProperties.CHINESE_STYLE.getTranslateKey(), "中式");
        translationBuilder.add(FoodProperties.SALTY.getTranslateKey(), "咸");
        translationBuilder.add(FoodProperties.UMAMI.getTranslateKey(), "鲜");
        translationBuilder.add(FoodProperties.SWEET.getTranslateKey(), "甜");
        translationBuilder.add(FoodProperties.RAW.getTranslateKey(), "生");
        translationBuilder.add(FoodProperties.PHOTOGENIC.getTranslateKey(), "适合拍照");
        translationBuilder.add(FoodProperties.COOL.getTranslateKey(), "凉爽");
        translationBuilder.add(FoodProperties.FIERY.getTranslateKey(), "灼热");
        translationBuilder.add(FoodProperties.POWER_SURGE.getTranslateKey(), "力量涌现");
        translationBuilder.add(FoodProperties.BIZARRE.getTranslateKey(), "猎奇");
        translationBuilder.add(FoodProperties.CULTURAL_DEPTH.getTranslateKey(), "文化底蕴");
        translationBuilder.add(FoodProperties.MUSHROOMS.getTranslateKey(), "菌类");
        translationBuilder.add(FoodProperties.UNBELIEVABLE.getTranslateKey(), "难以置信");
        translationBuilder.add(FoodProperties.PETITE.getTranslateKey(), "小巧");
        translationBuilder.add(FoodProperties.DREAMLIKE.getTranslateKey(), "梦幻");
        translationBuilder.add(FoodProperties.LOCAL_SPECIALTY.getTranslateKey(), "特产");
        translationBuilder.add(FoodProperties.FRUITY.getTranslateKey(), "果味");
        translationBuilder.add(FoodProperties.SOUP_AND_STEW.getTranslateKey(), "汤羹");
        translationBuilder.add(FoodProperties.GRILLED.getTranslateKey(), "烧烤");
        translationBuilder.add(FoodProperties.SPICY.getTranslateKey(), "辣");
        translationBuilder.add(FoodProperties.FLAMING.getTranslateKey(), "燃起来了");
        translationBuilder.add(FoodProperties.SOUR.getTranslateKey(), "酸");
        translationBuilder.add(FoodProperties.TOXIC.getTranslateKey(), "有毒");
        translationBuilder.add(FoodProperties.DARK_CUISINE.getTranslateKey(), "黑暗物质");
        translationBuilder.add(FoodProperties.ECONOMICAL.getTranslateKey(), "实惠");
        translationBuilder.add(FoodProperties.EXPENSIVE.getTranslateKey(), "昂贵");
        translationBuilder.add(FoodProperties.LARGE_PARTITION.getTranslateKey(), "大份");
        translationBuilder.add(FoodProperties.POPULAR_NEGATIVE.getTranslateKey(), "不流行");
        translationBuilder.add(FoodProperties.POPULAR_POSITIVE.getTranslateKey(), "流行的");
        translationBuilder.add(FoodProperties.SIGNATURE.getTranslateKey(), "招牌");
        translationBuilder.add(FoodProperties.CURSE.getTranslateKey(), "诅咒");
    }

    public void generateEntityTranslations(RegistryWrapper.WrapperLookup wrapperLookup, TranslationBuilder translationBuilder) {
        translationBuilder.add(ModEntities.KILLER_BEE_ENTITY_TYPE.getTranslationKey(), "杀人蜂");
        translationBuilder.add("item." + EntityType.getId(ModEntities.KILLER_BEE_ENTITY_TYPE).toString().replaceAll(":", ".") + "_spawn_egg", "杀人蜂刷怪蛋");
        translationBuilder.add(ModEntities.GHOST_ENTITY_TYPE.getTranslationKey(), "幽灵");
        translationBuilder.add("item." + EntityType.getId(ModEntities.GHOST_ENTITY_TYPE).toString().replaceAll(":", ".") + "_spawn_egg", "幽灵刷怪蛋");
        translationBuilder.add(ModEntities.YouseiTypes.BLUE.getEntry().getTranslationKey(), "蓝色妖精");
        translationBuilder.add("item." + EntityType.getId(ModEntities.YouseiTypes.BLUE.getEntry()).toString().replaceAll(":", ".") + "_spawn_egg", "蓝色妖精刷怪蛋");
        translationBuilder.add(ModEntities.YouseiTypes.ORANGE.getEntry().getTranslationKey(), "橙色妖精");
        translationBuilder.add("item." + EntityType.getId(ModEntities.YouseiTypes.ORANGE.getEntry()).toString().replaceAll(":", ".") + "_spawn_egg", "橙色妖精刷怪蛋");
        translationBuilder.add(ModEntities.YouseiTypes.GREEN.getEntry().getTranslationKey(), "绿色妖精");
        translationBuilder.add("item." + EntityType.getId(ModEntities.YouseiTypes.GREEN.getEntry()).toString().replaceAll(":", ".") + "_spawn_egg", "绿色妖精刷怪蛋");
        translationBuilder.add(ModEntities.SUNFLOWER_YOUSEI_ENTITY_TYPE.getTranslationKey(), "向日葵妖精");
        translationBuilder.add("item." + EntityType.getId(ModEntities.SUNFLOWER_YOUSEI_ENTITY_TYPE).toString().replaceAll(":", ".") + "_spawn_egg", "向日葵妖精刷怪蛋");
        translationBuilder.add(ModEntities.GOBLIN_ENTITY_TYPE.getTranslationKey(), "哥布林");
        translationBuilder.add("item." + EntityType.getId(ModEntities.GOBLIN_ENTITY_TYPE).toString().replaceAll(":", ".") + "_spawn_egg", "哥布林刷怪蛋");
        translationBuilder.add(ModEntities.WATER_ELEMENTAL_ENTITY_TYPE.getTranslationKey(), "水元素");
        translationBuilder.add("item." + EntityType.getId(ModEntities.WATER_ELEMENTAL_ENTITY_TYPE).toString().replaceAll(":", ".") + "_spawn_egg", "水元素刷怪蛋");
        translationBuilder.add(ModEntities.FIRE_ELEMENTAL_ENTITY_TYPE.getTranslationKey(), "火元素");
        translationBuilder.add("item." + EntityType.getId(ModEntities.FIRE_ELEMENTAL_ENTITY_TYPE).toString().replaceAll(":", ".") + "_spawn_egg", "火元素刷怪蛋");
        translationBuilder.add(ModEntities.ICE_ELEMENTAL_ENTITY_TYPE.getTranslationKey(), "冰元素");
        translationBuilder.add("item." + EntityType.getId(ModEntities.ICE_ELEMENTAL_ENTITY_TYPE).toString().replaceAll(":", ".") + "_spawn_egg", "冰元素刷怪蛋");
        translationBuilder.add(ModEntities.BROOM_ENTITY_TYPE.getTranslationKey(), "魔法扫帚");
        translationBuilder.add("item." + EntityType.getId(ModEntities.BROOM_ENTITY_TYPE).toString().replaceAll(":", ".") + "_spawn_egg", "魔法扫帚刷怪蛋");
        translationBuilder.add(ModEntities.HAIRBALL_ENTITY_TYPE.getTranslationKey(), "毛玉");
        translationBuilder.add("item." + EntityType.getId(ModEntities.HAIRBALL_ENTITY_TYPE).toString().replaceAll(":", ".") + "_spawn_egg", "毛玉刷怪蛋");
        translationBuilder.add(ModEntities.MUSHROOM_MONSTER_ENTITY_TYPE.getTranslationKey(), "蘑菇");
        translationBuilder.add("item." + EntityType.getId(ModEntities.MUSHROOM_MONSTER_ENTITY_TYPE).toString().replaceAll(":", ".") + "_spawn_egg", "蘑菇刷怪蛋");

    }

    public void generateEffectTranslations(RegistryWrapper.WrapperLookup wrapperLookup, TranslationBuilder translationBuilder) {
        this.generateStatusEffect(translationBuilder, ModStatusEffects.ELIXIR_OF_LIFE, "不死");
        this.generateStatusEffect(translationBuilder, ModStatusEffects.MENTAL_DISORDER, "精神错乱");
        this.generateStatusEffect(translationBuilder, ModStatusEffects.BACK_OF_LIFE, "还生药");

        this.generatePotion(translationBuilder, ModPotions.ELIXIR_OF_LIFE_POTION, "蓬莱之药", "喷溅型蓬莱之药", "滞留型蓬莱之药");
        this.generatePotion(translationBuilder, ModPotions.MENTAL_DISORDER_POTION, "精神错乱药水", "喷溅型精神错乱药水", "滞留型精神错乱药水");
        this.generatePotion(translationBuilder, ModPotions.BACK_OF_LIFE_POTION, "还生药", "喷溅型还生药", "滞留型还生药");

    }

    public void generateItemTranslations(RegistryWrapper.WrapperLookup wrapperLookup, TranslationBuilder translationBuilder) {
        // 调试
        translationBuilder.add(ModItems.BATTLE_STICK, "战斗调试棒");

        // 图标
        translationBuilder.add(ModItems.ICON, "幻想乡追忆录　～ Reverie of Lost Dreams");
        translationBuilder.add(ModItems.FUMO_ICON, "毛绒玩偶图标");
        translationBuilder.add(ModItems.SPAWN_EGG, "刷怪蛋");

        // 材料
        translationBuilder.add(ModItems.POINT, "Point");
        translationBuilder.add(ModItems.POWER, "Power");
        translationBuilder.add(ModItems.UPGRADED_HEALTH_FRAGMENT, "残机碎片");
        translationBuilder.add(ModItems.BOMB_FRAGMENT, "Bomb碎片");
        translationBuilder.add(ModItems.RED_ORB, "红宝玉");
        translationBuilder.add(ModItems.BLUE_ORB, "蓝宝玉");
        translationBuilder.add(ModItems.YELLOW_ORB, "黄宝玉");
        translationBuilder.add(ModItems.GREEN_ORB, "绿宝玉");
        translationBuilder.add(ModItems.PURPLE_ORB, "紫宝玉");
        translationBuilder.add(ModItems.YIN_YANG_ORB, "阴阳玉");

        // 道具
        translationBuilder.add(ModItems.TOUHOU_HELPER, "东方模组入门");
        translationBuilder.add(ModItems.UPGRADED_HEALTH, "残机");
        translationBuilder.add(ModItems.BOMB, "Bomb");
        translationBuilder.add(ModItems.HORAI_DAMA_NO_EDA, "蓬莱玉枝");
        translationBuilder.add(ModItems.CROSSING_CHISEL, "穿墙之凿");
        translationBuilder.add(ModItems.GAP_BALL, "隙间之球");
        translationBuilder.add(ModItems.BAGUA_FURNACE, "八卦炉");
        translationBuilder.add(ModItems.TIME_STOP_CLOCK, "时停钟");
        translationBuilder.add(ModItems.EARPHONE, "神子耳机");
        translationBuilder.add(ModItems.KOISHI_HAT, "恋恋钢盔");

        // 武器
        translationBuilder.add(ModItems.HAKUREI_CANE, "博丽御币");
        translationBuilder.add(ModItems.WIND_BLESSING_CANE, "祝风御币");
        translationBuilder.add(ModItems.MAGIC_BROOM, "魔法扫帚");
        translationBuilder.add(ModItems.GUNGNIR, "神枪冈格尼尔");
        translationBuilder.add(ModItems.LEVATIN, "莱瓦汀");
        translationBuilder.add(ModItems.ROKANKEN, "楼观剑");
        translationBuilder.add(ModItems.HAKUROKEN, "白楼剑");
        translationBuilder.add(ModItems.PAPILIO_PATTERN_FAN, "凤蝶纹扇");
        translationBuilder.add(ModItems.IBUKIHO, "伊吹瓢");
        translationBuilder.add(ModItems.SWORD_OF_HISOU, "非想之剑");
        translationBuilder.add(ModItems.MANPOZUCHI, "万宝槌");
        translationBuilder.add(ModItems.NUE_TRIDENT, "鵺之三叉戟");
        translationBuilder.add(ModItems.TRUMPET_GUN, "喇叭枪");
        translationBuilder.add(ModItems.TREASURE_HUNTING_ROD, "探宝棒");
        translationBuilder.add(ModItems.KNIFE, "飞刀");
        translationBuilder.add(ModItems.MAPLE_LEAF_FAN, "枫叶团扇");
        translationBuilder.add(ModItems.VIOLIN, "小提琴");
        translationBuilder.add(ModItems.KEYBOARD, "键盘");
        translationBuilder.add(ModItems.TRUMPET, "小号");

        // 工具矿物类
        translationBuilder.add(ModItems.RAW_SILVER, "生银矿");
        translationBuilder.add(ModItems.SILVER_INGOT, "银锭");
        translationBuilder.add(ModItems.SILVER_NUGGET, "银粒");
        translationBuilder.add(ModItems.SILVER_SWORD, "银剑");
        translationBuilder.add(ModItems.SILVER_AXE, "银斧");
        translationBuilder.add(ModItems.SILVER_PICKAXE, "银稿");
        translationBuilder.add(ModItems.SILVER_SHOVEL, "银锹");
        translationBuilder.add(ModItems.SILVER_HOE, "银锄");
        translationBuilder.add(ModItems.SILVER_HELMET, "银头盔");
        translationBuilder.add(ModItems.SILVER_CHESTPLATE, "银胸甲");
        translationBuilder.add(ModItems.SILVER_LEGGINGS, "银护腿");
        translationBuilder.add(ModItems.SILVER_BOOTS, "银靴子");

        // 其他
        translationBuilder.add(ModItems.SHIDE, "纸垂");
        translationBuilder.add(ModItems.SHIMENAWA, "注连绳");

//        translationBuilder.add(ModItems.DEBUG_DANMAKU_ITEM, "调试弹幕");
//        translationBuilder.add(ModItems.DEBUG_SPELL_CARD_ITEM, "调试符卡");
//        translationBuilder.add(ModItems.DEBUG_SPELL_CARD_ITEM2, "调试符卡2");

        this.generateDanmakuItemTranslations(wrapperLookup, translationBuilder, true);
    }

    public void generateSoundTranslations(RegistryWrapper.WrapperLookup wrapperLookup, TranslationBuilder translationBuilder) {
        for (var sound : ModSoundEvents.FUMO_SOUNDS) {
            this.generateSoundEventSubtitle(translationBuilder, sound, "fumo");
        }
        this.generateSoundEventSubtitle(translationBuilder, ModSoundEvents.BIU, "满身疮痍");
        this.generateSoundEventSubtitle(translationBuilder, ModSoundEvents.POINT, "收点");
        this.generateSoundEventSubtitle(translationBuilder, ModSoundEvents.SPELL_CARD, "符卡释放");
        this.generateSoundEventSubtitle(translationBuilder, ModSoundEvents.UP, "升级");
        this.generateSoundEventSubtitle(translationBuilder, ModSoundEvents.FIRE, "弹幕发射");

        this.generateDiscTranslations(wrapperLookup, translationBuilder);
    }

    public void generateDiscTranslations(RegistryWrapper.WrapperLookup wrapperLookup, TranslationBuilder translationBuilder) {
        this.generateJukeBox(translationBuilder, ModJukeboxSongs.HR01_01.getJukeboxSongRegistryKey(), "ZUN - 蓬莱人形　～ Dolls in Pseudo Paradise.");
        this.generateJukeBox(translationBuilder, ModJukeboxSongs.HR02_08.getJukeboxSongRegistryKey(), "莲台野夜行 - 过去的花 ～ Fairy of Flower");
        this.generateJukeBox(translationBuilder, ModJukeboxSongs.HR03_01.getJukeboxSongRegistryKey(), "ZUN - 童祭　～ Innocent Treasures");
        this.generateJukeBox(translationBuilder, ModJukeboxSongs.TH15_16.getJukeboxSongRegistryKey(), "东方绀珠传 - 前所未见的噩梦世界");
        this.generateJukeBox(translationBuilder, ModJukeboxSongs.TH15_17.getJukeboxSongRegistryKey(), "东方绀珠传 - Pandemonic Planet");
    }

    public void generateBlockTranslations(RegistryWrapper.WrapperLookup wrapperLookup, TranslationBuilder translationBuilder) {
        translationBuilder.add(ModBlocks.DANMAKU_CRAFTING_TABLE, "弹幕工作台");
        translationBuilder.add(ModBlocks.GENSOKYO_ALTAR, "幻想乡祭坛");
        translationBuilder.add(ModBlocks.STRENGTH_TABLE, "强化台");
        translationBuilder.add(ModBlocks.SPIRITUAL_LOG, "绳文杉原木");
        translationBuilder.add(ModBlocks.SPIRITUAL_WOOD, "绳文杉树皮");
        translationBuilder.add(ModBlocks.SPIRITUAL_STAIR, "绳文杉楼梯");
        translationBuilder.add(ModBlocks.SPIRITUAL_SLAB, "绳文杉台阶");
        translationBuilder.add(ModBlocks.SPIRITUAL_DOOR, "绳文杉门");
        translationBuilder.add(ModBlocks.SPIRITUAL_TRAPDOOR, "绳文杉活版门");
        translationBuilder.add(ModBlocks.SPIRITUAL_FENCE, "绳文杉栅栏");
        translationBuilder.add(ModBlocks.SPIRITUAL_FENCE_GATE, "绳文杉栅栏门");
        translationBuilder.add(ModBlocks.SPIRITUAL_BUTTON, "绳文杉按钮");
//        translationBuilder.add(ModBlocks.SPIRITUAL_SIGN, "绳文杉牌子");
//        translationBuilder.add(ModBlocks.SPIRITUAL_HANGING_SIGN, "绳文杉牌子");
//        translationBuilder.add(ModBlocks.WALL_SPIRITUAL_HANGING_SIGN, "绳文杉牌子");

        translationBuilder.add(ModBlocks.STRIPPED_SPIRITUAL_LOG, "去皮绳文杉");
        translationBuilder.add(ModBlocks.STRIPPED_SPIRITUAL_WOOD, "去皮绳文杉树皮");
        translationBuilder.add(ModBlocks.SPIRITUAL_PLANKS, "绳文杉木板");
        translationBuilder.add(ModBlocks.MAGIC_ICE_BLOCK, "魔法冰");
        translationBuilder.add(ModBlocks.POINT_BLOCK, "Point方块");
        translationBuilder.add(ModBlocks.POWER_BLOCK, "Power方块");
        translationBuilder.add(ModBlocks.SILVER_ORE, "银矿石");
        translationBuilder.add(ModBlocks.DEEPSLATE_SILVER_ORE, "深层银矿石");
        translationBuilder.add(ModBlocks.SILVER_BLOCK, "银块");
        translationBuilder.add(ModBlocks.ORB_ORE, "宝玉矿石");
        translationBuilder.add(ModBlocks.DEEPSLATE_ORB_ORE, "深层宝玉矿石");

        translationBuilder.add(ModBlocks.MARISA_HAT_BLOCK, "魔理沙的帽子");

        translationBuilder.add(ModBlocks.DREAM_RED_BLOCK, "网格方块");
        translationBuilder.add(ModBlocks.DREAM_BLUE_BLOCK, "网格方块");
    }

    public void generateFumoTranslations(RegistryWrapper.WrapperLookup wrapperLookup, TranslationBuilder translationBuilder) {
        translationBuilder.add(unpackBlockObjectContainer(FumoBlocks.FumoEnum.AYA), "射命丸文Fumo");
        translationBuilder.add(unpackBlockObjectContainer(FumoBlocks.FumoEnum.ALICE), "爱丽丝Fumo");
        translationBuilder.add(unpackBlockObjectContainer(FumoBlocks.FumoEnum.CHEN), "八云橙Fumo");
        translationBuilder.add(unpackBlockObjectContainer(FumoBlocks.FumoEnum.BLUE_REIMU), "蓝灵梦Fumo");
        translationBuilder.add(unpackBlockObjectContainer(FumoBlocks.FumoEnum.CHERRIES_SHION), "依神紫苑Fumo");
        translationBuilder.add(unpackBlockObjectContainer(FumoBlocks.FumoEnum.CHIMATA), "天弓千亦Fumo");
        translationBuilder.add(unpackBlockObjectContainer(FumoBlocks.FumoEnum.CIRNO), "琪露诺Fumo");
        translationBuilder.add(unpackBlockObjectContainer(FumoBlocks.FumoEnum.CLOWNPIECE), "克劳恩皮丝Fumo");
        translationBuilder.add(unpackBlockObjectContainer(FumoBlocks.FumoEnum.EIKI), "四季映姫Fumo");
        translationBuilder.add(unpackBlockObjectContainer(FumoBlocks.FumoEnum.EIRIN), "八意永琳Fumo");
        translationBuilder.add(unpackBlockObjectContainer(FumoBlocks.FumoEnum.FLAN), "芙兰朵露·斯卡蕾特Fumo");
        translationBuilder.add(unpackBlockObjectContainer(FumoBlocks.FumoEnum.FLANDRE), "芙兰朵露·斯卡蕾特Fumo");
        translationBuilder.add(unpackBlockObjectContainer(FumoBlocks.FumoEnum.HATATE), "姬海棠果Fumo");
        translationBuilder.add(unpackBlockObjectContainer(FumoBlocks.FumoEnum.JOON), "依神女苑Fumo");
        translationBuilder.add(unpackBlockObjectContainer(FumoBlocks.FumoEnum.KAGUYA), "蓬莱山辉夜Fumo");
        translationBuilder.add(unpackBlockObjectContainer(FumoBlocks.FumoEnum.KANMARISA), "雾雨魔理沙Fumo");
        translationBuilder.add(unpackBlockObjectContainer(FumoBlocks.FumoEnum.KASEN), "茨木华扇Fumo");
        translationBuilder.add(unpackBlockObjectContainer(FumoBlocks.FumoEnum.KOGASA), "多多良小伞Fumo");
        translationBuilder.add(unpackBlockObjectContainer(FumoBlocks.FumoEnum.KOISHI), "古明地恋Fumo");
        translationBuilder.add(unpackBlockObjectContainer(FumoBlocks.FumoEnum.KOKORO), "秦心Fumo");
//        translationBuilder.add(unpackBlockObjectContainer(FumoBlocks.FumoEnum.KORONE), "戌神沁音Fumo");
        translationBuilder.add(unpackBlockObjectContainer(FumoBlocks.FumoEnum.KOSUZU), "本居小铃Fumo");
//        translationBuilder.add(unpackBlockObjectContainer(FumoBlocks.FumoEnum.KYOU), "Fumo");
        translationBuilder.add(unpackBlockObjectContainer(FumoBlocks.FumoEnum.MARISA), "雾雨魔理沙Fumo");
        translationBuilder.add(unpackBlockObjectContainer(FumoBlocks.FumoEnum.MARISA_HAT), "雾雨魔理沙Fumo");
        translationBuilder.add(unpackBlockObjectContainer(FumoBlocks.FumoEnum.MCKY), "天弓千亦Fumo");
        translationBuilder.add(unpackBlockObjectContainer(FumoBlocks.FumoEnum.MEILING), "红美铃Fumo");
        translationBuilder.add(unpackBlockObjectContainer(FumoBlocks.FumoEnum.MELON), "伊吹萃香Fumo");
        translationBuilder.add(unpackBlockObjectContainer(FumoBlocks.FumoEnum.MOKOU), "藤原妹红Fumo");
        translationBuilder.add(unpackBlockObjectContainer(FumoBlocks.FumoEnum.MOMIJI), "犬走椛Fumo");
        translationBuilder.add(unpackBlockObjectContainer(FumoBlocks.FumoEnum.MYSTIA), "米斯蒂娅·萝蕾拉Fumo");
        translationBuilder.add(unpackBlockObjectContainer(FumoBlocks.FumoEnum.NAZRIN), "娜兹玲Fumo");
//        translationBuilder.add(unpackBlockObjectContainer(FumoBlocks.FumoEnum.NEBULA), "Fumo");
//        translationBuilder.add(unpackBlockObjectContainer(FumoBlocks.FumoEnum.NEBUMO), "Fumo");
//        translationBuilder.add(unpackBlockObjectContainer(FumoBlocks.FumoEnum.NEBUO), "Fumo");
        translationBuilder.add(unpackBlockObjectContainer(FumoBlocks.FumoEnum.NEW_REIMU), "博丽灵梦Fumo");
        translationBuilder.add(unpackBlockObjectContainer(FumoBlocks.FumoEnum.NUE), "封兽鵺Fumo");
        translationBuilder.add(unpackBlockObjectContainer(FumoBlocks.FumoEnum.PARSEE), "水桥帕露西Fumo");
        translationBuilder.add(unpackBlockObjectContainer(FumoBlocks.FumoEnum.PATCHOULI), "帕秋莉·诺蕾姬Fumo");
        translationBuilder.add(unpackBlockObjectContainer(FumoBlocks.FumoEnum.PC98_MARISA), "雾雨魔理沙Fumo（旧作）");
        translationBuilder.add(unpackBlockObjectContainer(FumoBlocks.FumoEnum.RAN), "八云蓝Fumo");
        translationBuilder.add(unpackBlockObjectContainer(FumoBlocks.FumoEnum.REIMU), "博丽灵梦Fumo");
        translationBuilder.add(unpackBlockObjectContainer(FumoBlocks.FumoEnum.REISEN), "铃仙·优昙华院·因幡Fumo");
        translationBuilder.add(unpackBlockObjectContainer(FumoBlocks.FumoEnum.REMILIA), "蕾米莉亚·斯卡蕾特Fumo");
        translationBuilder.add(unpackBlockObjectContainer(FumoBlocks.FumoEnum.RENKO), "宇佐见莲子Fumo");
        translationBuilder.add(unpackBlockObjectContainer(FumoBlocks.FumoEnum.RUMIA), "露米娅Fumo");
        translationBuilder.add(unpackBlockObjectContainer(FumoBlocks.FumoEnum.SAKUYA), "十六夜咲夜Fumo");
        translationBuilder.add(unpackBlockObjectContainer(FumoBlocks.FumoEnum.SANAE), "东风谷早苗Fumo");
        translationBuilder.add(unpackBlockObjectContainer(FumoBlocks.FumoEnum.SATORI), "古明地觉Fumo");
        translationBuilder.add(unpackBlockObjectContainer(FumoBlocks.FumoEnum.SEIJA), "鬼人正邪Fumo");
        translationBuilder.add(unpackBlockObjectContainer(FumoBlocks.FumoEnum.SHION), "依神紫苑Fumo");
//        translationBuilder.add(unpackBlockObjectContainer(FumoBlocks.FumoEnum.SHIRO), "Fumo");
        translationBuilder.add(unpackBlockObjectContainer(FumoBlocks.FumoEnum.SHOU), "寅丸星Fumo");
        translationBuilder.add(unpackBlockObjectContainer(FumoBlocks.FumoEnum.SMART_CIRNO), "小琪露诺Fumo");
//        translationBuilder.add(unpackBlockObjectContainer(FumoBlocks.FumoEnum.SNAILCHAN), "Fumo");
        translationBuilder.add(unpackBlockObjectContainer(FumoBlocks.FumoEnum.SUIKA), "伊吹萃香Fumo");
        translationBuilder.add(unpackBlockObjectContainer(FumoBlocks.FumoEnum.SUWAKO), "洩矢诹访子Fumo");
        translationBuilder.add(unpackBlockObjectContainer(FumoBlocks.FumoEnum.TAN_CIRNO), "大琪露诺Fumo");
        translationBuilder.add(unpackBlockObjectContainer(FumoBlocks.FumoEnum.TENSHI), "比那名居天子Fumo");
        translationBuilder.add(unpackBlockObjectContainer(FumoBlocks.FumoEnum.TEWI), "因幡帝Fumo");
        translationBuilder.add(unpackBlockObjectContainer(FumoBlocks.FumoEnum.UTSUHO), "灵乌路空Fumo");
        translationBuilder.add(unpackBlockObjectContainer(FumoBlocks.FumoEnum.WAKASAGIHIME), "若鹭姬Fumo");
        translationBuilder.add(unpackBlockObjectContainer(FumoBlocks.FumoEnum.YOUMU), "魂魄妖梦Fumo");
//        translationBuilder.add(unpackBlockObjectContainer(FumoBlocks.FumoEnum.YUE), "Fumo");
        translationBuilder.add(unpackBlockObjectContainer(FumoBlocks.FumoEnum.YUKARI), "八云紫Fumo");
        translationBuilder.add(unpackBlockObjectContainer(FumoBlocks.FumoEnum.YUUKA), "风见幽香Fumo");
        translationBuilder.add(unpackBlockObjectContainer(FumoBlocks.FumoEnum.YUYUKO), "西行寺幽幽子Fumo");
    }

    public Block unpackBlockObjectContainer(FumoBlocks.FumoEnum fumoEnum) {
        return fumoEnum.getBlockObjectContainer().getValue();
    }

    public void generateDanmakuItemTranslations(RegistryWrapper.WrapperLookup wrapperLookup, TranslationBuilder translationBuilder, boolean useChinese) {
        Map<String, String> bulletTranslations = new HashMap<>();
        bulletTranslations.put("amulet", "札弹");
        bulletTranslations.put("arrow", "箭弹");
        bulletTranslations.put("arrowhead", "鳞弹");
        bulletTranslations.put("bacteria", "杆菌弹");
        bulletTranslations.put("ball", "小玉");
        bulletTranslations.put("ball_outlined", "小玉（描边）");
        bulletTranslations.put("bubble", "大玉");
        bulletTranslations.put("bullet", "铳弹");
        bulletTranslations.put("butterfly", "蝶弹");
        bulletTranslations.put("coin", "钱币弹");
        bulletTranslations.put("fireball", "火光弹");
        bulletTranslations.put("fireball_glowy", "水光弹");
        bulletTranslations.put("heart", "心弹");
        bulletTranslations.put("jellybean", "椭弹");
        bulletTranslations.put("knife", "刀弹");
        bulletTranslations.put("kunai", "链弹");
        bulletTranslations.put("mentos", "中玉");
        bulletTranslations.put("note", "音符弹");
        bulletTranslations.put("orb", "光玉");
        bulletTranslations.put("pellet", "点弹");
        bulletTranslations.put("popcorn", "菌弹");
        bulletTranslations.put("raindrop", "滴弹");
        bulletTranslations.put("rest", "音符弹（休止符）");
        bulletTranslations.put("rice", "米弹");
        bulletTranslations.put("rose", "蔷薇弹");
        bulletTranslations.put("shard", "棱角米弹");
        bulletTranslations.put("star", "星弹");
        bulletTranslations.put("star_big", "大星弹");
        bulletTranslations.put("star_small", "小星弹");

        for (DanmakuItemType itemEntry : ModItems.DANMAKU_ITEMS) {
            List<BasicDanmakuItemTypeItem> values = itemEntry.getValues();
            for (BasicDanmakuItemTypeItem item : values) {
                Identifier identifier = Registries.ITEM.getId(item);

                String path = identifier.getPath();
                String[] parts = path.split("/");

                String key = parts[parts.length - 2];

                String translationString = bulletTranslations.getOrDefault(key, "null");
                translationBuilder.add(item, translationString);
            }
        }
    }
}
