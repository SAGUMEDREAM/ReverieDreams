package cc.thonly.reverie_dreams.item.prop;

import cc.thonly.reverie_dreams.api.registry.tag.ConventionalEntityTypeTags;
import cc.thonly.reverie_dreams.entity.projectile.ThrownExorcismPaper;
import cc.thonly.reverie_dreams.registry.tag.RDEntityTypeTags;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * 退魔符：
 * <ul>
 *     <li>对亡灵、妖怪、妖精有效；</li>
 *     <li>贴符直接生效，也可右击投掷；</li>
 *     <li>Boss 不吃长时间控制，只承受伤害和发光；</li>
 *     <li>玩家、已驯服伙伴和友方 NPC 不会被误伤。</li>
 * </ul>
 */
public class ExorcismPaperItem extends Item {
    public static final int COOLDOWN_TICKS = 10;
    private static final float DIRECT_DAMAGE = 13.0f;
    private static final float AREA_DAMAGE = 7.5f;
    private static final double AREA_RADIUS = 2.5D;

    public ExorcismPaperItem(Properties settings) {
        super(settings);
    }

    @Override
    public InteractionResult use(Level world, Player user, InteractionHand hand) {
        if (user == null) {
            return InteractionResult.FAIL;
        }
        if (world.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (!(world instanceof ServerLevel serverLevel)) {
            return InteractionResult.PASS;
        }

        ItemStack stack = user.getItemInHand(hand);
        ItemCooldowns cooldowns = user.getCooldowns();
        if (cooldowns.isOnCooldown(stack)) {
            return InteractionResult.FAIL;
        }

        ThrownExorcismPaper projectile = new ThrownExorcismPaper(serverLevel, user, stack.copyWithCount(1));
        projectile.shootFromRotation(user, user.getXRot(), user.getYRot(), 0.0f, 1.5f, 0.8f);
        serverLevel.addFreshEntity(projectile);

        cooldowns.addCooldown(stack, COOLDOWN_TICKS);
        consumePaper(stack, user);
        user.swing(hand);
        world.playSound(null, user.blockPosition(), SoundEvents.BOOK_PAGE_TURN, SoundSource.PLAYERS, 0.8f, 1.15f);
        return InteractionResult.SUCCESS_SERVER;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        return this.use(context.getLevel(), context.getPlayer(), context.getHand());
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player user, LivingEntity target, InteractionHand hand) {
        if (user == null || target == null) {
            return InteractionResult.PASS;
        }
        if (user.level().isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (!(user.level() instanceof ServerLevel serverLevel)) {
            return InteractionResult.PASS;
        }

        ItemCooldowns cooldowns = user.getCooldowns();
        if (cooldowns.isOnCooldown(stack)) {
            return InteractionResult.FAIL;
        }
        if (!isValidTarget(target)) {
            return InteractionResult.PASS;
        }

        applyExorcism(serverLevel, target, user, true);
        cooldowns.addCooldown(stack, COOLDOWN_TICKS);
        consumePaper(stack, user);
        user.swing(hand);
        return InteractionResult.SUCCESS_SERVER;
    }

    public static void applyExorcism(ServerLevel level, LivingEntity target, Entity owner, boolean fullStrength) {
        if (!isValidTarget(target)) {
            return;
        }

        float damage = fullStrength ? DIRECT_DAMAGE : AREA_DAMAGE;
        DamageSource source = owner instanceof LivingEntity livingOwner
                ? target.damageSources().indirectMagic(livingOwner, livingOwner)
                : target.damageSources().magic();
        target.hurtServer(level, source, damage);

        if (!target.isAlive()) {
            return;
        }

        int duration = fullStrength ? 160 : 80;
        if (!isBoss(target)) {
            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, duration, 1));
            target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, duration, 1));
        }
        target.addEffect(new MobEffectInstance(MobEffects.GLOWING, fullStrength ? 300 : 120, 0));

        level.sendParticles(ParticleTypes.ENCHANT,
                target.getX(), target.getY() + target.getBbHeight() * 0.5D, target.getZ(),
                24, 0.45D, 0.55D, 0.45D, 0.05D);
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                target.getX(), target.getY() + target.getBbHeight() * 0.5D, target.getZ(),
                8, 0.3D, 0.35D, 0.3D, 0.02D);
        level.playSound(null, target.blockPosition(), SoundEvents.ENCHANTMENT_TABLE_USE,
                SoundSource.PLAYERS, 0.9f, 1.45f);
    }

    public static void purifyArea(ServerLevel level, Vec3 center, Entity owner) {
        purifyArea(level, center, owner, null);
    }

    public static void purifyArea(ServerLevel level, Vec3 center, Entity owner, LivingEntity exclude) {
        AABB area = new AABB(
                center.x - AREA_RADIUS, center.y - AREA_RADIUS, center.z - AREA_RADIUS,
                center.x + AREA_RADIUS, center.y + AREA_RADIUS, center.z + AREA_RADIUS
        );
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, area, ExorcismPaperItem::isValidTarget)) {
            if (target == exclude) {
                continue;
            }
            applyExorcism(level, target, owner, false);
        }
        level.sendParticles(ParticleTypes.ENCHANT,
                center.x, center.y, center.z, 30, 0.7D, 0.7D, 0.7D, 0.08D);
        level.playSound(null, center.x, center.y, center.z, SoundEvents.ENCHANTMENT_TABLE_USE,
                SoundSource.PLAYERS, 0.7f, 1.65f);
    }

    public static boolean isValidTarget(LivingEntity target) {
        if (target == null || !target.isAlive() || target.isInvulnerable()) {
            return false;
        }
        // 只保护玩家和已驯服的伙伴；野生妖怪/妖精仍然可以被退魔。
        if (target instanceof Player
                || (target instanceof TamableAnimal tameable && tameable.isTame())) {
            return false;
        }
        return target.is(EntityTypeTags.UNDEAD)
                || target.is(RDEntityTypeTags.YOKAI)
                || target.is(RDEntityTypeTags.YOUSEI);
    }

    public static boolean isBoss(LivingEntity target) {
        return target.getType() == EntityType.WITHER
                || target.getType().builtInRegistryHolder().is(ConventionalEntityTypeTags.BOSSES);
    }

    private static void consumePaper(ItemStack stack, Player user) {
        if (!user.hasInfiniteMaterials()) {
            stack.consume(1, user);
        }
    }
}
