package cc.thonly.reverie_dreams.item.weapon;

import cc.thonly.reverie_dreams.danmaku.DanmakuTrajectory;
import cc.thonly.reverie_dreams.danmaku.DanmakuTypes;
import cc.thonly.reverie_dreams.entity.misc.BaguaFurnaceEntity;
import cc.thonly.reverie_dreams.entity.misc.DanmakuEntity;
import cc.thonly.reverie_dreams.server.DelayedTask;
import cc.thonly.reverie_dreams.sound.SoundEventInit;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.HungerManager;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.SmallFireballEntity;
import net.minecraft.entity.projectile.thrown.SnowballEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Items;
import net.minecraft.item.consume.UseAction;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class BaguaFurnace extends Item {
    public BaguaFurnace(Settings settings) {
        super(settings);
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        if (!world.isClient() && user instanceof ServerPlayerEntity player) {
            ServerWorld serverWorld = (ServerWorld) world;
            BaguaFurnaceEntity baguaFurnaceEntity = new BaguaFurnaceEntity(serverWorld, user);
            serverWorld.spawnEntity(baguaFurnaceEntity);

            HungerManager hungerManager = player.getHungerManager();
            hungerManager.add(-6, -6);

            ItemCooldownManager itemCooldownManager = player.getItemCooldownManager();
            itemCooldownManager.set(stack, 20 * 40);
            if (!player.isInCreativeMode()) {
                stack.damage(15, player);
            }
        }
        return stack;
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        return Items.FLINT_AND_STEEL.useOnBlock(context);
    }

    @Override
    public int getMaxUseTime(ItemStack stack, LivingEntity user) {
        return 40;
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.BOW;
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);
        if (!world.isClient && world instanceof ServerWorld serverWorld) {
            if (user.isSneaking()) {
                user.setCurrentHand(hand);
                return ActionResult.CONSUME;
            }
            world.playSound((Entity) null, user.getX(), user.getY(), user.getZ(), SoundEvents.ENTITY_SNOWBALL_THROW, SoundCategory.NEUTRAL, 0.5F, 0.4F / (world.getRandom().nextFloat() * 0.4F + 0.8F));
            SmallFireballEntity fireballEntity = new SmallFireballEntity(serverWorld, user.getX(), user.getEyeY(), user.getZ(), new Vec3d(0, 0, 0));
            fireballEntity.setVelocity(user, user.getPitch(), user.getYaw(), 0.0F, 1.5f, 1.0F);
            world.spawnEntity(fireballEntity);
            ItemCooldownManager itemCooldownManager = user.getItemCooldownManager();
            itemCooldownManager.set(itemStack, 20);
            return ActionResult.SUCCESS_SERVER;
        }

        user.incrementStat(Stats.USED.getOrCreateStat(this));
        itemStack.decrementUnlessCreative(1, user);
        return ActionResult.SUCCESS;
    }

}
