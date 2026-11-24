package cc.thonly.reverie_dreams.registry.content.block;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.api.registry.block.CompostingChanceRegistry;
import cc.thonly.reverie_dreams.registry.delegate.BlockDelegate;
import cc.thonly.reverie_dreams.world.gensokyo.tree.GensokyoForestType;
import cc.thonly.reverie_dreams.world.sapling.GensokyoSaplings;
import dev.architectury.registry.fuel.FuelRegistry;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

/** Registered by RDWoodBlocks; vanilla saplings and leaf loot remain independent. */
public final class RDGensokyoSaplings {
    public static final Map<GensokyoForestType, BlockDelegate> SAPLINGS = register();

    private static Map<GensokyoForestType, BlockDelegate> register() {
        var result = new EnumMap<GensokyoForestType, BlockDelegate>(GensokyoForestType.class);
        for (var entry : GensokyoSaplings.entries()) {
            result.put(entry.forest(), RDBlocks.registerSimpleBlock(entry.path(),
                    properties -> new SaplingBlock(entry.grower(), properties),
                    BlockBehaviour.Properties.ofFullCopy(entry.appearance())));
        }
        return Collections.unmodifiableMap(result);
    }

    public static void initialize() {
        SAPLINGS.values().forEach(block -> CompostingChanceRegistry.registerBlock(block, 0.3F));
        ReverieDreams.COMMON_LATE_INIT.add(() -> SAPLINGS.values().forEach(block -> FuelRegistry.register(100, block)));
    }

    private RDGensokyoSaplings() {}
}
