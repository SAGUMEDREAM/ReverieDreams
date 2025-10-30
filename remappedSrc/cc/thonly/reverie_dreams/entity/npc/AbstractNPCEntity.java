package cc.thonly.reverie_dreams.entity.npc;

import com.mojang.authlib.properties.Property;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractNPCEntity extends TamableAnimal {

    protected AbstractNPCEntity(EntityType<? extends TamableAnimal> entityType, Level world) {
        super(entityType, world);
    }

    @Override
    public abstract @Nullable LivingEntity getOwner();

    public abstract Property getSkin();

}
