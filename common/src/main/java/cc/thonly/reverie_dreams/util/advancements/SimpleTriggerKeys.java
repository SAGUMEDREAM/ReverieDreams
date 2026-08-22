package cc.thonly.reverie_dreams.util.advancements;

import cc.thonly.reverie_dreams.ReverieDreams;
import net.minecraft.resources.Identifier;

public class SimpleTriggerKeys {
    public static final Identifier EAT_PEACH = createKey("eat_peach");
    public static final Identifier EAT_FOOD = createKey("eat_food");
    public static final Identifier HAVING_DRINK = createKey("having_drink");
    public static final Identifier GENSOKYO_ALTAR_CRAFTING = createKey("gensokyo_altar_crafting");
    public static final Identifier KITCHEN_COOKING = createKey("kitchen_cooking");
    public static final Identifier KITCHEN_COOKING_AMOUNT_OF_5_TAG = createKey("kitchen_cooking_amount_of_5_tag");
    public static final Identifier KITCHEN_DARK_CUISINE = createKey("kitchen_dark_cuisine");
    public static final Identifier DANMAKU_UPGRADE = createKey("danmaku_upgrade");
    public static final Identifier MAKING_FRIEND = createKey("making_friend");
    public static final Identifier USE_MUSICAL_INSTRUMENTS = createKey("using_musical_instruments");
    public static final Identifier TOUHOU_PEOPLE_CAN_FLY = createKey("touhou_people_can_fly");
    public static final Identifier SERVING_DISHES_BY_THROWING = createKey("serving_dishes_by_throwing");
    public static final Identifier YOUKAI_SECRET_BREW = createKey("youkai_secret_brew");
    public static final Identifier WAITER = createKey("waiter");
    public static final Identifier OPEN_CHEST = createKey("open_chest");
    public static final Identifier ASKING_FOR_MONEY = createKey("asking_for_money");

    public static Identifier createKey(String name) {
        return ReverieDreams.id(name);
    }
}
