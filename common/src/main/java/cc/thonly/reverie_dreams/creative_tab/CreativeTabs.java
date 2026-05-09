package cc.thonly.reverie_dreams.creative_tab;

import cc.thonly.reverie_dreams.creative_tab.content.*;
import net.blay09.mods.balm.world.item.BalmCreativeModeTabRegistrar;

public class CreativeTabs {
    public static void initialize(BalmCreativeModeTabRegistrar registrar) {
        BaseCreativeTab.bootstrap(registrar);
        ItemBlockCreativeTab.bootstrap(registrar);
        DanmakuCreativeTab.bootstrap(registrar);
        TemplateCreativeTab.bootstrap(registrar);
        FumoCreativeTab.bootstrap(registrar);
        RoleCardCreativeTab.bootstrap(registrar);

        KitchenwareCreativeTab.bootstrap(registrar);
        IngredientCreativeTab.bootstrap(registrar);
        FoodCreativeTab.bootstrap(registrar);
        DrinkCreativeTab.bootstrap(registrar);
        SeedCreativeTab.bootstrap(registrar);

        SpawnEggCreativeTab.bootstrap(registrar);
        NPCSpawnEggCreativeTab.bootstrap(registrar);
    }
}
