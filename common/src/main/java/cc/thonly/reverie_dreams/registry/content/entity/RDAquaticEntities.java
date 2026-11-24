package cc.thonly.reverie_dreams.registry.content.entity;

import cc.thonly.reverie_dreams.entity.aquatic.AquaticSpecies;
import cc.thonly.reverie_dreams.entity.aquatic.CuisineAquaticEntity;
import cc.thonly.reverie_dreams.registry.content.item.RDIngredientItems;
import cc.thonly.reverie_dreams.registry.delegate.ItemDelegate;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.world.entity.EntityType;

/** Keeps living species and their existing cooking ingredients together. */
public final class RDAquaticEntities {
    private RDAquaticEntities() {}

    public static RegistrySupplier<EntityType<CuisineAquaticEntity>> type(AquaticSpecies species) {
        return switch (species) {
            case TROUT -> RDEntityTypes.TROUT;
            case LAMPREY -> RDEntityTypes.LAMPREY;
            case SALMON -> RDEntityTypes.SALMON;
            case TUNA -> RDEntityTypes.TUNA;
            case SUPREME_TUNA -> RDEntityTypes.SUPREME_TUNA;
            case SHRIMP -> RDEntityTypes.SHRIMP;
            case CRAB -> RDEntityTypes.CRAB;
            case OCTOPUS -> RDEntityTypes.OCTOPUS;
            case SEA_URCHIN -> RDEntityTypes.SEA_URCHIN;
        };
    }

    public static ItemDelegate ingredient(AquaticSpecies species) {
        return switch (species) {
            case TROUT -> RDIngredientItems.TROUT;
            case LAMPREY -> RDIngredientItems.HAGFISH;
            case SALMON -> RDIngredientItems.SALMON;
            case TUNA -> RDIngredientItems.TUNA;
            case SUPREME_TUNA -> RDIngredientItems.SUPREME_TUNA;
            case SHRIMP -> RDIngredientItems.SHRIMP;
            case CRAB -> RDIngredientItems.CRAB;
            case OCTOPUS -> RDIngredientItems.OCTOPUS;
            case SEA_URCHIN -> RDIngredientItems.SEA_URCHIN;
        };
    }
}
