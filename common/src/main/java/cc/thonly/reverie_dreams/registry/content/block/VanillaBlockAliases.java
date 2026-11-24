package cc.thonly.reverie_dreams.registry.content.block;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.ReverieDreamsConfiguration;
import cc.thonly.reverie_dreams.api.registry.AliasManager;
import cc.thonly.reverie_dreams.block.bundle.WoodBundle;
import cc.thonly.reverie_dreams.registry.delegate.BlockDelegate;
import dev.architectury.platform.Platform;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;

import java.util.List;

public final class VanillaBlockAliases {
    private VanillaBlockAliases() {
    }

    /** The platform calls this for both BLOCK and ITEM once its entries are available. */
    public static void apply(Registry<?> registry, AliasManager.Registrar registrar) {
        ReverieDreamsConfiguration config = ReverieDreams.config();
        boolean development = Platform.isDevelopmentEnvironment();
        if (enabled(config.enableVanillaWoolAliases, development)) {
            for (RDWoolBlocks.Family wool : RDWoolBlocks.FAMILIES) {
                add(registry, registrar, wool.slab().getId().getPath(), wool.slab());
                add(registry, registrar, wool.stairs().getId().getPath(), wool.stairs());
            }
        }
        if (enabled(config.enableVanillaPoplarAliases, development)) {
            add(registry, registrar, "red_poplar_leaves", RDWoodBlocks.IROHA_MAPLE_BUNDLE.leaves());
            add(registry, registrar, "orange_poplar_leaves", RDWoodBlocks.HAUCHIWA_MAPLE_BUNDLE.leaves());
            WoodBundle itaya = RDWoodBlocks.ITAYA_MAPLE_BUNDLE;
            add(registry, registrar, "yellow_poplar_leaves", itaya.leaves());
            add(registry, registrar, "poplar_sapling", itaya.sapling());
            add(registry, registrar, "stripped_poplar_log", itaya.strippedLog());
            add(registry, registrar, "stripped_poplar_wood", itaya.strippedWood());
            for (BlockDelegate block : List.of(itaya.log(), itaya.wood(), itaya.planks(), itaya.slab(),
                    itaya.stairs(), itaya.door(), itaya.trapdoor(), itaya.fence(), itaya.fenceGate(),
                    itaya.button(), itaya.pressurePlate())) {
                String suffix = block.getId().getPath().substring("itaya_maple".length());
                add(registry, registrar, "poplar" + suffix, block);
            }
        }
    }

    static boolean enabled(Boolean configured, boolean development) {
        return configured != null ? configured : development;
    }

    private static void add(Registry<?> registry, AliasManager.Registrar registrar, String path, BlockDelegate block) {
        Identifier from = Identifier.withDefaultNamespace(path);
        Identifier to = block.getId();
        if (registry.get(from).isPresent()) {
            ReverieDreams.LOGGER.warn("Skipping {} alias {} -> {} because {} is already in use",
                    registry.key().identifier(), from, to, from);
            return;
        }
        try {
            registrar.addAlias(from, to);
        } catch (IllegalArgumentException conflict) {
            ReverieDreams.LOGGER.warn("Skipping {} alias {} -> {}: {}",
                    registry.key().identifier(), from, to, conflict.getMessage());
        }
    }
}
