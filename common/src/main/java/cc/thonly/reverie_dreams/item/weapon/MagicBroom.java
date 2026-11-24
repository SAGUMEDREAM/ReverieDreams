package cc.thonly.reverie_dreams.item.weapon;

import cc.thonly.reverie_dreams.item.IngredientStack;
import cc.thonly.reverie_dreams.item.base.SwordItem;
import cc.thonly.reverie_dreams.registry.content.entity.RDEntityTypes;
import cc.thonly.reverie_dreams.registry.tag.RDBlockTags;
import cc.thonly.reverie_dreams.registry.tag.RDItemTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustColorTransitionOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class MagicBroom extends SwordItem {
    public static final ToolMaterial MAGIC_BROOM = new ToolMaterial(RDBlockTags.EMPTY, 600, 6.0f, 2.0f, 10, RDItemTags.POWER_BLOCK);
    private static final float JUMP_ATTACK_BASE_BONUS = 2.0f;
    private static final float JUMP_ATTACK_BONUS_PER_BLOCK = 0.5f;
    private static final float JUMP_ATTACK_MAX_FALL_DISTANCE = 6.0f;
    private static final DustColorTransitionOptions NORMAL_MAGIC =
            new DustColorTransitionOptions(0x8A2BE2, 0x55FFFF, 1.1f);
    private static final DustColorTransitionOptions JUMP_SLASH_MAGIC =
            new DustColorTransitionOptions(0xFFD166, 0xFF4FD8, 1.25f);

    public MagicBroom(float attackDamage, float attackSpeed, Properties settings) {
        super(MAGIC_BROOM, attackDamage, attackSpeed, settings);
    }

    @Override
    public float getAttackDamageBonus(Entity target, float baseAttackDamage, DamageSource damageSource) {
        if (!(damageSource.getDirectEntity() instanceof LivingEntity attacker)
                || !isJumpAttack(attacker)) {
            return super.getAttackDamageBonus(target, baseAttackDamage, damageSource);
        }

        float fallDistance = (float) Math.min(attacker.fallDistance, JUMP_ATTACK_MAX_FALL_DISTANCE);
        return JUMP_ATTACK_BASE_BONUS + fallDistance * JUMP_ATTACK_BONUS_PER_BLOCK;
    }

    @Override
    public void postHurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        super.postHurtEnemy(stack, target, attacker);
        if (!(attacker.level() instanceof ServerLevel level)) {
            return;
        }

        if (isJumpAttack(attacker)) {
            spawnJumpSlashMagic(level, attacker, target);
        } else {
            spawnNormalAttackMagic(level, target);
        }
    }

    private static boolean isJumpAttack(LivingEntity attacker) {
        return !attacker.onGround()
                && !attacker.isInWater()
                && !attacker.onClimbable()
                && !attacker.isFallFlying()
                && attacker.fallDistance > 0.0;
    }

    /**
     * 普通攻击：目标脚下的双层紫青魔法环，以及沿目标身体上升的螺旋魔法尘。
     */
    private static void spawnNormalAttackMagic(ServerLevel level, LivingEntity target) {
        Vec3 center = target.position();
        double feetY = target.getY() + 0.12;

        for (int ring = 0; ring < 2; ring++) {
            double radius = 0.55 + ring * 0.38;
            int count = 16 + ring * 8;
            for (int i = 0; i < count; i++) {
                double angle = Math.PI * 2.0 * i / count + ring * 0.18;
                level.sendParticles(NORMAL_MAGIC,
                        center.x + Math.cos(angle) * radius,
                        feetY + ring * 0.05,
                        center.z + Math.sin(angle) * radius,
                        1, 0.0, 0.0, 0.0, 0.0);
            }
        }

        for (int i = 0; i < 12; i++) {
            double t = i / 11.0;
            double angle = t * Math.PI * 2.6;
            double radius = 0.55 * (1.0 - t * 0.25);
            level.sendParticles(NORMAL_MAGIC,
                    center.x + Math.cos(angle) * radius,
                    target.getY() + 0.15 + t * target.getBbHeight() * 0.85,
                    center.z + Math.sin(angle) * radius,
                    1, 0.0, 0.0, 0.0, 0.0);
        }

        level.sendParticles(ParticleTypes.ENCHANT,
                center.x, target.getY() + target.getBbHeight() * 0.55, center.z,
                18, 0.55, 0.65, 0.55, 0.02);
        level.sendParticles(ParticleTypes.END_ROD,
                center.x, target.getY() + target.getBbHeight() * 0.9, center.z,
                6, 0.25, 0.25, 0.25, 0.01);
        level.playSound(null, target.blockPosition(), SoundEvents.ENCHANTMENT_TABLE_USE,
                SoundSource.PLAYERS, 0.75f, 1.35f);
    }

    /**
     * 跳劈：从攻击者眼睛到目标的斜线斩击轨迹，以及落点处的金粉色冲击环。
     */
    private static void spawnJumpSlashMagic(ServerLevel level, LivingEntity attacker, LivingEntity target) {
        Vec3 start = attacker.getEyePosition();
        Vec3 end = target.position().add(0.0, target.getBbHeight() * 0.55, 0.0);
        Vec3 delta = end.subtract(start);

        for (int i = 0; i <= 14; i++) {
            Vec3 point = start.add(delta.scale(i / 14.0));
            level.sendParticles(JUMP_SLASH_MAGIC, point.x, point.y, point.z, 1, 0.02, 0.02, 0.02, 0.0);
        }

        double impactY = target.getY() + 0.1;
        for (int ring = 0; ring < 2; ring++) {
            double radius = 0.75 + ring * 0.55;
            int count = 20 + ring * 8;
            for (int i = 0; i < count; i++) {
                double angle = Math.PI * 2.0 * i / count + ring * 0.24;
                level.sendParticles(JUMP_SLASH_MAGIC,
                        target.getX() + Math.cos(angle) * radius,
                        impactY,
                        target.getZ() + Math.sin(angle) * radius,
                        1, 0.0, 0.0, 0.0, 0.0);
            }
        }

        level.sendParticles(ParticleTypes.END_ROD,
                target.getX(), target.getY() + 0.45, target.getZ(),
                10, 0.6, 0.35, 0.6, 0.015);
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                target.getX(), target.getY() + 0.3, target.getZ(),
                8, 0.35, 0.25, 0.35, 0.02);
        level.playSound(null, target.blockPosition(), SoundEvents.MACE_SMASH_GROUND_HEAVY,
                SoundSource.PLAYERS, 1.0f, 0.75f);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level world = context.getLevel();
        ItemStack itemStack = context.getItemInHand();
        Player player = context.getPlayer();
        BlockPos blockPos = context.getClickedPos();
        InteractionHand hand = context.getHand();
        if (!world.isClientSide() && player != null) {
            cc.thonly.reverie_dreams.entity.misc.MagicBroom entity = new cc.thonly.reverie_dreams.entity.misc.MagicBroom(RDEntityTypes.MAGIC_BROOM.value(), world, blockPos.getX() + 0.5f, blockPos.getY() + 1, blockPos.getZ() + 0.5f, IngredientStack.of(itemStack.copy()), player.getUUID());
            world.addFreshEntity(entity);
            itemStack.consume(1, player);
            player.swing(hand);
            return InteractionResult.SUCCESS_SERVER;
        }
        return InteractionResult.SUCCESS;
    }

}
