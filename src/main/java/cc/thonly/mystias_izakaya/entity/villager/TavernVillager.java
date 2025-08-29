package cc.thonly.mystias_izakaya.entity.villager;

import cc.thonly.mystias_izakaya.entity.MIEntities;
import cc.thonly.mystias_izakaya.item.MIItems;
import cc.thonly.mystias_izakaya.item.base.DrinkItem;
import cc.thonly.reverie_dreams.entity.villager.AbstractSeller;
import cc.thonly.reverie_dreams.recipe.ItemStackWrapper;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.passive.WanderingTraderEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Pair;
import net.minecraft.village.*;
import net.minecraft.world.World;

import java.util.*;

public class TavernVillager extends AbstractSeller {

    public TavernVillager(EntityType<? extends WanderingTraderEntity> entityType, World world) {
        super(entityType, world);
    }

    public TavernVillager(World world) {
        super(MIEntities.TAVERN_VILLAGER, world);
    }

    public TavernVillager(VillagerData prev, World world) {
        super(MIEntities.TAVERN_VILLAGER, world);
        this.prev = prev;
    }

    public TavernVillager(VillagerEntity prevEntity, World world) {
        super(MIEntities.TAVERN_VILLAGER, world);
        this.prev = prevEntity.getVillagerData();
    }

    private static final List<Pair<Item, Integer>> ALWAYS_ITEMS = new ArrayList<>(List.of(
            new Pair<>(MIItems.GREEN_TEA, 8),
            new Pair<>(MIItems.FRUITY_HIGH_BALL, 8),
            new Pair<>(MIItems.FRUITY_SOUR, 8),
            new Pair<>(MIItems.QI, 8)
    ));

    @Override
    public List<TradeOffer> getVillagerOffers() {
        long seed = this.getVillagerSeed();
        Random random = new Random(seed);

        List<TradeOffer> offers = new ArrayList<>();

        for (Pair<Item, Integer> pair : ALWAYS_ITEMS) {
            Item item = pair.getLeft();
            int emeraldAmount = pair.getRight();

            ItemStack sellItem = new ItemStack(item, 6);
            ItemStackWrapper wrapper = ItemStackWrapper.of(sellItem);

            TradedItem first = new TradedItem(Items.EMERALD, emeraldAmount);
            TradedItem second = new TradedItem(Items.GLASS_BOTTLE, 1);

            TradeOffer offer = new TradeOffer(
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
        for (Pair<Item, Integer> pair : ALWAYS_ITEMS) {
            allDrinks.remove(pair.getLeft());
        }

        Collections.shuffle(allDrinks, random);

        int count = 4 + random.nextInt(5) + this.level;
        List<Item> selectedDrinks = allDrinks.subList(0, Math.min(count, allDrinks.size()));

        for (Item item : selectedDrinks) {
            ItemStack sellItem = new ItemStack(item, 6);
            ItemStackWrapper wrapper = ItemStackWrapper.of(sellItem);

            int emeraldAmount = DrinkItem.PRICE_CALCULATION_TABLE.getOrDefault(item,8) + random.nextInt(2);
            TradedItem first = new TradedItem(Items.EMERALD, emeraldAmount);
            TradedItem second = new TradedItem(Items.GLASS_BOTTLE, 1);

            TradeOffer offer = new TradeOffer(
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
        DynamicRegistryManager.Immutable registryManager = server.getRegistryManager();
        Registry<VillagerType> villagerTypeRegistry = registryManager.getOrThrow(RegistryKeys.VILLAGER_TYPE);
        Registry<VillagerProfession> villagerProfessionRegistry = registryManager.getOrThrow(RegistryKeys.VILLAGER_PROFESSION);

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
