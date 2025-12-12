package cc.thonly.reverie_dreams.util.advancements;

import cc.thonly.reverie_dreams.ReverieDreams;
import net.minecraft.resources.ResourceLocation;

public class SimpleTriggerKeys {
    public static final ResourceLocation EAT_PEACH = createKey("eat_peach");
    public static final ResourceLocation EAT_FOOD = createKey("eat_food");
    public static final ResourceLocation HAVING_DRINK = createKey("having_drink");
    public static final ResourceLocation GENSOKYO_ALTAR_CRAFTING = createKey("gensokyo_altar_crafting");
    public static final ResourceLocation KITCHEN_COOKING = createKey("kitchen_cooking");
    public static final ResourceLocation KITCHEN_COOKING_AMOUNT_OF_5_TAG = createKey("kitchen_cooking_amount_of_5_tag");
    public static final ResourceLocation KITCHEN_DARK_CUISINE = createKey("kitchen_dark_cuisine");
    public static final ResourceLocation DANMAKU_UPGRADE = createKey("danmaku_upgrade");
    public static final ResourceLocation MAKING_FRIEND = createKey("making_friend");
    public static final ResourceLocation USE_MUSICAL_INSTRUMENTS = createKey("using_musical_instruments");

    public static ResourceLocation createKey(String name) {
        return ReverieDreams.id(name);
    }
}
