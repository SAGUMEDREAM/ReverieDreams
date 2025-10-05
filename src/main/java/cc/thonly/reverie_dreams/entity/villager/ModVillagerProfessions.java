package cc.thonly.reverie_dreams.entity.villager;

import cc.thonly.reverie_dreams.Touhou;
import com.google.common.collect.ImmutableSet;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.village.VillagerProfession;
import net.minecraft.world.poi.PointOfInterestType;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

public class ModVillagerProfessions {
    public static final RegistryKey<VillagerProfession> HAWKERS = of("hawkers");
    public static final RegistryKey<VillagerProfession> PRIEST = of("priest");
    public static final RegistryKey<VillagerProfession> MONEY_SHOP_CLERK = of("money_shop_clerk");

    public static void registers() {
        registerProfession(HAWKERS, ModPointOfInterestTypes.HAWKERS, null);
        registerProfession(PRIEST, ModPointOfInterestTypes.PRIEST, SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP);
        registerProfession(MONEY_SHOP_CLERK, ModPointOfInterestTypes.MONEY_SHOP_CLERK, SoundEvents.ENTITY_VILLAGER_WORK_LIBRARIAN);
        registerOffers();
    }

    private static void registerOffers() {
        Hawkers.registers();
        Priest.registers();
        MoneyShopClerk.registers();
    }

    protected static RegistryKey<VillagerProfession> of(String id) {
        return RegistryKey.of(RegistryKeys.VILLAGER_PROFESSION, Touhou.id(id));
    }

    protected static VillagerProfession registerProfession(RegistryKey<VillagerProfession> key, RegistryKey<PointOfInterestType> heldWorkstation, @Nullable SoundEvent workSound) {
        return registerProfession(key, entry -> entry.matchesKey(heldWorkstation), entry -> entry.matchesKey(heldWorkstation), workSound);
    }

    protected static VillagerProfession registerProfession(RegistryKey<VillagerProfession> key, Predicate<RegistryEntry<PointOfInterestType>> heldWorkstation, Predicate<RegistryEntry<PointOfInterestType>> acquirableWorkstation, @Nullable SoundEvent workSound) {
        return registerProfession(Registries.VILLAGER_PROFESSION, key, heldWorkstation, acquirableWorkstation, ImmutableSet.of(), ImmutableSet.of(), workSound);
    }

    protected static VillagerProfession registerProfession(RegistryKey<VillagerProfession> key, RegistryKey<PointOfInterestType> heldWorkstation, ImmutableSet<Item> gatherableItems, ImmutableSet<Block> secondaryJobSites, @Nullable SoundEvent workSound) {
        return registerProfession(Registries.VILLAGER_PROFESSION, key, entry -> entry.matchesKey(heldWorkstation), entry -> entry.matchesKey(heldWorkstation), gatherableItems, secondaryJobSites, workSound);
    }

    protected static VillagerProfession registerProfession(Registry<VillagerProfession> registry, RegistryKey<VillagerProfession> key, Predicate<RegistryEntry<PointOfInterestType>> heldWorkstation, Predicate<RegistryEntry<PointOfInterestType>> acquirableWorkstation, ImmutableSet<Item> gatherableItems, ImmutableSet<Block> secondaryJobSites, @Nullable SoundEvent workSound) {
        MutableText translatable = Text.translatable("entity." + key.getValue().getNamespace() + ".villager." + key.getValue().getPath());
        VillagerProfession villagerProfession = Registry.register(registry, key, new VillagerProfession(translatable, heldWorkstation, acquirableWorkstation, gatherableItems, secondaryJobSites, workSound));
        return villagerProfession;
    }
}
