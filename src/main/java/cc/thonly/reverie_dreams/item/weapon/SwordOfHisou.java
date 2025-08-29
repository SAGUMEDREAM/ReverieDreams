package cc.thonly.reverie_dreams.item.weapon;

import cc.thonly.reverie_dreams.data.ModTags;
import cc.thonly.reverie_dreams.item.base.SwordItem;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.WindChargeEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolMaterial;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.World;

public class SwordOfHisou extends SwordItem {
    public static final ToolMaterial HISOU = new ToolMaterial(ModTags.BlockTypeTag.EMPTY, 1561, 8.0f, 4.5f, 10, ModTags.ItemTypeTag.PEACH);

    public SwordOfHisou(float attackDamage, float attackSpeed, Settings settings) {
        super(HISOU, attackDamage + 1, attackSpeed - 2.4f, settings);
    }

    @Override
    public void postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        World world = target.getWorld();
        if (!world.isClient() && world instanceof ServerWorld serverWorld) {
            if (world.isRaining() && serverWorld.random.nextFloat() < 0.3f) {
                if (world.isThundering()) {
                    target.setFireTicks(2 * 20);
                    serverWorld.playSound(null, attacker.getX(), attacker.getY(), attacker.getZ(), SoundEvents.ENTITY_LIGHTNING_BOLT_THUNDER, SoundCategory.NEUTRAL, 0.5f, 0.4f / (serverWorld.getRandom().nextFloat() * 0.4f + 0.8f));
                }
                ProjectileEntity.spawnWithVelocity((w, s, st) -> new WindChargeEntity(w, s.getX(), s.getY(), s.getZ(), s.getVelocity()), serverWorld, this.getDefaultStack(), attacker, 0.0f, 1.5f, 1.0f);
                serverWorld.playSound(null, attacker.getX(), attacker.getY(), attacker.getZ(), SoundEvents.ENTITY_WIND_CHARGE_THROW, SoundCategory.NEUTRAL, 0.5f, 0.4f / (serverWorld.getRandom().nextFloat() * 0.4f + 0.8f));
            }
        }
        super.postHit(stack, target, attacker);
    }
}
