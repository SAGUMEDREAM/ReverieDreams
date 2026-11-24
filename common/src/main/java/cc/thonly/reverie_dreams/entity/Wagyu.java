package cc.thonly.reverie_dreams.entity;

import cc.thonly.reverie_dreams.registry.content.entity.RDEntityTypes;
import cc.thonly.reverie_dreams.registry.content.variant.RDCowVariants;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.cow.Cow;
import net.minecraft.world.entity.animal.cow.CowVariant;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class Wagyu extends Cow {
    public Wagyu(EntityType<? extends Wagyu> type, Level level) {
        super(type, level);
        this.registryAccess().lookupOrThrow(Registries.COW_VARIANT)
                .get(RDCowVariants.WAGYU).ifPresent(this::setVariant);
    }

    @Override
    public void setVariant(Holder<CowVariant> variant) {
        // Vanilla Cow also calls this when selecting a biome variant and when reading saved data.
        if (variant.is(RDCowVariants.WAGYU)) {
            super.setVariant(variant);
        }
    }

    @Override
    @Nullable
    public Wagyu getBreedOffspring(ServerLevel level, AgeableMob partner) {
        return RDEntityTypes.WAGYU.value().create(level, EntitySpawnReason.BREEDING);
    }
}
