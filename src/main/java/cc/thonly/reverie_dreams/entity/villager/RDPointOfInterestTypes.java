package cc.thonly.reverie_dreams.entity.villager;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.registry.content.block.RDBlocks;
import com.google.common.collect.ImmutableSet;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Locale;
import java.util.Set;

public class RDPointOfInterestTypes extends PoiTypes {
    public static final ResourceKey<PoiType> HAWKERS = createKey("hawkers");
    public static final ResourceKey<PoiType> PRIEST = createKey("priest");
    public static final ResourceKey<PoiType> MONEY_SHOP_CLERK = createKey("money_shop_clerk");

    public static void registers() {
        register(HAWKERS, getBlockStates(RDBlocks.WOODEN_BOX.chestBlock()), 1, 2);
        register(PRIEST, getBlockStates(RDBlocks.CASH_BOX_BLOCK), 1, 2);
        register(MONEY_SHOP_CLERK, getBlockStates(Blocks.ENDER_CHEST), 1, 2);
    }

    private static Set<BlockState> getBlockStates(Block block) {
        return ImmutableSet.copyOf(block.getStateDefinition().getPossibleStates());
    }

    private static ResourceKey<PoiType> createKey(String id) {
        return ResourceKey.create(Registries.POINT_OF_INTEREST_TYPE, ReverieDreams.id(id));
    }

    private static PoiType register(ResourceKey<PoiType> key, Set<BlockState> states, int ticketCount, int searchDistance) {
        Registry<PoiType> registry = BuiltInRegistries.POINT_OF_INTEREST_TYPE;
        PoiType pointOfInterestType = new PoiType(states, ticketCount, searchDistance);
        Registry.register(registry, key, pointOfInterestType);
        registerBlockStates(registry.getOrThrow(key), states);
        return pointOfInterestType;
    }

    private static void registerBlockStates(Holder<PoiType> poiTypeEntry, Set<BlockState> states) {
        states.forEach(state -> {
            Holder<PoiType> registryEntry2 = TYPE_BY_STATE.put((BlockState) state, poiTypeEntry);
            if (registryEntry2 != null) {
                throw Util.pauseInIde(new IllegalStateException(String.format(Locale.ROOT, "%s is defined in more than one PoI type", state)));
            }
        });
    }
}
