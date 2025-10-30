package cc.thonly.reverie_dreams.world.gen;

import cc.thonly.reverie_dreams.Touhou;
import com.mojang.datafixers.util.Pair;
import java.util.List;
import java.util.function.Function;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.Pools;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;

public class ModTemplatePools {

    public static final ResourceKey<StructureTemplatePool> ABANDONED_ALTAR = getOrCreateRegistryKey("abandon_altar");
    public static final ResourceKey<StructureTemplatePool> ABANDONED_TORII = getOrCreateRegistryKey("abandoned_torii");
    public static final ResourceKey<StructureTemplatePool> MINI_BAR = getOrCreateRegistryKey("mini_bar");
    public static final ResourceKey<StructureTemplatePool> BAMBOO_FOREST_BBQ_STALL = getOrCreateRegistryKey("bamboo_forest_bbq_stall");
    public static final ResourceKey<StructureTemplatePool> BAMBOO_FOREST_HUT = getOrCreateRegistryKey("bamboo_forest_hut");
    public static final ResourceKey<StructureTemplatePool> SAKURAZUKA = getOrCreateRegistryKey("sakurazuka");
    public static final ResourceKey<StructureTemplatePool> OUTER_SHRINE = getOrCreateRegistryKey("outer_shrine");

    public static void init() {

    }

    public static void bootstrap(BootstrapContext<StructureTemplatePool> context) {
        HolderGetter<StructureTemplatePool> structurePool = context.lookup(Registries.TEMPLATE_POOL);
        Holder.Reference<StructureTemplatePool> emptyPool = structurePool.getOrThrow(Pools.EMPTY);
        context.register(ABANDONED_ALTAR, new StructureTemplatePool(
                emptyPool,
                List.of(Pair.of(StructurePoolElement.single(ModStructures.ABANDONED_ALTAR.location().toString()), 1)), StructureTemplatePool.Projection.RIGID)
        );
        context.register(ABANDONED_TORII, new StructureTemplatePool(
                emptyPool,
                List.of(Pair.of(StructurePoolElement.single(ModStructures.ABANDONED_TORII.location().toString()), 1)), StructureTemplatePool.Projection.RIGID)
        );
        context.register(MINI_BAR, new StructureTemplatePool(
                emptyPool,
                List.of(Pair.of(StructurePoolElement.single(ModStructures.MINI_BAR.location().toString()), 1)), StructureTemplatePool.Projection.RIGID)
        );
        context.register(OUTER_SHRINE, new StructureTemplatePool(
                emptyPool,
                List.of(Pair.of(StructurePoolElement.single(ModStructures.OUTER_SHRINE.location().toString()), 1)), StructureTemplatePool.Projection.RIGID)
        );
        context.register(BAMBOO_FOREST_BBQ_STALL, new StructureTemplatePool(
                emptyPool,
                List.of(Pair.of(StructurePoolElement.single(ModStructures.BAMBOO_FOREST_BBQ_STALL.location().toString()), 1)), StructureTemplatePool.Projection.RIGID)
        );
        context.register(BAMBOO_FOREST_HUT, new StructureTemplatePool(
                emptyPool,
                List.of(Pair.of(StructurePoolElement.single(ModStructures.BAMBOO_FOREST_HUT.location().toString()), 1)), StructureTemplatePool.Projection.RIGID)
        );
        context.register(SAKURAZUKA, new StructureTemplatePool(
                emptyPool,
                List.of(Pair.of(StructurePoolElement.single(ModStructures.SAKURAZUKA.location().toString()), 1)), StructureTemplatePool.Projection.RIGID)
        );
    }

    public static Holder.Reference<StructureTemplatePool> register(BootstrapContext<StructureTemplatePool> registry, ResourceKey<StructureTemplatePool> registryKey, StructureTemplatePool structurePool) {
        return registry.register(registryKey, structurePool);
    }

    public static ResourceKey<StructureTemplatePool> getOrCreateRegistryKey(String name) {
        return ResourceKey.create(Registries.TEMPLATE_POOL, Touhou.id(name));
    }
}
