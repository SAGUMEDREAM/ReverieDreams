package cc.thonly.reverie_dreams.creative_tab;

import cc.thonly.reverie_dreams.creative_tab.content.*;

public class RDCreativeTabs {
    public static void initialize() {
        BaseCreativeTab.bootstrap();
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
    }
}
