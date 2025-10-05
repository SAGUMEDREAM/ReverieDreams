package cc.thonly.reverie_dreams.entity.villager;

import cc.thonly.reverie_dreams.Touhou;
import cc.thonly.reverie_dreams.block.ModBlocks;
import com.google.common.collect.ImmutableSet;
import net.fabricmc.fabric.api.object.builder.v1.trade.TradeOfferHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.world.poi.PointOfInterestType;
import net.minecraft.world.poi.PointOfInterestTypes;

import java.util.Locale;
import java.util.Set;

public class ModPointOfInterestTypes extends PointOfInterestTypes {
    public static final RegistryKey<PointOfInterestType> HAWKERS = of("hawkers");
    public static final RegistryKey<PointOfInterestType> PRIEST = of("priest");
    public static final RegistryKey<PointOfInterestType> MONEY_SHOP_CLERK = of("money_shop_clerk");

    public static void registers() {
        register(HAWKERS, getStatesOfBlock(ModBlocks.WOODEN_BOX.chestBlock()), 1, 2);
        register(PRIEST, getStatesOfBlock(ModBlocks.CASH_BOX_BLOCK), 1, 2);
        register(MONEY_SHOP_CLERK, getStatesOfBlock(Blocks.ENDER_CHEST), 1, 2);
    }

    private static Set<BlockState> getStatesOfBlock(Block block) {
        return ImmutableSet.copyOf(block.getStateManager().getStates());
    }

    private static RegistryKey<PointOfInterestType> of(String id) {
        return RegistryKey.of(RegistryKeys.POINT_OF_INTEREST_TYPE, Touhou.id(id));
    }

    private static PointOfInterestType register(RegistryKey<PointOfInterestType> key, Set<BlockState> states, int ticketCount, int searchDistance) {
        Registry<PointOfInterestType> registry = Registries.POINT_OF_INTEREST_TYPE;
        PointOfInterestType pointOfInterestType = new PointOfInterestType(states, ticketCount, searchDistance);
        Registry.register(registry, key, pointOfInterestType);
        registerStates(registry.getOrThrow(key), states);
        return pointOfInterestType;
    }

    private static void registerStates(RegistryEntry<PointOfInterestType> poiTypeEntry, Set<BlockState> states) {
        states.forEach(state -> {
            RegistryEntry<PointOfInterestType> registryEntry2 = POI_STATES_TO_TYPE.put((BlockState) state, poiTypeEntry);
            if (registryEntry2 != null) {
                throw Util.getFatalOrPause(new IllegalStateException(String.format(Locale.ROOT, "%s is defined in more than one PoI type", state)));
            }
        });
    }
}
