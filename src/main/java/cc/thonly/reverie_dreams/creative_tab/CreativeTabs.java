package cc.thonly.reverie_dreams.creative_tab;

import cc.thonly.reverie_dreams.creative_tab.content.*;

import java.util.ArrayList;
import java.util.List;

public class CreativeTabs {
    public static final List<Runnable> LATE_INIT = new ArrayList<>();

    public static void registerItemGroups() {
        ItemBlockCreativeTab.bootstrap();
        DanmakuCreativeTab.bootstrap();
        TemplateCreativeTab.bootstrap();
        FumoCreativeTab.bootstrap();
        RoleCardCreativeTab.bootstrap();

        KitchenwareCreativeTab.bootstrap();
        IngredientCreativeTab.bootstrap();
        FoodCreativeTab.bootstrap();
        DrinkCreativeTab.bootstrap();
        SeedCreativeTab.bootstrap();

        SpawnEggCreativeTab.bootstrap();
        NPCSpawnEggCreativeTab.bootstrap();

        LATE_INIT.forEach(Runnable::run);
        LATE_INIT.clear();
    }

    public static void registerContent(Runnable runnable) {
        LATE_INIT.add(runnable);
    }
}
