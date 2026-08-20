package cc.thonly.reverie_dreams.fabric.datagen;

import cc.thonly.reverie_dreams.block.bundle.CropBlockBundle;
import cc.thonly.reverie_dreams.creative_tab.content.*;
import cc.thonly.reverie_dreams.entity.npc.container.NPCCustomerContainer;
import cc.thonly.reverie_dreams.gui.RecipeTypeCategoryManager;
import cc.thonly.reverie_dreams.registry.content.*;
import cc.thonly.reverie_dreams.registry.content.advancements.RDAdvancements;
import cc.thonly.reverie_dreams.registry.content.block.RDKitchenBlocks;
import cc.thonly.reverie_dreams.registry.content.block.RDBlocks;
import cc.thonly.reverie_dreams.registry.content.block.RDCropBlocks;
import cc.thonly.reverie_dreams.registry.content.block.RDWoodBlocks;
import cc.thonly.reverie_dreams.registry.content.danmaku.DanmakuTrajectories;
import cc.thonly.reverie_dreams.registry.content.danmaku.DanmakuTypes;
import cc.thonly.reverie_dreams.registry.content.effect.RDPotions;
import cc.thonly.reverie_dreams.registry.content.effect.RDStatusEffects;
import cc.thonly.reverie_dreams.registry.content.entity.RDEntityTypes;
import cc.thonly.reverie_dreams.registry.content.item.RDBeverageItems;
import cc.thonly.reverie_dreams.registry.content.item.RDCuisineItems;
import cc.thonly.reverie_dreams.registry.content.item.RDIngredientItems;
import cc.thonly.reverie_dreams.registry.content.item.RDItems;
import cc.thonly.reverie_dreams.registry.content.villager.RDVillagerProfessions;
import cc.thonly.reverie_dreams.server.BookPageManagerImpl;
import cc.thonly.reverie_dreams.sound.JukeboxSongInit;
import cc.thonly.reverie_dreams.sound.RDSoundEvents;
import cc.thonly.reverie_dreams.world.RDBuiltInGameRules;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import org.jspecify.annotations.NonNull;

import java.util.concurrent.CompletableFuture;

public class SimpChineseLangProvider extends FabricLanguageProvider implements ITranslationWrapper {

    public SimpChineseLangProvider(FabricPackOutput dataOutput, CompletableFuture<HolderLookup.Provider> registryLookup) {
        super(dataOutput, "zh_cn", registryLookup);
    }

    @Override
    public void generateTranslations(HolderLookup.@NonNull Provider wrapperLookup, @NonNull TranslationBuilder translationBuilder) {
        TranslationWrapper builder = ITranslationWrapper.ofWrapper(wrapperLookup, translationBuilder);
        builder.add(ItemBlockCreativeTab.ITEM_GROUP_KEY, "梦隐的幻想乡 | 物品/方块");
        builder.add(DanmakuCreativeTab.BULLET_ITEM_GROUP_KEY, "梦隐的幻想乡 | 弹幕");
        builder.add(TemplateCreativeTab.ITEM_GROUP_KEY, "梦隐的幻想乡 | 弹幕模板");
        builder.add(FumoCreativeTab.ITEM_GROUP_KEY, "梦隐的幻想乡 | Fumo");
        builder.add(RoleCardCreativeTab.ITEM_GROUP_KEY, "梦隐的幻想乡 | 角色卡");
        builder.add(SpawnEggCreativeTab.ITEM_GROUP_KEY, "梦隐的幻想乡 | 刷怪蛋");
        builder.add(NPCSpawnEggCreativeTab.ITEM_GROUP_KEY, "梦隐的幻想乡 | 角色刷怪蛋");
        builder.add(KitchenwareCreativeTab.ITEM_GROUP_KEY, "梦隐的幻想乡 | 厨具");
        builder.add(IngredientCreativeTab.ITEM_GROUP_KEY, "梦隐的幻想乡 | 食材");
        builder.add(SeedCreativeTab.ITEM_GROUP_KEY, "梦隐的幻想乡 | 种子");
        builder.add(FoodCreativeTab.ITEM_GROUP_KEY, "梦隐的幻想乡 | 食物");
        builder.add(BeverageCreativeTab.ITEM_GROUP_KEY, "梦隐的幻想乡 | 饮品");
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
        translationBuilder.add(RDBuiltInGameRules.DO_GHOST.value().getDescriptionId(), "禁止幽灵生成");

        translationBuilder.add("item_view.information.desc.chest_drop_items", "获取方式：开启宝箱获得");
        translationBuilder.add("item_view.information.desc.grass_drop_items", "获取方式：破坏草获得");
        translationBuilder.add("item_view.information.desc.fumos", "获取方式：可在 Fumo 商人处购买");
        translationBuilder.add("item_view.information.desc.drinks", "获取方式：酒馆老板处可购买");
        translationBuilder.add("item_view.information.desc.fishing", "获取方式：通过钓鱼获得");
        translationBuilder.add("item_view.information.desc.truffle", "获取方式：破坏橡木/白桦/深色橡木/云杉原木获得");

        this.generateItemTranslations(wrapperLookup, translationBuilder);
        this.generateBlockTranslations(wrapperLookup, translationBuilder);
        this.generateFumoTranslations(wrapperLookup, translationBuilder);
        this.generateSoundTranslations(wrapperLookup, translationBuilder);
        this.generateEntityTranslations(wrapperLookup, translationBuilder);
        this.generateRoleTranslations(wrapperLookup, translationBuilder);
        this.generateRoleCardTranslations(wrapperLookup, translationBuilder);
        this.generateEffectTranslations(wrapperLookup, translationBuilder);
        this.generateGuideTranslations(wrapperLookup, translationBuilder);
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
        translationBuilder.add("gui.npc.info.favorability", "§6好感度：%s");
        translationBuilder.add("gui.npc.mode.null", "§b当前模式为: §c序列化错误");
        translationBuilder.add(NPCStates.FOLLOW.translateKey(), "§b当前模式为: §a跟随");
        translationBuilder.add(NPCStates.NORMAL.translateKey(), "§b当前模式为: §a正常");
        translationBuilder.add(NPCStates.NO_WALK.translateKey(), "§b当前模式为: §a禁止移动");
        translationBuilder.add(NPCStates.SNAKING.translateKey(), "§b当前模式为: §a潜行");
        translationBuilder.add(NPCStates.SEATED.translateKey(), "§b当前模式为: §a坐下");
        translationBuilder.add(NPCStates.WORKING.translateKey(), "§b当前模式为: §a工作中");
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
        translationBuilder.add(NPCWorkModes.CUSTOMER.translateKey(), "§9顾客");
        translationBuilder.add(NPCWorkModes.FISHING.translateKey(), "§6钓鱼");
        translationBuilder.add("gui.npc.work.mode.create-fly/hand_crank", "§d摇曲柄");
        translationBuilder.add("gui.npc.work.mode.polyfactory/hand_crank", "§d摇曲柄");
        translationBuilder.add("gui.reverie_dreams.progress", "进度");
        translationBuilder.add("gui.reverie_dreams.close", "关闭");
        translationBuilder.add("gui.reverie_dreams.input_request", "请输入内容：");
        translationBuilder.add("gui.reverie_dreams.send", "发送");
        translationBuilder.add("gui.reverie_dreams.custom_skin_selector.title", "第三方皮肤选择");
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

        translationBuilder.add(NPCCustomerContainer.REQUEST_FOOD_KEY, "§r请给我§b%1$s§r的料理");
        translationBuilder.add(NPCCustomerContainer.REQUEST_BEVERAGE_KEY, "§r请给我§b%1$s§r的饮料");
        translationBuilder.add(NPCCustomerContainer.COOLDOWN_KEY, "§e冷却中...");

        translationBuilder.add("death.attack.danmaku", "%1$s 满身疮痍");
        translationBuilder.add("death.attack.danmaku.player", "%1$s 与 %2$s 对战的过程中满身疮痍");
        translationBuilder.add("death.attack.danmaku.item", "%1$s 被 %2$s 在对战的过程中使用 %3$s 被打的满身疮痍");

        translationBuilder.add("config.jade.plugin_reverie_dreams.kitchenware_data_provider", "厨具信息");
        translationBuilder.add("config.jade.plugin_reverie_dreams.music_block_data_provider", "播放信息");
        translationBuilder.add("config.jade.plugin_reverie_dreams.gensokyo_altar_data_provider", "合成信息");
        translationBuilder.add("config.jade.plugin_reverie_dreams.role_description", "角色信息");

        translationBuilder.add("narration.reverie_dreams.food", "Food: %s");

        translationBuilder.addEnchantment(RDEnchantments.EXTERMINATION, "退治");
        translationBuilder.addEnchantment(RDEnchantments.MOON_DAMAGE, "月伤");
        translationBuilder.addEnchantment(RDEnchantments.DANMAKU_PROTECTION, "弹幕保护");
        translationBuilder.addEnchantment(RDEnchantments.POWERFUL, "威力");
        translationBuilder.addEnchantment(RDEnchantments.FROZEN, "冰封");
        translationBuilder.addEnchantment(RDEnchantments.CHARGE, "冲锋");

        this.generateCommandTranslations(wrapperLookup, translationBuilder);
        this.generateMITranslations(wrapperLookup, translationBuilder);
        this.generateItemDescriptionTranslations(wrapperLookup, translationBuilder);
        this.generateOrderChatTranslations(wrapperLookup, translationBuilder);
        this.generateRoleChatTranslations(wrapperLookup, translationBuilder);
        this.generateRoleDescriptionTranslations(wrapperLookup, translationBuilder);
        this.generateDefaultEvaluationTranslations(wrapperLookup, translationBuilder);
        this.generateEvaluationTranslations(wrapperLookup, translationBuilder);
        this.generateConfigTranslations(wrapperLookup, translationBuilder);
    }

    public void generateDefaultEvaluationTranslations(HolderLookup.Provider wrapperLookup, TranslationBuilder translationBuilder) {
        translationBuilder.add(CustomerDefaultEvaluationConsts.DEFAULT_1_EXBAD, "这顿饭……还是算了吧。");
        translationBuilder.add(CustomerDefaultEvaluationConsts.DEFAULT_1_BAD, "味道不太合我的胃口。");
        translationBuilder.add(CustomerDefaultEvaluationConsts.DEFAULT_1_NORM, "嗯，吃饱了。");
        translationBuilder.add(CustomerDefaultEvaluationConsts.DEFAULT_1_GOOD, "挺不错的，谢谢招待。");
        translationBuilder.add(CustomerDefaultEvaluationConsts.DEFAULT_1_EXGOOD, "太好吃了！下次还要来！");
        translationBuilder.add(CustomerDefaultEvaluationConsts.DEFAULT_1_LACK_MONEY_ANGRY, "连吃顿饭的钱都付不起，真让人不爽。");
        translationBuilder.add(CustomerDefaultEvaluationConsts.DEFAULT_1_LACK_MONEY_NORMAL, "钱好像带得不太够……剩下的下次补上可以吗？");
        translationBuilder.add(CustomerDefaultEvaluationConsts.DEFAULT_1_REPELL, "……嗯？怎么突然不想靠近了。");
        translationBuilder.add(CustomerDefaultEvaluationConsts.DEFAULT_1_SEEN_REPELL, "刚才是我看错了吗？");

        translationBuilder.add(CustomerDefaultEvaluationConsts.DEFAULT_2_EXBAD, "呜哇！这也太难吃了吧！");
        translationBuilder.add(CustomerDefaultEvaluationConsts.DEFAULT_2_BAD, "唔……没有想象中那么好吃。");
        translationBuilder.add(CustomerDefaultEvaluationConsts.DEFAULT_2_NORM, "还不错，刚好吃饱。");
        translationBuilder.add(CustomerDefaultEvaluationConsts.DEFAULT_2_GOOD, "不错不错，这顿吃得很开心！");
        translationBuilder.add(CustomerDefaultEvaluationConsts.DEFAULT_2_EXGOOD, "好吃！这简直是今天最大的惊喜！");
        translationBuilder.add(CustomerDefaultEvaluationConsts.DEFAULT_2_LACK_MONEY_ANGRY, "等等！为什么偏偏这时候钱不够啊！");
        translationBuilder.add(CustomerDefaultEvaluationConsts.DEFAULT_2_LACK_MONEY_NORMAL, "哎呀，今天好像没带够钱……");
        translationBuilder.add(CustomerDefaultEvaluationConsts.DEFAULT_2_REPELL, "咦？刚刚是不是有人在看我？");
        translationBuilder.add(CustomerDefaultEvaluationConsts.DEFAULT_2_SEEN_REPELL, "啊，被发现啦？");

        translationBuilder.add(CustomerDefaultEvaluationConsts.DEFAULT_3_EXBAD, "难以下咽。");
        translationBuilder.add(CustomerDefaultEvaluationConsts.DEFAULT_3_BAD, "并不值得期待。");
        translationBuilder.add(CustomerDefaultEvaluationConsts.DEFAULT_3_NORM, "可以。");
        translationBuilder.add(CustomerDefaultEvaluationConsts.DEFAULT_3_GOOD, "味道不错。");
        translationBuilder.add(CustomerDefaultEvaluationConsts.DEFAULT_3_EXGOOD, "……出乎意料地好吃。");
        translationBuilder.add(CustomerDefaultEvaluationConsts.DEFAULT_3_LACK_MONEY_ANGRY, "这种价格，你是在开玩笑吗？");
        translationBuilder.add(CustomerDefaultEvaluationConsts.DEFAULT_3_LACK_MONEY_NORMAL, "预算不够。剩下的钱，我之后再想办法。");
        translationBuilder.add(CustomerDefaultEvaluationConsts.DEFAULT_3_REPELL, "请不要靠近。");
        translationBuilder.add(CustomerDefaultEvaluationConsts.DEFAULT_3_SEEN_REPELL, "……原来你还看得见我。");

        translationBuilder.add(CustomerDefaultEvaluationConsts.DEFAULT_4_EXBAD, "这、这种东西也敢端上来？");
        translationBuilder.add(CustomerDefaultEvaluationConsts.DEFAULT_4_BAD, "哼，比我想的还差。");
        translationBuilder.add(CustomerDefaultEvaluationConsts.DEFAULT_4_NORM, "勉强能吃吧。");
        translationBuilder.add(CustomerDefaultEvaluationConsts.DEFAULT_4_GOOD, "还算不错……我姑且认可了。");
        translationBuilder.add(CustomerDefaultEvaluationConsts.DEFAULT_4_EXGOOD, "才、才不是因为好吃才想再来一次的！");
        translationBuilder.add(CustomerDefaultEvaluationConsts.DEFAULT_4_LACK_MONEY_ANGRY, "什么？居然还敢嫌我钱不够？");
        translationBuilder.add(CustomerDefaultEvaluationConsts.DEFAULT_4_LACK_MONEY_NORMAL, "咳……只是今天刚好没有带那么多。");
        translationBuilder.add(CustomerDefaultEvaluationConsts.DEFAULT_4_REPELL, "离我远一点，我现在不想看到你。");
        translationBuilder.add(CustomerDefaultEvaluationConsts.DEFAULT_4_SEEN_REPELL, "哼，终于注意到我了吗？");

        translationBuilder.add(CustomerDefaultEvaluationConsts.DEFAULT_5_EXBAD, "抱歉……这份食物让我有些难过。");
        translationBuilder.add(CustomerDefaultEvaluationConsts.DEFAULT_5_BAD, "味道有些不习惯，不过还是谢谢你。");
        translationBuilder.add(CustomerDefaultEvaluationConsts.DEFAULT_5_NORM, "暖暖的，刚好填饱肚子。");
        translationBuilder.add(CustomerDefaultEvaluationConsts.DEFAULT_5_GOOD, "这一餐让我感觉很舒服。");
        translationBuilder.add(CustomerDefaultEvaluationConsts.DEFAULT_5_EXGOOD, "真幸福啊……能吃到这样的料理。");
        translationBuilder.add(CustomerDefaultEvaluationConsts.DEFAULT_5_LACK_MONEY_ANGRY, "对不起，我现在真的没有足够的钱……");
        translationBuilder.add(CustomerDefaultEvaluationConsts.DEFAULT_5_LACK_MONEY_NORMAL, "今天带的钱不多，可以少收一些吗？");
        translationBuilder.add(CustomerDefaultEvaluationConsts.DEFAULT_5_REPELL, "请稍微离我远一点，好吗？");
        translationBuilder.add(CustomerDefaultEvaluationConsts.DEFAULT_5_SEEN_REPELL, "啊，你发现我了。谢谢你。");
    }

    public void generateEvaluationTranslations(HolderLookup.Provider wrapperLookup, TranslationBuilder translationBuilder) {
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_WRIGGLE_NIGHTBUG_EVALUATION_EXBAD, "居然这么对我，就让你看看虫子的厉害！");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_WRIGGLE_NIGHTBUG_EVALUATION_BAD, "哦！谢谢。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_WRIGGLE_NIGHTBUG_EVALUATION_NORM, "哇，不错嘛！");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_WRIGGLE_NIGHTBUG_EVALUATION_GOOD, "看来你很懂虫族的品味！");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_WRIGGLE_NIGHTBUG_EVALUATION_EXGOOD, "居然吃到这么合我心意的菜…一定要有所回报才行！");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_WRIGGLE_NIGHTBUG_EVALUATION_LACKMONEYANGRY, "连莉格露大人的钱都想抢，你胆子挺大！");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_WRIGGLE_NIGHTBUG_EVALUATION_LACKMONEYNORMAL, "算我欠你的人情啦~下次放心来找我帮忙吧！");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_WRIGGLE_NIGHTBUG_EVALUATION_REPELL, "居然敢赶萤火虫大人？！");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_WRIGGLE_NIGHTBUG_EVALUATION_SEENREPELL, "哎，真没劲。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_RUMIA_EVALUATION_EXBAD, "这就是所谓的黑暗料理吗？");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_RUMIA_EVALUATION_BAD, "是——这样吗？");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_RUMIA_EVALUATION_NORM, "挺好吃的哦。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_RUMIA_EVALUATION_GOOD, "很好吃哦！");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_RUMIA_EVALUATION_EXGOOD, "在你的料理下，感觉肉都会发光了！");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_RUMIA_EVALUATION_LACKMONEYANGRY, "你的价格就像人类采用了十进制！");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_RUMIA_EVALUATION_LACKMONEYNORMAL, "就带了这么多啦…");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_RUMIA_EVALUATION_REPELL, "为什么不让我吃啦？");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_RUMIA_EVALUATION_SEENREPELL, "你也会像那样赶我走吗？");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_CHEN_EVALUATION_EXBAD, "这种东西连野猫都不吃！");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_CHEN_EVALUATION_BAD, "也就这种程度吧。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_CHEN_EVALUATION_NORM, "还、还不错！");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_CHEN_EVALUATION_GOOD, "看在这么好吃的份上，就夸你一下吧！");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_CHEN_EVALUATION_EXGOOD, "好、好好粗！不是！好好吃！！");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_CHEN_EVALUATION_LACKMONEYANGRY, "看来给你的贷款利息确实是太高了…");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_CHEN_EVALUATION_LACKMONEYNORMAL, "没、没带够，下次贷款考虑给你优惠点！");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_CHEN_EVALUATION_REPELL, "走就走！我才不稀罕！");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_CHEN_EVALUATION_SEENREPELL, "不要放过每一个客人，给我好好赚钱还债啊！");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_KAMISHIRASAWA_KEINE_EVALUATION_EXBAD, "食之无味，即便弃之也不可惜。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_KAMISHIRASAWA_KEINE_EVALUATION_BAD, "勤加练习的话一定可以做得更好。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_KAMISHIRASAWA_KEINE_EVALUATION_NORM, "味道不错，相信你不会止步于此。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_KAMISHIRASAWA_KEINE_EVALUATION_GOOD, "很美味，看来你下了不少功夫。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_KAMISHIRASAWA_KEINE_EVALUATION_EXGOOD, "太美味了！这是沉淀了历史的技术才能做出来的佳肴！");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_KAMISHIRASAWA_KEINE_EVALUATION_LACKMONEYANGRY, "这个定价恐怕不太适合幻想乡的居民。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_KAMISHIRASAWA_KEINE_EVALUATION_LACKMONEYNORMAL, "抱歉，下次我会注意点的。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_KAMISHIRASAWA_KEINE_EVALUATION_REPELL, "如果你是我的学生，我就上头槌了！");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_KAMISHIRASAWA_KEINE_EVALUATION_SEENREPELL, "这种行为实在太没有教养了。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_REIMU_EVALUATION_EXBAD, "给我吃这种东西是想被退治吗？！");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_REIMU_EVALUATION_BAD, "也没比我做的好多少嘛。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_REIMU_EVALUATION_NORM, "做得不错嘛。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_REIMU_EVALUATION_GOOD, "好吃！不枉我特意攒钱来吃！");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_REIMU_EVALUATION_EXGOOD, "这一顿，就算花光所有钱也是值得的！");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_REIMU_EVALUATION_LACKMONEYANGRY, "敢诓我的钱？我看你是想被退治了！！！");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_REIMU_EVALUATION_LACKMONEYNORMAL, "你以前参加我家的宴会也没给钱，总得给我打个折吧。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_REIMU_EVALUATION_REPELL, "可恶，我没退治你，你居然敢赶我走？！");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_REIMU_EVALUATION_SEENREPELL, "要是之后被投诉了我可不管。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_SUIKA_EVALUATION_EXBAD, "佐上这样的菜肴，真是糟蹋了我的酒！");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_SUIKA_EVALUATION_BAD, "就着酒的话，勉强也可以吃两口。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_SUIKA_EVALUATION_NORM, "不错的下酒菜！");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_SUIKA_EVALUATION_GOOD, "美酒佳肴，不醉不休！");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_SUIKA_EVALUATION_EXGOOD, "没想到，这世上居然有比酒更让我垂涎的料理！");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_SUIKA_EVALUATION_LACKMONEYANGRY, "失望的宴会…");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_SUIKA_EVALUATION_LACKMONEYNORMAL, "钱不够了。下次请你喝酒吧！");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_SUIKA_EVALUATION_REPELL, "是鬼离开了太久，让你忘了我的恐怖吗？");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_SUIKA_EVALUATION_SEENREPELL, "居然在宴席上驱赶客人，真是无趣。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_TENSHI_EVALUATION_EXBAD, "咸若啮檗吞针！");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_TENSHI_EVALUATION_BAD, "哼，就地上人而言，差不多也就这种程度吧。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_TENSHI_EVALUATION_NORM, "区区地上人，做得还挺像样的。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_TENSHI_EVALUATION_GOOD, "此等美味，就算放在天界也毫不逊色。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_TENSHI_EVALUATION_EXGOOD, "甚合吾心！没想到这种贫瘠之地竟能烹饪出此等佳肴！");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_TENSHI_EVALUATION_LACKMONEYANGRY, "哼，吾来汝店，本即无上荣光。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_TENSHI_EVALUATION_LACKMONEYNORMAL, "哼，这一顿也就值这么多！");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_TENSHI_EVALUATION_REPELL, "蚍蜉撼大树，可笑不自量！");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_TENSHI_EVALUATION_SEENREPELL, "斗筲之人，何足算也。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_MARISA_EVALUATION_EXBAD, "这什么东西啊，还不如生吞蘑菇呢！");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_MARISA_EVALUATION_BAD, "一般般吧。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_MARISA_EVALUATION_NORM, "味道不错诶，教教我怎么做呗。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_MARISA_EVALUATION_GOOD, "这个菜，感觉可以成为宴会的主角！");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_MARISA_EVALUATION_EXGOOD, "你是偷偷藏了什么魔法吗？居然会有这么好吃的料理！");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_MARISA_EVALUATION_LACKMONEYANGRY, "我去借点钱再来吃！");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_MARISA_EVALUATION_LACKMONEYNORMAL, "账我已经付啦。你感觉箱子里的钱少了？不关我事哦。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_MARISA_EVALUATION_REPELL, "相信我！我这回真的带钱了！");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_MARISA_EVALUATION_SEENREPELL, "这样不太友好啊。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_MEIRIN_EVALUATION_EXBAD, "太过分！居然拿这种东西来敷衍我！");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_MEIRIN_EVALUATION_BAD, "能吃饱就行吧…");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_MEIRIN_EVALUATION_NORM, "唔…还算好吃吧？");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_MEIRIN_EVALUATION_GOOD, "不愧是我冒着被刀子扎的风险也要来吃的菜。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_MEIRIN_EVALUATION_EXGOOD, "能吃到这样的菜，就算脑袋扎满刀子也无憾了！");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_MEIRIN_EVALUATION_LACKMONEYANGRY, "区区门卫吃不起你家的菜！");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_MEIRIN_EVALUATION_LACKMONEYNORMAL, "我的工资只剩这么多了…");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_MEIRIN_EVALUATION_REPELL, "老板娘好无情啊！");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_MEIRIN_EVALUATION_SEENREPELL, "老板娘原来是这样的…");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_CIRNO_EVALUATION_EXBAD, "像这种东西，就应该和英吉利牛肉一起被冰冻起来！");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_CIRNO_EVALUATION_BAD, "别以为这样就能把我打发掉！");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_CIRNO_EVALUATION_NORM, "吃饭的时候说话会咬到舌头。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_CIRNO_EVALUATION_GOOD, "好吃！以后就让你跟着本小姐混吧！");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_CIRNO_EVALUATION_EXGOOD, "好吃得舌头都要融化了！");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_CIRNO_EVALUATION_LACKMONEYANGRY, "咱！没！钱！");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_CIRNO_EVALUATION_LACKMONEYNORMAL, "咱们妖精哪来那么多钱。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_CIRNO_EVALUATION_REPELL, "居然敢把最强的咱赶走。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_CIRNO_EVALUATION_SEENREPELL, "赶人就不好玩了啊。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_PATCHOULI_EVALUATION_EXBAD, "不堪一试！这种水平也敢开店？");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_PATCHOULI_EVALUATION_BAD, "食不言，寝不语。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_PATCHOULI_EVALUATION_NORM, "要是能读点书，应该能做得更好。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_PATCHOULI_EVALUATION_GOOD, "这种沁人心脾的口感到底是怎么做出来的？");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_PATCHOULI_EVALUATION_EXGOOD, "即便博览群书，也很难有这等厨技造诣啊。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_PATCHOULI_EVALUATION_LACKMONEYANGRY, "呵呵！麻雀之食贵于玉，薪贵于桂。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_PATCHOULI_EVALUATION_LACKMONEYNORMAL, "咳咳，我有点不舒服，先回去了…");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_PATCHOULI_EVALUATION_REPELL, "人而无礼，胡不遄死！");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_PATCHOULI_EVALUATION_SEENREPELL, "哼，朽木难雕。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_HUZIWARA_NO_MOKOU_EVALUATION_EXBAD, "又硬又硌牙，想必比你的肝还要难啃！");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_HUZIWARA_NO_MOKOU_EVALUATION_BAD, "还得多下点功夫。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_HUZIWARA_NO_MOKOU_EVALUATION_NORM, "我觉得你应该能做得更好。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_HUZIWARA_NO_MOKOU_EVALUATION_GOOD, "在我的漫长人生中，这样美味的料理也属罕见。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_HUZIWARA_NO_MOKOU_EVALUATION_EXGOOD, "活着…真是太美妙了！");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_HUZIWARA_NO_MOKOU_EVALUATION_LACKMONEYANGRY, "时代的进化还不及你的定价变化之快！");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_HUZIWARA_NO_MOKOU_EVALUATION_LACKMONEYNORMAL, "我过得向来清贫，就只有这么多钱了…");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_HUZIWARA_NO_MOKOU_EVALUATION_REPELL, "也罢。我已经习惯了。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_HUZIWARA_NO_MOKOU_EVALUATION_SEENREPELL, "己所不欲，勿施于人。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_HOURAISAN_KAGUYA_EVALUATION_EXBAD, "竟敢呈此等秽物来招待我！");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_HOURAISAN_KAGUYA_EVALUATION_BAD, "这样的东西不值得我出门一趟。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_HOURAISAN_KAGUYA_EVALUATION_NORM, "地上的食物一直是不错的。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_HOURAISAN_KAGUYA_EVALUATION_GOOD, "馔玉炊金，其味无穷。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_HOURAISAN_KAGUYA_EVALUATION_EXGOOD, "地上有这么美味的食物，谁还会想回到月亮上呢？");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_HOURAISAN_KAGUYA_EVALUATION_LACKMONEYANGRY, "地上都是这般坑蒙拐骗吗？！");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_HOURAISAN_KAGUYA_EVALUATION_LACKMONEYNORMAL, "唔，月亮和地上的物价可能不太一样。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_HOURAISAN_KAGUYA_EVALUATION_REPELL, "无礼的地上妖怪！");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_HOURAISAN_KAGUYA_EVALUATION_SEENREPELL, "突然感觉没什么兴致了。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_KAWASIRO_NITORI_EVALUATION_EXBAD, "这种程度是怎么让食堂风靡幻想乡的？这是广告欺诈吧！");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_KAWASIRO_NITORI_EVALUATION_BAD, "胡乱加工不是正数的加法，而是负数的加法。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_KAWASIRO_NITORI_EVALUATION_NORM, "食品的加工是非常深奥的，你得多储备一些知识才行。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_KAWASIRO_NITORI_EVALUATION_GOOD, "邀请你到山里开店真是明断啊！");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_KAWASIRO_NITORI_EVALUATION_EXGOOD, "我享受到的服务已经超过了交易本身，必须要给予答谢！");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_KAWASIRO_NITORI_EVALUATION_LACKMONEYANGRY, "这定价与其说是做生意，不如说是抢劫！");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_KAWASIRO_NITORI_EVALUATION_LACKMONEYNORMAL, "虽然我最近赚了些钱，也经不住这样的高度消费！");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_KAWASIRO_NITORI_EVALUATION_REPELL, "这样做生意是不会长久的。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_KAWASIRO_NITORI_EVALUATION_SEENREPELL, "老板娘，你还是不明白生意该怎么做。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_INUBASHIRI_MOMIZI_EVALUATION_EXBAD, "压榨劳动人员的钱包却只给吃这种东西吗？！");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_INUBASHIRI_MOMIZI_EVALUATION_BAD, "早知道就只喝酒了。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_INUBASHIRI_MOMIZI_EVALUATION_NORM, "就着酒吃也还行吧。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_INUBASHIRI_MOMIZI_EVALUATION_GOOD, "美味的食物能把一天的疲劳全部消除！");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_INUBASHIRI_MOMIZI_EVALUATION_EXGOOD, "不愧是风靡幻想乡的食堂，多谢款待！");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_INUBASHIRI_MOMIZI_EVALUATION_LACKMONEYANGRY, "这定价严重违反了山中的秩序！");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_INUBASHIRI_MOMIZI_EVALUATION_LACKMONEYNORMAL, "守卫的工资果然是太低了，连顿饭也差点吃不起…");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_INUBASHIRI_MOMIZI_EVALUATION_REPELL, "我们这种底层天狗就连吃饭也要被瞧不起吗？");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_INUBASHIRI_MOMIZI_EVALUATION_SEENREPELL, "这一点也不利于山中的团结！");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_KOCHIYA_SANAE_EVALUATION_EXBAD, "用这种东西招待信徒只会让信仰心下降！");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_KOCHIYA_SANAE_EVALUATION_BAD, "这种程度的东西是无法凝聚人心的。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_KOCHIYA_SANAE_EVALUATION_NORM, "想用这个来收集信仰的话，还需要再精进一些吧。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_KOCHIYA_SANAE_EVALUATION_GOOD, "美味的食物果然可以收集信仰！");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_KOCHIYA_SANAE_EVALUATION_EXGOOD, "就连我都忍不住想去信仰你了！");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_KOCHIYA_SANAE_EVALUATION_LACKMONEYANGRY, "再怎么非常识，这顿饭也太贵了吧！");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_KOCHIYA_SANAE_EVALUATION_LACKMONEYNORMAL, "幻想乡的消费水平比外面世界还高啊…");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_KOCHIYA_SANAE_EVALUATION_REPELL, "欸？欸欸欸？！");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_KOCHIYA_SANAE_EVALUATION_SEENREPELL, "这种待客态度是无法凝聚信仰的。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_ALICE_EVALUATION_EXBAD, "明明用魔力就可以不用进食，为什么还要受这个罪！");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_ALICE_EVALUATION_BAD, "还不如人偶做的饭…");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_ALICE_EVALUATION_NORM, "还过得去吧。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_ALICE_EVALUATION_GOOD, "就是因为有这种美食，我才一直保持人类的进食习惯。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_ALICE_EVALUATION_EXGOOD, "美味得仿佛是只存在于童话世界里的幻想！");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_ALICE_EVALUATION_LACKMONEYANGRY, "这是什么新型骗局吗？绝对是敲诈吧？！");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_ALICE_EVALUATION_LACKMONEYNORMAL, "最近买材料花了不少钱，没计划好预算…");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_ALICE_EVALUATION_REPELL, "我本来也没打算吃。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_ALICE_EVALUATION_SEENREPELL, "吃饭就是图个气氛，气氛不到位食物也会变难吃。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_YATADERA_NARUMI_EVALUATION_EXBAD, "蹩脚的人和难吃的菜一样，看到就想吐。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_YATADERA_NARUMI_EVALUATION_BAD, "这不会是用臭水沟炒的菜吧？");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_YATADERA_NARUMI_EVALUATION_NORM, "这菜适合生存，不适合生活。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_YATADERA_NARUMI_EVALUATION_GOOD, "难得出一趟门，总算没有白跑一趟！");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_YATADERA_NARUMI_EVALUATION_EXGOOD, "好满足！五蕴皆空不如五脏俱饱。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_YATADERA_NARUMI_EVALUATION_LACKMONEYANGRY, "跟地藏要钱也太难看了，这一顿就当做是你的布施吧。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_YATADERA_NARUMI_EVALUATION_LACKMONEYNORMAL, "不够的钱等我下次化缘回来再补上吧。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_YATADERA_NARUMI_EVALUATION_REPELL, "我不是来化缘的，我带了钱！");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_YATADERA_NARUMI_EVALUATION_SEENREPELL, "是法平等无有高下。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_KURODANI_YAMAME_EVALUATION_EXBAD, "还不如蛛网里的猎物好吃。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_KURODANI_YAMAME_EVALUATION_BAD, "我不是想打击你，但是味道确实不太好…");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_KURODANI_YAMAME_EVALUATION_NORM, "传说中的夜雀食堂应该不止这种程度…");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_KURODANI_YAMAME_EVALUATION_GOOD, "氛围和食物都是满分哦。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_KURODANI_YAMAME_EVALUATION_EXGOOD, "能在黑暗的洞窟吃到这么美味的食物真是太棒了！");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_KURODANI_YAMAME_EVALUATION_LACKMONEYANGRY, "这家店比黑蜘蛛还要黑！");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_KURODANI_YAMAME_EVALUATION_LACKMONEYNORMAL, "我还不太习惯地上的消费水平…");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_KURODANI_YAMAME_EVALUATION_REPELL, "就算我们什么都没做也会遭到厌恶吗？");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_KURODANI_YAMAME_EVALUATION_SEENREPELL, "瞧不起被厌恶者就别在这里开店了。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_MIZUHASHI_PARSEE_EVALUATION_EXBAD, "这么稀巴烂的厨艺是怎么开店的？");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_MIZUHASHI_PARSEE_EVALUATION_BAD, "隔壁桌的菜绝对比我的好吃！");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_MIZUHASHI_PARSEE_EVALUATION_NORM, "总觉得别人碗里的更香…");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_MIZUHASHI_PARSEE_EVALUATION_GOOD, "这般治愈人心的美味，真是让人嫉妒啊。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_MIZUHASHI_PARSEE_EVALUATION_EXGOOD, "美味得让我一时间甚至连嫉妒都忘了。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_MIZUHASHI_PARSEE_EVALUATION_LACKMONEYANGRY, "我要把这个黑店钉在树上！");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_MIZUHASHI_PARSEE_EVALUATION_LACKMONEYNORMAL, "大家的口袋就那么富裕吗？");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_MIZUHASHI_PARSEE_EVALUATION_REPELL, "居然赶走我而不去赶走别的客人，好嫉妒啊。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_MIZUHASHI_PARSEE_EVALUATION_SEENREPELL, "居然赶走别的客人而不来赶走我，好嫉妒啊。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_HOSHIGUMA_YUGI_EVALUATION_EXBAD, "这样的料理简直是糟蹋我的酒！");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_HOSHIGUMA_YUGI_EVALUATION_BAD, "这菜的味道有点配不上我的酒啊。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_HOSHIGUMA_YUGI_EVALUATION_NORM, "有好酒相送，菜随便吃吃就行了。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_HOSHIGUMA_YUGI_EVALUATION_GOOD, "温泉后的饱餐时光足以治愈人心。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_HOSHIGUMA_YUGI_EVALUATION_EXGOOD, "好马配好鞍，好酒配好菜！");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_HOSHIGUMA_YUGI_EVALUATION_LACKMONEYANGRY, "敢宰鬼的黑店也是第一次见。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_HOSHIGUMA_YUGI_EVALUATION_LACKMONEYNORMAL, "钻钱眼子可不太好啊。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_HOSHIGUMA_YUGI_EVALUATION_REPELL, "胆子真不小啊。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_HOSHIGUMA_YUGI_EVALUATION_SEENREPELL, "无趣之举。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_KOMEIJI_SATORI_EVALUATION_EXBAD, "这一顿比工作积攒的精神压力更大。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_KOMEIJI_SATORI_EVALUATION_BAD, "难得过来一趟，有些失望。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_KOMEIJI_SATORI_EVALUATION_NORM, "吃食只是为了支撑自己不会倒下。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_KOMEIJI_SATORI_EVALUATION_GOOD, "阿燐的眼光向来不错。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_KOMEIJI_SATORI_EVALUATION_EXGOOD, "真可谓层层叠叠，色香味齐全啊！");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_KOMEIJI_SATORI_EVALUATION_LACKMONEYANGRY, "我很清楚成本是多少。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_KOMEIJI_SATORI_EVALUATION_LACKMONEYNORMAL, "抱歉，把零花钱分给宠物后没剩多少了…");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_KOMEIJI_SATORI_EVALUATION_REPELL, "不是说要为我准备健康料理吗？");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_KOMEIJI_SATORI_EVALUATION_SEENREPELL, "一个人的礼貌是一面照出它肖像的镜子。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_KAENBYOU_RIN_EVALUATION_EXBAD, "还不如吃腐肉呢，呸呸！");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_KAENBYOU_RIN_EVALUATION_BAD, "是我看走眼了吗…");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_KAENBYOU_RIN_EVALUATION_NORM, "这种程度真的能让觉大人提起食欲吗…");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_KAENBYOU_RIN_EVALUATION_GOOD, "这样的好东西真是吃一次少一次啊！");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_KAENBYOU_RIN_EVALUATION_EXGOOD, "美味到即便是厌食症也会化身饕餮的程度！");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_KAENBYOU_RIN_EVALUATION_LACKMONEYANGRY, "你都强买强卖，就别怪咱吃霸王餐了！");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_KAENBYOU_RIN_EVALUATION_LACKMONEYNORMAL, "最近手头有点紧…");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_KAENBYOU_RIN_EVALUATION_REPELL, "大姐，你太伤咱的心了。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_KAENBYOU_RIN_EVALUATION_SEENREPELL, "骄傲自满可是会招来损失的。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_REIUJI_UTSUH_EVALUATION_EXBAD, "还不如让我扔到灼热地狱烤一烤。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_REIUJI_UTSUH_EVALUATION_BAD, "火热程度太低了。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_REIUJI_UTSUH_EVALUATION_NORM, "多谢老板娘招待。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_REIUJI_UTSUH_EVALUATION_GOOD, "这一顿很火热啊！");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_REIUJI_UTSUH_EVALUATION_EXGOOD, "我得到了比灼热地狱还要热烈的感受！");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_REIUJI_UTSUH_EVALUATION_LACKMONEYANGRY, "假装忘记带钱吧…反正我是鸟脑袋嘛。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_REIUJI_UTSUH_EVALUATION_LACKMONEYNORMAL, "鸟脑袋只能装这么多钱啦。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_REIUJI_UTSUH_EVALUATION_REPELL, "当心我把你烤了哦！");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_REIUJI_UTSUH_EVALUATION_SEENREPELL, "干嘛要把人赶走啊…");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_TATARA_KOGASA_EVALUATION_EXBAD, "难吃到吓我一跳。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_TATARA_KOGASA_EVALUATION_BAD, "无法带来一点惊喜。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_TATARA_KOGASA_EVALUATION_NORM, "还不错，就是少了点惊喜。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_TATARA_KOGASA_EVALUATION_GOOD, "好吃到吓我一跳！");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_TATARA_KOGASA_EVALUATION_EXGOOD, "惊——！好吃到吓人！");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_TATARA_KOGASA_EVALUATION_LACKMONEYANGRY, "我没有钱哦！吓到了吧——？！");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_TATARA_KOGASA_EVALUATION_LACKMONEYNORMAL, "这些钱都是小孩子们给我的…");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_TATARA_KOGASA_EVALUATION_REPELL, "哭…我真是太没用了…");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_TATARA_KOGASA_EVALUATION_SEENREPELL, "我能理解这种被嫌弃的感觉。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_HOUJUU_NUE_EVALUATION_EXBAD, "这种东西不值得我展露真相。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_HOUJUU_NUE_EVALUATION_BAD, "马马虎虎吧。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_HOUJUU_NUE_EVALUATION_NORM, "看来你还没有挖掘出美食的真相。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_HOUJUU_NUE_EVALUATION_GOOD, "这就是美食的真相吗？！");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_HOUJUU_NUE_EVALUATION_EXGOOD, "展露真相就是为了品尝这样的美味！");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_HOUJUU_NUE_EVALUATION_LACKMONEYANGRY, "平安时代的强盗都没有你猖獗！");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_HOUJUU_NUE_EVALUATION_LACKMONEYNORMAL, "自平安时代攒下来的钱已经快见底了…");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_HOUJUU_NUE_EVALUATION_REPELL, "反正我也独来独往惯了。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_HOUJUU_NUE_EVALUATION_SEENREPELL, "我就是为了感受一下热闹才来的。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_MONONOBE_NO_FUTO_EVALUATION_EXBAD, "火候把握不好的话就让吾来助汝烧一把吧！");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_MONONOBE_NO_FUTO_EVALUATION_BAD, "只能算是无功无过吧。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_MONONOBE_NO_FUTO_EVALUATION_NORM, "多谢款待。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_MONONOBE_NO_FUTO_EVALUATION_GOOD, "口腹之欲都要被勾起来了！");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_MONONOBE_NO_FUTO_EVALUATION_EXGOOD, "国之宴席也莫过于此！");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_MONONOBE_NO_FUTO_EVALUATION_LACKMONEYANGRY, "贵店的物价恐怕连国库都能掏空。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_MONONOBE_NO_FUTO_EVALUATION_LACKMONEYNORMAL, "差点就在众目睽睽下吃霸王餐了！");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_MONONOBE_NO_FUTO_EVALUATION_REPELL, "小人无节，弃本逐末！");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_MONONOBE_NO_FUTO_EVALUATION_SEENREPELL, "礼者，人道之极也。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_KAKU_SEIGA_EVALUATION_EXBAD, "要不是走火入魔怎么可能会来吃这种东西！");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_KAKU_SEIGA_EVALUATION_BAD, "是我看走眼了吗？");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_KAKU_SEIGA_EVALUATION_NORM, "还要再加把劲哦。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_KAKU_SEIGA_EVALUATION_GOOD, "美食当前还嚷嚷着辟谷就太不解风情了。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_KAKU_SEIGA_EVALUATION_EXGOOD, "努力活着才能吃到这样的美味呢。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_KAKU_SEIGA_EVALUATION_LACKMONEYANGRY, "我像是身上有很重铜臭味的人吗？");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_KAKU_SEIGA_EVALUATION_LACKMONEYNORMAL, "小麻雀也挺会宰客的呢。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_KAKU_SEIGA_EVALUATION_REPELL, "你可要想清楚哟…");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_KAKU_SEIGA_EVALUATION_SEENREPELL, "是我看走眼了吗？");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_SOGA_NO_TOZIKO_EVALUATION_EXBAD, "拿糟糠来糊弄我吗？！");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_SOGA_NO_TOZIKO_EVALUATION_BAD, "还不如我自己瞎折腾弄得好吃。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_SOGA_NO_TOZIKO_EVALUATION_NORM, "聊胜于无。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_SOGA_NO_TOZIKO_EVALUATION_GOOD, "这般美味比仇恨更让人惦记。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_SOGA_NO_TOZIKO_EVALUATION_EXGOOD, "有此美食作伴就算千年孤独也不在话下！");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_SOGA_NO_TOZIKO_EVALUATION_LACKMONEYANGRY, "什么黑店！太子大人给的家用竟然堪堪支付一顿饭钱？！");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_SOGA_NO_TOZIKO_EVALUATION_LACKMONEYNORMAL, "我的钱和我的怨恨都消散得差不多了。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_SOGA_NO_TOZIKO_EVALUATION_REPELL, "你就不怕我怨恨你吗？");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_SOGA_NO_TOZIKO_EVALUATION_SEENREPELL, "麻雀老板的心眼也太小了…");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_AYA_EVALUATION_EXBAD, "烂到这种程度也足够上头条了！");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_AYA_EVALUATION_BAD, "喂喂，做成这样没关系吗…");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_AYA_EVALUATION_NORM, "作为新闻素材的话感觉无波无澜呢。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_AYA_EVALUATION_GOOD, "这个菜有成为爆款的潜力呢。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_AYA_EVALUATION_EXGOOD, "这个菜绝对能成为头条！");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_AYA_EVALUATION_LACKMONEYANGRY, "感谢我没有报导这种漫天要价的行为吧！");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_AYA_EVALUATION_LACKMONEYNORMAL, "自费做了太多报纸，差点连夜宵也吃不起。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_AYA_EVALUATION_REPELL, "喂喂？！不怕我报导出去吗？");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_AYA_EVALUATION_SEENREPELL, "把客人都赶走我还怎么打探情报？");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_KAZAMI_YUKA_EVALUATION_EXBAD, "呵——！我接受你的挑衅——！");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_KAZAMI_YUKA_EVALUATION_BAD, "如此相待，最随和的花儿也不会愿盛开。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_KAZAMI_YUKA_EVALUATION_NORM, "这样是无法获得花儿的青睐的。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_KAZAMI_YUKA_EVALUATION_GOOD, "心情就像缓缓打开的花蕾一样呢。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_KAZAMI_YUKA_EVALUATION_EXGOOD, "你拥有的是让心花皆绽放的能力！");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_KAZAMI_YUKA_EVALUATION_LACKMONEYANGRY, "我没有留下一记魔炮你就该感恩戴德了。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_KAZAMI_YUKA_EVALUATION_LACKMONEYNORMAL, "漫天要价是无法延续生意的。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_KIJIN_SEIJIA_EVALUATION_EXBAD, "我要颠倒正是这种将糟糠奉为美食的世界！");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_KIJIN_SEIJIA_EVALUATION_BAD, "软弱的味道。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_KIJIN_SEIJIA_EVALUATION_NORM, "“一般”颠倒过来还是“一般”。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_KIJIN_SEIJIA_EVALUATION_GOOD, "力量源源不绝！这就是美食的威力吗？");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_KIJIN_SEIJIA_EVALUATION_EXGOOD, "力量满满！此刻的我无论什么都能将其颠倒！");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_KIJIN_SEIJIA_EVALUATION_LACKMONEYANGRY, "小费就当做是你孝敬的保护费了！");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_KIJIN_SEIJIA_EVALUATION_LACKMONEYNORMAL, "靠开食堂来敛财真是绝妙的主意啊。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_KIJIN_SEIJIA_EVALUATION_REPELL, "你以为这是谁的地盘？！");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_KIJIN_SEIJIA_EVALUATION_SEENREPELL, "弱小妖怪什么时候才可以不被欺压？！");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_SUKUNA_SHINMYOUMARU_EVALUATION_EXBAD, "这道菜难吃得能侵蚀内心的勇气！");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_SUKUNA_SHINMYOUMARU_EVALUATION_BAD, "就算是作为路上的充饥之物都不够格呀。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_SUKUNA_SHINMYOUMARU_EVALUATION_NORM, "勉强能在路上作充饥之物吧。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_SUKUNA_SHINMYOUMARU_EVALUATION_GOOD, "这道菜给我的体验就像读勇者故事一样！");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_SUKUNA_SHINMYOUMARU_EVALUATION_EXGOOD, "这道菜让我充满了踏上旅程的勇气！");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_SUKUNA_SHINMYOUMARU_EVALUATION_LACKMONEYANGRY, "虽然是贵族，却是破败了的…");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_SUKUNA_SHINMYOUMARU_EVALUATION_LACKMONEYNORMAL, "小人族闭关太久，外面的物价已经这么高了吗？");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_SUKUNA_SHINMYOUMARU_EVALUATION_REPELL, "你竟然驱赶未来的勇者大人？！");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_SUKUNA_SHINMYOUMARU_EVALUATION_SEENREPELL, "身为勇者，我不能容许这种事发生。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_IMAIZUMI_KAGEROU_EVALUATION_EXBAD, "这样的料理感受不到任何善意。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_IMAIZUMI_KAGEROU_EVALUATION_BAD, "看来老板娘的厨艺也不是很稳定嘛。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_IMAIZUMI_KAGEROU_EVALUATION_NORM, "在我心里你应该能做得更好。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_IMAIZUMI_KAGEROU_EVALUATION_GOOD, "我能品尝出料理里的善意哦！");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_IMAIZUMI_KAGEROU_EVALUATION_EXGOOD, "老板娘做的菜才是真正的圆满！");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_IMAIZUMI_KAGEROU_EVALUATION_LACKMONEYANGRY, "掉下来的毛如果能变成钱就好了。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_IMAIZUMI_KAGEROU_EVALUATION_LACKMONEYNORMAL, "宵夜还真是一不小心就会吃过量…");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_IMAIZUMI_KAGEROU_EVALUATION_REPELL, "草根妖怪也是有尊严的！");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_IMAIZUMI_KAGEROU_EVALUATION_SEENREPELL, "为什么不能和平相处呢？");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_REISEN_EVALUATION_EXBAD, "真是糟糕的一天…");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_REISEN_EVALUATION_BAD, "今天也是就这样过去了吗…");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_REISEN_EVALUATION_NORM, "谢谢，我恢复力气了。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_REISEN_EVALUATION_GOOD, "谢谢老板娘治愈了我这一天的疲惫！");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_REISEN_EVALUATION_EXGOOD, "这就是我的光！！！");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_REISEN_EVALUATION_LACKMONEYANGRY, "就算把我卖了我也付不起呀！");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_REISEN_EVALUATION_LACKMONEYNORMAL, "打工的钱快花完了…");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_REISEN_EVALUATION_REPELL, "这就是属于我的命运吧。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_REISEN_EVALUATION_SEENREPELL, "这个世道到处都是不公的对待。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_WATATSUKI_TOYOHIME_EVALUATION_EXBAD, "要不是为了不浪费食物…");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_WATATSUKI_TOYOHIME_EVALUATION_BAD, "还是去吃桃子好了。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_WATATSUKI_TOYOHIME_EVALUATION_NORM, "不浪费食物是我的底线。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_WATATSUKI_TOYOHIME_EVALUATION_GOOD, "好吃！还想吃更多更多。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_WATATSUKI_TOYOHIME_EVALUATION_EXGOOD, "好吃到我想在店里住下来了哟！");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_WATATSUKI_TOYOHIME_EVALUATION_LACKMONEYANGRY, "我不是已经把桃子留下了吗？");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_WATATSUKI_TOYOHIME_EVALUATION_LACKMONEYNORMAL, "太计较钱财会失了风度哟。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_WATATSUKI_TOYOHIME_EVALUATION_REPELL, "小麻雀太坏了。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_WATATSUKI_TOYOHIME_EVALUATION_SEENREPELL, "不可以欺负人哟，小麻雀。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_REMILIA_EVALUATION_EXBAD, "做出这样的东西，果然应该用神枪刺穿你吧？");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_REMILIA_EVALUATION_BAD, "我一开始就没有对你抱有任何期待。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_REMILIA_EVALUATION_NORM, "和我们家的女仆比起来还差得远呢。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_REMILIA_EVALUATION_GOOD, "这次真的不错呢，总算值得一点期待了。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_REMILIA_EVALUATION_EXGOOD, "只有这样的美味才能够配得上吸血鬼的尊贵身份！");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_REMILIA_EVALUATION_LACKMONEYANGRY, "咲夜只给我准备了这么多钱！");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_REMILIA_EVALUATION_LACKMONEYNORMAL, "我还不习惯自己带钱出门…");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_REMILIA_EVALUATION_REPELL, "哼，看来我是太纵容你了啊。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_REMILIA_EVALUATION_SEENREPELL, "还是回家吃咲夜做的吧。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_YOUMU_EVALUATION_EXBAD, "就算是恶作剧也太过分了！");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_YOUMU_EVALUATION_BAD, "诶——不应该是这个味道吧？");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_YOUMU_EVALUATION_NORM, "我应该也可以做到这种程度吧？");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_YOUMU_EVALUATION_GOOD, "呜呜，我要是也能做出这个味道就好了！");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_YOUMU_EVALUATION_EXGOOD, "啊——这就是我无论如何都无法实现出来的味道吗！");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_YOUMU_EVALUATION_LACKMONEYANGRY, "爷爷说过，被敲诈的时候要掉头就走！");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_YOUMU_EVALUATION_LACKMONEYNORMAL, "对不起！我只能付得起这么多…");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_YOUMU_EVALUATION_REPELL, "为、为什么啊…");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_YOUMU_EVALUATION_SEENREPELL, "被赶走的人也太可怜了…");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_YUYUKO_EVALUATION_EXBAD, "这东西看起来还不如老板娘好吃。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_YUYUKO_EVALUATION_BAD, "唔，这不是我印象中的味道呢。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_YUYUKO_EVALUATION_NORM, "和我印象中的味道还差了一点。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_YUYUKO_EVALUATION_GOOD, "果然偶尔要出来吃一下外面的美食呢。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_YUYUKO_EVALUATION_EXGOOD, "这就是那个让我念念不忘的味道！");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_YUYUKO_EVALUATION_LACKMONEYANGRY, "哎呀，忘记带钱了呢。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_YUYUKO_EVALUATION_LACKMONEYNORMAL, "要结账了吗？我看看，这么多就够了吧。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_YUYUKO_EVALUATION_REPELL, "我不会把你的店吃垮的啦。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_YUYUKO_EVALUATION_SEENREPELL, "诶？小麻雀真没劲…");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_KOMEIJI_KOISHI_EVALUATION_EXBAD, "把小麻雀装饰在家里一定很好看。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_KOMEIJI_KOISHI_EVALUATION_BAD, "明明肚子空空却不想吃呢。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_KOMEIJI_KOISHI_EVALUATION_NORM, "吃饱啦。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_KOMEIJI_KOISHI_EVALUATION_GOOD, "姐姐说肚子吃得鼓鼓的就表示吃饱了！");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_KOMEIJI_KOISHI_EVALUATION_EXGOOD, "这么棒的老板娘装饰在家里一定很好看！");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_KOMEIJI_KOISHI_EVALUATION_LACKMONEYANGRY, "为什么吃饭要付钱啊？");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_KOMEIJI_KOISHI_EVALUATION_LACKMONEYNORMAL, "我就随便带了点钱，你都拿去好了。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_KOMEIJI_KOISHI_EVALUATION_REPELL, "怎么啦怎么啦？是没看到我吗？");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_KOMEIJI_KOISHI_EVALUATION_SEENREPELL, "恋恋喜欢热闹啦！");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_HUTATSUIWA_MAMIZOU_EVALUATION_EXBAD, "竟敢戏弄老朽？让你见识什么是真正的恶作剧！");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_HUTATSUIWA_MAMIZOU_EVALUATION_BAD, "有点让老朽失望啊。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_HUTATSUIWA_MAMIZOU_EVALUATION_NORM, "只有这种程度吗？");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_HUTATSUIWA_MAMIZOU_EVALUATION_GOOD, "嚯嚯嚯——不愧是老朽寄望的妖兽。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_HUTATSUIWA_MAMIZOU_EVALUATION_EXGOOD, "这是值得狸猫来报恩的一顿款待呐！");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_HUTATSUIWA_MAMIZOU_EVALUATION_LACKMONEYANGRY, "老朽没有用叶子结账就已经很讲义气了！！！");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_HUTATSUIWA_MAMIZOU_EVALUATION_LACKMONEYNORMAL, "叶子老朽倒是有很多，钱就只有这些了。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_HUTATSUIWA_MAMIZOU_EVALUATION_REPELL, "现在的妖怪怎么连敬老的美德都忘了。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_HUTATSUIWA_MAMIZOU_EVALUATION_SEENREPELL, "还是回去和狸猫们喝酒更有趣。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_YUKARI_EVALUATION_EXBAD, "这种东西连呆在隙间的资格都没有。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_YUKARI_EVALUATION_BAD, "这种东西应该扔到隙间去。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_YUKARI_EVALUATION_NORM, "似乎和我想象的有所出入。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_YUKARI_EVALUATION_GOOD, "不错，难怪享有盛名。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_YUKARI_EVALUATION_EXGOOD, "做得好！你值得拥有更多！");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_YUKARI_EVALUATION_LACKMONEYANGRY, "我不能允许幻想乡存在这种漫天要价的行为！");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_YUKARI_EVALUATION_LACKMONEYNORMAL, "小麻雀的生意经做得稍微有点过头了哟。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_YUKARI_EVALUATION_REPELL, "要到隙间来谈谈吗？");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_YUKARI_EVALUATION_SEENREPELL, "我期待的幻想乡并非这种模样。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_FLANDRE_EVALUATION_EXBAD, "呸！难吃！");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_FLANDRE_EVALUATION_BAD, "和女仆长送来的甜点一样无聊…");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_FLANDRE_EVALUATION_NORM, "还有什么更有趣的吗？");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_FLANDRE_EVALUATION_GOOD, "嗯嗯嗯！好吃好吃。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_FLANDRE_EVALUATION_EXGOOD, "哇！料理和星星一样会闪光耶！");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_FLANDRE_EVALUATION_LACKMONEYANGRY, "哇啊——我再也不要来了！");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_FLANDRE_EVALUATION_LACKMONEYNORMAL, "诶？原来这些不够吗？");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_FLANDRE_EVALUATION_REPELL, "游戏时间结束了吗？");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_FLANDRE_EVALUATION_SEENREPELL, "不玩就不玩，那么大脾气做什么嘛。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_ERIN_EVALUATION_EXBAD, "…不堪入目。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_ERIN_EVALUATION_BAD, "只比庸俗之人的作品好一点。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_ERIN_EVALUATION_NORM, "甚合心意，多谢款待。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_ERIN_EVALUATION_GOOD, "优秀的料理可以治疗心病，受教了。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_ERIN_EVALUATION_EXGOOD, "如此美味，竟如同神话一般的梦幻。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_ERIN_EVALUATION_LACKMONEYANGRY, "你还真是贪心不足啊…");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_ERIN_EVALUATION_LACKMONEYNORMAL, "抱歉，下次我会补上。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_ERIN_EVALUATION_REPELL, "既然需要我离开，那我离开就好。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_ERIN_EVALUATION_SEENREPELL, "无妨。这只是永恒的生命中的一点波澜而已。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_RAN_EVALUATION_EXBAD, "紫大人做的饭都比你做的好吃！");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_RAN_EVALUATION_BAD, "和橙学着做饭的水平差不多呢。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_RAN_EVALUATION_NORM, "嘛，还不错还不错。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_RAN_EVALUATION_GOOD, "怪不得紫大人会把希望寄托在你身上。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_RAN_EVALUATION_EXGOOD, "哪怕回到畜生界，也找不到这样合我口味的美食啊。");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_RAN_EVALUATION_LACKMONEYANGRY, "算天算地，算不到老板娘你的黑心！");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_RAN_EVALUATION_LACKMONEYNORMAL, "给橙买木天蓼花的有点多了…");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_RAN_EVALUATION_REPELL, "我连自己出来吃点东西都不行吗？");
        translationBuilder.add(CustomerEvaluationConsts.CUSTOMER_RAN_EVALUATION_SEENREPELL, "紫大人为什么要把希望寄托在这里呢…");

    }

    public void generateOrderChatTranslations(HolderLookup.Provider wrapperLookup, TranslationBuilder translationBuilder) {
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_WRIGGLE_NIGHTBUG_0, "如果点的菜带甜味就好了~");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_WRIGGLE_NIGHTBUG_1, "生肉总比熟肉好吃吧！你难道是牛排吃十分熟的那种人?");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_WRIGGLE_NIGHTBUG_2, "作为虫子奇奇怪怪的菜我来者不拒哦~");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_WRIGGLE_NIGHTBUG_3, "其实虫子也是吃肉的！");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_RUMIA_0, "肉肉肉~要吃肉~");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_RUMIA_1, "最新鲜的吃法就是生吃？");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_RUMIA_2, "人类觉得奇怪的料理说不定正适合我呢~");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_RUMIA_3, "吃不饱可不行哦~");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_RUMIA_4, "你有没有什么拿手好菜呀？");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_CHEN_0, "我还在长身体，不吃肉可不行！");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_CHEN_1, "猫当然是要吃鱼啦~");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_CHEN_2, "一点油水都没有的东西能有啥味道");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_CHEN_3, "甜的我也喜欢~");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_CHEN_4, "那种把肉串起来烧一烧的料理！");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_KAMISHIRASAWA_KEINE_0, "不知道中华风的菜肴会承载着怎样的历史呢。");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_KAMISHIRASAWA_KEINE_1, "不知道和风的菜肴会承载着怎样的历史呢。");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_KAMISHIRASAWA_KEINE_2, "偶尔吃点素菜，感觉身体会很舒服。");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_KAMISHIRASAWA_KEINE_3, "最近吃得比较清淡……");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_KAMISHIRASAWA_KEINE_4, "随便吃些家常菜就可以了。");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_KAMISHIRASAWA_KEINE_5, "历史和文化是相辅相成的。");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_REIMU_0, "糖在以前是奢侈品呢，现在随便就能吃到真是太好了~");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_REIMU_1, "吃不饱的话就太不划算了…");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_REIMU_2, "有什么配得上我的称号的菜吗？");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_REIMU_3, "有没有什么不花大价钱的高级料理啊……");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_SUIKA_0, "大口吃肉大口喝酒才叫爽快！");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_SUIKA_1, "说到鬼，就是和风了吧~");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_SUIKA_2, "喝酒之余来点佐酒的小菜也不错~");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_SUIKA_3, "鬼族的力量可都是很夸张的哦");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_SUIKA_4, "我可以分散很多个小的自己，有什么合适的菜吗~");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_TENSHI_0, "荤腥的东西离我远点！");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_TENSHI_1, "只有你们这种下等人才喜欢口味重的东西！");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_TENSHI_2, "不喜欢甜味的人是不存在的~");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_TENSHI_3, "卖相不好的低贱品就别给本天人端过来了！");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_TENSHI_4, "传说级别的菜品，这种小破店是不会有的吧？");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_TENSHI_5, "新鲜的水果，这种小店弄得到吗？");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_MARISA_0, "蘑菇是怎么吃也吃不腻的！");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_MARISA_1, "越是古老的东西越有价值~");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_MARISA_2, "我是和食主义者！");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_MARISA_3, "高热量的油炸食品能让人更快乐~");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_MARISA_4, "料理就和魔法一样，发光发热才叫好！");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_MEIRIN_0, "就算身处异乡也不能忘了祖国的味道。");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_MEIRIN_1, "有没有像漫画一样吃了就能让人涌现出力量的东西呢？");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_MEIRIN_2, "肉是最实在的！");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_MEIRIN_3, "吃饱了才有力气干活啊。");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_CIRNO_0, "冰精当然要吃冰的啦！");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_CIRNO_1, "甜甜美美的东西吃了会让心情变好~");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_CIRNO_2, "丑拒！");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_CIRNO_3, "真正的勇士，敢于尝试千奇百怪的东西！");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_PATCHOULI_0, "饮食文化的差异，你这种鸟脑袋能够理解吗？");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_PATCHOULI_1, "甜美是一种能沁入心田的滋味。");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_PATCHOULI_2, "品相好看的料理，仅在书里出现也能让人垂涎。");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_PATCHOULI_3, "能将人带入宛如真实的梦幻中的料理，真想见识一下。");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_PATCHOULI_4, "若想使用高级的魔法，就需要拥有很高的熟练度。料理也是如此。");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_HUZIWARA_NO_MOKOU_0, "飘荡在妖怪山上的无尽之烟，即便没有火焰，也如梦似幻般灼热。");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_HUZIWARA_NO_MOKOU_1, "涅槃重生的不死鸟啊，复仇之火永扑不尽、永浇不灭！");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_HUZIWARA_NO_MOKOU_2, "辣味能够让我感受到活着的刺激。");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_HUZIWARA_NO_MOKOU_3, "果子既能解渴又能饱腹，是上天恩赐的食物。");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_HUZIWARA_NO_MOKOU_4, "我其实也挺擅长烤肉的。");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_HOURAISAN_KAGUYA_0, "地上有个叫大和抚子的和风美人，我对她挺感兴趣的。");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_HOURAISAN_KAGUYA_1, "虽然地上的历史对我而言转瞬即逝，但也是不可磨灭的过去。");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_HOURAISAN_KAGUYA_2, "地上流传的大多传说，其实都是月人创造和散播的哦。");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_HOURAISAN_KAGUYA_3, "这片污秽却自由的土地，总让我感到不可思议。");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_KAWASIRO_NITORI_0, "人类绝对无法理解我们河童的口味……说不定有少部分人可以。");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_KAWASIRO_NITORI_1, "河童是妖怪中的驭水高手，食物也几乎都从水里获得。");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_KAWASIRO_NITORI_2, "招待我们河童，直接把最上品的菜端出来就好。");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_KAWASIRO_NITORI_3, "只有酒没有菜，囫囵地喝也没什么意思——");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_KAWASIRO_NITORI_4, "得多吃点咸的才能补充体力啊。");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_KAWASIRO_NITORI_5, "只有口碑最好的菜才能成为招牌菜。");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_INUBASHIRI_MOMIZI_0, "就算变成妖怪，狼爱吃肉的本性也不会改变。");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_INUBASHIRI_MOMIZI_1, "俗话说，靠山吃山，靠海吃海。");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_INUBASHIRI_MOMIZI_2, "清淡的食物实在是吃不下啊，不健康的饮食对我来说刚刚好。");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_INUBASHIRI_MOMIZI_3, "酒才是席上的主角啊，菜肴什么的要是不能给酒增色，那还不如不吃。");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_KOCHIYA_SANAE_0, "说到日本的传统，当然是大和风情啦！");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_KOCHIYA_SANAE_1, "如果你从来没有尝过糖果的美味，就不会惦记它的味道~");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_KOCHIYA_SANAE_2, "手机在幻想乡也不是完全无法使用哦~");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_KOCHIYA_SANAE_3, "那两位大人在的地方，就是我的家。");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_KOCHIYA_SANAE_4, "女孩子是最不能抵抗梦幻般的东西了。");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_ALICE_0, "就算知道魔法修行是孤独的，偶尔也还是会怀念家的味道。");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_ALICE_1, "我真是受不了那些低级趣味。");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_ALICE_2, "魔法使当然是西式的。");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_ALICE_3, "我偶尔会梦到自己在巧克力和糖果做的房子睡觉。");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_ALICE_4, "食之道与魔法之道有着一样悠久的底蕴。");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_YATADERA_NARUMI_0, "好歹也算是地藏，偶尔吃点清淡的吧。");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_YATADERA_NARUMI_1, "山里的走地兽可不在我的救助范围。");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_YATADERA_NARUMI_2, "饮食文化的底蕴也未必比地藏的经书浅显呢。");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_YATADERA_NARUMI_3, "我想尝尽不同地方的美食，但实在懒得走。");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_YATADERA_NARUMI_4, "在我还是石像的时候，和风是这片土地的唯一。");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_KURODANI_YAMAME_0, "生活充满甜蜜和喜悦。");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_KURODANI_YAMAME_1, "无论什么食物当然都是新鲜的最好啦！");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_KURODANI_YAMAME_2, "一般人会觉得蜘蛛的口味很奇怪吗？");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_KURODANI_YAMAME_3, "把好看的食物拍下来，回去和蜘蛛们分享吧~");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_KURODANI_YAMAME_4, "煮得太熟的食物总是吃不太习惯。");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_MIZUHASHI_PARSEE_0, "妒意的味道是酸的。");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_MIZUHASHI_PARSEE_1, "眼泪的味道是咸的。");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_MIZUHASHI_PARSEE_2, "愤怒的味道是辣的。");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_MIZUHASHI_PARSEE_3, "我不能忍受其他客人的食物比我的新鲜！");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_MIZUHASHI_PARSEE_4, "水果能让惆怅的情绪变的舒缓。");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_MIZUHASHI_PARSEE_5, "不给我吃肉的话就诅咒你。");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_HOSHIGUMA_YUGI_0, "直接把佐酒的菜端上来吧！");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_HOSHIGUMA_YUGI_1, "试试小麻雀家的招牌菜吧~");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_HOSHIGUMA_YUGI_2, "如今的地上是怎么流传鬼的传说？");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_HOSHIGUMA_YUGI_3, "不觉得和风和这个街道很配么~");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_HOSHIGUMA_YUGI_4, "没有力量的鬼只会被踩在脚下。");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_HOSHIGUMA_YUGI_5, "让我的血液沸腾起来吧！");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_KOMEIJI_SATORI_0, "家常美味，也是人生百味。");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_KOMEIJI_SATORI_1, "吃太饱不利于思考。");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_KOMEIJI_SATORI_2, "生活需要加点甜。");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_KOMEIJI_SATORI_3, "极致的美食宛如真实的梦幻。");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_KOMEIJI_SATORI_4, "食物能够缩短他乡与故乡的距离。");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_KOMEIJI_SATORI_5, "希望这一顿可以给我带来继续工作的力量。");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_KAENBYOU_RIN_0, "腐烂后会滋生大量细菌，还是新鲜的好。");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_KAENBYOU_RIN_1, "甜食是最简单最初始的美食体验~");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_KAENBYOU_RIN_2, "咱就是爱吃鱼才这么聪明呐。");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_KAENBYOU_RIN_3, "地底和海底其实是相通的吧？");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_KAENBYOU_RIN_4, "有时候一些奇怪的东西更能让猫咪兴奋！");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_KAENBYOU_RIN_5, "真想再去一次梦境里的童话公园。");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_REIUJI_UTSUH_0, "肉是力量的源泉，失去它生命就会枯竭。");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_REIUJI_UTSUH_1, "火辣辣的感觉会让人心痒痒不是吗？");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_REIUJI_UTSUH_2, "阿空盐分补充程序——启动！");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_REIUJI_UTSUH_3, "加入多少油，火焰的燃烧就有多猛烈！");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_REIUJI_UTSUH_4, "就算比火焰还要炽热我也毫不畏惧！");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_REIUJI_UTSUH_5, "我一直在感受体内的力量不断涌现。");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_TATARA_KOGASA_0, "甜食可以促进人际交往哦！");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_TATARA_KOGASA_1, "能让人吓一跳的食物不是很有趣吗？");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_TATARA_KOGASA_2, "一家人围在一起吃饭的感觉很温馨。");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_TATARA_KOGASA_3, "吓不到人肚子总是空空的……");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_TATARA_KOGASA_4, "不可思议也是一种惊讶的情绪哦。");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_TATARA_KOGASA_5, "我需要更多的力量去吓唬人类！");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_TATARA_KOGASA_6, "好看的食物应该也会很好吃吧？");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_HOUJUU_NUE_0, "其实人类才是最喜欢猎奇的动物。 ");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_HOUJUU_NUE_1, "经文可以慢慢看，肉必须趁热吃。");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_HOUJUU_NUE_2, "腐肉我过去已经吃得够多了。");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_HOUJUU_NUE_3, "生食这种进食方式虽然原始但很方便呢。");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_HOUJUU_NUE_4, "不可思议正是我的本质。");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_HOUJUU_NUE_5, "就算是最拿手的招牌菜也要有点变化才好。");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_HOUJUU_NUE_6, "每个地方都有各自独特的料理。");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_HOUJUU_NUE_7, "人类只要目睹到我的存在都会拍个不停。");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_MONONOBE_NO_FUTO_0, "吾之时代的风格还能延续至今真乃幸事一桩。");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_MONONOBE_NO_FUTO_1, "既不能置身火海，是否能让内心燃烧起来？");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_MONONOBE_NO_FUTO_2, "吾所仰慕的太子大人是留下了许多传说的圣人。");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_MONONOBE_NO_FUTO_3, "在吾之时代，贵族饮食皆以清淡为主。");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_MONONOBE_NO_FUTO_4, "道家荤食原料多用山珍野味，调料则惯用药料。");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_MONONOBE_NO_FUTO_5, "嗜好高级品或许是身为豪族时留下的习惯……");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_KAKU_SEIGA_0, "每一道甜品都有一个故事哟~");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_KAKU_SEIGA_1, "仙人要是满嘴荤腥就太难看了。");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_KAKU_SEIGA_2, "宵夜的份量宜少不宜多。");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_KAKU_SEIGA_3, "我的家乡是个遍地都是道士仙人的地方。");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_KAKU_SEIGA_4, "不可思议的人生才有趣~");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_KAKU_SEIGA_5, "人类的传说中也记录着我的故事呢。");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_KAKU_SEIGA_6, "小小的店就能吃遍各地美食真是太方便了。");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_SOGA_NO_TOZIKO_0, "偶尔也品尝一下过去吃过的东西吧。");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_SOGA_NO_TOZIKO_1, "脑子装不下的东西，就用肚子来装。");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_SOGA_NO_TOZIKO_2, "降雷的时候能感到力量一下子从身体里涌现出来。");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_SOGA_NO_TOZIKO_3, "成为亡灵以后什么重油的不健康的通通都能吃诶~");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_SOGA_NO_TOZIKO_4, "我想试试那种像被雷劈过一样的食物。");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_SOGA_NO_TOZIKO_5, "孤独守护的千年间 ，最怀念的却是家常。");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_SOGA_NO_TOZIKO_6, "从招牌菜就能看出一家店的风格。");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_AYA_0, "天狗的首选果然得是肉呢。");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_AYA_1, "天狗是和风妖怪中很强悍的一支哦。");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_AYA_2, "招牌是经过大众检验的广告。");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_AYA_3, "这里生意这么好皆因有家的味道~");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_AYA_4, "有酒无菜偶尔也会觉得少点滋味~");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_AYA_5, "有没有值得入我相机的菜呢~");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_KAZAMI_YUKA_0, "不要用沾染油腻的手去触碰花儿。");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_KAZAMI_YUKA_1, "花儿的语言比人类更含蓄，也更高级。");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_KAZAMI_YUKA_2, "蓝色满天星的花语是——");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_KAZAMI_YUKA_3, "每个花语都有一个流传至今的过去。");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_KAZAMI_YUKA_4, "无法离开出生地的花儿最终会成为当地的特色。");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_KAZAMI_YUKA_5, "我挺喜欢那种追崇绅士风度的社会风尚。");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_KAZAMI_YUKA_6, "冬紫罗兰的花语是——。");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_KIJIN_SEIJIA_0, "我钟意那些受人厌恶的怪诞之物！");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_KIJIN_SEIJIA_1, "苦酒入喉心作痛，配点小菜吃一吃。");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_KIJIN_SEIJIA_2, "把那些脑满肠肥的欺压者煎一煎的话会出很多油吧。");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_KIJIN_SEIJIA_3, "看着滋滋冒气的东西会让我心情大好。");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_KIJIN_SEIJIA_4, "一想到弱者颠覆强者的未来我就澎湃不已！");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_KIJIN_SEIJIA_5, "你们怎么形容根本不可能实现的事物？");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_KIJIN_SEIJIA_6, "为了达成我的伟业，我需要更多力量！");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_SUKUNA_SHINMYOUMARU_0, "甜品的力量是治愈的力量。");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_SUKUNA_SHINMYOUMARU_1, "小人的祖先是日本神话中少彦名神的后裔。");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_SUKUNA_SHINMYOUMARU_2, "我喜欢听那些充满勇气的伟大故事。");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_SUKUNA_SHINMYOUMARU_3, "看我的样子就知道我的食量了。");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_SUKUNA_SHINMYOUMARU_4, "这是一座充满底蕴却无人知晓的城……");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_SUKUNA_SHINMYOUMARU_5, "我想要很漂亮能跟别人分享的菜式！");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_SUKUNA_SHINMYOUMARU_6, "勇者的身份真是让人热血沸腾！");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_IMAIZUMI_KAGEROU_0, "毕竟人家是狼嘛……");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_IMAIZUMI_KAGEROU_1, "本州狼自然喜欢本州风格的食谱哦。");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_IMAIZUMI_KAGEROU_2, "比起水里游的我更喜欢山上跑的。");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_IMAIZUMI_KAGEROU_3, "我也很喜欢有家庭气氛的小菜哦。");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_IMAIZUMI_KAGEROU_4, "毛发增长的时候就很需要从胃里降温……");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_IMAIZUMI_KAGEROU_5, "漂亮的食物当然是先给相机吃！");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_REISEN_0, "想尝尝那些我无法抵达的地方的美食……");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_REISEN_1, "兔子的胃是很小的。");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_REISEN_2, "家的味道可以治愈一天的疲惫。");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_REISEN_3, "静海要是不那么死气沉沉就好了。");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_REISEN_4, "月兔也是仙子，这是什么地方的传说呀？");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_REISEN_5, "月之都如果有山，会有什么动物吗？");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_REISEN_6, "糖分是唯一的救赎。");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_WATATSUKI_TOYOHIME_0, "我就算吃再多糖也不会蛀牙哟。");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_WATATSUKI_TOYOHIME_1, "高级精致的品相更容易提起人的兴趣呢。");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_WATATSUKI_TOYOHIME_2, "最近有点喜欢上和歌了。");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_WATATSUKI_TOYOHIME_3, "听说吃草能够帮助消化？");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_WATATSUKI_TOYOHIME_4, "好热……可惜不能用这把扇子来扇风……");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_WATATSUKI_TOYOHIME_5, "桃子就是最棒的哦！");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_WATATSUKI_TOYOHIME_6, "我喜欢听料理里的故事。");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_REMILIA_0, "这里也能做出异国风味的料理吗？");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_REMILIA_1, "只有高级的料理才配得上我的身份。");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_REMILIA_2, "吸血鬼……当然是要吃生的。");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_REMILIA_3, "在过去，糖可是贵族的专属消费品呢。");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_REMILIA_4, "吸血鬼留下的传说百年无人敢忘。");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_KOMEIJI_KOISHI_0, "我有两个胃，一个吃饭一个吃甜点！");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_KOMEIJI_KOISHI_1, "有一种兴奋剂是咸味的哦！好像叫做汗水来着。");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_KOMEIJI_KOISHI_2, "生吃还有什么讲究吗？");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_KOMEIJI_KOISHI_3, "带着猎奇的心态去看待世界会更有趣哦~");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_KOMEIJI_KOISHI_4, "梦幻就是永恒的意思吗？");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_KOMEIJI_KOISHI_5, "你永远猜不到我想要的是什么啦~");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_HUTATSUIWA_MAMIZOU_0, "不吃肉怎么可能变得强大？");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_HUTATSUIWA_MAMIZOU_1, "老朽还是喜欢佐渡老家的味道呐~");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_HUTATSUIWA_MAMIZOU_2, "狸猫虽然不是猫，但也很爱吃鱼。");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_HUTATSUIWA_MAMIZOU_3, "在佐渡和人类共同生活的日常也很让人怀念呐。");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_HUTATSUIWA_MAMIZOU_4, "下酒的菜还没准备好吗——？");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_HUTATSUIWA_MAMIZOU_5, "人类就喜欢看狸猫吃果子的样子。");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_HUTATSUIWA_MAMIZOU_6, "狸猫的传说要多少有多少，不是吗？");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_YUKARI_0, "高级感是一种能让人感到极富品质感的体验。");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_YUKARI_1, "家就是能让你睡个好觉的地方。");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_YUKARI_2, "“传说”催生了妖怪。");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_YUKARI_3, "听说过“汤水养人”吗？");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_YUKARI_4, "我的隙间哪里猎奇了哟～");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_YUKARI_5, "呆在伞下很凉快哦～");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_YUKARI_6, "食之品相也十分重要。");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_FLANDRE_0, "没有做熟的食物和血散发着同一种味道呢。");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_FLANDRE_1, "世界肯定比我的房间大的多了！有什么有趣的东西吗？");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_FLANDRE_2, "外面的世界好美……好想多记住一些。");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_FLANDRE_3, "姐姐从来不让我见到热辣辣的太阳，真好奇，好好奇呀。");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_FLANDRE_4, "可爱的玩偶总会玩腻的……想来点不一样的……");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_FLANDRE_5, "有什么东西能比带着魔法的女仆长更神奇吗？");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_FLANDRE_6, "辣味是一种痛觉哦，喜欢！");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_ERIN_0, "如果拒绝接触家常些的东西的话，生命就会与现实疏离开来。");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_ERIN_1, "传说中的那些故事，在遥远的过去可能真实存在过。");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_ERIN_2, "即使拥有无限的生命，人的阅历也短于文明的尺度。");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_ERIN_3, "蝴蝶梦，梦蝴蝶……梦幻如现实，现实如梦幻。");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_ERIN_4, "穷尽天文的脑力，也不可能明晰整个世界的变数。在某个角落，总会藏着独特的惊喜。");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_ERIN_5, "微毒的食物很多可以入药，人们总是忘记这一点。");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_RAN_0, "八云家吃的东西多少是要高级一些的。");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_RAN_1, "狐妖在这片土地上可是创造了许多传说的。");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_RAN_2, "油炸豆腐虽然很好吃，但是偶尔也想吃点别的炸货。");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_RAN_3, "水会弄湿衣服和尾巴，还是山比较让人喜欢。");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_RAN_4, "狐狸可是和风的重要组成部分哦。");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_RAN_5, "侍奉紫大人真是累人呀……");
        translationBuilder.add(CustomerOrderConsts.CUSTOMER_ORDER_RAN_6, "我的智慧，连三途川的长度都能计算出来。是不是很不可思议？");

    }

    public void generateItemDescriptionTranslations(HolderLookup.Provider wrapperLookup, TranslationBuilder translationBuilder) {
        translationBuilder.add(ItemDescriptionConsts.SEAFOOD_MISO_SOUP_DESCRIPTION_0, "居酒屋常见的快手汤羹");
        translationBuilder.add(ItemDescriptionConsts.SEAFOOD_MISO_SOUP_DESCRIPTION_1, "来历不明却随处可见的海带在幻想乡出现之初");
        translationBuilder.add(ItemDescriptionConsts.SEAFOOD_MISO_SOUP_DESCRIPTION_2, "有人好奇水煮了一下");
        translationBuilder.add(ItemDescriptionConsts.SEAFOOD_MISO_SOUP_DESCRIPTION_3, "结果意外地发现有种异样的鲜味");
        translationBuilder.add(ItemDescriptionConsts.SEAFOOD_MISO_SOUP_DESCRIPTION_4, "从此便在幻想乡流行开了");
        translationBuilder.add(ItemDescriptionConsts.TOFU_MISO_DESCRIPTION_0, "居酒屋常见的快手汤羹");
        translationBuilder.add(ItemDescriptionConsts.TOFU_MISO_DESCRIPTION_1, "使用了豆腐来提鲜");
        translationBuilder.add(ItemDescriptionConsts.TOFU_MISO_DESCRIPTION_2, "最简单又最原始的美味");
        translationBuilder.add(ItemDescriptionConsts.STRENGTH_SOUP_DESCRIPTION_0, "荤素搭配的美味汤羹");
        translationBuilder.add(ItemDescriptionConsts.STRENGTH_SOUP_DESCRIPTION_1, "使用了野猪肉和海带煲煮而成");
        translationBuilder.add(ItemDescriptionConsts.STRENGTH_SOUP_DESCRIPTION_2, "能最快捷地补充身体所需能量");
        translationBuilder.add(ItemDescriptionConsts.PORK_AND_TROUT_SMOKED_DESCRIPTION_0, "把猪肉和鳟鱼放在一起熏制而成");
        translationBuilder.add(ItemDescriptionConsts.PORK_AND_TROUT_SMOKED_DESCRIPTION_1, "是简单好吃的肉食料理");
        translationBuilder.add(ItemDescriptionConsts.PORK_AND_TROUT_SMOKED_DESCRIPTION_2, "也比较能保存");
        translationBuilder.add(ItemDescriptionConsts.GRILLED_HAGFISH_DESCRIPTION_0, "本店招牌");
        translationBuilder.add(ItemDescriptionConsts.GRILLED_HAGFISH_DESCRIPTION_1, "为了打破红灯笼店就是烤鸟肉店的成见");
        translationBuilder.add(ItemDescriptionConsts.GRILLED_HAGFISH_DESCRIPTION_2, "特意选择了对夜盲症有奇效的八目鳗");
        translationBuilder.add(ItemDescriptionConsts.GRILLED_HAGFISH_DESCRIPTION_3, "据说在过去还被视作珍宝");
        translationBuilder.add(ItemDescriptionConsts.ENERGY_STRING_DESCRIPTION_0, "牛肉搭配洋葱、南瓜烤成的串串");
        translationBuilder.add(ItemDescriptionConsts.ENERGY_STRING_DESCRIPTION_1, "巧妙地利用了洋葱的刺激和南瓜的甜味去除肉质的油腻");
        translationBuilder.add(ItemDescriptionConsts.ENERGY_STRING_DESCRIPTION_2, "食之更加清爽");
        translationBuilder.add(ItemDescriptionConsts.TWO_HEAVENS_ONE_STYLE_DESCRIPTION_0, "传说中和鬼立下赌约并获胜的人类剑士所创下的烤串流派");
        translationBuilder.add(ItemDescriptionConsts.TWO_HEAVENS_ONE_STYLE_DESCRIPTION_1, "特别使用了野性十足的肉类烧制而成");
        translationBuilder.add(ItemDescriptionConsts.TWO_HEAVENS_ONE_STYLE_DESCRIPTION_2, "食之有种冲天的气魄");
        translationBuilder.add(ItemDescriptionConsts.TWO_HEAVENS_ONE_STYLE_DESCRIPTION_3, "让人惊叹不已");
        translationBuilder.add(ItemDescriptionConsts.RICE_BALL_DESCRIPTION_0, "最普通的饭团");
        translationBuilder.add(ItemDescriptionConsts.RICE_BALL_DESCRIPTION_1, "加点海带随便捏捏就可以了");
        translationBuilder.add(ItemDescriptionConsts.RICE_BALL_DESCRIPTION_2, "超便捷的经典");
        translationBuilder.add(ItemDescriptionConsts.GRILLED_PORK_RICE_BALLS_DESCRIPTION_0, "常见的平价饭团");
        translationBuilder.add(ItemDescriptionConsts.GRILLED_PORK_RICE_BALLS_DESCRIPTION_1, "在饭团中放入烤制后的猪肉");
        translationBuilder.add(ItemDescriptionConsts.GRILLED_PORK_RICE_BALLS_DESCRIPTION_2, "为其增加了一份香浓的嚼劲");
        translationBuilder.add(ItemDescriptionConsts.WARM_RICE_BALL_DESCRIPTION_0, "常见的平价饭团");
        translationBuilder.add(ItemDescriptionConsts.WARM_RICE_BALL_DESCRIPTION_1, "内馅儿加入了鳟鱼和洋葱");
        translationBuilder.add(ItemDescriptionConsts.WARM_RICE_BALL_DESCRIPTION_2, "融合了海鲜的细腻口感和洋葱的炽热");
        translationBuilder.add(ItemDescriptionConsts.WARM_RICE_BALL_DESCRIPTION_3, "无论是营养还是口感都属上佳");
        translationBuilder.add(ItemDescriptionConsts.FAILING_SAKURA_SNOW_DESCRIPTION_0, "高级寿司的一种");
        translationBuilder.add(ItemDescriptionConsts.FAILING_SAKURA_SNOW_DESCRIPTION_1, "粉红色的高级生鱼片盖在白米饭上");
        translationBuilder.add(ItemDescriptionConsts.FAILING_SAKURA_SNOW_DESCRIPTION_2, "就如樱花飘落在白雪上");
        translationBuilder.add(ItemDescriptionConsts.FAILING_SAKURA_SNOW_DESCRIPTION_3, "有着不可思议的美感");
        translationBuilder.add(ItemDescriptionConsts.FRIED_PORK_SHREDS_DESCRIPTION_0, "以猪肉作为主要食材制作而成的家常菜");
        translationBuilder.add(ItemDescriptionConsts.FRIED_PORK_SHREDS_DESCRIPTION_1, "口味偏重");
        translationBuilder.add(ItemDescriptionConsts.COLD_TOFU_DESCRIPTION_0, "夏天的消暑下酒菜");
        translationBuilder.add(ItemDescriptionConsts.COLD_TOFU_DESCRIPTION_1, "简单爽口");
        translationBuilder.add(ItemDescriptionConsts.BRAISED_EEL_DESCRIPTION_0, "本店招牌");
        translationBuilder.add(ItemDescriptionConsts.BRAISED_EEL_DESCRIPTION_1, "将鳗鱼用特殊酱料进行烧制后肉汁四溢");
        translationBuilder.add(ItemDescriptionConsts.BRAISED_EEL_DESCRIPTION_2, "光闻着香味便让人垂涎不已");
        translationBuilder.add(ItemDescriptionConsts.POTATO_CROQUETTES_DESCRIPTION_0, "主要由土豆制成的");
        translationBuilder.add(ItemDescriptionConsts.POTATO_CROQUETTES_DESCRIPTION_1, "外表酥脆");
        translationBuilder.add(ItemDescriptionConsts.POTATO_CROQUETTES_DESCRIPTION_2, "内在绵软可口");
        translationBuilder.add(ItemDescriptionConsts.POTATO_CROQUETTES_DESCRIPTION_3, "在油炸类食品中有着较高的人气");
        translationBuilder.add(ItemDescriptionConsts.GAME_SOUP_DESCRIPTION_0, "用农家蔬菜佐以优质黑毛猪肉炖煮的烩锅");
        translationBuilder.add(ItemDescriptionConsts.GAME_SOUP_DESCRIPTION_1, "口感饱满");
        translationBuilder.add(ItemDescriptionConsts.GAME_SOUP_DESCRIPTION_2, "香浓却不油腻");
        translationBuilder.add(ItemDescriptionConsts.GAME_SOUP_DESCRIPTION_3, "是农家人最高级的大菜");
        translationBuilder.add(ItemDescriptionConsts.PORK_RICE_DESCRIPTION_0, "常见的家常菜");
        translationBuilder.add(ItemDescriptionConsts.PORK_RICE_DESCRIPTION_1, "看上去颗颗饭粒饱满");
        translationBuilder.add(ItemDescriptionConsts.PORK_RICE_DESCRIPTION_2, "淋上的香酱和猪肉的口感融为一体");
        translationBuilder.add(ItemDescriptionConsts.PORK_RICE_DESCRIPTION_3, "令人胃口倍增");
        translationBuilder.add(ItemDescriptionConsts.BEEF_RICE_DESCRIPTION_0, "常见的家常菜");
        translationBuilder.add(ItemDescriptionConsts.BEEF_RICE_DESCRIPTION_1, "看上去颗颗饭粒饱满");
        translationBuilder.add(ItemDescriptionConsts.BEEF_RICE_DESCRIPTION_2, "淋上的香酱和牛肉的口感融为一体");
        translationBuilder.add(ItemDescriptionConsts.BEEF_RICE_DESCRIPTION_3, "令人胃口倍增");
        translationBuilder.add(ItemDescriptionConsts.FRIED_HAGFISH_DESCRIPTION_0, "本店招牌");
        translationBuilder.add(ItemDescriptionConsts.FRIED_HAGFISH_DESCRIPTION_1, "长相怪异的八目鳗在喜欢尝鲜的幻想乡曾一度成为话题");
        translationBuilder.add(ItemDescriptionConsts.FRIED_HAGFISH_DESCRIPTION_2, "油炸后爽滑酥嫩");
        translationBuilder.add(ItemDescriptionConsts.FRIED_HAGFISH_DESCRIPTION_3, "深受大众喜爱");
        translationBuilder.add(ItemDescriptionConsts.VEGETABLE_SPECIAL_DESCRIPTION_0, "用新鲜的蔬菜生拌而成的沙拉");
        translationBuilder.add(ItemDescriptionConsts.VEGETABLE_SPECIAL_DESCRIPTION_1, "口感清新");
        translationBuilder.add(ItemDescriptionConsts.VEGETABLE_SPECIAL_DESCRIPTION_2, "可以去除嘴里的油腻");
        translationBuilder.add(ItemDescriptionConsts.VEGETABLE_SPECIAL_DESCRIPTION_3, "不知为何被年轻的姑娘们奉为减肥圣餐");
        translationBuilder.add(ItemDescriptionConsts.SNOW_WHITE_DESCRIPTION_0, "使用鲜美的八目鳗和河豚");
        translationBuilder.add(ItemDescriptionConsts.SNOW_WHITE_DESCRIPTION_1, "再佐以海带炖煮而成的高级烩锅");
        translationBuilder.add(ItemDescriptionConsts.SNOW_WHITE_DESCRIPTION_2, "由于煮的过程中会飘出纯白的泡沫而得名");
        translationBuilder.add(ItemDescriptionConsts.SNOW_WHITE_DESCRIPTION_3, "是非常高级的家庭料理");
        translationBuilder.add(ItemDescriptionConsts.TOFU_POT_DESCRIPTION_0, "由豆腐炖煮而成的烩锅");
        translationBuilder.add(ItemDescriptionConsts.TOFU_POT_DESCRIPTION_1, "滑嫩的口感再加上其本身具有的较高营养价值");
        translationBuilder.add(ItemDescriptionConsts.TOFU_POT_DESCRIPTION_2, "使这道平价料理成为居酒屋最常见的烩锅");
        translationBuilder.add(ItemDescriptionConsts.ZHAJI_DESCRIPTION_0, "使用一些边角料食材杂烩而成的烩锅");
        translationBuilder.add(ItemDescriptionConsts.ZHAJI_DESCRIPTION_1, "享受美味的同时还能避免浪费");
        translationBuilder.add(ItemDescriptionConsts.ZHAJI_DESCRIPTION_2, "可谓一举两得");
        translationBuilder.add(ItemDescriptionConsts.SASHIMI_PLATTER_DESCRIPTION_0, "作为和风料理的代表");
        translationBuilder.add(ItemDescriptionConsts.SASHIMI_PLATTER_DESCRIPTION_1, "将刺身级的三文鱼和金枪鱼鱼生配上芥末和酱油");
        translationBuilder.add(ItemDescriptionConsts.SASHIMI_PLATTER_DESCRIPTION_2, "引出鲜味的绝妙料理");
        translationBuilder.add(ItemDescriptionConsts.GRAND_BANQUET_DESCRIPTION_0, "奢侈地选用了一系列高级食材炖煮成烩锅");
        translationBuilder.add(ItemDescriptionConsts.GRAND_BANQUET_DESCRIPTION_1, "通过火候的精妙控制");
        translationBuilder.add(ItemDescriptionConsts.GRAND_BANQUET_DESCRIPTION_2, "将食材之间的特点全部提炼了出来");
        translationBuilder.add(ItemDescriptionConsts.GRAND_BANQUET_DESCRIPTION_3, "肉质鲜嫩多汁、香滑入味");
        translationBuilder.add(ItemDescriptionConsts.GRAND_BANQUET_DESCRIPTION_4, "令人其味无穷");
        translationBuilder.add(ItemDescriptionConsts.TONKOTSU_RAMEN_DESCRIPTION_0, "用猪肉和蔬菜经过长时间熬制出来的高汤");
        translationBuilder.add(ItemDescriptionConsts.TONKOTSU_RAMEN_DESCRIPTION_1, "堪称整碗拉面的精髓和灵魂所在");
        translationBuilder.add(ItemDescriptionConsts.TONKOTSU_RAMEN_DESCRIPTION_2, "香浓醇厚的豚骨汤底配上香弹可口的拉面");
        translationBuilder.add(ItemDescriptionConsts.TONKOTSU_RAMEN_DESCRIPTION_3, "饱腹之余也让舌尖得到最大的满足");
        translationBuilder.add(ItemDescriptionConsts.MAGMA_DESCRIPTION_0, "用高级牛肉和松露炖煮而成的烩锅");
        translationBuilder.add(ItemDescriptionConsts.MAGMA_DESCRIPTION_1, "最初以麻辣为特色");
        translationBuilder.add(ItemDescriptionConsts.MAGMA_DESCRIPTION_2, "因炖煮中冒出的气泡如岩浆而得名");
        translationBuilder.add(ItemDescriptionConsts.MAGMA_DESCRIPTION_3, "款款而起的香味更是让人食指大动");
        translationBuilder.add(ItemDescriptionConsts.MAGMA_DESCRIPTION_4, "改良后也增加了不辣的版本");
        translationBuilder.add(ItemDescriptionConsts.DEEP_FRIED_CICADA_SHELLS_DESCRIPTION_0, "蝉科昆虫黑蚱羽化后的蜕壳");
        translationBuilder.add(ItemDescriptionConsts.DEEP_FRIED_CICADA_SHELLS_DESCRIPTION_1, "可以入药");
        translationBuilder.add(ItemDescriptionConsts.DEEP_FRIED_CICADA_SHELLS_DESCRIPTION_2, "有利咽开音、明目退翳之效");
        translationBuilder.add(ItemDescriptionConsts.DEEP_FRIED_CICADA_SHELLS_DESCRIPTION_3, "香炸后口感酥脆");
        translationBuilder.add(ItemDescriptionConsts.DEEP_FRIED_CICADA_SHELLS_DESCRIPTION_4, "颇受欢迎");
        translationBuilder.add(ItemDescriptionConsts.DEW_BOILED_EGGS_DESCRIPTION_0, "采集了清晨的露珠煮成的蛋");
        translationBuilder.add(ItemDescriptionConsts.DEW_BOILED_EGGS_DESCRIPTION_1, "比一般的水煮蛋多出一种甘甜的味道");
        translationBuilder.add(ItemDescriptionConsts.DEW_BOILED_EGGS_DESCRIPTION_2, "为了保持鲜嫩只煮到半熟");
        translationBuilder.add(ItemDescriptionConsts.DEW_BOILED_EGGS_DESCRIPTION_3, "蛋黄水嫩得似乎稍加晃动就会流出来");
        translationBuilder.add(ItemDescriptionConsts.UDUMBARA_CAKE_DESCRIPTION_0, "用上古时期便存在的奇迹之花制作的糕点");
        translationBuilder.add(ItemDescriptionConsts.UDUMBARA_CAKE_DESCRIPTION_1, "不仅甜而不腻");
        translationBuilder.add(ItemDescriptionConsts.UDUMBARA_CAKE_DESCRIPTION_2, "食后更是齿颊留香");
        translationBuilder.add(ItemDescriptionConsts.UDUMBARA_CAKE_DESCRIPTION_3, "据说能勾起人心中最想怀念的回忆");
        translationBuilder.add(ItemDescriptionConsts.BEAR_PAW_DESCRIPTION_0, "黑不溜秋的怪异美食之首！香飘万里");
        translationBuilder.add(ItemDescriptionConsts.BEAR_PAW_DESCRIPTION_1, "让人回味无穷");
        translationBuilder.add(ItemDescriptionConsts.BEAR_PAW_DESCRIPTION_2, "因为打不过熊");
        translationBuilder.add(ItemDescriptionConsts.BEAR_PAW_DESCRIPTION_3, "没法直接用熊掌做");
        translationBuilder.add(ItemDescriptionConsts.BEAR_PAW_DESCRIPTION_4, "但是比真正的熊掌还要鲜美百倍");
        translationBuilder.add(ItemDescriptionConsts.SECRET_DRIED_FISH_DESCRIPTION_0, "用秘制的香料将小鱼干腌制后晒干");
        translationBuilder.add(ItemDescriptionConsts.SECRET_DRIED_FISH_DESCRIPTION_1, "酥脆香口的同时又易于保存");
        translationBuilder.add(ItemDescriptionConsts.SECRET_DRIED_FISH_DESCRIPTION_2, "寻常人家都喜欢在家中保存一份");
        translationBuilder.add(ItemDescriptionConsts.COLD_DISH_CARVING_DESCRIPTION_0, "将鲜果蔬菜雕刻成鲜花的模样");
        translationBuilder.add(ItemDescriptionConsts.COLD_DISH_CARVING_DESCRIPTION_1, "虽然材料简单");
        translationBuilder.add(ItemDescriptionConsts.COLD_DISH_CARVING_DESCRIPTION_2, "但却非常考验刀工");
        translationBuilder.add(ItemDescriptionConsts.PEACH_BLOSSOM_SOUP_DESCRIPTION_0, "来自天上的配方");
        translationBuilder.add(ItemDescriptionConsts.PEACH_BLOSSOM_SOUP_DESCRIPTION_1, "采摘新鲜的桃花");
        translationBuilder.add(ItemDescriptionConsts.PEACH_BLOSSOM_SOUP_DESCRIPTION_2, "配以清晨的甘露水煮而成");
        translationBuilder.add(ItemDescriptionConsts.PEACH_BLOSSOM_SOUP_DESCRIPTION_3, "不仅芳香清甜");
        translationBuilder.add(ItemDescriptionConsts.PEACH_BLOSSOM_SOUP_DESCRIPTION_4, "而且具有祛病美容的神奇功效");
        translationBuilder.add(ItemDescriptionConsts.ARCTIC_SWEET_SHRIMP_AND_PEACH_SALAD_DESCRIPTION_0, "选用品质最好、肉质最鲜的虾和桃子加工而成的高级料理");
        translationBuilder.add(ItemDescriptionConsts.ARCTIC_SWEET_SHRIMP_AND_PEACH_SALAD_DESCRIPTION_1, "据说在外界只有在宴席上才有机会一见");
        translationBuilder.add(ItemDescriptionConsts.FRIED_TOFU_DESCRIPTION_0, "常见的家常菜");
        translationBuilder.add(ItemDescriptionConsts.FRIED_TOFU_DESCRIPTION_1, "传说中是稻荷神的狐狸使者最喜欢的食物");
        translationBuilder.add(ItemDescriptionConsts.POETRY_AND_GINKGO_DESCRIPTION_0, "以选用孔庙“诗礼堂”前银杏树所结果实烹制而得名");
        translationBuilder.add(ItemDescriptionConsts.POETRY_AND_GINKGO_DESCRIPTION_1, "清香甜美");
        translationBuilder.add(ItemDescriptionConsts.POETRY_AND_GINKGO_DESCRIPTION_2, "柔韧筋道");
        translationBuilder.add(ItemDescriptionConsts.POETRY_AND_GINKGO_DESCRIPTION_3, "可解酒止咳");
        translationBuilder.add(ItemDescriptionConsts.REAL_SEAFOOD_MISO_SOUP_DESCRIPTION_0, "选用新鲜鳟鱼和海带煲煮而成的味噌汤");
        translationBuilder.add(ItemDescriptionConsts.REAL_SEAFOOD_MISO_SOUP_DESCRIPTION_1, "浓浓的鲜味四处漂荡");
        translationBuilder.add(ItemDescriptionConsts.REAL_SEAFOOD_MISO_SOUP_DESCRIPTION_2, "鲜而不腥");
        translationBuilder.add(ItemDescriptionConsts.ROASTED_MUSHROOMS_DESCRIPTION_0, "采用蘑菇为原料");
        translationBuilder.add(ItemDescriptionConsts.ROASTED_MUSHROOMS_DESCRIPTION_1, "将蘑菇用竹签串起来后");
        translationBuilder.add(ItemDescriptionConsts.ROASTED_MUSHROOMS_DESCRIPTION_2, "刷少量油进行烧烤");
        translationBuilder.add(ItemDescriptionConsts.ROASTED_MUSHROOMS_DESCRIPTION_3, "撒上粗盐");
        translationBuilder.add(ItemDescriptionConsts.ROASTED_MUSHROOMS_DESCRIPTION_4, "味道简直不输给直接吃肉！");
        translationBuilder.add(ItemDescriptionConsts.COOKING_TOFU_DESCRIPTION_0, "常见的家常菜");
        translationBuilder.add(ItemDescriptionConsts.COOKING_TOFU_DESCRIPTION_1, "但也讲究烧制的火候");
        translationBuilder.add(ItemDescriptionConsts.COOKING_TOFU_DESCRIPTION_2, "才能将豆腐的鲜嫩口感得到最大展现");
        translationBuilder.add(ItemDescriptionConsts.FRIED_PORK_CUTLET_DESCRIPTION_0, "常见的家常菜");
        translationBuilder.add(ItemDescriptionConsts.FRIED_PORK_CUTLET_DESCRIPTION_1, "以猪肉为主要材料");
        translationBuilder.add(ItemDescriptionConsts.FRIED_PORK_CUTLET_DESCRIPTION_2, "裹以面粉一炸");
        translationBuilder.add(ItemDescriptionConsts.FRIED_PORK_CUTLET_DESCRIPTION_3, "邻居家的孩子都馋哭了");
        translationBuilder.add(ItemDescriptionConsts.BUTTER_STEAK_DESCRIPTION_0, "简单而复杂");
        translationBuilder.add(ItemDescriptionConsts.BUTTER_STEAK_DESCRIPTION_1, "根据火候和食材的选择");
        translationBuilder.add(ItemDescriptionConsts.BUTTER_STEAK_DESCRIPTION_2, "呈现出不同感觉的基础西餐");
        translationBuilder.add(ItemDescriptionConsts.BUTTER_STEAK_DESCRIPTION_3, "顺带一提红魔馆的那位喜欢的是三分熟");
        translationBuilder.add(ItemDescriptionConsts.RISOTTO_DESCRIPTION_0, "将食材炒熟之后倒入生米");
        translationBuilder.add(ItemDescriptionConsts.RISOTTO_DESCRIPTION_1, "充分混合米粒和食材香味的外界某个半岛的做法");
        translationBuilder.add(ItemDescriptionConsts.BEEF_WELLINGTON_DESCRIPTION_0, "将牛排和松露这两种鲜美的食材调味后包裹在酥皮中进行烘焙");
        translationBuilder.add(ItemDescriptionConsts.BEEF_WELLINGTON_DESCRIPTION_1, "让黄油酥皮的香味和牛排蘑菇的鲜美充分融合的极致菜肴");
        translationBuilder.add(ItemDescriptionConsts.BEEF_WELLINGTON_DESCRIPTION_2, "工序繁复");
        translationBuilder.add(ItemDescriptionConsts.BEEF_WELLINGTON_DESCRIPTION_3, "在外界是出了名的难做");
        translationBuilder.add(ItemDescriptionConsts.EGGS_BENEDICT_DESCRIPTION_0, "流黄的水波蛋和大口的碳水");
        translationBuilder.add(ItemDescriptionConsts.EGGS_BENEDICT_DESCRIPTION_1, "是早午餐的常见选择");
        translationBuilder.add(ItemDescriptionConsts.HOT_WAFFLES_DESCRIPTION_0, "早餐的简单选择");
        translationBuilder.add(ItemDescriptionConsts.HOT_WAFFLES_DESCRIPTION_1, "将准备好的面糊煎熟");
        translationBuilder.add(ItemDescriptionConsts.HOT_WAFFLES_DESCRIPTION_2, "浇上蜂蜜就可以吃了");
        translationBuilder.add(ItemDescriptionConsts.SCONES_DESCRIPTION_0, "英式下午茶的常客");
        translationBuilder.add(ItemDescriptionConsts.SCONES_DESCRIPTION_1, "外酥内软");
        translationBuilder.add(ItemDescriptionConsts.SCONES_DESCRIPTION_2, "一般蘸着果酱或者奶油一起吃");
        translationBuilder.add(ItemDescriptionConsts.PAN_FRIED_SALMON_DESCRIPTION_0, "将整块带皮的三文鱼煎至外焦里嫩");
        translationBuilder.add(ItemDescriptionConsts.PAN_FRIED_SALMON_DESCRIPTION_1, "配上鲜嫩的竹笋——不过外界这个菜谱一般是芦笋");
        translationBuilder.add(ItemDescriptionConsts.PAN_FRIED_SALMON_DESCRIPTION_2, "这也算是幻想乡式的融合菜吧");
        translationBuilder.add(ItemDescriptionConsts.CREAM_STEW_DESCRIPTION_0, "制作家常奶油浓汤");
        translationBuilder.add(ItemDescriptionConsts.CREAM_STEW_DESCRIPTION_1, "制作方法简单");
        translationBuilder.add(ItemDescriptionConsts.CREAM_STEW_DESCRIPTION_2, "无论是蘸面包还是当作炖菜来吃都是非常不错的料理");
        translationBuilder.add(ItemDescriptionConsts.HONEY_BBQ_PORK_DESCRIPTION_0, "来自红美铃老家的特殊做法");
        translationBuilder.add(ItemDescriptionConsts.HONEY_BBQ_PORK_DESCRIPTION_1, "制作工序有点繁复");
        translationBuilder.add(ItemDescriptionConsts.HONEY_BBQ_PORK_DESCRIPTION_2, "但是口感独一无二");
        translationBuilder.add(ItemDescriptionConsts.HONEY_BBQ_PORK_DESCRIPTION_3, "令人难忘");
        translationBuilder.add(ItemDescriptionConsts.GINKGO_AND_RADISH_PORK_RIB_SOUP_DESCRIPTION_0, "来自红美铃老家的煲汤技巧");
        translationBuilder.add(ItemDescriptionConsts.GINKGO_AND_RADISH_PORK_RIB_SOUP_DESCRIPTION_1, "色香味俱全");
        translationBuilder.add(ItemDescriptionConsts.GINKGO_AND_RADISH_PORK_RIB_SOUP_DESCRIPTION_2, "益气补血");
        translationBuilder.add(ItemDescriptionConsts.TAKETORIHIME_DESCRIPTION_0, "在永远亭就地取材");
        translationBuilder.add(ItemDescriptionConsts.TAKETORIHIME_DESCRIPTION_1, "使用新鲜的食材和米饭一起塞进竹筒中蒸熟");
        translationBuilder.add(ItemDescriptionConsts.TAKETORIHIME_DESCRIPTION_2, "饭被竹子的清香充分浸润后");
        translationBuilder.add(ItemDescriptionConsts.TAKETORIHIME_DESCRIPTION_3, "中和了山猪肉带来的油腻");
        translationBuilder.add(ItemDescriptionConsts.PHOENIX_DESCRIPTION_0, "使用面粉烘培出烤火鸡的形状");
        translationBuilder.add(ItemDescriptionConsts.PHOENIX_DESCRIPTION_1, "在表面刷上蜂蜜");
        translationBuilder.add(ItemDescriptionConsts.PHOENIX_DESCRIPTION_2, "肚子里塞满食材");
        translationBuilder.add(ItemDescriptionConsts.PHOENIX_DESCRIPTION_3, "进行充分的烘烤");
        translationBuilder.add(ItemDescriptionConsts.PHOENIX_DESCRIPTION_4, "出炉的假烤鸡有着香脆的外皮和多汁的内馅儿");
        translationBuilder.add(ItemDescriptionConsts.MOONLIGHT_DUMPLINGS_DESCRIPTION_0, "永远亭特产改良的麻薯团子");
        translationBuilder.add(ItemDescriptionConsts.MOONLIGHT_DUMPLINGS_DESCRIPTION_1, "加入了高级食材月光草");
        translationBuilder.add(ItemDescriptionConsts.MOONLIGHT_DUMPLINGS_DESCRIPTION_2, "造型可爱的同时");
        translationBuilder.add(ItemDescriptionConsts.MOONLIGHT_DUMPLINGS_DESCRIPTION_3, "还有“月光一样的口感”");
        translationBuilder.add(ItemDescriptionConsts.MOCHI_DESCRIPTION_0, "最普通的糯米团子");
        translationBuilder.add(ItemDescriptionConsts.MOCHI_DESCRIPTION_1, "大家都喜欢的和风甜食");
        translationBuilder.add(ItemDescriptionConsts.WHITE_PEACH_EIGHT_BRIDGE_DESCRIPTION_0, "状似外界古筝的经典和果子");
        translationBuilder.add(ItemDescriptionConsts.WHITE_PEACH_EIGHT_BRIDGE_DESCRIPTION_1, "加入白桃内馅儿之后呈现淡粉色");
        translationBuilder.add(ItemDescriptionConsts.WHITE_PEACH_EIGHT_BRIDGE_DESCRIPTION_2, "非常诱人");
        translationBuilder.add(ItemDescriptionConsts.MOON_LOVERS_DESCRIPTION_0, "外界似乎很流行使用〇〇恋人作为地区伴手礼");
        translationBuilder.add(ItemDescriptionConsts.MOON_LOVERS_DESCRIPTION_1, "永远亭也不甘落后潮流");
        translationBuilder.add(ItemDescriptionConsts.MOON_LOVERS_DESCRIPTION_2, "推出了自己的版本！");
        translationBuilder.add(ItemDescriptionConsts.PIG_DEER_BUTTERFLY_DESCRIPTION_0, "脱胎于花札的猪鹿蝶牌型");
        translationBuilder.add(ItemDescriptionConsts.PIG_DEER_BUTTERFLY_DESCRIPTION_1, "将猪肉和鹿肉清炖");
        translationBuilder.add(ItemDescriptionConsts.PIG_DEER_BUTTERFLY_DESCRIPTION_2, "佐以花朵引出食材本身鲜味的精致料理");
        translationBuilder.add(ItemDescriptionConsts.FLOWING_WATER_NOODLES_DESCRIPTION_0, "比起好吃");
        translationBuilder.add(ItemDescriptionConsts.FLOWING_WATER_NOODLES_DESCRIPTION_1, "流水素面更多的是好玩");
        translationBuilder.add(ItemDescriptionConsts.BAMBOO_SHOOTS_FRIED_MEAT_DESCRIPTION_0, "最朴素的的竹笋吃法");
        translationBuilder.add(ItemDescriptionConsts.BAMBOO_SHOOTS_FRIED_MEAT_DESCRIPTION_1, "由猪肉的油光引出竹笋的鲜美");
        translationBuilder.add(ItemDescriptionConsts.BAMBOO_STEAMED_EGG_DESCRIPTION_0, "竹子作为容器蒸出来的茶碗蒸");
        translationBuilder.add(ItemDescriptionConsts.BAMBOO_STEAMED_EGG_DESCRIPTION_1, "别有一番风味");
        translationBuilder.add(ItemDescriptionConsts.HORAI_DAMA_NO_EDA_DESCRIPTION_0, "简单地说就是使用竹签串起各种高级肉类");
        translationBuilder.add(ItemDescriptionConsts.HORAI_DAMA_NO_EDA_DESCRIPTION_1, "一口吃个饱的料理");
        translationBuilder.add(ItemDescriptionConsts.HORAI_DAMA_NO_EDA_DESCRIPTION_2, "但是在辉夜小姐的胁迫下");
        translationBuilder.add(ItemDescriptionConsts.HORAI_DAMA_NO_EDA_DESCRIPTION_3, "成为了高级的冠名料理");
        translationBuilder.add(ItemDescriptionConsts.STINKY_TOFU_DESCRIPTION_0, "少见的黑色豆腐");
        translationBuilder.add(ItemDescriptionConsts.STINKY_TOFU_DESCRIPTION_1, "散发着令人难以接近的味道…让人不禁怀疑这真的可以吃吗？但是实际吃过的人表示根本停不下来");
        translationBuilder.add(ItemDescriptionConsts.COLORFUL_JADE_FRIED_BUNS_DESCRIPTION_0, "散发着七彩的气场的高级生煎包");
        translationBuilder.add(ItemDescriptionConsts.COLORFUL_JADE_FRIED_BUNS_DESCRIPTION_1, "据说有些生煎原教旨主义者尖锐地反对加入猪肉以外食材的做法");
        translationBuilder.add(ItemDescriptionConsts.MAPO_TOFU_DESCRIPTION_0, "在日本很有名的中华料理");
        translationBuilder.add(ItemDescriptionConsts.MAPO_TOFU_DESCRIPTION_1, "使用独特的豆腐烹饪技巧烹制而成的辛辣料理");
        translationBuilder.add(ItemDescriptionConsts.MAPO_TOFU_DESCRIPTION_2, "用它来拌饭吃可是会上瘾的哦");
        translationBuilder.add(ItemDescriptionConsts.BOILED_FISH_DESCRIPTION_0, "正宗的四川中华料理");
        translationBuilder.add(ItemDescriptionConsts.BOILED_FISH_DESCRIPTION_1, "鲜嫩肥美的鱼肉和犹如半天朱霞的辣椒一起翻滚");
        translationBuilder.add(ItemDescriptionConsts.BOILED_FISH_DESCRIPTION_2, "煮成了一道鱼香四溢、椒味袭人的绝味");
        translationBuilder.add(ItemDescriptionConsts.MOON_CAKE_DESCRIPTION_0, "原本是内测人员的特殊食谱");
        translationBuilder.add(ItemDescriptionConsts.MOON_CAKE_DESCRIPTION_1, "三年来一直令全收集强迫症玩家坐卧不宁");
        translationBuilder.add(ItemDescriptionConsts.MOON_CAKE_DESCRIPTION_2, "这次彻底开放了");
        translationBuilder.add(ItemDescriptionConsts.MAOYU_TRICOLOR_ICE_CREAM_DESCRIPTION_0, "方形的三色毛玉冰激凌");
        translationBuilder.add(ItemDescriptionConsts.MAOYU_TRICOLOR_ICE_CREAM_DESCRIPTION_1, "从颜色到口味上都非常惹人喜爱");
        translationBuilder.add(ItemDescriptionConsts.MAOYU_TRICOLOR_ICE_CREAM_DESCRIPTION_2, "几乎是人手一份的招牌甜品");
        translationBuilder.add(ItemDescriptionConsts.MAOYU_LAVA_TOFU_DESCRIPTION_0, "方形的火山毛玉造型");
        translationBuilder.add(ItemDescriptionConsts.MAOYU_LAVA_TOFU_DESCRIPTION_1, "仿佛着了火的熔岩豆腐");
        translationBuilder.add(ItemDescriptionConsts.MAOYU_LAVA_TOFU_DESCRIPTION_2, "受到喜欢舌苔刺激的人的追捧");
        translationBuilder.add(ItemDescriptionConsts.SCARLET_DEVILS_CAKE_DESCRIPTION_0, "以猩红恶魔头上的帽子为原型制作的梦幻甜食");
        translationBuilder.add(ItemDescriptionConsts.SCARLET_DEVILS_CAKE_DESCRIPTION_1, "切开会有仿佛血液一般的甜美酱料流出");
        translationBuilder.add(ItemDescriptionConsts.UNCONSCIOUS_MONSTER_MOUSSE_DESCRIPTION_0, "以无意识妖怪的帽子为原型制作的深沉甜食");
        translationBuilder.add(ItemDescriptionConsts.UNCONSCIOUS_MONSTER_MOUSSE_DESCRIPTION_1, "即使切开感受到的也是无尽的黑暗");
        translationBuilder.add(ItemDescriptionConsts.UNCONSCIOUS_MONSTER_MOUSSE_DESCRIPTION_2, "但香醇的程度令人流连忘返");
        translationBuilder.add(ItemDescriptionConsts.DUMPLING_DESCRIPTION_0, "海的另一边");
        translationBuilder.add(ItemDescriptionConsts.DUMPLING_DESCRIPTION_1, "红美铃小姐家乡的著名食谱");
        translationBuilder.add(ItemDescriptionConsts.DUMPLING_DESCRIPTION_2, "用面粉制作成的筋道面皮");
        translationBuilder.add(ItemDescriptionConsts.DUMPLING_DESCRIPTION_3, "在其中包入任意喜欢的食材");
        translationBuilder.add(ItemDescriptionConsts.DUMPLING_DESCRIPTION_4, "放入沸腾的热水中煮熟");
        translationBuilder.add(ItemDescriptionConsts.DUMPLING_DESCRIPTION_5, "就会成为超级美味的食物");
        translationBuilder.add(ItemDescriptionConsts.DUMPLING_DESCRIPTION_6, "外表看起来朴素无华");
        translationBuilder.add(ItemDescriptionConsts.DUMPLING_DESCRIPTION_7, "内在却包着世间的宝藏");
        translationBuilder.add(ItemDescriptionConsts.GLUTINOUS_RICE_BALLS_DESCRIPTION_0, "海的另一边");
        translationBuilder.add(ItemDescriptionConsts.GLUTINOUS_RICE_BALLS_DESCRIPTION_1, "红美铃小姐家乡的著名食谱");
        translationBuilder.add(ItemDescriptionConsts.GLUTINOUS_RICE_BALLS_DESCRIPTION_2, "用弹牙的糯米揉成小团");
        translationBuilder.add(ItemDescriptionConsts.GLUTINOUS_RICE_BALLS_DESCRIPTION_3, "在其中包入甜品食材");
        translationBuilder.add(ItemDescriptionConsts.GLUTINOUS_RICE_BALLS_DESCRIPTION_4, "放入沸腾的热水中煮熟");
        translationBuilder.add(ItemDescriptionConsts.GLUTINOUS_RICE_BALLS_DESCRIPTION_5, "就会成为超级可口的甜品");
        translationBuilder.add(ItemDescriptionConsts.GLUTINOUS_RICE_BALLS_DESCRIPTION_6, "外表看起来朴素无华");
        translationBuilder.add(ItemDescriptionConsts.GLUTINOUS_RICE_BALLS_DESCRIPTION_7, "内在却包着世间的甜蜜");
        translationBuilder.add(ItemDescriptionConsts.FRIED_SHRIMP_TEMPURA_DESCRIPTION_0, "“天妇罗”又名“天麸罗”");
        translationBuilder.add(ItemDescriptionConsts.FRIED_SHRIMP_TEMPURA_DESCRIPTION_1, "将新鲜大虾裹上面粉做的罗衣");
        translationBuilder.add(ItemDescriptionConsts.FRIED_SHRIMP_TEMPURA_DESCRIPTION_2, "放入油锅炸至金黄酥脆");
        translationBuilder.add(ItemDescriptionConsts.FRIED_SHRIMP_TEMPURA_DESCRIPTION_3, "再控油捞出");
        translationBuilder.add(ItemDescriptionConsts.FRIED_SHRIMP_TEMPURA_DESCRIPTION_4, "隔壁家的童子都馋哭了");
        translationBuilder.add(ItemDescriptionConsts.GOLDEN_CRISPY_FISH_CAKE_DESCRIPTION_0, "在鱼馅儿内加入适量蜂蜜搅拌后");
        translationBuilder.add(ItemDescriptionConsts.GOLDEN_CRISPY_FISH_CAKE_DESCRIPTION_1, "碾压成鱼饼");
        translationBuilder.add(ItemDescriptionConsts.GOLDEN_CRISPY_FISH_CAKE_DESCRIPTION_2, "投入油锅炸至金黄色");
        translationBuilder.add(ItemDescriptionConsts.GOLDEN_CRISPY_FISH_CAKE_DESCRIPTION_3, "随炸随食");
        translationBuilder.add(ItemDescriptionConsts.ALL_MEAT_FEAST_DESCRIPTION_0, "将各种高级烤肉堆成小山的料理");
        translationBuilder.add(ItemDescriptionConsts.ALL_MEAT_FEAST_DESCRIPTION_1, "无论视觉还是分量上都简单暴力");
        translationBuilder.add(ItemDescriptionConsts.ALL_MEAT_FEAST_DESCRIPTION_2, "对于嗅觉灵敏的食客是秒杀级的食谱");
        translationBuilder.add(ItemDescriptionConsts.PICKLED_CUCUMBERS_DESCRIPTION_0, "将黄瓜切段后用盐等调味腌制");
        translationBuilder.add(ItemDescriptionConsts.PICKLED_CUCUMBERS_DESCRIPTION_1, "出现了神奇而难以抗拒的风味");
        translationBuilder.add(ItemDescriptionConsts.BAKED_CRAB_WITH_CREAM_DESCRIPTION_0, "奶油焗的螃蟹在没开盖之前就已经香气四溢");
        translationBuilder.add(ItemDescriptionConsts.BAKED_CRAB_WITH_CREAM_DESCRIPTION_1, "然而这香气只是前戏");
        translationBuilder.add(ItemDescriptionConsts.BAKED_CRAB_WITH_CREAM_DESCRIPTION_2, "撕开蟹钳");
        translationBuilder.add(ItemDescriptionConsts.BAKED_CRAB_WITH_CREAM_DESCRIPTION_3, "弹出白嫩饱满的肉质");
        translationBuilder.add(ItemDescriptionConsts.BAKED_CRAB_WITH_CREAM_DESCRIPTION_4, "此时再细细地吸入汤汁");
        translationBuilder.add(ItemDescriptionConsts.BAKED_CRAB_WITH_CREAM_DESCRIPTION_5, "螃蟹固有的鲜味才彻底的融合和释放");
        translationBuilder.add(ItemDescriptionConsts.PSEUDO_JIRITAMA_DESCRIPTION_0, "据说尻子玉是人类进化后残存的尾骨");
        translationBuilder.add(ItemDescriptionConsts.PSEUDO_JIRITAMA_DESCRIPTION_1, "是河童一族最喜欢的食物");
        translationBuilder.add(ItemDescriptionConsts.PSEUDO_JIRITAMA_DESCRIPTION_2, "从前用血腥的方式从人类那里夺取");
        translationBuilder.add(ItemDescriptionConsts.PSEUDO_JIRITAMA_DESCRIPTION_3, "但是随着时代的进步");
        translationBuilder.add(ItemDescriptionConsts.PSEUDO_JIRITAMA_DESCRIPTION_4, "已经有了料理的方式来替代");
        translationBuilder.add(ItemDescriptionConsts.PSEUDO_JIRITAMA_DESCRIPTION_5, "只是成本昂贵");
        translationBuilder.add(ItemDescriptionConsts.OKONOMIYAKI_DESCRIPTION_0, "听说是外界流行的街边小吃");
        translationBuilder.add(ItemDescriptionConsts.OKONOMIYAKI_DESCRIPTION_1, "使用面糊和各种食材混合后在铁板上烧制而成");
        translationBuilder.add(ItemDescriptionConsts.OKONOMIYAKI_DESCRIPTION_2, "脆香而丰富");
        translationBuilder.add(ItemDescriptionConsts.OKONOMIYAKI_DESCRIPTION_3, "因其百变和平价的特性被所有人喜爱");
        translationBuilder.add(ItemDescriptionConsts.TAKOYAKI_DESCRIPTION_0, "听说也是外界流行的街边小吃");
        translationBuilder.add(ItemDescriptionConsts.TAKOYAKI_DESCRIPTION_1, "在特制的面糊中裹上引发奇迹的章鱼脚");
        translationBuilder.add(ItemDescriptionConsts.TAKOYAKI_DESCRIPTION_2, "糯脆的外衣之下Q弹的章鱼脚产生让人幸福的感觉");
        translationBuilder.add(ItemDescriptionConsts.SEA_URCHIN_SASHIMI_DESCRIPTION_0, "制作意外的简单");
        translationBuilder.add(ItemDescriptionConsts.SEA_URCHIN_SASHIMI_DESCRIPTION_1, "但获取食材却非常困难");
        translationBuilder.add(ItemDescriptionConsts.SEA_URCHIN_SASHIMI_DESCRIPTION_2, "据说是现世流行的超高级料理");
        translationBuilder.add(ItemDescriptionConsts.SEA_URCHIN_SASHIMI_DESCRIPTION_3, "是每个人的梦想");
        translationBuilder.add(ItemDescriptionConsts.MUSHROOM_MEAT_SLICES_DESCRIPTION_0, "将蘑菇和肉切片后");
        translationBuilder.add(ItemDescriptionConsts.MUSHROOM_MEAT_SLICES_DESCRIPTION_1, "混入锅里一起炒");
        translationBuilder.add(ItemDescriptionConsts.MUSHROOM_MEAT_SLICES_DESCRIPTION_2, "是非常基本的家常料理");
        translationBuilder.add(ItemDescriptionConsts.SECRET_MUSHROOM_CASSEROLE_DESCRIPTION_0, "长期专研于蘑菇学的人类魔法使所创的秘制菜谱");
        translationBuilder.add(ItemDescriptionConsts.SECRET_MUSHROOM_CASSEROLE_DESCRIPTION_1, "食材均选自魔法森林所产的新鲜菌类");
        translationBuilder.add(ItemDescriptionConsts.SECRET_MUSHROOM_CASSEROLE_DESCRIPTION_2, "据说较其他地方产出的菌类有更高营养价值");
        translationBuilder.add(ItemDescriptionConsts.MUSHROOM_GIRLS_DANCE_STEW_DESCRIPTION_0, "蘑菇为主角");
        translationBuilder.add(ItemDescriptionConsts.MUSHROOM_GIRLS_DANCE_STEW_DESCRIPTION_1, "佐以各种鲜嫩食材的强力料理");
        translationBuilder.add(ItemDescriptionConsts.MUSHROOM_GIRLS_DANCE_STEW_DESCRIPTION_2, "金灿灿的光芒散发着强烈的存在感");
        translationBuilder.add(ItemDescriptionConsts.MUSHROOM_GIRLS_DANCE_STEW_DESCRIPTION_3, "香浓的口感可以瞬间蒸发人的灵魂");
        translationBuilder.add(ItemDescriptionConsts.MUSHROOM_GIRLS_DANCE_STEW_DESCRIPTION_4, "一发沉沦");
        translationBuilder.add(ItemDescriptionConsts.MILKY_MUSHROOM_SOUP_DESCRIPTION_0, "浓郁的奶香汤底煮出来的魔性之汤");
        translationBuilder.add(ItemDescriptionConsts.MILKY_MUSHROOM_SOUP_DESCRIPTION_1, "尝下第一口就无法停下来");
        translationBuilder.add(ItemDescriptionConsts.MILKY_MUSHROOM_SOUP_DESCRIPTION_2, "制作简单");
        translationBuilder.add(ItemDescriptionConsts.MILKY_MUSHROOM_SOUP_DESCRIPTION_3, "材料随处可见");
        translationBuilder.add(ItemDescriptionConsts.MILKY_MUSHROOM_SOUP_DESCRIPTION_4, "还拥有极高的营养价值");
        translationBuilder.add(ItemDescriptionConsts.ORDINARY_SMALL_CAKE_DESCRIPTION_0, "有着惊人热量的小点心");
        translationBuilder.add(ItemDescriptionConsts.ORDINARY_SMALL_CAKE_DESCRIPTION_1, "据说能迅速强健体魄");
        translationBuilder.add(ItemDescriptionConsts.ORDINARY_SMALL_CAKE_DESCRIPTION_2, "上面写着“吃掉我”");
        translationBuilder.add(ItemDescriptionConsts.ORDINARY_SMALL_CAKE_DESCRIPTION_3, "非常诱人");
        translationBuilder.add(ItemDescriptionConsts.ORDINARY_SMALL_CAKE_DESCRIPTION_4, "吃多了估计会长胖吧…");
        translationBuilder.add(ItemDescriptionConsts.SEVEN_COLORED_YOKAN_DESCRIPTION_0, "经典甜食辅以特殊的处理");
        translationBuilder.add(ItemDescriptionConsts.SEVEN_COLORED_YOKAN_DESCRIPTION_1, "呈现梦幻的色彩");
        translationBuilder.add(ItemDescriptionConsts.SEVEN_COLORED_YOKAN_DESCRIPTION_2, "让人敬畏");
        translationBuilder.add(ItemDescriptionConsts.SEVEN_COLORED_YOKAN_DESCRIPTION_3, "只敢远观不敢亵玩");
        translationBuilder.add(ItemDescriptionConsts.SEVEN_COLORED_YOKAN_DESCRIPTION_4, "能吃上一次");
        translationBuilder.add(ItemDescriptionConsts.SEVEN_COLORED_YOKAN_DESCRIPTION_5, "终生难忘");
        translationBuilder.add(ItemDescriptionConsts.NIGIRI_SUSHI_DESCRIPTION_0, "日本最传统的料理之一");
        translationBuilder.add(ItemDescriptionConsts.NIGIRI_SUSHI_DESCRIPTION_1, "将鱼切片后盖在手握的饭团上");
        translationBuilder.add(ItemDescriptionConsts.NIGIRI_SUSHI_DESCRIPTION_2, "解饿又鲜美");
        translationBuilder.add(ItemDescriptionConsts.NIGIRI_SUSHI_DESCRIPTION_3, "拥有很长的历史");
        translationBuilder.add(ItemDescriptionConsts.PUMPKIN_SHRIMP_CAKE_DESCRIPTION_0, "掏空小南瓜");
        translationBuilder.add(ItemDescriptionConsts.PUMPKIN_SHRIMP_CAKE_DESCRIPTION_1, "用鲜嫩的虾肉和豆腐填充");
        translationBuilder.add(ItemDescriptionConsts.PUMPKIN_SHRIMP_CAKE_DESCRIPTION_2, "再进行蒸制");
        translationBuilder.add(ItemDescriptionConsts.PUMPKIN_SHRIMP_CAKE_DESCRIPTION_3, "香甜可口");
        translationBuilder.add(ItemDescriptionConsts.PUMPKIN_SHRIMP_CAKE_DESCRIPTION_4, "又非常健康");
        translationBuilder.add(ItemDescriptionConsts.GENSOKYO_BUDDHA_JUMPS_OVER_THE_WALL_DESCRIPTION_0, "由东方文明古国最强料理改造而来");
        translationBuilder.add(ItemDescriptionConsts.GENSOKYO_BUDDHA_JUMPS_OVER_THE_WALL_DESCRIPTION_1, "据说得道的真佛也会因为它的气味夺墙而走");
        translationBuilder.add(ItemDescriptionConsts.GENSOKYO_BUDDHA_JUMPS_OVER_THE_WALL_DESCRIPTION_2, "摒弃斋戒");
        translationBuilder.add(ItemDescriptionConsts.GENSOKYO_BUDDHA_JUMPS_OVER_THE_WALL_DESCRIPTION_3, "真的很神奇！");
        translationBuilder.add(ItemDescriptionConsts.DEPRESSED_CHEESE_STICKS_DESCRIPTION_0, "在一些阴郁的妖怪中流行的零食");
        translationBuilder.add(ItemDescriptionConsts.DEPRESSED_CHEESE_STICKS_DESCRIPTION_1, "在浓郁的芝士中混有白果独有的苦味");
        translationBuilder.add(ItemDescriptionConsts.DEPRESSED_CHEESE_STICKS_DESCRIPTION_2, "能让味觉产生强烈的回甘后味");
        translationBuilder.add(ItemDescriptionConsts.DEPRESSED_CHEESE_STICKS_DESCRIPTION_3, "但我完全无法理解");
        translationBuilder.add(ItemDescriptionConsts.GLOOMY_FRUIT_PIE_DESCRIPTION_0, "对喜欢酸味的人来说是味蕾的盛宴");
        translationBuilder.add(ItemDescriptionConsts.GLOOMY_FRUIT_PIE_DESCRIPTION_1, "但不喜欢酸味的人就是牙齿和舌头的炸弹！只在一些小众的妖怪中流行的奇怪做法");
        translationBuilder.add(ItemDescriptionConsts.SCREAMING_ODEN_DESCRIPTION_0, "旧地狱中最流行的聚会小吃");
        translationBuilder.add(ItemDescriptionConsts.SCREAMING_ODEN_DESCRIPTION_1, "将各种食材置入后");
        translationBuilder.add(ItemDescriptionConsts.SCREAMING_ODEN_DESCRIPTION_2, "加上辣椒刺激");
        translationBuilder.add(ItemDescriptionConsts.SCREAMING_ODEN_DESCRIPTION_3, "让人汗流满面的同时却无法停下");
        translationBuilder.add(ItemDescriptionConsts.SCREAMING_ODEN_DESCRIPTION_4, "是魔力十足的料理");
        translationBuilder.add(ItemDescriptionConsts.CRISP_CYCLONE_DESCRIPTION_0, "将虫类的甲壳磨成大碎块");
        translationBuilder.add(ItemDescriptionConsts.CRISP_CYCLONE_DESCRIPTION_1, "拌入面中");
        translationBuilder.add(ItemDescriptionConsts.CRISP_CYCLONE_DESCRIPTION_2, "吃起来香脆又下火");
        translationBuilder.add(ItemDescriptionConsts.CRISP_CYCLONE_DESCRIPTION_3, "有一种别样的异世界猎奇感的奇怪料理");
        translationBuilder.add(ItemDescriptionConsts.LOOKING_UP_AT_THE_CEILING_FRUIT_PIE_DESCRIPTION_0, "在水果派里探出一个鱼头");
        translationBuilder.add(ItemDescriptionConsts.LOOKING_UP_AT_THE_CEILING_FRUIT_PIE_DESCRIPTION_1, "仿佛看着地底的天花板");
        translationBuilder.add(ItemDescriptionConsts.LOOKING_UP_AT_THE_CEILING_FRUIT_PIE_DESCRIPTION_2, "充满了和地狱相衬的绝望气息");
        translationBuilder.add(ItemDescriptionConsts.BEETLE_STEAMED_CAKE_DESCRIPTION_0, "无论是地上还是地下");
        translationBuilder.add(ItemDescriptionConsts.BEETLE_STEAMED_CAKE_DESCRIPTION_1, "兜角甲虫都是力量和无敌的象征！这是憧憬它的力量而诞生的盔甲料理！");
        translationBuilder.add(ItemDescriptionConsts.LION_HEAD_DESCRIPTION_0, "爱酒之人无不喜欢的基础下酒菜！气派又野性的狮子头配上烈性的酒");
        translationBuilder.add(ItemDescriptionConsts.LION_HEAD_DESCRIPTION_1, "是鬼族起床的早餐！");
        translationBuilder.add(ItemDescriptionConsts.GIANT_TAMAGOYAKI_DESCRIPTION_0, "将普通的玉子烧做大许多倍");
        translationBuilder.add(ItemDescriptionConsts.GIANT_TAMAGOYAKI_DESCRIPTION_1, "就是这款在鬼族中流行的巨人玉子烧了！其澎湃的存在感让豪迈之人为其燃烧！");
        translationBuilder.add(ItemDescriptionConsts.OEDO_BOAT_FESTIVAL_DESCRIPTION_0, "用华丽的祭典船造型");
        translationBuilder.add(ItemDescriptionConsts.OEDO_BOAT_FESTIVAL_DESCRIPTION_1, "摆满上好的鱼刺身");
        translationBuilder.add(ItemDescriptionConsts.OEDO_BOAT_FESTIVAL_DESCRIPTION_2, "周围散发着保鲜而制作的冰雾");
        translationBuilder.add(ItemDescriptionConsts.OEDO_BOAT_FESTIVAL_DESCRIPTION_3, "是真真正正的宴会的焦点！");
        translationBuilder.add(ItemDescriptionConsts.SAKURA_PUDDING_DESCRIPTION_0, "粉色的可爱甜品");
        translationBuilder.add(ItemDescriptionConsts.SAKURA_PUDDING_DESCRIPTION_1, "Q软又富有弹性");
        translationBuilder.add(ItemDescriptionConsts.SAKURA_PUDDING_DESCRIPTION_2, "香蜜的甜美气息使它成为世界上所有的女孩子都无法拒绝的无敌甜品");
        translationBuilder.add(ItemDescriptionConsts.REFRESHING_PUDDING_DESCRIPTION_0, "大大提升了酸度的可怕布丁");
        translationBuilder.add(ItemDescriptionConsts.REFRESHING_PUDDING_DESCRIPTION_1, "改良了提神的效果");
        translationBuilder.add(ItemDescriptionConsts.REFRESHING_PUDDING_DESCRIPTION_2, "只要吃一口");
        translationBuilder.add(ItemDescriptionConsts.REFRESHING_PUDDING_DESCRIPTION_3, "就能让困倦的身体打起精神");
        translationBuilder.add(ItemDescriptionConsts.REFRESHING_PUDDING_DESCRIPTION_4, "但因为其用料过于凶猛");
        translationBuilder.add(ItemDescriptionConsts.REFRESHING_PUDDING_DESCRIPTION_5, "很不利于口腔健康…");
        translationBuilder.add(ItemDescriptionConsts.BURNT_PUDDING_DESCRIPTION_0, "究极加料、一颗就调动起身上包括多巴胺和肾上腺激素等多种兴奋元素疯狂舞动的禁忌甜食");
        translationBuilder.add(ItemDescriptionConsts.BURNT_PUDDING_DESCRIPTION_1, "妖怪食用后可以疯狂舞蹈一整夜");
        translationBuilder.add(ItemDescriptionConsts.BURNT_PUDDING_DESCRIPTION_2, "但是兴奋过后会不由地感到“我燃尽了”");
        translationBuilder.add(ItemDescriptionConsts.CAT_FOOD_DESCRIPTION_0, "据说是阿燐刚刚被觉收养时");
        translationBuilder.add(ItemDescriptionConsts.CAT_FOOD_DESCRIPTION_1, "觉常做给她的简易盖浇饭");
        translationBuilder.add(ItemDescriptionConsts.CAT_FOOD_DESCRIPTION_2, "面粉勾芡后淋在鱼肉上");
        translationBuilder.add(ItemDescriptionConsts.CAT_FOOD_DESCRIPTION_3, "那种温柔的味道一直留在燐的记忆里");
        translationBuilder.add(ItemDescriptionConsts.SALMON_TEMPURA_DESCRIPTION_0, "将三文鱼裹上蛋液和面粉");
        translationBuilder.add(ItemDescriptionConsts.SALMON_TEMPURA_DESCRIPTION_1, "炸至通体金黄");
        translationBuilder.add(ItemDescriptionConsts.SALMON_TEMPURA_DESCRIPTION_2, "咬一口汁水四溢");
        translationBuilder.add(ItemDescriptionConsts.SALMON_TEMPURA_DESCRIPTION_3, "隔壁的猫猫都馋哭啦！");
        translationBuilder.add(ItemDescriptionConsts.FISH_LEAPS_OVER_DRAGON_GATE_DESCRIPTION_0, "外表是鲤鱼起跳的造型");
        translationBuilder.add(ItemDescriptionConsts.FISH_LEAPS_OVER_DRAGON_GATE_DESCRIPTION_1, "剖开后尽是梦幻的宝藏");
        translationBuilder.add(ItemDescriptionConsts.FISH_LEAPS_OVER_DRAGON_GATE_DESCRIPTION_2, "是猫科动物无法抵抗的究极美食");
        translationBuilder.add(ItemDescriptionConsts.CHEESE_EGG_DESCRIPTION_0, "据说是阿空刚刚被觉收养时");
        translationBuilder.add(ItemDescriptionConsts.CHEESE_EGG_DESCRIPTION_1, "觉常做给她的料理");
        translationBuilder.add(ItemDescriptionConsts.CHEESE_EGG_DESCRIPTION_2, "在蛋饼里混入浓香的芝士");
        translationBuilder.add(ItemDescriptionConsts.CHEESE_EGG_DESCRIPTION_3, "让人无法拒绝的小吃");
        translationBuilder.add(ItemDescriptionConsts.ONE_HIT_KILL_DESCRIPTION_0, "加入了极为刺激的食材和辅料");
        translationBuilder.add(ItemDescriptionConsts.ONE_HIT_KILL_DESCRIPTION_1, "对味觉的刺激满点的超级烤串");
        translationBuilder.add(ItemDescriptionConsts.ONE_HIT_KILL_DESCRIPTION_2, "阿空亲自为它起了一个中二度满满的名字");
        translationBuilder.add(ItemDescriptionConsts.ONE_HIT_KILL_DESCRIPTION_3, "结果在保持野性的妖怪中莫名的有人气");
        translationBuilder.add(ItemDescriptionConsts.HELL_THRILL_WARNING_DESCRIPTION_0, "超超超级辣加倍的牛肉咖喱饭！据说只有拥有极限忍耐力的人和傻瓜才会尝试这道料理！");
        translationBuilder.add(ItemDescriptionConsts.BAKED_SWEET_POTATOES_DESCRIPTION_0, "无论什么时候都大受欢迎的民间小吃");
        translationBuilder.add(ItemDescriptionConsts.BAKED_SWEET_POTATOES_DESCRIPTION_1, "尤其在寒冷的冬天");
        translationBuilder.add(ItemDescriptionConsts.BAKED_SWEET_POTATOES_DESCRIPTION_2, "看到热气腾腾的烤炉");
        translationBuilder.add(ItemDescriptionConsts.BAKED_SWEET_POTATOES_DESCRIPTION_3, "想到那红皮黄瓤的颜色");
        translationBuilder.add(ItemDescriptionConsts.BAKED_SWEET_POTATOES_DESCRIPTION_4, "热乎甜软的口感");
        translationBuilder.add(ItemDescriptionConsts.BAKED_SWEET_POTATOES_DESCRIPTION_5, "谁能忍得住呢？但不能贪嘴");
        translationBuilder.add(ItemDescriptionConsts.BAKED_SWEET_POTATOES_DESCRIPTION_6, "吃太多容易导致胃腹不适");
        translationBuilder.add(ItemDescriptionConsts.SKINNY_HORSE_DUMPLING_DESCRIPTION_0, "外表看起来像是拉长了的团子");
        translationBuilder.add(ItemDescriptionConsts.SKINNY_HORSE_DUMPLING_DESCRIPTION_1, "切开后中间裹着各式各样的图案");
        translationBuilder.add(ItemDescriptionConsts.SKINNY_HORSE_DUMPLING_DESCRIPTION_2, "大大增加了团子的新鲜感");
        translationBuilder.add(ItemDescriptionConsts.SKINNY_HORSE_DUMPLING_DESCRIPTION_3, "据说瘦马团子象征佛舍利");
        translationBuilder.add(ItemDescriptionConsts.SKINNY_HORSE_DUMPLING_DESCRIPTION_4, "那不就是佛祖的骨灰吗？");
        translationBuilder.add(ItemDescriptionConsts.FRIGHT_ADVENTURE_DESCRIPTION_0, "从幻昙华中提取色素");
        translationBuilder.add(ItemDescriptionConsts.FRIGHT_ADVENTURE_DESCRIPTION_1, "给一个个蘑菇伞染上颜色");
        translationBuilder.add(ItemDescriptionConsts.FRIGHT_ADVENTURE_DESCRIPTION_2, "将之铺在宝箱周围就像被锦花簇拥着");
        translationBuilder.add(ItemDescriptionConsts.FRIGHT_ADVENTURE_DESCRIPTION_3, "宝箱里究竟藏有什么惊喜呢？怀着这样的想法打开");
        translationBuilder.add(ItemDescriptionConsts.FRIGHT_ADVENTURE_DESCRIPTION_4, "跳出来的却是伸着大舌头的滑稽的伞！吓一跳了吧——？");
        translationBuilder.add(ItemDescriptionConsts.BISCAY_BISCUITS_DESCRIPTION_0, "从海难中逃到荒岛的幸存者");
        translationBuilder.add(ItemDescriptionConsts.BISCAY_BISCUITS_DESCRIPTION_1, "万般无奈地将已被海水浸湿的面粉和芝士混合成的“面糊”放在阳光下烤");
        translationBuilder.add(ItemDescriptionConsts.BISCAY_BISCUITS_DESCRIPTION_2, "没想到味道变得十分香脆可口");
        translationBuilder.add(ItemDescriptionConsts.BISCAY_BISCUITS_DESCRIPTION_3, "于是这个做法流传了下来");
        translationBuilder.add(ItemDescriptionConsts.PIRATE_BACON_DESCRIPTION_0, "据说“海盗”这个词的来源是在明火上烹制的熏肉");
        translationBuilder.add(ItemDescriptionConsts.PIRATE_BACON_DESCRIPTION_1, "加勒比本地人就是用这种做法来处理肉类");
        translationBuilder.add(ItemDescriptionConsts.PIRATE_BACON_DESCRIPTION_2, "然后卖给海盗");
        translationBuilder.add(ItemDescriptionConsts.PIRATE_BACON_DESCRIPTION_3, "听起来就和米饭盖浇米饭一样奇怪");
        translationBuilder.add(ItemDescriptionConsts.LUOHAN_VEGETARIAN_DESCRIPTION_0, "源自佛门的斋菜");
        translationBuilder.add(ItemDescriptionConsts.LUOHAN_VEGETARIAN_DESCRIPTION_1, "传闻正宗的罗汉斋用十八种原料制成");
        translationBuilder.add(ItemDescriptionConsts.LUOHAN_VEGETARIAN_DESCRIPTION_2, "工序复杂考究");
        translationBuilder.add(ItemDescriptionConsts.LUOHAN_VEGETARIAN_DESCRIPTION_3, "成菜色泽缤纷雅致");
        translationBuilder.add(ItemDescriptionConsts.LUOHAN_VEGETARIAN_DESCRIPTION_4, "味道清淡香郁");
        translationBuilder.add(ItemDescriptionConsts.LUOHAN_VEGETARIAN_DESCRIPTION_5, "堪称佛门最奢华的一道素菜");
        translationBuilder.add(ItemDescriptionConsts.YUNSHAN_COTTON_CANDY_DESCRIPTION_0, "命莲寺的弟子中有个能够变幻形态的入道");
        translationBuilder.add(ItemDescriptionConsts.YUNSHAN_COTTON_CANDY_DESCRIPTION_1, "只要见过他的样子");
        translationBuilder.add(ItemDescriptionConsts.YUNSHAN_COTTON_CANDY_DESCRIPTION_2, "就很容易联想到棉花糖");
        translationBuilder.add(ItemDescriptionConsts.YUNSHAN_COTTON_CANDY_DESCRIPTION_3, "加入桃汁制作的棉花糖甜而不腻");
        translationBuilder.add(ItemDescriptionConsts.YUNSHAN_COTTON_CANDY_DESCRIPTION_4, "造型生动有趣");
        translationBuilder.add(ItemDescriptionConsts.YUNSHAN_COTTON_CANDY_DESCRIPTION_5, "深受小孩子的喜爱");
        translationBuilder.add(ItemDescriptionConsts.HOLY_WHITE_LOTUS_SEED_CAKE_DESCRIPTION_0, "将新鲜莲子剥壳去芯");
        translationBuilder.add(ItemDescriptionConsts.HOLY_WHITE_LOTUS_SEED_CAKE_DESCRIPTION_1, "煮至软烂");
        translationBuilder.add(ItemDescriptionConsts.HOLY_WHITE_LOTUS_SEED_CAKE_DESCRIPTION_2, "再将黄油和面粉搅拌均匀");
        translationBuilder.add(ItemDescriptionConsts.HOLY_WHITE_LOTUS_SEED_CAKE_DESCRIPTION_3, "最后混合翻炒");
        translationBuilder.add(ItemDescriptionConsts.HOLY_WHITE_LOTUS_SEED_CAKE_DESCRIPTION_4, "用磨具压出美丽的莲花图案");
        translationBuilder.add(ItemDescriptionConsts.HOLY_WHITE_LOTUS_SEED_CAKE_DESCRIPTION_5, "看起来神圣洁白");
        translationBuilder.add(ItemDescriptionConsts.GENSOKYO_STAR_LOTUS_SHIP_DESCRIPTION_0, "以南瓜做的船");
        translationBuilder.add(ItemDescriptionConsts.GENSOKYO_STAR_LOTUS_SHIP_DESCRIPTION_1, "承载着如梦似幻的食材");
        translationBuilder.add(ItemDescriptionConsts.GENSOKYO_STAR_LOTUS_SHIP_DESCRIPTION_2, "在莲子铺成的河上驶入幻想");
        translationBuilder.add(ItemDescriptionConsts.GENSOKYO_STAR_LOTUS_SHIP_DESCRIPTION_3, "据说每每享用完这道料理");
        translationBuilder.add(ItemDescriptionConsts.GENSOKYO_STAR_LOTUS_SHIP_DESCRIPTION_4, "都会有如梦方醒的感觉");
        translationBuilder.add(ItemDescriptionConsts.GENSOKYO_STAR_LOTUS_SHIP_DESCRIPTION_5, "至于究竟是何种梦境");
        translationBuilder.add(ItemDescriptionConsts.GENSOKYO_STAR_LOTUS_SHIP_DESCRIPTION_6, "便是因人而异了");
        translationBuilder.add(ItemDescriptionConsts.PINE_NUT_CAKE_DESCRIPTION_0, "以糯米为主料、辅以松子制作的药膳");
        translationBuilder.add(ItemDescriptionConsts.PINE_NUT_CAKE_DESCRIPTION_1, "松子糕的粉质细腻");
        translationBuilder.add(ItemDescriptionConsts.PINE_NUT_CAKE_DESCRIPTION_2, "柔软可口");
        translationBuilder.add(ItemDescriptionConsts.PINE_NUT_CAKE_DESCRIPTION_3, "并有清香的松子味");
        translationBuilder.add(ItemDescriptionConsts.PINE_NUT_CAKE_DESCRIPTION_4, "深受道士们喜爱");
        translationBuilder.add(ItemDescriptionConsts.SHIRAGA_SADAMATSU_DESCRIPTION_0, "白鹿和松树都有长寿的寓意");
        translationBuilder.add(ItemDescriptionConsts.SHIRAGA_SADAMATSU_DESCRIPTION_1, "深受追求长生的道教人士的推崇");
        translationBuilder.add(ItemDescriptionConsts.SHIRAGA_SADAMATSU_DESCRIPTION_2, "于是有“鹿寿松贞”之画");
        translationBuilder.add(ItemDescriptionConsts.SHIRAGA_SADAMATSU_DESCRIPTION_3, "即一只白鹿立于松树之下");
        translationBuilder.add(ItemDescriptionConsts.SHIRAGA_SADAMATSU_DESCRIPTION_4, "这道菜就是以这幅画为印象创作出来的");
        translationBuilder.add(ItemDescriptionConsts.TAICHI_BAGUA_FISH_MAW_DESCRIPTION_0, "做法极其讲究的道教经典名菜");
        translationBuilder.add(ItemDescriptionConsts.TAICHI_BAGUA_FISH_MAW_DESCRIPTION_1, "鱼肚片片");
        translationBuilder.add(ItemDescriptionConsts.TAICHI_BAGUA_FISH_MAW_DESCRIPTION_2, "要越薄越好；太极图则要注意造型圆整、八卦形等距");
        translationBuilder.add(ItemDescriptionConsts.TAICHI_BAGUA_FISH_MAW_DESCRIPTION_3, "最后再经过细心蒸煮");
        translationBuilder.add(ItemDescriptionConsts.TAICHI_BAGUA_FISH_MAW_DESCRIPTION_4, "才能呈现出这道形象生动、鱼肚软糯的道家名菜");
        translationBuilder.add(ItemDescriptionConsts.CANDIED_CHESTNUTS_DESCRIPTION_0, "把栗子用蜂蜜熬煮之后的成品");
        translationBuilder.add(ItemDescriptionConsts.CANDIED_CHESTNUTS_DESCRIPTION_1, "栗子里温和而浓郁的味道");
        translationBuilder.add(ItemDescriptionConsts.CANDIED_CHESTNUTS_DESCRIPTION_2, "可以有效地平衡外在的甜味");
        translationBuilder.add(ItemDescriptionConsts.TIANSHI_BRAISED_CHESTNUT_MUSHROOMS_DESCRIPTION_0, "神灵庙位处仙界");
        translationBuilder.add(ItemDescriptionConsts.TIANSHI_BRAISED_CHESTNUT_MUSHROOMS_DESCRIPTION_1, "所栽种的栗树据说都蕴含仙气");
        translationBuilder.add(ItemDescriptionConsts.TIANSHI_BRAISED_CHESTNUT_MUSHROOMS_DESCRIPTION_2, "但我是分辨不出啦…将栗子佐以蘑菇焖煮");
        translationBuilder.add(ItemDescriptionConsts.TIANSHI_BRAISED_CHESTNUT_MUSHROOMS_DESCRIPTION_3, "把看不见摸不着的仙气浓缩成一锅鲜甜味美又解馋的杂烩");
        translationBuilder.add(ItemDescriptionConsts.TIANSHI_BRAISED_CHESTNUT_MUSHROOMS_DESCRIPTION_4, "岂不是更实在？");
        translationBuilder.add(ItemDescriptionConsts.LOTUS_FISH_RICE_BOWL_DESCRIPTION_0, "洁白的盘中铺上碧绿的荷叶");
        translationBuilder.add(ItemDescriptionConsts.LOTUS_FISH_RICE_BOWL_DESCRIPTION_1, "叶上还滚动着晶莹的水珠");
        translationBuilder.add(ItemDescriptionConsts.LOTUS_FISH_RICE_BOWL_DESCRIPTION_2, "盘中央一朵白荷则增添了“仙气”");
        translationBuilder.add(ItemDescriptionConsts.LOTUS_FISH_RICE_BOWL_DESCRIPTION_3, "每一盏里都以鲜嫩的粉色荷花花瓣为底");
        translationBuilder.add(ItemDescriptionConsts.LOTUS_FISH_RICE_BOWL_DESCRIPTION_4, "盛着极上金枪鱼、莲子组成的“鱼米盏”");
        translationBuilder.add(ItemDescriptionConsts.LOTUS_FISH_RICE_BOWL_DESCRIPTION_5, "又好吃又健康");
        translationBuilder.add(ItemDescriptionConsts.CANDIED_SWEET_POTATO_DESCRIPTION_0, "将番薯下锅炸至金黄");
        translationBuilder.add(ItemDescriptionConsts.CANDIED_SWEET_POTATO_DESCRIPTION_1, "再裹上能拉出细丝的糖衣");
        translationBuilder.add(ItemDescriptionConsts.CANDIED_SWEET_POTATO_DESCRIPTION_2, "最后在抹过油的盘子上滚一圈");
        translationBuilder.add(ItemDescriptionConsts.CANDIED_SWEET_POTATO_DESCRIPTION_3, "既可口又不粘牙");
        translationBuilder.add(ItemDescriptionConsts.CANDIED_SWEET_POTATO_DESCRIPTION_4, "但不能贪嘴");
        translationBuilder.add(ItemDescriptionConsts.CANDIED_SWEET_POTATO_DESCRIPTION_5, "吃太多容易导致胃腹不适");
        translationBuilder.add(ItemDescriptionConsts.PAN_FRIED_MUSHROOM_MEAT_ROLL_DESCRIPTION_0, "荤素搭配均匀");
        translationBuilder.add(ItemDescriptionConsts.PAN_FRIED_MUSHROOM_MEAT_ROLL_DESCRIPTION_1, "味道也很好的常见佳肴");
        translationBuilder.add(ItemDescriptionConsts.PAN_FRIED_MUSHROOM_MEAT_ROLL_DESCRIPTION_2, "用两种不同口感的菇类");
        translationBuilder.add(ItemDescriptionConsts.PAN_FRIED_MUSHROOM_MEAT_ROLL_DESCRIPTION_3, "包裹着精挑细选的嫩肉");
        translationBuilder.add(ItemDescriptionConsts.PAN_FRIED_MUSHROOM_MEAT_ROLL_DESCRIPTION_4, "给味蕾带来层次感十足的享受");
        translationBuilder.add(ItemDescriptionConsts.ASSORTED_TEMPURA_DESCRIPTION_0, "谁说炸物就必须得是鸟类呢？地上跑的、土里长的、水里游的都可以裹上面粉放到油锅里炸一炸");
        translationBuilder.add(ItemDescriptionConsts.ASSORTED_TEMPURA_DESCRIPTION_1, "出锅皆是香味四溢、酥脆爽口");
        translationBuilder.add(ItemDescriptionConsts.ASSORTED_TEMPURA_DESCRIPTION_2, "最后以梦幻的月光草为缀");
        translationBuilder.add(ItemDescriptionConsts.ASSORTED_TEMPURA_DESCRIPTION_3, "巧妙地中和了炸物拼盘的油腻");
        translationBuilder.add(ItemDescriptionConsts.FRIED_TOMATO_STRIPS_DESCRIPTION_0, "把西红柿裹上面粉后放到油锅炸一炸");
        translationBuilder.add(ItemDescriptionConsts.FRIED_TOMATO_STRIPS_DESCRIPTION_1, "出锅后淋上自制土豆酱");
        translationBuilder.add(ItemDescriptionConsts.FRIED_TOMATO_STRIPS_DESCRIPTION_2, "尝起来也算是别有一番趣味");
        translationBuilder.add(ItemDescriptionConsts.BRAISED_PORK_WITH_PEACH_DESCRIPTION_0, "软糯的肉加上香甜的桃子");
        translationBuilder.add(ItemDescriptionConsts.BRAISED_PORK_WITH_PEACH_DESCRIPTION_1, "即使白嘴吃也不会觉得腻");
        translationBuilder.add(ItemDescriptionConsts.BRAISED_PORK_WITH_PEACH_DESCRIPTION_2, "淋上蜂蜜一起翻炒");
        translationBuilder.add(ItemDescriptionConsts.BRAISED_PORK_WITH_PEACH_DESCRIPTION_3, "更是红润添香");
        translationBuilder.add(ItemDescriptionConsts.BRAISED_PORK_WITH_PEACH_DESCRIPTION_4, "非常适合下酒");
        translationBuilder.add(ItemDescriptionConsts.REVERSING_THE_WORLD_DESCRIPTION_0, "使用革新技术制作的分子料理");
        translationBuilder.add(ItemDescriptionConsts.REVERSING_THE_WORLD_DESCRIPTION_1, "据说是来自月都的食谱");
        translationBuilder.add(ItemDescriptionConsts.REVERSING_THE_WORLD_DESCRIPTION_2, "在制作过程中存在许多无法理解之处");
        translationBuilder.add(ItemDescriptionConsts.REVERSING_THE_WORLD_DESCRIPTION_3, "所以经过一定程度的再创作");
        translationBuilder.add(ItemDescriptionConsts.REVERSING_THE_WORLD_DESCRIPTION_4, "最终成为这样一个结合了世人眼中的“雅”和“俗”之物的地上料理");
        translationBuilder.add(ItemDescriptionConsts.REVERSING_THE_WORLD_DESCRIPTION_5, "也寄托着正邪想要搅混天下的意愿");
        translationBuilder.add(ItemDescriptionConsts.RED_BEAN_DAIFUKU_DESCRIPTION_0, "用糯米制成的外皮");
        translationBuilder.add(ItemDescriptionConsts.RED_BEAN_DAIFUKU_DESCRIPTION_1, "里头包着饱满的带皮红小豆馅儿");
        translationBuilder.add(ItemDescriptionConsts.RED_BEAN_DAIFUKU_DESCRIPTION_2, "馅料的量跟饼皮的量一样甚至更多");
        translationBuilder.add(ItemDescriptionConsts.RED_BEAN_DAIFUKU_DESCRIPTION_3, "使得大福的外型圆浑有致");
        translationBuilder.add(ItemDescriptionConsts.RED_BEAN_DAIFUKU_DESCRIPTION_4, "据说大福就因为这样的外型而被称为“大腹饼”");
        translationBuilder.add(ItemDescriptionConsts.RED_BEAN_DAIFUKU_DESCRIPTION_5, "后人取其吉祥的谐音改称“大福”");
        translationBuilder.add(ItemDescriptionConsts.DORAYAKI_DESCRIPTION_0, "一种烤制面皮、内置红豆沙夹心的甜点");
        translationBuilder.add(ItemDescriptionConsts.DORAYAKI_DESCRIPTION_1, "因由两块像铜锣一样的饼合起来的");
        translationBuilder.add(ItemDescriptionConsts.DORAYAKI_DESCRIPTION_2, "故而得名铜锣烧");
        translationBuilder.add(ItemDescriptionConsts.THE_BEAUTY_OF_HAN_PALACE_DESCRIPTION_0, "用豆腐的洁白来形容貂婵的纯洁");
        translationBuilder.add(ItemDescriptionConsts.THE_BEAUTY_OF_HAN_PALACE_DESCRIPTION_1, "以泥鳅的钻营来影射董卓的奸滑");
        translationBuilder.add(ItemDescriptionConsts.THE_BEAUTY_OF_HAN_PALACE_DESCRIPTION_2, "让人在品尝中");
        translationBuilder.add(ItemDescriptionConsts.THE_BEAUTY_OF_HAN_PALACE_DESCRIPTION_3, "想到王允献貂婵");
        translationBuilder.add(ItemDescriptionConsts.THE_BEAUTY_OF_HAN_PALACE_DESCRIPTION_4, "巧使美人计而除奸贼董卓的故事");
        translationBuilder.add(ItemDescriptionConsts.THE_BEAUTY_OF_HAN_PALACE_DESCRIPTION_5, "自然为美食增添了文化的含量");
        translationBuilder.add(ItemDescriptionConsts.BAMBOO_SHOOTS_STEWED_IN_STONE_POT_DESCRIPTION_0, "以牛肉、竹笋为主要食材的一道家常菜品");
        translationBuilder.add(ItemDescriptionConsts.BAMBOO_SHOOTS_STEWED_IN_STONE_POT_DESCRIPTION_1, "鲜嫩的竹笋炖肉具有开胃、促进消化的作用");
        translationBuilder.add(ItemDescriptionConsts.BAMBOO_SHOOTS_STEWED_IN_STONE_POT_DESCRIPTION_2, "再以竹子作为摆盘");
        translationBuilder.add(ItemDescriptionConsts.BAMBOO_SHOOTS_STEWED_IN_STONE_POT_DESCRIPTION_3, "可谓是色香味俱全");
        translationBuilder.add(ItemDescriptionConsts.BAMBOO_TUBE_STEAMED_PORK_DESCRIPTION_0, "将猪身上最嫩的部位");
        translationBuilder.add(ItemDescriptionConsts.BAMBOO_TUBE_STEAMED_PORK_DESCRIPTION_1, "以新鲜的竹筒锯留成节笆");
        translationBuilder.add(ItemDescriptionConsts.BAMBOO_TUBE_STEAMED_PORK_DESCRIPTION_2, "通小孔");
        translationBuilder.add(ItemDescriptionConsts.BAMBOO_TUBE_STEAMED_PORK_DESCRIPTION_3, "灌上菜料和香料");
        translationBuilder.add(ItemDescriptionConsts.BAMBOO_TUBE_STEAMED_PORK_DESCRIPTION_4, "开大火蒸熟后飘出竹子的独特清香");
        translationBuilder.add(ItemDescriptionConsts.BAMBOO_TUBE_STEAMED_PORK_DESCRIPTION_5, "有时间的条件下");
        translationBuilder.add(ItemDescriptionConsts.BAMBOO_TUBE_STEAMED_PORK_DESCRIPTION_6, "在腌渍肉片的时候加点清水会更加晶莹润口");
        translationBuilder.add(ItemDescriptionConsts.GREEN_BAMBOO_WELCOMES_SPRING_DESCRIPTION_0, "将各种鲜嫩的食材放入多节竹筒蒸熟");
        translationBuilder.add(ItemDescriptionConsts.GREEN_BAMBOO_WELCOMES_SPRING_DESCRIPTION_1, "寓意节节高升");
        translationBuilder.add(ItemDescriptionConsts.GREEN_BAMBOO_WELCOMES_SPRING_DESCRIPTION_2, "而春竹翠绿娇艳的样子");
        translationBuilder.add(ItemDescriptionConsts.GREEN_BAMBOO_WELCOMES_SPRING_DESCRIPTION_3, "既给客人带来视觉味觉上的新意");
        translationBuilder.add(ItemDescriptionConsts.GREEN_BAMBOO_WELCOMES_SPRING_DESCRIPTION_4, "还蕴含着迎春的美好寓意");
        translationBuilder.add(ItemDescriptionConsts.PLUM_TEA_RICE_DESCRIPTION_0, "用汤汁和热腾腾的白饭制作的茶泡饭！茶泡饭所用的汤汁通常为煎茶、烘焙茶或富有柴鱼香气的高汤");
        translationBuilder.add(ItemDescriptionConsts.PLUM_TEA_RICE_DESCRIPTION_1, "最后加入的梅子给这道素淡的菜品增添了一抹艳色");
        translationBuilder.add(ItemDescriptionConsts.STEAMED_EGG_WITH_SEA_URCHIN_DESCRIPTION_0, "将鸡蛋加入海胆蒸至海胆黄变色就可以出锅的简单料理");
        translationBuilder.add(ItemDescriptionConsts.STEAMED_EGG_WITH_SEA_URCHIN_DESCRIPTION_1, "吃的时候用勺子挖下去");
        translationBuilder.add(ItemDescriptionConsts.STEAMED_EGG_WITH_SEA_URCHIN_DESCRIPTION_2, "一口一勺");
        translationBuilder.add(ItemDescriptionConsts.STEAMED_EGG_WITH_SEA_URCHIN_DESCRIPTION_3, "海胆和蛋羹互相融合渗透的美味就在味蕾上蔓延了");
        translationBuilder.add(ItemDescriptionConsts.FANTASY_IS_ALL_THE_RAGE_DESCRIPTION_0, "对火候要求极其苛刻的一道菜");
        translationBuilder.add(ItemDescriptionConsts.FANTASY_IS_ALL_THE_RAGE_DESCRIPTION_1, "需聚息凝神地将肉品烤至完美的三分熟");
        translationBuilder.add(ItemDescriptionConsts.FANTASY_IS_ALL_THE_RAGE_DESCRIPTION_2, "暗红的色调以及极具破坏性的龙卷风形状");
        translationBuilder.add(ItemDescriptionConsts.FANTASY_IS_ALL_THE_RAGE_DESCRIPTION_3, "带来了山海欲来的压迫感");
        translationBuilder.add(ItemDescriptionConsts.FANTASY_IS_ALL_THE_RAGE_DESCRIPTION_4, "一口下去");
        translationBuilder.add(ItemDescriptionConsts.FANTASY_IS_ALL_THE_RAGE_DESCRIPTION_5, "有种仿佛征服了天下的快感");
        translationBuilder.add(ItemDescriptionConsts.GREEN_FAIRY_MUSHROOM_DESCRIPTION_0, "本质上是野菜拌蘑菇");
        translationBuilder.add(ItemDescriptionConsts.GREEN_FAIRY_MUSHROOM_DESCRIPTION_1, "但因为这个名字多了一些奇妙的童话感");
        translationBuilder.add(ItemDescriptionConsts.FLOWERS_BIRDS_WIND_AND_MOON_DESCRIPTION_0, "牵丝攀藤地解开“花鸟风月”一词");
        translationBuilder.add(ItemDescriptionConsts.FLOWERS_BIRDS_WIND_AND_MOON_DESCRIPTION_1, "同时想象着幽香小姐做出来的三角蛋糕");
        translationBuilder.add(ItemDescriptionConsts.FLOWERS_BIRDS_WIND_AND_MOON_DESCRIPTION_2, "整体风格就像她本人一样风雅");
        translationBuilder.add(ItemDescriptionConsts.FLOWERS_BIRDS_WIND_AND_MOON_DESCRIPTION_3, "不过上面插了一根我的羽毛");
        translationBuilder.add(ItemDescriptionConsts.FLOWERS_BIRDS_WIND_AND_MOON_DESCRIPTION_4, "应该还算有趣吧？");
        translationBuilder.add(ItemDescriptionConsts.THE_DREAM_DESCRIPTION_0, "以花卉为主题的双层奶油蛋糕");
        translationBuilder.add(ItemDescriptionConsts.THE_DREAM_DESCRIPTION_1, "锦簇花团铺在细腻的奶油上");
        translationBuilder.add(ItemDescriptionConsts.THE_DREAM_DESCRIPTION_2, "散发出丝丝香甜");
        translationBuilder.add(ItemDescriptionConsts.THE_DREAM_DESCRIPTION_3, "蓝色的蝴蝶轻轻驻足");
        translationBuilder.add(ItemDescriptionConsts.THE_DREAM_DESCRIPTION_4, "久久不归");
        translationBuilder.add(ItemDescriptionConsts.THE_DREAM_DESCRIPTION_5, "这不是梦境");
        translationBuilder.add(ItemDescriptionConsts.THE_DREAM_DESCRIPTION_6, "却胜似梦境");
        translationBuilder.add(ItemDescriptionConsts.TOON_PANCAKES_DESCRIPTION_0, "用有“毒”的野菜——香椿制成的煎饼");
        translationBuilder.add(ItemDescriptionConsts.TOON_PANCAKES_DESCRIPTION_1, "初尝有种苦苦涩涩的味道");
        translationBuilder.add(ItemDescriptionConsts.TOON_PANCAKES_DESCRIPTION_2, "嚼下去后口齿留甘");
        translationBuilder.add(ItemDescriptionConsts.TOON_PANCAKES_DESCRIPTION_3, "牢记！给普通客人食用须先将香椿用开水焯过一遍！");
        translationBuilder.add(ItemDescriptionConsts.POISONOUS_GARDEN_DESCRIPTION_0, "用各种“毒食材”混合炖煮的杂炊");
        translationBuilder.add(ItemDescriptionConsts.POISONOUS_GARDEN_DESCRIPTION_1, "通过细致而精巧的食材处理手段");
        translationBuilder.add(ItemDescriptionConsts.POISONOUS_GARDEN_DESCRIPTION_2, "能够不同程度地保留食材中的“毒性”");
        translationBuilder.add(ItemDescriptionConsts.POISONOUS_GARDEN_DESCRIPTION_3, "让不同需求的客人均能体验到他们想要的刺激");
        translationBuilder.add(ItemDescriptionConsts.A_LITTLE_SWEET_POISON_DESCRIPTION_0, "这是专门给小小的毒人偶——梅蒂欣制作的印象甜品");
        translationBuilder.add(ItemDescriptionConsts.A_LITTLE_SWEET_POISON_DESCRIPTION_1, "可爱中带着诡异的色彩");
        translationBuilder.add(ItemDescriptionConsts.A_LITTLE_SWEET_POISON_DESCRIPTION_2, "犹如自然界中徇丽的毒蘑菇勾人采摘");
        translationBuilder.add(ItemDescriptionConsts.A_LITTLE_SWEET_POISON_DESCRIPTION_3, "实际是没有毒的");
        translationBuilder.add(ItemDescriptionConsts.A_LITTLE_SWEET_POISON_DESCRIPTION_4, "梅蒂欣也是");
        translationBuilder.add(ItemDescriptionConsts.BAMBOO_TUBE_ROASTED_DRUNKEN_SHRIMP_DESCRIPTION_0, "把新鲜的虾放进醇香的酒中浸泡");
        translationBuilder.add(ItemDescriptionConsts.BAMBOO_TUBE_ROASTED_DRUNKEN_SHRIMP_DESCRIPTION_1, "再冰镇后淋上香料食用");
        translationBuilder.add(ItemDescriptionConsts.BAMBOO_TUBE_ROASTED_DRUNKEN_SHRIMP_DESCRIPTION_2, "既可以尝到虾的鲜香");
        translationBuilder.add(ItemDescriptionConsts.BAMBOO_TUBE_ROASTED_DRUNKEN_SHRIMP_DESCRIPTION_3, "同时也可以尝到酒的洌香");
        translationBuilder.add(ItemDescriptionConsts.BAMBOO_TUBE_ROASTED_DRUNKEN_SHRIMP_DESCRIPTION_4, "十分可口");
        translationBuilder.add(ItemDescriptionConsts.BEEF_HOT_POT_DESCRIPTION_0, "一边是魔界辣椒汤底");
        translationBuilder.add(ItemDescriptionConsts.BEEF_HOT_POT_DESCRIPTION_1, "一边是萝卜牛骨汤底");
        translationBuilder.add(ItemDescriptionConsts.BEEF_HOT_POT_DESCRIPTION_2, "兼具红白两种汤汁的特色双锅牛肉");
        translationBuilder.add(ItemDescriptionConsts.BEEF_HOT_POT_DESCRIPTION_3, "一家人");
        translationBuilder.add(ItemDescriptionConsts.BEEF_HOT_POT_DESCRIPTION_4, "一个锅");
        translationBuilder.add(ItemDescriptionConsts.BEEF_HOT_POT_DESCRIPTION_5, "两种口味团团圆圆");
        translationBuilder.add(ItemDescriptionConsts.BEEF_HOT_POT_DESCRIPTION_6, "仿佛有了它就有家的气息");
        translationBuilder.add(ItemDescriptionConsts.CAT_KULULI_DESCRIPTION_0, "迷人的焦糖脆外壳和软糯香浓的内心");
        translationBuilder.add(ItemDescriptionConsts.CAT_KULULI_DESCRIPTION_1, "无论在口感层次变化或者味觉体验都堪称一绝");
        translationBuilder.add(ItemDescriptionConsts.CAT_KULULI_DESCRIPTION_2, "不愧是“天使的铜铃”——咦？铃铛里居然还藏了只小猫咪！");
        translationBuilder.add(ItemDescriptionConsts.CAT_PIZZA_DESCRIPTION_0, "在发酵的圆面饼上面覆盖各种配料烤制而成");
        translationBuilder.add(ItemDescriptionConsts.CAT_PIZZA_DESCRIPTION_1, "完美兼顾了营养和品相");
        translationBuilder.add(ItemDescriptionConsts.CAT_PIZZA_DESCRIPTION_2, "焦糖洋葱的醇香滋味更是为其带来了多重的舌尖享受");
        translationBuilder.add(ItemDescriptionConsts.CAT_PIZZA_DESCRIPTION_3, "可爱的猫咪外形让人有些不忍下嘴呢");
        translationBuilder.add(ItemDescriptionConsts.CATS_PLAYING_IN_WATER_DESCRIPTION_0, "猫咪主题的吐司盒");
        translationBuilder.add(ItemDescriptionConsts.CATS_PLAYING_IN_WATER_DESCRIPTION_1, "把一整个方块吐司挖空");
        translationBuilder.add(ItemDescriptionConsts.CATS_PLAYING_IN_WATER_DESCRIPTION_2, "底部涂上一层奶油");
        translationBuilder.add(ItemDescriptionConsts.CATS_PLAYING_IN_WATER_DESCRIPTION_3, "放入一层桃子果酱");
        translationBuilder.add(ItemDescriptionConsts.CATS_PLAYING_IN_WATER_DESCRIPTION_4, "再涂上一层奶油");
        translationBuilder.add(ItemDescriptionConsts.CATS_PLAYING_IN_WATER_DESCRIPTION_5, "铺一片吐司");
        translationBuilder.add(ItemDescriptionConsts.CATS_PLAYING_IN_WATER_DESCRIPTION_6, "加入晶莹剔透的白凉粉");
        translationBuilder.add(ItemDescriptionConsts.CATS_PLAYING_IN_WATER_DESCRIPTION_7, "最后再用调色奶油映照出蓝色的水面效果");
        translationBuilder.add(ItemDescriptionConsts.CATS_PLAYING_IN_WATER_DESCRIPTION_8, "放入巧克力做的猫咪");
        translationBuilder.add(ItemDescriptionConsts.CATS_PLAYING_IN_WATER_DESCRIPTION_9, "就成了一副有趣的猫咪戏水图了");
        translationBuilder.add(ItemDescriptionConsts.RAPUNZEL_DESCRIPTION_0, "摆盘精致的虾仁南瓜意面");
        translationBuilder.add(ItemDescriptionConsts.RAPUNZEL_DESCRIPTION_1, "南瓜蒸熟后又甜又糯");
        translationBuilder.add(ItemDescriptionConsts.RAPUNZEL_DESCRIPTION_2, "用它代替奶油做出来的意面低脂低卡");
        translationBuilder.add(ItemDescriptionConsts.RAPUNZEL_DESCRIPTION_3, "味道浓郁");
        translationBuilder.add(ItemDescriptionConsts.RAPUNZEL_DESCRIPTION_4, "营养十足！");
        translationBuilder.add(ItemDescriptionConsts.SEA_URCHIN_SHINGEN_PANCAKE_DESCRIPTION_0, "以金枪鱼高汤制作的饼身清透干净");
        translationBuilder.add(ItemDescriptionConsts.SEA_URCHIN_SHINGEN_PANCAKE_DESCRIPTION_1, "和新鲜海胆组成高级的香槟色系搭配");
        translationBuilder.add(ItemDescriptionConsts.SEA_URCHIN_SHINGEN_PANCAKE_DESCRIPTION_2, "底层的胡麻酱更增一丝醇厚的口感");
        translationBuilder.add(ItemDescriptionConsts.SEA_URCHIN_SHINGEN_PANCAKE_DESCRIPTION_3, "如此玲珑的甜品");
        translationBuilder.add(ItemDescriptionConsts.SEA_URCHIN_SHINGEN_PANCAKE_DESCRIPTION_4, "让人不忍破坏这晶莹剔透的美丽");
        translationBuilder.add(ItemDescriptionConsts.MAD_HATTER_TEA_PARTY_DESCRIPTION_0, "以魔界茶会为印象制作的甜品");
        translationBuilder.add(ItemDescriptionConsts.MAD_HATTER_TEA_PARTY_DESCRIPTION_1, "表面看起来是一个巧克力做的茶壶");
        translationBuilder.add(ItemDescriptionConsts.MAD_HATTER_TEA_PARTY_DESCRIPTION_2, "茶壶的肚子里是层层堆叠的奶油蛋糕");
        translationBuilder.add(ItemDescriptionConsts.MAD_HATTER_TEA_PARTY_DESCRIPTION_3, "周围装饰着蘑菇和西蓝花");
        translationBuilder.add(ItemDescriptionConsts.MAD_HATTER_TEA_PARTY_DESCRIPTION_4, "就像某个故事里的巧克力房子一样");
        translationBuilder.add(ItemDescriptionConsts.MAD_HATTER_TEA_PARTY_DESCRIPTION_5, "是充满了梦幻和不可思议的幸福甜品");
        translationBuilder.add(ItemDescriptionConsts.PEACH_BLOSSOM_GLAZE_ROLL_DESCRIPTION_0, "通过胶凝化技术");
        translationBuilder.add(ItemDescriptionConsts.PEACH_BLOSSOM_GLAZE_ROLL_DESCRIPTION_1, "把粉嫩可爱的桃花制成晶莹剔透的琉璃果冻外皮");
        translationBuilder.add(ItemDescriptionConsts.PEACH_BLOSSOM_GLAZE_ROLL_DESCRIPTION_2, "裹着糯叽叽的豆沙馅儿");
        translationBuilder.add(ItemDescriptionConsts.PEACH_BLOSSOM_GLAZE_ROLL_DESCRIPTION_3, "记录季节的浪漫和甜蜜");
        translationBuilder.add(ItemDescriptionConsts.MOONLIGHT_OVER_LOTUS_POND_DESCRIPTION_0, "清爽细腻的葡萄乌龙冻");
        translationBuilder.add(ItemDescriptionConsts.MOONLIGHT_OVER_LOTUS_POND_DESCRIPTION_1, "白色的是奶油慕斯！奶油的绵密和葡萄的清爽在舌尖碰撞出别样滋味");
        translationBuilder.add(ItemDescriptionConsts.MOONLIGHT_OVER_LOTUS_POND_DESCRIPTION_2, "茶冻间点缀片片青提");
        translationBuilder.add(ItemDescriptionConsts.MOONLIGHT_OVER_LOTUS_POND_DESCRIPTION_3, "馥郁果香充盈唇齿");
        translationBuilder.add(ItemDescriptionConsts.MOONLIGHT_OVER_LOTUS_POND_DESCRIPTION_4, "菏泽上的小露珠也相当有趣");
        translationBuilder.add(ItemDescriptionConsts.LONGYIN_PEACH_DESCRIPTION_0, "本质上就是用桃子来制作桃子");
        translationBuilder.add(ItemDescriptionConsts.LONGYIN_PEACH_DESCRIPTION_1, "将桃子作为原材料打碎");
        translationBuilder.add(ItemDescriptionConsts.LONGYIN_PEACH_DESCRIPTION_2, "最后再还原出最初始的形状");
        translationBuilder.add(ItemDescriptionConsts.LONGYIN_PEACH_DESCRIPTION_3, "从表面来看似乎是贯彻了一种返璞归真的理念");
        translationBuilder.add(ItemDescriptionConsts.LONGYIN_PEACH_DESCRIPTION_4, "但制作过程极其繁琐考究");
        translationBuilder.add(ItemDescriptionConsts.MOLECULAR_EGG_DESCRIPTION_0, "选用水分较少的北豆腐作为蛋白");
        translationBuilder.add(ItemDescriptionConsts.MOLECULAR_EGG_DESCRIPTION_1, "甜糯的南瓜作为蛋黄");
        translationBuilder.add(ItemDescriptionConsts.MOLECULAR_EGG_DESCRIPTION_2, "再用白巧克力做成蛋壳");
        translationBuilder.add(ItemDescriptionConsts.MOLECULAR_EGG_DESCRIPTION_3, "还原出鸡蛋的模样和口感");
        translationBuilder.add(ItemDescriptionConsts.MOLECULAR_EGG_DESCRIPTION_4, "让人意想不到呢");
        translationBuilder.add(ItemDescriptionConsts.THE_SOURCE_OF_LIFE_DESCRIPTION_0, "初见就像是云山雾罩的神秘星球");
        translationBuilder.add(ItemDescriptionConsts.THE_SOURCE_OF_LIFE_DESCRIPTION_1, "打开一看“星球”里竟然摆了个生鸡蛋？仔细看蛋液其实是银耳汤");
        translationBuilder.add(ItemDescriptionConsts.THE_SOURCE_OF_LIFE_DESCRIPTION_2, "蛋黄则是南瓜泥");
        translationBuilder.add(ItemDescriptionConsts.THE_SOURCE_OF_LIFE_DESCRIPTION_3, "吃起来口感清爽");
        translationBuilder.add(ItemDescriptionConsts.THE_SOURCE_OF_LIFE_DESCRIPTION_4, "还有香浓的南瓜味");
        translationBuilder.add(ItemDescriptionConsts.THE_MARS_DESCRIPTION_0, "特别定制的盘底如火星地表一般");
        translationBuilder.add(ItemDescriptionConsts.THE_MARS_DESCRIPTION_1, "上面盛放着顶级的螃蟹冻以及白葡萄分子技术制作的果凝");
        translationBuilder.add(ItemDescriptionConsts.THE_MARS_DESCRIPTION_2, "螃蟹口感冰凉顺滑不腻");
        translationBuilder.add(ItemDescriptionConsts.THE_MARS_DESCRIPTION_3, "果凝如水滴一般");
        translationBuilder.add(ItemDescriptionConsts.THE_MARS_DESCRIPTION_4, "而且带着淡淡酒香");
        translationBuilder.add(ItemDescriptionConsts.THE_MARS_DESCRIPTION_5, "寓意火星上一滴水珠");
        translationBuilder.add(ItemDescriptionConsts.THE_MARS_DESCRIPTION_6, "代表着最后的希望");
        translationBuilder.add(ItemDescriptionConsts.HEART_PORRIDGE_GRUEL_DESCRIPTION_0, "口味偏甜、营养丰富的银耳莲子粥");
        translationBuilder.add(ItemDescriptionConsts.HEART_PORRIDGE_GRUEL_DESCRIPTION_1, "滋润而不腻滞");
        translationBuilder.add(ItemDescriptionConsts.HEART_PORRIDGE_GRUEL_DESCRIPTION_2, "还具有养心安神的功效");
        translationBuilder.add(ItemDescriptionConsts.HULA_SOUP_DESCRIPTION_0, "由多种天然中草药按比例配制的汤料");
        translationBuilder.add(ItemDescriptionConsts.HULA_SOUP_DESCRIPTION_1, "再加入胡椒和辣椒");
        translationBuilder.add(ItemDescriptionConsts.HULA_SOUP_DESCRIPTION_2, "又用骨头汤做底料的牛肉胡辣汤");
        translationBuilder.add(ItemDescriptionConsts.HULA_SOUP_DESCRIPTION_3, "喝一口就能驱散瞌睡虫");
        translationBuilder.add(ItemDescriptionConsts.SUPERME_SEAFOOD_NOODLES_DESCRIPTION_0, "以海鲜汤料煨煮的汤面");
        translationBuilder.add(ItemDescriptionConsts.SUPERME_SEAFOOD_NOODLES_DESCRIPTION_1, "劲道爽滑");
        translationBuilder.add(ItemDescriptionConsts.SUPERME_SEAFOOD_NOODLES_DESCRIPTION_2, "汤浓鲜美");
        translationBuilder.add(ItemDescriptionConsts.SUPERME_SEAFOOD_NOODLES_DESCRIPTION_3, "是一道非常豪华的家常菜");
        translationBuilder.add(ItemDescriptionConsts.SUPERME_SEAFOOD_NOODLES_DESCRIPTION_4, "凭一碗海鲜面");
        translationBuilder.add(ItemDescriptionConsts.SUPERME_SEAFOOD_NOODLES_DESCRIPTION_5, "便能让人记住深藏着的大海的味道");
        translationBuilder.add(ItemDescriptionConsts.DARK_CUISINE_DESCRIPTION_0, "烹饪失误、散发着黑色气场的不明物质");
        translationBuilder.add(ItemDescriptionConsts.DARK_CUISINE_DESCRIPTION_1, "不会有人想吃这种东西…吧？");
        translationBuilder.add(ItemDescriptionConsts.EGG_DESCRIPTION_0, "人里供应的鸡蛋");
        translationBuilder.add(ItemDescriptionConsts.EGG_DESCRIPTION_1, "较为常见");
        translationBuilder.add(ItemDescriptionConsts.PORKCHOP_DESCRIPTION_0, "人里圈养的家猪肉");
        translationBuilder.add(ItemDescriptionConsts.PORKCHOP_DESCRIPTION_1, "较为常见");
        translationBuilder.add(ItemDescriptionConsts.BEEF_DESCRIPTION_0, "人里圈养的肉牛肉");
        translationBuilder.add(ItemDescriptionConsts.BEEF_DESCRIPTION_1, "较为常见");
        translationBuilder.add(ItemDescriptionConsts.VENISON_DESCRIPTION_0, "猎人们在山间猎回来的鹿肉");
        translationBuilder.add(ItemDescriptionConsts.VENISON_DESCRIPTION_1, "有点珍贵");
        translationBuilder.add(ItemDescriptionConsts.WILD_BOAR_MEAT_DESCRIPTION_0, "猎人们在山间猎回来的野猪肉");
        translationBuilder.add(ItemDescriptionConsts.WILD_BOAR_MEAT_DESCRIPTION_1, "充满野性");
        translationBuilder.add(ItemDescriptionConsts.TOFU_DESCRIPTION_0, "人里有卖的豆腐");
        translationBuilder.add(ItemDescriptionConsts.TOFU_DESCRIPTION_1, "较为常见");
        translationBuilder.add(ItemDescriptionConsts.POTATO_DESCRIPTION_0, "随处可见的那种普通土豆");
        translationBuilder.add(ItemDescriptionConsts.ONION_DESCRIPTION_0, "人里农田产的洋葱");
        translationBuilder.add(ItemDescriptionConsts.ONION_DESCRIPTION_1, "较为常见");
        translationBuilder.add(ItemDescriptionConsts.PUMPKIN_DESCRIPTION_0, "人里农田产的南瓜");
        translationBuilder.add(ItemDescriptionConsts.PUMPKIN_DESCRIPTION_1, "较为常见");
        translationBuilder.add(ItemDescriptionConsts.WHITE_RADISH_DESCRIPTION_0, "人里农田产的萝卜");
        translationBuilder.add(ItemDescriptionConsts.WHITE_RADISH_DESCRIPTION_1, "较为常见");
        translationBuilder.add(ItemDescriptionConsts.KELP_DESCRIPTION_0, "不知道从哪里流入的外来食材");
        translationBuilder.add(ItemDescriptionConsts.KELP_DESCRIPTION_1, "较为常见");
        translationBuilder.add(ItemDescriptionConsts.TROUT_DESCRIPTION_0, "栖息于淡水中的冷水鱼");
        translationBuilder.add(ItemDescriptionConsts.TROUT_DESCRIPTION_1, "较为常见");
        translationBuilder.add(ItemDescriptionConsts.HAGFISH_DESCRIPTION_0, "一种洄游性海鱼");
        translationBuilder.add(ItemDescriptionConsts.HAGFISH_DESCRIPTION_1, "但却在幻想乡的河流湖泊随处可见");
        translationBuilder.add(ItemDescriptionConsts.SALMON_DESCRIPTION_0, "一种高度洄游海鱼");
        translationBuilder.add(ItemDescriptionConsts.SALMON_DESCRIPTION_1, "但在幻想乡的河流湖泊可见");
        translationBuilder.add(ItemDescriptionConsts.SALMON_DESCRIPTION_2, "有点珍贵");
        translationBuilder.add(ItemDescriptionConsts.TUNA_DESCRIPTION_0, "一种大洋性洄游海鱼");
        translationBuilder.add(ItemDescriptionConsts.TUNA_DESCRIPTION_1, "但在幻想乡的河流湖泊可见");
        translationBuilder.add(ItemDescriptionConsts.TUNA_DESCRIPTION_2, "有点珍贵");
        translationBuilder.add(ItemDescriptionConsts.BLACK_PORK_DESCRIPTION_0, "在高海拔深山中圈养的黑毛猪肉");
        translationBuilder.add(ItemDescriptionConsts.BLACK_PORK_DESCRIPTION_1, "非常高级");
        translationBuilder.add(ItemDescriptionConsts.WAGYU_BEEF_DESCRIPTION_0, "传闻是超优质的肉牛品种");
        translationBuilder.add(ItemDescriptionConsts.WAGYU_BEEF_DESCRIPTION_1, "又称雪花肉");
        translationBuilder.add(ItemDescriptionConsts.WAGYU_BEEF_DESCRIPTION_2, "非常高级");
        translationBuilder.add(ItemDescriptionConsts.BROWN_MUSHROOM_DESCRIPTION_0, "从魔法森林采回来的品相良好的蘑菇");
        translationBuilder.add(ItemDescriptionConsts.BROWN_MUSHROOM_DESCRIPTION_1, "无法人工种植");
        translationBuilder.add(ItemDescriptionConsts.BROWN_MUSHROOM_DESCRIPTION_2, "非常珍贵");
        translationBuilder.add(ItemDescriptionConsts.TRUFFLE_DESCRIPTION_0, "从魔法森林采回来的品相良好的松露");
        translationBuilder.add(ItemDescriptionConsts.TRUFFLE_DESCRIPTION_1, "无法人工种植");
        translationBuilder.add(ItemDescriptionConsts.TRUFFLE_DESCRIPTION_2, "非常珍贵");
        translationBuilder.add(ItemDescriptionConsts.SUPREME_TUNA_DESCRIPTION_0, "金枪鱼中的顶级品种");
        translationBuilder.add(ItemDescriptionConsts.SUPREME_TUNA_DESCRIPTION_1, "非常珍贵");
        translationBuilder.add(ItemDescriptionConsts.PUFFERFISH_DESCRIPTION_0, "高级水产品");
        translationBuilder.add(ItemDescriptionConsts.PUFFERFISH_DESCRIPTION_1, "但体内含毒素");
        translationBuilder.add(ItemDescriptionConsts.PUFFERFISH_DESCRIPTION_2, "需小心处理");
        translationBuilder.add(ItemDescriptionConsts.PEACH_DESCRIPTION_0, "桃子树的果实");
        translationBuilder.add(ItemDescriptionConsts.PEACH_DESCRIPTION_1, "较为常见");
        translationBuilder.add(ItemDescriptionConsts.GINKGO_DESCRIPTION_0, "白果树的果实");
        translationBuilder.add(ItemDescriptionConsts.GINKGO_DESCRIPTION_1, "较为常见");
        translationBuilder.add(ItemDescriptionConsts.SHRIMP_DESCRIPTION_0, "分布在雾之湖和玄武泽的淡水虾");
        translationBuilder.add(ItemDescriptionConsts.SHRIMP_DESCRIPTION_1, "较为常见");
        translationBuilder.add(ItemDescriptionConsts.HONEY_BOTTLE_DESCRIPTION_0, "能从树上蜂巢采到的野生蜂蜜");
        translationBuilder.add(ItemDescriptionConsts.HONEY_BOTTLE_DESCRIPTION_1, "较为常见");
        translationBuilder.add(ItemDescriptionConsts.CICADA_SHELL_DESCRIPTION_0, "在树干上常常可以采集的昆虫外壳");
        translationBuilder.add(ItemDescriptionConsts.CICADA_SHELL_DESCRIPTION_1, "较为常见");
        translationBuilder.add(ItemDescriptionConsts.UDUMBARA_DESCRIPTION_0, "生长在沼泽中的奇迹之花");
        translationBuilder.add(ItemDescriptionConsts.UDUMBARA_DESCRIPTION_1, "非常珍贵");
        translationBuilder.add(ItemDescriptionConsts.DEW_DESCRIPTION_0, "清晨采回来的露水");
        translationBuilder.add(ItemDescriptionConsts.DEW_DESCRIPTION_1, "有点珍贵");
        translationBuilder.add(ItemDescriptionConsts.BAMBOO_SHOOTS_DESCRIPTION_0, "从野外采回来的竹笋");
        translationBuilder.add(ItemDescriptionConsts.BAMBOO_SHOOTS_DESCRIPTION_1, "较为常见");
        translationBuilder.add(ItemDescriptionConsts.BUTTER_DESCRIPTION_0, "西餐常用的食材");
        translationBuilder.add(ItemDescriptionConsts.BUTTER_DESCRIPTION_1, "可以轻松地给食物增加难以抗拒的香味");
        translationBuilder.add(ItemDescriptionConsts.FLOUR_DESCRIPTION_0, "有多种用途");
        translationBuilder.add(ItemDescriptionConsts.FLOUR_DESCRIPTION_1, "较为常见");
        translationBuilder.add(ItemDescriptionConsts.BAMBOO_DESCRIPTION_0, "鲜翠欲滴的迷途竹林鲜切竹子");
        translationBuilder.add(ItemDescriptionConsts.BAMBOO_DESCRIPTION_1, "散发着清冽的竹香");
        translationBuilder.add(ItemDescriptionConsts.STICKY_RICE_DESCRIPTION_0, "很有粘性的米");
        translationBuilder.add(ItemDescriptionConsts.STICKY_RICE_DESCRIPTION_1, "制作出来的料理或绵软适口");
        translationBuilder.add(ItemDescriptionConsts.STICKY_RICE_DESCRIPTION_2, "或鲜嫩弹牙");
        translationBuilder.add(ItemDescriptionConsts.MOONFLOWER_DESCRIPTION_0, "永远亭的特产");
        translationBuilder.add(ItemDescriptionConsts.MOONFLOWER_DESCRIPTION_1, "由满月之夜的月光凝聚");
        translationBuilder.add(ItemDescriptionConsts.MOONFLOWER_DESCRIPTION_2, "非常珍贵");
        translationBuilder.add(ItemDescriptionConsts.MAGIC_ICE_BLOCK_DESCRIPTION_0, "水的固体形态");
        translationBuilder.add(ItemDescriptionConsts.MAGIC_ICE_BLOCK_DESCRIPTION_1, "帮助食材保温");
        translationBuilder.add(ItemDescriptionConsts.MAGIC_ICE_BLOCK_DESCRIPTION_2, "晶莹剔透");
        translationBuilder.add(ItemDescriptionConsts.CHILI_DESCRIPTION_0, "用于增加辣味的食材");
        translationBuilder.add(ItemDescriptionConsts.CHILI_DESCRIPTION_1, "评价非常两极");
        translationBuilder.add(ItemDescriptionConsts.GRAPE_DESCRIPTION_0, "在红魔馆种植的用于酿酒的葡萄");
        translationBuilder.add(ItemDescriptionConsts.CUCUMBER_DESCRIPTION_0, "河童的嗜好物");
        translationBuilder.add(ItemDescriptionConsts.CUCUMBER_DESCRIPTION_1, "虽然用来做菜和普通蔬菜没有区别");
        translationBuilder.add(ItemDescriptionConsts.CUCUMBER_DESCRIPTION_2, "但却能让河童无比上瘾");
        translationBuilder.add(ItemDescriptionConsts.CUCUMBER_DESCRIPTION_3, "究竟是其中的什么成分在起作用…");
        translationBuilder.add(ItemDescriptionConsts.OCTOPUS_DESCRIPTION_0, "鲜嫩可爱的海洋生物");
        translationBuilder.add(ItemDescriptionConsts.OCTOPUS_DESCRIPTION_1, "但幻想乡没有海…它的脚是宝贝");
        translationBuilder.add(ItemDescriptionConsts.OCTOPUS_DESCRIPTION_2, "有压倒性的肉质感");
        translationBuilder.add(ItemDescriptionConsts.OCTOPUS_DESCRIPTION_3, "而且只要用最简单的火烤");
        translationBuilder.add(ItemDescriptionConsts.OCTOPUS_DESCRIPTION_4, "就能享受弹牙的美味！");
        translationBuilder.add(ItemDescriptionConsts.SEA_URCHIN_DESCRIPTION_0, "据说在外界被称作传说级的食材");
        translationBuilder.add(ItemDescriptionConsts.SEA_URCHIN_DESCRIPTION_1, "只要一小颗就能让人感受到整个海洋的美味");
        translationBuilder.add(ItemDescriptionConsts.SEA_URCHIN_DESCRIPTION_2, "会是真的吗？样子长得倒是挺奇怪的…");
        translationBuilder.add(ItemDescriptionConsts.BLACK_SALT_DESCRIPTION_0, "火山能量");
        translationBuilder.add(ItemDescriptionConsts.BLACK_SALT_DESCRIPTION_1, "累积上千万年；只为净化");
        translationBuilder.add(ItemDescriptionConsts.BLACK_SALT_DESCRIPTION_2, "你灵魂的尘埃…广告词这样写着");
        translationBuilder.add(ItemDescriptionConsts.BLACK_SALT_DESCRIPTION_3, "其实就是普通的火山岩盐");
        translationBuilder.add(ItemDescriptionConsts.CREAM_DESCRIPTION_0, "用非常特殊的方法处理的奶制品");
        translationBuilder.add(ItemDescriptionConsts.CREAM_DESCRIPTION_1, "无论什么时候都能秒杀甜食嗜好者的味蕾！");
        translationBuilder.add(ItemDescriptionConsts.CRAB_DESCRIPTION_0, "以为有了盔甲就可以横行霸道的八脚笨蛋");
        translationBuilder.add(ItemDescriptionConsts.CRAB_DESCRIPTION_1, "只需最简单的清蒸就能成为究极美味！不过要小心它的钳子…");
        translationBuilder.add(ItemDescriptionConsts.TWIN_LOTUS_DESCRIPTION_0, "根茎生长在桥的附近");
        translationBuilder.add(ItemDescriptionConsts.TWIN_LOTUS_DESCRIPTION_1, "花朵成熟后会破水而出");
        translationBuilder.add(ItemDescriptionConsts.TWIN_LOTUS_DESCRIPTION_2, "淡红色并有淡香味");
        translationBuilder.add(ItemDescriptionConsts.TWIN_LOTUS_DESCRIPTION_3, "高级食材");
        translationBuilder.add(ItemDescriptionConsts.TWIN_LOTUS_DESCRIPTION_4, "也是地底人的节日装饰");
        translationBuilder.add(ItemDescriptionConsts.LEMON_DESCRIPTION_0, "仅在桥附近特别的树上产出的奇怪果实");
        translationBuilder.add(ItemDescriptionConsts.LEMON_DESCRIPTION_1, "据说没有人能够无表情板着脸吃完一整颗");
        translationBuilder.add(ItemDescriptionConsts.LEMON_DESCRIPTION_2, "有着强大的“酸”味");
        translationBuilder.add(ItemDescriptionConsts.LEMON_DESCRIPTION_3, "只能取其果汁来调味");
        translationBuilder.add(ItemDescriptionConsts.CHEESE_DESCRIPTION_0, "浓郁的奶油熟成后的珍贵食材");
        translationBuilder.add(ItemDescriptionConsts.CHEESE_DESCRIPTION_1, "取一片加热融化就能让料理变得奶香浓郁");
        translationBuilder.add(ItemDescriptionConsts.CHEESE_DESCRIPTION_2, "美味十足");
        translationBuilder.add(ItemDescriptionConsts.LOTUS_NUTS_DESCRIPTION_0, "非常古老的水生植物——莲的种子");
        translationBuilder.add(ItemDescriptionConsts.LOTUS_NUTS_DESCRIPTION_1, "莲子的芯很苦");
        translationBuilder.add(ItemDescriptionConsts.LOTUS_NUTS_DESCRIPTION_2, "千万得处理好");
        translationBuilder.add(ItemDescriptionConsts.LOTUS_NUTS_DESCRIPTION_3, "别混到料理给客人吃了");
        translationBuilder.add(ItemDescriptionConsts.SWEET_POTATO_DESCRIPTION_0, "世上最实在的食材！不仅香甜软糯");
        translationBuilder.add(ItemDescriptionConsts.SWEET_POTATO_DESCRIPTION_1, "而且能强效地应对饥饿");
        translationBuilder.add(ItemDescriptionConsts.SWEET_POTATO_DESCRIPTION_2, "在冬天还有暖手的效果");
        translationBuilder.add(ItemDescriptionConsts.PINE_NUT_DESCRIPTION_0, "红松树的种子");
        translationBuilder.add(ItemDescriptionConsts.PINE_NUT_DESCRIPTION_1, "据说在外界已经被列为濒危物种");
        translationBuilder.add(ItemDescriptionConsts.PINE_NUT_DESCRIPTION_2, "但在幻想乡仍然很常见");
        translationBuilder.add(ItemDescriptionConsts.PINE_NUT_DESCRIPTION_3, "传说松子有延年益寿的功能");
        translationBuilder.add(ItemDescriptionConsts.PINE_NUT_DESCRIPTION_4, "是古代道士辟谷时的常备之物");
        translationBuilder.add(ItemDescriptionConsts.CHESTNUT_DESCRIPTION_0, "相传道教创始人修炼时");
        translationBuilder.add(ItemDescriptionConsts.CHESTNUT_DESCRIPTION_1, "由于不爱荤腥");
        translationBuilder.add(ItemDescriptionConsts.CHESTNUT_DESCRIPTION_2, "便栽了许多板栗树");
        translationBuilder.add(ItemDescriptionConsts.CHESTNUT_DESCRIPTION_3, "以栗代饭");
        translationBuilder.add(ItemDescriptionConsts.CHESTNUT_DESCRIPTION_4, "栗子种仁肥厚");
        translationBuilder.add(ItemDescriptionConsts.CHESTNUT_DESCRIPTION_5, "营养丰富");
        translationBuilder.add(ItemDescriptionConsts.CHESTNUT_DESCRIPTION_6, "不仅受到许多道教人士的推崇");
        translationBuilder.add(ItemDescriptionConsts.CHESTNUT_DESCRIPTION_7, "也经常被农民用来代替粮食");
        translationBuilder.add(ItemDescriptionConsts.PLUM_DESCRIPTION_0, "果梅树结的果实");
        translationBuilder.add(ItemDescriptionConsts.PLUM_DESCRIPTION_1, "可以盐渍或干制");
        translationBuilder.add(ItemDescriptionConsts.PLUM_DESCRIPTION_2, "做成梅干后有种咸中带酸的特殊口感");
        translationBuilder.add(ItemDescriptionConsts.PLUM_DESCRIPTION_3, "能够大大激起食欲");
        translationBuilder.add(ItemDescriptionConsts.PLUM_DESCRIPTION_4, "且品尝后唇齿留甘");
        translationBuilder.add(ItemDescriptionConsts.PLUM_DESCRIPTION_5, "让人回味无穷");
        translationBuilder.add(ItemDescriptionConsts.RED_BEANS_DESCRIPTION_0, "又称赤豆");
        translationBuilder.add(ItemDescriptionConsts.RED_BEANS_DESCRIPTION_1, "是适应能力较强的草本植物");
        translationBuilder.add(ItemDescriptionConsts.RED_BEANS_DESCRIPTION_2, "食用能够起到增强自身免疫力和抗病能力的效果");
        translationBuilder.add(ItemDescriptionConsts.RED_BEANS_DESCRIPTION_3, "古代人觉得中风得病都是疫鬼作祟");
        translationBuilder.add(ItemDescriptionConsts.RED_BEANS_DESCRIPTION_4, "所以有“赤豆打鬼”的传说");
        translationBuilder.add(ItemDescriptionConsts.FLOWERS_DESCRIPTION_0, "一朵朵绚丽烂漫的鲜花扎成的花束");
        translationBuilder.add(ItemDescriptionConsts.FLOWERS_DESCRIPTION_1, "花儿们以生命为代价也要完成的事");
        translationBuilder.add(ItemDescriptionConsts.FLOWERS_DESCRIPTION_2, "必须要对此付出敬意！");
        translationBuilder.add(ItemDescriptionConsts.TOON_DESCRIPTION_0, "一种常见的野菜");
        translationBuilder.add(ItemDescriptionConsts.TOON_DESCRIPTION_1, "具有一定的毒性");
        translationBuilder.add(ItemDescriptionConsts.TOON_DESCRIPTION_2, "不建议过量食用");
        translationBuilder.add(ItemDescriptionConsts.TOON_DESCRIPTION_3, "并且食用前须焯水烹饪");
        translationBuilder.add(ItemDescriptionConsts.TOMATO_DESCRIPTION_0, "颜色鲜艳的浆果");
        translationBuilder.add(ItemDescriptionConsts.TOMATO_DESCRIPTION_1, "曾经还因为太鲜艳被视为“狐狸的果实”");
        translationBuilder.add(ItemDescriptionConsts.TOMATO_DESCRIPTION_2, "觉得它具有剧毒");
        translationBuilder.add(ItemDescriptionConsts.TOMATO_DESCRIPTION_3, "所以只用来观赏");
        translationBuilder.add(ItemDescriptionConsts.TOMATO_DESCRIPTION_4, "在发现可以食用以后就开始大面积种植了");
        translationBuilder.add(ItemDescriptionConsts.COCOA_BEANS_DESCRIPTION_0, "可可树的果实");
        translationBuilder.add(ItemDescriptionConsts.COCOA_BEANS_DESCRIPTION_1, "据说在不同地区有不同的风味");
        translationBuilder.add(ItemDescriptionConsts.COCOA_BEANS_DESCRIPTION_2, "有的会带点果香");
        translationBuilder.add(ItemDescriptionConsts.COCOA_BEANS_DESCRIPTION_3, "有的带有烟熏的风味");
        translationBuilder.add(ItemDescriptionConsts.COCOA_BEANS_DESCRIPTION_4, "可以磨成粉食用");
        translationBuilder.add(ItemDescriptionConsts.COCOA_BEANS_DESCRIPTION_5, "是制作巧克力的基本原料");
        translationBuilder.add(ItemDescriptionConsts.BROCCOLI_DESCRIPTION_0, "魔界土壤培育出来的西蓝花");
        translationBuilder.add(ItemDescriptionConsts.BROCCOLI_DESCRIPTION_1, "因为光照不足植株徒长");
        translationBuilder.add(ItemDescriptionConsts.BROCCOLI_DESCRIPTION_2, "花球颜色和地上也略微有些不同");
        translationBuilder.add(ItemDescriptionConsts.PUFF_YO_FRUIT_DESCRIPTION_0, "魔界里到处可见的怪异果子");
        translationBuilder.add(ItemDescriptionConsts.PUFF_YO_FRUIT_DESCRIPTION_1, "似乎会根据食用者的类型产生不同的食用效果");
        translationBuilder.add(ItemDescriptionConsts.PUFF_YO_FRUIT_DESCRIPTION_2, "十分奇特");
        translationBuilder.add(ItemDescriptionConsts.FICUS_MICROCARPA_DESCRIPTION_0, "一种药食两用植物");
        translationBuilder.add(ItemDescriptionConsts.FICUS_MICROCARPA_DESCRIPTION_1, "将薜荔籽浸泡后搓汁");
        translationBuilder.add(ItemDescriptionConsts.FICUS_MICROCARPA_DESCRIPTION_2, "冷藏后会凝固成晶莹剔透的胶状物");
        translationBuilder.add(ItemDescriptionConsts.FICUS_MICROCARPA_DESCRIPTION_3, "可以用来制作果冻和凉粉等");
        translationBuilder.add(ItemDescriptionConsts.FICUS_MICROCARPA_DESCRIPTION_4, "是消暑神品");
        translationBuilder.add(ItemDescriptionConsts.TREMELLA_DESCRIPTION_0, "一种像花儿一样美丽的菌类");
        translationBuilder.add(ItemDescriptionConsts.TREMELLA_DESCRIPTION_1, "据说历代皇家贵族把银耳看作延年益寿之品");
        translationBuilder.add(ItemDescriptionConsts.TREMELLA_DESCRIPTION_2, "但在月都似乎很常见");
        translationBuilder.add(ItemDescriptionConsts.TREMELLA_DESCRIPTION_3, "这种看起来一尘不染的洁白");
        translationBuilder.add(ItemDescriptionConsts.TREMELLA_DESCRIPTION_4, "和月都的感觉很相衬");
        translationBuilder.add(ItemDescriptionConsts.GREEN_TEA_DESCRIPTION_0, "最普通的饮料");
        translationBuilder.add(ItemDescriptionConsts.GREEN_TEA_DESCRIPTION_1, "给一滴酒都不能沾的弱小妖怪准备的");
        translationBuilder.add(ItemDescriptionConsts.FRUITY_HIGH_BALL_DESCRIPTION_0, "居酒屋常见的");
        translationBuilder.add(ItemDescriptionConsts.FRUITY_HIGH_BALL_DESCRIPTION_1, "使用威士忌、果汁和苏打勾兑的简单调酒");
        translationBuilder.add(ItemDescriptionConsts.FRUITY_HIGH_BALL_DESCRIPTION_2, "降低了酒精度之后成为了谁都可以享受的饮料");
        translationBuilder.add(ItemDescriptionConsts.FRUITY_SOUR_DESCRIPTION_0, "居酒屋常见的");
        translationBuilder.add(ItemDescriptionConsts.FRUITY_SOUR_DESCRIPTION_1, "使用烧酒、果汁和苏打勾兑的简单调酒");
        translationBuilder.add(ItemDescriptionConsts.FRUITY_SOUR_DESCRIPTION_2, "比起果味High Ball更加有日式风格");
        translationBuilder.add(ItemDescriptionConsts.QI_DESCRIPTION_0, "在加入了大量的苏打后");
        translationBuilder.add(ItemDescriptionConsts.QI_DESCRIPTION_1, "这种起泡清酒酸甜的口感");
        translationBuilder.add(ItemDescriptionConsts.QI_DESCRIPTION_2, "以及较低的酒精度");
        translationBuilder.add(ItemDescriptionConsts.QI_DESCRIPTION_3, "使其很受女性的欢迎；可以说是清酒的香槟版本");
        translationBuilder.add(ItemDescriptionConsts.BEER_DESCRIPTION_0, "某位和幻想乡很有渊源的大人物作为副业的产品");
        translationBuilder.add(ItemDescriptionConsts.BEER_DESCRIPTION_1, "虽然是出于兴趣而研制的啤酒");
        translationBuilder.add(ItemDescriptionConsts.BEER_DESCRIPTION_2, "但意外地十分有人气");
        translationBuilder.add(ItemDescriptionConsts.SUN_MOON_STAR_DESCRIPTION_0, "纯米酒");
        translationBuilder.add(ItemDescriptionConsts.SUN_MOON_STAR_DESCRIPTION_1, "有着妖精的祝福");
        translationBuilder.add(ItemDescriptionConsts.SUN_MOON_STAR_DESCRIPTION_2, "度数不高");
        translationBuilder.add(ItemDescriptionConsts.SUN_MOON_STAR_DESCRIPTION_3, "口感柔顺");
        translationBuilder.add(ItemDescriptionConsts.SUN_MOON_STAR_DESCRIPTION_4, "价格平易近人");
        translationBuilder.add(ItemDescriptionConsts.SUN_MOON_STAR_DESCRIPTION_5, "是居酒屋受欢迎的选择");
        translationBuilder.add(ItemDescriptionConsts.PLUM_WINE_DESCRIPTION_0, "人间之里的人类自酿的梅子酒");
        translationBuilder.add(ItemDescriptionConsts.PLUM_WINE_DESCRIPTION_1, "因为喝起来很甜");
        translationBuilder.add(ItemDescriptionConsts.PLUM_WINE_DESCRIPTION_2, "所以经常有不了解它的生物被它的后劲儿击倒");
        translationBuilder.add(ItemDescriptionConsts.TENGU_DANCE_DESCRIPTION_0, "传说连天狗喝了也会开心地跳舞的美味清酒");
        translationBuilder.add(ItemDescriptionConsts.TENGU_DANCE_DESCRIPTION_1, "在妖怪之山开一次店就可以验证了吧");
        translationBuilder.add(ItemDescriptionConsts.TENGU_DANCE_DESCRIPTION_2, "不同于其他无色的清酒");
        translationBuilder.add(ItemDescriptionConsts.TENGU_DANCE_DESCRIPTION_3, "天狗踊带有淡淡的琥珀色");
        translationBuilder.add(ItemDescriptionConsts.SCARLET_DEVIL_DESCRIPTION_0, "由伏特加、番茄汁、柠檬片、芹菜根混合调制");
        translationBuilder.add(ItemDescriptionConsts.SCARLET_DEVIL_DESCRIPTION_1, "甜、酸、苦、辣四味俱全");
        translationBuilder.add(ItemDescriptionConsts.SCARLET_DEVIL_DESCRIPTION_2, "因血红的颜色让人联想到某洋馆的吸血鬼");
        translationBuilder.add(ItemDescriptionConsts.SCARLET_DEVIL_DESCRIPTION_3, "故得此名");
        translationBuilder.add(ItemDescriptionConsts.GODS_WHEAT_DESCRIPTION_0, "使用妖怪之山上被秋天的神明们所庇佑的大麦所酿造的大麦烧酒");
        translationBuilder.add(ItemDescriptionConsts.OTTER_FESTIVAL_DESCRIPTION_0, "鬼杰组的战利品之一");
        translationBuilder.add(ItemDescriptionConsts.OTTER_FESTIVAL_DESCRIPTION_1, "因为水獭灵把敌组的战利品摆在地上的样子仿佛祭典");
        translationBuilder.add(ItemDescriptionConsts.OTTER_FESTIVAL_DESCRIPTION_2, "于是随便取的名字");
        translationBuilder.add(ItemDescriptionConsts.OTTER_FESTIVAL_DESCRIPTION_3, "实际上好像是相当高级的纯米大吟酿");
        translationBuilder.add(ItemDescriptionConsts.DAWN_DESCRIPTION_0, "针对幻想乡居民口味改良的威士忌");
        translationBuilder.add(ItemDescriptionConsts.DAWN_DESCRIPTION_1, "在保留威士忌独有的气味的同时也有着顺滑的口感");
        translationBuilder.add(ItemDescriptionConsts.SPARROW_SAKE_DESCRIPTION_0, "传说中由麻雀酿的酒");
        translationBuilder.add(ItemDescriptionConsts.SPARROW_SAKE_DESCRIPTION_1, "将米粒藏入截断的竹中");
        translationBuilder.add(ItemDescriptionConsts.SPARROW_SAKE_DESCRIPTION_2, "当竹中有水时");
        translationBuilder.add(ItemDescriptionConsts.SPARROW_SAKE_DESCRIPTION_3, "日经月累便成佳酿");
        translationBuilder.add(ItemDescriptionConsts.SPARROW_SAKE_DESCRIPTION_4, "据说喝了此酒会舞无休止");
        translationBuilder.add(ItemDescriptionConsts.SPARROW_SAKE_DESCRIPTION_5, "是带着“诅咒”的美味呢");
        translationBuilder.add(ItemDescriptionConsts.SCARLET_DEVIL_MANSION_BLACK_TEA_DESCRIPTION_0, "使用柠檬、佛手柑和女仆长精心挑选的红茶品种烘培出带有独特香味的红茶饮料");
        translationBuilder.add(ItemDescriptionConsts.SCARLET_DEVIL_MANSION_BLACK_TEA_DESCRIPTION_1, "为了扩张红魔馆的威严");
        translationBuilder.add(ItemDescriptionConsts.SCARLET_DEVIL_MANSION_BLACK_TEA_DESCRIPTION_2, "非常普通地以红魔馆命名");
        translationBuilder.add(ItemDescriptionConsts.AFFGADO_DESCRIPTION_0, "将冰激凌融化在咖啡中的做法");
        translationBuilder.add(ItemDescriptionConsts.AFFGADO_DESCRIPTION_1, "对于怕苦又需要咖啡提神的人来说是再好不过的饮料");
        translationBuilder.add(ItemDescriptionConsts.RED_MIST_DESCRIPTION_0, "洋馆的女仆特制的红葡萄酒");
        translationBuilder.add(ItemDescriptionConsts.RED_MIST_DESCRIPTION_1, "酒体丰满");
        translationBuilder.add(ItemDescriptionConsts.RED_MIST_DESCRIPTION_2, "因为好像不是正常的时间下酿造出来的");
        translationBuilder.add(ItemDescriptionConsts.RED_MIST_DESCRIPTION_3, "所以总有一种雾气般的朦胧感");
        translationBuilder.add(ItemDescriptionConsts.NEGRONI_DESCRIPTION_0, "微苦的橙味为主");
        translationBuilder.add(ItemDescriptionConsts.NEGRONI_DESCRIPTION_1, "香气扑鼻");
        translationBuilder.add(ItemDescriptionConsts.NEGRONI_DESCRIPTION_2, "入口柔顺");
        translationBuilder.add(ItemDescriptionConsts.NEGRONI_DESCRIPTION_3, "最近非常流行");
        translationBuilder.add(ItemDescriptionConsts.GODFATHER_DESCRIPTION_0, "浓烈的北方威士忌和杏仁利口酒混合");
        translationBuilder.add(ItemDescriptionConsts.GODFATHER_DESCRIPTION_1, "非常古典的调酒");
        translationBuilder.add(ItemDescriptionConsts.GODFATHER_DESCRIPTION_2, "口感也相当硬汉");
        translationBuilder.add(ItemDescriptionConsts.GODFATHER_DESCRIPTION_3, "普通的妖怪应付不来");
        translationBuilder.add(ItemDescriptionConsts.BLESSING_WIND_DESCRIPTION_0, "轻松愉快的餐后酒");
        translationBuilder.add(ItemDescriptionConsts.BLESSING_WIND_DESCRIPTION_1, "薄荷和奶油为主的口感");
        translationBuilder.add(ItemDescriptionConsts.BLESSING_WIND_DESCRIPTION_2, "比起酒精饮料更像甜品");
        translationBuilder.add(ItemDescriptionConsts.WINTER_BREW_DESCRIPTION_0, "来自某个古国的习俗");
        translationBuilder.add(ItemDescriptionConsts.WINTER_BREW_DESCRIPTION_1, "在冬至日酿造的酒");
        translationBuilder.add(ItemDescriptionConsts.WINTER_BREW_DESCRIPTION_2, "色泽金黄");
        translationBuilder.add(ItemDescriptionConsts.WINTER_BREW_DESCRIPTION_3, "清甜甘冽");
        translationBuilder.add(ItemDescriptionConsts.WINTER_BREW_DESCRIPTION_4, "加上桂花的香气");
        translationBuilder.add(ItemDescriptionConsts.WINTER_BREW_DESCRIPTION_5, "无论冰着喝还是热着喝都非常可口");
        translationBuilder.add(ItemDescriptionConsts.FOURTEENTH_NIGHT_DESCRIPTION_0, "比起十五夜的月亮是满月");
        translationBuilder.add(ItemDescriptionConsts.FOURTEENTH_NIGHT_DESCRIPTION_1, "也许更想留下十四夜时期待的心情");
        translationBuilder.add(ItemDescriptionConsts.FOURTEENTH_NIGHT_DESCRIPTION_2, "以这样的感觉酿造的高级清酒");
        translationBuilder.add(ItemDescriptionConsts.FOURTEENTH_NIGHT_DESCRIPTION_3, "也许只有它才配得上迷途竹林所见到的月亮吧");
        translationBuilder.add(ItemDescriptionConsts.FIRE_RAT_FUR_DESCRIPTION_0, "如果不使用火鼠裘来承装也许就会烧起来的烈酒");
        translationBuilder.add(ItemDescriptionConsts.FIRE_RAT_FUR_DESCRIPTION_1, "几乎无人能承受其辣度的超级辣口烧酒");
        translationBuilder.add(ItemDescriptionConsts.GYOKURO_TEA_DESCRIPTION_0, "几乎是日本茶中最高级的茶叶");
        translationBuilder.add(ItemDescriptionConsts.GYOKURO_TEA_DESCRIPTION_1, "需要用较低的水温来冲泡");
        translationBuilder.add(ItemDescriptionConsts.GYOKURO_TEA_DESCRIPTION_2, "甘醇飘香");
        translationBuilder.add(ItemDescriptionConsts.GYOKURO_TEA_DESCRIPTION_3, "口感独特");
        translationBuilder.add(ItemDescriptionConsts.MOON_ROCKET_DESCRIPTION_0, "使用月之都先进技术制作的高级气泡水");
        translationBuilder.add(ItemDescriptionConsts.MOON_ROCKET_DESCRIPTION_1, "迸发的口感有如火箭一般");
        translationBuilder.add(ItemDescriptionConsts.MOON_ROCKET_DESCRIPTION_2, "只需要加一片柠檬就是完美的饮品");
        translationBuilder.add(ItemDescriptionConsts.MILK_DESCRIPTION_0, "温润纯白的饮品");
        translationBuilder.add(ItemDescriptionConsts.MILK_DESCRIPTION_1, "无论小孩还是大人都适合饮用");
        translationBuilder.add(ItemDescriptionConsts.MILK_DESCRIPTION_2, "好处多到说不完");
        translationBuilder.add(ItemDescriptionConsts.RED_GRAPEFRUIT_JUICE_DESCRIPTION_0, "据说是来自外界的人气饮料");
        translationBuilder.add(ItemDescriptionConsts.RED_GRAPEFRUIT_JUICE_DESCRIPTION_1, "用红色柚子这种水果榨汁");
        translationBuilder.add(ItemDescriptionConsts.RED_GRAPEFRUIT_JUICE_DESCRIPTION_2, "尤其是盛夏饮用");
        translationBuilder.add(ItemDescriptionConsts.RED_GRAPEFRUIT_JUICE_DESCRIPTION_3, "健康祛暑");
        translationBuilder.add(ItemDescriptionConsts.RED_GRAPEFRUIT_JUICE_DESCRIPTION_4, "让人回甘无穷");
        translationBuilder.add(ItemDescriptionConsts.SODA_DESCRIPTION_0, "瓶口有弹珠的设计");
        translationBuilder.add(ItemDescriptionConsts.SODA_DESCRIPTION_1, "只要向下按压将弹珠打入汽水内");
        translationBuilder.add(ItemDescriptionConsts.SODA_DESCRIPTION_2, "引起二氧化碳的剧烈反应");
        translationBuilder.add(ItemDescriptionConsts.SODA_DESCRIPTION_3, "此时将瓶中沸腾又冰冷的气泡一饮而尽的话");
        translationBuilder.add(ItemDescriptionConsts.SODA_DESCRIPTION_4, "你会在胃中感受到整个夏天");
        translationBuilder.add(ItemDescriptionConsts.ICEBERG_MAPLE_FROZEN_LEMON_DESCRIPTION_0, "仿佛是融化在可口的冰沙中的毛玉");
        translationBuilder.add(ItemDescriptionConsts.ICEBERG_MAPLE_FROZEN_LEMON_DESCRIPTION_1, "佐以冻柠口味");
        translationBuilder.add(ItemDescriptionConsts.ICEBERG_MAPLE_FROZEN_LEMON_DESCRIPTION_2, "在炎热的夏季");
        translationBuilder.add(ItemDescriptionConsts.ICEBERG_MAPLE_FROZEN_LEMON_DESCRIPTION_3, "不知拯救了多少人命");
        translationBuilder.add(ItemDescriptionConsts.BIG_POPSICLE_DESCRIPTION_0, "简单又富有重量感的大冰块");
        translationBuilder.add(ItemDescriptionConsts.BIG_POPSICLE_DESCRIPTION_1, "有梦幻的甜蜜和薄荷的调味");
        translationBuilder.add(ItemDescriptionConsts.BIG_POPSICLE_DESCRIPTION_2, "夏天解暑、让所有人满血复活的神奇冰品");
        translationBuilder.add(ItemDescriptionConsts.DAIGINJO_DESCRIPTION_0, "最高级的清酒");
        translationBuilder.add(ItemDescriptionConsts.DAIGINJO_DESCRIPTION_1, "口感极佳而且有水果的香味");
        translationBuilder.add(ItemDescriptionConsts.DAIGINJO_DESCRIPTION_2, "必须避光");
        translationBuilder.add(ItemDescriptionConsts.DAIGINJO_DESCRIPTION_3, "在太阳的照射下颜色会迅速变深");
        translationBuilder.add(ItemDescriptionConsts.COFFEE_DESCRIPTION_0, "用现代磨制工艺将稀少的咖啡豆磨成粉末制作的饮品");
        translationBuilder.add(ItemDescriptionConsts.COFFEE_DESCRIPTION_1, "能够奇妙地提升精神和集中力");
        translationBuilder.add(ItemDescriptionConsts.COFFEE_DESCRIPTION_2, "是脑力劳动者不可或缺的神奇饮料");
        translationBuilder.add(ItemDescriptionConsts.FAIRY_RAIN_DESCRIPTION_0, "妖精们采集露水");
        translationBuilder.add(ItemDescriptionConsts.FAIRY_RAIN_DESCRIPTION_1, "和花蜜混合的甘甜饮料");
        translationBuilder.add(ItemDescriptionConsts.FAIRY_RAIN_DESCRIPTION_2, "由于妖精们会忘记自己的劳动产品");
        translationBuilder.add(ItemDescriptionConsts.FAIRY_RAIN_DESCRIPTION_3, "所以常被动物或者人类采走");
        translationBuilder.add(ItemDescriptionConsts.FAIRY_RAIN_DESCRIPTION_4, "传说可以治愈百病甚至起死回生");
        translationBuilder.add(ItemDescriptionConsts.FAIRY_RAIN_DESCRIPTION_5, "但似乎是谣言");
        translationBuilder.add(ItemDescriptionConsts.PALEO_CREAMY_SMOOTHIE_DESCRIPTION_0, "河童们窖藏的天然冰块");
        translationBuilder.add(ItemDescriptionConsts.PALEO_CREAMY_SMOOTHIE_DESCRIPTION_1, "和牛乳、糖等配料一起在铁桶里打碎");
        translationBuilder.add(ItemDescriptionConsts.PALEO_CREAMY_SMOOTHIE_DESCRIPTION_2, "加速搅拌得到的产物");
        translationBuilder.add(ItemDescriptionConsts.PALEO_CREAMY_SMOOTHIE_DESCRIPTION_3, "是古法制造的冰激凌");
        translationBuilder.add(ItemDescriptionConsts.PALEO_CREAMY_SMOOTHIE_DESCRIPTION_4, "虽然现世已经不再用这种传统方法制作");
        translationBuilder.add(ItemDescriptionConsts.PALEO_CREAMY_SMOOTHIE_DESCRIPTION_5, "但在幻想乡作为特色依旧很受欢迎");
        translationBuilder.add(ItemDescriptionConsts.ORDINARY_FITNESS_TEA_DESCRIPTION_0, "魔女研制的药茶");
        translationBuilder.add(ItemDescriptionConsts.ORDINARY_FITNESS_TEA_DESCRIPTION_1, "据说瘦身功效拔群");
        translationBuilder.add(ItemDescriptionConsts.ORDINARY_FITNESS_TEA_DESCRIPTION_2, "标签上写着“喝掉我”");
        translationBuilder.add(ItemDescriptionConsts.ORDINARY_FITNESS_TEA_DESCRIPTION_3, "虽然有点可疑但它神奇的丰富味道着实令人惊叹");
        translationBuilder.add(ItemDescriptionConsts.DEMON_SLAYER_DESCRIPTION_0, "传说一杯就能让酒量无底洞一般的鬼族醉生梦死的传说之酒…但我见到的是…鬼明明都当做凉白开来喝的？！传说一点都不靠谱啊");
        translationBuilder.add(ItemDescriptionConsts.QI_HEALTH_DESCRIPTION_0, "河童贩卖的外面世界的某种提神功能性饮料的山寨品");
        translationBuilder.add(ItemDescriptionConsts.QI_HEALTH_DESCRIPTION_1, "据说提取了主要成分");
        translationBuilder.add(ItemDescriptionConsts.QI_HEALTH_DESCRIPTION_2, "换个名字谁都可以制作…但这样真的没问题吗？");
        translationBuilder.add(ItemDescriptionConsts.KOMEIJI_ICE_CREAM_DESCRIPTION_0, "地灵殿的限定纪念甜品！以地灵殿的觉妖怪姐妹为原型设计的可爱甜筒");
        translationBuilder.add(ItemDescriptionConsts.KOMEIJI_ICE_CREAM_DESCRIPTION_1, "在地底妖怪中有很大的人气");
        translationBuilder.add(ItemDescriptionConsts.MANGO_POMELO_SAGO_DESCRIPTION_0, "命莲寺特产");
        translationBuilder.add(ItemDescriptionConsts.MANGO_POMELO_SAGO_DESCRIPTION_1, "传说观音菩萨右手持杨枝");
        translationBuilder.add(ItemDescriptionConsts.MANGO_POMELO_SAGO_DESCRIPTION_2, "左手持盛有甘露的净瓶");
        translationBuilder.add(ItemDescriptionConsts.MANGO_POMELO_SAGO_DESCRIPTION_3, "用杨柳枝撒下甘露会带来好运");
        translationBuilder.add(ItemDescriptionConsts.MANGO_POMELO_SAGO_DESCRIPTION_4, "许多慕名而来的信徒都会饮上一杯");
        translationBuilder.add(ItemDescriptionConsts.QILIN_DESCRIPTION_0, "道士们采用外界的技术");
        translationBuilder.add(ItemDescriptionConsts.QILIN_DESCRIPTION_1, "只提取第一道麦汁酿造的啤酒");
        translationBuilder.add(ItemDescriptionConsts.QILIN_DESCRIPTION_2, "因此没有一般啤酒的涩味");
        translationBuilder.add(ItemDescriptionConsts.QILIN_DESCRIPTION_3, "口感更纯更顺");
        translationBuilder.add(ItemDescriptionConsts.HEAVEN_AND_EARTH_ARE_USELESS_DESCRIPTION_0, "鬼人正邪亲自酿造的酒精浓度超级高、据说连鬼都能醉倒的无牌酒");
        translationBuilder.add(ItemDescriptionConsts.HEAVEN_AND_EARTH_ARE_USELESS_DESCRIPTION_1, "在村子没有销路");
        translationBuilder.add(ItemDescriptionConsts.HEAVEN_AND_EARTH_ARE_USELESS_DESCRIPTION_2, "价格还不便宜");
        translationBuilder.add(ItemDescriptionConsts.HEAVEN_AND_EARTH_ARE_USELESS_DESCRIPTION_3, "只能靠手下以半吓半卖的方式销售出去");
        translationBuilder.add(ItemDescriptionConsts.DRUNK_ACTOR_DESCRIPTION_0, "桃花酿的酒");
        translationBuilder.add(ItemDescriptionConsts.DRUNK_ACTOR_DESCRIPTION_1, "妖精之间有着饮一壶桃花酒");
        translationBuilder.add(ItemDescriptionConsts.DRUNK_ACTOR_DESCRIPTION_2, "醉卧花间");
        translationBuilder.add(ItemDescriptionConsts.DRUNK_ACTOR_DESCRIPTION_3, "就会遇到桃花仙的传说");
        translationBuilder.add(ItemDescriptionConsts.DRUNK_ACTOR_DESCRIPTION_4, "这怎么看都是喝醉了吧？");
        translationBuilder.add(ItemDescriptionConsts.DAUGHTER_OF_THE_SEA_DESCRIPTION_0, "传说海的女儿最终为爱化为泡沫");
        translationBuilder.add(ItemDescriptionConsts.DAUGHTER_OF_THE_SEA_DESCRIPTION_1, "这杯有着独特口味的鸡尾酒");
        translationBuilder.add(ItemDescriptionConsts.DAUGHTER_OF_THE_SEA_DESCRIPTION_2, "或许就是她当时流下的、替她回归海中的眼泪");
        translationBuilder.add(ItemDescriptionConsts.DEMONIC_COFFEE_DESCRIPTION_0, "在魔界的烈酒里加入热咖啡");
        translationBuilder.add(ItemDescriptionConsts.DEMONIC_COFFEE_DESCRIPTION_1, "搅拌到融化后");
        translationBuilder.add(ItemDescriptionConsts.DEMONIC_COFFEE_DESCRIPTION_2, "再在顶部盖上一团细腻的奶油");
        translationBuilder.add(ItemDescriptionConsts.DEMONIC_COFFEE_DESCRIPTION_3, "由奶香到酒香再到咖啡香层次分明");
        translationBuilder.add(ItemDescriptionConsts.DEMONIC_COFFEE_DESCRIPTION_4, "口感醇厚");
        translationBuilder.add(ItemDescriptionConsts.DEMONIC_COFFEE_DESCRIPTION_5, "还能驱除一身的寒意");
        translationBuilder.add(ItemDescriptionConsts.MOJITO_BURST_BALL_DESCRIPTION_0, "利用海藻酸钠溶液和氯化钙溶液反应形成一层海藻酸钠凝胶薄膜");
        translationBuilder.add(ItemDescriptionConsts.MOJITO_BURST_BALL_DESCRIPTION_1, "将调好的莫吉托酒包裹在内");
        translationBuilder.add(ItemDescriptionConsts.MOJITO_BURST_BALL_DESCRIPTION_2, "做成一个球状的透明凝胶");
        translationBuilder.add(ItemDescriptionConsts.MOJITO_BURST_BALL_DESCRIPTION_3, "咬一口就爆浆");
        translationBuilder.add(ItemDescriptionConsts.MOJITO_BURST_BALL_DESCRIPTION_4, "达到视觉和味觉的双重惊喜");
        translationBuilder.add(ItemDescriptionConsts.MOJITO_BURST_BALL_DESCRIPTION_5, "看起来非常诱人");
        translationBuilder.add(ItemDescriptionConsts.SPACE_BEER_DESCRIPTION_0, "由航天器搭载到太空的酿酒酵母");
        translationBuilder.add(ItemDescriptionConsts.SPACE_BEER_DESCRIPTION_1, "经过科学家们精心研制");
        translationBuilder.add(ItemDescriptionConsts.SPACE_BEER_DESCRIPTION_2, "将空间酵母技术和现代啤酒工艺相结合");
        translationBuilder.add(ItemDescriptionConsts.SPACE_BEER_DESCRIPTION_3, "酿造出这款口感清爽醇厚、泡沫细密持久、饱含桃花香味的独特啤酒");
        translationBuilder.add(ItemDescriptionConsts.SATELLITE_ICED_COFFEE_DESCRIPTION_0, "咖啡被瞬间暴露于真空中");
        translationBuilder.add(ItemDescriptionConsts.SATELLITE_ICED_COFFEE_DESCRIPTION_1, "会因为过低的气压导致瞬间沸腾");
        translationBuilder.add(ItemDescriptionConsts.SATELLITE_ICED_COFFEE_DESCRIPTION_2, "因汽化现象而被不断带走热量的水最终会在沸腾的过程中凝固");
        translationBuilder.add(ItemDescriptionConsts.SATELLITE_ICED_COFFEE_DESCRIPTION_3, "这种一边沸腾一边冰冻的咖啡是月都的特色饮料");

    }

    public void generateRoleDescriptionTranslations(HolderLookup.Provider wrapperLookup, TranslationBuilder translationBuilder) {
        translationBuilder.add(RoleDescriptionConsts.ROLE_WRIGGLE_NIGHTBUG_DESCRIPTION_0, "喜欢虫子的老友。虽然咱们已经认识了很久，但我还是受不了围在她身边的那群虫子。");
        translationBuilder.add(RoleDescriptionConsts.ROLE_WRIGGLE_NIGHTBUG_DESCRIPTION_1, "这家伙表面上是个萤火虫妖怪，其实是强大的上古虫族的末裔哦，所以才总摆出一副了不起的样子。");
        translationBuilder.add(RoleDescriptionConsts.ROLE_WRIGGLE_NIGHTBUG_DESCRIPTION_2, "我很了解她，虽然看起来脾气不太好，但其实是很为朋友着想的家伙！谁能想到，就是这么个小小的萤火虫，希冀着用自己的微末光芒，孤独又决然地照亮这衰败已久的一族呢？");
        translationBuilder.add(RoleDescriptionConsts.ROLE_RUMIA_DESCRIPTION_0, "喜欢黑暗的老友，总觉得她脑子空空的样子。但不得不说，和我很合得来。无论是第一点还是第二点。");
        translationBuilder.add(RoleDescriptionConsts.ROLE_RUMIA_DESCRIPTION_1, "这家伙总是到处漂荡，我从来都不知道她在做什么。一段时间没见，就能从她口中听到些新鲜的词汇…她到底是去哪了呢？");
        translationBuilder.add(RoleDescriptionConsts.ROLE_RUMIA_DESCRIPTION_2, "相识越久，越觉得自己对她的过去一无所知，就算问她，她也只会说不记得了。不过就算如此，我们也依然是好友，永远都会是好友。");
        translationBuilder.add(RoleDescriptionConsts.ROLE_CHEN_DESCRIPTION_0, "与其说是凶兆的黑猫，不如说是凶巴巴的黑猫…");
        translationBuilder.add(RoleDescriptionConsts.ROLE_CHEN_DESCRIPTION_1, "老是脱口而出要监视我们什么的，就那么怕我们赖账吗？明明一直都很准时还债的啊！");
        translationBuilder.add(RoleDescriptionConsts.ROLE_CHEN_DESCRIPTION_2, "我说，这家伙果然有什么不可告人的目的吧？总觉得她在拙劣地隐瞒着什么…不过这孩子性格这么直，我能感受到她对我是没有恶意的！所以，不管她是不是真的隐瞒了什么，我都会对她一如既往的。");
        translationBuilder.add(RoleDescriptionConsts.ROLE_KAMISHIRASAWA_KEINE_DESCRIPTION_0, "在人类村子开私塾的妖怪。哎呀，总觉得她与其说是妖怪，倒不如说更像是人类呢。");
        translationBuilder.add(RoleDescriptionConsts.ROLE_KAMISHIRASAWA_KEINE_DESCRIPTION_1, "明明是妖怪，却深受人类的信赖，收集了不少信仰心吧？在村民们的心中，她比巫女更可靠。听说还在村子里代理着巫女的工作。");
        translationBuilder.add(RoleDescriptionConsts.ROLE_KAMISHIRASAWA_KEINE_DESCRIPTION_2, "一边担忧着人类，一边担忧着妖怪，真是伟大啊，所谓贤者就是指这样的存在吧？她似乎有着另一个形态，一个我应该没有见过的形态，但却又隐约觉得自己见过…");
        translationBuilder.add(RoleDescriptionConsts.ROLE_REIMU_DESCRIPTION_0, "贫穷且凶狠的巫女小姐…让人怀疑她真的是人类吗？总之，千万要小心不要惹她生气。");
        translationBuilder.add(RoleDescriptionConsts.ROLE_REIMU_DESCRIPTION_1, "灵梦小姐真是一点都不客气啊…不过，不知不觉也受了她不少照顾呢。开始有点明白为什么有那么多妖怪聚集在她身边了。");
        translationBuilder.add(RoleDescriptionConsts.ROLE_REIMU_DESCRIPTION_2, "所谓刀子嘴豆腐心，说的就是灵梦小姐这样的人吧。虽然看起来挺没心没肺的，但那双眼睛却总是能直视前方。博丽的巫女是灵梦小姐真是太好了。");
        translationBuilder.add(RoleDescriptionConsts.ROLE_SUIKA_DESCRIPTION_0, "没想到现在的幻想乡居然还有鬼存在，第一次见到的时候真的吓了一跳！不过…她真的是鬼吗？");
        translationBuilder.add(RoleDescriptionConsts.ROLE_SUIKA_DESCRIPTION_1, "毫无疑问，她就是鬼。虽然平时摆着一副小小的身姿和迷迷糊糊的模样，但言语间的气魄，有时会瞬间像一座山一样压下来。真不愧是传说中的山中王者啊。");
        translationBuilder.add(RoleDescriptionConsts.ROLE_SUIKA_DESCRIPTION_2, "鬼虽然是很可怕的种族，但是一旦成为朋友，就是最值得信赖的伙伴！萃香小姐虽然总是醉醺醺的，但是心思清明着呢。那种豪情逸致，真让人向往啊。");
        translationBuilder.add(RoleDescriptionConsts.ROLE_TENSHI_DESCRIPTION_0, "传说中气焰嚣张、目中无人的不良天人，如果可以的话，真不想招待这样的客人啊…");
        translationBuilder.add(RoleDescriptionConsts.ROLE_TENSHI_DESCRIPTION_1, "我得为之前说过的话道歉，怎么能对客人生出不想招待的念头呢？就算她气焰嚣张、目中无人、口无遮拦、毫无常识、鼻子比天高，我都必须要好好招待她！尽量吧…");
        translationBuilder.add(RoleDescriptionConsts.ROLE_TENSHI_DESCRIPTION_2, "其实，这家伙也不是那么讨厌啦。虽然嘴巴是坏了点（是很坏），但说不定…是个相当天真的家伙呢。似乎在天界过得不是很舒心，如果在地上能让她好好释放压力就好了。");
        translationBuilder.add(RoleDescriptionConsts.ROLE_MARISA_DESCRIPTION_0, "非常开朗健谈的人类魔法使，独自住在人烟罕至的魔法森林里修行，不管是人类、妖精还是妖怪都能轻松与之打成一片。对蘑菇似乎有很深刻的研究！");
        translationBuilder.add(RoleDescriptionConsts.ROLE_MARISA_DESCRIPTION_1, "虽然看起来无所事事，其实也经营着一家“雾雨魔法店”。因为她喜欢收集却从不整理的性格，这家店常常被误以为是垃圾回收站。客人可想而知也是没有的。");
        translationBuilder.add(RoleDescriptionConsts.ROLE_MARISA_DESCRIPTION_2, "虽然性格上有着心术不良的地方，但本性正直，和她一起的感觉很愉快！她从不愿让人看到她努力的样子，但我们都知道…这个自小就离家修行的小女孩，如果可以更多地依靠别人就好了。");
        translationBuilder.add(RoleDescriptionConsts.ROLE_MEIRIN_DESCRIPTION_0, "没想到给恶魔之家的看门的妖怪居然是个热血亲切的大姐！可惜爱睡觉，每次想去找她聊聊都看到她在打盹。");
        translationBuilder.add(RoleDescriptionConsts.ROLE_MEIRIN_DESCRIPTION_1, "美铃小姐独自守卫着这么大的洋馆，连个轮班的都没有，难怪会睡眠不足啦。要是在我力所能及的范围内，能帮到她一点点忙就好了。");
        translationBuilder.add(RoleDescriptionConsts.ROLE_MEIRIN_DESCRIPTION_2, "原来美铃小姐的家乡在海的另一边。但是幻想乡是没有海的，美铃小姐是不是已经回不去了？她的家乡有很多耳熟能详的名菜，我努力学习做给她吃，或许可以缓解一些她的思乡之情。");
        translationBuilder.add(RoleDescriptionConsts.ROLE_CIRNO_DESCRIPTION_0, "自称最强的、长着冰之羽翼的妖精，那样的翅膀也能飞起来真是神奇啊。当然神奇的还有她的脑回路，老是做一些和大家相反的选择，总之别太深究她说的话就好了。");
        translationBuilder.add(RoleDescriptionConsts.ROLE_CIRNO_DESCRIPTION_1, "虽然是冰之妖精，却是个热血的家伙！说不定她真的是最强的妖精，尤其是那股勇往直前的气势，真是见了棺材也不掉泪，撞了南墙也不回头。了不起了不起！");
        translationBuilder.add(RoleDescriptionConsts.ROLE_CIRNO_DESCRIPTION_2, "我发现，和她在一起的时候，会不知不觉就被她的气势带走，这就是领袖的气质吗？！不过这种感觉很轻松，说不定我们很合拍呢！难道说我也是笨蛋吗？哈哈开玩笑啦。");
        translationBuilder.add(RoleDescriptionConsts.ROLE_PATCHOULI_DESCRIPTION_0, "总是穿着睡衣躲在树荫下的奇怪的魔法使。似乎不在意别人的眼光，对潮流什么的也不感兴趣。虽然头脑很好，不过看起来弱不禁风的样子。比起头脑好，身体好更重要啊。");
        translationBuilder.add(RoleDescriptionConsts.ROLE_PATCHOULI_DESCRIPTION_1, "帕秋莉小姐总是手不离书，一直盯着书本看眼睛不会累吗？而且书上说的也不一定都是对的啊，像这样光读书不出门，就算知识渊博，常识却稍微有些不足呢。");
        translationBuilder.add(RoleDescriptionConsts.ROLE_PATCHOULI_DESCRIPTION_2, "师傅原来身患贫血症和哮喘症，真是让人担心啊（我只在这里叫一下应该可以吧？）。虽然认识这么久，师傅对我的态度依然不太亲切，但却教了我不少知识，我可不能辜负她。");
        translationBuilder.add(RoleDescriptionConsts.ROLE_HUZIWARA_NO_MOKOU_DESCRIPTION_0, "隐居在竹林里的不可思议的人类，欲望极低，对世间追逐的一切都没有兴趣。因为曾经吃了什么奇怪的东西而成为不老不死之身（不能乱吃东西啊）。");
        translationBuilder.add(RoleDescriptionConsts.ROLE_HUZIWARA_NO_MOKOU_DESCRIPTION_1, "妹红小姐真是帅气啊，虽然是人类，却强到没边，性格也很豪迈。据说爱吃辣的人性格都很爽快，妹红小姐可是无辣不欢呢。");
        translationBuilder.add(RoleDescriptionConsts.ROLE_HUZIWARA_NO_MOKOU_DESCRIPTION_2, "她比我想象中的要心思细腻，过去似乎经历了很多难以想象的苦难。即便如此，她仍然心存良善，一直暗中帮助别人。这样的她，一定会获得幸福的！");
        translationBuilder.add(RoleDescriptionConsts.ROLE_HOURAISAN_KAGUYA_DESCRIPTION_0, "住在永远亭的公主殿下，据说来自月亮。放着好好的月之公主不做，跑到幻想乡来当家里蹲到底是为什么呢？");
        translationBuilder.add(RoleDescriptionConsts.ROLE_HOURAISAN_KAGUYA_DESCRIPTION_1, "外表看起来是气质优雅的公主殿下，实际行事却有些无赖…有点理解为什么她能成为那只黑兔子的主人了（黑形容的是心哦）…");
        translationBuilder.add(RoleDescriptionConsts.ROLE_HOURAISAN_KAGUYA_DESCRIPTION_2, "原来高贵的公主也会和普通人一样烦恼工作的事情。辉夜小姐和我也没什么不同嘛。作为工作了数载的前辈，我得好好为迷惘的后辈打气呢！");
        translationBuilder.add(RoleDescriptionConsts.ROLE_KAWASIRO_NITORI_DESCRIPTION_0, "如果说河童都是些唯利是图的商人，作为小头目的她更是其中的佼佼者！虽然说商人趋利也没什么好指责的，但是再多一点人情味不是也挺好吗？");
        translationBuilder.add(RoleDescriptionConsts.ROLE_KAWASIRO_NITORI_DESCRIPTION_1, "荷取小姐的头脑真是相当好啊，堪称是商业和技术的天才！一直以为河童性本趋利，可是她们却愿意在技术上一掷千金，我真是搞不懂她们了…");
        translationBuilder.add(RoleDescriptionConsts.ROLE_KAWASIRO_NITORI_DESCRIPTION_2, "能够抓住一切机会、将危机转化为商机的能力，真是让我叹为观止。在她身上学到的一切，值得我用一辈子去消化！");
        translationBuilder.add(RoleDescriptionConsts.ROLE_INUBASHIRI_MOMIZI_DESCRIPTION_0, "常常在妖怪山上巡逻的白狼天狗，嗅觉、听觉非常灵敏，被称为山上的千里眼，能迅速发现入侵者。正因为有这样的守卫在，我们这种小妖怪平时根本不敢上山。");
        translationBuilder.add(RoleDescriptionConsts.ROLE_INUBASHIRI_MOMIZI_DESCRIPTION_1, "因为妖怪之山基本没什么侵入者，所以守卫的工作非常清闲。夜雀食堂开张以后，她对于工作之余多了个去处似乎非常高兴，面上虽然没怎么显露，但尾巴暴露了心情。");
        translationBuilder.add(RoleDescriptionConsts.ROLE_INUBASHIRI_MOMIZI_DESCRIPTION_2, "性格原本也算是一板一眼的，熟悉以后经常能听到她那些毫无城府的牢骚，还时不时能看到她露出乖巧、呆萌的一面。比想象中的好应付啊…早知道这样，我就早点到山上来了！");
        translationBuilder.add(RoleDescriptionConsts.ROLE_KOCHIYA_SANAE_DESCRIPTION_0, "数年前与守矢神社一起突如其来的巫女。据说来自外界，和她一起来的还有两位神明…以及一座湖。这件事实在太离奇了，直到现在都感觉怪吓人的。");
        translationBuilder.add(RoleDescriptionConsts.ROLE_KOCHIYA_SANAE_DESCRIPTION_1, "和喜欢宅在屋里喝茶的博丽巫女不同，早苗小姐非常勤奋，随处都能看到她在努力收集信仰的身影。以人类之身来收集信仰真是了不起啊，似乎是什么现人神呢。");
        translationBuilder.add(RoleDescriptionConsts.ROLE_KOCHIYA_SANAE_DESCRIPTION_2, "博丽神社在幻想乡有着特殊的、绝对的地位，无论是守矢神社还是早苗小姐，想超越博丽之名都很艰难。不过，在早苗小姐的兢兢业业下，现在大家都记住了守矢巫女之名，真是太好了！");
        translationBuilder.add(RoleDescriptionConsts.ROLE_ALICE_DESCRIPTION_0, "住在魔法森林的魔法使，有着金色的头发和白皙的皮肤，外貌精致得就像人偶一样。据说，她的家里也尽是些人偶。被她操纵的那些人偶栩栩如生，感觉还…蛮瘆人的。");
        translationBuilder.add(RoleDescriptionConsts.ROLE_ALICE_DESCRIPTION_1, "这些人偶真的是被操纵的吗？无论是谁见了都会有这样的疑问。虽然她看起来对别人漠不关心，但是待人接物却都非常周到，这就是所谓的都市派吗？");
        translationBuilder.add(RoleDescriptionConsts.ROLE_ALICE_DESCRIPTION_2, "想要走进她的心房真是不容易啊。她矜持而又好客，温柔而又疏离。她之所以制作这么多人偶，会不会是因为孤独呢？");
        translationBuilder.add(RoleDescriptionConsts.ROLE_YATADERA_NARUMI_DESCRIPTION_0, "原本是安置在魔法之森的地藏石像，因为森林的魔力而得到了生命——森林里居然蕴藏着能够将存在本身进行变化的魔力，难怪有那么多魔法使住在这，这对修行来说也太便利了！");
        translationBuilder.add(RoleDescriptionConsts.ROLE_YATADERA_NARUMI_DESCRIPTION_1, "还以为地藏像会更稳重一点，结果也就是刚认识的时候会摆个架子，稍微见多几次面就把话匣子全打开了嘛。是个直率的女孩呢。");
        translationBuilder.add(RoleDescriptionConsts.ROLE_YATADERA_NARUMI_DESCRIPTION_2, "好不容易成为了拥有自我的生命体，结果还是个家里蹲，这不是很可惜吗？！我一定要想办法让这家伙提起干劲！");
        translationBuilder.add(RoleDescriptionConsts.ROLE_KURODANI_YAMAME_DESCRIPTION_0, "在黑暗的风穴遇到的亲切的指路人。因为知道自己惹人讨厌所以几乎不会到村里去，明明那么可爱…真希望地上的大家也能认识她呢！");
        translationBuilder.add(RoleDescriptionConsts.ROLE_KURODANI_YAMAME_DESCRIPTION_1, "山女小姐原来是类似地底偶像一样的存在呢！不愧是她。不过，和她那可爱的性格相对的是，她的爱好似乎异于常人…");
        translationBuilder.add(RoleDescriptionConsts.ROLE_KURODANI_YAMAME_DESCRIPTION_2, "虽然一直被误会乃至成为“被厌恶者”，山女小姐也并不在意。我能感觉到，她确实深爱着这个地底世界。能有这样豁达的心境，真是了不起的境界啊。");
        translationBuilder.add(RoleDescriptionConsts.ROLE_MIZUHASHI_PARSEE_DESCRIPTION_0, "原本以为她是个亲切开朗的妖怪，结果一转身就看到她在背地里说我坏话…以嫉妒为食的妖怪，不煽动别人的嫉妒心就会活不下去吗？感觉很可怜呐。不过她似乎不这么觉得。性格也阴晴不定，真是难以理解。");
        translationBuilder.add(RoleDescriptionConsts.ROLE_MIZUHASHI_PARSEE_DESCRIPTION_1, "桥姬既是桥的妖怪，也是桥的守护神。帕露西小姐其实在日复一日地守望着他人平安地从地上世界和地下世界往来…这么说来，她也是个很伟大的妖怪呢。");
        translationBuilder.add(RoleDescriptionConsts.ROLE_MIZUHASHI_PARSEE_DESCRIPTION_2, "我发现，帕露西小姐熟知所有人的优点！虽然大家都说帕露西小姐很善妒，但她的内心深处其实很憧憬大家吧？能够更进一步了解她真是太好了！");
        translationBuilder.add(RoleDescriptionConsts.ROLE_HOSHIGUMA_YUGI_DESCRIPTION_0, "很久以前就离开了幻想乡、有着大姐头气魄的鬼，性格刚毅且干脆利落。如今在管理着旧地狱街道上的温泉。偶尔去泡一泡温泉吧！");
        translationBuilder.add(RoleDescriptionConsts.ROLE_HOSHIGUMA_YUGI_DESCRIPTION_1, "原来勇仪小姐曾经是山中四天王之一。传闻说她有异常程度的怪力，腕力即便在鬼之中也几乎无可匹敌。没想到我也能结识到这么厉害的妖怪…果然还是有点紧张啊。");
        translationBuilder.add(RoleDescriptionConsts.ROLE_HOSHIGUMA_YUGI_DESCRIPTION_2, "本来以为鬼已经对人类失去兴趣…但勇仪小姐其实在偷偷羡慕着在地上自由往来的同伴的身影。既然对地上感兴趣，那就去地上玩吧！鬼就该是这样嘛！");
        translationBuilder.add(RoleDescriptionConsts.ROLE_KOMEIJI_SATORI_DESCRIPTION_0, "传说中能够读心的觉妖怪，名字也是觉。大概是对自己的种族或能力很自豪吧？因为日以继夜都在工作，所以没什么搭话的机会…");
        translationBuilder.add(RoleDescriptionConsts.ROLE_KOMEIJI_SATORI_DESCRIPTION_1, "觉小姐真是我见过最操劳的领主了。虽然有宠物们帮忙干杂活，但是行政方面的工作似乎只有她能做。为了工作似乎放弃了很多日常爱好，唯独不能放弃读心。累到虚脱可不好啊。");
        translationBuilder.add(RoleDescriptionConsts.ROLE_KOMEIJI_SATORI_DESCRIPTION_2, "我明白为什么宠物们那么依恋觉小姐了。被繁重的工作埋没，心中还能一直牵挂着妹妹和宠物。在清冷的外表下的这份温柔，确实是会让人念念不忘呢。");
        translationBuilder.add(RoleDescriptionConsts.ROLE_KAENBYOU_RIN_DESCRIPTION_0, "被称作火车的猫妖，是地灵殿之主的左右手。能够和尸体和怨灵对话似乎很方便！不过…说到火车就是偷尸体啊。就是因为这样所以被厌恶吧？唔唔，对别人的兴趣说三道四似乎不太好…");
        translationBuilder.add(RoleDescriptionConsts.ROLE_KAENBYOU_RIN_DESCRIPTION_1, "仔细想想，人类刚死掉的时候，应该会茫然吧？如果能在猝然长辞之际遇到开朗善谈的阿燐，或许能中和一些对死亡的恐惧。");
        translationBuilder.add(RoleDescriptionConsts.ROLE_KAENBYOU_RIN_DESCRIPTION_2, "活在偏见下的“凶兆的黑猫”，原来要经历那么艰辛的事。偏见真是不可取啊。幸好阿燐遇到了觉小姐。一个好的主人可以改变宠物的一生。希望阿燐今后可以一直过着无忧无虑地偷尸体的生活。");
        translationBuilder.add(RoleDescriptionConsts.ROLE_REIUJI_UTSUH_DESCRIPTION_0, "看守灼热地狱遗址的地狱鸦，有着火焰般热烈爽快的性格。外表看起来和其他地狱鸦不太一样，唯独脑袋空空这一点让我感到很亲切。");
        translationBuilder.add(RoleDescriptionConsts.ROLE_REIUJI_UTSUH_DESCRIPTION_1, "阿空的外表之所以大放异彩，原来是因为她的体内寄宿了神灵八咫乌！自地狱的黑暗诞生而来的地狱鸦，和栖息于太阳的八咫乌，真是奇妙的组合啊！不过…让她拥有这么强大的力量真的没问题吗？");
        translationBuilder.add(RoleDescriptionConsts.ROLE_REIUJI_UTSUH_DESCRIPTION_2, "最近突然觉得，没有比阿空更适合拥有这种力量的人选了。拥有异常强大的力量，却没有使用这种力量的头脑，这就是一种幸运吧？脑袋空空的阿空很可爱哦！不过我也常被说脑袋空空呢。");
        translationBuilder.add(RoleDescriptionConsts.ROLE_TATARA_KOGASA_DESCRIPTION_0, "寄宿在怪异唐伞上的灵，因为一直没有人使用，不知不觉就付丧神化了。是个只要见到别人被吓了一跳，就会快乐得手舞足蹈的家伙。");
        translationBuilder.add(RoleDescriptionConsts.ROLE_TATARA_KOGASA_DESCRIPTION_1, "这家伙的性格好麻烦啊！本来以为是个开朗亲切的妖怪，结果动不动就哭鼻子…我拿这种家伙最没办法啦！好在她总是很快就能振作起来！");
        translationBuilder.add(RoleDescriptionConsts.ROLE_TATARA_KOGASA_DESCRIPTION_2, "尽管偶尔会露出悲观的一面，但她内心深处却是个爽朗坚毅的妖怪。虽然总是遭遇挫折，却总是花样层出地努力着呢。这份能量，希望我也可以拥有！");
        translationBuilder.add(RoleDescriptionConsts.ROLE_HOUJUU_NUE_DESCRIPTION_0, "古老的真相不明的妖怪——鵺。长得和传说完全不一样真可惜…虽然也是命莲寺的弟子，但总是见到她独自在寺院附近无所事事地游荡，似乎不太合群的样子。");
        translationBuilder.add(RoleDescriptionConsts.ROLE_HOUJUU_NUE_DESCRIPTION_1, "她就像是被忽视、通过恶作剧来吸引大家注意力的别扭的孩子。内心希望融入寺中，却笨挫地不知道该怎么表达…明明活了这么久，还是不谙世故呢。");
        translationBuilder.add(RoleDescriptionConsts.ROLE_HOUJUU_NUE_DESCRIPTION_2, "越是在意就越容易畏缩…结果她的内心就和她的形象一样无法被人看清，这难道就是“无法明状之物”的寂寞吗？但她如今已经展露出自己的真相，大家以后也一定会慢慢理解她的率真的！");
        translationBuilder.add(RoleDescriptionConsts.ROLE_MONONOBE_NO_FUTO_DESCRIPTION_0, "通过长时间自己诅咒自己从而沉眠的古代人。大概是睡了太久，思考方式也跟古时候一样，和普通人完全脱节…目前在努力学习现代知识，结果来说变得更加不伦不类了。");
        translationBuilder.add(RoleDescriptionConsts.ROLE_MONONOBE_NO_FUTO_DESCRIPTION_1, "如果不提她那套落伍的作风，其实她也蛮有仙人那种风格，尤其对风水什么的似乎很有研究。我是搞不懂啦…要不下次装修的时候请她帮忙看看好了。");
        translationBuilder.add(RoleDescriptionConsts.ROLE_MONONOBE_NO_FUTO_DESCRIPTION_2, "无法完全割舍过去，就说明对过去还留有怀念吧？尽管这样，布都小姐还是毅然决然地成为尸解仙的实验品。我虽然无法理解人类的这份忠心，但也感到非常敬佩！");
        translationBuilder.add(RoleDescriptionConsts.ROLE_KAKU_SEIGA_DESCRIPTION_0, "经常见到她在神庙附近晃荡嬉玩的样子，似乎对修行不太热心…但又十分热衷于向别人传达修道的好处。实在搞不明白她到底在想什么。是个看起来很正统，行为和思想却很另类的仙人。");
        translationBuilder.add(RoleDescriptionConsts.ROLE_KAKU_SEIGA_DESCRIPTION_1, "想要做成一件事就会不计后果地去达成，就算自己的行为不被人理解，她也会摆出一副理所当然的态度。要说她是被欲望驱使，又觉得恰恰相反…她所做的那些事，与其说是为了欲望，或许只是单纯的性格使然。");
        translationBuilder.add(RoleDescriptionConsts.ROLE_KAKU_SEIGA_DESCRIPTION_2, "邪仙不一定就是邪恶的仙人。仅仅由于思考和行动都不够妥当就被断绝天人之路，确实是让我有些费解…我只知道青娥小姐非常有个性！如果成为天人就要泯灭个性，那我希望青娥小姐还是一直保持这个样子比较好。");
        translationBuilder.add(RoleDescriptionConsts.ROLE_SOGA_NO_TOZIKO_DESCRIPTION_0, "古代人的亡灵。过去似乎怀有很深的怨恨，所以性质上是妖怪们最害怕的怨灵。有点不太敢接触她呢…");
        translationBuilder.add(RoleDescriptionConsts.ROLE_SOGA_NO_TOZIKO_DESCRIPTION_1, "实际接触后，发现屠自古小姐的性格确实是直爽又豁达，还充满活力。一点也不像是怨灵呢。大概是怨恨已经消散得差不多了吧？能让心胸豁达的屠自古小姐变成怨灵，一定是相当痛苦的事情…");
        translationBuilder.add(RoleDescriptionConsts.ROLE_SOGA_NO_TOZIKO_DESCRIPTION_2, "没想到没想到，让屠自古小姐变成怨灵的元凶竟然是布都小姐！可她们两人的感情还是很要好的样子？！屠自古小姐的心胸何止是豁达的程度，她大概离成佛只有一步之遥了。");
        translationBuilder.add(RoleDescriptionConsts.ROLE_AYA_DESCRIPTION_0, "每天在幻想乡各处奔走，收集题材来写成新闻的鸦天狗。也是大名鼎鼎的《文文。新闻》的作者。之所以说大名鼎鼎，并不是这个报纸有多出色，而是派发得实在太多了…");
        translationBuilder.add(RoleDescriptionConsts.ROLE_AYA_DESCRIPTION_1, "本来就很强的她，再加上有新闻这种强力的武器，导致文文小姐在幻想乡里经常不被待见。不过看着如此拼命工作的她，感觉也会受到鼓舞呢。如果她的新闻能少些胡扯就好了。");
        translationBuilder.add(RoleDescriptionConsts.ROLE_AYA_DESCRIPTION_2, "报纸真的是非常伟大的东西！文文小姐创作的报刊，记录了幻想乡的过去，虽然稍微有些失真的地方…但也还是有值得回忆的地方！希望文文小姐最终能够找到适合她的新闻风格。");
        translationBuilder.add(RoleDescriptionConsts.ROLE_KAZAMI_YUKA_DESCRIPTION_0, "虽然看起来是一副高雅的淑女形象，但不知道为什么，很多人将她视作“花之暴君”。虽然恐怖的传说满天飞，但大家其实都没有见过她欺负人类或者弱小妖怪。真实的她，究竟是一位怎样的人呢？");
        translationBuilder.add(RoleDescriptionConsts.ROLE_KAZAMI_YUKA_DESCRIPTION_1, "只要见过她照顾花儿的样子，那些有关她的谣言就会不攻自破。但我还是不明白，她有那么强大的妖力，为什么会容忍谣言满天飞，为什么要默默地承受那些恶意中伤呢？");
        translationBuilder.add(RoleDescriptionConsts.ROLE_KAZAMI_YUKA_DESCRIPTION_2, "大错特错！她从来都没有“忍受”过那些谣言，因为她根本就不在乎。于是连澄清都变得多此一举。幽香小姐最为强大之处，正是她的心。我想，并不是强大的妖力让她睥睨众人，真正让她立于不败之地的，是她纯粹又干净的自我。");
        translationBuilder.add(RoleDescriptionConsts.ROLE_KIJIN_SEIJIA_DESCRIPTION_0, "擅长花言巧语的天邪鬼。打着要颠倒幻想乡的口号，蛊惑了许多年轻气盛的妖怪甚至人类来当手下，但却因为资金不足无法善待他们，甚至找天狗刊登广告骗游客来收取过路费…无论怎么看，都是个乱来的家伙。");
        translationBuilder.add(RoleDescriptionConsts.ROLE_KIJIN_SEIJIA_DESCRIPTION_1, "正邪小姐真的想下克上吗？感觉她对造反成功之后的事情并没什么兴趣。或许她只是从和社会作对汲取乐趣，并不是真的想改变这个社会。");
        translationBuilder.add(RoleDescriptionConsts.ROLE_KIJIN_SEIJIA_DESCRIPTION_2, "看着同伴一个接一个的离开，一般人多少都会觉得此路不通吧？但是正邪小姐完全没有想过放弃。在如今人人都悠闲地过着普通日子的幻想乡中，她是少有的不达目的绝不罢休的那一类。从某种程度来说，还蛮让我敬佩的…");
        translationBuilder.add(RoleDescriptionConsts.ROLE_SUKUNA_SHINMYOUMARU_DESCRIPTION_0, "非常崇拜一寸法师的小人族的城主，性格克制认真，是一个有着英雄梦的勇者厨。向往冒险的深闺公主还真是新鲜呢。擅长和菓子这一点和气质很相衬！");
        translationBuilder.add(RoleDescriptionConsts.ROLE_SUKUNA_SHINMYOUMARU_DESCRIPTION_1, "明明很向往勇者的冒险，也做了非常多的准备，但是却迟迟没有踏上旅程。对于能否留下完美的传说这件事非常在意。会不会有些在意过头了…");
        translationBuilder.add(RoleDescriptionConsts.ROLE_SUKUNA_SHINMYOUMARU_DESCRIPTION_2, "终于解开心结的针妙丸小姐勇敢地踏上了道路，而那些“凭着一股勇气就可以拯救世界”的故事就是她的精神道标。从她身上我也明白到，太在意结果就会寸步难行，果然还是随心而动比较适合我！");
        translationBuilder.add(RoleDescriptionConsts.ROLE_IMAIZUMI_KAGEROU_DESCRIPTION_0, "稳重温婉的狼女，给人一种亲切的邻家大姐姐的感觉。不知是不是因为月人在竹林里住下的缘故，那里和月亮相关的妖怪似乎很多，影狼小姐也是其中一员。");
        translationBuilder.add(RoleDescriptionConsts.ROLE_IMAIZUMI_KAGEROU_DESCRIPTION_1, "影狼小姐似乎很在意皮肤，这么爱美的狼人还真是第一次见呢。草根妖怪的日常真是平和又热闹。偶尔忍不住也会想…如果我不开店，大概也在过着这样的生活吧。");
        translationBuilder.add(RoleDescriptionConsts.ROLE_IMAIZUMI_KAGEROU_DESCRIPTION_2, "影狼小姐作为狼的部分，是在外面世界已经绝种的本州狼。一度被敬仰、被崇拜，最后却落得灭绝下场真可怜。幻想乡的人类和妖怪要多多爱护我们兽（禽）类呀！");
        translationBuilder.add(RoleDescriptionConsts.ROLE_REISEN_DESCRIPTION_0, "似乎因为我来自地上，所以对我很亲切的月之使者，和永远亭的铃仙小姐有着一样的名字。偶尔会表现出对地上的向往，不知道是不是过去发生了什么…也可能是因为月之使者的训练太辛苦了吧。");
        translationBuilder.add(RoleDescriptionConsts.ROLE_REISEN_DESCRIPTION_1, "原来铃仙曾经是一只捣药兔，因为厌烦无休止的捣药逃到幻想乡而发生了许多事，所以才成为一名月之使者。难怪她会对地上表现出眷恋。不过她似乎也不喜欢月之使者的工作，感觉她从内到外都过得好辛苦啊…");
        translationBuilder.add(RoleDescriptionConsts.ROLE_REISEN_DESCRIPTION_2, "一直活在后悔中是一种怎样的心情呢？如果时间可以重来，她就不会从捣药的工作中逃走了吗？那样她就会感到幸福吗？这些问题我都没能问出口…不过，与其徘徊在过去，还是向前看比较好吧？希望她有朝一日能得到真正的自由。");
        translationBuilder.add(RoleDescriptionConsts.ROLE_WATATSUKI_TOYOHIME_DESCRIPTION_0, "绵月家的姐姐。性格和蔼温柔，偶尔会露出孩子气的一面。除了偶尔会在月之使者训练时给兔子们带小点心，几乎没有参与月之使者的训练，因此很受月兔的喜爱。");
        translationBuilder.add(RoleDescriptionConsts.ROLE_WATATSUKI_TOYOHIME_DESCRIPTION_1, "丰姬小姐真是相当——相当喜欢桃子，以及相当——相当优哉游哉。像她这么强大的人，真的可以这样什么都不用操心吗？无论在什么环境下都能活得这么天真，这应该也算是一种能力吧？");
        translationBuilder.add(RoleDescriptionConsts.ROLE_WATATSUKI_TOYOHIME_DESCRIPTION_2, "原来丰姬小姐心里也有想要完成的事呢。她是从什么时候开始产生想要隐退的想法呢？或许从一开始，她就只是被迫继承月之使者首领的工作吧。至今没有隐退，一来是没有合适的继任者，二来…恐怕还是为了依姬小姐吧？");
        translationBuilder.add(RoleDescriptionConsts.ROLE_REMILIA_DESCRIPTION_0, "红魔馆的主人。远远看去还以为是个幼童，走近却被她的气势压得不敢说话，不愧是响彻一方的吸血鬼领主！但任性起来真的和小孩子很像…");
        translationBuilder.add(RoleDescriptionConsts.ROLE_REMILIA_DESCRIPTION_1, "虽然她还不太成熟，但她背负着许多人的命运。明明是那么小小的肩膀，却把一大家子全都庇护在自己的羽翼下，还真有点一家之主的感觉。蕾米小姐，也不完全像大家认为的那样以自我为中心呢。");
        translationBuilder.add(RoleDescriptionConsts.ROLE_YOUMU_DESCRIPTION_0, "侍奉着冥界之主的庭师兼侍卫、厨师。也就是说，她不仅要一个人照顾两百由旬的西行寺庭院，还要守卫西行寺家以及主人的安危，同时还要照顾大胃王主人的饮食需求！无论哪一个都是重担呢，希望她能在夜雀食堂得到一些治愈吧！");
        translationBuilder.add(RoleDescriptionConsts.ROLE_YUYUKO_DESCRIPTION_0, "差点吞噬了幻想乡的可怕的家伙。原以为恢复神志后就会世界和平，结果一见面就想吃了我？！现在偶尔也会到食堂用餐，有时候会看着我咽口水…我这辈子都不想再见到这个亡灵。");
        translationBuilder.add(RoleDescriptionConsts.ROLE_KOMEIJI_KOISHI_DESCRIPTION_0, "地灵殿之主的妹妹，封闭了第三只眼的觉！由于封闭了内心所以存在感也变得很低，神秘的身影让她在地底收获了许多追崇者。总是挂着灿烂的笑容，但谁也无法知道她真正的内心是怎么想的。就连她自己也不知道。");
        translationBuilder.add(RoleDescriptionConsts.ROLE_HUTATSUIWA_MAMIZOU_DESCRIPTION_0, "妖怪狸的首领，有着相应的威严和领导风范，擅长变幻术。不过再如何擅长变幻的妖兽，想要完全藏起尾巴和耳朵都是不可能的。另外尾巴的大小其实是妖力的计量表，或许她从最初开始就没有藏起自己尾巴的打算。");
        translationBuilder.add(RoleDescriptionConsts.ROLE_YUKARI_DESCRIPTION_0, "创造了幻想乡的贤者之一，据说是幻想乡最古老的妖怪，虽然神出鬼没而且性格有点不近人情，但其实一直以来暗中维护着幻想乡的安全和稳定…虽然没有得到多数人的理解，但恐怕也没有像她一样真挚地对待幻想乡、爱着幻想乡的人了。");
        translationBuilder.add(RoleDescriptionConsts.ROLE_FLANDRE_DESCRIPTION_0, "传说中非常非常可怕的吸血鬼，妖精客人们一直提醒我要小心……虽然看起来明明是个小孩子嘛。");
        translationBuilder.add(RoleDescriptionConsts.ROLE_FLANDRE_DESCRIPTION_1, "芙兰朵露小姐的心智…似乎真的就像一个没接触过世界的小孩子呢。到底是吸血鬼的长寿天赋导致了心智发育缓慢，还是极少接触外人所导致的呢？");
        translationBuilder.add(RoleDescriptionConsts.ROLE_FLANDRE_DESCRIPTION_2, "其实我还挺同情芙兰朵露小姐的。她的力量似乎成为了一份不可能挣脱的束缚，即使包括蕾米莉亚小姐在内的所有人的本意都是好的，可惜这不是一个能被“爱”或者“友善”改变的命题。不过，就这段时间的相处而言，“伙伴”似乎可以是答案之一哦？至于证明，就交给时间吧。");
        translationBuilder.add(RoleDescriptionConsts.ROLE_ERIN_DESCRIPTION_0, "隐居在竹林深处的谜之存在，浑身散发着高贵但平和的气质，总是表现出在思考着什么的样子。");
        translationBuilder.add(RoleDescriptionConsts.ROLE_ERIN_DESCRIPTION_1, "实际上是个很善良很温和的人啊。明明是“月之头脑”这么大的来头来着…完全感受不到架子呢，不过举手投足之间的高贵风范依然引人侧目就是了。");
        translationBuilder.add(RoleDescriptionConsts.ROLE_ERIN_DESCRIPTION_2, "永远亭，月亮的子民，不死身…永琳小姐原来身上承担了这么多责任吗？明明看起来只是一个技术超群的医师的说。可能所谓的“大人物”就是“承担着大责任的人物”吧？使命什么的…对于鸟脑袋来说比天上的月亮还让人摸不清楚啊。");
        translationBuilder.add(RoleDescriptionConsts.ROLE_RAN_DESCRIPTION_0, "操控式神的式神妖怪，似乎是橙的监护人。明明是妖力很强的妖怪，却不管什么时候都是笑眯眯的。");
        translationBuilder.add(RoleDescriptionConsts.ROLE_RAN_DESCRIPTION_1, "式神什么的果然还是想不明白…有着不为人知的过去，还和那个饕餮尤魔有联系？明明是性格天差地别的两个人。");
        translationBuilder.add(RoleDescriptionConsts.ROLE_RAN_DESCRIPTION_2, "哇，原来还发生过这种事情。式神什么的，我好像也明白一点了。蓝小姐照顾橙，照顾紫小姐，性格温柔又体贴，真希望她能不这么辛苦。");

    }

    public void generateRoleChatTranslations(HolderLookup.Provider wrapperLookup, TranslationBuilder translationBuilder) {
        translationBuilder.add(RoleChatConsts.ROLE_WRIGGLE_NIGHTBUG_CHAT_0, "我作为邻居来捧场了哦老板娘！");
        translationBuilder.add(RoleChatConsts.ROLE_WRIGGLE_NIGHTBUG_CHAT_1, "别这么看着我！我来到店里并不会影响店铺的卫生啦！");
        translationBuilder.add(RoleChatConsts.ROLE_WRIGGLE_NIGHTBUG_CHAT_2, "你家附近的虫子？和我没关系啦！");
        translationBuilder.add(RoleChatConsts.ROLE_WRIGGLE_NIGHTBUG_CHAT_3, "为了虫族的复兴！现在我们推出“昆虫的通知”——为什么要让我闭嘴！");
        translationBuilder.add(RoleChatConsts.ROLE_WRIGGLE_NIGHTBUG_CHAT_4, "夜晚的萤火虫不是很美好吗？驻足多看一会吧。");
        translationBuilder.add(RoleChatConsts.ROLE_RUMIA_CHAT_0, "“黑夜给了我们黑色的眼睛”，不就是为了让人寻找黑暗嘛。");
        translationBuilder.add(RoleChatConsts.ROLE_RUMIA_CHAT_1, "你看我像不像圣人被钉在十字架上？");
        translationBuilder.add(RoleChatConsts.ROLE_RUMIA_CHAT_2, "你这里太亮了，我的眼睛有点不舒服。");
        translationBuilder.add(RoleChatConsts.ROLE_RUMIA_CHAT_3, "最近有些人喊我三小姐，你知道这是为什么吗？");
        translationBuilder.add(RoleChatConsts.ROLE_RUMIA_CHAT_4, "为了来你这里就餐，我连身边的黑暗都减弱了。");
        translationBuilder.add(RoleChatConsts.ROLE_CHEN_CHAT_0, "我才不是嘴馋，我只是来监督你的。");
        translationBuilder.add(RoleChatConsts.ROLE_CHEN_CHAT_1, "你要好好工作，一定要按时把债务完成！");
        translationBuilder.add(RoleChatConsts.ROLE_CHEN_CHAT_2, "别以为给我做点好吃的就可以放宽贷款哦。");
        translationBuilder.add(RoleChatConsts.ROLE_CHEN_CHAT_3, "下次要不要把我家的猫咪们也带过来呢？");
        translationBuilder.add(RoleChatConsts.ROLE_CHEN_CHAT_4, "吃不下的食物可以打包吗？我想带给我家猫咪吃。");
        translationBuilder.add(RoleChatConsts.ROLE_KAMISHIRASAWA_KEINE_CHAT_0, "食物的历史也是非常久远的。");
        translationBuilder.add(RoleChatConsts.ROLE_KAMISHIRASAWA_KEINE_CHAT_1, "总觉得，满月时的记忆似乎不太完整…");
        translationBuilder.add(RoleChatConsts.ROLE_KAMISHIRASAWA_KEINE_CHAT_2, "单纯的事件是不能称为历史的，只有将其记载下来，方为历史。");
        translationBuilder.add(RoleChatConsts.ROLE_KAMISHIRASAWA_KEINE_CHAT_3, "最近孩子们对课程似乎有点意兴阑珊，是不是应该再增加些课程呢？");
        translationBuilder.add(RoleChatConsts.ROLE_KAMISHIRASAWA_KEINE_CHAT_4, "有时候，料理中承载的不止酸甜苦辣，还有思念。");
        translationBuilder.add(RoleChatConsts.ROLE_REIMU_CHAT_0, "明明是妖怪开的店，为什么人比神社都多？！");
        translationBuilder.add(RoleChatConsts.ROLE_REIMU_CHAT_1, "既然是面向人类经营，卫生方面可要给我好好注意。");
        translationBuilder.add(RoleChatConsts.ROLE_REIMU_CHAT_2, "今天没有闹事的吧？发生什么要及时告诉我。");
        translationBuilder.add(RoleChatConsts.ROLE_REIMU_CHAT_3, "看着妖怪和人类共食，身为巫女感觉还是挺微妙的。");
        translationBuilder.add(RoleChatConsts.ROLE_REIMU_CHAT_4, "这种好像挺赚钱啊，我要不要也开一家店呢？");
        translationBuilder.add(RoleChatConsts.ROLE_SUIKA_CHAT_0, "最近宴会次数减少了啊，看来有什么在悄悄地发生着吧？");
        translationBuilder.add(RoleChatConsts.ROLE_SUIKA_CHAT_1, "你的事我很清楚哦，因为一直看着呢。");
        translationBuilder.add(RoleChatConsts.ROLE_SUIKA_CHAT_2, "解嘲破惑有常言，酒不醉人人自醉。");
        translationBuilder.add(RoleChatConsts.ROLE_SUIKA_CHAT_3, "喝酒就是要在这种热闹的地方才有味道嘛。");
        translationBuilder.add(RoleChatConsts.ROLE_SUIKA_CHAT_4, "差不多该觉察到心中的压力了吧。");
        translationBuilder.add(RoleChatConsts.ROLE_TENSHI_CHAT_0, "虽然在我看来是个破落的小店，但在地上也算是不错了吧。");
        translationBuilder.add(RoleChatConsts.ROLE_TENSHI_CHAT_1, "天界那群老顽固，一点都不懂变通！");
        translationBuilder.add(RoleChatConsts.ROLE_TENSHI_CHAT_2, "地上虽然比不上天界的富饶，不过勉强还是能呆一阵子的。");
        translationBuilder.add(RoleChatConsts.ROLE_TENSHI_CHAT_3, "有本天人来光顾的店，那些愚民们一定会争先恐后地过来吧。");
        translationBuilder.add(RoleChatConsts.ROLE_TENSHI_CHAT_4, "地上稀奇古怪的东西真不少，虽然不入流，倒也挺有趣的。");
        translationBuilder.add(RoleChatConsts.ROLE_MARISA_CHAT_0, "放心吧，我只是喜欢偷…借书，从来不吃霸王餐。");
        translationBuilder.add(RoleChatConsts.ROLE_MARISA_CHAT_1, "可恶，今天又输给灵梦那个家伙了呀！");
        translationBuilder.add(RoleChatConsts.ROLE_MARISA_CHAT_2, "偶尔像这样吃一顿也不错Da☆Ze！");
        translationBuilder.add(RoleChatConsts.ROLE_MARISA_CHAT_3, "这么多人看着，我不会跑单的啦！");
        translationBuilder.add(RoleChatConsts.ROLE_MARISA_CHAT_4, "魔法就是光和热，料理其实也差不多吧。");
        translationBuilder.add(RoleChatConsts.ROLE_MEIRIN_CHAT_0, "我可没有偷懒哦——虽然确实是偷偷溜出来的。");
        translationBuilder.add(RoleChatConsts.ROLE_MEIRIN_CHAT_1, "咲夜小姐的飞刀就像是长了眼睛一样。");
        translationBuilder.add(RoleChatConsts.ROLE_MEIRIN_CHAT_2, "大小姐什么时候才把那本漫画的下册借回来呢？");
        translationBuilder.add(RoleChatConsts.ROLE_MEIRIN_CHAT_3, "得赶在被女仆长发现前回去。");
        translationBuilder.add(RoleChatConsts.ROLE_MEIRIN_CHAT_4, "饭前饭后都不适宜运动，稍微打个盹就是最合适不过了。");
        translationBuilder.add(RoleChatConsts.ROLE_CIRNO_CHAT_0, "本小姐是最强的！");
        translationBuilder.add(RoleChatConsts.ROLE_CIRNO_CHAT_1, "嘿嘿，我的冰可是连飞鸟都能打落哦！");
        translationBuilder.add(RoleChatConsts.ROLE_CIRNO_CHAT_2, "能不能别把我当成随处可见的普通妖精？");
        translationBuilder.add(RoleChatConsts.ROLE_CIRNO_CHAT_3, "泡菜的话，腌之前就吃掉好了。");
        translationBuilder.add(RoleChatConsts.ROLE_CIRNO_CHAT_4, "中场休息！捉迷藏什么的吃饱再继续玩吧。");
        translationBuilder.add(RoleChatConsts.ROLE_PATCHOULI_CHAT_0, "生活就是魔法意大利面的结合。");
        translationBuilder.add(RoleChatConsts.ROLE_PATCHOULI_CHAT_1, "被念叨得烦了，就姑且出来走走吧。");
        translationBuilder.add(RoleChatConsts.ROLE_PATCHOULI_CHAT_2, "我的书并没有乱放，所有的书都在它们自己想呆的位置上。");
        translationBuilder.add(RoleChatConsts.ROLE_PATCHOULI_CHAT_3, "魔法的力量，其实就是科学那种东西。");
        translationBuilder.add(RoleChatConsts.ROLE_PATCHOULI_CHAT_4, "烹饪和魔法的结合，或许会很有趣呢。");
        translationBuilder.add(RoleChatConsts.ROLE_HUZIWARA_NO_MOKOU_CHAT_0, "不老不死只意味着，永不得救赎的孤独。");
        translationBuilder.add(RoleChatConsts.ROLE_HUZIWARA_NO_MOKOU_CHAT_1, "炭火不够的话，尽管来找我！");
        translationBuilder.add(RoleChatConsts.ROLE_HUZIWARA_NO_MOKOU_CHAT_2, "要是拥有了前往月亮的方法，那家伙会返回月亮吗…");
        translationBuilder.add(RoleChatConsts.ROLE_HUZIWARA_NO_MOKOU_CHAT_3, "从不尽的火诞生的，是无论多少次都会复活的不死鸟。");
        translationBuilder.add(RoleChatConsts.ROLE_HUZIWARA_NO_MOKOU_CHAT_4, "不要随地乱扔烟蒂啊。");
        translationBuilder.add(RoleChatConsts.ROLE_HOURAISAN_KAGUYA_CHAT_0, "不劳者不得食，我是不是也该做点什么呢？");
        translationBuilder.add(RoleChatConsts.ROLE_HOURAISAN_KAGUYA_CHAT_1, "那五个美丽的难题，能不能解决已经无所谓了。");
        translationBuilder.add(RoleChatConsts.ROLE_HOURAISAN_KAGUYA_CHAT_2, "我不是家里蹲，只是永琳每次都要嘱咐半天所以我才懒得出门。");
        translationBuilder.add(RoleChatConsts.ROLE_HOURAISAN_KAGUYA_CHAT_3, "幻想乡的雨PH值为6哦，就是几乎不含酸性的意思。");
        translationBuilder.add(RoleChatConsts.ROLE_HOURAISAN_KAGUYA_CHAT_4, "七色的优昙花很美，七色的料理感觉却很恶心呢。");
        translationBuilder.add(RoleChatConsts.ROLE_KAWASIRO_NITORI_CHAT_0, "只有嗅觉敏锐的商人，才能将商业情报作用发挥到极致。");
        translationBuilder.add(RoleChatConsts.ROLE_KAWASIRO_NITORI_CHAT_1, "当你没有决定要成功时，你就已经决定要失败了。");
        translationBuilder.add(RoleChatConsts.ROLE_KAWASIRO_NITORI_CHAT_2, "船在港湾里很安全，但这不是造船的目的。");
        translationBuilder.add(RoleChatConsts.ROLE_KAWASIRO_NITORI_CHAT_3, "潜在顾客不在乎你。他们在乎自己，对与他们无关的东西他们不愿读也不愿听。");
        translationBuilder.add(RoleChatConsts.ROLE_KAWASIRO_NITORI_CHAT_4, "对实业家而言只有一条规则：以尽可能低的成本，做质量最好的产品，付尽可能高的工资。");
        translationBuilder.add(RoleChatConsts.ROLE_KAWASIRO_NITORI_CHAT_5, "是不是也应该让一些河童专门去专研厨艺呢…");
        translationBuilder.add(RoleChatConsts.ROLE_INUBASHIRI_MOMIZI_CHAT_0, "今天那局棋真是恰逢敌手啊。这样才有意思！");
        translationBuilder.add(RoleChatConsts.ROLE_INUBASHIRI_MOMIZI_CHAT_1, "即使没有入侵者，也万不可掉以轻心。");
        translationBuilder.add(RoleChatConsts.ROLE_INUBASHIRI_MOMIZI_CHAT_2, "都说河童是天才，但无论是剑术还是棋术都是我的手下败将。");
        translationBuilder.add(RoleChatConsts.ROLE_INUBASHIRI_MOMIZI_CHAT_3, "组织规模越大，需要的规则越多。");
        translationBuilder.add(RoleChatConsts.ROLE_INUBASHIRI_MOMIZI_CHAT_4, "那只鸦天狗不知道又在打什么主意…");
        translationBuilder.add(RoleChatConsts.ROLE_KOCHIYA_SANAE_CHAT_0, "总觉得河童们最近是不是兴奋得有些异常…");
        translationBuilder.add(RoleChatConsts.ROLE_KOCHIYA_SANAE_CHAT_1, "现在的我，应该能被幻想乡的大家记住了吧？");
        translationBuilder.add(RoleChatConsts.ROLE_KOCHIYA_SANAE_CHAT_2, "我要让守矢之名响彻幻想乡！");
        translationBuilder.add(RoleChatConsts.ROLE_KOCHIYA_SANAE_CHAT_3, "那两位大人都是好事却不爱凑热闹的人呢。");
        translationBuilder.add(RoleChatConsts.ROLE_KOCHIYA_SANAE_CHAT_4, "虽说吃宵夜会长胖…不过我今天走了很多地方，应该没问题！");
        translationBuilder.add(RoleChatConsts.ROLE_ALICE_CHAT_0, "偶尔是不是也回去一趟比较好呢…");
        translationBuilder.add(RoleChatConsts.ROLE_ALICE_CHAT_1, "最近人偶损耗得有点厉害，等下回去得好好修补一下。");
        translationBuilder.add(RoleChatConsts.ROLE_ALICE_CHAT_2, "明天表演什么人偶剧比较好呢…");
        translationBuilder.add(RoleChatConsts.ROLE_ALICE_CHAT_3, "这家店开了才知道竟然有这么多魔法使…");
        translationBuilder.add(RoleChatConsts.ROLE_YATADERA_NARUMI_CHAT_0, "人间烟火气，最抚凡人心。");
        translationBuilder.add(RoleChatConsts.ROLE_YATADERA_NARUMI_CHAT_1, "满肚子的食物，人才不会空虚。");
        translationBuilder.add(RoleChatConsts.ROLE_YATADERA_NARUMI_CHAT_2, "酸甜苦辣都是滋味，每顿饭都值得被用心对待。");
        translationBuilder.add(RoleChatConsts.ROLE_YATADERA_NARUMI_CHAT_3, "四方食事，不过一碗人间烟火。");
        translationBuilder.add(RoleChatConsts.ROLE_YATADERA_NARUMI_CHAT_4, "本来无东西，何处有南北。");
        translationBuilder.add(RoleChatConsts.ROLE_KURODANI_YAMAME_CHAT_0, "被地上厌恶的地底世界，是我们的天堂。");
        translationBuilder.add(RoleChatConsts.ROLE_KURODANI_YAMAME_CHAT_1, "洞窟虽然是黑暗的，但是我的网很明亮哦。");
        translationBuilder.add(RoleChatConsts.ROLE_KURODANI_YAMAME_CHAT_2, "最近街道那边好热闹呀，偶尔去玩玩吧。");
        translationBuilder.add(RoleChatConsts.ROLE_KURODANI_YAMAME_CHAT_3, "只有抵住最黑的暗，才能收获最光的亮。");
        translationBuilder.add(RoleChatConsts.ROLE_MIZUHASHI_PARSEE_CHAT_0, "你站在桥上看风景，看风景的人在楼上看你。");
        translationBuilder.add(RoleChatConsts.ROLE_MIZUHASHI_PARSEE_CHAT_1, "所有人看起来都这么幸福，我好嫉妒啊…");
        translationBuilder.add(RoleChatConsts.ROLE_MIZUHASHI_PARSEE_CHAT_2, "桥从此端通往彼端的连系，被寓意着从现世通往彼世的路途。");
        translationBuilder.add(RoleChatConsts.ROLE_MIZUHASHI_PARSEE_CHAT_3, "为什么隔壁的菜看起来那么美味，我好嫉妒啊…");
        translationBuilder.add(RoleChatConsts.ROLE_HOSHIGUMA_YUGI_CHAT_0, "一个两个都往地上钻，地上真的那么有趣吗？");
        translationBuilder.add(RoleChatConsts.ROLE_HOSHIGUMA_YUGI_CHAT_1, "酒就是越热闹才越好喝。");
        translationBuilder.add(RoleChatConsts.ROLE_HOSHIGUMA_YUGI_CHAT_2, "酒杯就不必了，我的酒器是随身携带的。");
        translationBuilder.add(RoleChatConsts.ROLE_HOSHIGUMA_YUGI_CHAT_3, "真热闹啊，比嗓门的话没有人能比得过鬼。");
        translationBuilder.add(RoleChatConsts.ROLE_KOMEIJI_SATORI_CHAT_0, "距离太远的话，就读不到了…");
        translationBuilder.add(RoleChatConsts.ROLE_KOMEIJI_SATORI_CHAT_1, "赶紧吃完回去工作吧。");
        translationBuilder.add(RoleChatConsts.ROLE_KOMEIJI_SATORI_CHAT_2, "好不容易挤出一些时间，也该犒劳一下自己。");
        translationBuilder.add(RoleChatConsts.ROLE_KOMEIJI_SATORI_CHAT_3, "美食不仅是味觉感受，更是一种精神享受。");
        translationBuilder.add(RoleChatConsts.ROLE_KAENBYOU_RIN_CHAT_0, "以阿空的性格来说，要时时刻刻盯着核熔炉的火力，真是难为她了。");
        translationBuilder.add(RoleChatConsts.ROLE_KAENBYOU_RIN_CHAT_1, "今天送来的怨灵有点多啊。");
        translationBuilder.add(RoleChatConsts.ROLE_KAENBYOU_RIN_CHAT_2, "下次叫上觉大人一起来吧。");
        translationBuilder.add(RoleChatConsts.ROLE_KAENBYOU_RIN_CHAT_3, "觉大人有好好吃饭吗…");
        translationBuilder.add(RoleChatConsts.ROLE_REIUJI_UTSUH_CHAT_0, "等待美食出炉的时间是快乐的。");
        translationBuilder.add(RoleChatConsts.ROLE_REIUJI_UTSUH_CHAT_1, "享受美食的时间是最快乐的。");
        translationBuilder.add(RoleChatConsts.ROLE_REIUJI_UTSUH_CHAT_2, "天要下雨，菜要下饭。");
        translationBuilder.add(RoleChatConsts.ROLE_REIUJI_UTSUH_CHAT_3, "脑子里装不下的东西，就用肚子来装。");
        translationBuilder.add(RoleChatConsts.ROLE_TATARA_KOGASA_CHAT_0, "我又来了！吓~一跳了吗？");
        translationBuilder.add(RoleChatConsts.ROLE_TATARA_KOGASA_CHAT_1, "这里变得更热闹了！好开心啊——！");
        translationBuilder.add(RoleChatConsts.ROLE_TATARA_KOGASA_CHAT_2, "伺机寻找目标…好！一会儿就吓那个家伙吧！");
        translationBuilder.add(RoleChatConsts.ROLE_TATARA_KOGASA_CHAT_3, "今天又没有吓到人…");
        translationBuilder.add(RoleChatConsts.ROLE_HOUJUU_NUE_CHAT_0, "你听说过幽灵食堂的都市传说吗？");
        translationBuilder.add(RoleChatConsts.ROLE_HOUJUU_NUE_CHAT_1, "最近的人类是不是都缺根筋？");
        translationBuilder.add(RoleChatConsts.ROLE_HOUJUU_NUE_CHAT_2, "孤独才是享受美食的最好状态。");
        translationBuilder.add(RoleChatConsts.ROLE_HOUJUU_NUE_CHAT_3, "好不容易找到的容身之处，我绝不离开。");
        translationBuilder.add(RoleChatConsts.ROLE_MONONOBE_NO_FUTO_CHAT_0, "平淡无奇的锅碗瓢盆里，盛满了人类的一生。");
        translationBuilder.add(RoleChatConsts.ROLE_MONONOBE_NO_FUTO_CHAT_1, "若能让吾把这个盘子摔了，店里的生意会更上一层楼。");
        translationBuilder.add(RoleChatConsts.ROLE_MONONOBE_NO_FUTO_CHAT_2, "后厨的火力够吗？需要吾帮忙添一把火吗？");
        translationBuilder.add(RoleChatConsts.ROLE_MONONOBE_NO_FUTO_CHAT_3, "与火为伴的厨子生活说不定挺适合吾…");
        translationBuilder.add(RoleChatConsts.ROLE_KAKU_SEIGA_CHAT_0, "小麻雀离烟火气有点太近了呢…");
        translationBuilder.add(RoleChatConsts.ROLE_KAKU_SEIGA_CHAT_1, "我可爱的部下们受你关照了哟。");
        translationBuilder.add(RoleChatConsts.ROLE_KAKU_SEIGA_CHAT_2, "四方食事，不过一碗人间烟火。");
        translationBuilder.add(RoleChatConsts.ROLE_KAKU_SEIGA_CHAT_3, "想吃仙人的妖怪不少，给仙人做饭的还是第一次见呢。");
        translationBuilder.add(RoleChatConsts.ROLE_SOGA_NO_TOZIKO_CHAT_0, "三餐正常，四餐满意。");
        translationBuilder.add(RoleChatConsts.ROLE_SOGA_NO_TOZIKO_CHAT_1, "美食和风景，可以抵抗全世界所有的悲伤和迷惘。");
        translationBuilder.add(RoleChatConsts.ROLE_SOGA_NO_TOZIKO_CHAT_2, "死了以后就什么都不用担心了。");
        translationBuilder.add(RoleChatConsts.ROLE_SOGA_NO_TOZIKO_CHAT_3, "遇酒且呵呵，人生能几何！");
        translationBuilder.add(RoleChatConsts.ROLE_AYA_CHAT_0, "今天的素材也不太能用呢…");
        translationBuilder.add(RoleChatConsts.ROLE_AYA_CHAT_1, "想要获得情报就要深入群众。");
        translationBuilder.add(RoleChatConsts.ROLE_AYA_CHAT_2, "劳逸结合才是长久之道哟。");
        translationBuilder.add(RoleChatConsts.ROLE_AYA_CHAT_3, "新闻和做菜一样，加点料更香。");
        translationBuilder.add(RoleChatConsts.ROLE_KAZAMI_YUKA_CHAT_0, "我对无聊的战斗不感兴趣。");
        translationBuilder.add(RoleChatConsts.ROLE_KAZAMI_YUKA_CHAT_1, "花儿们看起来也很高兴。");
        translationBuilder.add(RoleChatConsts.ROLE_KAZAMI_YUKA_CHAT_2, "这里从来没有过这样的氛围。");
        translationBuilder.add(RoleChatConsts.ROLE_KAZAMI_YUKA_CHAT_3, "花间有酒，独酌何碍。");
        translationBuilder.add(RoleChatConsts.ROLE_KIJIN_SEIJIA_CHAT_0, "温温吞吞永远都无法革命成功。");
        translationBuilder.add(RoleChatConsts.ROLE_KIJIN_SEIJIA_CHAT_1, "弱小的妖怪们，和我一起站起来吧！");
        translationBuilder.add(RoleChatConsts.ROLE_KIJIN_SEIJIA_CHAT_2, "总有一天我会把你收入麾下。");
        translationBuilder.add(RoleChatConsts.ROLE_KIJIN_SEIJIA_CHAT_3, "依恋“家的味道”也太软弱了！");
        translationBuilder.add(RoleChatConsts.ROLE_SUKUNA_SHINMYOUMARU_CHAT_0, "来这里就可以听到好故事吗？");
        translationBuilder.add(RoleChatConsts.ROLE_SUKUNA_SHINMYOUMARU_CHAT_1, "辉针城从来没有这么热闹过…");
        translationBuilder.add(RoleChatConsts.ROLE_SUKUNA_SHINMYOUMARU_CHAT_2, "她应该也常来这里吧？");
        translationBuilder.add(RoleChatConsts.ROLE_SUKUNA_SHINMYOUMARU_CHAT_3, "谢谢你给这座寂寞的城带来尘嚣。");
        translationBuilder.add(RoleChatConsts.ROLE_IMAIZUMI_KAGEROU_CHAT_0, "太晚睡对皮肤不好…");
        translationBuilder.add(RoleChatConsts.ROLE_IMAIZUMI_KAGEROU_CHAT_1, "节食减肥绝对不是美丽之道。");
        translationBuilder.add(RoleChatConsts.ROLE_IMAIZUMI_KAGEROU_CHAT_2, "大晚上的感觉也好热呀…");
        translationBuilder.add(RoleChatConsts.ROLE_IMAIZUMI_KAGEROU_CHAT_3, "是不是应该把头发也剪一下呢…");
        translationBuilder.add(RoleChatConsts.ROLE_IMAIZUMI_KAGEROU_CHAT_4, "老板娘辛苦了。");
        translationBuilder.add(RoleChatConsts.ROLE_REISEN_CHAT_0, "今天也好累啊…");
        translationBuilder.add(RoleChatConsts.ROLE_REISEN_CHAT_1, "忙碌的一天终于结束了！");
        translationBuilder.add(RoleChatConsts.ROLE_REISEN_CHAT_2, "现在是犒赏自己的时间！");
        translationBuilder.add(RoleChatConsts.ROLE_REISEN_CHAT_3, "肌肉好酸痛。");
        translationBuilder.add(RoleChatConsts.ROLE_WATATSUKI_TOYOHIME_CHAT_0, "偶尔也要吃吃桃子以外的呢。");
        translationBuilder.add(RoleChatConsts.ROLE_WATATSUKI_TOYOHIME_CHAT_1, "吃喜欢的东西，过可爱的人生。");
        translationBuilder.add(RoleChatConsts.ROLE_WATATSUKI_TOYOHIME_CHAT_2, "今天的第五顿饭启动！");
        translationBuilder.add(RoleChatConsts.ROLE_WATATSUKI_TOYOHIME_CHAT_3, "吃货最高境界就是眼见为食。");
        translationBuilder.add(RoleChatConsts.ROLE_REMILIA_CHAT_0, "芙兰那孩子的精力真是用不尽啊…");
        translationBuilder.add(RoleChatConsts.ROLE_REMILIA_CHAT_1, "一会也带点回去给芙兰尝尝吧。");
        translationBuilder.add(RoleChatConsts.ROLE_REMILIA_CHAT_2, "这家店的命运说不定已经悄悄地改变了。");
        translationBuilder.add(RoleChatConsts.ROLE_REMILIA_CHAT_3, "和我们家的女仆比起来还差得远呢。");
        translationBuilder.add(RoleChatConsts.ROLE_YOUMU_CHAT_0, "终于可以轻松地来光顾了！");
        translationBuilder.add(RoleChatConsts.ROLE_YOUMU_CHAT_1, "之前太失礼了，老板娘能不计前嫌真是太好了。");
        translationBuilder.add(RoleChatConsts.ROLE_YOUMU_CHAT_2, "唉，侍奉幽幽子大人累得快让我的骨头散架了。");
        translationBuilder.add(RoleChatConsts.ROLE_YOUMU_CHAT_3, "老板娘如果有什么需要帮忙的，尽管开口！");
        translationBuilder.add(RoleChatConsts.ROLE_YOUMU_CHAT_4, "如果冥界也有一家食堂那我就轻松多了。");
        translationBuilder.add(RoleChatConsts.ROLE_YUYUKO_CHAT_0, "我对梦里的味道念念不忘，所以就来了。");
        translationBuilder.add(RoleChatConsts.ROLE_YUYUKO_CHAT_1, "唔，麻雀的小碎骨有点太多了…");
        translationBuilder.add(RoleChatConsts.ROLE_YUYUKO_CHAT_2, "这家店的老板娘真的不能吃吗？好可惜…");
        translationBuilder.add(RoleChatConsts.ROLE_YUYUKO_CHAT_3, "不要紧张，把我视为普通客人就好了哟。");
        translationBuilder.add(RoleChatConsts.ROLE_YUYUKO_CHAT_4, "进食的时候被这样警惕地盯着，我会不好意思的哦。");
        translationBuilder.add(RoleChatConsts.ROLE_KOMEIJI_KOISHI_CHAT_0, "贪吃不胖，美梦不空。");
        translationBuilder.add(RoleChatConsts.ROLE_KOMEIJI_KOISHI_CHAT_1, "只要填饱肚子就不会感到空虚啦！");
        translationBuilder.add(RoleChatConsts.ROLE_KOMEIJI_KOISHI_CHAT_2, "怪诞评审好酷——啊呀！那不就是我嘛！");
        translationBuilder.add(RoleChatConsts.ROLE_KOMEIJI_KOISHI_CHAT_3, "等啊等——会等来什么呢。");
        translationBuilder.add(RoleChatConsts.ROLE_HUTATSUIWA_MAMIZOU_CHAT_0, "叶子变的钱非常便利哦。");
        translationBuilder.add(RoleChatConsts.ROLE_HUTATSUIWA_MAMIZOU_CHAT_1, "这种传统与现代交融的风格颇合老朽心意。");
        translationBuilder.add(RoleChatConsts.ROLE_HUTATSUIWA_MAMIZOU_CHAT_2, "老朽还年轻着呐。");
        translationBuilder.add(RoleChatConsts.ROLE_HUTATSUIWA_MAMIZOU_CHAT_3, "那边的…嚯嚯嚯，这么拙劣的变身可骗不了老朽。");
        translationBuilder.add(RoleChatConsts.ROLE_YUKARI_CHAT_0, "久违地出来吃饭了呢…");
        translationBuilder.add(RoleChatConsts.ROLE_YUKARI_CHAT_1, "接下来可以睡个长觉了。");
        translationBuilder.add(RoleChatConsts.ROLE_YUKARI_CHAT_2, "一路走来真是辛苦你了。");
        translationBuilder.add(RoleChatConsts.ROLE_YUKARI_CHAT_3, "你的努力我都看在眼里。");
        translationBuilder.add(RoleChatConsts.ROLE_FLANDRE_CHAT_0, "要一起玩嘛？");
        translationBuilder.add(RoleChatConsts.ROLE_FLANDRE_CHAT_1, "蓝蓝的，绿绿的，都是外界的颜色诶。");
        translationBuilder.add(RoleChatConsts.ROLE_FLANDRE_CHAT_2, "我是我，四个我加在一起也是我！");
        translationBuilder.add(RoleChatConsts.ROLE_FLANDRE_CHAT_3, "外面的世界看起来好脆弱…");
        translationBuilder.add(RoleChatConsts.ROLE_ERIN_CHAT_0, "今夜的月亮比昨夜的更明亮。");
        translationBuilder.add(RoleChatConsts.ROLE_ERIN_CHAT_1, "满月究竟代表祥瑞还是代表灾厄？");
        translationBuilder.add(RoleChatConsts.ROLE_ERIN_CHAT_2, "公主想必也会喜欢这里的氛围。");
        translationBuilder.add(RoleChatConsts.ROLE_ERIN_CHAT_3, "人和妖怪原来可以以这种方式达成平衡吗…");
        translationBuilder.add(RoleChatConsts.ROLE_RAN_CHAT_0, "一会儿吃完饭要给橙买木天蓼，给紫大人买吃的…");
        translationBuilder.add(RoleChatConsts.ROLE_RAN_CHAT_1, "最近压力有点大，掉毛变多了。");
        translationBuilder.add(RoleChatConsts.ROLE_RAN_CHAT_2, "这个味道…她怎么会来这里？");
        translationBuilder.add(RoleChatConsts.ROLE_RAN_CHAT_3, "我可爱的橙，现在看见尸体就兴高采烈的…");

    }

    public void generateConfigTranslations(HolderLookup.Provider wrapperLookup, TranslationBuilder translationBuilder) {
        translationBuilder.add("text.autoconfig.reverie_dreams.option.title", "梦隐的幻想乡 | 配置文件");
        translationBuilder.add("text.autoconfig.reverie_dreams.option.configVersion", "Configuration File Version");
        translationBuilder.add("text.autoconfig.reverie_dreams.option.configVersion.@Tooltip", "Do not modify");
        translationBuilder.add("text.autoconfig.reverie_dreams.option.checkUpdate", "Enable Update Checker");
        translationBuilder.add("text.autoconfig.reverie_dreams.option.checkUpdate.@Tooltip", "If enabled, the mod will automatically check for new versions.");
        translationBuilder.add("text.autoconfig.reverie_dreams.option.debugMode", "Enable Debug Mode");
        translationBuilder.add("text.autoconfig.reverie_dreams.option.debugMode.@Tooltip", "Shows extra debug logs and developer-only information.");
        translationBuilder.add("text.autoconfig.reverie_dreams.option.enableDanmakuGlow", "Enable Glow Effect for Danmaku Items");
        translationBuilder.add("text.autoconfig.reverie_dreams.option.enableDanmakuGlow.@Tooltip", "Replaces vanilla torch item overlay to display a glowing outline.");
        translationBuilder.add("text.autoconfig.reverie_dreams.option.maxUpgradedHealthValue", "Maximum Upgraded Health");
        translationBuilder.add("text.autoconfig.reverie_dreams.option.maxUpgradedHealthValue.@Tooltip", "Defines the highest total health value players can reach through upgrades.");
        translationBuilder.add("text.autoconfig.reverie_dreams.option.maxCustomerTickTime", "Maximum customer wait time");
        translationBuilder.add("text.autoconfig.reverie_dreams.option.maxCustomerTickTime.@Tooltip", "Defines maximum customer wait time.");
        translationBuilder.add("text.autoconfig.reverie_dreams.option.enableYouseiSpawn", "Enable Yousei Spawning");
        translationBuilder.add("text.autoconfig.reverie_dreams.option.enableYouseiSpawn.@Tooltip", "Toggle to allow Yousei to spawn naturally.");
        translationBuilder.add("text.autoconfig.reverie_dreams.option.enableGhostSpawn", "Enable Ghost Spawning");
        translationBuilder.add("text.autoconfig.reverie_dreams.option.enableGhostSpawn.@Tooltip", "Toggle to allow Ghosts to spawn naturally.");
        translationBuilder.add("text.autoconfig.reverie_dreams.option.enableAIReplacesGeneralChat", "Enable AI chat to replace interaction");
        translationBuilder.add("text.autoconfig.reverie_dreams.option.enableAIReplacesGeneralChat.@Tooltip", "Toggle to replace general chat by AIChat");
        translationBuilder.add("text.autoconfig.reverie_dreams.option.apiUrl", "AI Chat API URL");
        translationBuilder.add("text.autoconfig.reverie_dreams.option.apiUrl.@Tooltip", "Set Chat API url");
        translationBuilder.add("text.autoconfig.reverie_dreams.option.apiKey", "AI Chat API KEY");
        translationBuilder.add("text.autoconfig.reverie_dreams.option.apiKey.@Tooltip", "Set Chat API key");
        translationBuilder.add("text.autoconfig.reverie_dreams.option.model", "AI Chat Model");
        translationBuilder.add("text.autoconfig.reverie_dreams.option.model.@Tooltip", "Set Chat API Model");
        translationBuilder.add("text.autoconfig.reverie_dreams.option.chatType", "Set Chat API Type: openai/deepseek/gemini/claude");
        translationBuilder.add("text.autoconfig.reverie_dreams.option.chatType.@Tooltip", "AI Chat Type");
        translationBuilder.add("text.autoconfig.reverie_dreams.option.autoUpdateItemTag", "Enable Auto Refresh Item Tag");
        translationBuilder.add("text.autoconfig.reverie_dreams.option.autoUpdateItemTag.@Tooltip", "Toggle to Enable Auto Refresh Item Tag");
        translationBuilder.add("text.autoconfig.reverie_dreams.option.reportResourceReloadError", "Enable Set Report");
        translationBuilder.add("text.autoconfig.reverie_dreams.option.reportResourceReloadError.@Tooltip", "Toggle to report Resource Error");
    }

    public void generateGuideTranslations(HolderLookup.Provider wrapperLookup, TranslationBuilder translationBuilder) {
        translationBuilder.add("book_page.reverie_dreams.root.title", "梦隐的幻想乡指南");
        translationBuilder.add("book_page.reverie_dreams.root.description", "欢迎游玩梦隐的幻想乡，本页面将帮助您作为游玩本模组的指南导航");
        translationBuilder.add(BookPageManagerImpl.titleLangKey(DefaultBookPages.BASIC_GETTING_STARTED), "入门指南");
        translationBuilder.add(BookPageManagerImpl.contentLangKey(DefaultBookPages.BASIC_GETTING_STARTED), """
                梦隐的幻想乡是一个魔法冒险类的东方 Project 模组，融合了其他优秀东方同人模组及游戏的灵感，为现代 Minecraft 增添了大量东方 Project 的内容。
                
                要开始游玩它，首先要制作§e幻想乡祭坛§r，它可以制作§e蓬莱玉枝§r、§e神枪冈格尼尔§r和§e博丽御币§r等物品。
                
                - §ePower§r和§bPoint§r是基本的合成材料之一，它们可以通过击杀怪物或妖精获得。
                
                之后，你可以获得并合成更多物品，你可以在本指南中了解更多信息，或者在Polydex中查看它们的配方。
                """);
        translationBuilder.add(BookPageManagerImpl.titleLangKey(DefaultBookPages.BASIC_GENSOKYO_ALTAR), "幻想乡祭坛摆放");
        translationBuilder.add(BookPageManagerImpl.contentLangKey(DefaultBookPages.BASIC_GENSOKYO_ALTAR), """
                §e幻想乡工作台§r是本模组中重要的合成工作方块，大部分物品都需要通过它进行合成。
                该祭坛支持由九种物品组成的合成配方结构。
                要使其正常工作，还需要搭建如下结构：
                
                该结构四周的柱子需使用：§e去皮绳文杉木§r，这种树通常可以在§a热带草原§r中找到。
                顶层木头需要使用'纸'对其进行右键祝福。
                如需解除祝福状态，可使用剪刀右键取消祝福。
                最后，在确认配方无误后，使用“空手 + Shift + 右键”完成合成。
                """);
        translationBuilder.add(BookPageManagerImpl.titleLangKey(DefaultBookPages.BASIC_DANMAKU_TUTORIAL), "弹幕指南");
        translationBuilder.add(BookPageManagerImpl.contentLangKey(DefaultBookPages.BASIC_DANMAKU_TUTORIAL), """
                §e弹幕§r指的是 STG 游戏中常见的弹幕机制，即通过大量密集的弹体构成复杂攻击轨迹的表现形式。
                在本模组中，“弹幕”同样沿用这一概念。
                
                玩家需要使用§e弹幕工作台§r来合成弹幕，其材料摆放如下：
                
                - 任意染料 ×4
                - 弹幕核心 ×4
                - Power ×35
                - Point ×35
                - 弹幕创作模板 ×1
                
                §e弹幕创作模板§r是用于设置弹幕形态的关键工具，也是合成弹幕不可或缺的一部分。
                玩家可以通过右键模板，以点阵方式构建弹幕形态。
                
                不同类型的弹幕具有各自的属性，但所有弹幕都可以在§e强化台§r中进行强化升级。
                目前已定义的升级材料及其对应效果如下：
                
                - 铁剑 - 提升弹幕伤害
                - 粘液块 - 增加单次发射数量
                - 速度羽毛 - 提高弹幕射击速度
                - 符卡模板 - 设置弹幕轨迹
                
                """);
        translationBuilder.add(BookPageManagerImpl.titleLangKey(DefaultBookPages.BASIC_FUMO_TUTORIAL), "Fumo玩偶");
        translationBuilder.add(BookPageManagerImpl.contentLangKey(DefaultBookPages.BASIC_FUMO_TUTORIAL), """
                §aFumo（ふもふも）§r特指由日本 Gift 公司出品、基于同人社团 ANGELTYPE 角色设计的《东方Project》主题毛绒玩偶。
                其核心特征包括：坐姿造型、标志性的呆滞表情（ᗜˬᗜ）、柔软的毛绒触感，以及经授权制作的精致 Q 版形象，是东方圈中极具代表性的疗愈系周边。
                
                在本模组中，大多数 Fumo 可在生存模式下通过价格较高的交易获得。
                放置后，Fumo 会自动朝向玩家当前所在位置，并支持 16 个方向的摆放；右键点击 Fumo 方块时，还会发出对应的 Fumo 声音。
                
                玩家只能通过与 Fumo 商人交易来获取 Fumo。通常情况下，Fumo 商人的交易内容每天都会发生变化。
                
                Fumo 商人可通过使用§bFumo销售许可§r对村民右键，将其转变为对应职业。
                
                """);
        translationBuilder.add(BookPageManagerImpl.titleLangKey(DefaultBookPages.BASIC_ROLE_AND_PARTNER), "角色伙伴");
        translationBuilder.add(BookPageManagerImpl.contentLangKey(DefaultBookPages.BASIC_ROLE_AND_PARTNER), """
                §a角色伙伴（Role）§r是本 Mod 中的随从类实体，其功能类似于 Touhou Little Maid 模组中的女仆单位。
                
                角色伙伴具备背包、战斗、工作以及互动等多种功能。通常情况下，每个角色伙伴都需要一位主人，玩家可以通过喂食蛋糕来完成驯服并建立主从关系。
                
                在生存模式下，玩家可以在§e幻想乡工作台§r中合成各类东方 Project 角色卡，以此召唤对应的角色伙伴。
                
                当角色伙伴死亡时，会掉落§e角色存档卡§r。玩家可以在§e幻想乡工作台§r中摆放该物品，并在配方中围绕两圈钻石，以重新复活对应角色。
                
                其他操作说明：
                - 鼠标右键 - 触发日常对话
                - Shift + 鼠标左键（空手） - 停止攻击目标
                - Shift + 鼠标右键 - 打开背包
                
                """);
        translationBuilder.add(BookPageManagerImpl.titleLangKey(DefaultBookPages.BASIC_TOUHOU_MYSTIA), "东方夜雀食堂");
        translationBuilder.add(BookPageManagerImpl.contentLangKey(DefaultBookPages.BASIC_TOUHOU_MYSTIA), """
                §a《东方夜雀食堂》§r是一款东方 Project 同人游戏，本 Mod 也将其作为一项独立玩法引入游戏中。
                该系统主要添加了原作中的食材、料理、酒水以及烹饪配方，并对 Minecraft 原版的部分掉落物进行了调整。
                
                在本 Mod 中，大部分食材都拥有对应的农作物来源；部分种子可通过开启宝箱获得，而海产品则需要通过钓鱼或采集获取。
                每种食物都带有不同的 Tag，不同 Tag 会赋予食物不同的效果。
                
                -- 烹饪 --
                
                烹饪系统的厨具主要分为五类：§e煮锅§r、§e料理台§r、§e油锅§r、§e烧烤架§r以及§e蒸锅§r，并均存在对应的升级版本。
                
                当配方输入不恰当或冲突词条时，可能会生成§e黑暗物质§r。
                
                部分配方支持使用与本 Mod 兼容的其他模组物品进行等效替代。
                所有烹饪配方可通过本 Mod 的配方管理器或夜雀食堂维基进行查询。
                
                -- 酒水 --
                
                大多数酒水可通过与酒馆老板交易获得。饮用后会提供不同的效果与增益，且每日可交易的酒水种类可能发生变化。
                
                不过，§e绿茶§r、§e果味High Ball§r、§e果味Sour§r以及§e淇§r始终会出现在售卖列表中。
                
                玩家可以手持木桶对村民右键，将其转职为酒馆老板。
                """);
        translationBuilder.add(BookPageManagerImpl.titleLangKey(DefaultBookPages.OTHER_COMPAT), "Mod 兼容");
        translationBuilder.add(BookPageManagerImpl.contentLangKey(DefaultBookPages.OTHER_COMPAT), """
                §f本页面用于列出与本模组相关的其他模组兼容情况及推荐搭配。
                
                Suggestion
                
                以下模组为推荐搭配，可显著提升游戏体验或功能完整性：
                
                - Polydex ：提供基础的服务端配方查询功能
                - Polymer ：优化整体游戏体验
                - JEI ：提供配方与物品列表查询功能
                - REI ：提供配方与物品列表查询功能
                - RRV ：提供配方与物品列表查询功能
                
                -- 兼容 --
                
                以下模组与本模组存在兼容内容或联动效果：
                
                - Create Fly ：为角色伙伴添加摇曲柄相关功能
                - PolyFactory ：为角色伙伴添加摇曲柄相关功能
                - Polymer Patch For Gensokyo: Reverie of Lost Dreams ：为仅服务端运行环境添加了支持
                
                以下模组可为烹饪系统提供额外兼容食材或配方支持：
                
                - Borukva Food
                - Borukva Food Exotic
                - Borukva Fish
                - Farmer's Delight Refabricated
                - More Delight
                - Ocean's Delight (Polymer Port)
                - Spanish Delight Refabricated
                - Fishing 101
                - Gone Fishing!
                - 森罗物语
                
                """);
    }

    public void generateAdvancementsTranslations(HolderLookup.Provider wrapperLookup, TranslationBuilder translationBuilder) {
        TranslationWrapper builder = ITranslationWrapper.ofWrapper(wrapperLookup, translationBuilder);
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
        TranslationWrapper builder = ITranslationWrapper.ofWrapper(wrapperLookup, translationBuilder);
        translationBuilder.add("block.feedback.working", "§c该厨具正在工作中...");

        translationBuilder.add("item.tooltip.food_properties", "属性：");
//        translationBuilder.add("item_group.kitchenware_item_group", "梦隐的幻想乡 - 厨具");
//        translationBuilder.add("item_group.ingredients_item_group", "梦隐的幻想乡 - 食材");
//        translationBuilder.add("item_group.seed_item_group", "梦隐的幻想乡 - 种子");
//        translationBuilder.add("item_group.food_item_group", "梦隐的幻想乡 - 食物");
//        translationBuilder.add("item_group.drink_item_group", "梦隐的幻想乡 - 饮品");

        translationBuilder.add(RDKitchenBlocks.COOKING_POT.asBlock(), "煮锅");
        translationBuilder.add(RDKitchenBlocks.CUTTING_BOARD.asBlock(), "料理台");
        translationBuilder.add(RDKitchenBlocks.FRYING_PAN.asBlock(), "油锅");
        translationBuilder.add(RDKitchenBlocks.GRILL.asBlock(), "烧烤架");
        translationBuilder.add(RDKitchenBlocks.STEAMER.asBlock(), "蒸锅");
        translationBuilder.add(RDKitchenBlocks.MYSTIA_COOKING_POT.asBlock(), "夜雀⦁煮锅");
        translationBuilder.add(RDKitchenBlocks.MYSTIA_CUTTING_BOARD.asBlock(), "夜雀⦁料理台");
        translationBuilder.add(RDKitchenBlocks.MYSTIA_FRYING_PAN.asBlock(), "夜雀⦁油锅");
        translationBuilder.add(RDKitchenBlocks.MYSTIA_GRILL.asBlock(), "夜雀⦁烧烤架");
        translationBuilder.add(RDKitchenBlocks.MYSTIA_STEAMER.asBlock(), "夜雀⦁蒸锅");
        translationBuilder.add(RDKitchenBlocks.SUPER_COOKING_POT.asBlock(), "超⦁煮锅");
        translationBuilder.add(RDKitchenBlocks.SUPER_CUTTING_BOARD.asBlock(), "超⦁料理台");
        translationBuilder.add(RDKitchenBlocks.SUPER_FRYING_PAN.asBlock(), "超⦁油锅");
        translationBuilder.add(RDKitchenBlocks.SUPER_GRILL.asBlock(), "超⦁烧烤架");
        translationBuilder.add(RDKitchenBlocks.SUPER_STEAMER.asBlock(), "超⦁蒸锅");
        translationBuilder.add(RDKitchenBlocks.EXTREME_COOKING_POT.asBlock(), "极⦁煮锅");
        translationBuilder.add(RDKitchenBlocks.EXTREME_CUTTING_BOARD.asBlock(), "极⦁料理台");
        translationBuilder.add(RDKitchenBlocks.EXTREME_FRYING_PAN.asBlock(), "极⦁油锅");
        translationBuilder.add(RDKitchenBlocks.EXTREME_GRILL.asBlock(), "极⦁烧烤架");
        translationBuilder.add(RDKitchenBlocks.EXTREME_STEAMER.asBlock(), "极⦁蒸锅");
        translationBuilder.add(RDKitchenBlocks.NUKE_COOKING_POT.asBlock(), "核能⦁煮锅");
        translationBuilder.add(RDKitchenBlocks.NUKE_CUTTING_BOARD.asBlock(), "核能⦁料理台");
        translationBuilder.add(RDKitchenBlocks.NUKE_FRYING_PAN.asBlock(), "核能⦁油锅");
        translationBuilder.add(RDKitchenBlocks.NUKE_GRILL.asBlock(), "核能⦁烧烤架");
        translationBuilder.add(RDKitchenBlocks.NUKE_STEAMER.asBlock(), "核能⦁蒸锅");

        translationBuilder.add(RDBlocks.PLATE.asBlock(), "盘子");
        translationBuilder.add(RDBlocks.BLACK_SALT_BLOCK.asBlock(), "黑盐块");

        translationBuilder.add(RDItems.MYSTIA_ICON.asItem(), "东方夜雀食堂图标");

        translationBuilder.add(RDIngredientItems.BAMBOO_SHOOTS.asItem(), "竹笋");
        translationBuilder.add(RDIngredientItems.BLACK_SALT.asItem(), "黑盐");
        translationBuilder.add(RDIngredientItems.BLACK_PORK.asItem(), "黑毛猪肉");
        translationBuilder.add(RDIngredientItems.BROCCOLI.asItem(), "西兰花");
        translationBuilder.add(RDIngredientItems.VENISON.asItem(), "鹿肉");
        translationBuilder.add(RDIngredientItems.BUTTER.asItem(), "黄油");
        translationBuilder.add(RDIngredientItems.CAPSAICIN.asItem(), "辣椒素");
        translationBuilder.add(RDIngredientItems.CHEESE.asItem(), "芝士");
        translationBuilder.add(RDIngredientItems.CHESTNUT.asItem(), "板栗");
        translationBuilder.add(RDIngredientItems.CHILI.asItem(), "辣椒");
        translationBuilder.add(RDIngredientItems.CRAB.asItem(), "螃蟹");
        translationBuilder.add(RDIngredientItems.CREAM.asItem(), "奶油");
        translationBuilder.add(RDIngredientItems.CUCUMBER.asItem(), "黄瓜");
        translationBuilder.add(RDIngredientItems.DEW.asItem(), "露水");
        translationBuilder.add(RDIngredientItems.FLOUR.asItem(), "面粉");
        translationBuilder.add(RDIngredientItems.FLOWERS.asItem(), "鲜花");
        translationBuilder.add(RDIngredientItems.FICUS_MICROCARPA.asItem(), "薜茘");
        translationBuilder.add(RDIngredientItems.GINKGO.asItem(), "白果");
        translationBuilder.add(RDIngredientItems.GRAPE.asItem(), "葡萄");
        translationBuilder.add(RDIngredientItems.HAGFISH.asItem(), "八目鳗");
        translationBuilder.add(RDIngredientItems.LEMON.asItem(), "柠檬");
        translationBuilder.add(RDIngredientItems.LOTUS_NUTS.asItem(), "莲子");
        translationBuilder.add(RDIngredientItems.MOONFLOWER.asItem(), "月光草");
        translationBuilder.add(RDIngredientItems.OCTOPUS.asItem(), "章鱼");
        translationBuilder.add(RDIngredientItems.ONION.asItem(), "洋葱");
        translationBuilder.add(RDIngredientItems.PEACH.asItem(), "桃子");
        translationBuilder.add(RDIngredientItems.PINE_NUT.asItem(), "松子");
        translationBuilder.add(RDIngredientItems.PLUM.asItem(), "梅子");
        translationBuilder.add(RDIngredientItems.PUFF_YO_FRUIT.asItem(), "噗噗哟果");
        translationBuilder.add(RDIngredientItems.RED_BEANS.asItem(), "红豆");
        translationBuilder.add(RDIngredientItems.SALMON.asItem(), "三文鱼");
        translationBuilder.add(RDIngredientItems.SEA_URCHIN.asItem(), "海胆");
        translationBuilder.add(RDIngredientItems.SHRIMP.asItem(), "虾");
        translationBuilder.add(RDIngredientItems.CICADA_SHELL.asItem(), "蝉蜕");
        translationBuilder.add(RDIngredientItems.UDUMBARA.asItem(), "幻昙华");
        translationBuilder.add(RDIngredientItems.STICKY_RICE.asItem(), "糯米");
        translationBuilder.add(RDIngredientItems.SUPREME_TUNA.asItem(), "极上金枪鱼");
        translationBuilder.add(RDIngredientItems.SWEET_POTATO.asItem(), "地瓜");
        translationBuilder.add(RDIngredientItems.TOFU.asItem(), "豆腐");
        translationBuilder.add(RDIngredientItems.TOMATO.asItem(), "西红柿");
        translationBuilder.add(RDIngredientItems.TOON.asItem(), "香椿");
        translationBuilder.add(RDIngredientItems.TREMELLA.asItem(), "银耳");
        translationBuilder.add(RDIngredientItems.TROUT.asItem(), "鳟鱼");
        translationBuilder.add(RDIngredientItems.TRUFFLE.asItem(), "松露");
        translationBuilder.add(RDIngredientItems.TUNA.asItem(), "金枪鱼");
        translationBuilder.add(RDIngredientItems.TWIN_LOTUS.asItem(), "并蒂莲");
        translationBuilder.add(RDIngredientItems.WAGYU_BEEF.asItem(), "和牛");
        translationBuilder.add(RDIngredientItems.WHITE_RADISH.asItem(), "白萝卜");
        translationBuilder.add(RDIngredientItems.WILD_BOAR_MEAT.asItem(), "野猪肉");

        translationBuilder.add(RDCuisineItems.ALL_MEAT_FEAST.asItem(), "全肉盛宴");
        translationBuilder.add(RDCuisineItems.ARCTIC_SWEET_SHRIMP_AND_PEACH_SALAD.asItem(), "北极甜虾蜜桃色拉");
        translationBuilder.add(RDCuisineItems.ASSORTED_TEMPURA.asItem(), "什锦天妇罗");
        translationBuilder.add(RDCuisineItems.A_LITTLE_SWEET_POISON.asItem(), "小小的甜蜜「毒药」");
        translationBuilder.add(RDCuisineItems.BAKED_CRAB_WITH_CREAM.asItem(), "奶油焗蟹");
        translationBuilder.add(RDCuisineItems.BAKED_SWEET_POTATOES.asItem(), "烤地瓜");
        translationBuilder.add(RDCuisineItems.BAMBOO_SHOOTS_FRIED_MEAT.asItem(), "竹笋炒肉");
        translationBuilder.add(RDCuisineItems.BAMBOO_SHOOTS_STEWED_IN_STONE_POT.asItem(), "石锅竹笋炖肉");
        translationBuilder.add(RDCuisineItems.BAMBOO_STEAMED_EGG.asItem(), "竹筒蒸蛋");
        translationBuilder.add(RDCuisineItems.BAMBOO_TUBE_ROASTED_DRUNKEN_SHRIMP.asItem(), "竹筒烧醉虾");
        translationBuilder.add(RDCuisineItems.BAMBOO_TUBE_STEAMED_PORK.asItem(), "竹筒粉蒸肉");
        translationBuilder.add(RDCuisineItems.BEAR_PAW.asItem(), "赛熊掌");
        translationBuilder.add(RDCuisineItems.BEEF_HOT_POT.asItem(), "牛肉鸳鸯火锅");
        translationBuilder.add(RDCuisineItems.BEEF_RICE.asItem().asItem(), "牛肉盖浇饭");
        translationBuilder.add(RDCuisineItems.BEEF_WELLINGTON.asItem(), "惠灵顿牛排");
        translationBuilder.add(RDCuisineItems.BEETLE_STEAMED_CAKE.asItem(), "兜甲蒸糕");
        translationBuilder.add(RDCuisineItems.BURNT_PUDDING.asItem(), "燃尽布丁");
        translationBuilder.add(RDCuisineItems.BISCAY_BISCUITS.asItem(), "比斯开湾饼干");
        translationBuilder.add(RDCuisineItems.BOILED_FISH.asItem(), "水煮鱼");
        translationBuilder.add(RDCuisineItems.BRAISED_EEL.asItem(), "红烧鳗鱼");
        translationBuilder.add(RDCuisineItems.BRAISED_PORK_WITH_PEACH.asItem(), "蜜桃红烧肉");
        translationBuilder.add(RDCuisineItems.BUTTER_STEAK.asItem(), "黄油牛排");
        translationBuilder.add(RDCuisineItems.CANDIED_CHESTNUTS.asItem(), "蜜饯栗子");
        translationBuilder.add(RDCuisineItems.CANDIED_SWEET_POTATO.asItem(), "拔丝地瓜");
        translationBuilder.add(RDCuisineItems.CATS_PLAYING_IN_WATER.asItem(), "猫咪戏水");
        translationBuilder.add(RDCuisineItems.CAT_FOOD.asItem(), "猫饭");
        translationBuilder.add(RDCuisineItems.CAT_KULULI.asItem(), "猫咪可露丽");
        translationBuilder.add(RDCuisineItems.CAT_PIZZA.asItem(), "猫咪披萨");
        translationBuilder.add(RDCuisineItems.CHEESE_EGG.asItem(), "芝士蛋");
        translationBuilder.add(RDCuisineItems.COLD_DISH_CARVING.asItem(), "凉菜雕花");
        translationBuilder.add(RDCuisineItems.COLD_TOFU.asItem(), "冷豆腐");
        translationBuilder.add(RDCuisineItems.COLORFUL_JADE_FRIED_BUNS.asItem(), "华光玉煎包");
        translationBuilder.add(RDCuisineItems.COOKING_TOFU.asItem(), "煮豆腐");
        translationBuilder.add(RDCuisineItems.CREAM_STEW.asItem(), "奶油炖菜");
        translationBuilder.add(RDCuisineItems.CRISP_CYCLONE.asItem(), "脆旋风");
        translationBuilder.add(RDCuisineItems.DARK_CUISINE.asItem(), "黑暗物质");
        translationBuilder.add(RDCuisineItems.DEEP_FRIED_CICADA_SHELLS.asItem(), "香炸蝉蜕");
        translationBuilder.add(RDCuisineItems.DEPRESSED_CHEESE_STICKS.asItem(), "丧气芝士条");
        translationBuilder.add(RDCuisineItems.DEW_BOILED_EGGS.asItem(), "露水煮蛋");
        translationBuilder.add(RDCuisineItems.DORAYAKI.asItem(), "铜锣烧");
        translationBuilder.add(RDCuisineItems.DUMPLING.asItem(), "水饺");
        translationBuilder.add(RDCuisineItems.EEL_EGG_DONBURI.asItem(), "鳗鱼蛋盖饭");
        translationBuilder.add(RDCuisineItems.EGGS_BENEDICT.asItem(), "班尼迪克蛋");
        translationBuilder.add(RDCuisineItems.ENERGY_STRING.asItem(), "能量串");
        translationBuilder.add(RDCuisineItems.FAILING_SAKURA_SNOW.asItem(), "樱落雪");
        translationBuilder.add(RDCuisineItems.FANTASY_IS_ALL_THE_RAGE.asItem(), "幻想风靡");
        translationBuilder.add(RDCuisineItems.FISH_LEAPS_OVER_DRAGON_GATE.asItem(), "鱼跃龙门");
        translationBuilder.add(RDCuisineItems.FLOWERS_BIRDS_WIND_AND_MOON.asItem(), "花鸟风月");
        translationBuilder.add(RDCuisineItems.FLOWING_WATER_NOODLES.asItem(), "流水素面");
        translationBuilder.add(RDCuisineItems.FRIED_HAGFISH.asItem(), "炸八目鳗");
        translationBuilder.add(RDCuisineItems.FRIED_PORK_CUTLET.asItem(), "炸猪肉排");
        translationBuilder.add(RDCuisineItems.FRIED_PORK_SHREDS.asItem(), "炒肉丝");
        translationBuilder.add(RDCuisineItems.FRIED_SHRIMP_TEMPURA.asItem(), "炸虾天妇罗");
        translationBuilder.add(RDCuisineItems.FRIED_TOFU.asItem(), "油豆腐");
        translationBuilder.add(RDCuisineItems.FRIED_TOMATO_STRIPS.asItem(), "炸番茄条");
        translationBuilder.add(RDCuisineItems.FRIGHT_ADVENTURE.asItem(), "惊吓！大冒险");
        translationBuilder.add(RDCuisineItems.GAME_SOUP.asItem(), "野味加农");
        translationBuilder.add(RDCuisineItems.GENSOKYO_BUDDHA_JUMPS_OVER_THE_WALL.asItem(), "幻想佛跳墙");
        translationBuilder.add(RDCuisineItems.GENSOKYO_STAR_LOTUS_SHIP.asItem(), "幻想星莲船");
        translationBuilder.add(RDCuisineItems.GIANT_TAMAGOYAKI.asItem(), "巨人玉子烧");
        translationBuilder.add(RDCuisineItems.GINKGO_AND_RADISH_PORK_RIB_SOUP.asItem(), "白果萝卜排骨汤");
        translationBuilder.add(RDCuisineItems.GLOOMY_FRUIT_PIE.asItem(), "阴郁水果派");
        translationBuilder.add(RDCuisineItems.GLUTINOUS_RICE_BALLS.asItem(), "汤圆");
        translationBuilder.add(RDCuisineItems.GOLDEN_CRISPY_FISH_CAKE.asItem(), "黄金酥鱼饼");
        translationBuilder.add(RDCuisineItems.GRAND_BANQUET.asItem(), "大奢宴");
        translationBuilder.add(RDCuisineItems.GREEN_BAMBOO_WELCOMES_SPRING.asItem(), "翠竹迎春");
        translationBuilder.add(RDCuisineItems.GREEN_FAIRY_MUSHROOM.asItem(), "绿野仙菇");
        translationBuilder.add(RDCuisineItems.GRILLED_HAGFISH.asItem(), "烤八目鳗");
        translationBuilder.add(RDCuisineItems.GRILLED_PORK_RICE_BALLS.asItem(), "炙猪肉饭团");
        translationBuilder.add(RDCuisineItems.HEART_PORRIDGE_GRUEL.asItem(), "养心粥");
        translationBuilder.add(RDCuisineItems.HELL_THRILL_WARNING.asItem(), "地狱激辛警告！");
        translationBuilder.add(RDCuisineItems.HOLY_WHITE_LOTUS_SEED_CAKE.asItem(), "圣白莲子糕");
        translationBuilder.add(RDCuisineItems.HONEY_BBQ_PORK.asItem(), "蜜汁叉烧");
        translationBuilder.add(RDCuisineItems.HORAI_DAMA_NO_EDA.asItem(), "蓬莱玉枝");
        translationBuilder.add(RDCuisineItems.HOT_WAFFLES.asItem(), "热松饼");
        translationBuilder.add(RDCuisineItems.HULA_SOUP.asItem(), "胡辣汤");
        translationBuilder.add(RDCuisineItems.LION_HEAD.asItem(), "狮子头");
        translationBuilder.add(RDCuisineItems.LONGYIN_PEACH.asItem(), "龙吟桃子");
        translationBuilder.add(RDCuisineItems.LOOKING_UP_AT_THE_CEILING_FRUIT_PIE.asItem(), "仰望天花板派");
        translationBuilder.add(RDCuisineItems.LOTUS_FISH_RICE_BOWL.asItem(), "荷花鱼米盏");
        translationBuilder.add(RDCuisineItems.LUOHAN_VEGETARIAN.asItem(), "罗汉上素");
        translationBuilder.add(RDCuisineItems.MAD_HATTER_TEA_PARTY.asItem(), "疯帽子茶会");
        translationBuilder.add(RDCuisineItems.MAGMA.asItem(), "岩浆");
        translationBuilder.add(RDCuisineItems.MAOYU_LAVA_TOFU.asItem(), "毛玉熔岩豆腐");
        translationBuilder.add(RDCuisineItems.MAOYU_TRICOLOR_ICE_CREAM.asItem(), "毛玉三色冰激凌");
        translationBuilder.add(RDCuisineItems.MAPO_TOFU.asItem(), "麻婆豆腐");
        translationBuilder.add(RDCuisineItems.MILKY_MUSHROOM_SOUP.asItem(), "奶香蘑菇汤");
        translationBuilder.add(RDCuisineItems.MOCHI.asItem(), "麻薯");
        translationBuilder.add(RDCuisineItems.MOLECULAR_EGG.asItem(), "分子蛋");
        translationBuilder.add(RDCuisineItems.MOONLIGHT_DUMPLINGS.asItem(), "月光团子");
        translationBuilder.add(RDCuisineItems.MOONLIGHT_OVER_LOTUS_POND.asItem(), "荷塘月色");
        translationBuilder.add(RDCuisineItems.MOON_CAKE.asItem(), "月饼");
        translationBuilder.add(RDCuisineItems.MOON_LOVERS.asItem(), "月之恋人");
        translationBuilder.add(RDCuisineItems.MUSHROOM_GIRLS_DANCE_STEW.asItem(), "蘑女的舞踏烩");
        translationBuilder.add(RDCuisineItems.MUSHROOM_MEAT_SLICES.asItem(), "蘑菇肉片");
        translationBuilder.add(RDCuisineItems.NIGIRI_SUSHI.asItem(), "手握寿司");
        translationBuilder.add(RDCuisineItems.OEDO_BOAT_FESTIVAL.asItem(), "大江户船祭");
        translationBuilder.add(RDCuisineItems.OKONOMIYAKI.asItem(), "大阪烧");
        translationBuilder.add(RDCuisineItems.ONE_HIT_KILL.asItem(), "一击☆必杀");
        translationBuilder.add(RDCuisineItems.ORDINARY_SMALL_CAKE.asItem(), "普通小蛋糕");
        translationBuilder.add(RDCuisineItems.PAN_FRIED_MUSHROOM_MEAT_ROLL.asItem(), "香煎双菇肉卷");
        translationBuilder.add(RDCuisineItems.PAN_FRIED_SALMON.asItem(), "香煎三文鱼");
        translationBuilder.add(RDCuisineItems.PEACH_BLOSSOM_GLAZE_ROLL.asItem(), "桃花琉璃卷");
        translationBuilder.add(RDCuisineItems.PEACH_BLOSSOM_SOUP.asItem(), "桃花羹");
        translationBuilder.add(RDCuisineItems.PHOENIX.asItem(), "不死鸟");
        translationBuilder.add(RDCuisineItems.PICKLED_CUCUMBERS.asItem(), "腌黄瓜");
        translationBuilder.add(RDCuisineItems.PIG_DEER_BUTTERFLY.asItem(), "猪鹿蝶");
        translationBuilder.add(RDCuisineItems.PINE_NUT_CAKE.asItem(), "松子糕");
        translationBuilder.add(RDCuisineItems.PIRATE_BACON.asItem(), "海盗熏肉");
        translationBuilder.add(RDCuisineItems.PLUM_TEA_RICE.asItem(), "梅子茶泡饭");
        translationBuilder.add(RDCuisineItems.POETRY_AND_GINKGO.asItem(), "诗礼银杏");
        translationBuilder.add(RDCuisineItems.POISONOUS_GARDEN.asItem(), "毒瘴花园");
        translationBuilder.add(RDCuisineItems.PORK_AND_TROUT_SMOKED.asItem(), "猪肉鳟鱼熏");
        translationBuilder.add(RDCuisineItems.PORK_RICE.asItem(), "猪肉盖浇饭");
        translationBuilder.add(RDCuisineItems.POTATO_CROQUETTES.asItem(), "土豆可乐饼");
        translationBuilder.add(RDCuisineItems.PSEUDO_JIRITAMA.asItem(), "拟尻子玉");
        translationBuilder.add(RDCuisineItems.PUMPKIN_SHRIMP_CAKE.asItem(), "南瓜虾盅");
        translationBuilder.add(RDCuisineItems.RAPUNZEL.asItem(), "长发公主");
        translationBuilder.add(RDCuisineItems.REAL_SEAFOOD_MISO_SOUP.asItem(), "真·海鲜味噌汤");
        translationBuilder.add(RDCuisineItems.RED_BEAN_DAIFUKU.asItem(), "红豆大福");
        translationBuilder.add(RDCuisineItems.REFRESHING_PUDDING.asItem(), "提神布丁");
        translationBuilder.add(RDCuisineItems.REVERSING_THE_WORLD.asItem(), "逆转天地！");
        translationBuilder.add(RDCuisineItems.RICE_BALL.asItem(), "饭团");
        translationBuilder.add(RDCuisineItems.RISOTTO.asItem(), "意式烩饭");
        translationBuilder.add(RDCuisineItems.ROASTED_MUSHROOMS.asItem(), "烤蘑菇");
        translationBuilder.add(RDCuisineItems.SAKURA_PUDDING.asItem(), "樱花布丁");
        translationBuilder.add(RDCuisineItems.SALMON_TEMPURA.asItem(), "三文鱼天妇罗");
        translationBuilder.add(RDCuisineItems.SASHIMI_PLATTER.asItem(), "刺身拼盘");
        translationBuilder.add(RDCuisineItems.SCARLET_DEVILS_CAKE.asItem(), "猩红恶魔蛋糕");
        translationBuilder.add(RDCuisineItems.SCONES.asItem(), "司康饼");
        translationBuilder.add(RDCuisineItems.SCREAMING_ODEN.asItem(), "绝叫关东煮");
        translationBuilder.add(RDCuisineItems.SEAFOOD_MISO_SOUP.asItem(), "海鲜味噌汤");
        translationBuilder.add(RDCuisineItems.SEA_URCHIN_SHINGEN_PANCAKE.asItem(), "海胆信玄饼");
        translationBuilder.add(RDCuisineItems.SEA_URCHIN_SASHIMI.asItem(), "海胆刺身");
        translationBuilder.add(RDCuisineItems.SECRET_DRIED_FISH.asItem(), "秘制小鱼干");
        translationBuilder.add(RDCuisineItems.SECRET_MUSHROOM_CASSEROLE.asItem(), "秘制鲜菌煲");
        translationBuilder.add(RDCuisineItems.SEVEN_COLORED_YOKAN.asItem(), "七色羊羹");
        translationBuilder.add(RDCuisineItems.SHIRAGA_SADAMATSU.asItem(), "白鹿贞松");
        translationBuilder.add(RDCuisineItems.SKINNY_HORSE_DUMPLING.asItem(), "瘦马团子");
        translationBuilder.add(RDCuisineItems.SNOW_WHITE.asItem(), "白雪");
        translationBuilder.add(RDCuisineItems.STEAMED_EGG_WITH_SEA_URCHIN.asItem(), "海胆蒸蛋");
        translationBuilder.add(RDCuisineItems.STINKY_TOFU.asItem(), "臭豆腐");
        translationBuilder.add(RDCuisineItems.STRENGTH_SOUP.asItem(), "力量汤");
        translationBuilder.add(RDCuisineItems.SUPERME_SEAFOOD_NOODLES.asItem(), "至尊海鲜面");
        translationBuilder.add(RDCuisineItems.TAICHI_BAGUA_FISH_MAW.asItem(), "太极八卦鱼肚");
        translationBuilder.add(RDCuisineItems.TAKETORIHIME.asItem(), "竹取姬");
        translationBuilder.add(RDCuisineItems.TAKOYAKI.asItem(), "章鱼烧");
        translationBuilder.add(RDCuisineItems.THE_BEAUTY_OF_HAN_PALACE.asItem(), "汉宫藏娇");
        translationBuilder.add(RDCuisineItems.THE_DREAM.asItem(), "幽梦");
        translationBuilder.add(RDCuisineItems.THE_MARS.asItem(), "火星");
        translationBuilder.add(RDCuisineItems.THE_SOURCE_OF_LIFE.asItem(), "生命之源");
        translationBuilder.add(RDCuisineItems.TIANSHI_BRAISED_CHESTNUT_MUSHROOMS.asItem(), "天师板栗焖菇");
        translationBuilder.add(RDCuisineItems.TOFU_MISO.asItem(), "豆腐味噌");
        translationBuilder.add(RDCuisineItems.TOFU_POT.asItem(), "豆腐锅");
        translationBuilder.add(RDCuisineItems.TONKOTSU_RAMEN.asItem(), "豚骨拉面");
        translationBuilder.add(RDCuisineItems.TOON_PANCAKES.asItem(), "香椿煎饼");
        translationBuilder.add(RDCuisineItems.TWO_HEAVENS_ONE_STYLE.asItem(), "二天一流");
        translationBuilder.add(RDCuisineItems.UDUMBARA_CAKE.asItem(), "幻昙花糕");
        translationBuilder.add(RDCuisineItems.UNCONSCIOUS_MONSTER_MOUSSE.asItem(), "无意识妖怪慕斯");
        translationBuilder.add(RDCuisineItems.VEGETABLE_SPECIAL.asItem(), "蔬菜专辑");
        translationBuilder.add(RDCuisineItems.WARM_RICE_BALL.asItem(), "温暖饭团");
        translationBuilder.add(RDCuisineItems.WHITE_PEACH_EIGHT_BRIDGE.asItem(), "白桃生八桥");
        translationBuilder.add(RDCuisineItems.YUNSHAN_COTTON_CANDY.asItem(), "云山棉花糖");
        translationBuilder.add(RDCuisineItems.ZHAJI.asItem(), "杂炊");

        translationBuilder.add(RDBeverageItems.GREEN_TEA.asItem(), "绿茶");
        translationBuilder.add(RDBeverageItems.FRUITY_HIGH_BALL.asItem(), "果味High Ball");
        translationBuilder.add(RDBeverageItems.FRUITY_SOUR.asItem(), "果味SOUR");
        translationBuilder.add(RDBeverageItems.QI.asItem(), "淇");
        translationBuilder.add(RDBeverageItems.BEER.asItem(), "超ZUN啤酒");
        translationBuilder.add(RDBeverageItems.SUN_MOON_STAR.asItem(), "日月星");
        translationBuilder.add(RDBeverageItems.PLUM_WINE.asItem(), "梅酒");
        translationBuilder.add(RDBeverageItems.TENGU_DANCE.asItem(), "天狗踊");
        translationBuilder.add(RDBeverageItems.SCARLET_DEVIL.asItem(), "猩红恶魔");
        translationBuilder.add(RDBeverageItems.GODS_WHEAT.asItem(), "神之麦");
        translationBuilder.add(RDBeverageItems.OTTER_FESTIVAL.asItem(), "水獭祭");
        translationBuilder.add(RDBeverageItems.DAWN.asItem(), "晓");
        translationBuilder.add(RDBeverageItems.SPARROW_SAKE.asItem(), "雀酒");
        translationBuilder.add(RDBeverageItems.SCARLET_DEVIL_MANSION_BLACK_TEA.asItem(), "红魔馆红茶");
        translationBuilder.add(RDBeverageItems.AFFGADO.asItem(), "阿芙加朵");
        translationBuilder.add(RDBeverageItems.RED_MIST.asItem(), "红雾");
        translationBuilder.add(RDBeverageItems.NEGRONI.asItem(), "尼格罗尼");
        translationBuilder.add(RDBeverageItems.GODFATHER.asItem(), "教父");
        translationBuilder.add(RDBeverageItems.BLESSING_WIND.asItem(), "风祝");
        translationBuilder.add(RDBeverageItems.WINTER_BREW.asItem(), "冬酿");
        translationBuilder.add(RDBeverageItems.FOURTEENTH_NIGHT.asItem(), "十四夜");
        translationBuilder.add(RDBeverageItems.FIRE_RAT_FUR.asItem(), "火鼠裘");
        translationBuilder.add(RDBeverageItems.GYOKURO_TEA.asItem(), "玉露茶");
        translationBuilder.add(RDBeverageItems.MOON_ROCKET.asItem(), "月面火箭");
        translationBuilder.add(RDBeverageItems.MILK.asItem(), "牛奶");
        translationBuilder.add(RDBeverageItems.RED_GRAPEFRUIT_JUICE.asItem(), "红柚果汁");
        translationBuilder.add(RDBeverageItems.SODA.asItem(), "波子汽水");
        translationBuilder.add(RDBeverageItems.ICEBERG_MAPLE_FROZEN_LEMON.asItem(), "冰山毛玉冻柠");
        translationBuilder.add(RDBeverageItems.BIG_POPSICLE.asItem(), "“大冰棍儿！”");
        translationBuilder.add(RDBeverageItems.DAIGINJO.asItem(), "大吟酿");
        translationBuilder.add(RDBeverageItems.COFFEE.asItem(), "咖啡");
        translationBuilder.add(RDBeverageItems.FAIRY_RAIN.asItem(), "妖精雨露");
        translationBuilder.add(RDBeverageItems.PALEO_CREAMY_SMOOTHIE.asItem(), "古法奶油冰沙");
        translationBuilder.add(RDBeverageItems.ORDINARY_FITNESS_TEA.asItem(), "普通健身茶");
        translationBuilder.add(RDBeverageItems.DEMON_SLAYER.asItem(), "鬼杀");
        translationBuilder.add(RDBeverageItems.QI_HEALTH.asItem(), "气保健");
        translationBuilder.add(RDBeverageItems.KOMEIJI_ICE_CREAM.asItem(), "古明地冰激凌");
        translationBuilder.add(RDBeverageItems.MANGO_POMELO_SAGO.asItem(), "杨枝甘露");
        translationBuilder.add(RDBeverageItems.QILIN.asItem(), "麒麟");
        translationBuilder.add(RDBeverageItems.HEAVEN_AND_EARTH_ARE_USELESS.asItem(), "天地无用");
        translationBuilder.add(RDBeverageItems.DRUNK_ACTOR.asItem(), "伶人醉");
        translationBuilder.add(RDBeverageItems.DAUGHTER_OF_THE_SEA.asItem(), "海的女儿");
        translationBuilder.add(RDBeverageItems.DEMONIC_COFFEE.asItem(), "魔界咖啡");
        translationBuilder.add(RDBeverageItems.MOJITO_BURST_BALL.asItem(), "莫吉托爆浆球");
        translationBuilder.add(RDBeverageItems.SPACE_BEER.asItem(), "太空啤酒");
        translationBuilder.add(RDBeverageItems.SATELLITE_ICED_COFFEE.asItem(), "卫星冰咖啡");

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

        translationBuilder.add(BeverageProperties.UNDEFINED.translateKey(), "未定义");
        translationBuilder.add(BeverageProperties.ALCOHOL_FREE.translateKey(), "无酒精");
        translationBuilder.add(BeverageProperties.LOW_ALCOHOL.translateKey(), "低酒精");
        translationBuilder.add(BeverageProperties.MID_ALCOHOL.translateKey(), "中酒精");
        translationBuilder.add(BeverageProperties.HIGH_ALCOHOL.translateKey(), "高酒精");
        translationBuilder.add(BeverageProperties.CAN_ADD_ICE.translateKey(), "可加冰");
        translationBuilder.add(BeverageProperties.CAN_HEATED.translateKey(), "可加热");
        translationBuilder.add(BeverageProperties.COCKTAIL.translateKey(), "鸡尾酒");
        translationBuilder.add(BeverageProperties.WESTERN_WINE.translateKey(), "西洋酒");
        translationBuilder.add(BeverageProperties.FRUIT.translateKey(), "水果味");
        translationBuilder.add(BeverageProperties.SWEET.translateKey(), "甘");
        translationBuilder.add(BeverageProperties.BITTER.translateKey(), "苦");
        translationBuilder.add(BeverageProperties.SOJU.translateKey(), "烧酒");
        translationBuilder.add(BeverageProperties.SAKE.translateKey(), "清酒");
        translationBuilder.add(BeverageProperties.PUNGENT.translateKey(), "辛");
        translationBuilder.add(BeverageProperties.BUBBLE.translateKey(), "气泡");
        translationBuilder.add(BeverageProperties.BEER.translateKey(), "啤酒");
        translationBuilder.add(BeverageProperties.DIRECT_DRINKING.translateKey(), "直饮");
        translationBuilder.add(BeverageProperties.LIQUEUR.translateKey(), "利口酒");
        translationBuilder.add(BeverageProperties.REFRESHING.translateKey(), "提神");
        translationBuilder.add(BeverageProperties.CLASSICAL.translateKey(), "古典");
        translationBuilder.add(BeverageProperties.MODERN.translateKey(), "现代");

        // 植物
        translationBuilder.add(RDWoodBlocks.UDUMBARA_FLOWER.asBlock(), "幻昙华花");
        translationBuilder.add(RDWoodBlocks.TREMELLA.asBlock(), "银耳丛");

        // 种子
        generateCropBlock(translationBuilder, RDCropBlocks.CHILL, "辣椒种子");
        generateCropBlock(translationBuilder, RDCropBlocks.CUCUMBER, "黄瓜种子");
        generateCropBlock(translationBuilder, RDCropBlocks.GRAPE, "葡萄种子");
        generateCropBlock(translationBuilder, RDCropBlocks.ONION, "洋葱种子");
        generateCropBlock(translationBuilder, RDCropBlocks.RED_BEANS, "红豆种子");
        generateCropBlock(translationBuilder, RDCropBlocks.TOMATO, "西红柿种子");
        generateCropBlock(translationBuilder, RDCropBlocks.TOON, "香椿种子");
        generateCropBlock(translationBuilder, RDCropBlocks.WHITE_RADISH, "白萝卜种子");
        generateCropBlock(translationBuilder, RDCropBlocks.SWEET_POTATO, "红薯种子");
        generateCropBlock(translationBuilder, RDCropBlocks.BROCCOLI, "西兰花种子");
        generateCropBlock(translationBuilder, RDCropBlocks.SOY_BEANS, "黄豆种子");
    }

    void generateCropBlock(TranslationBuilder builder, CropBlockBundle.Entry entry, String seedName) {
        builder.add(entry.getCropBlock().asBlock(), seedName);
        builder.add(entry.getSeed().asItem(), seedName);
    }

    public void generateEntityTranslations(HolderLookup.Provider wrapperLookup, TranslationBuilder translationBuilder) {
        TranslationWrapper builder = ITranslationWrapper.ofWrapper(wrapperLookup, translationBuilder);
        builder.add(RDEntityTypes.FUMO_SELLER_VILLAGER.value(), "Fumo贩卖商人", "Fumo贩卖商人刷怪蛋");
        builder.add(RDEntityTypes.KILLER_BEE.value(), "杀人蜂", "杀人蜂刷怪蛋");
        builder.add(RDEntityTypes.GHOST.value(), "幽灵", "幽灵刷怪蛋");
        builder.add(RDEntityTypes.MOON_RABBIT.value(), "月兔", "月兔刷怪蛋");
        builder.add(RDEntityTypes.YOUSEI.value(), "妖精", "妖精刷怪蛋");
        builder.add(RDEntityTypes.MAID_YOUSEI.value(), "女仆妖精", "女仆妖精刷怪蛋");
        builder.add(RDEntityTypes.SUNFLOWER_YOUSEI.value(), "向日葵妖精", "向日葵妖精刷怪蛋");
        builder.add(RDEntityTypes.GOBLIN.value(), "哥布林", "哥布林刷怪蛋");
        builder.add(RDEntityTypes.RABBIT_UNIT.value(), "月兔战士", "月兔战士刷怪蛋");
        builder.add(RDEntityTypes.WATER_ELEMENTAL.value(), "水元素", "水元素刷怪蛋");
        builder.add(RDEntityTypes.FIRE_ELEMENTAL.value(), "火元素", "火元素刷怪蛋");
        builder.add(RDEntityTypes.ICE_ELEMENTAL.value(), "冰元素", "冰元素刷怪蛋");
        builder.add(RDEntityTypes.MAGIC_BROOM.value(), "魔法扫帚", "魔法扫帚刷怪蛋");
        builder.add(RDEntityTypes.WHEEL_CHAIR.value(), "轮椅", "轮椅刷怪蛋");
        builder.add(RDEntityTypes.HAIRBALL.value(), "毛玉", "毛玉刷怪蛋");
        builder.add(RDEntityTypes.SCARECROW.value(), "稻草人", "稻草人刷怪蛋");
        builder.add(RDEntityTypes.UFO.value(), "UFO", "UFO刷怪蛋");
        builder.add(RDEntityTypes.MUSHROOM_MONSTER.value(), "蘑菇", "蘑菇刷怪蛋");
        builder.add(RDEntityTypes.WILD_PIG.value(), "野猪", "野猪刷怪蛋");
        builder.add(RDEntityTypes.TAVERN_VILLAGER.value(), "酒馆老板", "酒馆老板刷怪蛋");
        builder.add(RDEntityTypes.ONI.value(), "鬼", "鬼刷怪蛋");
        builder.add(RDEntityTypes.FISHING_BOBBER.value(), "鱼钩", "");

        var registry = wrapperLookup.lookupOrThrow(Registries.VILLAGER_PROFESSION);
        VillagerProfession hawkers = registry.getOrThrow(RDVillagerProfessions.HAWKERS_KEY).value();
        VillagerProfession priest = registry.getOrThrow(RDVillagerProfessions.PRIEST_KEY).value();
        VillagerProfession moneyShopClerk = registry.getOrThrow(RDVillagerProfessions.MONEY_SHOP_CLERK_KEY).value();
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
        TranslationWrapper builder = ITranslationWrapper.ofWrapper(wrapperLookup, translationBuilder);
        // 主角组
        builder.addRoleEntity(NPCRoleTypes.REIMU, "博丽灵梦", "刷怪蛋");
        builder.addRoleEntity(NPCRoleTypes.CYAN_REIMU, "青灵梦", "刷怪蛋");
        builder.addRoleEntity(NPCRoleTypes.MARISA, "雾雨魔理沙", "刷怪蛋");
        builder.addRoleEntity(NPCRoleTypes.RUMIA, "露米娅", "刷怪蛋");
        builder.addRoleEntity(NPCRoleTypes.CIRNO, "琪露诺", "刷怪蛋");
        builder.addRoleEntity(NPCRoleTypes.MEIRIN, "红美铃", "刷怪蛋");
        builder.addRoleEntity(NPCRoleTypes.PATCHOULI, "帕秋莉·诺蕾姬", "刷怪蛋");
        builder.addRoleEntity(NPCRoleTypes.SAKUYA, "十六夜咲夜", "刷怪蛋");
        builder.addRoleEntity(NPCRoleTypes.REMILIA, "蕾米莉亚·斯卡蕾特", "刷怪蛋");
        builder.addRoleEntity(NPCRoleTypes.FLANDRE, "芙兰朵露·斯卡蕾特", "刷怪蛋");

        // 妖妖梦
        builder.addRoleEntity(NPCRoleTypes.LETTY_WHITEROCK, "蕾蒂·霍瓦特洛克", "刷怪蛋");
        builder.addRoleEntity(NPCRoleTypes.CHEN, "八云橙", "刷怪蛋");
        builder.addRoleEntity(NPCRoleTypes.ALICE, "爱丽丝·玛格特罗依德", "刷怪蛋");
        builder.addRoleEntity(NPCRoleTypes.LILY_WHITE, "莉莉白", "刷怪蛋");
        builder.addRoleEntity(NPCRoleTypes.LUNASA_PRISMRIVER, "露娜萨·普莉兹姆利巴", "刷怪蛋");
        builder.addRoleEntity(NPCRoleTypes.MERLIN_PRISMRIVER, "梅露兰·普莉兹姆利巴", "刷怪蛋");
        builder.addRoleEntity(NPCRoleTypes.LYRICA_PRISMRIVER, "莉莉卡·普莉兹姆利巴", "刷怪蛋");
        builder.addRoleEntity(NPCRoleTypes.RAN, "八云蓝", "刷怪蛋");
        builder.addRoleEntity(NPCRoleTypes.YOUMU, "魂魄妖梦", "刷怪蛋");
        builder.addRoleEntity(NPCRoleTypes.YUYUKO, "西行寺幽幽子", "刷怪蛋");
        builder.addRoleEntity(NPCRoleTypes.YUKARI, "八云紫", "刷怪蛋");

        // 永夜抄
        builder.addRoleEntity(NPCRoleTypes.MYSTIA_LORELEI, "米斯蒂娅·萝蕾拉", "刷怪蛋");
        builder.addRoleEntity(NPCRoleTypes.WRIGGLE_NIGHTBUG, "莉格露·奈特巴格", "刷怪蛋");
        builder.addRoleEntity(NPCRoleTypes.KAMISHIRASAWA_KEINE, "上白泽慧音", "刷怪蛋");
        builder.addRoleEntity(NPCRoleTypes.REISEN, "铃仙·优昙华院·因幡", "刷怪蛋");
        builder.addRoleEntity(NPCRoleTypes.ERIN, "八意永琳", "刷怪蛋");
        builder.addRoleEntity(NPCRoleTypes.HOURAISAN_KAGUYA, "蓬莱山辉夜", "刷怪蛋");
        builder.addRoleEntity(NPCRoleTypes.HUZIWARA_NO_MOKOU, "藤原妹红", "刷怪蛋");

        // 花映塚
        builder.addRoleEntity(NPCRoleTypes.SHIKIEIKI_YAMAXANADU, "四季映姬·夜摩仙那度", "刷怪蛋");
        builder.addRoleEntity(NPCRoleTypes.KAZAMI_YUKA, "风见幽香", "刷怪蛋");

        // 风神录
        builder.addRoleEntity(NPCRoleTypes.KAGIYAMA_HINA, "键山雏", "刷怪蛋");
        builder.addRoleEntity(NPCRoleTypes.INUBASHIRI_MOMIZI, "犬走椛", "刷怪蛋");
        builder.addRoleEntity(NPCRoleTypes.KAWASIRO_NITORI, "河城荷取", "刷怪蛋");
        builder.addRoleEntity(NPCRoleTypes.AYA, "射命丸文", "刷怪蛋");
        builder.addRoleEntity(NPCRoleTypes.KOCHIYA_SANAE, "东风谷早苗", "刷怪蛋");
        builder.addRoleEntity(NPCRoleTypes.YASAKA_KANAKO, "八坂神奈子", "刷怪蛋");
        builder.addRoleEntity(NPCRoleTypes.MORIYA_SUWAKO, "洩矢诹访子", "刷怪蛋");

        // 地灵殿
        builder.addRoleEntity(NPCRoleTypes.KISUME, "琪斯美", "刷怪蛋");
        builder.addRoleEntity(NPCRoleTypes.KURODANI_YAMAME, "黑谷山女", "刷怪蛋");
        builder.addRoleEntity(NPCRoleTypes.MIZUHASHI_PARSEE, "水桥帕露西", "刷怪蛋");
        builder.addRoleEntity(NPCRoleTypes.HOSHIGUMA_YUGI, "星熊勇仪", "刷怪蛋");
        builder.addRoleEntity(NPCRoleTypes.KAENBYOU_RIN, "火焰猫燐", "刷怪蛋");
        builder.addRoleEntity(NPCRoleTypes.KOMEIJI_SATORI, "古明地觉", "刷怪蛋");
        builder.addRoleEntity(NPCRoleTypes.REIUJI_UTSUH, "灵乌路空", "刷怪蛋");
        builder.addRoleEntity(NPCRoleTypes.KOMEIJI_KOISHI, "古明地恋", "刷怪蛋");
        builder.addRoleEntity(NPCRoleTypes.WHITE_KOMEIJI_KOISHI, "白色古明地恋", "刷怪蛋");

        // 星莲船
        builder.addRoleEntity(NPCRoleTypes.NAZRIN, "纳兹琳", "刷怪蛋");
        builder.addRoleEntity(NPCRoleTypes.TATARA_KOGASA, "多多良小伞", "刷怪蛋");
        builder.addRoleEntity(NPCRoleTypes.HOUJUU_NUE, "封兽鵺", "刷怪蛋");

        // 神灵庙
        builder.addRoleEntity(NPCRoleTypes.KASODANI_KYOUKO, "幽谷响子", "刷怪蛋");
        builder.addRoleEntity(NPCRoleTypes.MIYAKO_YOSHIKA, "宫古芳香", "刷怪蛋");
        builder.addRoleEntity(NPCRoleTypes.KAKU_SEIGA, "霍青娥", "刷怪蛋");
        builder.addRoleEntity(NPCRoleTypes.SOGA_NO_TOZIKO, "苏我屠自古", "刷怪蛋");
        builder.addRoleEntity(NPCRoleTypes.MONONOBE_NO_FUTO, "物部布都", "刷怪蛋");
        builder.addRoleEntity(NPCRoleTypes.TOYOSATOMIMI_NO_MIKO, "丰聪耳神子", "刷怪蛋");
        builder.addRoleEntity(NPCRoleTypes.HUTATSUIWA_MAMIZOU, "二岩猯藏", "刷怪蛋");

        // 辉针城
        builder.addRoleEntity(NPCRoleTypes.WAKASAGIHIME, "若狭姬", "刷怪蛋");
        builder.addRoleEntity(NPCRoleTypes.SEKIBANKI, "赤蛮奇", "刷怪蛋");
        builder.addRoleEntity(NPCRoleTypes.IMAIZUMI_KAGEROU, "今泉影狼", "刷怪蛋");
        builder.addRoleEntity(NPCRoleTypes.KIJIN_SEIJIA, "鬼人正邪", "刷怪蛋");
        builder.addRoleEntity(NPCRoleTypes.SUKUNA_SHINMYOUMARU, "少名针妙丸", "刷怪蛋");
        builder.addRoleEntity(NPCRoleTypes.HORIKAWA_RAIKO, "堀川雷鼓", "刷怪蛋");

        // 绀珠传
        builder.addRoleEntity(NPCRoleTypes.SEIRAN, "清兰", "刷怪蛋");
        builder.addRoleEntity(NPCRoleTypes.RINGO, "铃瑚", "刷怪蛋");
        builder.addRoleEntity(NPCRoleTypes.DOREMY_SWEET, "哆来咪·苏伊特", "刷怪蛋");
        builder.addRoleEntity(NPCRoleTypes.KISIN_SAGUME, "稀神探女", "刷怪蛋");
        builder.addRoleEntity(NPCRoleTypes.CLOWNPIECE, "克劳恩皮丝", "刷怪蛋");
        builder.addRoleEntity(NPCRoleTypes.JUNKO, "纯狐", "刷怪蛋");
        builder.addRoleEntity(NPCRoleTypes.HECATIA_LAPISLAZULI, "赫卡提亚·拉碧斯拉祖利", "刷怪蛋");

        // 天空璋
        builder.addRoleEntity(NPCRoleTypes.ETERNITY_LARVA, "爱塔妮堤·拉尔瓦", "刷怪蛋");
        builder.addRoleEntity(NPCRoleTypes.SAKUTA_NEMUNO, "坂田合欢乃", "刷怪蛋");
        builder.addRoleEntity(NPCRoleTypes.KOMANO_AUNN, "高丽野阿吽", "刷怪蛋");
        builder.addRoleEntity(NPCRoleTypes.YATADERA_NARUMI, "矢田寺成美", "刷怪蛋");
        builder.addRoleEntity(NPCRoleTypes.NISHIDA_SATONO, "丁礼田舞", "刷怪蛋");
        builder.addRoleEntity(NPCRoleTypes.TEIREIDA_MAI, "尔子田里乃", "刷怪蛋");
        builder.addRoleEntity(NPCRoleTypes.MATARA_OKINA, "摩多罗隐岐奈", "刷怪蛋");

        // 鬼形兽
        builder.addRoleEntity(NPCRoleTypes.EBISU_EIKA, "戎璎花", "刷怪蛋");
        builder.addRoleEntity(NPCRoleTypes.USHIZAKI_URUMI, "牛崎润美", "刷怪蛋");
        builder.addRoleEntity(NPCRoleTypes.NIWATARI_KUTAKA, "庭渡久侘歌", "刷怪蛋");
        builder.addRoleEntity(NPCRoleTypes.KITCHO_YACHIE, "吉吊八千慧", "刷怪蛋");
        builder.addRoleEntity(NPCRoleTypes.JOUTOUGU_MAYUMI, "杖刀偶磨弓", "刷怪蛋");
        builder.addRoleEntity(NPCRoleTypes.HANIYASUSHIN_KEIKI, "埴安神袿姬", "刷怪蛋");
        builder.addRoleEntity(NPCRoleTypes.KUROKOMA_SAKI, "骊驹早鬼", "刷怪蛋");

        // 虹龙洞
        builder.addRoleEntity(NPCRoleTypes.GOUTOKUZI_MIKE, "豪德寺三花", "刷怪蛋");
        builder.addRoleEntity(NPCRoleTypes.YAMASHIRO_TAKANE, "山城高岭", "刷怪蛋");
        builder.addRoleEntity(NPCRoleTypes.KOMAKUSA_SANNYO, "驹草山如", "刷怪蛋");
        builder.addRoleEntity(NPCRoleTypes.TAMATSUKURI_MISUMARU, "玉造魅须丸", "刷怪蛋");
        builder.addRoleEntity(NPCRoleTypes.KUDAMAKI_TSUKASA, "菅牧典", "刷怪蛋");
        builder.addRoleEntity(NPCRoleTypes.IIZUNAMARU_MEGUMU, "饭纲丸龙", "刷怪蛋");
        builder.addRoleEntity(NPCRoleTypes.TENKYU_CHIMATA, "天弓千亦", "刷怪蛋");
        builder.addRoleEntity(NPCRoleTypes.HIMEMUSHI_MOMOYO, "姬虫百百世", "刷怪蛋");

        // 兽王园
        builder.addRoleEntity(NPCRoleTypes.SON_BITEN, "孙美天", "刷怪蛋");
        builder.addRoleEntity(NPCRoleTypes.MITSUGASHIRA_ENOKO, "三头慧乃子", "刷怪蛋");
        builder.addRoleEntity(NPCRoleTypes.TENKAJIN_CHIYARI, "天火人血枪", "刷怪蛋");
        builder.addRoleEntity(NPCRoleTypes.YOMOTSU_HISAMI, " 豫母都日狭美", "刷怪蛋");
        builder.addRoleEntity(NPCRoleTypes.NIPPAKU_ZANMU, "日白残无", "刷怪蛋");

        // 锦上京
        builder.addRoleEntity(NPCRoleTypes.CHIRIZUKA_UBAME, "尘塚姥芽", "刷怪蛋");
        builder.addRoleEntity(NPCRoleTypes.HOUJU_CHIMI, "封兽魑魅", "刷怪蛋");
        builder.addRoleEntity(NPCRoleTypes.MICHIGAMI_NAREKO, "道神驯子", "刷怪蛋");
        builder.addRoleEntity(NPCRoleTypes.YUIMAN_ASAMA, "维缦·浅间", "刷怪蛋");
        builder.addRoleEntity(NPCRoleTypes.WATATSUKI_TOYOHIME, "绵月丰姬", "刷怪蛋");
        builder.addRoleEntity(NPCRoleTypes.IWANAGA_ARIYA, "磐永阿梨夜", "刷怪蛋");
        builder.addRoleEntity(NPCRoleTypes.WATARI_NINA, "渡里贝子", "刷怪蛋");

        // 三月精
        builder.addRoleEntity(NPCRoleTypes.STAR, "斯塔·萨菲雅", "刷怪蛋");
        builder.addRoleEntity(NPCRoleTypes.LUNAR, "露娜·切露德", "刷怪蛋");
        builder.addRoleEntity(NPCRoleTypes.SUNNY, "桑尼·米尔克", "刷怪蛋");

        // 其他
        builder.addRoleEntity(NPCRoleTypes.USAMI_RENKO, "宇佐见莲子", "刷怪蛋");
        builder.addRoleEntity(NPCRoleTypes.MARIBEL_HEARN, "玛艾露贝莉·赫恩", "刷怪蛋");

        // 黄昏
        builder.addRoleEntity(NPCRoleTypes.SUIKA, "伊吹萃香", "刷怪蛋");
        builder.addRoleEntity(NPCRoleTypes.TENSHI, "比那名居天子", "刷怪蛋");
    }

    public void generateEffectTranslations(HolderLookup.Provider wrapperLookup, TranslationBuilder translationBuilder) {
        TranslationWrapper builder = ITranslationWrapper.ofWrapper(wrapperLookup, translationBuilder);
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
        translationBuilder.add(RDItems.BATTLE_STICK.asItem().asItem(), "战斗调试棒");
        translationBuilder.add(RDItems.OWNER_STICK.asItem().asItem(), "主权调试棒");

        // 图标
        translationBuilder.add(RDItems.ICON.asItem(), "梦隐的幻想乡");
        translationBuilder.add(RDItems.FUMO_ICON.asItem(), "毛绒玩偶图标");
        translationBuilder.add(RDItems.SPAWN_EGG.asItem(), "刷怪蛋");
        translationBuilder.add(RDItems.DANMAKU.asItem(), "弹幕");

        // 材料
        translationBuilder.add(RDItems.POINT.asItem(), "Point");
        translationBuilder.add(RDItems.POWER.asItem(), "Power");
        translationBuilder.add(RDItems.DANMAKU_CORE.asItem(), "弹幕核心");
        translationBuilder.add(RDItems.UPGRADED_HEALTH_FRAGMENT.asItem(), "残机碎片");
        translationBuilder.add(RDItems.BOMB_FRAGMENT.asItem(), "Bomb碎片");
        translationBuilder.add(RDItems.RED_ORB.asItem(), "红宝玉");
        translationBuilder.add(RDItems.BLUE_ORB.asItem(), "蓝宝玉");
        translationBuilder.add(RDItems.YELLOW_ORB.asItem(), "黄宝玉");
        translationBuilder.add(RDItems.GREEN_ORB.asItem(), "绿宝玉");
        translationBuilder.add(RDItems.PURPLE_ORB.asItem(), "紫宝玉");
        translationBuilder.add(RDItems.YIN_YANG_ORB.asItem(), "阴阳玉");
        translationBuilder.add(RDItems.SPEED_FEATHER.asItem(), "速度羽毛");
        translationBuilder.add(RDItems.DREAM_CRYSTAL_FRAGMENT.asItem(), "梦境水晶碎片");
        translationBuilder.add(RDItems.EMPTY_PHOTO.asItem(), "空照片");
        translationBuilder.add(RDItems.WATERPROOF_LEATHER.asItem(), "防水皮革");

        // 道具
        translationBuilder.add(RDItems.GUIDEBOOK.asItem(), "幻想乡指南");
        translationBuilder.add("item.reverie_dreams.guidebook.desc", "§a夢見る幻想郷§r使用指南！");
        translationBuilder.add(RDItems.UPGRADED_HEALTH.asItem(), "残机");
        translationBuilder.add(RDItems.BOMB.asItem(), "Bomb");
        translationBuilder.add(RDItems.HORAI_DAMA_NO_EDA.asItem(), "蓬莱玉枝");
        translationBuilder.add(RDItems.YUKA_FLOWER_UMBRELLA.asItem(), "花洋伞");
        translationBuilder.add(RDItems.CROSSING_CHISEL.asItem(), "穿墙之凿");
        translationBuilder.add(RDItems.GAP_BALL.asItem(), "隙间之球");
        translationBuilder.add(RDItems.BAGUA_FURNACE.asItem(), "八卦炉");
        translationBuilder.add(RDItems.TIME_STOP_CLOCK.asItem(), "时停钟");
        translationBuilder.add(RDItems.EARPHONE.asItem(), "神子耳机");
        translationBuilder.add(RDItems.KOISHI_HAT.asItem(), "恋恋钢盔");
        translationBuilder.add(RDItems.FUMO_LICENSE.asItem(), "Fumo销售许可");
        translationBuilder.add(RDItems.CURSED_DECOY_DOLl.asItem(), "被诅咒的诱饵人偶");
        translationBuilder.add(RDItems.VAISRAVANAS_PAGODA.asItem(), "毘沙门天的宝塔");
        translationBuilder.add(RDItems.DREAM_PILLOW.asItem(), "梦境枕头");
        translationBuilder.add(RDItems.TENGU_SHIELD.asItem(), "天狗手盾");
        translationBuilder.add(RDItems.TENGU_CAMERA.asItem(), "天狗相机");
        translationBuilder.add(RDItems.HIMEKAIDOU_HATATES_PHONE.asItem(), "姬海棠果的手机");
        translationBuilder.add(RDItems.BAD_APPLE.asItem(), "Bad Apple!!");
        translationBuilder.add(RDItems.SCARECROW.asItem(), "稻草人");
        translationBuilder.add(RDItems.EXORCISM_PAPER.asItem(), "驱魔符纸");
        translationBuilder.add(RDItems.COPPER_COIN.asItem(), "铜币");
        translationBuilder.add(RDItems.SILVER_COIN.asItem(), "银币");
        translationBuilder.add(RDItems.GOLD_COIN.asItem(), "金币");
        translationBuilder.add(RDItems.SPELLCARD.asItem(), "符卡");
        translationBuilder.add(RDItems.SATORI_EYE.asItem(), "觉之眼");
        translationBuilder.add(RDItems.FAST_RECIPE_BOOK.asItem(), "快速食谱");
        translationBuilder.add(RDItems.LOW_GRAVITY_BOOT.asItem(), "低重力靴子");
        translationBuilder.add(RDItems.CROWN_OF_THE_UNDERWORLD.asItem(), "冥界之冠");
        translationBuilder.add(RDItems.SUNFLOWER.asItem(), "自然之力");
        translationBuilder.add(RDItems.CUSTOM_SKIN_SELECTOR.asItem(), "自定义皮肤更换工具");
        translationBuilder.add(RDItems.SOUL_CARD.asItem(), "魂符");

        // 武器
        translationBuilder.add(RDItems.HAKUREI_CANE.asItem(), "博丽御币");
        translationBuilder.add(RDItems.WIND_BLESSING_CANE.asItem(), "祝风御币");
        translationBuilder.add(RDItems.MAGIC_BROOM.asItem(), "魔法扫帚");
        translationBuilder.add(RDItems.GUNGNIR.asItem(), "神枪冈格尼尔");
        translationBuilder.add(RDItems.LEVATIN.asItem(), "莱瓦汀");
        translationBuilder.add(RDItems.ROKANKEN.asItem(), "楼观剑");
        translationBuilder.add(RDItems.HAKUROKEN.asItem(), "白楼剑");
        translationBuilder.add(RDItems.PAPILIO_PATTERN_FAN.asItem(), "凤蝶纹扇");
        translationBuilder.add(RDItems.IBUKIHO.asItem(), "伊吹瓢");
        translationBuilder.add(RDItems.SWORD_OF_HISOU.asItem(), "非想之剑");
        translationBuilder.add(RDItems.MANPOZUCHI.asItem(), "万宝槌");
        translationBuilder.add(RDItems.NUE_TRIDENT.asItem(), "鵺之三叉戟");
        translationBuilder.add(RDItems.TRUMPET_GUN.asItem(), "喇叭枪");
        translationBuilder.add(RDItems.TREASURE_HUNTING_ROD.asItem(), "探宝棒");
        translationBuilder.add(RDItems.KNIFE.asItem(), "飞刀");
        translationBuilder.add(RDItems.MAPLE_LEAF_FAN.asItem(), "枫叶团扇");
        translationBuilder.add(RDItems.VIOLIN.asItem(), "小提琴");
        translationBuilder.add(RDItems.KEYBOARD.asItem(), "键盘");
        translationBuilder.add(RDItems.TRUMPET.asItem(), "小号");
        translationBuilder.add(RDItems.IRON_BAR.asItem(), "铁棒");
        translationBuilder.add(RDItems.DEATH_SCYTHE.asItem(), "死神镰刀");
        translationBuilder.add(RDItems.WEAPON_OF_THE_MOON.asItem(), "月之兵器");

        // 工具矿物类
        translationBuilder.add(RDItems.RAW_SILVER.asItem(), "生银矿");
        translationBuilder.add(RDItems.SILVER_INGOT.asItem(), "银锭");
        translationBuilder.add(RDItems.SILVER_NUGGET.asItem(), "银粒");
        translationBuilder.add(RDItems.SILVER_SWORD.asItem(), "银剑");
        translationBuilder.add(RDItems.SILVER_AXE.asItem(), "银斧");
        translationBuilder.add(RDItems.SILVER_PICKAXE.asItem(), "银镐");
        translationBuilder.add(RDItems.SILVER_SHOVEL.asItem(), "银锹");
        translationBuilder.add(RDItems.SILVER_HOE.asItem(), "银锄");
        translationBuilder.add(RDItems.SILVER_SPEAR.asItem(), "银矛");
        translationBuilder.add(RDItems.SILVER_HELMET.asItem(), "银头盔");
        translationBuilder.add(RDItems.SILVER_CHESTPLATE.asItem(), "银胸甲");
        translationBuilder.add(RDItems.SILVER_LEGGINGS.asItem(), "银护腿");
        translationBuilder.add(RDItems.SILVER_BOOTS.asItem(), "银靴子");
        translationBuilder.add(RDItems.ICE_SCALES.asItem(), "冰之鳞");
        translationBuilder.add(RDItems.MAGIC_ICE_SWORD.asItem(), "冰剑");
        translationBuilder.add(RDItems.MAGIC_ICE_AXE.asItem(), "冰斧");
        translationBuilder.add(RDItems.MAGIC_ICE_PICKAXE.asItem(), "冰稿");
        translationBuilder.add(RDItems.MAGIC_ICE_SHOVEL.asItem(), "冰锹");
        translationBuilder.add(RDItems.MAGIC_ICE_HOE.asItem(), "冰锄");
        translationBuilder.add(RDItems.MAGIC_ICE_SPEAR.asItem(), "冰矛");
        translationBuilder.add(RDItems.MAGIC_ICE_HELMET.asItem(), "冰头盔");
        translationBuilder.add(RDItems.MAGIC_ICE_CHESTPLATE.asItem(), "冰胸甲");
        translationBuilder.add(RDItems.MAGIC_ICE_LEGGINGS.asItem(), "冰护腿");
        translationBuilder.add(RDItems.MAGIC_ICE_BOOTS.asItem(), "冰靴子");
        translationBuilder.add(RDItems.MAID_HAIRBAND.asItem(), "女仆发卡");
        translationBuilder.add(RDItems.MAID_UPPER_SKIRT.asItem(), "女仆上衣");
        translationBuilder.add(RDItems.MAID_LOWER_SKIRT.asItem(), "女仆裙");
        translationBuilder.add(RDItems.MAID_SHOE.asItem(), "女仆鞋子");
        translationBuilder.add(RDItems.DANMAKU_SHAPE_CREATOR.asItem(), "弹幕创作模板");
        translationBuilder.add(RDItems.SPELL_CARD_TEMPLATE.asItem(), "符卡模板");
        translationBuilder.add(RDItems.DREAM_SWORD.asItem(), "梦境水晶剑");
        translationBuilder.add(RDItems.DREAM_AXE.asItem(), "梦境水晶斧");
        translationBuilder.add(RDItems.DREAM_PICKAXE.asItem(), "梦境水晶稿");
        translationBuilder.add(RDItems.DREAM_SHOVEL.asItem(), "梦境水晶锹");
        translationBuilder.add(RDItems.DREAM_HOE.asItem(), "梦境水晶锄");
        translationBuilder.add(RDItems.DREAM_SPEAR.asItem(), "梦境水晶矛");
        translationBuilder.add(RDItems.DREAM_HELMET.asItem(), "梦境水晶头盔");
        translationBuilder.add(RDItems.DREAM_CHESTPLATE.asItem(), "梦境水晶胸甲");
        translationBuilder.add(RDItems.DREAM_LEGGINGS.asItem(), "梦境水晶护腿");
        translationBuilder.add(RDItems.DREAM_BOOTS.asItem(), "梦境水晶靴子");
        translationBuilder.add(RDItems.WATER_PROOF_HAT.asItem(), "防水帽子");
        translationBuilder.add(RDItems.WATER_PROOF_CLOTHING.asItem(), "防水衣");
        translationBuilder.add(RDItems.WATER_PROOF_LEGGINGS.asItem(), "防水护腿");
        translationBuilder.add(RDItems.WATER_PROOF_BOOTS.asItem(), "防水靴子");

        // 角色卡
        translationBuilder.add(RDItems.ROLE_CARD.asItem(), "空白角色卡");
        translationBuilder.add(RDItems.ROLE_ARCHIVE.asItem(), "角色存档卡");

        // 其他


//        translationBuilder.add(ModItems.DEBUG_DANMAKU_ITEM, "调试弹幕");
//        translationBuilder.add(ModItems.DEBUG_SPELL_CARD_ITEM, "调试符卡");
//        translationBuilder.add(ModItems.DEBUG_SPELL_CARD_ITEM2, "调试符卡2");

        this.generateDanmakuItemTranslations(wrapperLookup, translationBuilder, true);
    }

    public void generateSoundTranslations(HolderLookup.Provider wrapperLookup, TranslationBuilder translationBuilder) {
        TranslationWrapper builder = ITranslationWrapper.ofWrapper(wrapperLookup, translationBuilder);

        for (var sound : RDSoundEvents.FUMO_SOUNDS) {
            builder.generateSoundEventSubtitle(sound.value(), "fumo");
        }
        builder.generateSoundEventSubtitle(RDSoundEvents.BIU.value(), "满身疮痍");
        builder.generateSoundEventSubtitle(RDSoundEvents.POINT.value(), "收点");
        builder.generateSoundEventSubtitle(RDSoundEvents.SPELL_CARD.value(), "符卡释放");
        builder.generateSoundEventSubtitle(RDSoundEvents.UP.value(), "升级");
        builder.generateSoundEventSubtitle(RDSoundEvents.FIRE.value(), "弹幕发射");
        builder.generateSoundEventSubtitle(RDSoundEvents.BAGUA.value(), "魔炮");
        builder.generateSoundEventSubtitle(RDSoundEvents.PHOTO.value(), "拍照");
        builder.generateSoundEventSubtitle(RDSoundEvents.TICK_WAVE.value(), "波");
        builder.generateSoundEventSubtitle(RDSoundEvents.GRAZE.value(), "擦弹");

        this.generateDiscTranslations(wrapperLookup, translationBuilder);
        this.generateDanmakuType(wrapperLookup, translationBuilder);
    }

    public void generateDanmakuType(HolderLookup.Provider wrapperLookup, TranslationBuilder translationBuilder) {
        TranslationWrapper builder = ITranslationWrapper.ofWrapper(wrapperLookup, translationBuilder);

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
        TranslationWrapper builder = ITranslationWrapper.ofWrapper(wrapperLookup, translationBuilder);

        builder.generateJukeBox(JukeboxSongInit.HR01_01.getJukeboxSongKey(), "蓬莱人形　～ Dolls in Pseudo Paradise. - 蓬莱伝説");
        builder.generateJukeBox(JukeboxSongInit.HR02_08.getJukeboxSongKey(), "莲台野夜行 - 过去的花 ～ Fairy of Flower");
        builder.generateJukeBox(JukeboxSongInit.HR03_01.getJukeboxSongKey(), "ZUN - 童祭　～ Innocent Treasures");
        builder.generateJukeBox(JukeboxSongInit.MELODIC_TASTE_NIGHTMARE_BEFORE_CROSSROADS.getJukeboxSongKey(), "Melodic-Taste-Nightmare-before-Crossroads");
        builder.generateJukeBox(JukeboxSongInit.YV_FLOWER_CLOCK_AND_DREAMS.getJukeboxSongKey(), "Yonder-Voice - 花時計と夢");
        builder.generateJukeBox(JukeboxSongInit.GLOWING_NEEDLES_LITTLE_PEOPLE.getJukeboxSongKey(), "Inchlings of the Shining Needle ~ Little Princess : 「Miracle Remix」");
        builder.generateJukeBox(JukeboxSongInit.COOKIE.getJukeboxSongKey(), "温馨的神社（迫真）");
        builder.generateJukeBox(JukeboxSongInit.BAD_APPLE.getJukeboxSongKey(), "Bad Apple");
    }

    public void generateBlockTranslations(HolderLookup.Provider wrapperLookup, TranslationBuilder translationBuilder) {
        translationBuilder.add(RDBlocks.DANMAKU_CRAFTING_TABLE.asBlock(), "弹幕工作台");
        translationBuilder.add(RDBlocks.GENSOKYO_ALTAR.asBlock(), "幻想乡祭坛");
        translationBuilder.add(RDBlocks.STRENGTH_TABLE.asBlock(), "强化台");
        translationBuilder.add(RDBlocks.MUSIC_BLOCK.asBlock(), "音乐盒");

        translationBuilder.add(RDBlocks.MAGIC_ICE_BLOCK.asBlock(), "魔法冰");
        translationBuilder.add(RDBlocks.POINT_BLOCK.asBlock(), "Point方块");
        translationBuilder.add(RDBlocks.POWER_BLOCK.asBlock(), "Power方块");
        translationBuilder.add(RDBlocks.MOON_IRON_ORE.asBlock(), "月面铁矿石");
        translationBuilder.add(RDBlocks.MOON_GOLD_ORE.asBlock(), "月面金矿石");
        translationBuilder.add(RDBlocks.MOON_DIAMOND_ORE.asBlock(), "月面钻石矿石");
        translationBuilder.add(RDBlocks.MOON_QUARTZ_ORE.asBlock(), "月面石英矿石");
        translationBuilder.add(RDBlocks.SILVER_ORE.asBlock(), "银矿石");
        translationBuilder.add(RDBlocks.DEEPSLATE_SILVER_ORE.asBlock(), "深层银矿石");
        translationBuilder.add(RDBlocks.SILVER_BLOCK.asBlock(), "银块");
        translationBuilder.add(RDBlocks.SILVER_CHEST_BLOCK.chestBlock().asBlock(), "银箱子");
        translationBuilder.add(RDBlocks.ORB_ORE.asBlock(), "宝玉矿石");
        translationBuilder.add(RDBlocks.DEEPSLATE_ORB_ORE.asBlock(), "深层宝玉矿石");
        translationBuilder.add(RDBlocks.RED_ORB_BLOCK.asBlock(), "红宝玉块");
        translationBuilder.add(RDBlocks.YELLOW_ORB_BLOCK.asBlock(), "黄宝玉块");
        translationBuilder.add(RDBlocks.BLUE_ORB_BLOCK.asBlock(), "蓝宝玉块");
        translationBuilder.add(RDBlocks.GREEN_ORB_BLOCK.asBlock(), "绿宝玉块");
        translationBuilder.add(RDBlocks.PURPLE_ORB_BLOCK.asBlock(), "紫宝玉块");

        translationBuilder.add(RDBlocks.MARISA_HAT_BLOCK.asBlock(), "魔理沙的帽子");
        translationBuilder.add(RDBlocks.CASH_BOX_BLOCK.asBlock(), "塞钱箱");
        translationBuilder.add(RDBlocks.ANTI_COLLISION_BARREL.asBlock(), "防撞桶");
        translationBuilder.add(RDBlocks.WHEEL_CHAIR.asBlock(), "轮椅");
        translationBuilder.add(RDBlocks.WOODEN_BOX.chestBlock().asBlock(), "木板箱");
        translationBuilder.add(RDBlocks.CHAIR.asBlock(), "椅子");
        translationBuilder.add(RDBlocks.TABLE.asBlock(), "桌子");

        translationBuilder.add(RDBlocks.DREAM_RED_BLOCK.asBlock(), "网格方块");
        translationBuilder.add(RDBlocks.DREAM_BLUE_BLOCK.asBlock(), "网格方块");
        translationBuilder.add(RDBlocks.DREAM_CRYSTAL_ORE.asBlock(), "梦境水晶矿");

        translationBuilder.add(RDWoodBlocks.SPIRITUAL_BUNDLE.log().asBlock(), "绳文杉原木");
        translationBuilder.add(RDWoodBlocks.SPIRITUAL_BUNDLE.wood().asBlock(), "绳文杉树皮");
        translationBuilder.add(RDWoodBlocks.SPIRITUAL_BUNDLE.stairs().asBlock(), "绳文杉楼梯");
        translationBuilder.add(RDWoodBlocks.SPIRITUAL_BUNDLE.slab().asBlock(), "绳文杉台阶");
        translationBuilder.add(RDWoodBlocks.SPIRITUAL_BUNDLE.door().asBlock(), "绳文杉门");
        translationBuilder.add(RDWoodBlocks.SPIRITUAL_BUNDLE.trapdoor().asBlock(), "绳文杉活版门");
        translationBuilder.add(RDWoodBlocks.SPIRITUAL_BUNDLE.fence().asBlock(), "绳文杉栅栏");
        translationBuilder.add(RDWoodBlocks.SPIRITUAL_BUNDLE.fenceGate().asBlock(), "绳文杉栅栏门");
        translationBuilder.add(RDWoodBlocks.SPIRITUAL_BUNDLE.button().asBlock(), "绳文杉按钮");
        translationBuilder.add(RDWoodBlocks.SPIRITUAL_BUNDLE.strippedLog().asBlock(), "去皮绳文杉");
        translationBuilder.add(RDWoodBlocks.SPIRITUAL_BUNDLE.strippedWood().asBlock(), "去皮绳文杉树皮");
        translationBuilder.add(RDWoodBlocks.BLESSED_SPIRITUAL_LOG.asBlock(), "被祝福的绳文杉柱");
        translationBuilder.add(RDWoodBlocks.SPIRITUAL_BUNDLE.leaves().asBlock(), "绳文杉树叶");
        translationBuilder.add(RDWoodBlocks.SPIRITUAL_BUNDLE.sapling().asBlock(), "绳文杉树苗");
        translationBuilder.add(RDWoodBlocks.SPIRITUAL_BUNDLE.planks().asBlock(), "绳文杉木板");

        translationBuilder.add(RDBlocks.ICE_SCALES.block().asBlock(), "冰鳞砖块");
        translationBuilder.add(RDBlocks.ICE_SCALES.stair().asBlock(), "冰鳞砖楼梯");
        translationBuilder.add(RDBlocks.ICE_SCALES.slab().asBlock(), "冰鳞砖台阶");
        translationBuilder.add(RDBlocks.ICE_SCALES.wall().asBlock(), "冰鳞砖墙");

        translationBuilder.add(RDBlocks.DREAM_STONE.block().asBlock(), "梦境石");
        translationBuilder.add(RDBlocks.DREAM_STONE.stair().asBlock(), "梦境石楼梯");
        translationBuilder.add(RDBlocks.DREAM_STONE.slab().asBlock(), "梦境石台阶");
        translationBuilder.add(RDBlocks.DREAM_STONE.wall().asBlock(), "梦境石砖墙");
        translationBuilder.add(RDBlocks.DREAM_STONE_BRICK.block().asBlock(), "梦境砖块");
        translationBuilder.add(RDBlocks.DREAM_STONE_BRICK.stair().asBlock(), "梦境砖楼梯");
        translationBuilder.add(RDBlocks.DREAM_STONE_BRICK.slab().asBlock(), "梦境砖台阶");
        translationBuilder.add(RDBlocks.DREAM_STONE_BRICK.wall().asBlock(), "梦境砖墙");

        translationBuilder.add(RDBlocks.MOON_STONE.block().asBlock(), "月石");
        translationBuilder.add(RDBlocks.MOON_STONE.stair().asBlock(), "月石楼梯");
        translationBuilder.add(RDBlocks.MOON_STONE.slab().asBlock(), "月石台阶");
        translationBuilder.add(RDBlocks.MOON_STONE.wall().asBlock(), "月石墙");
        translationBuilder.add(RDBlocks.MOON_STONE_BRICK.block().asBlock(), "月石砖块");
        translationBuilder.add(RDBlocks.MOON_STONE_BRICK.stair().asBlock(), "月石砖楼梯");
        translationBuilder.add(RDBlocks.MOON_STONE_BRICK.slab().asBlock(), "月石砖台阶");
        translationBuilder.add(RDBlocks.MOON_STONE_BRICK.wall().asBlock(), "月石砖墙");

        translationBuilder.add(RDWoodBlocks.LEMON_BUNDLE.log().asBlock(), "柠檬原木");
        translationBuilder.add(RDWoodBlocks.LEMON_BUNDLE.wood().asBlock(), "柠檬树皮");
        translationBuilder.add(RDWoodBlocks.LEMON_BUNDLE.stairs().asBlock(), "柠檬楼梯");
        translationBuilder.add(RDWoodBlocks.LEMON_BUNDLE.slab().asBlock(), "柠檬台阶");
        translationBuilder.add(RDWoodBlocks.LEMON_BUNDLE.door().asBlock(), "柠檬门");
        translationBuilder.add(RDWoodBlocks.LEMON_BUNDLE.trapdoor().asBlock(), "柠檬活版门");
        translationBuilder.add(RDWoodBlocks.LEMON_BUNDLE.fence().asBlock(), "柠檬栅栏");
        translationBuilder.add(RDWoodBlocks.LEMON_BUNDLE.fenceGate().asBlock(), "柠檬栅栏门");
        translationBuilder.add(RDWoodBlocks.LEMON_BUNDLE.button().asBlock(), "柠檬按钮");
        translationBuilder.add(RDWoodBlocks.LEMON_BUNDLE.strippedLog().asBlock(), "去皮柠檬木");
        translationBuilder.add(RDWoodBlocks.LEMON_BUNDLE.strippedWood().asBlock(), "去皮柠檬树皮");
        translationBuilder.add(RDWoodBlocks.LEMON_BUNDLE.leaves().asBlock(), "柠檬树叶");
        translationBuilder.add(RDWoodBlocks.LEMON_BUNDLE.sapling().asBlock(), "柠檬树苗");
        translationBuilder.add(RDWoodBlocks.LEMON_BUNDLE.planks().asBlock(), "柠檬木板");
        translationBuilder.add(RDWoodBlocks.LEMON_FRUIT_LEAVES.asBlock(), "柠檬果树叶");

        translationBuilder.add(RDWoodBlocks.GINKGO_BUNDLE.log().asBlock(), "白果原木");
        translationBuilder.add(RDWoodBlocks.GINKGO_BUNDLE.wood().asBlock(), "白果树皮");
        translationBuilder.add(RDWoodBlocks.GINKGO_BUNDLE.stairs().asBlock(), "白果楼梯");
        translationBuilder.add(RDWoodBlocks.GINKGO_BUNDLE.slab().asBlock(), "白果台阶");
        translationBuilder.add(RDWoodBlocks.GINKGO_BUNDLE.door().asBlock(), "白果门");
        translationBuilder.add(RDWoodBlocks.GINKGO_BUNDLE.trapdoor().asBlock(), "白果活版门");
        translationBuilder.add(RDWoodBlocks.GINKGO_BUNDLE.fence().asBlock(), "白果栅栏");
        translationBuilder.add(RDWoodBlocks.GINKGO_BUNDLE.fenceGate().asBlock(), "白果栅栏门");
        translationBuilder.add(RDWoodBlocks.GINKGO_BUNDLE.button().asBlock(), "白果按钮");
        translationBuilder.add(RDWoodBlocks.GINKGO_BUNDLE.strippedLog().asBlock(), "去皮白果木");
        translationBuilder.add(RDWoodBlocks.GINKGO_BUNDLE.strippedWood().asBlock(), "去皮白果树皮");
        translationBuilder.add(RDWoodBlocks.GINKGO_BUNDLE.leaves().asBlock(), "白果树叶");
        translationBuilder.add(RDWoodBlocks.GINKGO_BUNDLE.sapling().asBlock(), "白果树苗");
        translationBuilder.add(RDWoodBlocks.GINKGO_BUNDLE.planks().asBlock(), "白果木板");
        translationBuilder.add(RDWoodBlocks.GINKGO_FRUIT_LEAVES.asBlock(), "白果果树叶");

        translationBuilder.add(RDWoodBlocks.PEACH_BUNDLE.log().asBlock(), "桃木原木");
        translationBuilder.add(RDWoodBlocks.PEACH_BUNDLE.wood().asBlock(), "桃木树皮");
        translationBuilder.add(RDWoodBlocks.PEACH_BUNDLE.stairs().asBlock(), "桃木楼梯");
        translationBuilder.add(RDWoodBlocks.PEACH_BUNDLE.slab().asBlock(), "桃木台阶");
        translationBuilder.add(RDWoodBlocks.PEACH_BUNDLE.door().asBlock(), "桃木门");
        translationBuilder.add(RDWoodBlocks.PEACH_BUNDLE.trapdoor().asBlock(), "桃木活版门");
        translationBuilder.add(RDWoodBlocks.PEACH_BUNDLE.fence().asBlock(), "桃木栅栏");
        translationBuilder.add(RDWoodBlocks.PEACH_BUNDLE.fenceGate().asBlock(), "桃木栅栏门");
        translationBuilder.add(RDWoodBlocks.PEACH_BUNDLE.button().asBlock(), "桃木按钮");
        translationBuilder.add(RDWoodBlocks.PEACH_BUNDLE.strippedLog().asBlock(), "去皮桃木");
        translationBuilder.add(RDWoodBlocks.PEACH_BUNDLE.strippedWood().asBlock(), "去皮桃木树皮");
        translationBuilder.add(RDWoodBlocks.PEACH_BUNDLE.leaves().asBlock(), "桃木树叶");
        translationBuilder.add(RDWoodBlocks.PEACH_BUNDLE.sapling().asBlock(), "桃木树苗");
        translationBuilder.add(RDWoodBlocks.PEACH_BUNDLE.planks().asBlock(), "桃木木板");
        translationBuilder.add(RDWoodBlocks.PEACH_FRUIT_LEAVES.asBlock(), "桃果树叶");

        translationBuilder.add(RDBlocks.RAIL_CONTROLLER_BLOCK.asBlock(), "控制铁轨");
        translationBuilder.add(RDBlocks.SIGNAL_RAIL_BLOCK.asBlock(), "信号铁轨");
        translationBuilder.add(RDBlocks.SIGNAL_DELAYER_BLOCK.asBlock(), "信号延迟器");
        translationBuilder.add(RDBlocks.REMOTE_CLIENT.asBlock(), "远程信号接收端");
        translationBuilder.add(RDBlocks.REMOTE_SERVER.asBlock(), "远程信号发送端");
        translationBuilder.add(RDBlocks.SPEAKER.asBlock(), "广播器");
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

        translationBuilder.add(DanmakuTypes.AMULET.getItemHolder().asItem(), "札弹");
        translationBuilder.add(DanmakuTypes.ARROWHEAD.getItemHolder().asItem(), "鳞弹");
        translationBuilder.add(DanmakuTypes.BALL.getItemHolder().asItem(), "小玉");
        translationBuilder.add(DanmakuTypes.BUBBLE.getItemHolder().asItem(), "大玉");
        translationBuilder.add(DanmakuTypes.BULLET.getItemHolder().asItem(), "铳弹");
        translationBuilder.add(DanmakuTypes.FIREBALL.getItemHolder().asItem(), "火光弹");
        translationBuilder.add(DanmakuTypes.FIREBALL_GLOWY.getItemHolder().asItem(), "水光弹");
        translationBuilder.add(DanmakuTypes.KUNAI.getItemHolder().asItem(), "链弹");
        translationBuilder.add(DanmakuTypes.RICE.getItemHolder().asItem(), "米弹");
        translationBuilder.add(DanmakuTypes.STAR.getItemHolder().asItem(), "星弹");
        translationBuilder.add(DanmakuTypes.NOTE.getItemHolder().asItem(), "音符");
        translationBuilder.add(DanmakuTypes.LASER.getItemHolder().asItem(), "激光");
        translationBuilder.add(DanmakuTypes.BIG_LASER.getItemHolder().asItem(), "大激光");

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
