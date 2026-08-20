package cc.thonly.reverie_dreams.entity.villager;

import cc.thonly.reverie_dreams.item.IngredientStack;
import cc.thonly.reverie_dreams.registry.content.BeverageProperties;
import cc.thonly.reverie_dreams.registry.content.entity.RDEntityTypes;
import cc.thonly.reverie_dreams.registry.content.item.RDBeverageItems;
import cc.thonly.reverie_dreams.registry.content.item.RDItems;
import cc.thonly.reverie_dreams.registry.delegate.ItemDelegate;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerData;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.entity.npc.villager.VillagerType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.Level;

import java.util.*;

public class TavernVillager extends AbstractSeller {
    public TavernVillager(VillagerData prev, Level world) {
        super(RDEntityTypes.TAVERN_VILLAGER.value(), world);
        this.prev = prev;
    }

    public TavernVillager(Villager prevEntity, Level world) {
        super(RDEntityTypes.TAVERN_VILLAGER.value(), world);
        this.prev = prevEntity.getVillagerData();
    }

    private static final List<Tuple<ItemDelegate, Integer>> ALWAYS_ITEMS = new ArrayList<>();

    public TavernVillager(EntityType<TavernVillager> type, Level level) {
        super(type, level);
    }

    public static List<Tuple<ItemDelegate, Integer>> getAlwaysItems() {
        if (ALWAYS_ITEMS.isEmpty()) {
            ALWAYS_ITEMS.addAll(List.of(
                    new Tuple<>(RDBeverageItems.GREEN_TEA, 8),
                    new Tuple<>(RDBeverageItems.FRUITY_HIGH_BALL, 8),
                    new Tuple<>(RDBeverageItems.FRUITY_SOUR, 8),
                    new Tuple<>(RDBeverageItems.QI, 8)
            ));
        }
        return ALWAYS_ITEMS;
    }

    @Override
    public List<MerchantOffer> getVillagerOffers() {
        long seed = this.getVillagerSeed();
        Random random = new Random(seed);

        List<MerchantOffer> offers = new ArrayList<>();

        for (Tuple<ItemDelegate, Integer> pair : getAlwaysItems()) {
            Item item = pair.getA().asItem();
            int amount = pair.getB();

            ItemStack sellItem = new ItemStack(item, 6);
            IngredientStack wrapper = IngredientStack.of(sellItem);

            ItemCost first = new ItemCost(RDItems.COPPER_COIN, amount);
            ItemCost second = new ItemCost(Items.GLASS_BOTTLE, 1);

            MerchantOffer offer = new MerchantOffer(
                    first,
                    Optional.of(second),
                    sellItem,
                    Math.max(0, 2 - this.sellInfo.getSellArchive(seed, wrapper)),
                    1,
                    0.05f
            );
            offers.add(offer);
        }

        List<Item> allDrinks = new ArrayList<>(RDBeverageItems.DRINK_ITEMS.stream().map(ItemDelegate::asItem).toList());
        for (Tuple<ItemDelegate, Integer> pair : getAlwaysItems()) {
            allDrinks.remove(pair.getA().asItem());
        }

        Collections.shuffle(allDrinks, random);

        int count = 4 + random.nextInt(5) + this.level;
        List<Item> selectedDrinks = allDrinks.subList(0, Math.min(count, allDrinks.size()));

        for (Item item : selectedDrinks) {
            ItemStack sellItem = new ItemStack(item, 6);
            IngredientStack wrapper = IngredientStack.of(sellItem);

            int amount = BeverageProperties.getPriceCalculationTable().getOrDefault(item, 8) + random.nextInt(2);
            ItemCost first = new ItemCost(RDItems.COPPER_COIN, amount);
            ItemCost second = new ItemCost(Items.GLASS_BOTTLE, 1);

            MerchantOffer offer = new MerchantOffer(
                    first,
                    Optional.of(second),
                    sellItem,
                    Math.max(0, 2 - this.sellInfo.getSellArchive(seed, wrapper)),
                    1,
                    0.05f
            );
            offers.add(offer);
        }

        return offers;
    }


    @Override
    public VillagerData getModifyVillagerData(RegistryAccess registryManager) {
        Registry<VillagerType> villagerTypeRegistry = registryManager.lookupOrThrow(Registries.VILLAGER_TYPE);
        Registry<VillagerProfession> villagerProfessionRegistry = registryManager.lookupOrThrow(Registries.VILLAGER_PROFESSION);

        return new VillagerData(
                villagerTypeRegistry.getOrThrow(VillagerType.JUNGLE),
                villagerProfessionRegistry.getOrThrow(VillagerProfession.CARTOGRAPHER),
                2
        );
    }

    @Override
    public boolean canReset() {
        return true;
    }
}
