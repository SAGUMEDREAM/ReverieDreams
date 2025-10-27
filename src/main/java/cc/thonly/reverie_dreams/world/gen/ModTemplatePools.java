package cc.thonly.reverie_dreams.world.gen;

import cc.thonly.reverie_dreams.Touhou;
import com.mojang.datafixers.util.Pair;
import net.minecraft.registry.Registerable;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.structure.StructureSet;
import net.minecraft.structure.pool.SinglePoolElement;
import net.minecraft.structure.pool.StructurePool;
import net.minecraft.structure.pool.StructurePoolElement;
import net.minecraft.structure.pool.StructurePools;
import net.minecraft.world.gen.structure.Structure;

import java.util.List;
import java.util.function.Function;

public class ModTemplatePools {

    public static final RegistryKey<StructurePool> ABANDONED_ALTAR = getOrCreateRegistryKey("abandon_altar");
    public static final RegistryKey<StructurePool> ABANDONED_TORII = getOrCreateRegistryKey("abandoned_torii");
    public static final RegistryKey<StructurePool> MINI_BAR = getOrCreateRegistryKey("mini_bar");
    public static final RegistryKey<StructurePool> BAMBOO_FOREST_BBQ_STALL = getOrCreateRegistryKey("bamboo_forest_bbq_stall");
    public static final RegistryKey<StructurePool> BAMBOO_FOREST_HUT = getOrCreateRegistryKey("bamboo_forest_hut");
    public static final RegistryKey<StructurePool> SAKURAZUKA = getOrCreateRegistryKey("sakurazuka");
    public static final RegistryKey<StructurePool> OUTER_SHRINE = getOrCreateRegistryKey("outer_shrine");

    public static void init() {

    }

    public static void bootstrap(Registerable<StructurePool> context) {
        RegistryEntryLookup<StructurePool> structurePool = context.getRegistryLookup(RegistryKeys.TEMPLATE_POOL);
        RegistryEntry.Reference<StructurePool> emptyPool = structurePool.getOrThrow(StructurePools.EMPTY);
        context.register(ABANDONED_ALTAR, new StructurePool(
                emptyPool,
                List.of(Pair.of(StructurePoolElement.ofSingle(ModStructures.ABANDONED_ALTAR.getValue().toString()), 1)), StructurePool.Projection.RIGID)
        );
        context.register(ABANDONED_TORII, new StructurePool(
                emptyPool,
                List.of(Pair.of(StructurePoolElement.ofSingle(ModStructures.ABANDONED_TORII.getValue().toString()), 1)), StructurePool.Projection.RIGID)
        );
        context.register(MINI_BAR, new StructurePool(
                emptyPool,
                List.of(Pair.of(StructurePoolElement.ofSingle(ModStructures.MINI_BAR.getValue().toString()), 1)), StructurePool.Projection.RIGID)
        );
        context.register(OUTER_SHRINE, new StructurePool(
                emptyPool,
                List.of(Pair.of(StructurePoolElement.ofSingle(ModStructures.OUTER_SHRINE.getValue().toString()), 1)), StructurePool.Projection.RIGID)
        );
        context.register(BAMBOO_FOREST_BBQ_STALL, new StructurePool(
                emptyPool,
                List.of(Pair.of(StructurePoolElement.ofSingle(ModStructures.BAMBOO_FOREST_BBQ_STALL.getValue().toString()), 1)), StructurePool.Projection.RIGID)
        );
        context.register(BAMBOO_FOREST_HUT, new StructurePool(
                emptyPool,
                List.of(Pair.of(StructurePoolElement.ofSingle(ModStructures.BAMBOO_FOREST_HUT.getValue().toString()), 1)), StructurePool.Projection.RIGID)
        );
        context.register(SAKURAZUKA, new StructurePool(
                emptyPool,
                List.of(Pair.of(StructurePoolElement.ofSingle(ModStructures.SAKURAZUKA.getValue().toString()), 1)), StructurePool.Projection.RIGID)
        );
    }

    public static RegistryEntry.Reference<StructurePool> register(Registerable<StructurePool> registry, RegistryKey<StructurePool> registryKey, StructurePool structurePool) {
        return registry.register(registryKey, structurePool);
    }

    public static RegistryKey<StructurePool> getOrCreateRegistryKey(String name) {
        return RegistryKey.of(RegistryKeys.TEMPLATE_POOL, Touhou.id(name));
    }
}
