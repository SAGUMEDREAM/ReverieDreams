package cc.thonly.reverie_dreams.registry.content.villager;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.registry.content.villager.offer.HawkerOffers;
import cc.thonly.reverie_dreams.registry.content.villager.offer.MoneyShopClerkOffers;
import cc.thonly.reverie_dreams.registry.content.villager.offer.MystiaModOffers;
import cc.thonly.reverie_dreams.registry.content.villager.offer.PriestOffers;
import com.google.common.collect.ImmutableSet;
import net.blay09.mods.balm.Balm;
import net.blay09.mods.balm.core.BalmRegistrar;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

public class RDVillagerProfessions {
    public static final ResourceKey<VillagerProfession> HAWKERS_KEY = of("hawkers");
    public static final ResourceKey<VillagerProfession> PRIEST_KEY = of("priest");
    public static final ResourceKey<VillagerProfession> MONEY_SHOP_CLERK_KEY = of("money_shop_clerk");
    public static Holder<VillagerProfession> HAWKERS;
    public static Holder<VillagerProfession> PRIEST;
    public static Holder<VillagerProfession> MONEY_SHOP_CLERK;

    private static void registerOffers() {
        Balm.getRuntime().villagerTrades(ReverieDreams.MOD_ID, tradeRegistrar -> {
            HawkerOffers.registers(tradeRegistrar);
            PriestOffers.registers(tradeRegistrar);
            MoneyShopClerkOffers.registers(tradeRegistrar);
            MystiaModOffers.registers(tradeRegistrar);
        });
    }

    public static void initialize(BalmRegistrar.Scoped<VillagerProfession> scoped) {
        HAWKERS = registerProfession(scoped, HAWKERS_KEY, RDPointOfInterestTypes.HAWKERS_KEY, null);
        PRIEST = registerProfession(scoped, PRIEST_KEY, RDPointOfInterestTypes.PRIEST_KEY, SoundEvents.EXPERIENCE_ORB_PICKUP);
        MONEY_SHOP_CLERK = registerProfession(scoped, MONEY_SHOP_CLERK_KEY, RDPointOfInterestTypes.MONEY_SHOP_CLERK_KEY, SoundEvents.VILLAGER_WORK_LIBRARIAN);
        registerOffers();
    }

    protected static ResourceKey<VillagerProfession> of(String id) {
        return ResourceKey.create(Registries.VILLAGER_PROFESSION, ReverieDreams.id(id));
    }

    protected static Holder<VillagerProfession> registerProfession(BalmRegistrar.Scoped<VillagerProfession> scoped, ResourceKey<VillagerProfession> key, ResourceKey<PoiType> heldWorkstation, @Nullable SoundEvent workSound) {
        return registerProfession(scoped, key, entry -> entry.is(heldWorkstation), entry -> entry.is(heldWorkstation), workSound);
    }

    protected static Holder<VillagerProfession> registerProfession(BalmRegistrar.Scoped<VillagerProfession> scoped, ResourceKey<VillagerProfession> key, Predicate<Holder<PoiType>> heldWorkstation, Predicate<Holder<PoiType>> acquirableWorkstation, @Nullable SoundEvent workSound) {
        return registerProfession(scoped, key, heldWorkstation, acquirableWorkstation, ImmutableSet.of(), ImmutableSet.of(), workSound);
    }

    protected static Holder<VillagerProfession> registerProfession(BalmRegistrar.Scoped<VillagerProfession> scoped, ResourceKey<VillagerProfession> key, ResourceKey<PoiType> heldWorkstation, ImmutableSet<Item> gatherableItems, ImmutableSet<Block> secondaryJobSites, @Nullable SoundEvent workSound) {
        return registerProfession(scoped, key, entry -> entry.is(heldWorkstation), entry -> entry.is(heldWorkstation), gatherableItems, secondaryJobSites, workSound);
    }

    protected static Holder<VillagerProfession> registerProfession(BalmRegistrar.Scoped<VillagerProfession> scoped, ResourceKey<VillagerProfession> key, Predicate<Holder<PoiType>> heldWorkstation, Predicate<Holder<PoiType>> acquirableWorkstation, ImmutableSet<Item> gatherableItems, ImmutableSet<Block> secondaryJobSites, @Nullable SoundEvent workSound) {
        MutableComponent translatable = Component.translatable("entity." + key.identifier().getNamespace() + ".villager." + key.identifier().getPath());
        return scoped.register(key.identifier().getPath(), id -> new VillagerProfession(translatable, heldWorkstation, acquirableWorkstation, gatherableItems, secondaryJobSites, workSound));
    }

}
