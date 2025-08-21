package cc.thonly.reverie_dreams.datagen;

import cc.thonly.mystias_izakaya.block.MIBlocks;
import cc.thonly.mystias_izakaya.entity.MIEntities;
import cc.thonly.mystias_izakaya.item.MIItemGroups;
import cc.thonly.mystias_izakaya.item.MIItems;
import cc.thonly.mystias_izakaya.registry.DrinkProperties;
import cc.thonly.mystias_izakaya.registry.FoodProperties;
import cc.thonly.reverie_dreams.EndLoaderInit;
import cc.thonly.reverie_dreams.entity.npc.NPCStates;
import cc.thonly.reverie_dreams.entity.npc.NPCWorkModes;
import cc.thonly.reverie_dreams.fumo.Fumos;
import cc.thonly.reverie_dreams.block.ModBlocks;
import cc.thonly.reverie_dreams.danmaku.DanmakuTrajectories;
import cc.thonly.reverie_dreams.danmaku.DanmakuTypes;
import cc.thonly.reverie_dreams.effect.ModPotions;
import cc.thonly.reverie_dreams.effect.ModStatusEffects;
import cc.thonly.reverie_dreams.entity.ModEntities;
import cc.thonly.reverie_dreams.entity.npc.NPCRoles;
import cc.thonly.reverie_dreams.gui.RecipeTypeCategoryManager;
import cc.thonly.reverie_dreams.item.ModItemGroups;
import cc.thonly.reverie_dreams.item.ModItems;
import cc.thonly.reverie_dreams.item.RoleCards;
import cc.thonly.reverie_dreams.sound.JukeboxSongInit;
import cc.thonly.reverie_dreams.sound.SoundEventInit;
import cc.thonly.reverie_dreams.world.GameRulesInit;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.minecraft.registry.RegistryWrapper;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class ModSimpChineseLangProvider extends FabricLanguageProvider implements TranslationCreatorImpl {

    public ModSimpChineseLangProvider(FabricDataOutput dataOutput, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup) {
        super(dataOutput, "zh_cn", registryLookup);
    }

    @Override
    public void generateTranslations(RegistryWrapper.WrapperLookup wrapperLookup, TranslationBuilder translationBuilder) {
        TranslationExporter builder = TranslationCreatorImpl.createBuilder(wrapperLookup, translationBuilder);
        builder.add(ModItemGroups.ITEM_GROUP, "Gensokyo: Reverie of Lost Dreams | 物品/方块");
        builder.add(ModItemGroups.ITEM_GROUP_BULLET, "Gensokyo: Reverie of Lost Dreams | 弹幕");
        builder.add(ModItemGroups.TEMPLATE_ITEM_GROUP_BULLET, "Gensokyo: Reverie of Lost Dreams | 弹幕模板");
        builder.add(ModItemGroups.ITEM_GROUP_FUMO, "Gensokyo: Reverie of Lost Dreams | Fumo");
        builder.add(ModItemGroups.ITEM_GROUP_ROLE_CARD, "Gensokyo: Reverie of Lost Dreams | 角色卡");
        builder.add(EndLoaderInit.ITEM_GROUP_SPAWN_EGG, "Gensokyo: Reverie of Lost Dreams | 刷怪蛋");
        builder.add(EndLoaderInit.ITEM_GROUP_NPC_SPAWN_EGG, "Gensokyo: Reverie of Lost Dreams | 角色刷怪蛋");
//        translationBuilder.add("item_group.touhou_block_and_item", "Gensokyo: Reverie of Lost Dreams - 物品/方块");
//        translationBuilder.add("item_group.touhou.bullet", "Gensokyo: Reverie of Lost Dreams - 子弹");
//        translationBuilder.add("item_group.touhou.template", "Gensokyo: Reverie of Lost Dreams - 弹幕模板");
//        translationBuilder.add("item_group.touhou.fumo", "Gensokyo: Reverie of Lost Dreams - Fumo");
//        translationBuilder.add("item_group.touhou.spawn_egg", "Gensokyo: Reverie of Lost Dreams - 刷怪蛋");
//        translationBuilder.add("item_group.touhou.role_card", "Gensokyo: Reverie of Lost Dreams - 角色卡");
//        translationBuilder.add("item_group.touhou.role.spawn_egg", "Gensokyo: Reverie of Lost Dreams - 角色刷怪蛋");
        translationBuilder.add("item.tooltip.use", "§b[右键使用]");
        translationBuilder.add("item.tooltip.use.villager", "§b[右键村民使用]");
        translationBuilder.add("item.tooltip.color", "颜色：");
        translationBuilder.add("item.tooltip.damage", "伤害：");
        translationBuilder.add("item.tooltip.speed", "速度：");
        translationBuilder.add("item.tooltip.count", "数量：");
        translationBuilder.add("item.tooltip.base_type", "弹幕轨迹：");
        translationBuilder.add(RecipeTypeCategoryManager.DANMAKU_TABLE_ICON.toTranslationKey(), "弹幕工作台");
        translationBuilder.add(RecipeTypeCategoryManager.GENSOKYO_ALTAR_ICON.toTranslationKey(), "幻想乡祭坛");
        translationBuilder.add(RecipeTypeCategoryManager.STRENGTH_TABLE_ICON.toTranslationKey(), "强化台");
        translationBuilder.add(RecipeTypeCategoryManager.KITCHEN_ICON.toTranslationKey(), "夜雀食堂");
        translationBuilder.add("message.gensokyo_altar.miss_structure", "§c结构错误");
        translationBuilder.add("message.gensokyo_altar.miss_recipe", "§c未知合成仪式");
        translationBuilder.add("message.treasure_hunting_rod.find", "离目标还有 %s 格，偏移：X=%s, Y=%s, Z=%s，目标：");
        translationBuilder.add("message.treasure_hunting_rod.not_found", "未在附近找到矿物。");
        translationBuilder.add(GameRulesInit.DO_GHOST.getTranslationKey(), "禁止幽灵生成");

        this.generateItemTranslations(wrapperLookup, translationBuilder);
        this.generateBlockTranslations(wrapperLookup, translationBuilder);
        this.generateFumoTranslations(wrapperLookup, translationBuilder);
        this.generateSoundTranslations(wrapperLookup, translationBuilder);
        this.generateEntityTranslations(wrapperLookup, translationBuilder);
        this.generateRoleTranslations(wrapperLookup, translationBuilder);
        this.generateRoleCardTranslations(wrapperLookup, translationBuilder);
        this.generateEffectTranslations(wrapperLookup, translationBuilder);
        this.generateDialogTranslations(wrapperLookup, translationBuilder);
        this.generateTestTranslations(wrapperLookup, translationBuilder);

        translationBuilder.add("gui.npc.info", "§d");
        translationBuilder.add("gui.npc.info.name", "§d名字: %s");
        translationBuilder.add("gui.npc.info.food", "§d食物信息: ");
        translationBuilder.add("gui.npc.info.food.nutrition", "§6饥饿度: %s");
        translationBuilder.add("gui.npc.info.food.saturation", "§6饱食度: %s");
        translationBuilder.add("gui.npc.info.uuid", "§d通用唯一识别码: %s");
        translationBuilder.add("gui.npc.info.health", "§d生命值: %s/%s");
        translationBuilder.add("gui.npc.info.armor", "§d护甲值: %s");

        translationBuilder.add("gui.npc.mode.null", "§b当前模式为: §c序列化错误");
        translationBuilder.add(NPCStates.FOLLOW.translationId(), "§b当前模式为: §a跟随");
        translationBuilder.add(NPCStates.NORMAL.translationId(), "§b当前模式为: §a正常");
        translationBuilder.add(NPCStates.NO_WALK.translationId(), "§b当前模式为: §a禁止移动");
        translationBuilder.add(NPCStates.SNAKING.translationId(), "§b当前模式为: §a潜行");
        translationBuilder.add(NPCStates.SEATED.translationId(), "§b当前模式为: §a坐下");
        translationBuilder.add(NPCStates.WORKING.translationId(), "§b当前模式为: §a工作中");
        translationBuilder.add("gui.npc.mode.work.originpos", "工作原点位置");

        translationBuilder.add("gui.npc.work.mode", "工作模式切换");
        translationBuilder.add(NPCWorkModes.COMBAT.translationId(), "§b当前模式为: §a清理怪物");
        translationBuilder.add(NPCWorkModes.FARM.translationId(), "§b当前模式为: §b种植作物");
        translationBuilder.add(NPCWorkModes.BREED.translationId(), "§b当前模式为: §c繁殖动物");
        translationBuilder.add(NPCWorkModes.SMELT.translationId(), "§b当前模式为: §d烧炼矿物");
//        translationBuilder.add("gui.npc.woke.mode.disable", "工作原点位置");

        translationBuilder.add("item.reverie_dreams.music.no_files", "§c未找到任何可用的音乐文件！");
        translationBuilder.add("item.reverie_dreams.music.switch_music", "§a切换音乐为：§f%s");
        translationBuilder.add("item.reverie_dreams.music.no_music_selected", "§e未选择任何音乐，潜行右键选择。");
        translationBuilder.add("item.reverie_dreams.music.playing_music", "§b播放音乐：§f%s §7[乐器: %s]");

        this.generateCommandTranslations(wrapperLookup, translationBuilder);
        this.generateMITranslations(wrapperLookup, translationBuilder);
    }

    public void generateCommandTranslations(RegistryWrapper.WrapperLookup wrapperLookup, TranslationBuilder builder) {
        builder.add("command.touhou.suggest_help", "Use /touhou help for more information");
        builder.add("command.touhou.help.title", "Help Menu:");
        builder.add("command.touhou.help.help", " - Help Menu: /touhou help");
        builder.add("command.touhou.help.recipe", " - Recipe Manager: /touhou recipe");
        builder.add("command.touhou.help.about", " - About this mod: /touhou about");
        builder.add("command.touhou.help.empty", "");

        builder.add("command.touhou.about.line1", "");
        builder.add("command.touhou.about.line2", "");
        builder.add("command.touhou.about.line3", "");
        builder.add("command.touhou.about.line4", "");
        builder.add("command.touhou.about.line5", "");
        builder.add("command.touhou.about.line6", "");
        builder.add("command.touhou.about.title", "Gensokyo: Reverie of Lost Dreams");
        builder.add("command.touhou.about.version", "Version: %s");
        builder.add("command.touhou.about.author", "Author: 稀神灵梦");
        builder.add("command.touhou.about.line10", "");
        builder.add("command.touhou.about.line11", "");

        builder.add("command.touhou.video.reload", "视频重载完成");
        builder.add("command.touhou.video.load", "视频加载中...");
        builder.add("command.touhou.video.load.done", "视频加载完成");


    }

    public void generateTestTranslations(RegistryWrapper.WrapperLookup wrapperLookup, TranslationBuilder translationBuilder) {
//        translationBuilder.add(ModItems.TEST_COLOR_DANMAKU_ITEM, "测试可染色弹幕");
    }

    public void generateMITranslations(RegistryWrapper.WrapperLookup wrapperLookup, TranslationBuilder translationBuilder) {
        TranslationExporter builder = TranslationCreatorImpl.createBuilder(wrapperLookup, translationBuilder);
        translationBuilder.add("block.feedback.working", "§c该厨具正在工作中...");

        translationBuilder.add("item.tooltip.food_properties", "属性：");
        builder.add(MIItemGroups.KITCHENWARE_ITEM_GROUP, "Gensokyo: Reverie of Lost Dreams | 厨具");
        builder.add(MIItemGroups.INGREDIENT_ITEM_GROUP, "Gensokyo: Reverie of Lost Dreams | 食材");
        builder.add(MIItemGroups.SEEDS_ITEM_GROUP, "Gensokyo: Reverie of Lost Dreams | 种子");
        builder.add(MIItemGroups.FOOD_ITEM_GROUP, "Gensokyo: Reverie of Lost Dreams | 食物");
        builder.add(MIItemGroups.DRINK_ITEM_GROUP, "Gensokyo: Reverie of Lost Dreams | 饮品");
//        translationBuilder.add("item_group.kitchenware_item_group", "Gensokyo: Reverie of Lost Dreams - 厨具");
//        translationBuilder.add("item_group.ingredients_item_group", "Gensokyo: Reverie of Lost Dreams - 食材");
//        translationBuilder.add("item_group.seed_item_group", "Gensokyo: Reverie of Lost Dreams - 种子");
//        translationBuilder.add("item_group.food_item_group", "Gensokyo: Reverie of Lost Dreams - 食物");
//        translationBuilder.add("item_group.drink_item_group", "Gensokyo: Reverie of Lost Dreams - 饮品");

        translationBuilder.add(MIBlocks.COOKING_POT, "煮锅");
        translationBuilder.add(MIBlocks.CUTTING_BOARD, "料理台");
        translationBuilder.add(MIBlocks.FRYING_PAN, "油锅");
        translationBuilder.add(MIBlocks.GRILL, "烧烤架");
        translationBuilder.add(MIBlocks.STEAMER, "蒸锅");
        translationBuilder.add(MIBlocks.MYSTIA_COOKING_POT, "夜雀⦁煮锅");
        translationBuilder.add(MIBlocks.MYSTIA_CUTTING_BOARD, "夜雀⦁料理台");
        translationBuilder.add(MIBlocks.MYSTIA_FRYING_PAN, "夜雀⦁油锅");
        translationBuilder.add(MIBlocks.MYSTIA_GRILL, "夜雀⦁烧烤架");
        translationBuilder.add(MIBlocks.MYSTIA_STEAMER, "夜雀⦁蒸锅");
        translationBuilder.add(MIBlocks.SUPER_COOKING_POT, "超⦁煮锅");
        translationBuilder.add(MIBlocks.SUPER_CUTTING_BOARD, "超⦁料理台");
        translationBuilder.add(MIBlocks.SUPER_FRYING_PAN, "超⦁油锅");
        translationBuilder.add(MIBlocks.SUPER_GRILL, "超⦁烧烤架");
        translationBuilder.add(MIBlocks.SUPER_STEAMER, "超⦁蒸锅");
        translationBuilder.add(MIBlocks.EXTREME_COOKING_POT, "极⦁煮锅");
        translationBuilder.add(MIBlocks.EXTREME_CUTTING_BOARD, "极⦁料理台");
        translationBuilder.add(MIBlocks.EXTREME_FRYING_PAN, "极⦁油锅");
        translationBuilder.add(MIBlocks.EXTREME_GRILL, "极⦁烧烤架");
        translationBuilder.add(MIBlocks.EXTREME_STEAMER, "极⦁蒸锅");
        translationBuilder.add(MIBlocks.ITEM_DISPLAY, "展示盘");
//        translationBuilder.add(MIBlocks.COOKTOP, "能量灶");
        translationBuilder.add(MIBlocks.BLACK_SALT_BLOCK, "黑盐块");

        translationBuilder.add(MIItems.MYSTIA_ICON, "东方夜雀食堂图标");

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
        translationBuilder.add(MIItems.DEW, "露水");
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
        translationBuilder.add(MIItems.BAKED_SWEET_POTATOES, "烤地瓜");
        translationBuilder.add(MIItems.BAMBOO_SHOOTS_FRIED_MEAT, "竹笋炒肉");
        translationBuilder.add(MIItems.BAMBOO_SHOOTS_STEWED_IN_STONE_POT, "石锅竹笋炖肉");
        translationBuilder.add(MIItems.BAMBOO_STEAMED_EGG, "竹筒蒸蛋");
        translationBuilder.add(MIItems.BAMBOO_TUBE_ROASTED_DRUNKEN_SHRIMP, "竹筒烧醉虾");
        translationBuilder.add(MIItems.BAMBOO_TUBE_STEAMED_PORK, "竹筒粉蒸肉");
        translationBuilder.add(MIItems.BEAR_PAW, "熊掌");
        translationBuilder.add(MIItems.BEEF_HOT_POT, "牛肉鸳鸯火锅");
        translationBuilder.add(MIItems.BEEF_RICE, "牛肉盖浇饭");
        translationBuilder.add(MIItems.BEEF_WELLINGTON, "惠灵顿牛排");
        translationBuilder.add(MIItems.BEETLE_STEAMED_CAKE, "兜甲蒸糕");
        translationBuilder.add(MIItems.BURNT_PUDDING, "燃尽布丁");
        translationBuilder.add(MIItems.BISCAY_BISCUITS, "比斯开湾饼干");
        translationBuilder.add(MIItems.BOILED_FISH, "水煮鱼");
        translationBuilder.add(MIItems.BRAISED_EEL, "红烧鳗鱼");
        translationBuilder.add(MIItems.BRAISED_PORK_WITH_PEACH, "桃子红烧肉");
        translationBuilder.add(MIItems.BUTTER_STEAK, "黄油牛排");
        translationBuilder.add(MIItems.CANDIED_CHESTNUTS, "糖栗子");
        translationBuilder.add(MIItems.CANDIED_SWEET_POTATO, "拔丝地瓜");
        translationBuilder.add(MIItems.CATS_PLAYING_IN_WATER, "猫咪戏水");
        translationBuilder.add(MIItems.CAT_FOOD, "猫饭");
        translationBuilder.add(MIItems.CAT_KULULI, "猫咕噜哩");
        translationBuilder.add(MIItems.CAT_PIZZA, "猫咪披萨");
        translationBuilder.add(MIItems.CHEESE_EGG, "芝士蛋");
        translationBuilder.add(MIItems.COLD_DISH_CARVING, "凉菜雕花");
        translationBuilder.add(MIItems.COLD_TOFU, "冷豆腐");
        translationBuilder.add(MIItems.COLORFUL_JADE_FRIED_BUNS, "华光玉煎包");
        translationBuilder.add(MIItems.COOKING_TOFU, "煮豆腐");
        translationBuilder.add(MIItems.CREAM_STEW, "奶油炖菜");
        translationBuilder.add(MIItems.CRISP_CYCLONE, "脆旋风");
        translationBuilder.add(MIItems.DARK_CUISINE, "黑暗物质");
        translationBuilder.add(MIItems.DEEP_FRIED_CICADA_SHELLS, "炸蝉蜕");
        translationBuilder.add(MIItems.DEPRESSED_CHEESE_STICKS, "忧郁芝士条");
        translationBuilder.add(MIItems.DEW_BOILED_EGGS, "露水煮蛋");
        translationBuilder.add(MIItems.DORAYAKI, "铜锣烧");
        translationBuilder.add(MIItems.DUMPLING, "饺子");
        translationBuilder.add(MIItems.EEL_EGG_DONBURI, "鳗鱼蛋盖饭");
        translationBuilder.add(MIItems.EGGS_BENEDICT, "班尼迪克蛋");
        translationBuilder.add(MIItems.ENERGY_STRING, "能量串");
        translationBuilder.add(MIItems.FAILING_SAKURA_SNOW, "樱落雪");
        translationBuilder.add(MIItems.FANTASY_IS_ALL_THE_RAGE, "幻想风靡");
        translationBuilder.add(MIItems.FISH_LEAPS_OVER_DRAGON_GATE, "鱼跃龙门");
        translationBuilder.add(MIItems.FLOWERS_BIRDS_WIND_AND_MOON, "花鸟风月");
        translationBuilder.add(MIItems.FLOWING_WATER_NOODLES, "流水素面");
        translationBuilder.add(MIItems.FRIED_HAGFISH, "炸八目鳗");
        translationBuilder.add(MIItems.FRIED_PORK_CUTLET, "炸猪排");
        translationBuilder.add(MIItems.FRIED_PORK_SHREDS, "炒肉丝");
        translationBuilder.add(MIItems.FRIED_SHRIMP_TEMPURA, "炸虾天妇罗");
        translationBuilder.add(MIItems.FRIED_TOFU, "炸豆腐");
        translationBuilder.add(MIItems.FRIED_TOMATO_STRIPS, "炸番茄条");
        translationBuilder.add(MIItems.FRIGHT_ADVENTURE, "惊吓！大冒险");
        translationBuilder.add(MIItems.GAME_SOUP, "野味杂烩汤");
        translationBuilder.add(MIItems.GENSOKYO_BUDDHA_JUMPS_OVER_THE_WALL, "幻想佛跳墙");
        translationBuilder.add(MIItems.GENSOKYO_STAR_LOTUS_SHIP, "幻想星莲船");
        translationBuilder.add(MIItems.GIANT_TAMAGOYAKI, "巨人玉子烧");
        translationBuilder.add(MIItems.GINKGO_AND_RADISH_PORK_RIB_SOUP, "银杏萝卜排骨汤");
        translationBuilder.add(MIItems.GLOOMY_FRUIT_PIE, "忧郁水果派");
        translationBuilder.add(MIItems.GLUTINOUS_RICE_BALLS, "汤圆");
        translationBuilder.add(MIItems.GOLDEN_CRISPY_FISH_CAKE, "金黄酥鱼饼");
        translationBuilder.add(MIItems.GRAND_BANQUET, "大奢宴");
        translationBuilder.add(MIItems.GREEN_BAMBOO_WELCOMES_SPRING, "翠竹迎春");
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
        translationBuilder.add(MIItems.MOCHI, "麻薯");
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
        translationBuilder.add(MIItems.PAN_FRIED_MUSHROOM_MEAT_ROLL, "香煎双菇肉卷");
        translationBuilder.add(MIItems.PAN_FRIED_SALMON, "煎三文鱼");
        translationBuilder.add(MIItems.PEACH_BLOSSOM_GLAZE_ROLL, "桃花琉璃卷");
        translationBuilder.add(MIItems.PEACH_BLOSSOM_SOUP, "桃花羹");
        translationBuilder.add(MIItems.PHOENIX, "不死鸟");
        translationBuilder.add(MIItems.PICKLED_CUCUMBERS, "腌黄瓜");
        translationBuilder.add(MIItems.PIG_DEER_BUTTERFLY, "猪鹿蝶");
        translationBuilder.add(MIItems.PINE_NUT_CAKE, "松子糕");
        translationBuilder.add(MIItems.PIRATE_BACON, "海盗熏肉");
        translationBuilder.add(MIItems.PLUM_TEA_RICE, "梅子茶泡饭");
        translationBuilder.add(MIItems.POETRY_AND_GINKGO, "诗礼银杏");
        translationBuilder.add(MIItems.POISONOUS_GARDEN, "毒瘴花园");
        translationBuilder.add(MIItems.PORK_AND_TROUT_SMOKED, "猪肉鳟鱼熏");
        translationBuilder.add(MIItems.PORK_RICE, "猪肉盖浇饭");
        translationBuilder.add(MIItems.POTATO_CROQUETTES, "土豆可乐饼");
        translationBuilder.add(MIItems.PSEUDO_JIRITAMA, "拟尻子玉");
        translationBuilder.add(MIItems.PUMPKIN_SHRIMP_CAKE, "南瓜虾盅");
        translationBuilder.add(MIItems.RAPUNZEL, "长发公主");
        translationBuilder.add(MIItems.REAL_SEAFOOD_MISO_SOUP, "真·海鲜味噌汤");
        translationBuilder.add(MIItems.RED_BEAN_DAIFUKU, "红豆大福");
        translationBuilder.add(MIItems.REFRESHING_PUDDING, "提神布丁");
        translationBuilder.add(MIItems.REVERSING_THE_WORLD, "逆转天地！");
        translationBuilder.add(MIItems.RICE_BALL, "饭团");
        translationBuilder.add(MIItems.RISOTTO, "意式烩饭");
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
        translationBuilder.add(MIItems.SHIRAGA_SADAMATSU, "白鹿贞松");
        translationBuilder.add(MIItems.SKINNY_HORSE_DUMPLING, "瘦马团子");
        translationBuilder.add(MIItems.SNOW_WHITE, "白雪公主");
        translationBuilder.add(MIItems.STEAMED_EGG_WITH_SEA_URCHIN, "海胆蒸蛋");
        translationBuilder.add(MIItems.STINKY_TOFU, "臭豆腐");
        translationBuilder.add(MIItems.STRENGTH_SOUP, "力量汤");
        translationBuilder.add(MIItems.SUPERME_SEAFOOD_NOODLES, "至尊海鲜面");
        translationBuilder.add(MIItems.TAICHI_BAGUA_FISH_MAW, "太极八卦鱼肚");
        translationBuilder.add(MIItems.TAKETORIHIME, "辉夜姬");
        translationBuilder.add(MIItems.TAKOYAKI, "章鱼烧");
        translationBuilder.add(MIItems.THE_BEAUTY_OF_HAN_PALACE, "汉宫藏娇");
        translationBuilder.add(MIItems.THE_DREAM, "幽梦");
        translationBuilder.add(MIItems.THE_MARS, "火星料理");
        translationBuilder.add(MIItems.THE_SOURCE_OF_LIFE, "生命之源");
        translationBuilder.add(MIItems.TIANSHI_BRAISED_CHESTNUT_MUSHROOMS, "天师板栗焖菇");
        translationBuilder.add(MIItems.TOFU_MISO, "豆腐味噌");
        translationBuilder.add(MIItems.TOFU_POT, "豆腐锅");
        translationBuilder.add(MIItems.TONKOTSU_RAMEN, "豚骨拉面");
        translationBuilder.add(MIItems.TOON_PANCAKES, "香椿煎饼");
        translationBuilder.add(MIItems.TWO_HEAVENS_ONE_STYLE, "二天一流");
        translationBuilder.add(MIItems.UDUMBARA_CAKE, "幻昙花糕");
        translationBuilder.add(MIItems.UNCONSCIOUS_MONSTER_MOUSSE, "无意识怪物慕斯");
        translationBuilder.add(MIItems.VEGETABLE_SPECIAL, "蔬菜特辑");
        translationBuilder.add(MIItems.WARM_RICE_BALL, "温热饭团");
        translationBuilder.add(MIItems.WHITE_PEACH_EIGHT_BRIDGE, "白桃八桥");
        translationBuilder.add(MIItems.YUNSHAN_COTTON_CANDY, "云山棉花糖");
        translationBuilder.add(MIItems.ZHAJI, "炸脊");

        translationBuilder.add(MIItems.GREEN_TEA, "绿茶");
        translationBuilder.add(MIItems.FRUITY_HIGH_BALL, "果味High Ball");
        translationBuilder.add(MIItems.FRUITY_SOUR, "果味Sour");
        translationBuilder.add(MIItems.QI, "淇");
        translationBuilder.add(MIItems.BEER, "超ZUN啤酒");
        translationBuilder.add(MIItems.SUN_MOON_STAR, "日月星");
        translationBuilder.add(MIItems.PLUM_WINE, "梅酒");
        translationBuilder.add(MIItems.TENGU_DANCE, "天狗踊");
        translationBuilder.add(MIItems.SCARLET_DEVIL, "猩红恶魔");
        translationBuilder.add(MIItems.GODS_WHEAT, "神之麦");
        translationBuilder.add(MIItems.OTTER_FESTIVAL, "水獭祭");
        translationBuilder.add(MIItems.DAWN, "水獭祭");
        translationBuilder.add(MIItems.SPARROW_SAKE, "雀酒");
        translationBuilder.add(MIItems.SCARLET_DEVIL_MANSION_BLACK_TEA, "红魔馆红茶");
        translationBuilder.add(MIItems.AFFGADO, "阿芙加朵");
        translationBuilder.add(MIItems.RED_MIST, "红雾");
        translationBuilder.add(MIItems.NEGRONI, "尼格罗尼");
        translationBuilder.add(MIItems.GODFATHER, "教父");
        translationBuilder.add(MIItems.BLESSING_WIND, "风祝");
        translationBuilder.add(MIItems.WINTER_BREW, "冬酿");
        translationBuilder.add(MIItems.FOURTEENTH_NIGHT, "十四夜");
        translationBuilder.add(MIItems.FIRE_RAT_FUR, "火鼠裘");
        translationBuilder.add(MIItems.GYOKURO_TEA, "玉露茶");
        translationBuilder.add(MIItems.MOON_ROCKET, "月面火箭");
        translationBuilder.add(MIItems.MILK, "牛奶");
        translationBuilder.add(MIItems.RED_GRAPEFRUIT_JUICE, "红柚果汁");
        translationBuilder.add(MIItems.SODA, "波子汽水");
        translationBuilder.add(MIItems.ICEBERG_MAPLE_FROZEN_LEMON, "冰山毛玉冻柠");
        translationBuilder.add(MIItems.BIG_POPSICLE, "“大冰棍儿！”");
        translationBuilder.add(MIItems.DAIGINJO, "大吟酿");
        translationBuilder.add(MIItems.COFFEE, "咖啡");
        translationBuilder.add(MIItems.FAIRY_RAIN, "妖精雨露");
        translationBuilder.add(MIItems.PALEO_CREAMY_SMOOTHIE, "古法奶油冰沙");
        translationBuilder.add(MIItems.ORDINARY_FITNESS_TEA, "普通健身茶");
        translationBuilder.add(MIItems.DEMON_SLAYER, "鬼杀");
        translationBuilder.add(MIItems.QI_HEALTH, "气保健");
        translationBuilder.add(MIItems.KOMEIJI_ICE_CREAM, "古明地冰激凌");
        translationBuilder.add(MIItems.MANGO_POMELO_SAGO, "杨枝甘露");
        translationBuilder.add(MIItems.QILIN, "麒麟");
        translationBuilder.add(MIItems.HEAVEN_AND_EARTH_ARE_USELESS, "天地无用");
        translationBuilder.add(MIItems.DRUNK_ACTOR, "伶人醉");
        translationBuilder.add(MIItems.DAUGHTER_OF_THE_SEA, "海的女儿");
        translationBuilder.add(MIItems.DEMONIC_COFFEE, "魔界咖啡");
        translationBuilder.add(MIItems.MOJITO_BURST_BALL, "莫吉托爆浆球");
        translationBuilder.add(MIItems.SPACE_BEER, "太空啤酒");
        translationBuilder.add(MIItems.SATELLITE_ICED_COFFEE, "卫星冰咖啡");

        translationBuilder.add(FoodProperties.UNDEFINED.translateKey(), "未定义");
        translationBuilder.add(FoodProperties.MEAT.translateKey(), "肉类");
        translationBuilder.add(FoodProperties.AQUATIC_PRODUCTS.translateKey(), "水产");
        translationBuilder.add(FoodProperties.VEGETARIAN.translateKey(), "素食");
        translationBuilder.add(FoodProperties.HOMESTYLE.translateKey(), "家常");
        translationBuilder.add(FoodProperties.GOURMET.translateKey(), "高级");
        translationBuilder.add(FoodProperties.LEGENDARY.translateKey(), "传说");
        translationBuilder.add(FoodProperties.GREASY.translateKey(), "重油");
        translationBuilder.add(FoodProperties.LIGHT.translateKey(), "清淡");
        translationBuilder.add(FoodProperties.GOOD_WITH_ALCOHOL.translateKey(), "下酒");
        translationBuilder.add(FoodProperties.FILLING.translateKey(), "饱腹");
        translationBuilder.add(FoodProperties.MOUNTAIN_DELICACY.translateKey(), "山珍");
        translationBuilder.add(FoodProperties.OCEAN_FLAVOR.translateKey(), "海味");
        translationBuilder.add(FoodProperties.JAPANESE_STYLE.translateKey(), "和风");
        translationBuilder.add(FoodProperties.WESTERN_STYLE.translateKey(), "西式");
        translationBuilder.add(FoodProperties.CHINESE_STYLE.translateKey(), "中式");
        translationBuilder.add(FoodProperties.SALTY.translateKey(), "咸");
        translationBuilder.add(FoodProperties.UMAMI.translateKey(), "鲜");
        translationBuilder.add(FoodProperties.SWEET.translateKey(), "甜");
        translationBuilder.add(FoodProperties.RAW.translateKey(), "生");
        translationBuilder.add(FoodProperties.PHOTOGENIC.translateKey(), "适合拍照");
        translationBuilder.add(FoodProperties.COOL.translateKey(), "凉爽");
        translationBuilder.add(FoodProperties.FIERY.translateKey(), "灼热");
        translationBuilder.add(FoodProperties.POWER_SURGE.translateKey(), "力量涌现");
        translationBuilder.add(FoodProperties.BIZARRE.translateKey(), "猎奇");
        translationBuilder.add(FoodProperties.CULTURAL_DEPTH.translateKey(), "文化底蕴");
        translationBuilder.add(FoodProperties.MUSHROOMS.translateKey(), "菌类");
        translationBuilder.add(FoodProperties.UNBELIEVABLE.translateKey(), "难以置信");
        translationBuilder.add(FoodProperties.PETITE.translateKey(), "小巧");
        translationBuilder.add(FoodProperties.DREAMLIKE.translateKey(), "梦幻");
        translationBuilder.add(FoodProperties.LOCAL_SPECIALTY.translateKey(), "特产");
        translationBuilder.add(FoodProperties.FRUITY.translateKey(), "果味");
        translationBuilder.add(FoodProperties.SOUP_AND_STEW.translateKey(), "汤羹");
        translationBuilder.add(FoodProperties.GRILLED.translateKey(), "烧烤");
        translationBuilder.add(FoodProperties.SPICY.translateKey(), "辣");
        translationBuilder.add(FoodProperties.FLAMING.translateKey(), "燃起来了");
        translationBuilder.add(FoodProperties.SOUR.translateKey(), "酸");
        translationBuilder.add(FoodProperties.TOXIC.translateKey(), "有毒");
        translationBuilder.add(FoodProperties.DARK_CUISINE.translateKey(), "黑暗物质");
        translationBuilder.add(FoodProperties.ECONOMICAL.translateKey(), "实惠");
        translationBuilder.add(FoodProperties.EXPENSIVE.translateKey(), "昂贵");
        translationBuilder.add(FoodProperties.LARGE_PARTITION.translateKey(), "大份");
        translationBuilder.add(FoodProperties.POPULAR_NEGATIVE.translateKey(), "不流行");
        translationBuilder.add(FoodProperties.POPULAR_POSITIVE.translateKey(), "流行的");
        translationBuilder.add(FoodProperties.SIGNATURE.translateKey(), "招牌");
        translationBuilder.add(FoodProperties.CURSE.translateKey(), "诅咒");

        translationBuilder.add(DrinkProperties.UNDEFINED.translateKey(), "未定义");
        translationBuilder.add(DrinkProperties.ALCOHOL_FREE.translateKey(), "无酒精");
        translationBuilder.add(DrinkProperties.LOW_ALCOHOL.translateKey(), "低酒精");
        translationBuilder.add(DrinkProperties.MID_ALCOHOL.translateKey(), "中酒精");
        translationBuilder.add(DrinkProperties.HIGH_ALCOHOL.translateKey(), "高酒精");
        translationBuilder.add(DrinkProperties.CAN_ADD_ICE.translateKey(), "可加冰");
        translationBuilder.add(DrinkProperties.CAN_HEATED.translateKey(), "可加热");
        translationBuilder.add(DrinkProperties.COCKTAIL.translateKey(), "鸡尾酒");
        translationBuilder.add(DrinkProperties.WESTERN_WINE.translateKey(), "西洋酒");
        translationBuilder.add(DrinkProperties.FRUIT.translateKey(), "水果味");
        translationBuilder.add(DrinkProperties.SWEET.translateKey(), "甘");
        translationBuilder.add(DrinkProperties.BITTER.translateKey(), "苦");
        translationBuilder.add(DrinkProperties.SOJU.translateKey(), "烧酒");
        translationBuilder.add(DrinkProperties.SAKE.translateKey(), "清酒");
        translationBuilder.add(DrinkProperties.PUNGENT.translateKey(), "辛");
        translationBuilder.add(DrinkProperties.BUBBLE.translateKey(), "气泡");
        translationBuilder.add(DrinkProperties.BEER.translateKey(), "啤酒");
        translationBuilder.add(DrinkProperties.DIRECT_DRINKING.translateKey(), "直饮");
        translationBuilder.add(DrinkProperties.LIQUEUR.translateKey(), "利口酒");
        translationBuilder.add(DrinkProperties.REFRESHING.translateKey(), "提神");
        translationBuilder.add(DrinkProperties.CLASSICAL.translateKey(), "古典");
        translationBuilder.add(DrinkProperties.MODERN.translateKey(), "现代");

        // 植物
        translationBuilder.add(MIBlocks.UDUMBARA_FLOWER, "幻昙华花");
        translationBuilder.add(MIBlocks.TREMELLA, "银耳丛");

        // 种子
        MIBlocks.CHILL.generateTranslation(translationBuilder, "辣椒种子");
        MIBlocks.CUCUMBER.generateTranslation(translationBuilder, "黄瓜种子");
        MIBlocks.GRAPE.generateTranslation(translationBuilder, "葡萄种子");
        MIBlocks.ONION.generateTranslation(translationBuilder, "洋葱种子");
        MIBlocks.RED_BEANS.generateTranslation(translationBuilder, "红豆种子");
        MIBlocks.TOMATO.generateTranslation(translationBuilder, "番茄种子");
        MIBlocks.TOON.generateTranslation(translationBuilder, "香椿种子");
        MIBlocks.WHITE_RADISH.generateTranslation(translationBuilder, "白萝卜种子");
        MIBlocks.SWEET_POTATO.generateTranslation(translationBuilder, "红薯种子");
        MIBlocks.BROCCOLI.generateTranslation(translationBuilder, "西兰花种子");
        MIBlocks.SOY_BEANS.generateTranslation(translationBuilder, "黄豆种子");
    }

    public void generateEntityTranslations(RegistryWrapper.WrapperLookup wrapperLookup, TranslationBuilder translationBuilder) {
        TranslationExporter builder = TranslationCreatorImpl.createBuilder(wrapperLookup, translationBuilder);
        builder.add(ModEntities.FUMO_SELLER_VILLAGER, "Fumo贩卖商人", "Fumo贩卖商人刷怪蛋");
        builder.add(ModEntities.KILLER_BEE_ENTITY_TYPE, "杀人蜂", "杀人蜂刷怪蛋");
        builder.add(ModEntities.GHOST_ENTITY_TYPE, "幽灵", "幽灵刷怪蛋");
        builder.add(ModEntities.YOUSEI_ENTITY_TYPE, "妖精", "妖精刷怪蛋");
        builder.add(ModEntities.SUNFLOWER_YOUSEI_ENTITY_TYPE, "向日葵妖精", "向日葵妖精刷怪蛋");
        builder.add(ModEntities.GOBLIN_ENTITY_TYPE, "哥布林", "哥布林刷怪蛋");
        builder.add(ModEntities.WATER_ELEMENTAL_ENTITY_TYPE, "水元素", "水元素刷怪蛋");
        builder.add(ModEntities.FIRE_ELEMENTAL_ENTITY_TYPE, "火元素", "火元素刷怪蛋");
        builder.add(ModEntities.ICE_ELEMENTAL_ENTITY_TYPE, "冰元素", "冰元素刷怪蛋");
        builder.add(ModEntities.BROOM_ENTITY_TYPE, "魔法扫帚", "魔法扫帚刷怪蛋");
        builder.add(ModEntities.HAIRBALL_ENTITY_TYPE, "毛玉", "毛玉刷怪蛋");
        builder.add(ModEntities.MUSHROOM_MONSTER_ENTITY_TYPE, "蘑菇", "蘑菇刷怪蛋");

        builder.add(MIEntities.WILD_PIG_ENTITY_TYPE, "野猪", "野猪刷怪蛋");
        builder.add(MIEntities.TAVERN_VILLAGER, "酒馆老板", "酒馆老板刷怪蛋");

    }

    public void generateRoleCardTranslations(RegistryWrapper.WrapperLookup wrapperLookup, TranslationBuilder translationBuilder) {
        translationBuilder.add(RoleCards.PROTAGONIST_GROUP.translationKey(), "§c主角组§r角色卡");
        translationBuilder.add(RoleCards.KOUMAKYOU.translationKey(), "§c红魔乡§r角色卡");
        translationBuilder.add(RoleCards.YOUYOUMU.translationKey(), "§5妖妖梦§r角色卡");
        translationBuilder.add(RoleCards.EIYASHOU.translationKey(), "§d永夜抄§r角色卡");
        translationBuilder.add(RoleCards.KAEIZUKA.translationKey(), "§a花映塚§r角色卡");
        translationBuilder.add(RoleCards.FUUJINROKU.translationKey(), "§a风神录§r角色卡");
        translationBuilder.add(RoleCards.CHIREIDEN.translationKey(), "§3地灵殿§r角色卡");
        translationBuilder.add(RoleCards.SEIRENSEN.translationKey(), "§e星莲船§r角色卡");
        translationBuilder.add(RoleCards.SHINREIBYOU.translationKey(), "§e神灵庙§r角色卡");
        translationBuilder.add(RoleCards.KISHINJOU.translationKey(), "§6辉针城§r角色卡");
        translationBuilder.add(RoleCards.KANJUDEN.translationKey(), "§4绀珠传§r角色卡");
        translationBuilder.add(RoleCards.TENKUUSHOU.translationKey(), "§a天空璋§r角色卡");
        translationBuilder.add(RoleCards.KIKEIJUU.translationKey(), "§c鬼形兽§r角色卡");
        translationBuilder.add(RoleCards.KOURYUUDOU.translationKey(), "§b虹龙洞§r角色卡");
        translationBuilder.add(RoleCards.JUUOUEN.translationKey(), "§2兽王园§r角色卡");
        translationBuilder.add(RoleCards.KINJOUKYOU.translationKey(), "§f锦上京§r角色卡");
        translationBuilder.add(RoleCards.SANGETSUSEI.translationKey(), "§f三月精§r角色卡");
        translationBuilder.add(RoleCards.HIFUU.translationKey(), "§d秘封§r角色卡");
        translationBuilder.add(RoleCards.TASOGARE_FURONTIA.translationKey(), "§6黄昏边境§r角色卡");
    }

    public void generateRoleTranslations(RegistryWrapper.WrapperLookup wrapperLookup, TranslationBuilder translationBuilder) {
        TranslationExporter builder = TranslationCreatorImpl.createBuilder(wrapperLookup, translationBuilder);
        // 主角组
        builder.addRoleEntity(NPCRoles.REIMU, "博丽灵梦", "刷怪蛋");
        builder.addRoleEntity(NPCRoles.CYAN_REIMU, "青灵梦", "刷怪蛋");
        builder.addRoleEntity(NPCRoles.MARISA, "雾雨魔理沙", "刷怪蛋");
        builder.addRoleEntity(NPCRoles.RUMIA, "露米娅", "刷怪蛋");
        builder.addRoleEntity(NPCRoles.CIRNO, "琪露诺", "刷怪蛋");
        builder.addRoleEntity(NPCRoles.MEIRIN, "红美铃", "刷怪蛋");
        builder.addRoleEntity(NPCRoles.PATCHOULI, "帕秋莉·诺蕾姬", "刷怪蛋");
        builder.addRoleEntity(NPCRoles.SAKUYA, "十六夜咲夜", "刷怪蛋");
        builder.addRoleEntity(NPCRoles.REMILIA, "蕾米莉亚·斯卡蕾特", "刷怪蛋");
        builder.addRoleEntity(NPCRoles.FLANDRE, "芙兰朵露·斯卡蕾特", "刷怪蛋");

        // 妖妖梦
        builder.addRoleEntity(NPCRoles.LETTY_WHITEROCK, "蕾蒂·霍瓦特洛克", "刷怪蛋");
        builder.addRoleEntity(NPCRoles.CHEN, "八云橙", "刷怪蛋");
        builder.addRoleEntity(NPCRoles.ALICE, "爱丽丝·玛格特罗依德", "刷怪蛋");
        builder.addRoleEntity(NPCRoles.LILY_WHITE, "莉莉白", "刷怪蛋");
        builder.addRoleEntity(NPCRoles.LUNASA_PRISMRIVER, "露娜萨·普莉兹姆利巴", "刷怪蛋");
        builder.addRoleEntity(NPCRoles.MERLIN_PRISMRIVER, "梅露兰·普莉兹姆利巴", "刷怪蛋");
        builder.addRoleEntity(NPCRoles.LYRICA_PRISMRIVER, "莉莉卡·普莉兹姆利巴", "刷怪蛋");
        builder.addRoleEntity(NPCRoles.RAN, "八云蓝", "刷怪蛋");
        builder.addRoleEntity(NPCRoles.YOUMU, "魂魄妖梦", "刷怪蛋");
        builder.addRoleEntity(NPCRoles.YUYUKO, "西行寺幽幽子", "刷怪蛋");
        builder.addRoleEntity(NPCRoles.YUKARI, "八云紫", "刷怪蛋");

        // 永夜抄
        builder.addRoleEntity(NPCRoles.MYSTIA_LORELEI, "米斯蒂娅·萝蕾拉", "刷怪蛋");
        builder.addRoleEntity(NPCRoles.WRIGGLE_NIGHTBUG, "莉格露·奈特巴格", "刷怪蛋");
        builder.addRoleEntity(NPCRoles.KAMISHIRASAWA_KEINE, "上白泽慧音", "刷怪蛋");
        builder.addRoleEntity(NPCRoles.REISEN, "铃仙·优昙华院·因幡", "刷怪蛋");
        builder.addRoleEntity(NPCRoles.ERIN, "八意永琳", "刷怪蛋");
        builder.addRoleEntity(NPCRoles.HOURAISAN_KAGUYA, "蓬莱山辉夜", "刷怪蛋");
        builder.addRoleEntity(NPCRoles.HUZIWARA_NO_MOKOU, "藤原妹红", "刷怪蛋");

        // 花映塚
        builder.addRoleEntity(NPCRoles.SHIKIEIKI_YAMAXANADU, "四季映姬·夜摩仙那度", "刷怪蛋");
        builder.addRoleEntity(NPCRoles.KAZAMI_YUKA, "风见幽香", "刷怪蛋");

        // 风神录
        builder.addRoleEntity(NPCRoles.KAGIYAMA_HINA, "键山雏", "刷怪蛋");
        builder.addRoleEntity(NPCRoles.INUBASHIRI_MOMIZI, "犬走椛", "刷怪蛋");
        builder.addRoleEntity(NPCRoles.KAWASIRO_NITORI, "河城荷取", "刷怪蛋");
        builder.addRoleEntity(NPCRoles.AYA, "射命丸文", "刷怪蛋");
        builder.addRoleEntity(NPCRoles.KOCHIYA_SANAE, "东风谷早苗", "刷怪蛋");
        builder.addRoleEntity(NPCRoles.YASAKA_KANAKO, "八坂神奈子", "刷怪蛋");
        builder.addRoleEntity(NPCRoles.MORIYA_SUWAKO, "洩矢诹访子", "刷怪蛋");

        // 地灵殿
        builder.addRoleEntity(NPCRoles.KISUME, "琪斯美", "刷怪蛋");
        builder.addRoleEntity(NPCRoles.KURODANI_YAMAME, "黑谷山女", "刷怪蛋");
        builder.addRoleEntity(NPCRoles.MIZUHASHI_PARSEE, "水桥帕露西", "刷怪蛋");
        builder.addRoleEntity(NPCRoles.HOSHIGUMA_YUGI, "星熊勇仪", "刷怪蛋");
        builder.addRoleEntity(NPCRoles.KAENBYOU_RIN, "火焰猫燐", "刷怪蛋");
        builder.addRoleEntity(NPCRoles.KOMEIJI_SATORI, "古明地觉", "刷怪蛋");
        builder.addRoleEntity(NPCRoles.REIUJI_UTSUH, "灵乌路空", "刷怪蛋");
        builder.addRoleEntity(NPCRoles.KOMEIJI_KOISHI, "古明地恋", "刷怪蛋");
        builder.addRoleEntity(NPCRoles.WHITE_KOMEIJI_KOISHI, "白色古明地恋", "刷怪蛋");

        // 星莲船
        builder.addRoleEntity(NPCRoles.NAZRIN, "纳兹琳", "刷怪蛋");
        builder.addRoleEntity(NPCRoles.TATARA_KOGASA, "多多良小伞", "刷怪蛋");
        builder.addRoleEntity(NPCRoles.NUE, "封兽鵺", "刷怪蛋");

        // 神灵庙
        builder.addRoleEntity(NPCRoles.KASODANI_KYOUKO, "幽谷响子", "刷怪蛋");
        builder.addRoleEntity(NPCRoles.MIYAKO_YOSHIKA, "宫古芳香", "刷怪蛋");
        builder.addRoleEntity(NPCRoles.KAKU_SEIGA, "霍青娥", "刷怪蛋");
        builder.addRoleEntity(NPCRoles.SOGA_NO_TOZIKO, "苏我屠自古", "刷怪蛋");
        builder.addRoleEntity(NPCRoles.MONONOBE_NO_FUTO, "物部布都", "刷怪蛋");
        builder.addRoleEntity(NPCRoles.TOYOSATOMIMI_NO_MIKO, "丰聪耳神子", "刷怪蛋");
        builder.addRoleEntity(NPCRoles.HOUJUU_NUE, "封兽鵺", "刷怪蛋");
        builder.addRoleEntity(NPCRoles.HUTATSUIWA_MAMIZOU, "二岩猯藏", "刷怪蛋");

        // 辉针城
        builder.addRoleEntity(NPCRoles.WAKASAGIHIME, "若狭姬", "刷怪蛋");
        builder.addRoleEntity(NPCRoles.SEKIBANKI, "赤蛮奇", "刷怪蛋");
        builder.addRoleEntity(NPCRoles.IMAIZUMI_KAGEROU, "今泉影狼", "刷怪蛋");
        builder.addRoleEntity(NPCRoles.KIJIN_SEIJIA, "鬼人正邪", "刷怪蛋");
        builder.addRoleEntity(NPCRoles.SUKUNA_SHINMYOUMARU, "少名针妙丸", "刷怪蛋");
        builder.addRoleEntity(NPCRoles.HORIKAWA_RAIKO, "堀川雷鼓", "刷怪蛋");

        // 绀珠传
        builder.addRoleEntity(NPCRoles.SEIRAN, "清兰", "刷怪蛋");
        builder.addRoleEntity(NPCRoles.RINGO, "铃瑚", "刷怪蛋");
        builder.addRoleEntity(NPCRoles.DOREMY_SWEET, "哆来咪·苏伊特", "刷怪蛋");
        builder.addRoleEntity(NPCRoles.KISIN_SAGUME, "稀神探女", "刷怪蛋");
        builder.addRoleEntity(NPCRoles.CLOWNPIECE, "克劳恩皮丝", "刷怪蛋");
        builder.addRoleEntity(NPCRoles.JUNKO, "纯狐", "刷怪蛋");
        builder.addRoleEntity(NPCRoles.HECATIA_LAPISLAZULI, "赫卡提亚·拉碧斯拉祖利", "刷怪蛋");

        // 天空璋
        builder.addRoleEntity(NPCRoles.ETERNITY_LARVA, "爱塔妮堤·拉尔瓦", "刷怪蛋");
        builder.addRoleEntity(NPCRoles.SAKUTA_NEMUNO, "坂田合欢乃", "刷怪蛋");
        builder.addRoleEntity(NPCRoles.KOMANO_AUNN, "高丽野阿吽", "刷怪蛋");
        builder.addRoleEntity(NPCRoles.YATADERA_NARUMI, "矢田寺成美", "刷怪蛋");
        builder.addRoleEntity(NPCRoles.NISHIDA_SATONO, "丁礼田舞", "刷怪蛋");
        builder.addRoleEntity(NPCRoles.TEIREIDA_MAI, "尔子田里乃", "刷怪蛋");
        builder.addRoleEntity(NPCRoles.MATARA_OKINA, "摩多罗隐岐奈", "刷怪蛋");

        // 鬼形兽
        builder.addRoleEntity(NPCRoles.EBISU_EIKA, "戎璎花", "刷怪蛋");
        builder.addRoleEntity(NPCRoles.USHIZAKI_URUMI, "牛崎润美", "刷怪蛋");
        builder.addRoleEntity(NPCRoles.NIWATARI_KUTAKA, "庭渡久侘歌", "刷怪蛋");
        builder.addRoleEntity(NPCRoles.KITCHO_YACHIE, "吉吊八千慧", "刷怪蛋");
        builder.addRoleEntity(NPCRoles.JOUTOUGU_MAYUMI, "杖刀偶磨弓", "刷怪蛋");
        builder.addRoleEntity(NPCRoles.HANIYASUSHIN_KEIKI, "埴安神袿姬", "刷怪蛋");
        builder.addRoleEntity(NPCRoles.KUROKOMA_SAKI, "骊驹早鬼", "刷怪蛋");

        // 虹龙洞
        builder.addRoleEntity(NPCRoles.GOUTOKUZI_MIKE, "豪德寺三花", "刷怪蛋");
        builder.addRoleEntity(NPCRoles.YAMASHIRO_TAKANE, "山城高岭", "刷怪蛋");
        builder.addRoleEntity(NPCRoles.KOMAKUSA_SANNYO, "驹草山如", "刷怪蛋");
        builder.addRoleEntity(NPCRoles.TAMATSUKURI_MISUMARU, "玉造魅须丸", "刷怪蛋");
        builder.addRoleEntity(NPCRoles.KUDAMAKI_TSUKASA, "菅牧典", "刷怪蛋");
        builder.addRoleEntity(NPCRoles.IIZUNAMARU_MEGUMU, "饭纲丸龙", "刷怪蛋");
        builder.addRoleEntity(NPCRoles.TENKYU_CHIMATA, "天弓千亦", "刷怪蛋");
        builder.addRoleEntity(NPCRoles.HIMEMUSHI_MOMOYO, "姬虫百百世", "刷怪蛋");


        // 三月精
        builder.addRoleEntity(NPCRoles.STAR, "斯塔·萨菲雅", "刷怪蛋");
        builder.addRoleEntity(NPCRoles.LUNAR, "露娜·切露德", "刷怪蛋");
        builder.addRoleEntity(NPCRoles.SUNNY, "桑尼·米尔克", "刷怪蛋");

        // 其他
        builder.addRoleEntity(NPCRoles.USAMI_RENKO, "宇佐见莲子", "刷怪蛋");
        builder.addRoleEntity(NPCRoles.MARIBEL_HEARN, "玛艾露贝莉·赫恩", "刷怪蛋");

        // 黄昏
        builder.addRoleEntity(NPCRoles.SUIKA, "伊吹萃香", "刷怪蛋");
        builder.addRoleEntity(NPCRoles.TENSHI, "比那名居天子", "刷怪蛋");
    }

    public void generateEffectTranslations(RegistryWrapper.WrapperLookup wrapperLookup, TranslationBuilder translationBuilder) {
        TranslationExporter builder = TranslationCreatorImpl.createBuilder(wrapperLookup, translationBuilder);
        builder.add(ModStatusEffects.ELIXIR_OF_LIFE.value(), "不死");
        builder.add(ModStatusEffects.MENTAL_DISORDER.value(), "精神错乱");
        builder.add(ModStatusEffects.BACK_OF_LIFE.value(), "返生");
        builder.add(ModStatusEffects.KANJU_KUSURI.value(), "绀珠");

        builder.generatePotion(ModPotions.ELIXIR_OF_LIFE_POTION.value(), "蓬莱之药", "喷溅型蓬莱之药", "滞留型蓬莱之药");
        builder.generatePotion(ModPotions.MENTAL_DISORDER_POTION.value(), "精神错乱药水", "喷溅型精神错乱药水", "滞留型精神错乱药水");
        builder.generatePotion(ModPotions.BACK_OF_LIFE_POTION.value(), "还生药", "喷溅型还生药", "滞留型还生药");
        builder.generatePotion(ModPotions.KANJU_KUSURI_POTION.value(), "绀珠药", "喷溅型绀珠药", "滞留型绀珠药");

    }

    public void generateDialogTranslations(RegistryWrapper.WrapperLookup wrapperLookup, TranslationBuilder translationBuilder) {
        translationBuilder.add("dialog.message.select", "请选择一个角色");

        translationBuilder.add("dialog.title.main", "东方入门手册");
        translationBuilder.add("dialog.title.altar", "祭坛摆放");
        translationBuilder.add("dialog.title.danmaku", "弹幕合成");
        translationBuilder.add("dialog.title.upgrade_danmaku", "升级弹幕");
        translationBuilder.add("dialog.title.fumo", "Fumo 指南");
        translationBuilder.add("dialog.title.role", "角色养成");
        translationBuilder.add("dialog.title.mystia.main", "夜雀食堂");
        translationBuilder.add("dialog.title.cn.issue", "QQ开发/反馈群");
        translationBuilder.add("dialog.title.other_mod_list", "其他配套Mod列表");
        translationBuilder.add("dialog.title.open_recipe_manager", "打开配方管理器");

        translationBuilder.add("dialog.text.empty", "空");
        translationBuilder.add("dialog.text.exit", "退出");
        translationBuilder.add("dialog.text.random", "随机");
        translationBuilder.add("dialog.text.back", "返回");

        translationBuilder.add("dialog.main.welcome", "欢迎游玩Gensokyo: Reverie of Lost Dreams，本屏幕将介绍模组的游玩指南");
        translationBuilder.add("dialog.main.description.0", "当前版本为 %s");

        translationBuilder.add("dialog.altar.material", "材料");
        translationBuilder.add("dialog.altar.ways", "摆放方式");

        translationBuilder.add("dialog.danmaku.description.0", "弹幕工作台是一个用于合成弹幕物品的工作方块，里面共有5个槽位可以摆放物品合成位。");
        translationBuilder.add("dialog.danmaku.description.1", "摆放示例：");

        translationBuilder.add("dialog.upgrade_danmaku.description.0","%s可升级提升弹幕的战斗力");
        translationBuilder.add("dialog.upgrade_danmaku.description.1","目前有效强化道具有：");

        translationBuilder.add("dialog.fumo.description.0", "Fumo 是《东方 Project》角色为原型的毛绒玩偶，绝大多数 Fumo 可在生存模式内通过较为昂贵的交易获得，且放置后会立刻转向玩家当前所在位置，一共有16个方向可以转向放置，右键 Fumo 方块可以发出 Fumo 声音。");
        translationBuilder.add("dialog.fumo.description.1", "Fumo 商人每天的交易内容都会变化");
        translationBuilder.add("dialog.fumo.description.2", "商人创建方式：使用%s对村民右键转化为Fumo商人");

        translationBuilder.add("dialog.role.description.0", "角色（Role）是本 Mod 中的随从类实体，类似 Touhou Little Maid，拥有背包，战斗，工作模式，睡眠和饥饿等模块，可以使用蛋糕驯服");
        translationBuilder.add("dialog.role.description.1", "一般生存模式下可通过角色卡召唤，角色卡可通过幻想乡祭坛合成");
        translationBuilder.add("dialog.role.description.2", "角色死亡后会掉落物品%s，可在在祭坛摆放一圈钻石*2重新复活角色");

        translationBuilder.add(
                "dialog.touhou_mystia.description.0",
                "夜雀食堂 —— 原作《东方夜雀食堂 - Touhou Mystia's Izakaya》"
        );
        translationBuilder.add(
                "dialog.touhou_mystia.description.1",
                "这是本 Mod 的一个模块，添加了原作中的所有食材、食物、酒水及烹饪配方，并对原版掉落物进行了调整。"
        );
        translationBuilder.add(
                "dialog.touhou_mystia.description.2",
                "农作物种子主要通过打开世界中的战利品宝箱获得，海产品则可通过相应的钓鱼或采集方式获取。"
        );
        translationBuilder.add(
                "dialog.touhou_mystia.description.3",
                "烹饪系统可识别 Farmer's Delight Refabricated、Borukva Food 以及 Borukva Food Exotic 中的食材，并支持相应的 TAG 和可替代配方。"
        );
        translationBuilder.add(
                "dialog.touhou_mystia.description.4",
                "厨具主要分为煮锅、料理台、油锅、烧烤架和蒸锅五种及其升级版。烹饪配方可通过夜雀食堂维基查询，食材搭配不当可能产生黑暗物质哦。"
        );
        translationBuilder.add(
                "dialog.touhou_mystia.description.5",
                ""
        );
        translationBuilder.add(
                "dialog.touhou_mystia.description.6",
                "酒水获取指南"
        );
        translationBuilder.add(
                "dialog.touhou_mystia.description.7",
                "普通酒水可通过与 %s 交易获得。饮用后会获得不同的效果，每天可交易的酒水及其加成可能会变化，但绿茶、果味 High Ball 和果味 SOUR 始终会出现在交易列表中。"
        );
        translationBuilder.add(
                "dialog.touhou_mystia.description.8",
                "手持%s对村民右键即可将村民升级为%s。"
        );
        translationBuilder.add(
                "dialog.touhou_mystia.wiki",
                "打开夜雀食堂维基"
        );

        translationBuilder.add("dialog.other_mod_list.description.0", "本页面列举了对本 Mod 游玩有帮助的辅助性 Mod");

    }

    public void generateItemTranslations(RegistryWrapper.WrapperLookup wrapperLookup, TranslationBuilder translationBuilder) {
        // 调试
        translationBuilder.add(ModItems.BATTLE_STICK, "战斗调试棒");
        translationBuilder.add(ModItems.OWNER_STICK, "主权调试棒");

        // 图标
        translationBuilder.add(ModItems.ICON, "Gensokyo: Reverie of Lost Dreams");
        translationBuilder.add(ModItems.FUMO_ICON, "毛绒玩偶图标");
        translationBuilder.add(ModItems.SPAWN_EGG, "刷怪蛋");
        translationBuilder.add(ModItems.DANMAKU, "弹幕");

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
        translationBuilder.add(ModItems.SPEED_FEATHER, "速度羽毛");

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
        translationBuilder.add(ModItems.FUMO_LICENSE, "Fumo销售许可");
        translationBuilder.add(ModItems.CURSED_DECOY_DOLl, "被诅咒的诱饵人偶");
        translationBuilder.add(ModItems.VAISRAVANAS_PAGODA, "毘沙门天的宝塔");
        translationBuilder.add(ModItems.DREAM_PILLOW, "梦境枕头");
        translationBuilder.add(ModItems.TENGU_SHIELD, "天狗手盾");
        translationBuilder.add(ModItems.TENGU_CAMERA, "天狗相机");
        translationBuilder.add(ModItems.BAD_APPLE, "Bad Apple!!");

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
        translationBuilder.add(ModItems.DEATH_SCYTHE, "死神镰刀");

        // 工具矿物类
        translationBuilder.add(ModItems.RAW_SILVER, "生银矿");
        translationBuilder.add(ModItems.SILVER_INGOT, "银锭");
        translationBuilder.add(ModItems.SILVER_NUGGET, "银粒");
        translationBuilder.add(ModItems.SILVER_SWORD, "银剑");
        translationBuilder.add(ModItems.SILVER_AXE, "银斧");
        translationBuilder.add(ModItems.SILVER_PICKAXE, "银镐");
        translationBuilder.add(ModItems.SILVER_SHOVEL, "银锹");
        translationBuilder.add(ModItems.SILVER_HOE, "银锄");
        translationBuilder.add(ModItems.SILVER_HELMET, "银头盔");
        translationBuilder.add(ModItems.SILVER_CHESTPLATE, "银胸甲");
        translationBuilder.add(ModItems.SILVER_LEGGINGS, "银护腿");
        translationBuilder.add(ModItems.SILVER_BOOTS, "银靴子");
        translationBuilder.add(ModItems.ICE_SCALES, "冰之鳞");
        translationBuilder.add(ModItems.MAGIC_ICE_SWORD, "冰剑");
        translationBuilder.add(ModItems.MAGIC_ICE_AXE, "冰斧");
        translationBuilder.add(ModItems.MAGIC_ICE_PICKAXE, "冰稿");
        translationBuilder.add(ModItems.MAGIC_ICE_SHOVEL, "冰锹");
        translationBuilder.add(ModItems.MAGIC_ICE_HOE, "冰锄");
        translationBuilder.add(ModItems.MAGIC_ICE_HELMET, "冰头盔");
        translationBuilder.add(ModItems.MAGIC_ICE_CHESTPLATE, "冰胸甲");
        translationBuilder.add(ModItems.MAGIC_ICE_LEGGINGS, "冰护腿");
        translationBuilder.add(ModItems.MAGIC_ICE_BOOTS, "冰靴子");
        translationBuilder.add(ModItems.MAID_HAIRBAND, "女仆发卡");
        translationBuilder.add(ModItems.MAID_UPPER_SKIRT, "女仆上衣");
        translationBuilder.add(ModItems.MAID_LOWER_SKIRT, "女仆裙");
        translationBuilder.add(ModItems.MAID_SHOE, "女仆鞋子");
        translationBuilder.add(ModItems.SPELL_CARD_TEMPLATE, "符卡模板");


        // 角色卡
        translationBuilder.add(ModItems.ROLE_CARD, "空白角色卡");
        translationBuilder.add(ModItems.ROLE_ARCHIVE, "角色存档卡");

        // 其他


//        translationBuilder.add(ModItems.DEBUG_DANMAKU_ITEM, "调试弹幕");
//        translationBuilder.add(ModItems.DEBUG_SPELL_CARD_ITEM, "调试符卡");
//        translationBuilder.add(ModItems.DEBUG_SPELL_CARD_ITEM2, "调试符卡2");

        this.generateDanmakuItemTranslations(wrapperLookup, translationBuilder, true);
    }

    public void generateSoundTranslations(RegistryWrapper.WrapperLookup wrapperLookup, TranslationBuilder translationBuilder) {
        TranslationExporter builder = TranslationCreatorImpl.createBuilder(wrapperLookup, translationBuilder);

        for (var sound : SoundEventInit.FUMO_SOUNDS) {
            builder.generateSoundEventSubtitle(sound, "fumo");
        }
        builder.generateSoundEventSubtitle(SoundEventInit.BIU, "满身疮痍");
        builder.generateSoundEventSubtitle(SoundEventInit.POINT, "收点");
        builder.generateSoundEventSubtitle(SoundEventInit.SPELL_CARD, "符卡释放");
        builder.generateSoundEventSubtitle(SoundEventInit.UP, "升级");
        builder.generateSoundEventSubtitle(SoundEventInit.FIRE, "弹幕发射");
        builder.generateSoundEventSubtitle(SoundEventInit.BAGUA, "魔炮");
        builder.generateSoundEventSubtitle(SoundEventInit.PHOTO, "拍照");
        builder.generateSoundEventSubtitle(SoundEventInit.TICK_WAVE, "波");

        this.generateDiscTranslations(wrapperLookup, translationBuilder);
        this.generateDanmakuType(wrapperLookup, translationBuilder);
    }

    public void generateDanmakuType(RegistryWrapper.WrapperLookup wrapperLookup, TranslationBuilder translationBuilder) {
        TranslationExporter builder = TranslationCreatorImpl.createBuilder(wrapperLookup, translationBuilder);

        builder.generateDanmakuType(DanmakuTrajectories.SINGLE, "线性");
        builder.generateDanmakuType(DanmakuTrajectories.TRIPLE, "三线");
        builder.generateDanmakuType(DanmakuTrajectories.BULLET, "子弹");
        builder.generateDanmakuType(DanmakuTrajectories.HEART, "心形");
        builder.generateDanmakuType(DanmakuTrajectories.X, "十字");
        builder.generateDanmakuType(DanmakuTrajectories.STAR, "星星");
        builder.generateDanmakuType(DanmakuTrajectories.ROUND, "圆");
        builder.generateDanmakuType(DanmakuTrajectories.RING, "环");
    }

    public void generateDiscTranslations(RegistryWrapper.WrapperLookup wrapperLookup, TranslationBuilder translationBuilder) {
        TranslationExporter builder = TranslationCreatorImpl.createBuilder(wrapperLookup, translationBuilder);

        builder.generateJukeBox(JukeboxSongInit.HR01_01.getJukeboxSongRegistryKey(), "蓬莱人形　～ Dolls in Pseudo Paradise. - 蓬莱伝説");
        builder.generateJukeBox(JukeboxSongInit.HR02_08.getJukeboxSongRegistryKey(), "莲台野夜行 - 过去的花 ～ Fairy of Flower");
        builder.generateJukeBox(JukeboxSongInit.HR03_01.getJukeboxSongRegistryKey(), "ZUN - 童祭　～ Innocent Treasures");
        builder.generateJukeBox(JukeboxSongInit.MELODIC_TASTE_NIGHTMARE_BEFORE_CROSSROADS.getJukeboxSongRegistryKey(), "Melodic-Taste-Nightmare-before-Crossroads");
        builder.generateJukeBox(JukeboxSongInit.YV_FLOWER_CLOCK_AND_DREAMS.getJukeboxSongRegistryKey(), "Yonder-Voice - 花時計と夢");
        builder.generateJukeBox(JukeboxSongInit.GLOWING_NEEDLES_LITTLE_PEOPLE.getJukeboxSongRegistryKey(), "Inchlings of the Shining Needle ~ Little Princess : 「Miracle Remix」");
        builder.generateJukeBox(JukeboxSongInit.COOKIE.getJukeboxSongRegistryKey(), "温馨的神社（迫真）");
        builder.generateJukeBox(JukeboxSongInit.BAD_APPLE.getJukeboxSongRegistryKey(), "Bad Apple");
    }

    public void generateBlockTranslations(RegistryWrapper.WrapperLookup wrapperLookup, TranslationBuilder translationBuilder) {
        translationBuilder.add(ModBlocks.DANMAKU_CRAFTING_TABLE, "弹幕工作台");
        translationBuilder.add(ModBlocks.GENSOKYO_ALTAR, "幻想乡祭坛");
        translationBuilder.add(ModBlocks.STRENGTH_TABLE, "强化台");
        translationBuilder.add(ModBlocks.MUSIC_BLOCK, "音乐盒");

        translationBuilder.add(ModBlocks.MAGIC_ICE_BLOCK, "魔法冰");
        translationBuilder.add(ModBlocks.POINT_BLOCK, "Point方块");
        translationBuilder.add(ModBlocks.POWER_BLOCK, "Power方块");
        translationBuilder.add(ModBlocks.SILVER_ORE, "银矿石");
        translationBuilder.add(ModBlocks.DEEPSLATE_SILVER_ORE, "深层银矿石");
        translationBuilder.add(ModBlocks.SILVER_BLOCK, "银块");
        translationBuilder.add(ModBlocks.ORB_ORE, "宝玉矿石");
        translationBuilder.add(ModBlocks.DEEPSLATE_ORB_ORE, "深层宝玉矿石");
        translationBuilder.add(ModBlocks.RED_ORB_BLOCK, "红宝玉块");
        translationBuilder.add(ModBlocks.YELLOW_ORB_BLOCK, "黄宝玉块");
        translationBuilder.add(ModBlocks.BLUE_ORB_BLOCK, "蓝宝玉块");
        translationBuilder.add(ModBlocks.GREEN_ORB_BLOCK, "绿宝玉块");
        translationBuilder.add(ModBlocks.PURPLE_ORB_BLOCK, "紫宝玉块");

        translationBuilder.add(ModBlocks.MARISA_HAT_BLOCK, "魔理沙的帽子");

        translationBuilder.add(ModBlocks.DREAM_RED_BLOCK, "网格方块");
        translationBuilder.add(ModBlocks.DREAM_BLUE_BLOCK, "网格方块");

        translationBuilder.add(ModBlocks.SPIRITUAL.log(), "绳文杉原木");
        translationBuilder.add(ModBlocks.SPIRITUAL.wood(), "绳文杉树皮");
        translationBuilder.add(ModBlocks.SPIRITUAL.stair(), "绳文杉楼梯");
        translationBuilder.add(ModBlocks.SPIRITUAL.slab(), "绳文杉台阶");
        translationBuilder.add(ModBlocks.SPIRITUAL.door(), "绳文杉门");
        translationBuilder.add(ModBlocks.SPIRITUAL.trapdoor(), "绳文杉活版门");
        translationBuilder.add(ModBlocks.SPIRITUAL.fence(), "绳文杉栅栏");
        translationBuilder.add(ModBlocks.SPIRITUAL.fenceGate(), "绳文杉栅栏门");
        translationBuilder.add(ModBlocks.SPIRITUAL.button(), "绳文杉按钮");
        translationBuilder.add(ModBlocks.SPIRITUAL.strippedLog(), "去皮绳文杉");
        translationBuilder.add(ModBlocks.SPIRITUAL.strippedWood(), "去皮绳文杉树皮");
        translationBuilder.add(ModBlocks.SPIRITUAL.leaves(), "绳文杉树叶");
        translationBuilder.add(ModBlocks.SPIRITUAL.sapling(), "绳文杉树苗");
        translationBuilder.add(ModBlocks.SPIRITUAL.planks(), "绳文杉木板");

        translationBuilder.add(ModBlocks.ICE_SCALES.block(), "冰鳞砖块");
        translationBuilder.add(ModBlocks.ICE_SCALES.stair(), "冰鳞砖楼梯");
        translationBuilder.add(ModBlocks.ICE_SCALES.slab(), "冰鳞砖台阶");
        translationBuilder.add(ModBlocks.ICE_SCALES.wall(), "冰鳞砖墙");

        translationBuilder.add(MIBlocks.LEMON.log(), "柠檬原木");
        translationBuilder.add(MIBlocks.LEMON.wood(), "柠檬树皮");
        translationBuilder.add(MIBlocks.LEMON.stair(), "柠檬楼梯");
        translationBuilder.add(MIBlocks.LEMON.slab(), "柠檬台阶");
        translationBuilder.add(MIBlocks.LEMON.door(), "柠檬门");
        translationBuilder.add(MIBlocks.LEMON.trapdoor(), "柠檬活版门");
        translationBuilder.add(MIBlocks.LEMON.fence(), "柠檬栅栏");
        translationBuilder.add(MIBlocks.LEMON.fenceGate(), "柠檬栅栏门");
        translationBuilder.add(MIBlocks.LEMON.button(), "柠檬按钮");
        translationBuilder.add(MIBlocks.LEMON.strippedLog(), "去皮柠檬");
        translationBuilder.add(MIBlocks.LEMON.strippedWood(), "去皮柠檬树皮");
        translationBuilder.add(MIBlocks.LEMON.leaves(), "柠檬树叶");
        translationBuilder.add(MIBlocks.LEMON.sapling(), "柠檬树苗");
        translationBuilder.add(MIBlocks.LEMON.planks(), "柠檬木板");
        translationBuilder.add(MIBlocks.LEMON_FRUIT_LEAVES, "柠檬果树叶");

        translationBuilder.add(MIBlocks.GINKGO.log(), "白果原木");
        translationBuilder.add(MIBlocks.GINKGO.wood(), "白果树皮");
        translationBuilder.add(MIBlocks.GINKGO.stair(), "白果楼梯");
        translationBuilder.add(MIBlocks.GINKGO.slab(), "白果台阶");
        translationBuilder.add(MIBlocks.GINKGO.door(), "白果门");
        translationBuilder.add(MIBlocks.GINKGO.trapdoor(), "白果活版门");
        translationBuilder.add(MIBlocks.GINKGO.fence(), "白果栅栏");
        translationBuilder.add(MIBlocks.GINKGO.fenceGate(), "白果栅栏门");
        translationBuilder.add(MIBlocks.GINKGO.button(), "白果按钮");
        translationBuilder.add(MIBlocks.GINKGO.strippedLog(), "去皮白果");
        translationBuilder.add(MIBlocks.GINKGO.strippedWood(), "去皮白果树皮");
        translationBuilder.add(MIBlocks.GINKGO.leaves(), "白果树叶");
        translationBuilder.add(MIBlocks.GINKGO.sapling(), "白果树苗");
        translationBuilder.add(MIBlocks.GINKGO.planks(), "白果木板");
        translationBuilder.add(MIBlocks.GINKGO_FRUIT_LEAVES, "白果果树叶");

        translationBuilder.add(MIBlocks.PEACH.log(), "桃木原木");
        translationBuilder.add(MIBlocks.PEACH.wood(), "桃木树皮");
        translationBuilder.add(MIBlocks.PEACH.stair(), "桃木楼梯");
        translationBuilder.add(MIBlocks.PEACH.slab(), "桃木台阶");
        translationBuilder.add(MIBlocks.PEACH.door(), "桃木门");
        translationBuilder.add(MIBlocks.PEACH.trapdoor(), "桃木活版门");
        translationBuilder.add(MIBlocks.PEACH.fence(), "桃木栅栏");
        translationBuilder.add(MIBlocks.PEACH.fenceGate(), "桃木栅栏门");
        translationBuilder.add(MIBlocks.PEACH.button(), "桃木按钮");
        translationBuilder.add(MIBlocks.PEACH.strippedLog(), "去皮桃木");
        translationBuilder.add(MIBlocks.PEACH.strippedWood(), "去皮桃木树皮");
        translationBuilder.add(MIBlocks.PEACH.leaves(), "桃木树叶");
        translationBuilder.add(MIBlocks.PEACH.sapling(), "桃木树苗");
        translationBuilder.add(MIBlocks.PEACH.planks(), "桃木木板");
        translationBuilder.add(MIBlocks.PEACH_FRUIT_LEAVES, "桃果树叶");
    }

    public void generateFumoTranslations(RegistryWrapper.WrapperLookup wrapperLookup, TranslationBuilder translationBuilder) {
        translationBuilder.add(Fumos.AYA.block(), "射命丸文Fumo");
        translationBuilder.add(Fumos.ALICE.block(), "爱丽丝Fumo");
        translationBuilder.add(Fumos.CHEN.block(), "八云橙Fumo");
        translationBuilder.add(Fumos.BLUE_REIMU.block(), "蓝灵梦Fumo");
        translationBuilder.add(Fumos.CHERRIES_SHION.block(), "依神紫苑Fumo");
        translationBuilder.add(Fumos.CHIMATA.block(), "天弓千亦Fumo");
        translationBuilder.add(Fumos.CIRNO.block(), "琪露诺Fumo");
        translationBuilder.add(Fumos.CLOWNPIECE.block(), "克劳恩皮丝Fumo");
        translationBuilder.add(Fumos.EIKI.block(), "四季映姫Fumo");
        translationBuilder.add(Fumos.EIRIN.block(), "八意永琳Fumo");
        translationBuilder.add(Fumos.FLAN.block(), "芙兰朵露·斯卡蕾特Fumo");
        translationBuilder.add(Fumos.FLANDRE.block(), "芙兰朵露·斯卡蕾特Fumo");
        translationBuilder.add(Fumos.HATATE.block(), "姬海棠果Fumo");
        translationBuilder.add(Fumos.JOON.block(), "依神女苑Fumo");
        translationBuilder.add(Fumos.KAGUYA.block(), "蓬莱山辉夜Fumo");
        translationBuilder.add(Fumos.KANMARISA.block(), "雾雨魔理沙Fumo");
        translationBuilder.add(Fumos.KASEN.block(), "茨木华扇Fumo");
        translationBuilder.add(Fumos.KOGASA.block(), "多多良小伞Fumo");
        translationBuilder.add(Fumos.KOISHI.block(), "古明地恋Fumo");
        translationBuilder.add(Fumos.KOKORO.block(), "秦心Fumo");
        translationBuilder.add(Fumos.KOSUZU.block(), "本居小铃Fumo");
        translationBuilder.add(Fumos.MARISA.block(), "雾雨魔理沙Fumo");
        translationBuilder.add(Fumos.MARISA_HAT.block(), "雾雨魔理沙Fumo");
        translationBuilder.add(Fumos.MCKY.block(), "天弓千亦Fumo");
        translationBuilder.add(Fumos.MEILING.block(), "红美铃Fumo");
        translationBuilder.add(Fumos.MELON.block(), "伊吹萃香Fumo");
        translationBuilder.add(Fumos.MOKOU.block(), "藤原妹红Fumo");
        translationBuilder.add(Fumos.MOMIJI.block(), "犬走椛Fumo");
        translationBuilder.add(Fumos.MYSTIA.block(), "米斯蒂娅·萝蕾拉Fumo");
        translationBuilder.add(Fumos.NAZRIN.block(), "娜兹玲Fumo");
        translationBuilder.add(Fumos.NEW_REIMU.block(), "博丽灵梦Fumo");
        translationBuilder.add(Fumos.NUE.block(), "封兽鵺Fumo");
        translationBuilder.add(Fumos.PARSEE.block(), "水桥帕露西Fumo");
        translationBuilder.add(Fumos.PATCHOULI.block(), "帕秋莉·诺蕾姬Fumo");
        translationBuilder.add(Fumos.PC98_MARISA.block(), "雾雨魔理沙Fumo（旧作）");
        translationBuilder.add(Fumos.RAN.block(), "八云蓝Fumo");
        translationBuilder.add(Fumos.REIMU.block(), "博丽灵梦Fumo");
        translationBuilder.add(Fumos.REISEN.block(), "铃仙·优昙华院·因幡Fumo");
        translationBuilder.add(Fumos.REMILIA.block(), "蕾米莉亚·斯卡蕾特Fumo");
        translationBuilder.add(Fumos.RENKO.block(), "宇佐见莲子Fumo");
        translationBuilder.add(Fumos.RUMIA.block(), "露米娅Fumo");
        translationBuilder.add(Fumos.SAKUYA.block(), "十六夜咲夜Fumo");
        translationBuilder.add(Fumos.SANAE.block(), "东风谷早苗Fumo");
        translationBuilder.add(Fumos.SATORI.block(), "古明地觉Fumo");
        translationBuilder.add(Fumos.SEIJA.block(), "鬼人正邪Fumo");
        translationBuilder.add(Fumos.SHION.block(), "依神紫苑Fumo");
        translationBuilder.add(Fumos.SHOU.block(), "寅丸星Fumo");
        translationBuilder.add(Fumos.SMART_CIRNO.block(), "小琪露诺Fumo");
        translationBuilder.add(Fumos.SUIKA.block(), "伊吹萃香Fumo");
        translationBuilder.add(Fumos.SUWAKO.block(), "洩矢诹访子Fumo");
        translationBuilder.add(Fumos.TAN_CIRNO.block(), "大琪露诺Fumo");
        translationBuilder.add(Fumos.TENSHI.block(), "比那名居天子Fumo");
        translationBuilder.add(Fumos.TEWI.block(), "因幡帝Fumo");
        translationBuilder.add(Fumos.UTSUHO.block(), "灵乌路空Fumo");
        translationBuilder.add(Fumos.WAKASAGIHIME.block(), "若鹭姬Fumo");
        translationBuilder.add(Fumos.YOUMU.block(), "魂魄妖梦Fumo");
        translationBuilder.add(Fumos.YUKARI.block(), "八云紫Fumo");
        translationBuilder.add(Fumos.YUUKA.block(), "风见幽香Fumo");
        translationBuilder.add(Fumos.YUYUKO.block(), "西行寺幽幽子Fumo");
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

        translationBuilder.add(DanmakuTypes.AMULET.getItem(), "札弹");
        translationBuilder.add(DanmakuTypes.ARROWHEAD.getItem(), "鳞弹");
        translationBuilder.add(DanmakuTypes.BALL.getItem(), "小玉");
        translationBuilder.add(DanmakuTypes.BUBBLE.getItem(), "大玉");
        translationBuilder.add(DanmakuTypes.BULLET.getItem(), "铳弹");
        translationBuilder.add(DanmakuTypes.FIREBALL.getItem(), "火光弹");
        translationBuilder.add(DanmakuTypes.FIREBALL_GLOWY.getItem(), "水光弹");
        translationBuilder.add(DanmakuTypes.KUNAI.getItem(), "链弹");
        translationBuilder.add(DanmakuTypes.RICE.getItem(), "米弹");
        translationBuilder.add(DanmakuTypes.STAR.getItem(), "星弹");
        translationBuilder.add(DanmakuTypes.LASER.getItem(), "激光");
        translationBuilder.add(DanmakuTypes.BIG_LASER.getItem(), "大激光");

//        for (IDanmakuItem itemEntry : ModItems.DANMAKU_ITEMS) {
//            List<BasicDanmakuItemTypeItem> values = itemEntry.getValues();
//            for (BasicDanmakuItemTypeItem item : values) {
//                Identifier identifier = Registries.ITEM.getId(item);
//
//                String path = identifier.getPath();
//                String[] parts = path.split("/");
//
//                String key = parts[parts.length - 2];
//
//                String translationString = bulletTranslations.getOrDefault(key, "null");
//                translationBuilder.add(item, translationString);
//            }
//        }
    }
}
