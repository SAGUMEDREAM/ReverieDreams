package cc.thonly.reverie_dreams.item.weapon;

import cc.thonly.reverie_dreams.item.base.SwordItem;
import cc.thonly.reverie_dreams.registry.tag.RDBlockTags;
import cc.thonly.reverie_dreams.registry.content.sound.RDSoundEvents;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.hurtingprojectile.windcharge.WindCharge;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * 祝风御币。
 *
 * <p>下雨时攻击有更高概率释放风弹。潜行并持续使用 10 秒可发动
 * “风之祝福”，消耗饥饿值并为周围非敌对生物提供幸运与生命恢复。</p>
 */
public class WindBlessingCane extends SwordItem {
    public static final ToolMaterial WIND_BLESSING_CANE = new ToolMaterial(
            RDBlockTags.EMPTY, 250, 4.0f, 3.5f, 5, ItemTags.IRON_TOOL_MATERIALS
    );

    private static final float BASE_WIND_CHARGE_CHANCE = 0.33F;
    private static final float RAIN_WIND_CHARGE_CHANCE = 0.66F;
    private static final int BLESSING_USE_TICKS = 20 * 5;
    private static final int BLESSING_COOLDOWN_TICKS = 20 * 360;
    private static final float BLESSING_HUNGER_COST = 5.0F; // 25% of a full 20-point hunger bar
    private static final double BLESSING_RADIUS = 24.0D;
    private static final int BLESSING_LUCK_TICKS = 20 * 60 * 3;
    private static final int BLESSING_REGENERATION_TICKS = 20 * 90;

    public WindBlessingCane(float attackDamage, float attackSpeed, Properties settings) {
        super(WIND_BLESSING_CANE, attackDamage, attackSpeed, settings);
    }

    @Override
    public void hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        Level world = target.level();
        if (!world.isClientSide() && world instanceof ServerLevel serverWorld) {
            float chance = serverWorld.isRaining() ? RAIN_WIND_CHARGE_CHANCE : BASE_WIND_CHARGE_CHANCE;
            if (serverWorld.getRandom().nextFloat() < chance) {
                Projectile.spawnProjectileFromRotation((w, s, st) ->
                                new WindCharge(w, s.getX(), s.getEyeY(), s.getZ(), s.getDeltaMovement()),
                        serverWorld, this.getDefaultInstance(), attacker, 0.0f, 1.5f, 1.0f
                );
                serverWorld.playSound(null, attacker.getX(), attacker.getEyeY(), attacker.getZ(),
                        SoundEvents.WIND_CHARGE_THROW, SoundSource.NEUTRAL,
                        0.5f, 0.4f / (serverWorld.getRandom().nextFloat() * 0.4f + 0.8f));
            }
        }
        super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public InteractionResult use(Level world, Player user, InteractionHand hand) {
        if (world.isClientSide()) {
            if (world.isRaining()) {
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.PASS;
        }
        if (!(world instanceof ServerLevel serverLevel) || !(user instanceof ServerPlayer player)) {
            return InteractionResult.PASS;
        }
        if (!player.isShiftKeyDown() || !serverLevel.isRaining()) {
            return InteractionResult.PASS;
        }

        ItemStack stack = player.getItemInHand(hand);
        if (player.getCooldowns().isOnCooldown(stack) || availableFood(player) < BLESSING_HUNGER_COST) {
            return InteractionResult.FAIL;
        }

        player.startUsingItem(hand);
        return InteractionResult.CONSUME;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level world, LivingEntity user) {
        if (!(world instanceof ServerLevel serverLevel) || !(user instanceof ServerPlayer player)) {
            return stack;
        }
        if (!serverLevel.isRaining()) {
            return stack;
        }

        applyBlessing(serverLevel, player);
        consumeHunger(player, BLESSING_HUNGER_COST);
        player.getCooldowns().addCooldown(stack, BLESSING_COOLDOWN_TICKS);
        serverLevel.playSound(null, player.blockPosition(), RDSoundEvents.SPELL_CARD.value(),
                SoundSource.PLAYERS, 1.0F, 1.0F);
        return stack;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity user) {
        return BLESSING_USE_TICKS;
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.BOW;
    }

    @Override
    public void onUseTick(Level world, LivingEntity user, ItemStack stack, int remainingUseDuration) {
        if (!(world instanceof ServerLevel serverLevel) || !(user instanceof ServerPlayer player)) {
            return;
        }
        if (!player.isShiftKeyDown() || !serverLevel.isRaining()) {
            player.stopUsingItem();
            return;
        }

        int elapsed = BLESSING_USE_TICKS - remainingUseDuration;
        if (elapsed % 4 == 0) {
            spawnPentagram(serverLevel, player, elapsed / 4);
        }
        if (elapsed % 20 == 0) {
            serverLevel.playSound(null, player.blockPosition(), RDSoundEvents.TICK_WAVE.value(),
                    SoundSource.PLAYERS, 0.5F, 1.1F);
        }
    }

    private static void applyBlessing(ServerLevel level, ServerPlayer player) {
        AABB area = new AABB(
                player.getX() - BLESSING_RADIUS, player.getY() - 12.0D, player.getZ() - BLESSING_RADIUS,
                player.getX() + BLESSING_RADIUS, player.getY() + 12.0D, player.getZ() + BLESSING_RADIUS
        );

        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, area,
                target -> target.isAlive() && !target.isSpectator() && !(target instanceof Enemy))) {

            entity.addEffect(new MobEffectInstance(MobEffects.LUCK, BLESSING_LUCK_TICKS, 1));
            entity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, BLESSING_REGENERATION_TICKS, 0));

            spawnBlessingParticles(level, player, entity);
        }

        level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                player.getX(), player.getY() + 0.8D, player.getZ(),
                36, 2.2D, 1.2D, 2.2D, 0.02D);

        level.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                player.getX(), player.getY() + 0.8D, player.getZ(),
                36, 2.2D, 1.2D, 2.2D, 0.02D);
    }

    /**
     * 从施法者向受到祝福的生物发射粒子。
     */
    private static void spawnBlessingParticles(ServerLevel level, ServerPlayer player, LivingEntity target) {
        Vec3 start = player.position().add(0.0D, 1.0D, 0.0D);
        Vec3 end = target.position().add(0.0D, target.getBbHeight() * 0.5D, 0.0D);

        Vec3 direction = end.subtract(start);
        double distance = direction.length();

        if (distance < 0.1D) {
            return;
        }

        Vec3 normalized = direction.normalize();

        int samples = Math.max(4, (int) (distance * 3.0D));

        for (int i = 0; i <= samples; i++) {
            double progress = i / (double) samples;
            Vec3 position = start.add(normalized.scale(distance * progress));

            ParticleOptions particle = i % 2 == 0
                    ? ParticleTypes.END_ROD
                    : ParticleTypes.WAX_ON;

            level.sendParticles(
                    particle,
                    position.x, position.y, position.z,
                    1,
                    0.025D, 0.025D, 0.025D,
                    0.0D
            );
        }

        // 目标位置产生祝福汇聚效果
        level.sendParticles(
                ParticleTypes.HAPPY_VILLAGER,
                end.x, end.y, end.z,
                8,
                0.3D, 0.4D, 0.3D,
                0.05D
        );
    }

    /**
     * Draws a five-pointed star with alternating blue and green particles.
     * The star is drawn on the ground and slowly alternates its colours.
     */
    private static void spawnPentagram(ServerLevel level, ServerPlayer player, int step) {
        double radius = 2.8D;
        Vec3 center = player.position();
        double[][] points = new double[5][2];
        for (int i = 0; i < 5; i++) {
            double angle = -Math.PI / 2.0D + i * Math.PI * 2.0D / 5.0D;
            points[i][0] = Math.cos(angle) * radius;
            points[i][1] = Math.sin(angle) * radius;
        }

        ParticleOptions blue = ParticleTypes.SOUL_FIRE_FLAME;
        ParticleOptions green = ParticleTypes.HAPPY_VILLAGER;
        for (int i = 0; i < 5; i++) {
            int next = (i + 2) % 5;
            ParticleOptions particle = (i + step) % 2 == 0 ? blue : green;
            for (int sample = 0; sample <= 6; sample++) {
                double t = sample / 6.0D;
                double x = center.x + points[i][0] + (points[next][0] - points[i][0]) * t;
                double z = center.z + points[i][1] + (points[next][1] - points[i][1]) * t;
                level.sendParticles(particle, x, center.y + 0.15D, z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
            }
        }
    }

    private static float availableFood(Player player) {
        FoodData food = player.getFoodData();
        return food.getFoodLevel() + food.getSaturationLevel();
    }

    private static void consumeHunger(ServerPlayer player, float amount) {
        FoodData food = player.getFoodData();
        float saturation = food.getSaturationLevel();
        float fromSaturation = Math.min(saturation, amount);
        food.setSaturation(saturation - fromSaturation);

        float remaining = amount - fromSaturation;
        if (remaining > 0.0F) {
            food.setFoodLevel(Math.max(0, food.getFoodLevel() - (int) Math.ceil(remaining)));
        }
    }
}
