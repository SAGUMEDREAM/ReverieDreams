package cc.thonly.reverie_dreams.entity.projectile;

import cc.thonly.reverie_dreams.registry.content.block.RDBlocks;
import cc.thonly.reverie_dreams.registry.content.entity.RDEntityTypes;
import net.minecraft.core.particles.BlockParticleOption;
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

public class ThrownIceBlock extends ThrownIceProjectile {
    public ThrownIceBlock(EntityType<? extends ThrownIceBlock> type, Level level) {
        super(type, level);
    }

    public ThrownIceBlock(Level level, LivingEntity owner, ItemStack stack) {
        super(RDEntityTypes.THROWN_ICE_BLOCK.get(), owner, level, stack);
    }

    public ThrownIceBlock(Level level, double x, double y, double z, ItemStack stack) {
        super(RDEntityTypes.THROWN_ICE_BLOCK.get(), x, y, z, level, stack);
    }

    @Override
    protected Item getDefaultItem() {
        return Items.ICE;
    }

    @Override
    protected float hitDamage() {
        return 4.0f;
    }

    @Override
    protected int slownessTicks() {
        return 80;
    }

    @Override
    protected int slownessAmplifier() {
        return 1;
    }

    @Override
    protected int freezeTicks() {
        return 80;
    }

    @Override
    protected int aoeRadius() {
        return 2;
    }

    @Override
    protected ParticleOptions breakParticle() {
        return new BlockParticleOption(ParticleTypes.BLOCK, RDBlocks.MAGIC_ICE_BLOCK.defaultBlockState());
    }

    @Override
    protected SoundEvent breakSound() {
        return SoundEvents.GLASS_BREAK;
    }
}
