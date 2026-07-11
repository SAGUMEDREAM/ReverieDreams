package cc.thonly.reverie_dreams.item.weapon;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.util.sound.SoundEventPlayUtils;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class IronBarItem extends Item {
    public static final List<SoundEvent> SOUND_EVENTS = new ArrayList<>(List.of(SoundEvents.MACE_SMASH_GROUND_HEAVY, SoundEvents.MACE_SMASH_GROUND, SoundEvents.MACE_SMASH_AIR));

    public IronBarItem(Properties properties) {
        super(properties);
    }

    @Override
    public void hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        super.hurtEnemy(stack, target, attacker);
        double strength = 1.8D;

        double dx = attacker.getX() - target.getX();
        double dz = attacker.getZ() - target.getZ();

        target.knockback(strength, dx, dz);
        target.setDeltaMovement(
                target.getDeltaMovement().x,
                0.3D,
                target.getDeltaMovement().z
        );
        SoundEventPlayUtils.playSound(target.level(), target, this.getAttackSound(), SoundSource.NEUTRAL);
    }

    public SoundEvent getAttackSound() {
        return SOUND_EVENTS.get(ReverieDreams.RD.nextInt(SOUND_EVENTS.size() - 1));
    }

}
