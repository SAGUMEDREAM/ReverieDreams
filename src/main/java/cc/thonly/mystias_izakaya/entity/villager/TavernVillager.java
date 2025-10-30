package cc.thonly.mystias_izakaya.entity.villager;

import cc.thonly.mystias_izakaya.entity.MIEntities;
import cc.thonly.mystias_izakaya.item.MIItems;
import cc.thonly.mystias_izakaya.item.base.DrinkItem;
import cc.thonly.reverie_dreams.entity.villager.AbstractSellerEntity;
import cc.thonly.reverie_dreams.item.ModItems;
import cc.thonly.reverie_dreams.recipe.ItemStackWrapper;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerData;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerType;
import net.minecraft.world.entity.npc.WanderingTrader;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.Level;
import java.util.*;

public class TavernVillager extends AbstractSellerEntity {

    public TavernVillager(EntityType<? extends WanderingTrader> entityType, Level world) {
        super(entityType, world);
    }

    public TavernVillager(Level world) {
        super(MIEntities.TAVERN_VILLAGER, world);
    }

    public TavernVillager(VillagerData prev, Level world) {
        super(MIEntities.TAVERN_VILLAGER, world);
        this.prev = prev;
    }

    public TavernVillager(Villager prevEntity, Level world) {
        super(MIEntities.TAVERN_VILLAGER, world);
        this.prev = prevEntity.getVillagerData();
    }

    private static final List<Tuple<Item, Integer>> ALWAYS_ITEMS = new ArrayList<>(List.of(
            new Tuple<>(MIItems.GREEN_TEA, 8),
            new Tuple<>(MIItems.FRUITY_HIGH_BALL, 8),
            new Tuple<>(MIItems.FRUITY_SOUR, 8),
            new Tuple<>(MIItems.QI, 8)
    ));

    @Override
    public List<MerchantOffer> getVillagerOffers() {
        long seed = this.getVillagerSeed();
        Random random = new Random(seed);

        List<MerchantOffer> offers = new ArrayList<>();

        for (Tuple<Item, Integer> pair : ALWAYS_ITEMS) {
            Item item = pair.getA();
            int amount = pair.getB();

            ItemStack sellItem = new ItemStack(item, 6);
            ItemStackWrapper wrapper = ItemStackWrapper.of(sellItem);

            ItemCost first = new ItemCost(ModItems.COPPER_COIN, amount);
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

        List<Item> allDrinks = new ArrayList<>(MIItems.DRINK_ITEMS);
        for (Tuple<Item, Integer> pair : ALWAYS_ITEMS) {
            allDrinks.remove(pair.getA());
        }

        Collections.shuffle(allDrinks, random);

        int count = 4 + random.nextInt(5) + this.level;
        List<Item> selectedDrinks = allDrinks.subList(0, Math.min(count, allDrinks.size()));

        for (Item item : selectedDrinks) {
            ItemStack sellItem = new ItemStack(item, 6);
            ItemStackWrapper wrapper = ItemStackWrapper.of(sellItem);

            int amount = DrinkItem.PRICE_CALCULATION_TABLE.getOrDefault(item,8) + random.nextInt(2);
            ItemCost first = new ItemCost(ModItems.COPPER_COIN, amount);
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
    public VillagerData getModifyVillagerData(MinecraftServer server) {
        RegistryAccess.Frozen registryManager = server.registryAccess();
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
