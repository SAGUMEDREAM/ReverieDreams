package cc.thonly.reverie_dreams.item.weapon;

import cc.thonly.reverie_dreams.item.base.SwordItem;
import cc.thonly.reverie_dreams.registry.tag.RDBlockTags;
import cc.thonly.reverie_dreams.registry.tag.RDItemTags;
import cc.thonly.reverie_dreams.util.sound.SoundEventPlayUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.hurtingprojectile.windcharge.WindCharge;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.level.Level;

public class Rokanken extends SwordItem implements YoumuSwordUsing {
    public static final ToolMaterial ROKANKEN = new ToolMaterial(RDBlockTags.EMPTY, 1250, 8.0f, 5.5f, 10, RDItemTags.SILVER_BLOCK);

    public Rokanken(float attackDamage, float attackSpeed, Properties settings) {
        super(ROKANKEN, attackDamage, attackSpeed, settings);
    }

    @Override
    public void postHurtEnemy(ItemStack itemStack, LivingEntity mob, LivingEntity attacker) {
        super.postHurtEnemy(itemStack, mob, attacker);
        Level level = attacker.level();
        if (level.isClientSide()) {
            return;
        }
        var look = attacker.getLookAngle();
        attacker.addEffect(new MobEffectInstance(MobEffects.SATURATION, 1));
        double forwardStrength = 0.34;
        double yBoost = 0.05;

        attacker.push(
                look.x * forwardStrength,
                yBoost,
                look.z * forwardStrength
        );

        attacker.hurtMarked = true;
        attacker.fallDistance = 0;
        SoundEventPlayUtils.playSound(level, mob.getX(), mob.getY(), mob.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.NEUTRAL);
    }

    @Override
    public InteractionResult use(Level world, Player user, InteractionHand hand) {
        this.useItem(world, user, hand);
        if (!world.isClientSide() && world instanceof ServerLevel serverWorld) {
            ItemStack itemStack = user.getItemInHand(hand);
            Projectile.spawnProjectileFromRotation((w, s, st) ->
                    new WindCharge(w, s.getX(), s.getEyeY(), s.getZ(), s.getDeltaMovement()), serverWorld, this.getDefaultInstance(), user, 0.0f, 1.5f, 1.0f
            );
            serverWorld.playSound(null, user.getX(), user.getEyeY(), user.getZ(), SoundEvents.WIND_CHARGE_THROW, SoundSource.NEUTRAL, 0.5f, 0.4f / (serverWorld.getRandom().nextFloat() * 0.4f + 0.8f));
            ItemCooldowns cooldowns = user.getCooldowns();
            cooldowns.addCooldown(itemStack, 20 * 5);
        }
        return InteractionResult.SUCCESS;
    }
}
