package cc.thonly.reverie_dreams.datagen;

import cc.thonly.reverie_dreams.block.creator.CropBlockCreator;
import cc.thonly.reverie_dreams.creative_tab.content.*;
import cc.thonly.reverie_dreams.entity.villager.RDVillagerProfessions;
import cc.thonly.reverie_dreams.gui.RecipeTypeCategoryManager;
import cc.thonly.reverie_dreams.registry.content.*;
import cc.thonly.reverie_dreams.registry.content.advancements.RDAdvancements;
import cc.thonly.reverie_dreams.registry.content.block.KitchenBlocks;
import cc.thonly.reverie_dreams.registry.content.block.RDBlocks;
import cc.thonly.reverie_dreams.registry.content.block.RDCropBlocks;
import cc.thonly.reverie_dreams.registry.content.block.RDWoodBlocks;
import cc.thonly.reverie_dreams.registry.content.danmaku.DanmakuTrajectories;
import cc.thonly.reverie_dreams.registry.content.danmaku.DanmakuTypes;
import cc.thonly.reverie_dreams.registry.content.effect.RDPotions;
import cc.thonly.reverie_dreams.registry.content.effect.RDStatusEffects;
import cc.thonly.reverie_dreams.registry.content.entity.RDEntityTypes;
import cc.thonly.reverie_dreams.registry.content.item.RDDrinkItems;
import cc.thonly.reverie_dreams.registry.content.item.RDFoodItems;
import cc.thonly.reverie_dreams.registry.content.item.RDIngredientItems;
import cc.thonly.reverie_dreams.registry.content.item.RDItems;
import cc.thonly.reverie_dreams.sound.JukeboxSongInit;
import cc.thonly.reverie_dreams.sound.SoundEventInit;
import cc.thonly.reverie_dreams.world.GameRulesInit;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.npc.villager.VillagerProfession;

import java.util.concurrent.CompletableFuture;

public class SimpChineseLangProvider extends FabricLanguageProvider implements ITranslationExporter {

    public SimpChineseLangProvider(FabricDataOutput dataOutput, CompletableFuture<HolderLookup.Provider> registryLookup) {
        super(dataOutput, "zh_cn", registryLookup);
    }

    @Override
    public void generateTranslations(HolderLookup.Provider wrapperLookup, TranslationBuilder translationBuilder) {
        TranslationWrapper builder = ITranslationExporter.ofWrapper(wrapperLookup, translationBuilder);
        builder.add(ItemBlockCreativeTab.ITEM_GROUP, "梦隐的幻想乡 | 物品/方块");
        builder.add(DanmakuCreativeTab.ITEM_GROUP_BULLET, "梦隐的幻想乡 | 弹幕");
        builder.add(TemplateCreativeTab.ITEM_GROUP, "梦隐的幻想乡 | 弹幕模板");
        builder.add(FumoCreativeTab.ITEM_GROUP, "梦隐的幻想乡 | Fumo");
        builder.add(RoleCardCreativeTab.ITEM_GROUP, "梦隐的幻想乡 | 角色卡");
        builder.add(SpawnEggCreativeTab.ITEM_GROUP, "梦隐的幻想乡 | 刷怪蛋");
        builder.add(NPCSpawnEggCreativeTab.ITEM_GROUP, "梦隐的幻想乡 | 角色刷怪蛋");
        translationBuilder.add("reverie_dreams.midnightlib.title", "配置页面");
        translationBuilder.add("item.action.click.left", "§b左键查看");
        translationBuilder.add("item.action.click.right", "§b右键查看");
        translationBuilder.add("item.action.click.shape_recipe.success", "§a成功");
        translationBuilder.add("item.action.click.shape_recipe.fail", "§c无效配方");
        translationBuilder.add("item.action.click.cashbox.success", "§f你受到了神社的祝福，信仰值+%s");
        translationBuilder.add("item.action.click.cashbox.fails.coin", "§f缺少钱币物品");
        translationBuilder.add("item.action.click.cashbox.fails.used", "§c你今天已经被祝福过了哦");
        translationBuilder.add("item.action.click.cashbox.fails.full", "§c你的信仰值已超过了%s哦");
        translationBuilder.add("item.action.click.upgraded_health.max_full", "§c你的最大生命值已超过了%s§c哦");
        translationBuilder.add("item.tooltip.use", "§b[右键使用]");
        translationBuilder.add("item.tooltip.use.villager", "§b[右键村民使用]");
        translationBuilder.add("item.tooltip.shape", "形状：");
        translationBuilder.add("item.tooltip.color", "颜色：");
        translationBuilder.add("item.tooltip.damage", "伤害：");
        translationBuilder.add("item.tooltip.speed", "速度：");
        translationBuilder.add("item.tooltip.count", "数量：");
        translationBuilder.add("item.tooltip.base_type", "弹幕轨迹：");
        translationBuilder.add("item.tooltip.recipe.no_compat", "当前不兼容该配方显示，请使用 §b/touhou recipe §r查询");
        translationBuilder.add(RecipeTypeCategoryManager.DANMAKU_TABLE_ICON.toLanguageKey(), "弹幕工作台");
        translationBuilder.add(RecipeTypeCategoryManager.DANMAKU_SHAPE_ICON.toLanguageKey(), "弹幕创作模板");
        translationBuilder.add(RecipeTypeCategoryManager.GENSOKYO_ALTAR_ICON.toLanguageKey(), "幻想乡祭坛");
        translationBuilder.add(RecipeTypeCategoryManager.STRENGTH_TABLE_ICON.toLanguageKey(), "强化台");
        translationBuilder.add(RecipeTypeCategoryManager.KITCHEN_ICON.toLanguageKey(), "夜雀食堂");
        translationBuilder.add("message.gensokyo_altar.miss_structure", "§c结构错误");
        translationBuilder.add("message.gensokyo_altar.miss_recipe", "§c未知合成仪式");
        translationBuilder.add("message.treasure_hunting_rod.find", "离目标还有 %s 格，偏移：X=%s, Y=%s, Z=%s，目标：");
        translationBuilder.add("message.treasure_hunting_rod.not_found", "未在附近找到矿物。");
        translationBuilder.add("message.reverie_dreams.update", "检测到模组更新，最新版本 %s");
        translationBuilder.add(GameRulesInit.DO_GHOST.getDescriptionId(), "禁止幽灵生成");

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
        this.generateAdvancementsTranslations(wrapperLookup, translationBuilder);

        translationBuilder.add("gui.npc.info", "§d");
        translationBuilder.add("gui.npc.info.name", "§d名字: %s");
        translationBuilder.add("gui.npc.info.food", "§d食物信息: ");
        translationBuilder.add("gui.npc.info.food.nutrition", "§6饥饿度: %s");
        translationBuilder.add("gui.npc.info.food.saturation", "§6饱食度: %s");
        translationBuilder.add("gui.npc.info.uuid", "§d通用唯一识别码: %s");
        translationBuilder.add("gui.npc.info.health", "§d生命值: %s/%s");
        translationBuilder.add("gui.npc.info.armor", "§d护甲值: %s");

        translationBuilder.add("gui.npc.info.xp", "§a经验存储：%s");
        translationBuilder.add("gui.npc.info.xp.button", "§f点击取出");

        translationBuilder.add("gui.npc.info.auto-pick", "§b自动寻路拾取");

        translationBuilder.add("gui.npc.mode.null", "§b当前模式为: §c序列化错误");
        translationBuilder.add(NPCStates.FOLLOW.translateKey(), "§b当前模式为: §a跟随");
        translationBuilder.add(NPCStates.NORMAL.translateKey(), "§b当前模式为: §a正常");
        translationBuilder.add(NPCStates.NO_WALK.translateKey(), "§b当前模式为: §a禁止移动");
        translationBuilder.add(NPCStates.SNAKING.translateKey(), "§b当前模式为: §a潜行");
        translationBuilder.add(NPCStates.SEATED.translateKey(), "§a当前模式为:坐下");
        translationBuilder.add(NPCStates.WORKING.translateKey(), "§a当前模式为:工作中");
        translationBuilder.add("gui.npc.mode.work.originpos", "工作原点位置");

        translationBuilder.add("gui.npc.work.button", "模式开关");
        translationBuilder.add("gui.npc.work.mode", "工作模式切换");
        translationBuilder.add(NPCWorkModes.COMBAT.translateKey(), "§a清理怪物");
        translationBuilder.add(NPCWorkModes.FARM.translateKey(), "§b种植作物");
        translationBuilder.add(NPCWorkModes.BREED.translateKey(), "§c繁殖动物");
        translationBuilder.add(NPCWorkModes.SMELT.translateKey(), "§d烧炼矿物");
        translationBuilder.add(NPCWorkModes.CHEST_CLASSIFICATION.translateKey(), "§e箱子分类");
        translationBuilder.add(NPCWorkModes.SHEEP_SHEARING.translateKey(), "§f剪羊毛");
        translationBuilder.add(NPCWorkModes.PLAYING_MUSIC.translateKey(), "§6演奏音乐");
        translationBuilder.add("gui.npc.work.mode.create-fly/hand_crank", "§d摇曲柄");
        translationBuilder.add("gui.npc.work.mode.polyfactory/hand_crank", "§d摇曲柄");
//        translationBuilder.add("gui.npc.woke.mode.disable", "工作原点位置");

        translationBuilder.add("item.reverie_dreams.music.no_files", "§c未找到任何可用的音乐文件！");
        translationBuilder.add("item.reverie_dreams.music.switch_music", "§a切换音乐为：§f%s");
        translationBuilder.add("item.reverie_dreams.music.no_music_selected", "§e未选择任何音乐，潜行右键选择。");
        translationBuilder.add("item.reverie_dreams.music.playing_music", "§b播放音乐：§f%s §7[乐器: %s]");

        translationBuilder.add("npc.event.send_message.0", "哎呀~你终于来了呀！都等得不耐烦了！");
        translationBuilder.add("npc.event.send_message.1", "呜哇！你来找我做什么呢？不会又是给我带任务吧？");
        translationBuilder.add("npc.event.send_message.2", "嘿嘿，今天又能见到你了，真是太开心了！");
        translationBuilder.add("npc.event.send_message.3", "嘻嘻~你是不是又做了什么坏事？我知道了！");
        translationBuilder.add("npc.event.send_message.4", "哎呀，今天好像有点困困的呢……不过看到你心情马上变好了！");
        translationBuilder.add("npc.event.send_message.5", "咦？你来找我有什么事？不会是要和我一起冒险吧？");
        translationBuilder.add("npc.event.send_message.6", "呵呵~你又来了，今天是想让我给你做点什么吗？");
        translationBuilder.add("npc.event.send_message.7", "嘿嘿，和你在一起真的是最开心的事情啦！");
        translationBuilder.add("npc.event.send_message.8", "呀呀！不小心又摔了一跤，不过有你在，心情一下子就好了！");
        translationBuilder.add("npc.event.send_message.9", "哼哼~又来找我了，是不是已经迫不及待想要我帮忙了呢？");

        translationBuilder.add("death.attack.danmaku", "%1$s 满身疮痍");
        translationBuilder.add("death.attack.danmaku.player", "%1$s 与 %2$s 对战的过程中满身疮痍");
        translationBuilder.add("death.attack.danmaku.item", "%1$s 被 %2$s 在对战的过程中使用 %3$s 被打的满身疮痍");

        translationBuilder.addEnchantment(RDEnchantments.EXTERMINATION, "退治");
        translationBuilder.addEnchantment(RDEnchantments.MOON_DAMAGE, "月伤");
        translationBuilder.addEnchantment(RDEnchantments.DANMAKU_PROTECTION, "弹幕保护");
        translationBuilder.addEnchantment(RDEnchantments.POWERFUL, "威力");

        this.generateCommandTranslations(wrapperLookup, translationBuilder);
        this.generateMITranslations(wrapperLookup, translationBuilder);
    }

    public void generateAdvancementsTranslations(HolderLookup.Provider wrapperLookup, TranslationBuilder translationBuilder) {
        TranslationWrapper builder = ITranslationExporter.ofWrapper(wrapperLookup, translationBuilder);
        builder.addAdvancement(RDAdvancements.ROOT, "夢見る幻想郷", "§r获得§a夢見る幻想郷§r模组指南");
        builder.addAdvancement(RDAdvancements.DANMAKU_TABLE, "弹幕工作台", "获得§c弹幕工作台");
        builder.addAdvancement(RDAdvancements.DANMAKU_WARS, "弹幕大战争！", "§r在§c弹幕工作台§r中制作你的弹幕");
        builder.addAdvancement(RDAdvancements.DANMAKU_UPGRADE, "火力升级", "通过强化台强化你的弹幕");
        builder.addAdvancement(RDAdvancements.DARK_CUISINE, "搞砸了", "烹饪出黑暗料理");
        builder.addAdvancement(RDAdvancements.ABANDONED_SHRINE, "第一次接触", "进入废弃神社");
        builder.addAdvancement(RDAdvancements.ENTER_DREAM, "梦里什么都有", "进入第四槐安通道");
        builder.addAdvancement(RDAdvancements.FUMOFUMO, "FUMO FUMO", "获得fumo");
        builder.addAdvancement(RDAdvancements.SHINY_COINS, "闪亮的货币", "获得金属货币");
        builder.addAdvancement(RDAdvancements.LAYLA_PRISMRIVER, "蕾拉·普莉兹姆利巴", "获得一件乐器");
        builder.addAdvancement(RDAdvancements.WOOD_WITH_SPIRITUAL_POWER, "灵力的木头", "获得绳文杉木头");
        builder.addAdvancement(RDAdvancements.GENSOKYO_ALTAR_CRAFTING, "祭坛合成", "§r使用§d幻想乡祭坛§r合成一些物品");
        builder.addAdvancement(RDAdvancements.PSYCHOLOGIST, "心理医生", "对生物使用§d觉之眼§r诊断");
        builder.addAdvancement(RDAdvancements.MAKE_FRIEND, "旅途的伙伴", "使用蛋糕邀请一位角色成为你的伙伴");
        builder.addAdvancement(RDAdvancements.TAKING_PHOTO, "不良新闻记者", "使用照相机拍照");
        builder.addAdvancement(RDAdvancements.I_WILL_TAKE_YOUR_SOUL, "我要偷走你的灵魂！", "获得死神镰刀");
        builder.addAdvancement(RDAdvancements.BURST, "爆！", "使用§aBomb§r");
        builder.addAdvancement(RDAdvancements.LEVEL_UP, "+UP", "使用§d残机§r");
        builder.addAdvancement(RDAdvancements.REHABILITATION_EXPERT, "退治专家", "击败一只妖怪或妖精");
        builder.addAdvancement(RDAdvancements.A_CELESTIAL_BEING_DESCENDED_TO_EARTH, "天神下凡", "尝试带有威力附魔的桃子");
        builder.addAdvancement(RDAdvancements.TOUHOU_MYSTIA_IZAKAYA, "东方夜雀食堂", "获得任意厨具");
        builder.addAdvancement(RDAdvancements.COOKING_BY_MYSELF, "第一次烹饪", "使用任意厨具烹饪食物");
        builder.addAdvancement(RDAdvancements.COOKING_BY_MYSELF_AMOUNT_5_TAG, "烹饪专家", "用厨具制作出带有 5 个以上标签的料理");
        builder.addAdvancement(RDAdvancements.DELICACY, "美味", "品尝一次由厨具烹饪出的料理");
        builder.addAdvancement(RDAdvancements.FINE_WINE, "好酒", "品尝一杯酒水");
    }

    public void generateCommandTranslations(HolderLookup.Provider wrapperLookup, TranslationBuilder builder) {
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
        builder.add("command.touhou.about.title", "梦隐的幻想乡");
        builder.add("command.touhou.about.version", "Version: %s");
        builder.add("command.touhou.about.author", "Author: 稀神灵梦");
        builder.add("command.touhou.about.line10", "");
        builder.add("command.touhou.about.line11", "");

        builder.add("command.touhou.video.reload", "视频重载完成");
        builder.add("command.touhou.video.load", "视频加载中...");
        builder.add("command.touhou.video.load.done", "视频加载完成");


    }

    public void generateTestTranslations(HolderLookup.Provider wrapperLookup, TranslationBuilder translationBuilder) {
//        translationBuilder.add(ModItems.TEST_COLOR_DANMAKU_ITEM, "测试可染色弹幕");
    }

    public void generateMITranslations(HolderLookup.Provider wrapperLookup, TranslationBuilder translationBuilder) {
        TranslationWrapper builder = ITranslationExporter.ofWrapper(wrapperLookup, translationBuilder);
        translationBuilder.add("block.feedback.working", "§c该厨具正在工作中...");

        translationBuilder.add("item.tooltip.food_properties", "属性：");
        builder.add(KitchenwareCreativeTab.ITEM_GROUP, "梦隐的幻想乡 | 厨具");
        builder.add(IngredientCreativeTab.ITEM_GROUP, "梦隐的幻想乡 | 食材");
        builder.add(SeedCreativeTab.ITEM_GROUP, "梦隐的幻想乡 | 种子");
        builder.add(FoodCreativeTab.ITEM_GROUP, "梦隐的幻想乡 | 食物");
        builder.add(DrinkCreativeTab.ITEM_GROUP, "梦隐的幻想乡 | 饮品");
//        translationBuilder.add("item_group.kitchenware_item_group", "梦隐的幻想乡 - 厨具");
//        translationBuilder.add("item_group.ingredients_item_group", "梦隐的幻想乡 - 食材");
//        translationBuilder.add("item_group.seed_item_group", "梦隐的幻想乡 - 种子");
//        translationBuilder.add("item_group.food_item_group", "梦隐的幻想乡 - 食物");
//        translationBuilder.add("item_group.drink_item_group", "梦隐的幻想乡 - 饮品");

        translationBuilder.add(KitchenBlocks.COOKING_POT, "煮锅");
        translationBuilder.add(KitchenBlocks.CUTTING_BOARD, "料理台");
        translationBuilder.add(KitchenBlocks.FRYING_PAN, "油锅");
        translationBuilder.add(KitchenBlocks.GRILL, "烧烤架");
        translationBuilder.add(KitchenBlocks.STEAMER, "蒸锅");
        translationBuilder.add(KitchenBlocks.MYSTIA_COOKING_POT, "夜雀⦁煮锅");
        translationBuilder.add(KitchenBlocks.MYSTIA_CUTTING_BOARD, "夜雀⦁料理台");
        translationBuilder.add(KitchenBlocks.MYSTIA_FRYING_PAN, "夜雀⦁油锅");
        translationBuilder.add(KitchenBlocks.MYSTIA_GRILL, "夜雀⦁烧烤架");
        translationBuilder.add(KitchenBlocks.MYSTIA_STEAMER, "夜雀⦁蒸锅");
        translationBuilder.add(KitchenBlocks.SUPER_COOKING_POT, "超⦁煮锅");
        translationBuilder.add(KitchenBlocks.SUPER_CUTTING_BOARD, "超⦁料理台");
        translationBuilder.add(KitchenBlocks.SUPER_FRYING_PAN, "超⦁油锅");
        translationBuilder.add(KitchenBlocks.SUPER_GRILL, "超⦁烧烤架");
        translationBuilder.add(KitchenBlocks.SUPER_STEAMER, "超⦁蒸锅");
        translationBuilder.add(KitchenBlocks.EXTREME_COOKING_POT, "极⦁煮锅");
        translationBuilder.add(KitchenBlocks.EXTREME_CUTTING_BOARD, "极⦁料理台");
        translationBuilder.add(KitchenBlocks.EXTREME_FRYING_PAN, "极⦁油锅");
        translationBuilder.add(KitchenBlocks.EXTREME_GRILL, "极⦁烧烤架");
        translationBuilder.add(KitchenBlocks.EXTREME_STEAMER, "极⦁蒸锅");
        translationBuilder.add(KitchenBlocks.NUKE_COOKING_POT, "核能⦁煮锅");
        translationBuilder.add(KitchenBlocks.NUKE_CUTTING_BOARD, "核能⦁料理台");
        translationBuilder.add(KitchenBlocks.NUKE_FRYING_PAN, "核能⦁油锅");
        translationBuilder.add(KitchenBlocks.NUKE_GRILL, "核能⦁烧烤架");
        translationBuilder.add(KitchenBlocks.NUKE_STEAMER, "核能⦁蒸锅");

        translationBuilder.add(RDBlocks.ITEM_DISPLAY, "食物盘");
//        translationBuilder.add(MIBlocks.COOKTOP, "能量灶");
        translationBuilder.add(RDBlocks.BLACK_SALT_BLOCK, "黑盐块");

        translationBuilder.add(RDItems.MYSTIA_ICON, "东方夜雀食堂图标");

        translationBuilder.add(RDIngredientItems.BAMBOO_SHOOTS, "竹笋");
        translationBuilder.add(RDIngredientItems.BLACK_SALT, "黑盐");
        translationBuilder.add(RDIngredientItems.BLACK_PORK, "黑猪肉");
        translationBuilder.add(RDIngredientItems.BROCCOLI, "西兰花");
        translationBuilder.add(RDIngredientItems.VENISON, "鹿肉");
        translationBuilder.add(RDIngredientItems.BUTTER, "黄油");
        translationBuilder.add(RDIngredientItems.CAPSAICIN, "辣椒素");
        translationBuilder.add(RDIngredientItems.CHEESE, "奶酪");
        translationBuilder.add(RDIngredientItems.CHESTNUT, "栗子");
        translationBuilder.add(RDIngredientItems.CHILI, "辣椒");
        translationBuilder.add(RDIngredientItems.CRAB, "螃蟹");
        translationBuilder.add(RDIngredientItems.CREAM, "奶油");
        translationBuilder.add(RDIngredientItems.CUCUMBER, "黄瓜");
        translationBuilder.add(RDIngredientItems.DEW, "露水");
        translationBuilder.add(RDIngredientItems.FLOUR, "面粉");
        translationBuilder.add(RDIngredientItems.FLOWERS, "食用花");
        translationBuilder.add(RDIngredientItems.FICUS_MICROCARPA, "薜茘");
        translationBuilder.add(RDIngredientItems.GINKGO, "白果");
        translationBuilder.add(RDIngredientItems.GRAPE, "葡萄");
        translationBuilder.add(RDIngredientItems.HAGFISH, "八目鳗");
        translationBuilder.add(RDIngredientItems.LEMON, "柠檬");
        translationBuilder.add(RDIngredientItems.LOTUS_NUTS, "莲子");
        translationBuilder.add(RDIngredientItems.MOONFLOWER, "月光花");
        translationBuilder.add(RDIngredientItems.OCTOPUS, "章鱼");
        translationBuilder.add(RDIngredientItems.ONION, "洋葱");
        translationBuilder.add(RDIngredientItems.PEACH, "桃子");
        translationBuilder.add(RDIngredientItems.PINE_NUT, "松子");
        translationBuilder.add(RDIngredientItems.PLUM, "李子");
        translationBuilder.add(RDIngredientItems.PUFF_YO_FRUIT, "噗噗哟果");
        translationBuilder.add(RDIngredientItems.RED_BEANS, "红豆");
        translationBuilder.add(RDIngredientItems.SALMON, "三文鱼");
        translationBuilder.add(RDIngredientItems.SEA_URCHIN, "海胆");
        translationBuilder.add(RDIngredientItems.SHRIMP, "虾");
        translationBuilder.add(RDIngredientItems.CICADA_SHELL, "蝉蜕");
        translationBuilder.add(RDIngredientItems.UDUMBARA, "幻昙华");
        translationBuilder.add(RDIngredientItems.STICKY_RICE, "糯米");
        translationBuilder.add(RDIngredientItems.SUPREME_TUNA, "极上金枪鱼");
        translationBuilder.add(RDIngredientItems.SWEET_POTATO, "红薯");
        translationBuilder.add(RDIngredientItems.TOFU, "豆腐");
        translationBuilder.add(RDIngredientItems.TOMATO, "番茄");
        translationBuilder.add(RDIngredientItems.TOON, "香椿");
        translationBuilder.add(RDIngredientItems.TREMELLA, "银耳");
        translationBuilder.add(RDIngredientItems.TROUT, "鳟鱼");
        translationBuilder.add(RDIngredientItems.TRUFFLE, "松露");
        translationBuilder.add(RDIngredientItems.TUNA, "金枪鱼");
        translationBuilder.add(RDIngredientItems.TWIN_LOTUS, "并蒂莲");
        translationBuilder.add(RDIngredientItems.WAGYU_BEEF, "和牛");
        translationBuilder.add(RDIngredientItems.WHITE_RADISH, "白萝卜");
        translationBuilder.add(RDIngredientItems.WILD_BOAR_MEAT, "野猪肉");

        translationBuilder.add(RDFoodItems.ALL_MEAT_FEAST, "全肉盛宴");
        translationBuilder.add(RDFoodItems.ARCTIC_SWEET_SHRIMP_AND_PEACH_SALAD, "北极甜虾蜜桃色拉");
        translationBuilder.add(RDFoodItems.ASSORTED_TEMPURA, "什锦天妇罗");
        translationBuilder.add(RDFoodItems.A_LITTLE_SWEET_POISON, "小小的甜蜜「毒药」");
        translationBuilder.add(RDFoodItems.BAKED_CRAB_WITH_CREAM, "奶油焗蟹");
        translationBuilder.add(RDFoodItems.BAKED_SWEET_POTATOES, "烤地瓜");
        translationBuilder.add(RDFoodItems.BAMBOO_SHOOTS_FRIED_MEAT, "竹笋炒肉");
        translationBuilder.add(RDFoodItems.BAMBOO_SHOOTS_STEWED_IN_STONE_POT, "石锅竹笋炖肉");
        translationBuilder.add(RDFoodItems.BAMBOO_STEAMED_EGG, "竹筒蒸蛋");
        translationBuilder.add(RDFoodItems.BAMBOO_TUBE_ROASTED_DRUNKEN_SHRIMP, "竹筒烧醉虾");
        translationBuilder.add(RDFoodItems.BAMBOO_TUBE_STEAMED_PORK, "竹筒粉蒸肉");
        translationBuilder.add(RDFoodItems.BEAR_PAW, "熊掌");
        translationBuilder.add(RDFoodItems.BEEF_HOT_POT, "牛肉鸳鸯火锅");
        translationBuilder.add(RDFoodItems.BEEF_RICE, "牛肉盖浇饭");
        translationBuilder.add(RDFoodItems.BEEF_WELLINGTON, "惠灵顿牛排");
        translationBuilder.add(RDFoodItems.BEETLE_STEAMED_CAKE, "兜甲蒸糕");
        translationBuilder.add(RDFoodItems.BURNT_PUDDING, "燃尽布丁");
        translationBuilder.add(RDFoodItems.BISCAY_BISCUITS, "比斯开湾饼干");
        translationBuilder.add(RDFoodItems.BOILED_FISH, "水煮鱼");
        translationBuilder.add(RDFoodItems.BRAISED_EEL, "红烧鳗鱼");
        translationBuilder.add(RDFoodItems.BRAISED_PORK_WITH_PEACH, "桃子红烧肉");
        translationBuilder.add(RDFoodItems.BUTTER_STEAK, "黄油牛排");
        translationBuilder.add(RDFoodItems.CANDIED_CHESTNUTS, "糖栗子");
        translationBuilder.add(RDFoodItems.CANDIED_SWEET_POTATO, "拔丝地瓜");
        translationBuilder.add(RDFoodItems.CATS_PLAYING_IN_WATER, "猫咪戏水");
        translationBuilder.add(RDFoodItems.CAT_FOOD, "猫饭");
        translationBuilder.add(RDFoodItems.CAT_KULULI, "猫咕噜哩");
        translationBuilder.add(RDFoodItems.CAT_PIZZA, "猫咪披萨");
        translationBuilder.add(RDFoodItems.CHEESE_EGG, "芝士蛋");
        translationBuilder.add(RDFoodItems.COLD_DISH_CARVING, "凉菜雕花");
        translationBuilder.add(RDFoodItems.COLD_TOFU, "冷豆腐");
        translationBuilder.add(RDFoodItems.COLORFUL_JADE_FRIED_BUNS, "华光玉煎包");
        translationBuilder.add(RDFoodItems.COOKING_TOFU, "煮豆腐");
        translationBuilder.add(RDFoodItems.CREAM_STEW, "奶油炖菜");
        translationBuilder.add(RDFoodItems.CRISP_CYCLONE, "脆旋风");
        translationBuilder.add(RDFoodItems.DARK_CUISINE, "黑暗物质");
        translationBuilder.add(RDFoodItems.DEEP_FRIED_CICADA_SHELLS, "炸蝉蜕");
        translationBuilder.add(RDFoodItems.DEPRESSED_CHEESE_STICKS, "忧郁芝士条");
        translationBuilder.add(RDFoodItems.DEW_BOILED_EGGS, "露水煮蛋");
        translationBuilder.add(RDFoodItems.DORAYAKI, "铜锣烧");
        translationBuilder.add(RDFoodItems.DUMPLING, "饺子");
        translationBuilder.add(RDFoodItems.EEL_EGG_DONBURI, "鳗鱼蛋盖饭");
        translationBuilder.add(RDFoodItems.EGGS_BENEDICT, "班尼迪克蛋");
        translationBuilder.add(RDFoodItems.ENERGY_STRING, "能量串");
        translationBuilder.add(RDFoodItems.FAILING_SAKURA_SNOW, "樱落雪");
        translationBuilder.add(RDFoodItems.FANTASY_IS_ALL_THE_RAGE, "幻想风靡");
        translationBuilder.add(RDFoodItems.FISH_LEAPS_OVER_DRAGON_GATE, "鱼跃龙门");
        translationBuilder.add(RDFoodItems.FLOWERS_BIRDS_WIND_AND_MOON, "花鸟风月");
        translationBuilder.add(RDFoodItems.FLOWING_WATER_NOODLES, "流水素面");
        translationBuilder.add(RDFoodItems.FRIED_HAGFISH, "炸八目鳗");
        translationBuilder.add(RDFoodItems.FRIED_PORK_CUTLET, "炸猪排");
        translationBuilder.add(RDFoodItems.FRIED_PORK_SHREDS, "炒肉丝");
        translationBuilder.add(RDFoodItems.FRIED_SHRIMP_TEMPURA, "炸虾天妇罗");
        translationBuilder.add(RDFoodItems.FRIED_TOFU, "炸豆腐");
        translationBuilder.add(RDFoodItems.FRIED_TOMATO_STRIPS, "炸番茄条");
        translationBuilder.add(RDFoodItems.FRIGHT_ADVENTURE, "惊吓！大冒险");
        translationBuilder.add(RDFoodItems.GAME_SOUP, "野味杂烩汤");
        translationBuilder.add(RDFoodItems.GENSOKYO_BUDDHA_JUMPS_OVER_THE_WALL, "幻想佛跳墙");
        translationBuilder.add(RDFoodItems.GENSOKYO_STAR_LOTUS_SHIP, "幻想星莲船");
        translationBuilder.add(RDFoodItems.GIANT_TAMAGOYAKI, "巨人玉子烧");
        translationBuilder.add(RDFoodItems.GINKGO_AND_RADISH_PORK_RIB_SOUP, "银杏萝卜排骨汤");
        translationBuilder.add(RDFoodItems.GLOOMY_FRUIT_PIE, "忧郁水果派");
        translationBuilder.add(RDFoodItems.GLUTINOUS_RICE_BALLS, "汤圆");
        translationBuilder.add(RDFoodItems.GOLDEN_CRISPY_FISH_CAKE, "金黄酥鱼饼");
        translationBuilder.add(RDFoodItems.GRAND_BANQUET, "大奢宴");
        translationBuilder.add(RDFoodItems.GREEN_BAMBOO_WELCOMES_SPRING, "翠竹迎春");
        translationBuilder.add(RDFoodItems.GREEN_FAIRY_MUSHROOM, "绿野仙菇");
        translationBuilder.add(RDFoodItems.GRILLED_HAGFISH, "烤八目鳗");
        translationBuilder.add(RDFoodItems.GRILLED_PORK_RICE_BALLS, "炙猪肉饭团");
        translationBuilder.add(RDFoodItems.HEART_PORRIDGE_GRUEL, "养心粥");
        translationBuilder.add(RDFoodItems.HELL_THRILL_WARNING, "地狱激辛警告！");
        translationBuilder.add(RDFoodItems.HOLY_WHITE_LOTUS_SEED_CAKE, "圣白莲子糕");
        translationBuilder.add(RDFoodItems.HONEY_BBQ_PORK, "蜜汁叉烧");
        translationBuilder.add(RDFoodItems.HORAI_DAMA_NO_EDA, "蓬莱玉枝");
        translationBuilder.add(RDFoodItems.HOT_WAFFLES, "热华夫饼");
        translationBuilder.add(RDFoodItems.HULA_SOUP, "呼啦汤");
        translationBuilder.add(RDFoodItems.LION_HEAD, "狮子头");
        translationBuilder.add(RDFoodItems.LONGYIN_PEACH, "龙吟桃子");
        translationBuilder.add(RDFoodItems.LOOKING_UP_AT_THE_CEILING_FRUIT_PIE, "仰望天花板派");
        translationBuilder.add(RDFoodItems.LOTUS_FISH_RICE_BOWL, "荷花鱼米盏");
        translationBuilder.add(RDFoodItems.LUOHAN_VEGETARIAN, "罗汉上素");
        translationBuilder.add(RDFoodItems.MAD_HATTER_TEA_PARTY, "疯帽子茶会");
        translationBuilder.add(RDFoodItems.MAGMA, "岩浆");
        translationBuilder.add(RDFoodItems.MAOYU_LAVA_TOFU, "茂羽岩浆豆腐");
        translationBuilder.add(RDFoodItems.MAOYU_TRICOLOR_ICE_CREAM, "茂羽三色冰淇淋");
        translationBuilder.add(RDFoodItems.MAPO_TOFU, "麻婆豆腐");
        translationBuilder.add(RDFoodItems.MILKY_MUSHROOM_SOUP, "奶香蘑菇汤");
        translationBuilder.add(RDFoodItems.MOCHI, "麻薯");
        translationBuilder.add(RDFoodItems.MOLECULAR_EGG, "分子蛋");
        translationBuilder.add(RDFoodItems.MOONLIGHT_DUMPLINGS, "月光团子");
        translationBuilder.add(RDFoodItems.MOONLIGHT_OVER_LOTUS_POND, "荷塘月色");
        translationBuilder.add(RDFoodItems.MOON_CAKE, "月饼");
        translationBuilder.add(RDFoodItems.MOON_LOVERS, "月之恋人");
        translationBuilder.add(RDFoodItems.MUSHROOM_GIRLS_DANCE_STEW, "蘑女的舞踏烩");
        translationBuilder.add(RDFoodItems.MUSHROOM_MEAT_SLICES, "香菇肉片");
        translationBuilder.add(RDFoodItems.NIGIRI_SUSHI, "握寿司");
        translationBuilder.add(RDFoodItems.OEDO_BOAT_FESTIVAL, "大江户船祭");
        translationBuilder.add(RDFoodItems.OKONOMIYAKI, "御好烧");
        translationBuilder.add(RDFoodItems.ONE_HIT_KILL, "一击必杀");
        translationBuilder.add(RDFoodItems.ORDINARY_SMALL_CAKE, "普通小蛋糕");
        translationBuilder.add(RDFoodItems.PAN_FRIED_MUSHROOM_MEAT_ROLL, "香煎双菇肉卷");
        translationBuilder.add(RDFoodItems.PAN_FRIED_SALMON, "煎三文鱼");
        translationBuilder.add(RDFoodItems.PEACH_BLOSSOM_GLAZE_ROLL, "桃花琉璃卷");
        translationBuilder.add(RDFoodItems.PEACH_BLOSSOM_SOUP, "桃花羹");
        translationBuilder.add(RDFoodItems.PHOENIX, "不死鸟");
        translationBuilder.add(RDFoodItems.PICKLED_CUCUMBERS, "腌黄瓜");
        translationBuilder.add(RDFoodItems.PIG_DEER_BUTTERFLY, "猪鹿蝶");
        translationBuilder.add(RDFoodItems.PINE_NUT_CAKE, "松子糕");
        translationBuilder.add(RDFoodItems.PIRATE_BACON, "海盗熏肉");
        translationBuilder.add(RDFoodItems.PLUM_TEA_RICE, "梅子茶泡饭");
        translationBuilder.add(RDFoodItems.POETRY_AND_GINKGO, "诗礼银杏");
        translationBuilder.add(RDFoodItems.POISONOUS_GARDEN, "毒瘴花园");
        translationBuilder.add(RDFoodItems.PORK_AND_TROUT_SMOKED, "猪肉鳟鱼熏");
        translationBuilder.add(RDFoodItems.PORK_RICE, "猪肉盖浇饭");
        translationBuilder.add(RDFoodItems.POTATO_CROQUETTES, "土豆可乐饼");
        translationBuilder.add(RDFoodItems.PSEUDO_JIRITAMA, "拟尻子玉");
        translationBuilder.add(RDFoodItems.PUMPKIN_SHRIMP_CAKE, "南瓜虾盅");
        translationBuilder.add(RDFoodItems.RAPUNZEL, "长发公主");
        translationBuilder.add(RDFoodItems.REAL_SEAFOOD_MISO_SOUP, "真·海鲜味噌汤");
        translationBuilder.add(RDFoodItems.RED_BEAN_DAIFUKU, "红豆大福");
        translationBuilder.add(RDFoodItems.REFRESHING_PUDDING, "提神布丁");
        translationBuilder.add(RDFoodItems.REVERSING_THE_WORLD, "逆转天地！");
        translationBuilder.add(RDFoodItems.RICE_BALL, "饭团");
        translationBuilder.add(RDFoodItems.RISOTTO, "意式烩饭");
        translationBuilder.add(RDFoodItems.ROASTED_MUSHROOMS, "烤蘑菇");
        translationBuilder.add(RDFoodItems.SAKURA_PUDDING, "樱花布丁");
        translationBuilder.add(RDFoodItems.SALMON_TEMPURA, "三文鱼天妇罗");
        translationBuilder.add(RDFoodItems.SASHIMI_PLATTER, "刺身拼盘");
        translationBuilder.add(RDFoodItems.SCARLET_DEVILS_CAKE, "红魔蛋糕");
        translationBuilder.add(RDFoodItems.SCONES, "司康饼");
        translationBuilder.add(RDFoodItems.SCREAMING_ODEN, "绝叫关东煮");
        translationBuilder.add(RDFoodItems.SEAFOOD_MISO_SOUP, "海鲜味噌汤");
        translationBuilder.add(RDFoodItems.SEA_URCHIN_SHINGEN_PANCAKE, "海胆信玄饼");
        translationBuilder.add(RDFoodItems.SEA_URCHIN_SASHIMI, "海胆刺身");
        translationBuilder.add(RDFoodItems.SECRET_DRIED_FISH, "秘制小鱼干");
        translationBuilder.add(RDFoodItems.SECRET_MUSHROOM_CASSEROLE, "秘制蘑菇煲");
        translationBuilder.add(RDFoodItems.SEVEN_COLORED_YOKAN, "七彩羊羹");
        translationBuilder.add(RDFoodItems.SHIRAGA_SADAMATSU, "白鹿贞松");
        translationBuilder.add(RDFoodItems.SKINNY_HORSE_DUMPLING, "瘦马团子");
        translationBuilder.add(RDFoodItems.SNOW_WHITE, "白雪公主");
        translationBuilder.add(RDFoodItems.STEAMED_EGG_WITH_SEA_URCHIN, "海胆蒸蛋");
        translationBuilder.add(RDFoodItems.STINKY_TOFU, "臭豆腐");
        translationBuilder.add(RDFoodItems.STRENGTH_SOUP, "力量汤");
        translationBuilder.add(RDFoodItems.SUPERME_SEAFOOD_NOODLES, "至尊海鲜面");
        translationBuilder.add(RDFoodItems.TAICHI_BAGUA_FISH_MAW, "太极八卦鱼肚");
        translationBuilder.add(RDFoodItems.TAKETORIHIME, "辉夜姬");
        translationBuilder.add(RDFoodItems.TAKOYAKI, "章鱼烧");
        translationBuilder.add(RDFoodItems.THE_BEAUTY_OF_HAN_PALACE, "汉宫藏娇");
        translationBuilder.add(RDFoodItems.THE_DREAM, "幽梦");
        translationBuilder.add(RDFoodItems.THE_MARS, "火星料理");
        translationBuilder.add(RDFoodItems.THE_SOURCE_OF_LIFE, "生命之源");
        translationBuilder.add(RDFoodItems.TIANSHI_BRAISED_CHESTNUT_MUSHROOMS, "天师板栗焖菇");
        translationBuilder.add(RDFoodItems.TOFU_MISO, "豆腐味噌");
        translationBuilder.add(RDFoodItems.TOFU_POT, "豆腐锅");
        translationBuilder.add(RDFoodItems.TONKOTSU_RAMEN, "豚骨拉面");
        translationBuilder.add(RDFoodItems.TOON_PANCAKES, "香椿煎饼");
        translationBuilder.add(RDFoodItems.TWO_HEAVENS_ONE_STYLE, "二天一流");
        translationBuilder.add(RDFoodItems.UDUMBARA_CAKE, "幻昙花糕");
        translationBuilder.add(RDFoodItems.UNCONSCIOUS_MONSTER_MOUSSE, "无意识怪物慕斯");
        translationBuilder.add(RDFoodItems.VEGETABLE_SPECIAL, "蔬菜特辑");
        translationBuilder.add(RDFoodItems.WARM_RICE_BALL, "温热饭团");
        translationBuilder.add(RDFoodItems.WHITE_PEACH_EIGHT_BRIDGE, "白桃八桥");
        translationBuilder.add(RDFoodItems.YUNSHAN_COTTON_CANDY, "云山棉花糖");
        translationBuilder.add(RDFoodItems.ZHAJI, "炸脊");

        translationBuilder.add(RDDrinkItems.GREEN_TEA, "绿茶");
        translationBuilder.add(RDDrinkItems.FRUITY_HIGH_BALL, "果味High Ball");
        translationBuilder.add(RDDrinkItems.FRUITY_SOUR, "果味Sour");
        translationBuilder.add(RDDrinkItems.QI, "淇");
        translationBuilder.add(RDDrinkItems.BEER, "超ZUN啤酒");
        translationBuilder.add(RDDrinkItems.SUN_MOON_STAR, "日月星");
        translationBuilder.add(RDDrinkItems.PLUM_WINE, "梅酒");
        translationBuilder.add(RDDrinkItems.TENGU_DANCE, "天狗踊");
        translationBuilder.add(RDDrinkItems.SCARLET_DEVIL, "猩红恶魔");
        translationBuilder.add(RDDrinkItems.GODS_WHEAT, "神之麦");
        translationBuilder.add(RDDrinkItems.OTTER_FESTIVAL, "水獭祭");
        translationBuilder.add(RDDrinkItems.DAWN, "水獭祭");
        translationBuilder.add(RDDrinkItems.SPARROW_SAKE, "雀酒");
        translationBuilder.add(RDDrinkItems.SCARLET_DEVIL_MANSION_BLACK_TEA, "红魔馆红茶");
        translationBuilder.add(RDDrinkItems.AFFGADO, "阿芙加朵");
        translationBuilder.add(RDDrinkItems.RED_MIST, "红雾");
        translationBuilder.add(RDDrinkItems.NEGRONI, "尼格罗尼");
        translationBuilder.add(RDDrinkItems.GODFATHER, "教父");
        translationBuilder.add(RDDrinkItems.BLESSING_WIND, "风祝");
        translationBuilder.add(RDDrinkItems.WINTER_BREW, "冬酿");
        translationBuilder.add(RDDrinkItems.FOURTEENTH_NIGHT, "十四夜");
        translationBuilder.add(RDDrinkItems.FIRE_RAT_FUR, "火鼠裘");
        translationBuilder.add(RDDrinkItems.GYOKURO_TEA, "玉露茶");
        translationBuilder.add(RDDrinkItems.MOON_ROCKET, "月面火箭");
        translationBuilder.add(RDDrinkItems.MILK, "牛奶");
        translationBuilder.add(RDDrinkItems.RED_GRAPEFRUIT_JUICE, "红柚果汁");
        translationBuilder.add(RDDrinkItems.SODA, "波子汽水");
        translationBuilder.add(RDDrinkItems.ICEBERG_MAPLE_FROZEN_LEMON, "冰山毛玉冻柠");
        translationBuilder.add(RDDrinkItems.BIG_POPSICLE, "“大冰棍儿！”");
        translationBuilder.add(RDDrinkItems.DAIGINJO, "大吟酿");
        translationBuilder.add(RDDrinkItems.COFFEE, "咖啡");
        translationBuilder.add(RDDrinkItems.FAIRY_RAIN, "妖精雨露");
        translationBuilder.add(RDDrinkItems.PALEO_CREAMY_SMOOTHIE, "古法奶油冰沙");
        translationBuilder.add(RDDrinkItems.ORDINARY_FITNESS_TEA, "普通健身茶");
        translationBuilder.add(RDDrinkItems.DEMON_SLAYER, "鬼杀");
        translationBuilder.add(RDDrinkItems.QI_HEALTH, "气保健");
        translationBuilder.add(RDDrinkItems.KOMEIJI_ICE_CREAM, "古明地冰激凌");
        translationBuilder.add(RDDrinkItems.MANGO_POMELO_SAGO, "杨枝甘露");
        translationBuilder.add(RDDrinkItems.QILIN, "麒麟");
        translationBuilder.add(RDDrinkItems.HEAVEN_AND_EARTH_ARE_USELESS, "天地无用");
        translationBuilder.add(RDDrinkItems.DRUNK_ACTOR, "伶人醉");
        translationBuilder.add(RDDrinkItems.DAUGHTER_OF_THE_SEA, "海的女儿");
        translationBuilder.add(RDDrinkItems.DEMONIC_COFFEE, "魔界咖啡");
        translationBuilder.add(RDDrinkItems.MOJITO_BURST_BALL, "莫吉托爆浆球");
        translationBuilder.add(RDDrinkItems.SPACE_BEER, "太空啤酒");
        translationBuilder.add(RDDrinkItems.SATELLITE_ICED_COFFEE, "卫星冰咖啡");

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
        translationBuilder.add(RDWoodBlocks.UDUMBARA_FLOWER, "幻昙华花");
        translationBuilder.add(RDWoodBlocks.TREMELLA, "银耳丛");

        // 种子
        generateCropBlock(translationBuilder, RDCropBlocks.CHILL, "辣椒种子");
        generateCropBlock(translationBuilder, RDCropBlocks.CUCUMBER, "黄瓜种子");
        generateCropBlock(translationBuilder, RDCropBlocks.GRAPE, "葡萄种子");
        generateCropBlock(translationBuilder, RDCropBlocks.ONION, "洋葱种子");
        generateCropBlock(translationBuilder, RDCropBlocks.RED_BEANS, "红豆种子");
        generateCropBlock(translationBuilder, RDCropBlocks.TOMATO, "番茄种子");
        generateCropBlock(translationBuilder, RDCropBlocks.TOON, "香椿种子");
        generateCropBlock(translationBuilder, RDCropBlocks.WHITE_RADISH, "白萝卜种子");
        generateCropBlock(translationBuilder, RDCropBlocks.SWEET_POTATO, "红薯种子");
        generateCropBlock(translationBuilder, RDCropBlocks.BROCCOLI, "西兰花种子");
        generateCropBlock(translationBuilder, RDCropBlocks.SOY_BEANS, "黄豆种子");
    }

    void generateCropBlock(TranslationBuilder builder, CropBlockCreator.Instance instance, String seedName) {
        builder.add(instance.getCropBlock(), seedName);
        builder.add(instance.getSeed(), seedName);
    }

    public void generateEntityTranslations(HolderLookup.Provider wrapperLookup, TranslationBuilder translationBuilder) {
        TranslationWrapper builder = ITranslationExporter.ofWrapper(wrapperLookup, translationBuilder);
        builder.add(RDEntityTypes.FUMO_SELLER_VILLAGER, "Fumo贩卖商人", "Fumo贩卖商人刷怪蛋");
        builder.add(RDEntityTypes.KILLER_BEE, "杀人蜂", "杀人蜂刷怪蛋");
        builder.add(RDEntityTypes.GHOST, "幽灵", "幽灵刷怪蛋");
        builder.add(RDEntityTypes.MOON_RABBIT, "月兔", "月兔刷怪蛋");
        builder.add(RDEntityTypes.YOUSEI, "妖精", "妖精刷怪蛋");
        builder.add(RDEntityTypes.MAID_YOUSEI, "女仆妖精", "女仆妖精刷怪蛋");
        builder.add(RDEntityTypes.SUNFLOWER_YOUSEI, "向日葵妖精", "向日葵妖精刷怪蛋");
        builder.add(RDEntityTypes.GOBLIN, "哥布林", "哥布林刷怪蛋");
        builder.add(RDEntityTypes.RABBIT_UNIT, "月兔战士", "月兔战士刷怪蛋");
        builder.add(RDEntityTypes.WATER_ELEMENTAL, "水元素", "水元素刷怪蛋");
        builder.add(RDEntityTypes.FIRE_ELEMENTAL, "火元素", "火元素刷怪蛋");
        builder.add(RDEntityTypes.ICE_ELEMENTAL, "冰元素", "冰元素刷怪蛋");
        builder.add(RDEntityTypes.MAGIC_BROOM, "魔法扫帚", "魔法扫帚刷怪蛋");
        builder.add(RDEntityTypes.WHEEL_CHAIR, "轮椅", "轮椅刷怪蛋");
        builder.add(RDEntityTypes.HAIRBALL, "毛玉", "毛玉刷怪蛋");
        builder.add(RDEntityTypes.SCARECROW, "稻草人", "稻草人刷怪蛋");
        builder.add(RDEntityTypes.UFO, "UFO", "UFO刷怪蛋");
        builder.add(RDEntityTypes.MUSHROOM_MONSTER, "蘑菇", "蘑菇刷怪蛋");
        builder.add(RDEntityTypes.WILD_PIG, "野猪", "野猪刷怪蛋");
        builder.add(RDEntityTypes.TAVERN_VILLAGER, "酒馆老板", "酒馆老板刷怪蛋");

        var registry = wrapperLookup.lookupOrThrow(Registries.VILLAGER_PROFESSION);
        VillagerProfession hawkers = registry.getOrThrow(RDVillagerProfessions.HAWKERS).value();
        VillagerProfession priest = registry.getOrThrow(RDVillagerProfessions.PRIEST).value();
        VillagerProfession moneyShopClerk = registry.getOrThrow(RDVillagerProfessions.MONEY_SHOP_CLERK).value();
        builder.add(hawkers.name(), "小贩");
        builder.add(priest.name(), "神主");
        builder.add(moneyShopClerk.name(), "钱庄伙计");
    }

    public void generateRoleCardTranslations(HolderLookup.Provider wrapperLookup, TranslationBuilder translationBuilder) {
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

    public void generateRoleTranslations(HolderLookup.Provider wrapperLookup, TranslationBuilder translationBuilder) {
        TranslationWrapper builder = ITranslationExporter.ofWrapper(wrapperLookup, translationBuilder);
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

        // 兽王园
        builder.addRoleEntity(NPCRoles.SON_BITEN, "孙美天", "刷怪蛋");
        builder.addRoleEntity(NPCRoles.MITSUGASHIRA_ENOKO, "三头慧乃子", "刷怪蛋");
        builder.addRoleEntity(NPCRoles.TENKAJIN_CHIYARI, "天火人血枪", "刷怪蛋");
        builder.addRoleEntity(NPCRoles.YOMOTSU_HISAMI, " 豫母都日狭美", "刷怪蛋");
        builder.addRoleEntity(NPCRoles.NIPPAKU_ZANMU, "日白残无", "刷怪蛋");

        // 锦上京
        builder.addRoleEntity(NPCRoles.CHIRIZUKA_UBAME, "尘塚姥芽", "刷怪蛋");
        builder.addRoleEntity(NPCRoles.HOUJU_CHIMI, "封兽魑魅", "刷怪蛋");
        builder.addRoleEntity(NPCRoles.MICHIGAMI_NAREKO, "道神驯子", "刷怪蛋");
        builder.addRoleEntity(NPCRoles.YUIMAN_ASAMA, "维缦·浅间", "刷怪蛋");
        builder.addRoleEntity(NPCRoles.WATATSUKI_TOYOHIME, "绵月丰姬", "刷怪蛋");
        builder.addRoleEntity(NPCRoles.IWANAGA_ARIYA, "磐永阿梨夜", "刷怪蛋");
        builder.addRoleEntity(NPCRoles.WATARI_NINA, "渡里贝子", "刷怪蛋");

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

    public void generateEffectTranslations(HolderLookup.Provider wrapperLookup, TranslationBuilder translationBuilder) {
        TranslationWrapper builder = ITranslationExporter.ofWrapper(wrapperLookup, translationBuilder);
        builder.add(RDStatusEffects.ELIXIR_OF_LIFE.value(), "不死");
        builder.add(RDStatusEffects.MENTAL_DISORDER.value(), "精神错乱");
        builder.add(RDStatusEffects.BACK_OF_LIFE.value(), "返生");
        builder.add(RDStatusEffects.KANJU_KUSURI.value(), "绀珠");

        builder.generatePotion(RDPotions.ELIXIR_OF_LIFE_POTION.value(), "蓬莱之药", "喷溅型蓬莱之药", "滞留型蓬莱之药");
        builder.generatePotion(RDPotions.MENTAL_DISORDER_POTION.value(), "精神错乱药水", "喷溅型精神错乱药水", "滞留型精神错乱药水");
        builder.generatePotion(RDPotions.BACK_OF_LIFE_POTION.value(), "还生药", "喷溅型还生药", "滞留型还生药");
        builder.generatePotion(RDPotions.KANJU_KUSURI_POTION.value(), "绀珠药", "喷溅型绀珠药", "滞留型绀珠药");

    }

    public void generateDialogTranslations(HolderLookup.Provider wrapperLookup, TranslationBuilder translationBuilder) {
        translationBuilder.add("dialog.message.select", "请选择一个角色");

        translationBuilder.add("dialog.title.main", "东方入门手册");
        translationBuilder.add("dialog.title.altar", "祭坛摆放");
        translationBuilder.add("dialog.title.danmaku", "弹幕合成");
        translationBuilder.add("dialog.title.upgrade_danmaku", "升级弹幕");
        translationBuilder.add("dialog.title.fumo", "Fumo 指南");
        translationBuilder.add("dialog.title.role", "角色养成");
        translationBuilder.add("dialog.title.mystia.main", "夜雀食堂");
        translationBuilder.add("dialog.title.cn.issue", "QQ开发/反馈群");
        translationBuilder.add("dialog.title.registries", "注册表预览");
        translationBuilder.add("dialog.title.other_mod_list", "其他配套Mod列表");
        translationBuilder.add("dialog.title.open_recipe_manager", "打开配方管理器");

        translationBuilder.add("dialog.text.empty", "空");
        translationBuilder.add("dialog.text.exit", "退出");
        translationBuilder.add("dialog.text.random", "随机");
        translationBuilder.add("dialog.text.back", "返回");

        translationBuilder.add("dialog.main.welcome", "欢迎游玩梦隐的幻想乡，本屏幕将介绍模组的游玩指南");
        translationBuilder.add("dialog.main.description.0", "当前版本为 %s");

        translationBuilder.add("dialog.altar.material", "材料");
        translationBuilder.add("dialog.altar.ways", "摆放方式");
        translationBuilder.add("dialog.altar.on_top", "提示: 需要使用纸对顶层的木头右键祝福");

        translationBuilder.add("dialog.danmaku.description.0", "弹幕工作台是一个用于合成弹幕物品的工作方块，里面共有5个槽位可以摆放物品合成位。");
        translationBuilder.add("dialog.danmaku.description.1", "摆放示例：");

        translationBuilder.add("dialog.upgrade_danmaku.description.0", "%s可升级提升弹幕的战斗力");
        translationBuilder.add("dialog.upgrade_danmaku.description.1", "目前有效强化道具有：");

        translationBuilder.add("dialog.fumo.description.0", "Fumo 是《东方 Project》角色为原型的毛绒玩偶，绝大多数 Fumo 可在生存模式内通过较为昂贵的交易获得，且放置后会立刻转向玩家当前所在位置，一共有16个方向可以转向放置，右键 Fumo 方块可以发出 Fumo 声音。");
        translationBuilder.add("dialog.fumo.description.1", "Fumo 商人每天的交易内容都会变化");
        translationBuilder.add("dialog.fumo.description.2", "商人创建方式：使用%s对村民右键转化为Fumo商人");

        translationBuilder.add("dialog.role.description.0", "角色（Role）是本 Mod 中的随从类实体，类似 Touhou Little Maid，拥有背包，战斗，工作模式，睡眠和饥饿等模块，可以使用蛋糕驯服");
        translationBuilder.add("dialog.role.description.1", "一般生存模式下可通过角色卡召唤，角色卡可通过幻想乡祭坛合成");
        translationBuilder.add("dialog.role.description.2", "角色死亡后会掉落物品%s，可在在祭坛摆放一圈钻石*2重新复活角色");
        translationBuilder.add("dialog.role.description.3", "鼠标右键: 触发对话交互");
        translationBuilder.add("dialog.role.description.4", "潜行 + 鼠标左键 + 空手: 停止攻击目标");
        translationBuilder.add("dialog.role.description.5", "潜行 + 鼠标右键: 打开背包");

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

    public void generateItemTranslations(HolderLookup.Provider wrapperLookup, TranslationBuilder translationBuilder) {
        // 调试
        translationBuilder.add(RDItems.BATTLE_STICK, "战斗调试棒");
        translationBuilder.add(RDItems.OWNER_STICK, "主权调试棒");

        // 图标
        translationBuilder.add(RDItems.ICON, "梦隐的幻想乡");
        translationBuilder.add(RDItems.FUMO_ICON, "毛绒玩偶图标");
        translationBuilder.add(RDItems.SPAWN_EGG, "刷怪蛋");
        translationBuilder.add(RDItems.DANMAKU, "弹幕");

        // 材料
        translationBuilder.add(RDItems.POINT, "Point");
        translationBuilder.add(RDItems.POWER, "Power");
        translationBuilder.add(RDItems.DANMAKU_CORE, "弹幕核心");
        translationBuilder.add(RDItems.UPGRADED_HEALTH_FRAGMENT, "残机碎片");
        translationBuilder.add(RDItems.BOMB_FRAGMENT, "Bomb碎片");
        translationBuilder.add(RDItems.RED_ORB, "红宝玉");
        translationBuilder.add(RDItems.BLUE_ORB, "蓝宝玉");
        translationBuilder.add(RDItems.YELLOW_ORB, "黄宝玉");
        translationBuilder.add(RDItems.GREEN_ORB, "绿宝玉");
        translationBuilder.add(RDItems.PURPLE_ORB, "紫宝玉");
        translationBuilder.add(RDItems.YIN_YANG_ORB, "阴阳玉");
        translationBuilder.add(RDItems.SPEED_FEATHER, "速度羽毛");
        translationBuilder.add(RDItems.DREAM_CRYSTAL_FRAGMENT, "梦境水晶碎片");
        translationBuilder.add(RDItems.EMPTY_PHOTO, "空照片");
        translationBuilder.add(RDItems.WATERPROOF_LEATHER, "防水皮革");

        // 道具
        translationBuilder.add(RDItems.TOUHOU_HELPER, "§a夢見る幻想郷§r模组指南");
        translationBuilder.add(RDItems.UPGRADED_HEALTH, "残机");
        translationBuilder.add(RDItems.BOMB, "Bomb");
        translationBuilder.add(RDItems.HORAI_DAMA_NO_EDA, "蓬莱玉枝");
        translationBuilder.add(RDItems.YUKA_FLOWER_UMBRELLA, "花洋伞");
        translationBuilder.add(RDItems.CROSSING_CHISEL, "穿墙之凿");
        translationBuilder.add(RDItems.GAP_BALL, "隙间之球");
        translationBuilder.add(RDItems.BAGUA_FURNACE, "八卦炉");
        translationBuilder.add(RDItems.TIME_STOP_CLOCK, "时停钟");
        translationBuilder.add(RDItems.EARPHONE, "神子耳机");
        translationBuilder.add(RDItems.KOISHI_HAT, "恋恋钢盔");
        translationBuilder.add(RDItems.FUMO_LICENSE, "Fumo销售许可");
        translationBuilder.add(RDItems.CURSED_DECOY_DOLl, "被诅咒的诱饵人偶");
        translationBuilder.add(RDItems.VAISRAVANAS_PAGODA, "毘沙门天的宝塔");
        translationBuilder.add(RDItems.DREAM_PILLOW, "梦境枕头");
        translationBuilder.add(RDItems.TENGU_SHIELD, "天狗手盾");
        translationBuilder.add(RDItems.TENGU_CAMERA, "天狗相机");
        translationBuilder.add(RDItems.HIMEKAIDOU_HATATES_PHONE, "姬海棠果的手机");
        translationBuilder.add(RDItems.BAD_APPLE, "Bad Apple!!");
        translationBuilder.add(RDItems.SCARECROW, "稻草人");
        translationBuilder.add(RDItems.EXORCISM_PAPER, "驱魔符纸");
        translationBuilder.add(RDItems.COPPER_COIN, "铜币");
        translationBuilder.add(RDItems.SILVER_COIN, "银币");
        translationBuilder.add(RDItems.GOLD_COIN, "金币");
        translationBuilder.add(RDItems.SPELLCARD, "符卡");
        translationBuilder.add(RDItems.SATORI_EYE, "觉之眼");
        translationBuilder.add(RDItems.WEAPON_OF_THE_MOON, "月之兵器");

        // 武器
        translationBuilder.add(RDItems.HAKUREI_CANE, "博丽御币");
        translationBuilder.add(RDItems.WIND_BLESSING_CANE, "祝风御币");
        translationBuilder.add(RDItems.MAGIC_BROOM, "魔法扫帚");
        translationBuilder.add(RDItems.GUNGNIR, "神枪冈格尼尔");
        translationBuilder.add(RDItems.LEVATIN, "莱瓦汀");
        translationBuilder.add(RDItems.ROKANKEN, "楼观剑");
        translationBuilder.add(RDItems.HAKUROKEN, "白楼剑");
        translationBuilder.add(RDItems.PAPILIO_PATTERN_FAN, "凤蝶纹扇");
        translationBuilder.add(RDItems.IBUKIHO, "伊吹瓢");
        translationBuilder.add(RDItems.SWORD_OF_HISOU, "非想之剑");
        translationBuilder.add(RDItems.MANPOZUCHI, "万宝槌");
        translationBuilder.add(RDItems.NUE_TRIDENT, "鵺之三叉戟");
        translationBuilder.add(RDItems.TRUMPET_GUN, "喇叭枪");
        translationBuilder.add(RDItems.TREASURE_HUNTING_ROD, "探宝棒");
        translationBuilder.add(RDItems.KNIFE, "飞刀");
        translationBuilder.add(RDItems.MAPLE_LEAF_FAN, "枫叶团扇");
        translationBuilder.add(RDItems.VIOLIN, "小提琴");
        translationBuilder.add(RDItems.KEYBOARD, "键盘");
        translationBuilder.add(RDItems.TRUMPET, "小号");
        translationBuilder.add(RDItems.DEATH_SCYTHE, "死神镰刀");

        // 工具矿物类
        translationBuilder.add(RDItems.RAW_SILVER, "生银矿");
        translationBuilder.add(RDItems.SILVER_INGOT, "银锭");
        translationBuilder.add(RDItems.SILVER_NUGGET, "银粒");
        translationBuilder.add(RDItems.SILVER_SWORD, "银剑");
        translationBuilder.add(RDItems.SILVER_AXE, "银斧");
        translationBuilder.add(RDItems.SILVER_PICKAXE, "银镐");
        translationBuilder.add(RDItems.SILVER_SHOVEL, "银锹");
        translationBuilder.add(RDItems.SILVER_HOE, "银锄");
        translationBuilder.add(RDItems.SILVER_SPEAR, "银矛");
        translationBuilder.add(RDItems.SILVER_HELMET, "银头盔");
        translationBuilder.add(RDItems.SILVER_CHESTPLATE, "银胸甲");
        translationBuilder.add(RDItems.SILVER_LEGGINGS, "银护腿");
        translationBuilder.add(RDItems.SILVER_BOOTS, "银靴子");
        translationBuilder.add(RDItems.ICE_SCALES, "冰之鳞");
        translationBuilder.add(RDItems.MAGIC_ICE_SWORD, "冰剑");
        translationBuilder.add(RDItems.MAGIC_ICE_AXE, "冰斧");
        translationBuilder.add(RDItems.MAGIC_ICE_PICKAXE, "冰稿");
        translationBuilder.add(RDItems.MAGIC_ICE_SHOVEL, "冰锹");
        translationBuilder.add(RDItems.MAGIC_ICE_HOE, "冰锄");
        translationBuilder.add(RDItems.MAGIC_ICE_SPEAR, "冰矛");
        translationBuilder.add(RDItems.MAGIC_ICE_HELMET, "冰头盔");
        translationBuilder.add(RDItems.MAGIC_ICE_CHESTPLATE, "冰胸甲");
        translationBuilder.add(RDItems.MAGIC_ICE_LEGGINGS, "冰护腿");
        translationBuilder.add(RDItems.MAGIC_ICE_BOOTS, "冰靴子");
        translationBuilder.add(RDItems.MAID_HAIRBAND, "女仆发卡");
        translationBuilder.add(RDItems.MAID_UPPER_SKIRT, "女仆上衣");
        translationBuilder.add(RDItems.MAID_LOWER_SKIRT, "女仆裙");
        translationBuilder.add(RDItems.MAID_SHOE, "女仆鞋子");
        translationBuilder.add(RDItems.DANMAKU_SHAPE_CREATOR, "弹幕创作模板");
        translationBuilder.add(RDItems.SPELL_CARD_TEMPLATE, "符卡模板");
        translationBuilder.add(RDItems.DREAM_SWORD, "梦境水晶剑");
        translationBuilder.add(RDItems.DREAM_AXE, "梦境水晶斧");
        translationBuilder.add(RDItems.DREAM_PICKAXE, "梦境水晶稿");
        translationBuilder.add(RDItems.DREAM_SHOVEL, "梦境水晶锹");
        translationBuilder.add(RDItems.DREAM_HOE, "梦境水晶锄");
        translationBuilder.add(RDItems.DREAM_SPEAR, "梦境水晶矛");
        translationBuilder.add(RDItems.DREAM_HELMET, "梦境水晶头盔");
        translationBuilder.add(RDItems.DREAM_CHESTPLATE, "梦境水晶胸甲");
        translationBuilder.add(RDItems.DREAM_LEGGINGS, "梦境水晶护腿");
        translationBuilder.add(RDItems.DREAM_BOOTS, "梦境水晶靴子");
        translationBuilder.add(RDItems.WATER_PROOF_HAT, "防水帽子");
        translationBuilder.add(RDItems.WATER_PROOF_CLOTHING, "防水衣");
        translationBuilder.add(RDItems.WATER_PROOF_LEGGINGS, "防水护腿");
        translationBuilder.add(RDItems.WATER_PROOF_BOOTS, "防水靴子");

        // 角色卡
        translationBuilder.add(RDItems.ROLE_CARD, "空白角色卡");
        translationBuilder.add(RDItems.ROLE_ARCHIVE, "角色存档卡");

        // 其他


//        translationBuilder.add(ModItems.DEBUG_DANMAKU_ITEM, "调试弹幕");
//        translationBuilder.add(ModItems.DEBUG_SPELL_CARD_ITEM, "调试符卡");
//        translationBuilder.add(ModItems.DEBUG_SPELL_CARD_ITEM2, "调试符卡2");

        this.generateDanmakuItemTranslations(wrapperLookup, translationBuilder, true);
    }

    public void generateSoundTranslations(HolderLookup.Provider wrapperLookup, TranslationBuilder translationBuilder) {
        TranslationWrapper builder = ITranslationExporter.ofWrapper(wrapperLookup, translationBuilder);

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
        builder.generateSoundEventSubtitle(SoundEventInit.GRAZE, "擦弹");

        this.generateDiscTranslations(wrapperLookup, translationBuilder);
        this.generateDanmakuType(wrapperLookup, translationBuilder);
    }

    public void generateDanmakuType(HolderLookup.Provider wrapperLookup, TranslationBuilder translationBuilder) {
        TranslationWrapper builder = ITranslationExporter.ofWrapper(wrapperLookup, translationBuilder);

        builder.generateDanmakuType(DanmakuTrajectories.SINGLE, "线性");
        builder.generateDanmakuType(DanmakuTrajectories.TRIPLE, "三线");
        builder.generateDanmakuType(DanmakuTrajectories.BULLET, "子弹");
        builder.generateDanmakuType(DanmakuTrajectories.HEART, "心形");
        builder.generateDanmakuType(DanmakuTrajectories.X, "十字");
        builder.generateDanmakuType(DanmakuTrajectories.STAR, "星星");
        builder.generateDanmakuType(DanmakuTrajectories.ROUND, "圆");
        builder.generateDanmakuType(DanmakuTrajectories.RING, "环");
    }

    public void generateDiscTranslations(HolderLookup.Provider wrapperLookup, TranslationBuilder translationBuilder) {
        TranslationWrapper builder = ITranslationExporter.ofWrapper(wrapperLookup, translationBuilder);

        builder.generateJukeBox(JukeboxSongInit.HR01_01.getJukeboxSongRegistryKey(), "蓬莱人形　～ Dolls in Pseudo Paradise. - 蓬莱伝説");
        builder.generateJukeBox(JukeboxSongInit.HR02_08.getJukeboxSongRegistryKey(), "莲台野夜行 - 过去的花 ～ Fairy of Flower");
        builder.generateJukeBox(JukeboxSongInit.HR03_01.getJukeboxSongRegistryKey(), "ZUN - 童祭　～ Innocent Treasures");
        builder.generateJukeBox(JukeboxSongInit.MELODIC_TASTE_NIGHTMARE_BEFORE_CROSSROADS.getJukeboxSongRegistryKey(), "Melodic-Taste-Nightmare-before-Crossroads");
        builder.generateJukeBox(JukeboxSongInit.YV_FLOWER_CLOCK_AND_DREAMS.getJukeboxSongRegistryKey(), "Yonder-Voice - 花時計と夢");
        builder.generateJukeBox(JukeboxSongInit.GLOWING_NEEDLES_LITTLE_PEOPLE.getJukeboxSongRegistryKey(), "Inchlings of the Shining Needle ~ Little Princess : 「Miracle Remix」");
        builder.generateJukeBox(JukeboxSongInit.COOKIE.getJukeboxSongRegistryKey(), "温馨的神社（迫真）");
        builder.generateJukeBox(JukeboxSongInit.BAD_APPLE.getJukeboxSongRegistryKey(), "Bad Apple");
    }

    public void generateBlockTranslations(HolderLookup.Provider wrapperLookup, TranslationBuilder translationBuilder) {
        translationBuilder.add(RDBlocks.DANMAKU_CRAFTING_TABLE, "弹幕工作台");
        translationBuilder.add(RDBlocks.GENSOKYO_ALTAR, "幻想乡祭坛");
        translationBuilder.add(RDBlocks.STRENGTH_TABLE, "强化台");
        translationBuilder.add(RDBlocks.MUSIC_BLOCK, "音乐盒");

        translationBuilder.add(RDBlocks.MAGIC_ICE_BLOCK, "魔法冰");
        translationBuilder.add(RDBlocks.POINT_BLOCK, "Point方块");
        translationBuilder.add(RDBlocks.POWER_BLOCK, "Power方块");
        translationBuilder.add(RDBlocks.SILVER_ORE, "银矿石");
        translationBuilder.add(RDBlocks.DEEPSLATE_SILVER_ORE, "深层银矿石");
        translationBuilder.add(RDBlocks.SILVER_BLOCK, "银块");
        translationBuilder.add(RDBlocks.SILVER_CHEST_BLOCK.chestBlock(), "银箱子");
        translationBuilder.add(RDBlocks.ORB_ORE, "宝玉矿石");
        translationBuilder.add(RDBlocks.DEEPSLATE_ORB_ORE, "深层宝玉矿石");
        translationBuilder.add(RDBlocks.RED_ORB_BLOCK, "红宝玉块");
        translationBuilder.add(RDBlocks.YELLOW_ORB_BLOCK, "黄宝玉块");
        translationBuilder.add(RDBlocks.BLUE_ORB_BLOCK, "蓝宝玉块");
        translationBuilder.add(RDBlocks.GREEN_ORB_BLOCK, "绿宝玉块");
        translationBuilder.add(RDBlocks.PURPLE_ORB_BLOCK, "紫宝玉块");

        translationBuilder.add(RDBlocks.MARISA_HAT_BLOCK, "魔理沙的帽子");
        translationBuilder.add(RDBlocks.CASH_BOX_BLOCK, "塞钱箱");
        translationBuilder.add(RDBlocks.ANTI_COLLISION_BARREL, "防撞桶");
        translationBuilder.add(RDBlocks.WHEEL_CHAIR, "轮椅");
        translationBuilder.add(RDBlocks.WOODEN_BOX.chestBlock(), "木板箱");

        translationBuilder.add(RDBlocks.DREAM_RED_BLOCK, "网格方块");
        translationBuilder.add(RDBlocks.DREAM_BLUE_BLOCK, "网格方块");
        translationBuilder.add(RDBlocks.DREAM_CRYSTAL_ORE, "梦境水晶矿");

        translationBuilder.add(RDWoodBlocks.SPIRITUAL.log(), "绳文杉原木");
        translationBuilder.add(RDWoodBlocks.SPIRITUAL.wood(), "绳文杉树皮");
        translationBuilder.add(RDWoodBlocks.SPIRITUAL.stairs(), "绳文杉楼梯");
        translationBuilder.add(RDWoodBlocks.SPIRITUAL.slab(), "绳文杉台阶");
        translationBuilder.add(RDWoodBlocks.SPIRITUAL.door(), "绳文杉门");
        translationBuilder.add(RDWoodBlocks.SPIRITUAL.trapdoor(), "绳文杉活版门");
        translationBuilder.add(RDWoodBlocks.SPIRITUAL.fence(), "绳文杉栅栏");
        translationBuilder.add(RDWoodBlocks.SPIRITUAL.fenceGate(), "绳文杉栅栏门");
        translationBuilder.add(RDWoodBlocks.SPIRITUAL.button(), "绳文杉按钮");
        translationBuilder.add(RDWoodBlocks.SPIRITUAL.strippedLog(), "去皮绳文杉");
        translationBuilder.add(RDWoodBlocks.SPIRITUAL.strippedWood(), "去皮绳文杉树皮");
        translationBuilder.add(RDWoodBlocks.BLESSED_SPIRITUAL_LOG, "被祝福的绳文杉柱");
        translationBuilder.add(RDWoodBlocks.SPIRITUAL.leaves(), "绳文杉树叶");
        translationBuilder.add(RDWoodBlocks.SPIRITUAL.sapling(), "绳文杉树苗");
        translationBuilder.add(RDWoodBlocks.SPIRITUAL.planks(), "绳文杉木板");

        translationBuilder.add(RDBlocks.ICE_SCALES.block(), "冰鳞砖块");
        translationBuilder.add(RDBlocks.ICE_SCALES.stair(), "冰鳞砖楼梯");
        translationBuilder.add(RDBlocks.ICE_SCALES.slab(), "冰鳞砖台阶");
        translationBuilder.add(RDBlocks.ICE_SCALES.wall(), "冰鳞砖墙");

        translationBuilder.add(RDBlocks.DREAM_STONE.block(), "梦境石");
        translationBuilder.add(RDBlocks.DREAM_STONE.stair(), "梦境石楼梯");
        translationBuilder.add(RDBlocks.DREAM_STONE.slab(), "梦境石台阶");
        translationBuilder.add(RDBlocks.DREAM_STONE.wall(), "梦境石砖墙");
        translationBuilder.add(RDBlocks.DREAM_STONE_BRICK.block(), "梦境砖块");
        translationBuilder.add(RDBlocks.DREAM_STONE_BRICK.stair(), "梦境砖楼梯");
        translationBuilder.add(RDBlocks.DREAM_STONE_BRICK.slab(), "梦境砖台阶");
        translationBuilder.add(RDBlocks.DREAM_STONE_BRICK.wall(), "梦境砖墙");

        translationBuilder.add(RDBlocks.MOON_STONE.block(), "月石");
        translationBuilder.add(RDBlocks.MOON_STONE.stair(), "月石楼梯");
        translationBuilder.add(RDBlocks.MOON_STONE.slab(), "月石台阶");
        translationBuilder.add(RDBlocks.MOON_STONE.wall(), "月石墙");
        translationBuilder.add(RDBlocks.MOON_STONE_BRICK.block(), "月石砖块");
        translationBuilder.add(RDBlocks.MOON_STONE_BRICK.stair(), "月石砖楼梯");
        translationBuilder.add(RDBlocks.MOON_STONE_BRICK.slab(), "月石砖台阶");
        translationBuilder.add(RDBlocks.MOON_STONE_BRICK.wall(), "月石砖墙");

        translationBuilder.add(RDWoodBlocks.LEMON.log(), "柠檬原木");
        translationBuilder.add(RDWoodBlocks.LEMON.wood(), "柠檬树皮");
        translationBuilder.add(RDWoodBlocks.LEMON.stairs(), "柠檬楼梯");
        translationBuilder.add(RDWoodBlocks.LEMON.slab(), "柠檬台阶");
        translationBuilder.add(RDWoodBlocks.LEMON.door(), "柠檬门");
        translationBuilder.add(RDWoodBlocks.LEMON.trapdoor(), "柠檬活版门");
        translationBuilder.add(RDWoodBlocks.LEMON.fence(), "柠檬栅栏");
        translationBuilder.add(RDWoodBlocks.LEMON.fenceGate(), "柠檬栅栏门");
        translationBuilder.add(RDWoodBlocks.LEMON.button(), "柠檬按钮");
        translationBuilder.add(RDWoodBlocks.LEMON.strippedLog(), "去皮柠檬木");
        translationBuilder.add(RDWoodBlocks.LEMON.strippedWood(), "去皮柠檬树皮");
        translationBuilder.add(RDWoodBlocks.LEMON.leaves(), "柠檬树叶");
        translationBuilder.add(RDWoodBlocks.LEMON.sapling(), "柠檬树苗");
        translationBuilder.add(RDWoodBlocks.LEMON.planks(), "柠檬木板");
        translationBuilder.add(RDWoodBlocks.LEMON_FRUIT_LEAVES, "柠檬果树叶");

        translationBuilder.add(RDWoodBlocks.GINKGO.log(), "白果原木");
        translationBuilder.add(RDWoodBlocks.GINKGO.wood(), "白果树皮");
        translationBuilder.add(RDWoodBlocks.GINKGO.stairs(), "白果楼梯");
        translationBuilder.add(RDWoodBlocks.GINKGO.slab(), "白果台阶");
        translationBuilder.add(RDWoodBlocks.GINKGO.door(), "白果门");
        translationBuilder.add(RDWoodBlocks.GINKGO.trapdoor(), "白果活版门");
        translationBuilder.add(RDWoodBlocks.GINKGO.fence(), "白果栅栏");
        translationBuilder.add(RDWoodBlocks.GINKGO.fenceGate(), "白果栅栏门");
        translationBuilder.add(RDWoodBlocks.GINKGO.button(), "白果按钮");
        translationBuilder.add(RDWoodBlocks.GINKGO.strippedLog(), "去皮白果木");
        translationBuilder.add(RDWoodBlocks.GINKGO.strippedWood(), "去皮白果树皮");
        translationBuilder.add(RDWoodBlocks.GINKGO.leaves(), "白果树叶");
        translationBuilder.add(RDWoodBlocks.GINKGO.sapling(), "白果树苗");
        translationBuilder.add(RDWoodBlocks.GINKGO.planks(), "白果木板");
        translationBuilder.add(RDWoodBlocks.GINKGO_FRUIT_LEAVES, "白果果树叶");

        translationBuilder.add(RDWoodBlocks.PEACH.log(), "桃木原木");
        translationBuilder.add(RDWoodBlocks.PEACH.wood(), "桃木树皮");
        translationBuilder.add(RDWoodBlocks.PEACH.stairs(), "桃木楼梯");
        translationBuilder.add(RDWoodBlocks.PEACH.slab(), "桃木台阶");
        translationBuilder.add(RDWoodBlocks.PEACH.door(), "桃木门");
        translationBuilder.add(RDWoodBlocks.PEACH.trapdoor(), "桃木活版门");
        translationBuilder.add(RDWoodBlocks.PEACH.fence(), "桃木栅栏");
        translationBuilder.add(RDWoodBlocks.PEACH.fenceGate(), "桃木栅栏门");
        translationBuilder.add(RDWoodBlocks.PEACH.button(), "桃木按钮");
        translationBuilder.add(RDWoodBlocks.PEACH.strippedLog(), "去皮桃木");
        translationBuilder.add(RDWoodBlocks.PEACH.strippedWood(), "去皮桃木树皮");
        translationBuilder.add(RDWoodBlocks.PEACH.leaves(), "桃木树叶");
        translationBuilder.add(RDWoodBlocks.PEACH.sapling(), "桃木树苗");
        translationBuilder.add(RDWoodBlocks.PEACH.planks(), "桃木木板");
        translationBuilder.add(RDWoodBlocks.PEACH_FRUIT_LEAVES, "桃果树叶");
    }

    public void generateFumoTranslations(HolderLookup.Provider wrapperLookup, TranslationBuilder translationBuilder) {
        translationBuilder.add(FumoTypes.AYA.block(), "射命丸文Fumo");
        translationBuilder.add(FumoTypes.ALICE.block(), "爱丽丝Fumo");
        translationBuilder.add(FumoTypes.CHEN.block(), "八云橙Fumo");
        translationBuilder.add(FumoTypes.BLUE_REIMU.block(), "蓝灵梦Fumo");
        translationBuilder.add(FumoTypes.CHERRIES_SHION.block(), "依神紫苑Fumo");
        translationBuilder.add(FumoTypes.CHIMATA.block(), "天弓千亦Fumo");
        translationBuilder.add(FumoTypes.CIRNO.block(), "琪露诺Fumo");
        translationBuilder.add(FumoTypes.CLOWNPIECE.block(), "克劳恩皮丝Fumo");
        translationBuilder.add(FumoTypes.EIKI.block(), "四季映姫Fumo");
        translationBuilder.add(FumoTypes.EIRIN.block(), "八意永琳Fumo");
        translationBuilder.add(FumoTypes.FLAN.block(), "芙兰朵露·斯卡蕾特Fumo");
        translationBuilder.add(FumoTypes.FLANDRE.block(), "芙兰朵露·斯卡蕾特Fumo");
        translationBuilder.add(FumoTypes.HATATE.block(), "姬海棠果Fumo");
        translationBuilder.add(FumoTypes.JOON.block(), "依神女苑Fumo");
        translationBuilder.add(FumoTypes.KAGUYA.block(), "蓬莱山辉夜Fumo");
        translationBuilder.add(FumoTypes.KANMARISA.block(), "雾雨魔理沙Fumo");
        translationBuilder.add(FumoTypes.KASEN.block(), "茨木华扇Fumo");
        translationBuilder.add(FumoTypes.KOGASA.block(), "多多良小伞Fumo");
        translationBuilder.add(FumoTypes.KOISHI.block(), "古明地恋Fumo");
        translationBuilder.add(FumoTypes.KOKORO.block(), "秦心Fumo");
        translationBuilder.add(FumoTypes.KOSUZU.block(), "本居小铃Fumo");
        translationBuilder.add(FumoTypes.MARISA.block(), "雾雨魔理沙Fumo");
        translationBuilder.add(FumoTypes.MARISA_HAT.block(), "雾雨魔理沙Fumo");
        translationBuilder.add(FumoTypes.MCKY.block(), "天弓千亦Fumo");
        translationBuilder.add(FumoTypes.MEILING.block(), "红美铃Fumo");
        translationBuilder.add(FumoTypes.MELON.block(), "伊吹萃香Fumo");
        translationBuilder.add(FumoTypes.MOKOU.block(), "藤原妹红Fumo");
        translationBuilder.add(FumoTypes.MOMIJI.block(), "犬走椛Fumo");
        translationBuilder.add(FumoTypes.MYSTIA.block(), "米斯蒂娅·萝蕾拉Fumo");
        translationBuilder.add(FumoTypes.NAZRIN.block(), "娜兹玲Fumo");
        translationBuilder.add(FumoTypes.NEW_REIMU.block(), "博丽灵梦Fumo");
        translationBuilder.add(FumoTypes.NUE.block(), "封兽鵺Fumo");
        translationBuilder.add(FumoTypes.PARSEE.block(), "水桥帕露西Fumo");
        translationBuilder.add(FumoTypes.PATCHOULI.block(), "帕秋莉·诺蕾姬Fumo");
        translationBuilder.add(FumoTypes.PC98_MARISA.block(), "雾雨魔理沙Fumo（旧作）");
        translationBuilder.add(FumoTypes.RAN.block(), "八云蓝Fumo");
        translationBuilder.add(FumoTypes.REIMU.block(), "博丽灵梦Fumo");
        translationBuilder.add(FumoTypes.REISEN.block(), "铃仙·优昙华院·因幡Fumo");
        translationBuilder.add(FumoTypes.REMILIA.block(), "蕾米莉亚·斯卡蕾特Fumo");
        translationBuilder.add(FumoTypes.RENKO.block(), "宇佐见莲子Fumo");
        translationBuilder.add(FumoTypes.RUMIA.block(), "露米娅Fumo");
        translationBuilder.add(FumoTypes.SAKUYA.block(), "十六夜咲夜Fumo");
        translationBuilder.add(FumoTypes.SANAE.block(), "东风谷早苗Fumo");
        translationBuilder.add(FumoTypes.SATORI.block(), "古明地觉Fumo");
        translationBuilder.add(FumoTypes.SEIJA.block(), "鬼人正邪Fumo");
        translationBuilder.add(FumoTypes.SHION.block(), "依神紫苑Fumo");
        translationBuilder.add(FumoTypes.SHOU.block(), "寅丸星Fumo");
        translationBuilder.add(FumoTypes.SMART_CIRNO.block(), "聪明琪露诺Fumo");
        translationBuilder.add(FumoTypes.SUIKA.block(), "伊吹萃香Fumo");
        translationBuilder.add(FumoTypes.SUWAKO.block(), "洩矢诹访子Fumo");
        translationBuilder.add(FumoTypes.TAN_CIRNO.block(), "大琪露诺Fumo");
        translationBuilder.add(FumoTypes.TENSHI.block(), "比那名居天子Fumo");
        translationBuilder.add(FumoTypes.TEWI.block(), "因幡帝Fumo");
        translationBuilder.add(FumoTypes.UTSUHO.block(), "灵乌路空Fumo");
        translationBuilder.add(FumoTypes.WAKASAGIHIME.block(), "若鹭姬Fumo");
        translationBuilder.add(FumoTypes.YOUMU.block(), "魂魄妖梦Fumo");
        translationBuilder.add(FumoTypes.YUKARI.block(), "八云紫Fumo");
        translationBuilder.add(FumoTypes.YUUKA.block(), "风见幽香Fumo");
        translationBuilder.add(FumoTypes.YUYUKO.block(), "西行寺幽幽子Fumo");
    }

    public void generateDanmakuItemTranslations(HolderLookup.Provider wrapperLookup, TranslationBuilder translationBuilder, boolean useChinese) {
//        Map<String, String> bulletTranslations = new HashMap<>();
//        bulletTranslations.put("amulet", "札弹");
//        bulletTranslations.put("arrow", "箭弹");
//        bulletTranslations.put("arrowhead", "鳞弹");
//        bulletTranslations.put("bacteria", "杆菌弹");
//        bulletTranslations.put("ball", "小玉");
//        bulletTranslations.put("ball_outlined", "小玉（描边）");
//        bulletTranslations.put("bubble", "大玉");
//        bulletTranslations.put("bullet", "铳弹");
//        bulletTranslations.put("butterfly", "蝶弹");
//        bulletTranslations.put("coin", "钱币弹");
//        bulletTranslations.put("fireball", "火光弹");
//        bulletTranslations.put("fireball_glowy", "水光弹");
//        bulletTranslations.put("heart", "心弹");
//        bulletTranslations.put("jellybean", "椭弹");
//        bulletTranslations.put("knife", "刀弹");
//        bulletTranslations.put("kunai", "链弹");
//        bulletTranslations.put("mentos", "中玉");
//        bulletTranslations.put("note", "音符弹");
//        bulletTranslations.put("orb", "光玉");
//        bulletTranslations.put("pellet", "点弹");
//        bulletTranslations.put("popcorn", "菌弹");
//        bulletTranslations.put("raindrop", "滴弹");
//        bulletTranslations.put("rest", "音符弹（休止符）");
//        bulletTranslations.put("rice", "米弹");
//        bulletTranslations.put("rose", "蔷薇弹");
//        bulletTranslations.put("shard", "棱角米弹");
//        bulletTranslations.put("star", "星弹");
//        bulletTranslations.put("star_big", "大星弹");
//        bulletTranslations.put("star_small", "小星弹");

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
        translationBuilder.add(DanmakuTypes.NOTE.getItem(), "音符");
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
