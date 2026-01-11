package cc.thonly.reverie_dreams.entity.villager;

import cc.thonly.reverie_dreams.ReverieDreams;
import com.google.common.collect.ImmutableSet;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
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
    public static final ResourceKey<VillagerProfession> HAWKERS = of("hawkers");
    public static final ResourceKey<VillagerProfession> PRIEST = of("priest");
    public static final ResourceKey<VillagerProfession> MONEY_SHOP_CLERK = of("money_shop_clerk");

    public static void registers() {
        registerProfession(HAWKERS, RDPointOfInterestTypes.HAWKERS, null);
        registerProfession(PRIEST, RDPointOfInterestTypes.PRIEST, SoundEvents.EXPERIENCE_ORB_PICKUP);
        registerProfession(MONEY_SHOP_CLERK, RDPointOfInterestTypes.MONEY_SHOP_CLERK, SoundEvents.VILLAGER_WORK_LIBRARIAN);
        registerOffers();
    }

    private static void registerOffers() {
        Hawkers.registers();
        Priest.registers();
        MoneyShopClerk.registers();
        MystiaModOffers.registers();
    }

    protected static ResourceKey<VillagerProfession> of(String id) {
        return ResourceKey.create(Registries.VILLAGER_PROFESSION, ReverieDreams.id(id));
    }

    protected static VillagerProfession registerProfession(ResourceKey<VillagerProfession> key, ResourceKey<PoiType> heldWorkstation, @Nullable SoundEvent workSound) {
        return registerProfession(key, entry -> entry.is(heldWorkstation), entry -> entry.is(heldWorkstation), workSound);
    }

    protected static VillagerProfession registerProfession(ResourceKey<VillagerProfession> key, Predicate<Holder<PoiType>> heldWorkstation, Predicate<Holder<PoiType>> acquirableWorkstation, @Nullable SoundEvent workSound) {
        return registerProfession(BuiltInRegistries.VILLAGER_PROFESSION, key, heldWorkstation, acquirableWorkstation, ImmutableSet.of(), ImmutableSet.of(), workSound);
    }

    protected static VillagerProfession registerProfession(ResourceKey<VillagerProfession> key, ResourceKey<PoiType> heldWorkstation, ImmutableSet<Item> gatherableItems, ImmutableSet<Block> secondaryJobSites, @Nullable SoundEvent workSound) {
        return registerProfession(BuiltInRegistries.VILLAGER_PROFESSION, key, entry -> entry.is(heldWorkstation), entry -> entry.is(heldWorkstation), gatherableItems, secondaryJobSites, workSound);
    }

    protected static VillagerProfession registerProfession(Registry<VillagerProfession> registry, ResourceKey<VillagerProfession> key, Predicate<Holder<PoiType>> heldWorkstation, Predicate<Holder<PoiType>> acquirableWorkstation, ImmutableSet<Item> gatherableItems, ImmutableSet<Block> secondaryJobSites, @Nullable SoundEvent workSound) {
        MutableComponent translatable = Component.translatable("entity." + key.identifier().getNamespace() + ".villager." + key.identifier().getPath());
        VillagerProfession villagerProfession = Registry.register(registry, key, new VillagerProfession(translatable, heldWorkstation, acquirableWorkstation, gatherableItems, secondaryJobSites, workSound));
        return villagerProfession;
    }
}
