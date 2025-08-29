package cc.thonly.reverie_dreams.entity.villager;

import cc.thonly.reverie_dreams.entity.ModEntities;
import cc.thonly.reverie_dreams.fumo.Fumo;
import cc.thonly.reverie_dreams.recipe.ItemStackWrapper;
import cc.thonly.reverie_dreams.registry.RegistryManager;
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
import net.minecraft.village.*;
import net.minecraft.world.World;

import java.util.*;


public class FumoSellerVillager extends AbstractSeller {

    public FumoSellerVillager(EntityType<? extends WanderingTraderEntity> entityType, World world) {
        super(entityType, world);
    }

    public FumoSellerVillager(World world) {
        super(ModEntities.FUMO_SELLER_VILLAGER, world);
    }

    public FumoSellerVillager(VillagerData prev, World world) {
        super(ModEntities.FUMO_SELLER_VILLAGER, world);
        this.prev = prev;
    }

    public FumoSellerVillager(VillagerEntity prevEntity, World world) {
        super(ModEntities.FUMO_SELLER_VILLAGER, world);
        this.prev = prevEntity.getVillagerData();
    }

    public List<TradeOffer> getVillagerOffers() {
        long seed = this.getVillagerSeed();
        Random random = new Random(seed);

        List<TradeOffer> offers = new ArrayList<>();
        List<Fumo> allFumos = new ArrayList<>(RegistryManager.FUMO.values());

        Collections.shuffle(allFumos, random);

        int count = 4 + random.nextInt(5) + this.level;
        List<Fumo> selectedFumos = allFumos.subList(0, Math.min(count, allFumos.size()));

        for (Fumo fumo : selectedFumos) {
            Item item = fumo.item();
            ItemStack sellItem = new ItemStack(item);
            ItemStackWrapper wrapper = ItemStackWrapper.of(sellItem);

            int emeraldAmount = 31 + random.nextInt(14);
            TradedItem first = new TradedItem(Items.EMERALD, emeraldAmount);
            TradedItem second = new TradedItem(Items.WHITE_WOOL, 32);

            TradeOffer offer = new TradeOffer(first, Optional.of(second), sellItem, Math.max(0, 2 - this.sellInfo.getSellArchive(seed, wrapper)), 1, 0.05f);
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
                villagerTypeRegistry.getOrThrow(VillagerType.PLAINS),
                villagerProfessionRegistry.getOrThrow(VillagerProfession.LIBRARIAN),
                2
        );
    }

    @Override
    public boolean canReset() {
        return true;
    }
}
