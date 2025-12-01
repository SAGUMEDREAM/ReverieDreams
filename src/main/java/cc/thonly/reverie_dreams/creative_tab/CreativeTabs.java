package cc.thonly.reverie_dreams.creative_tab;

import cc.thonly.reverie_dreams.creative_tab.content.*;

import java.util.ArrayList;
import java.util.List;

public class CreativeTabs {
    public static final List<Runnable> LATE_INIT = new ArrayList<>();

    public static void registerItemGroups() {
        CreativeTabs.registerContent(BaseCreativeTab::bootstrap);
        CreativeTabs.registerContent(ItemBlockCreativeTab::bootstrap);
        CreativeTabs.registerContent(DanmakuCreativeTab::bootstrap);
        CreativeTabs.registerContent(TemplateCreativeTab::bootstrap);
        CreativeTabs.registerContent(FumoCreativeTab::bootstrap);
        CreativeTabs.registerContent(RoleCardCreativeTab::bootstrap);

        CreativeTabs.registerContent(KitchenwareCreativeTab::bootstrap);
        CreativeTabs.registerContent(IngredientCreativeTab::bootstrap);
        CreativeTabs.registerContent(FoodCreativeTab::bootstrap);
        CreativeTabs.registerContent(DrinkCreativeTab::bootstrap);
        CreativeTabs.registerContent(SeedCreativeTab::bootstrap);

        CreativeTabs.registerContent(SpawnEggCreativeTab::bootstrap);
        CreativeTabs.registerContent(NPCSpawnEggCreativeTab::bootstrap);

        LATE_INIT.forEach(Runnable::run);
        LATE_INIT.clear();
    }

    public static void registerContent(Runnable runnable) {
        LATE_INIT.add(runnable);
    }
}
