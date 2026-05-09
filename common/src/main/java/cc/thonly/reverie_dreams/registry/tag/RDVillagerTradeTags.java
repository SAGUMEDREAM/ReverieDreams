package cc.thonly.reverie_dreams.registry.tag;

import cc.thonly.reverie_dreams.ReverieDreams;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.VillagerTradeTags;
import net.minecraft.world.item.trading.VillagerTrade;

public class RDVillagerTradeTags {
    public static final TagKey<VillagerTrade> HAWKERS_LEVEL_1 = of("hawkers/level_1");
    public static final TagKey<VillagerTrade> HAWKERS_LEVEL_2 = of("hawkers/level_2");
    public static final TagKey<VillagerTrade> HAWKERS_LEVEL_3 = of("hawkers/level_3");
    public static final TagKey<VillagerTrade> HAWKERS_LEVEL_4 = of("hawkers/level_4");
    public static final TagKey<VillagerTrade> HAWKERS_LEVEL_5 = of("hawkers/level_5");
    public static final TagKey<VillagerTrade> PRIEST_LEVEL_1 = of("priest/level_1");
    public static final TagKey<VillagerTrade> PRIEST_LEVEL_2 = of("priest/level_2");
    public static final TagKey<VillagerTrade> PRIEST_LEVEL_3 = of("priest/level_3");
    public static final TagKey<VillagerTrade> PRIEST_LEVEL_4 = of("priest/level_4");
    public static final TagKey<VillagerTrade> PRIEST_LEVEL_5 = of("priest/level_5");
    public static final TagKey<VillagerTrade> MONEY_SHOP_CLERK_LEVEL_1 = of("money_shop_clerk/level_1");
    public static final TagKey<VillagerTrade> MONEY_SHOP_CLERK_LEVEL_2 = of("money_shop_clerk/level_2");
    public static final TagKey<VillagerTrade> MONEY_SHOP_CLERK_LEVEL_3 = of("money_shop_clerk/level_3");
    public static final TagKey<VillagerTrade> MONEY_SHOP_CLERK_LEVEL_4 = of("money_shop_clerk/level_4");
    public static final TagKey<VillagerTrade> MONEY_SHOP_CLERK_LEVEL_5 = of("money_shop_clerk/level_5");
    public static final TagKey<VillagerTrade> BUTCHER_LEVEL_1 = VillagerTradeTags.BUTCHER_LEVEL_1;
    public static final TagKey<VillagerTrade> BUTCHER_LEVEL_2 = VillagerTradeTags.BUTCHER_LEVEL_2;
    public static final TagKey<VillagerTrade> BUTCHER_LEVEL_3 = VillagerTradeTags.BUTCHER_LEVEL_3;
    public static final TagKey<VillagerTrade> BUTCHER_LEVEL_4 = VillagerTradeTags.BUTCHER_LEVEL_4;
    public static final TagKey<VillagerTrade> BUTCHER_LEVEL_5 = VillagerTradeTags.BUTCHER_LEVEL_5;

    private static TagKey<VillagerTrade> of(String id) {
        return TagKey.create(Registries.VILLAGER_TRADE, ReverieDreams.id(id));
    }
}
