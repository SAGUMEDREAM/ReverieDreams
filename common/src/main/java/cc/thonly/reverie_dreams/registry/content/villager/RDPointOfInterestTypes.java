package cc.thonly.reverie_dreams.registry.content.villager;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.registry.MCBuiltInRegistries;
import cc.thonly.reverie_dreams.registry.content.block.RDBlocks;
import com.google.common.collect.ImmutableSet;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Util;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Locale;
import java.util.Set;
import java.util.function.Supplier;

public class RDPointOfInterestTypes extends PoiTypes {
    public static final ResourceKey<PoiType> HAWKERS_KEY = createKey("hawkers");
    public static final ResourceKey<PoiType> PRIEST_KEY = createKey("priest");
    public static final ResourceKey<PoiType> MONEY_SHOP_CLERK_KEY = createKey("money_shop_clerk");
    public static final RegistrySupplier<PoiType> HAWKERS = register( HAWKERS_KEY, () -> new Entry(getBlockStates(RDBlocks.WOODEN_BOX.chestBlock().asBlock()), 1, 2));
    public static final RegistrySupplier<PoiType> PRIEST = register( PRIEST_KEY, () -> new Entry(getBlockStates(RDBlocks.CASH_BOX_BLOCK.asBlock()), 1, 2));
    public static final RegistrySupplier<PoiType> MONEY_SHOP_CLERK = register( MONEY_SHOP_CLERK_KEY, () -> new Entry(getBlockStates(Blocks.ENDER_CHEST), 1, 2));

    public static void initialize() {
    }

    private static Set<BlockState> getBlockStates(Block block) {
        return ImmutableSet.copyOf(block.getStateDefinition().getPossibleStates());
    }

    private static ResourceKey<PoiType> createKey(String id) {
        return ResourceKey.create(Registries.POINT_OF_INTEREST_TYPE, ReverieDreams.id(id));
    }

    private static RegistrySupplier<PoiType> register(ResourceKey<PoiType> key, Supplier<Entry> entrySuppler) {
        RegistrySupplier<PoiType> registrySupplier = MCBuiltInRegistries.POI_TYPE.register(key.identifier().getPath(), () -> {
            Entry entry = entrySuppler.get();
            return new PoiType(entry.states, entry.ticketCount, entry.searchDistance);
        });
        ReverieDreams.COMMON_LATE_INIT.add(() -> registrySupplier.value().matchingStates().forEach(state -> {
            if (TYPE_BY_STATE.containsKey(state)) {
                return;
            }
            Holder<PoiType> registryEntry2 = TYPE_BY_STATE.put(state, registrySupplier);
            if (registryEntry2 != null) {
                throw Util.pauseInIde(new IllegalStateException(String.format(Locale.ROOT, "%s is defined in more than one PoI type", state)));
            }
        }));
        return registrySupplier;
    }

    public record Entry(Set<BlockState> states, int ticketCount, int searchDistance) {

    }
}
