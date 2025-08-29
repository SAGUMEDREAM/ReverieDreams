package cc.thonly.reverie_dreams.item.weapon;

import cc.thonly.reverie_dreams.data.ModTags;
import cc.thonly.reverie_dreams.item.base.SwordItem;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.WindChargeEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolMaterial;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Position;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public class MapleLeafFan extends SwordItem {
    public static final ToolMaterial MAPLE_LEAF_FAN = new ToolMaterial(ModTags.BlockTypeTag.EMPTY, 250, 4.0f, 2.5f, 5, ItemTags.GOLD_TOOL_MATERIALS);
    public static float POWER = 1.5f;

    public MapleLeafFan(float attackDamage, float attackSpeed, Settings settings) {
        super(MAPLE_LEAF_FAN, attackDamage + 1f, attackSpeed - 2.4f, settings.useCooldown(0.5f));
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);
        if (!world.isClient() && world instanceof ServerWorld serverWorld) {
            ProjectileEntity.spawnWithVelocity((world2, shooter, stack) -> new WindChargeEntity(user, world, user.getPos().getX(), user.getEyePos().getY(), user.getPos().getZ()), serverWorld, itemStack, user, 0.0f, POWER, 1.0f);
            world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.ENTITY_WIND_CHARGE_THROW, SoundCategory.NEUTRAL, 0.5f, 0.4f / (world.getRandom().nextFloat() * 0.4f + 0.8f));
            user.incrementStat(Stats.USED.getOrCreateStat(this));
            if (itemStack.isDamageable()) {
                itemStack.damage(1, user, EquipmentSlot.MAINHAND);
            }
            return ActionResult.SUCCESS_SERVER;
        }
        return ActionResult.SUCCESS;
    }

    public ProjectileEntity createEntity(World world, Position pos, ItemStack stack, Direction direction) {
        Random random = world.getRandom();
        double d = random.nextTriangular((double) direction.getOffsetX(), 0.11485000000000001);
        double e = random.nextTriangular((double) direction.getOffsetY(), 0.11485000000000001);
        double f = random.nextTriangular((double) direction.getOffsetZ(), 0.11485000000000001);
        Vec3d vec3d = new Vec3d(d, e, f);
        WindChargeEntity windChargeEntity = new WindChargeEntity(world, pos.getX(), pos.getY(), pos.getZ(), vec3d);
        windChargeEntity.setVelocity(vec3d);
        return windChargeEntity;
    }
}
