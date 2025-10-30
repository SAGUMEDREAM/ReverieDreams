package cc.thonly.reverie_dreams.entity.villager;

import cc.thonly.reverie_dreams.entity.ModEntities;
import cc.thonly.reverie_dreams.fumo.Fumo;
import cc.thonly.reverie_dreams.recipe.ItemStackWrapper;
import cc.thonly.reverie_dreams.registry.RegistryManager;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.village.*;
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


public class FumoSellerVillager extends AbstractSellerEntity {

    public FumoSellerVillager(EntityType<? extends WanderingTrader> entityType, Level world) {
        super(entityType, world);
    }

    public FumoSellerVillager(Level world) {
        super(ModEntities.FUMO_SELLER_VILLAGER, world);
    }

    public FumoSellerVillager(VillagerData prev, Level world) {
        super(ModEntities.FUMO_SELLER_VILLAGER, world);
        this.prev = prev;
    }

    public FumoSellerVillager(Villager prevEntity, Level world) {
        super(ModEntities.FUMO_SELLER_VILLAGER, world);
        this.prev = prevEntity.getVillagerData();
    }

    public List<MerchantOffer> getVillagerOffers() {
        long seed = this.getVillagerSeed();
        Random random = new Random(seed);

        List<MerchantOffer> offers = new ArrayList<>();
        List<Fumo> allFumos = new ArrayList<>(RegistryManager.FUMO.values());

        Collections.shuffle(allFumos, random);

        int count = 4 + random.nextInt(5) + this.level;
        List<Fumo> selectedFumos = allFumos.subList(0, Math.min(count, allFumos.size()));

        for (Fumo fumo : selectedFumos) {
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
    public VillagerData getModifyVillagerData(MinecraftServer server) {
        RegistryAccess.Frozen registryManager = server.registryAccess();
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
