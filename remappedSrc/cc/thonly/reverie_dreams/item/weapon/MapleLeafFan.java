package cc.thonly.reverie_dreams.item.weapon;

import cc.thonly.reverie_dreams.data.ModTags;
import cc.thonly.reverie_dreams.item.base.SwordItem;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.windcharge.WindCharge;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class MapleLeafFan extends SwordItem {
    public static final ToolMaterial MAPLE_LEAF_FAN = new ToolMaterial(ModTags.BlockTypeTag.EMPTY, 250, 4.0f, 2.5f, 5, ItemTags.GOLD_TOOL_MATERIALS);
    public static float POWER = 1.5f;

    public MapleLeafFan(float attackDamage, float attackSpeed, Properties settings) {
        super(MAPLE_LEAF_FAN, attackDamage, attackSpeed, settings.useCooldown(0.5f));
    }

    @Override
    public InteractionResult use(Level world, Player user, InteractionHand hand) {
        ItemStack itemStack = user.getItemInHand(hand);
        if (!world.isClientSide() && world instanceof ServerLevel serverWorld) {
            Projectile.spawnProjectileFromRotation((world2, shooter, stack) -> new WindCharge(user, world, user.position().x(), user.getEyePosition().y(), user.position().z()), serverWorld, itemStack, user, 0.0f, POWER, 1.0f);
            world.playSound(null, user.getX(), user.getY(), user.getZ(), SoundEvents.WIND_CHARGE_THROW, SoundSource.NEUTRAL, 0.5f, 0.4f / (world.getRandom().nextFloat() * 0.4f + 0.8f));
            user.awardStat(Stats.ITEM_USED.get(this));
            if (itemStack.isDamageableItem()) {
                itemStack.hurtAndBreak(1, user, EquipmentSlot.MAINHAND);
            }
            return InteractionResult.SUCCESS_SERVER;
        }
        return InteractionResult.SUCCESS;
    }

    public Projectile createEntity(Level world, Position pos, ItemStack stack, Direction direction) {
        RandomSource random = world.getRandom();
        double d = random.triangle((double) direction.getStepX(), 0.11485000000000001);
        double e = random.triangle((double) direction.getStepY(), 0.11485000000000001);
        double f = random.triangle((double) direction.getStepZ(), 0.11485000000000001);
        Vec3 vec3d = new Vec3(d, e, f);
        WindCharge windChargeEntity = new WindCharge(world, pos.x(), pos.y(), pos.z(), vec3d);
        windChargeEntity.setDeltaMovement(vec3d);
        return windChargeEntity;
    }
}
