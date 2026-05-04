package cc.thonly.reverie_dreams.entity.villager;

import cc.thonly.reverie_dreams.data.FumoType;
import cc.thonly.reverie_dreams.recipe.ItemStackWrapper;
import cc.thonly.reverie_dreams.registry.RegistryImpls;
import cc.thonly.reverie_dreams.registry.content.entity.RDEntityTypes;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.villager.*;
import net.minecraft.world.entity.npc.wanderingtrader.*;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.Level;

import java.util.*;


public class FumoSeller extends AbstractSeller {

    public FumoSeller(EntityType<? extends WanderingTrader> entityType, Level world) {
        super(entityType, world);
    }

    public FumoSeller(Level world) {
        super(RDEntityTypes.FUMO_SELLER_VILLAGER.asHolder().value(), world);
    }

    public FumoSeller(VillagerData prev, Level world) {
        super(RDEntityTypes.FUMO_SELLER_VILLAGER.asHolder().value(), world);
        this.prev = prev;
    }

    public FumoSeller(Villager prevEntity, Level world) {
        super(RDEntityTypes.FUMO_SELLER_VILLAGER.asHolder().value(), world);
        this.prev = prevEntity.getVillagerData();
    }

    public List<MerchantOffer> getVillagerOffers() {
        long seed = this.getVillagerSeed();
        Random random = new Random(seed);

        List<MerchantOffer> offers = new ArrayList<>();
        List<FumoType> allFumos = new ArrayList<>(RegistryImpls.FUMO.values());

        Collections.shuffle(allFumos, random);

        int count = 4 + random.nextInt(5) + this.level;
        List<FumoType> selectedFumos = allFumos.subList(0, Math.min(count, allFumos.size()));

        for (FumoType fumo : selectedFumos) {
            Item item = fumo.item();
            ItemStack sellItem = new ItemStack(item);
            ItemStackWrapper wrapper = ItemStackWrapper.of(sellItem);

            int emeraldAmount = 31 + random.nextInt(14);
            ItemCost first = new ItemCost(Items.EMERALD, emeraldAmount);
            ItemCost second = new ItemCost(Items.WHITE_WOOL, 32);

            MerchantOffer offer = new MerchantOffer(first, Optional.of(second), sellItem, Math.max(0, 2 - this.sellInfo.getSellArchive(seed, wrapper)), 1, 0.05f);
            offers.add(offer);
        }

        return offers;
    }

    @Override
    public VillagerData getModifyVillagerData(RegistryAccess registryManager) {
        Registry<VillagerType> villagerTypeRegistry = registryManager.lookupOrThrow(Registries.VILLAGER_TYPE);
        Registry<VillagerProfession> villagerProfessionRegistry = registryManager.lookupOrThrow(Registries.VILLAGER_PROFESSION);

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
