package cc.thonly.reverie_dreams.registry.content.villager;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.registry.MCBuiltInRegistries;
import cc.thonly.reverie_dreams.registry.delegate.RegistryDelegate;
import cc.thonly.reverie_dreams.util.trading.TradeSet;
import com.google.common.collect.ImmutableSet;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
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

import java.util.Map;
import java.util.function.Predicate;

public class RDVillagerProfessions {
    public static final Map<RegistryDelegate<VillagerProfession>, Int2ObjectMap<ResourceKey<TradeSet>>> TRADE_INFOS = new Object2ObjectLinkedOpenHashMap<>(16);
    public static final ResourceKey<VillagerProfession> HAWKERS_KEY = of("hawkers");
    public static final ResourceKey<VillagerProfession> PRIEST_KEY = of("priest");
    public static final ResourceKey<VillagerProfession> MONEY_SHOP_CLERK_KEY = of("money_shop_clerk");
    public static final RegistryDelegate<VillagerProfession> HAWKERS = registerProfession(HAWKERS_KEY, RDPointOfInterestTypes.HAWKERS_KEY, null, Int2ObjectMap.ofEntries(
            Int2ObjectMap.entry(1, RDTradeSets.HAWKERS_LEVEL_1),
            Int2ObjectMap.entry(2, RDTradeSets.HAWKERS_LEVEL_2),
            Int2ObjectMap.entry(3, RDTradeSets.HAWKERS_LEVEL_3),
            Int2ObjectMap.entry(4, RDTradeSets.HAWKERS_LEVEL_4),
            Int2ObjectMap.entry(5, RDTradeSets.HAWKERS_LEVEL_5)
    ));
    public static final RegistryDelegate<VillagerProfession> PRIEST = registerProfession(PRIEST_KEY, RDPointOfInterestTypes.PRIEST_KEY, SoundEvents.EXPERIENCE_ORB_PICKUP, Int2ObjectMap.ofEntries(
            Int2ObjectMap.entry(1, RDTradeSets.PRIEST_LEVEL_1),
            Int2ObjectMap.entry(2, RDTradeSets.PRIEST_LEVEL_2)
    ));
    public static final RegistryDelegate<VillagerProfession> MONEY_SHOP_CLERK = registerProfession(MONEY_SHOP_CLERK_KEY, RDPointOfInterestTypes.MONEY_SHOP_CLERK_KEY, SoundEvents.VILLAGER_WORK_LIBRARIAN, Int2ObjectMap.ofEntries(
            Int2ObjectMap.entry(1, RDTradeSets.MONEY_SHOP_CLERK_LEVEL_1),
            Int2ObjectMap.entry(2, RDTradeSets.MONEY_SHOP_CLERK_LEVEL_2)
    ));

    public static void initialize() {
    }

    protected static ResourceKey<VillagerProfession> of(String id) {
        return ResourceKey.create(Registries.VILLAGER_PROFESSION, ReverieDreams.id(id));
    }

    protected static RegistryDelegate<VillagerProfession> registerProfession(ResourceKey<VillagerProfession> key, ResourceKey<PoiType> heldWorkstation, @Nullable SoundEvent workSound, Int2ObjectMap<ResourceKey<TradeSet>> tradeSet) {
        return registerProfession(key, entry -> entry.is(heldWorkstation), entry -> entry.is(heldWorkstation), workSound, tradeSet);
    }

    protected static RegistryDelegate<VillagerProfession> registerProfession(ResourceKey<VillagerProfession> key, Predicate<Holder<PoiType>> heldWorkstation, Predicate<Holder<PoiType>> acquirableWorkstation, @Nullable SoundEvent workSound, Int2ObjectMap<ResourceKey<TradeSet>> tradeSet) {
        return registerProfession(key, heldWorkstation, acquirableWorkstation, ImmutableSet.of(), ImmutableSet.of(), workSound, tradeSet);
    }

    protected static RegistryDelegate<VillagerProfession> registerProfession(ResourceKey<VillagerProfession> key, ResourceKey<PoiType> heldWorkstation, ImmutableSet<Item> gatherableItems, ImmutableSet<Block> secondaryJobSites, @Nullable SoundEvent workSound, Int2ObjectMap<ResourceKey<TradeSet>> tradeSet) {
        return registerProfession(key, entry -> entry.is(heldWorkstation), entry -> entry.is(heldWorkstation), gatherableItems, secondaryJobSites, workSound, tradeSet);
    }

    protected static RegistryDelegate<VillagerProfession> registerProfession(ResourceKey<VillagerProfession> key, Predicate<Holder<PoiType>> heldWorkstation, Predicate<Holder<PoiType>> acquirableWorkstation, ImmutableSet<Item> gatherableItems, ImmutableSet<Block> secondaryJobSites, @Nullable SoundEvent workSound, Int2ObjectMap<ResourceKey<TradeSet>> tradeSet) {
        MutableComponent translatable = Component.translatable("entity." + key.identifier().getNamespace() + ".villager." + key.identifier().getPath());
        RegistryDelegate<VillagerProfession> registryDelegate = MCBuiltInRegistries.VILLAGER_PROFESSION.register(key.identifier().getPath(), () -> new VillagerProfession(translatable,
                heldWorkstation,
                acquirableWorkstation,
                gatherableItems,
                secondaryJobSites,
                workSound)
        );
        TRADE_INFOS.put(registryDelegate, tradeSet);
        return registryDelegate;
    }

}
