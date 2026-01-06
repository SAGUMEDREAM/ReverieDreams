package cc.thonly.reverie_dreams.item.weapon;

import cc.thonly.reverie_dreams.item.base.SwordItem;
import cc.thonly.reverie_dreams.registry.tag.RDBlockTags;
import cc.thonly.reverie_dreams.registry.tag.RDItemTags;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.windcharge.WindCharge;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.level.Level;

public class SwordOfHisou extends SwordItem {
    public static final ToolMaterial HISOU = new ToolMaterial(RDBlockTags.EMPTY, 1561, 8.0f, 4.5f, 10, RDItemTags.PEACH);

    public SwordOfHisou(float attackDamage, float attackSpeed, Properties settings) {
        super(HISOU, attackDamage, attackSpeed, settings);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        Level world = target.level();
        if (!world.isClientSide() && world instanceof ServerLevel serverWorld) {
            if (world.isRaining() && serverWorld.random.nextFloat() < 0.3f) {
                if (world.isThundering()) {
                    target.setRemainingFireTicks(2 * 20);
                    serverWorld.playSound(null, attacker.getX(), attacker.getY(), attacker.getZ(), SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.NEUTRAL, 0.5f, 0.4f / (serverWorld.getRandom().nextFloat() * 0.4f + 0.8f));
                }
                Projectile.spawnProjectileFromRotation((w, s, st) -> new WindCharge(w, s.getX(), s.getY(), s.getZ(), s.getDeltaMovement()), serverWorld, this.getDefaultInstance(), attacker, 0.0f, 1.5f, 1.0f);
                serverWorld.playSound(null, attacker.getX(), attacker.getY(), attacker.getZ(), SoundEvents.WIND_CHARGE_THROW, SoundSource.NEUTRAL, 0.5f, 0.4f / (serverWorld.getRandom().nextFloat() * 0.4f + 0.8f));
            }
        }
        return super.hurtEnemy(stack, target, attacker);
    }
}
