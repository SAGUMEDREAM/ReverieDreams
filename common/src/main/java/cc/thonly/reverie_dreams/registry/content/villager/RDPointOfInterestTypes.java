package cc.thonly.reverie_dreams.registry.content.villager;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.registry.content.block.RDBlocks;
import com.google.common.collect.ImmutableSet;
import net.blay09.mods.balm.world.entity.ai.village.poi.BalmPoiTypeRegistrar;
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
    public static Holder<PoiType> HAWKERS;
    public static Holder<PoiType> PRIEST;
    public static Holder<PoiType> MONEY_SHOP_CLERK;

    public static void initialize(BalmPoiTypeRegistrar registrar) {
        HAWKERS = register(registrar, HAWKERS_KEY, () -> new Entry(getBlockStates(RDBlocks.WOODEN_BOX.chestBlock().asBlock()), 1, 2));
        PRIEST = register(registrar, PRIEST_KEY, () -> new Entry(getBlockStates(RDBlocks.CASH_BOX_BLOCK.asBlock()), 1, 2));
        MONEY_SHOP_CLERK = register(registrar, MONEY_SHOP_CLERK_KEY, () -> new Entry(getBlockStates(Blocks.ENDER_CHEST), 1, 2));
    }

    private static Set<BlockState> getBlockStates(Block block) {
        return ImmutableSet.copyOf(block.getStateDefinition().getPossibleStates());
    }

    private static ResourceKey<PoiType> createKey(String id) {
        return ResourceKey.create(Registries.POINT_OF_INTEREST_TYPE, ReverieDreams.id(id));
    }

    private static Holder<PoiType> register(BalmPoiTypeRegistrar registrar, ResourceKey<PoiType> key, Supplier<Entry> entrySuppler) {
        Holder<PoiType> holder = registrar.register(key.identifier().getPath(), () -> {
            Entry entry = entrySuppler.get();
            return new PoiType(entry.states, entry.ticketCount, entry.searchDistance);
        });
        ReverieDreams.COMMON_LATE_INIT.add(() -> holder.value().matchingStates().forEach(state -> {
            if (TYPE_BY_STATE.containsKey(state)) {
                return;
            }
            Holder<PoiType> registryEntry2 = TYPE_BY_STATE.put(state, holder);
            if (registryEntry2 != null) {
                throw Util.pauseInIde(new IllegalStateException(String.format(Locale.ROOT, "%s is defined in more than one PoI type", state)));
            }
        }));
        return holder;
    }

    public record Entry(Set<BlockState> states, int ticketCount, int searchDistance) {

    }
}
