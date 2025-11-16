package cc.thonly.reverie_dreams.creative_tab;

import cc.thonly.reverie_dreams.creative_tab.content.*;

import java.util.ArrayList;
import java.util.List;

public class CreativeTabs {
    public static final List<Runnable> LATE_INIT = new ArrayList<>();

    public static void registerItemGroups() {
        registerContent(BaseCreativeTab::bootstrap);
        registerContent(ItemBlockCreativeTab::bootstrap);
        registerContent(DanmakuCreativeTab::bootstrap);
        registerContent(TemplateCreativeTab::bootstrap);
        registerContent(FumoCreativeTab::bootstrap);
        registerContent(RoleCardCreativeTab::bootstrap);

        registerContent(KitchenwareCreativeTab::bootstrap);
        registerContent(IngredientCreativeTab::bootstrap);
        registerContent(FoodCreativeTab::bootstrap);
        registerContent(DrinkCreativeTab::bootstrap);
        registerContent(SeedCreativeTab::bootstrap);

        registerContent(SpawnEggCreativeTab::bootstrap);
        registerContent(NPCSpawnEggCreativeTab::bootstrap);

        LATE_INIT.forEach(Runnable::run);
        LATE_INIT.clear();
    }

    public static void registerContent(Runnable runnable) {
        LATE_INIT.add(runnable);
    }
}
