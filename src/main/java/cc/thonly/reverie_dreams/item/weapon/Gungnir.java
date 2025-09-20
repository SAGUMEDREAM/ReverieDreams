package cc.thonly.reverie_dreams.item.weapon;

import cc.thonly.reverie_dreams.data.ModTags;
import cc.thonly.reverie_dreams.item.base.SwordItem;
import net.minecraft.component.EnchantmentEffectComponentTypes;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ProjectileItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.item.consume.UseAction;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Position;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class Gungnir extends SwordItem implements ProjectileItem {
    public static final ToolMaterial GUNGNIR = new ToolMaterial(ModTags.BlockTypeTag.EMPTY, 1561, 8.0f, 5.5f, 10, ItemTags.NETHERITE_TOOL_MATERIALS);

    public Gungnir(float attackDamage, float attackSpeed, Item.Settings settings) {
        super(GUNGNIR, attackDamage, attackSpeed, settings);
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.BOW;
    }

    @Override
    public int getMaxUseTime(ItemStack stack, LivingEntity user) {
        return 72000;
    }

    @Override
    public boolean onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
        if (!(user instanceof PlayerEntity playerEntity)) {
            return false;
        }
        int i = this.getMaxUseTime(stack, user) - remainingUseTicks;
        if (i < 10) {
            return false;
        }
        float f = EnchantmentHelper.getTridentSpinAttackStrength(stack, playerEntity);
        if (f > 0.0f && !playerEntity.isTouchingWaterOrRain()) {
            return false;
        }
        if (stack.willBreakNextUse()) {
            return false;
        }
        RegistryEntry<SoundEvent> registryEntry = EnchantmentHelper.getEffect(stack, EnchantmentEffectComponentTypes.TRIDENT_SOUND).orElse(SoundEvents.ITEM_TRIDENT_THROW);
        playerEntity.incrementStat(Stats.USED.getOrCreateStat(this));
        if (world instanceof ServerWorld serverWorld) {
            stack.damage(1, playerEntity);
            if (f == 0.0f) {
                ItemStack itemStack = stack.splitUnlessCreative(1, playerEntity);
                TridentEntity tridentEntity = ProjectileEntity.spawnWithVelocity(TridentEntity::new, serverWorld, itemStack, playerEntity, 0.0f, 2.5f, 1.0f);
                if (playerEntity.isInCreativeMode()) {
                    tridentEntity.pickupType = PersistentProjectileEntity.PickupPermission.CREATIVE_ONLY;
                }
                world.playSoundFromEntity(null, tridentEntity, registryEntry.value(), SoundCategory.PLAYERS, 1.0f, 1.0f);
                return true;
            }
        }
        if (f > 0.0f) {
            float g = playerEntity.getYaw();
            float h = playerEntity.getPitch();
            float j = -MathHelper.sin(g * ((float)Math.PI / 180)) * MathHelper.cos(h * ((float)Math.PI / 180));
            float k = -MathHelper.sin(h * ((float)Math.PI / 180));
            float l = MathHelper.cos(g * ((float)Math.PI / 180)) * MathHelper.cos(h * ((float)Math.PI / 180));
            float m = MathHelper.sqrt(j * j + k * k + l * l);
            playerEntity.addVelocity(j *= f / m, k *= f / m, l *= f / m);
            playerEntity.useRiptide(20, 8.0f, stack);
            if (playerEntity.isOnGround()) {
                float n = 1.1999999f;
                playerEntity.move(MovementType.SELF, new Vec3d(0.0, 1.1999999284744263, 0.0));
            }
            world.playSoundFromEntity(null, playerEntity, registryEntry.value(), SoundCategory.PLAYERS, 1.0f, 1.0f);
            return true;
        }
        return false;
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);
        if (itemStack.willBreakNextUse()) {
            return ActionResult.FAIL;
        }
        if (EnchantmentHelper.getTridentSpinAttackStrength(itemStack, user) > 0.0f && !user.isTouchingWaterOrRain()) {
            return ActionResult.FAIL;
        }
        user.setCurrentHand(hand);
        return ActionResult.CONSUME;
    }

    @Override
    public ProjectileEntity createEntity(World world, Position pos, ItemStack stack, Direction direction) {
        TridentEntity tridentEntity = new TridentEntity(world, pos.getX(), pos.getY(), pos.getZ(), stack.copyWithCount(1));
        tridentEntity.pickupType = PersistentProjectileEntity.PickupPermission.ALLOWED;
        return tridentEntity;
    }
}
