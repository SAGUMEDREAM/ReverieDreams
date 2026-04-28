package cc.thonly.reverie_dreams.util.math;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record Vec3d2Entity(@NotNull Vec3 vec3d, @Nullable Entity entity) {
}
