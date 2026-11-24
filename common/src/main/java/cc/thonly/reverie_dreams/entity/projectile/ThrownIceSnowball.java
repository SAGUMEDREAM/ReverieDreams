package cc.thonly.reverie_dreams.entity.projectile;

import cc.thonly.reverie_dreams.registry.content.entity.RDEntityTypes;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public class ThrownIceSnowball extends ThrownIceProjectile {
    public ThrownIceSnowball(EntityType<? extends ThrownIceSnowball> type, Level level) {
        super(type, level);
    }

    public ThrownIceSnowball(Level level, LivingEntity owner, ItemStack stack) {
        super(RDEntityTypes.THROWN_ICE_SNOWBALL.get(), owner, level, stack);
    }

    public ThrownIceSnowball(Level level, double x, double y, double z, ItemStack stack) {
        super(RDEntityTypes.THROWN_ICE_SNOWBALL.get(), x, y, z, level, stack);
    }

    @Override
    protected Item getDefaultItem() {
        return Items.SNOWBALL;
    }

    @Override
    protected float hitDamage() {
        return 1.0f;
    }

    @Override
    protected int slownessTicks() {
        return 40;
    }

    @Override
    protected int slownessAmplifier() {
        return 0;
    }

    @Override
    protected int freezeTicks() {
        return 30;
    }

    @Override
    protected int aoeRadius() {
        return 0;
    }

    @Override
    protected ParticleOptions breakParticle() {
        return ParticleTypes.ITEM_SNOWBALL;
    }

    @Override
    protected SoundEvent breakSound() {
        return SoundEvents.SNOW_BREAK;
    }
}
