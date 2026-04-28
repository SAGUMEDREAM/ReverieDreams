package cc.thonly.reverie_dreams.world.gen;

import cc.thonly.reverie_dreams.ReverieDreams;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadType;

public class ModStructureSets {

    public static final ResourceKey<StructureSet> ABANDONED_ALTAR = getOrCreateRegistryKey("abandoned_altar");
    public static final ResourceKey<StructureSet> ABANDONED_TORII = getOrCreateRegistryKey("abandoned_torii");
    public static final ResourceKey<StructureSet> MINI_BAR = getOrCreateRegistryKey("mini_bar");
    public static final ResourceKey<StructureSet> BAMBOO_FOREST_BBQ_STALL = getOrCreateRegistryKey("bamboo_forest_bbq_stall");
    public static final ResourceKey<StructureSet> BAMBOO_FOREST_HUT = getOrCreateRegistryKey("bamboo_forest_hut");
    public static final ResourceKey<StructureSet> SAKURAZUKA = getOrCreateRegistryKey("sakurazuka");
    public static final ResourceKey<StructureSet> OUTER_SHRINE = getOrCreateRegistryKey("outer_shrine");
    public static final ResourceKey<StructureSet> NETHER_HOT_SPRING = getOrCreateRegistryKey("nether_hot_spring");

    public static void init() {

    }

    public static void bootstrap(BootstrapContext<StructureSet> context) {
        HolderGetter<Structure> structureLookup = context.lookup(Registries.STRUCTURE);
        Holder.Reference<Structure> abandonedAltarStructure = structureLookup.getOrThrow(ModStructures.ABANDONED_ALTAR);
        Holder.Reference<Structure> abandonedToriiStructure = structureLookup.getOrThrow(ModStructures.ABANDONED_TORII);
        Holder.Reference<Structure> miniBarStructure = structureLookup.getOrThrow(ModStructures.MINI_BAR);
        Holder.Reference<Structure> outerShrineStructure = structureLookup.getOrThrow(ModStructures.OUTER_SHRINE);
        Holder.Reference<Structure> bambooForestBBQStallStructure = structureLookup.getOrThrow(ModStructures.BAMBOO_FOREST_BBQ_STALL);
        Holder.Reference<Structure> bambooForestHuyStructure = structureLookup.getOrThrow(ModStructures.BAMBOO_FOREST_HUT);
        Holder.Reference<Structure> sakurazukaStructure = structureLookup.getOrThrow(ModStructures.SAKURAZUKA);
        Holder.Reference<Structure> netherHotSpringStructure = structureLookup.getOrThrow(ModStructures.NETHER_HOT_SPRING);
        context.register(ABANDONED_ALTAR, new StructureSet(abandonedAltarStructure, new RandomSpreadStructurePlacement(165, 25, RandomSpreadType.LINEAR, 1947319134)));
        context.register(ABANDONED_TORII, new StructureSet(abandonedToriiStructure, new RandomSpreadStructurePlacement(165, 25, RandomSpreadType.LINEAR, 2134619147)));
        context.register(MINI_BAR, new StructureSet(miniBarStructure, new RandomSpreadStructurePlacement(165, 25, RandomSpreadType.LINEAR, 889525356)));
        context.register(OUTER_SHRINE, new StructureSet(outerShrineStructure, new RandomSpreadStructurePlacement(100, 20, RandomSpreadType.LINEAR, 1319720890)));
        context.register(BAMBOO_FOREST_BBQ_STALL, new StructureSet(bambooForestBBQStallStructure, new RandomSpreadStructurePlacement(100, 20, RandomSpreadType.LINEAR, 1191649132)));
        context.register(BAMBOO_FOREST_HUT, new StructureSet(bambooForestHuyStructure, new RandomSpreadStructurePlacement(100, 20, RandomSpreadType.LINEAR, 1191649132)));
        context.register(SAKURAZUKA, new StructureSet(sakurazukaStructure, new RandomSpreadStructurePlacement(100, 20, RandomSpreadType.LINEAR, 1191649132)));
        context.register(NETHER_HOT_SPRING, new StructureSet(netherHotSpringStructure, new RandomSpreadStructurePlacement(100, 20, RandomSpreadType.LINEAR, 1191649132)));
    }

    public static Holder.Reference<StructureSet> register(BootstrapContext<StructureSet> registry, ResourceKey<StructureSet> registryKey, StructureSet structureSet) {
        return registry.register(registryKey, structureSet);
    }

    public static ResourceKey<StructureSet> getOrCreateRegistryKey(String name) {
        return ResourceKey.create(Registries.STRUCTURE_SET, ReverieDreams.id(name));
    }
}
