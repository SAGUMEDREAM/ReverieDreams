package cc.thonly.reverie_dreams.mixin.accessor;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnPlacementType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.levelgen.Heightmap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(SpawnPlacements.class)
public interface SpawnPlacementsAccessor {
    @Invoker("register")
    static <T extends Mob> void callRegister(EntityType<T> type, SpawnPlacementType placementType, Heightmap.Types heightmap, SpawnPlacements.SpawnPredicate<T> spawnPredicate) {
        throw new AssertionError();
    }
}
