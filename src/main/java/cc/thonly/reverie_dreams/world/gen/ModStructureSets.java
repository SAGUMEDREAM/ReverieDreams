package cc.thonly.reverie_dreams.world.gen;

import cc.thonly.reverie_dreams.Touhou;
import net.minecraft.registry.*;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.structure.StructureSet;
import net.minecraft.structure.pool.StructurePool;
import net.minecraft.world.gen.chunk.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.gen.chunk.placement.SpreadType;
import net.minecraft.world.gen.chunk.placement.StructurePlacement;
import net.minecraft.world.gen.chunk.placement.StructurePlacementType;
import net.minecraft.world.gen.structure.Structure;

import java.util.List;

public class ModStructureSets {

    public static final RegistryKey<StructureSet> ABANDONED_ALTAR = getOrCreateRegistryKey("abandoned_altar");
    public static final RegistryKey<StructureSet> ABANDONED_TORII = getOrCreateRegistryKey("abandoned_torii");
    public static final RegistryKey<StructureSet> MINI_BAR = getOrCreateRegistryKey("mini_bar");
    public static final RegistryKey<StructureSet> BAMBOO_FOREST_BBQ_STALL = getOrCreateRegistryKey("bamboo_forest_bbq_stall");
    public static final RegistryKey<StructureSet> BAMBOO_FOREST_HUT = getOrCreateRegistryKey("bamboo_forest_hut");
    public static final RegistryKey<StructureSet> SAKURAZUKA = getOrCreateRegistryKey("sakurazuka");
    public static final RegistryKey<StructureSet> OUTER_SHRINE = getOrCreateRegistryKey("outer_shrine");

    public static void init() {

    }

    public static void bootstrap(Registerable<StructureSet> context) {
        RegistryEntryLookup<Structure> structureLookup = context.getRegistryLookup(RegistryKeys.STRUCTURE);
        RegistryEntry.Reference<Structure> abandonedAltarStructure = structureLookup.getOrThrow(ModStructures.ABANDONED_ALTAR);
        RegistryEntry.Reference<Structure> abandonedToriiStructure = structureLookup.getOrThrow(ModStructures.ABANDONED_TORII);
        RegistryEntry.Reference<Structure> miniBarStructure = structureLookup.getOrThrow(ModStructures.MINI_BAR);
        RegistryEntry.Reference<Structure> outerShrineStructure = structureLookup.getOrThrow(ModStructures.OUTER_SHRINE);
        RegistryEntry.Reference<Structure> bambooForestBBQStallStructure = structureLookup.getOrThrow(ModStructures.BAMBOO_FOREST_BBQ_STALL);
        RegistryEntry.Reference<Structure> bambooForestHuyStructure = structureLookup.getOrThrow(ModStructures.BAMBOO_FOREST_HUT);
        RegistryEntry.Reference<Structure> sakurazukaStructure = structureLookup.getOrThrow(ModStructures.SAKURAZUKA);
        context.register(ABANDONED_ALTAR, new StructureSet(abandonedAltarStructure, new RandomSpreadStructurePlacement(165, 25, SpreadType.LINEAR, 1947319134)));
        context.register(ABANDONED_TORII, new StructureSet(abandonedToriiStructure, new RandomSpreadStructurePlacement(165, 25, SpreadType.LINEAR, 2134619147)));
        context.register(MINI_BAR, new StructureSet(miniBarStructure, new RandomSpreadStructurePlacement(165, 25, SpreadType.LINEAR, 889525356)));
        context.register(OUTER_SHRINE, new StructureSet(outerShrineStructure, new RandomSpreadStructurePlacement(100, 20, SpreadType.LINEAR, 1319720890)));
        context.register(BAMBOO_FOREST_BBQ_STALL, new StructureSet(bambooForestBBQStallStructure, new RandomSpreadStructurePlacement(100, 20, SpreadType.LINEAR, 1191649132)));
        context.register(BAMBOO_FOREST_HUT, new StructureSet(bambooForestHuyStructure, new RandomSpreadStructurePlacement(100, 20, SpreadType.LINEAR, 1191649132)));
        context.register(SAKURAZUKA, new StructureSet(sakurazukaStructure, new RandomSpreadStructurePlacement(100, 20, SpreadType.LINEAR, 1191649132)));
    }

    public static RegistryEntry.Reference<StructureSet> register(Registerable<StructureSet> registry, RegistryKey<StructureSet> registryKey, StructureSet structureSet) {
        return registry.register(registryKey, structureSet);
    }

    public static RegistryKey<StructureSet> getOrCreateRegistryKey(String name) {
        return RegistryKey.of(RegistryKeys.STRUCTURE_SET, Touhou.id(name));
    }
}
