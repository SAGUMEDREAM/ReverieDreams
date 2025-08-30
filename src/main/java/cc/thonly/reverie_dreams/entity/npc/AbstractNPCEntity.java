package cc.thonly.reverie_dreams.entity.npc;

import cc.thonly.polymer.entity.PlayerPolymerEntity;
import com.mojang.authlib.properties.Property;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractNPCEntity extends TameableEntity {

    protected AbstractNPCEntity(EntityType<? extends TameableEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    public abstract @Nullable LivingEntity getOwner();

    public abstract Property getSkin();

}
