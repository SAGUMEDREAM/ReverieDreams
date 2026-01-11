package cc.thonly.reverie_dreams.item.weapon;

import cc.thonly.reverie_dreams.item.base.SwordItem;
import cc.thonly.reverie_dreams.registry.tag.RDBlockTags;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.hurtingprojectile.windcharge.WindCharge;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.level.Level;

public class WindBlessingCane extends SwordItem {
    public static final ToolMaterial WIND_BLESSING_CANE = new ToolMaterial(RDBlockTags.EMPTY, 250, 4.0f, 3.5f, 5, ItemTags.IRON_TOOL_MATERIALS);

    public WindBlessingCane(float attackDamage, float attackSpeed, Properties settings) {
        super(WIND_BLESSING_CANE,attackDamage, attackSpeed, settings);
    }

    @Override
    public void hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        Level world = target.level();
        if (!world.isClientSide() && world instanceof ServerLevel serverWorld) {
            if (serverWorld.random.nextFloat() < 0.33f) {
                Projectile.spawnProjectileFromRotation((w, s, st) -> new WindCharge(w, s.getX(), s.getY(), s.getZ(), s.getDeltaMovement()), serverWorld, this.getDefaultInstance(), attacker, 0.0f, 1.5f, 1.0f);
                serverWorld.playSound(null, attacker.getX(), attacker.getY(), attacker.getZ(), SoundEvents.WIND_CHARGE_THROW, SoundSource.NEUTRAL, 0.5f, 0.4f / (serverWorld.getRandom().nextFloat() * 0.4f + 0.8f));
            }
        }
        super.hurtEnemy(stack, target, attacker);
    }
}
