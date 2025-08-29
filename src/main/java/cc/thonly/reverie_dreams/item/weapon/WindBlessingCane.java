package cc.thonly.reverie_dreams.item.weapon;

import cc.thonly.reverie_dreams.data.ModTags;
import cc.thonly.reverie_dreams.item.base.SwordItem;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.WindChargeEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolMaterial;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.World;

public class WindBlessingCane extends SwordItem {
    public static final ToolMaterial WIND_BLESSING_CANE = new ToolMaterial(ModTags.BlockTypeTag.EMPTY, 250, 4.0f, 3.5f, 5, ItemTags.IRON_TOOL_MATERIALS);

    public WindBlessingCane(float attackDamage, float attackSpeed, Settings settings) {
        super(WIND_BLESSING_CANE,attackDamage, attackSpeed, settings);
    }

    @Override
    public void postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        World world = target.getWorld();
        if (!world.isClient() && world instanceof ServerWorld serverWorld) {
            if (serverWorld.random.nextFloat() < 0.2f) {
                ProjectileEntity.spawnWithVelocity((w, s, st) -> new WindChargeEntity(w, s.getX(), s.getY(), s.getZ(), s.getVelocity()), serverWorld, this.getDefaultStack(), attacker, 0.0f, 1.5f, 1.0f);
                serverWorld.playSound(null, attacker.getX(), attacker.getY(), attacker.getZ(), SoundEvents.ENTITY_WIND_CHARGE_THROW, SoundCategory.NEUTRAL, 0.5f, 0.4f / (serverWorld.getRandom().nextFloat() * 0.4f + 0.8f));
            }
        }
        super.postHit(stack, target, attacker);
    }
}
