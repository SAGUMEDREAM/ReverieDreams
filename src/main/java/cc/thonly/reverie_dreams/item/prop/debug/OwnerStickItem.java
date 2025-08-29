package cc.thonly.reverie_dreams.item.prop.debug;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public class OwnerStickItem extends Item {
    public OwnerStickItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {
        World world = user.getWorld();
        if (world.isClient()) return ActionResult.SUCCESS;
        if (entity instanceof TameableEntity target) {
            target.setOwner(user);
            ((ServerWorld) world).spawnParticles(ParticleTypes.HEART, target.getX(), target.getY() + 1.0, target.getZ(), 5, 0.5, 0.5, 0.5, 0.1);
        }
        return ActionResult.SUCCESS_SERVER;
    }
}
