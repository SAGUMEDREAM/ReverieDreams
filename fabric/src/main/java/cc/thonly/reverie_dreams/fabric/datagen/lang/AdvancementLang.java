package cc.thonly.reverie_dreams.fabric.datagen.lang;

import cc.thonly.reverie_dreams.registry.content.advancements.RDBuiltInAdvancements;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider.TranslationBuilder;
import net.minecraft.advancements.Advancement;
import net.minecraft.resources.ResourceKey;

public interface AdvancementLang {

    static void add(TranslationBuilder builder) {
        addAdvancement(builder, RDBuiltInAdvancements.ROOT, "夢見る幻想郷", "§r获得§a夢見る幻想郷§r模组指南");
        addAdvancement(builder, RDBuiltInAdvancements.DANMAKU_TABLE, "弹幕工作台", "获得§c弹幕工作台");
        addAdvancement(builder, RDBuiltInAdvancements.DANMAKU_WARS, "弹幕大战争！", "§r在§c弹幕工作台§r中制作你的弹幕");
        addAdvancement(builder, RDBuiltInAdvancements.DANMAKU_UPGRADE, "火力升级", "通过强化台强化你的弹幕");
        addAdvancement(builder, RDBuiltInAdvancements.DARK_CUISINE, "搞砸了", "烹饪出黑暗料理");
        addAdvancement(builder, RDBuiltInAdvancements.ABANDONED_SHRINE, "第一次接触", "进入废弃神社");
        addAdvancement(builder, RDBuiltInAdvancements.ENTER_DREAM, "梦里什么都有", "进入第四槐安通道");
        addAdvancement(builder, RDBuiltInAdvancements.FUMOFUMO, "FUMO FUMO", "获得fumo");
        addAdvancement(builder, RDBuiltInAdvancements.SHINY_COINS, "闪亮的货币", "获得金属货币");
        addAdvancement(builder, RDBuiltInAdvancements.LAYLA_PRISMRIVER, "蕾拉·普莉兹姆利巴", "获得一件乐器");
        addAdvancement(builder, RDBuiltInAdvancements.WOOD_WITH_SPIRITUAL_POWER, "灵力的木头", "获得绳文杉木头");
        addAdvancement(builder, RDBuiltInAdvancements.GENSOKYO_ALTAR_CRAFTING, "祭坛合成", "§r使用§d幻想乡祭坛§r合成一些物品");
        addAdvancement(builder, RDBuiltInAdvancements.PSYCHOLOGIST, "心理医生", "对生物使用§d觉之眼§r诊断");
        addAdvancement(builder, RDBuiltInAdvancements.MAKE_FRIEND, "旅途的伙伴", "使用蛋糕邀请一位角色成为你的伙伴");
        addAdvancement(builder, RDBuiltInAdvancements.TAKING_PHOTO, "不良新闻记者", "使用照相机拍照");
        addAdvancement(builder, RDBuiltInAdvancements.I_WILL_TAKE_YOUR_SOUL, "我要偷走你的灵魂！", "获得死神镰刀");
        addAdvancement(builder, RDBuiltInAdvancements.BURST, "爆！", "使用§aBomb§r");
        addAdvancement(builder, RDBuiltInAdvancements.LEVEL_UP, "+UP", "使用§d残机§r");
        addAdvancement(builder, RDBuiltInAdvancements.REHABILITATION_EXPERT, "退治专家", "击败一只妖怪或妖精");
        addAdvancement(builder, RDBuiltInAdvancements.A_CELESTIAL_BEING_DESCENDED_TO_EARTH, "天神下凡", "尝试带有威力附魔的桃子");
        addAdvancement(builder, RDBuiltInAdvancements.TOUHOU_MYSTIA_IZAKAYA, "东方夜雀食堂", "获得任意厨具");
        addAdvancement(builder, RDBuiltInAdvancements.COOKING_BY_MYSELF, "第一次烹饪", "使用任意厨具烹饪食物");
        addAdvancement(builder, RDBuiltInAdvancements.COOKING_BY_MYSELF_AMOUNT_5_TAG, "烹饪专家", "用厨具制作出带有 5 个以上标签的料理");
        addAdvancement(builder, RDBuiltInAdvancements.DELICACY, "美味", "品尝一次由厨具烹饪出的料理");
        addAdvancement(builder, RDBuiltInAdvancements.FINE_WINE, "好酒", "品尝一杯酒水");
        addAdvancement(builder, RDBuiltInAdvancements.TOUHOU_PEOPLE_CAN_FLY, "东方人能飞", "骑上魔法扫帚");
        addAdvancement(builder, RDBuiltInAdvancements.WELCOME_TO_THE_MOON_TOUR, "欢迎来到月面旅行团", "进入月面");
        addAdvancement(builder, RDBuiltInAdvancements.SERVING_DISHES_BY_THROWING, "料理投掷手", "向实体投掷料理");
        addAdvancement(builder, RDBuiltInAdvancements.TREASURES_BENEATH_THE_MOON, "月球的宝藏", "发现月球的宝藏箱子");
        addAdvancement(builder, RDBuiltInAdvancements.YOUKAI_SECRET_BREW, "妖怪秘酿", "学会酿造酒水");
        addAdvancement(builder, RDBuiltInAdvancements.WAITER, "服务员", "给客人上菜");
        addAdvancement(builder, RDBuiltInAdvancements.ASKING_FOR_MONEY, "讨数!", "海哥讨走了你的￥1500!");
    }

    static void addAdvancement(TranslationBuilder builder, ResourceKey<Advancement> key, String title, String description) {
        builder.add(RDBuiltInAdvancements.getTitleKey(key), title);
        builder.add(RDBuiltInAdvancements.getDescriptionKey(key), description);
    }
}
