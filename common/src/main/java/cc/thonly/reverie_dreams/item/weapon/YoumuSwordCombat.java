package cc.thonly.reverie_dreams.item.weapon;

import cc.thonly.reverie_dreams.entity.misc.BaseDanmakuEntity;
import cc.thonly.reverie_dreams.registry.content.effect.RDStatusEffects;
import cc.thonly.reverie_dreams.registry.content.entity.RDEntityTypes;
import dev.architectury.event.EventResult;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.*;
import java.util.function.Supplier;

import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;

/**
 * Transient, server-owned sword actions. Values never retain the weak player key or its world.
 */
public final class YoumuSwordCombat {
    public static final int MIN_CHARGE = 8, FULL_CHARGE = 16, PARRY_TICKS = 6;
    private static final Map<Player, State> STATES = new WeakHashMap<>();
    private static final ThreadLocal<Projectile> INCOMING = new ThreadLocal<>();

    private YoumuSwordCombat() {
    }

    private static final class State {
        final ResourceKey<Level> dimension;
        long lastTick, start, counterUntil;
        final Map<Identifier, CooldownWindow> cooldowns = new HashMap<>();
        int chargeStage, purificationTicks;
        ItemStack charge = ItemStack.EMPTY, guard = ItemStack.EMPTY, partner = ItemStack.EMPTY;
        InteractionHand hand;
        boolean parried;
        Attack attack;
        long meleeTick = -1;
        UUID meleeTarget;
        float meleeStrength;

        State(Player player) {
            dimension = player.level().dimension();
            lastTick = player.level().getGameTime();
        }
    }

    private record CooldownWindow(long started, long until, ItemStack key) {
    }

    private static final class Attack {
        final ItemStack sword;
        final InteractionHand hand;
        final Vec3 direction, waveDirection;
        final Set<UUID> hit = new HashSet<>();
        final float damage;
        final boolean full;
        int dashTicks = 4, waveTicks;
        final double step;
        Vec3 wavePosition;

        Attack(ItemStack sword, InteractionHand hand, Vec3 look, float damage, double distance, boolean full) {
            this.sword = sword;
            this.hand = hand;
            direction = YoumuSwordGeometry.horizontal(look);
            waveDirection = look.normalize();
            this.damage = damage;
            step = distance / dashTicks;
            this.full = full;
        }
    }

    private static boolean live(Player player) {
        return !player.level().isClientSide() && player.level() instanceof ServerLevel && player.isAlive() && !player.isSpectator()
                && !(player instanceof ServerPlayer serverPlayer && serverPlayer.hasDisconnected());
    }

    private static State state(Player player) {
        State existing = STATES.get(player);
        if (existing != null && (!existing.dimension.equals(player.level().dimension()) || player.level().getGameTime() < existing.lastTick))
            STATES.remove(player);
        return STATES.computeIfAbsent(player, State::new);
    }

    static boolean ready(Player player, ItemStack sword) {
        return live(player) && !player.isPassenger() && !player.isSleeping()
                && !sword.isEmpty() && !player.getCooldowns().isOnCooldown(sword);
    }

    static void cooldown(Player player, ItemStack sword, int ticks) {
        State s = state(player);
        long now = player.level().getGameTime();
        Identifier group = player.getCooldowns().getCooldownGroup(sword);
        s.cooldowns.put(group, new CooldownWindow(now, now + ticks, sword.copyWithCount(1)));
        player.getCooldowns().addCooldown(sword, ticks);
    }

    private static boolean checkReady(Player player, ItemStack sword) {
        if (ready(player, sword))
            return true;
        if (live(player)) {
            State s = STATES.get(player);
            if (player.getCooldowns().isOnCooldown(sword)) {
                CooldownWindow window = s == null ? null : s.cooldowns.get(player.getCooldowns().getCooldownGroup(sword));
                long ticks = window == null ? 0 : Math.max(0, window.until - player.level().getGameTime());
                actionBar(player, ticks > 0 ? YoumuSwordFeedback.text("cooldown", sword.getHoverName(), String.format(Locale.ROOT, "%.1f", ticks / 20.0))
                        : YoumuSwordFeedback.text("cooldown_unknown", sword.getHoverName()));
            } else
                actionBar(player, YoumuSwordFeedback.text("unavailable"));
        }
        return false;
    }

    private static void wear(Player player, ItemStack stack, InteractionHand hand, int amount) {
        if (!player.hasInfiniteMaterials())
            stack.hurtAndBreak(amount, player,
                    hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
    }

    public static InteractionResult startCharge(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide()) {
            player.startUsingItem(hand);
            return InteractionResult.CONSUME;
        }
        if (!checkReady(player, player.getItemInHand(hand)) || !(player.getItemInHand(hand).getItem() instanceof Rokanken))
            return InteractionResult.FAIL;
        State s = state(player);
        if (s.attack != null || !s.charge.isEmpty() || !s.guard.isEmpty()) {
            actionBar(player, YoumuSwordFeedback.text("unavailable"));
            return InteractionResult.FAIL;
        }
        s.charge = player.getItemInHand(hand);
        s.hand = hand;
        s.start = level.getGameTime();
        s.lastTick = s.start;
        s.parried = false;
        s.counterUntil = 0;
        s.chargeStage = 0;
        s.partner = hand == InteractionHand.MAIN_HAND && player.getOffhandItem().getItem() instanceof Hakuroken
                && !player.getCooldowns().isOnCooldown(player.getOffhandItem())
                ? player.getOffhandItem() : ItemStack.EMPTY;
        player.startUsingItem(hand);
        actionBar(player, YoumuSwordFeedback.chargeText(0, false));
        level.playSound(null, player.blockPosition(), SoundEvents.NOTE_BLOCK_CHIME.value(), SoundSource.PLAYERS, .35f, .6f);
        return InteractionResult.CONSUME;
    }

    public static InteractionResult startGuard(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide()) {
            player.startUsingItem(hand);
            return InteractionResult.CONSUME;
        }
        if (!checkReady(player, player.getItemInHand(hand)) || !(player.getItemInHand(hand).getItem() instanceof Hakuroken))
            return InteractionResult.FAIL;
        State s = state(player);
        if (s.attack != null || !s.charge.isEmpty() || !s.guard.isEmpty()) {
            actionBar(player, YoumuSwordFeedback.text("unavailable"));
            return InteractionResult.FAIL;
        }
        s.guard = player.getItemInHand(hand);
        s.hand = hand;
        s.start = level.getGameTime();
        s.lastTick = s.start;
        s.parried = false;
        cooldown(player, s.guard, 30);
        player.startUsingItem(hand);
        actionBar(player, YoumuSwordFeedback.text("guard_window"));
        YoumuSwordFeedback.guard((ServerLevel) level, player);
        return InteractionResult.CONSUME;
    }

    public static void onUseTick(Level level, Player player, ItemStack stack) {
        if (level.isClientSide())
            return;
        State s = STATES.get(player);
        if (s == null || !live(player) || !s.dimension.equals(level.dimension())
                || player.getItemInHand(s.hand) != stack || (s.charge != stack && s.guard != stack)) {
            player.stopUsingItem();
            return;
        }
        long held = level.getGameTime() - s.start;
        if (held < 0 || held > 60 || (!s.guard.isEmpty() && held >= 10)) {
            if (!s.charge.isEmpty()) {
                cooldown(player, s.charge, 12);
                s.charge = ItemStack.EMPTY;
                actionBar(player, YoumuSwordFeedback.text("interrupted"));
            }
            s.guard = ItemStack.EMPTY;
            player.stopUsingItem();
            return;
        }
        if (!s.charge.isEmpty()) {
            int stage = held >= FULL_CHARGE ? 2 : held >= MIN_CHARGE ? 1 : 0;
            if (stage > s.chargeStage)
                level.playSound(null, player.blockPosition(),
                        SoundEvents.NOTE_BLOCK_CHIME.value(), SoundSource.PLAYERS, .65f, stage == 2 ? 1.6f : 1.1f);
            s.chargeStage = stage;
            if (held % 2 == 0)
                actionBar(player, YoumuSwordFeedback.chargeText(held,
                        s.parried && level.getGameTime() <= s.counterUntil && !s.partner.isEmpty() && player.getOffhandItem() == s.partner));
            if (held % (stage == 2 ? 6 : 4) == 0)
                YoumuSwordFeedback.charge((ServerLevel) level, player, held);
        } else if (!s.guard.isEmpty()) {
            if (held == PARRY_TICKS)
                actionBar(player, YoumuSwordFeedback.text("guard_ended"));
            if (held < PARRY_TICKS && held % 2 == 0)
                YoumuSwordFeedback.guard((ServerLevel) level, player);
        }
    }

    public static boolean releaseCharge(Level level, Player player, ItemStack stack) {
        if (!(level instanceof ServerLevel world) || !live(player))
            return false;
        State s = STATES.get(player);
        if (s == null || s.charge != stack || s.hand == null || player.getItemInHand(s.hand) != stack
                || !s.dimension.equals(level.dimension()))
            return false;
        long held = level.getGameTime() - s.start;
        boolean counter = s.parried && level.getGameTime() <= s.counterUntil
                && !s.partner.isEmpty() && player.getOffhandItem() == s.partner;
        s.charge = ItemStack.EMPTY;
        s.partner = ItemStack.EMPTY;
        if (held < (counter ? 2 : MIN_CHARGE) || held > 60 || !ready(player, stack)) {
            cooldown(player, stack, 12);
            actionBar(player, YoumuSwordFeedback.text(held < (counter ? 2 : MIN_CHARGE) ? "too_short" : "interrupted"));
            world.playSound(null, player.blockPosition(), SoundEvents.NOTE_BLOCK_CHIME.value(), SoundSource.PLAYERS, .25f, .5f);
            return false;
        }
        double progress = Math.clamp((double) held / FULL_CHARGE, .5, 1);
        boolean full = held >= FULL_CHARGE || counter;
        float damage = (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE) * (float) (1 + .35 * progress) + (counter ? 4 : 0);
        cooldown(player, stack, counter ? 80 : 100);
        wear(player, stack, s.hand, full ? 4 : 2);
        if (stack.isEmpty())
            return false;
        s.attack = new Attack(stack, s.hand, player.getLookAngle(), damage, 4 + 2 * progress, full);
        actionBar(player, YoumuSwordFeedback.text(counter ? "counter_released" : full ? "wave_released" : "dash_released"));
        YoumuSwordFeedback.slash(world, player.position().add(0, 1, 0), s.attack.direction, full);
        world.playSound(null, player.blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1, counter ? 1.6f : .8f);
        slash(world, player, player.position().add(0, 1, 0), s.attack.direction, 2.5, .8, damage, s.attack.hit);
        return true;
    }

    public static void endGuard(Player player) {
        if (!player.level().isClientSide()) {
            State s = STATES.get(player);
            if (s != null && !s.guard.isEmpty()) {
                s.guard = ItemStack.EMPTY;
                actionBar(player, YoumuSwordFeedback.text("guard_ended"));
            }
        }
    }

    public static boolean tryParry(LivingEntity entity, DamageSource source, float amount) {
        if (!(entity instanceof Player player) || !live(player) || amount <= 0 || !Float.isFinite(amount)
                || source.is(DamageTypeTags.BYPASSES_SHIELD) || source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)
                || source.is(DamageTypeTags.IS_EXPLOSION) || !player.isUsingItem())
            return false;
        State s = STATES.get(player);
        if (s == null || s.parried || !s.dimension.equals(player.level().dimension()))
            return false;
        long elapsed = player.level().getGameTime() - s.start;
        if (elapsed < 0 || elapsed >= PARRY_TICKS)
            return false;
        ItemStack white = s.guard;
        boolean paired = !s.charge.isEmpty() && !s.partner.isEmpty() && player.getMainHandItem() == s.charge
                && player.getOffhandItem() == s.partner && player.getUseItem() == s.charge
                && !player.getCooldowns().isOnCooldown(s.partner);
        if (paired)
            white = s.partner;
        else if (white.isEmpty() || player.getUseItem() != white || player.getItemInHand(s.hand) != white)
            return false;
        Entity attacker = source.getEntity(), direct = source.getDirectEntity();
        Projectile incoming = INCOMING.get();
        if (incoming != null && incoming.getOwner() == attacker)
            direct = incoming;
        if (attacker == null || attacker == player || attacker.isAlliedTo(player) || player.isAlliedTo(attacker)
                || (attacker instanceof TamableAnimal pet && pet.isTame())
                || (attacker instanceof Player other && !other.canHarmPlayer(player)))
            return false;
        if (!(direct instanceof LivingEntity) && !(direct instanceof Projectile))
            return false;
        Vec3 from = direct instanceof Projectile p && p.getDeltaMovement().lengthSqr() > 1e-8
                ? p.getDeltaMovement().scale(-1) : attacker.position().subtract(player.position());
        if (!YoumuSwordGeometry.facing(player.getLookAngle(), from))
            return false;
        s.parried = true;
        s.counterUntil = player.level().getGameTime() + 40;
        cooldown(player, white, 6);
        wear(player, white, paired ? InteractionHand.OFF_HAND : s.hand, 2);
        if (direct instanceof Projectile projectile)
            stopProjectile(projectile);
        if (!paired) {
            s.guard = ItemStack.EMPTY;
            player.stopUsingItem();
        }
        ServerLevel world = (ServerLevel) player.level();
        world.playSound(null, player.blockPosition(), SoundEvents.SHIELD_BLOCK.value(), SoundSource.PLAYERS, 1, 1.5f);
        world.sendParticles(ParticleTypes.SWEEP_ATTACK, player.getX(), player.getEyeY(), player.getZ(), 1, 0, 0, 0, 0);
        YoumuSwordFeedback.impact(world, player.getEyePosition().add(player.getLookAngle().scale(.8)));
        actionBar(player, Component.translatable(paired ? "item.reverie_dreams.youmu.counter_ready" : "item.reverie_dreams.youmu.parried"));
        return true;
    }

    public static InteractionResult purify(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide())
            return InteractionResult.SUCCESS;
        ItemStack stack = player.getItemInHand(hand);
        if (!checkReady(player, stack))
            return InteractionResult.FAIL;
        if (!(stack.getItem() instanceof Hakuroken))
            return InteractionResult.FAIL;
        List<Component> cleared = new ArrayList<>();
        if (player.removeEffect(RDStatusEffects.MENTAL_DISORDER.builtInHolder()))
            cleared.add(YoumuSwordFeedback.text("effect_mental"));
        if (player.removeEffect(MobEffects.BLINDNESS))
            cleared.add(Component.translatable("effect.minecraft.blindness"));
        if (player.removeEffect(MobEffects.NAUSEA))
            cleared.add(Component.translatable("effect.minecraft.nausea"));
        ServerLevel world = (ServerLevel) level;
        List<LivingEntity> spirits = world.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(16),
                target -> target != player && spirit(target) && target.isAlive() && player.distanceToSqr(target) <= 256
                        && !(target instanceof TamableAnimal pet && pet.isTame()));
        spirits.sort(Comparator.comparingDouble(player::distanceToSqr));
        for (LivingEntity target : spirits.subList(0, Math.min(32, spirits.size())))
            target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 240));
        cooldown(player, stack, 100);
        wear(player, stack, hand, 2);
        world.playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, .8f, 1.3f);
        world.sendParticles(ParticleTypes.ENCHANT, player.getX(), player.getY() + 1, player.getZ(), 12, .5, .6, .5, .1);
        YoumuSwordFeedback.purification(world, player);
        state(player).purificationTicks = 8;
        MutableComponent summary = Component.empty();
        for (Component effect : cleared) {
            if (!summary.getSiblings().isEmpty())
                summary.append("、");
            summary.append(effect);
        }
        actionBar(player, YoumuSwordFeedback.text("purified_detail", cleared.isEmpty() ? YoumuSwordFeedback.text("effect_none") : summary, Math.min(32, spirits.size())));
        return InteractionResult.SUCCESS_SERVER;
    }

    public static EventResult captureAttack(Player player, Level level, Entity target, InteractionHand hand, EntityHitResult hit) {
        if (level.isClientSide() || !(player.getMainHandItem().getItem() instanceof YoumuSwordUsing))
            return EventResult.pass();
        State s = state(player);
        if (!s.charge.isEmpty() || !s.guard.isEmpty() || s.attack != null)
            return EventResult.interruptFalse();
        s.meleeTick = level.getGameTime();
        s.meleeTarget = target.getUUID();
        s.meleeStrength = player.getAttackStrengthScale(.5f);
        return EventResult.pass();
    }

    public static void afterMeleeHit(ItemStack sword, LivingEntity target, LivingEntity attacker) {
        if (!(attacker.level() instanceof ServerLevel world))
            return;
        if (sword.getItem() instanceof Hakuroken) {
            if (ordinaryGhost(target) && !target.isAlive())
                YoumuSwordFeedback.ascend(world, target.position().add(0, .3, 0));
            return;
        }
        if (!(attacker instanceof Player player))
            return;
        State s = STATES.get(player);
        if (s == null || s.meleeTick != world.getGameTime() || !target.getUUID().equals(s.meleeTarget) || s.meleeStrength < .9f)
            return;
        s.meleeTick = -1;
        Set<UUID> hit = new HashSet<>();
        hit.add(target.getUUID());
        YoumuSwordFeedback.slash(world, player.position().add(0, 1, 0), player.getLookAngle(), false);
        slash(world, player, player.position().add(0, 1, 0), YoumuSwordGeometry.horizontal(player.getLookAngle()),
                3.5, .9, (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE) * .45f, hit);
    }

    public static boolean spirit(LivingEntity target) {
        return target.getType() == RDEntityTypes.GHOST.value() || target.getType().builtInRegistryHolder().is(EntityTypeTags.UNDEAD);
    }

    public static boolean ordinaryGhost(LivingEntity target) {
        return target.getType() == RDEntityTypes.GHOST.value() && !target.hasCustomName() && target.getMaxHealth() <= 40
                && !(target instanceof TamableAnimal pet && pet.isTame());
    }

    private static boolean canHarm(Player player, LivingEntity target) {
        return target != player && target.isAlive() && !target.isSpectator() && !target.isAlliedTo(player) && !player.isAlliedTo(target)
                && !(target instanceof TamableAnimal pet && pet.isTame())
                && (!(target instanceof Player other) || player.canHarmPlayer(other));
    }

    private static void slash(ServerLevel world, Player player, Vec3 start, Vec3 direction, double reach, double width, float damage, Set<UUID> hit) {
        Vec3 end = start.add(direction.scale(reach));
        AABB area = new AABB(start, end).inflate(width + 1);
        int count = 0;
        for (LivingEntity target : world.getEntitiesOfClass(LivingEntity.class, area, entity -> canHarm(player, entity))) {
            if (count >= 24)
                break;
            if (hit.contains(target.getUUID()) || !YoumuSwordGeometry.intersects(start, end, target.getBoundingBox(), width))
                continue;
            if (world.clip(new ClipContext(start, target.getBoundingBox().getCenter(), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player)).getType() != HitResult.Type.MISS)
                continue;
            hit.add(target.getUUID());
            count++;
            if (target.hurtServer(world, player.damageSources().playerAttack(player), damage + (spirit(target) ? 4 : 0)))
                YoumuSwordFeedback.impact(world, target.getBoundingBox().getCenter());
        }
    }

    public static void tick(Player player) {
        // Entity equality uses network ID: the integrated client's mirror equals the server player.
        // Do not even look up the server-owned map from a client-side callback.
        if (player.level().isClientSide())
            return;
        State s = STATES.get(player);
        if (s == null)
            return;
        long now = player.level().getGameTime();
        if (!live(player) || !s.dimension.equals(player.level().dimension()) || now < s.lastTick || now - s.lastTick > 4) {
            STATES.remove(player);
            return;
        }
        s.lastTick = now;
        if (s.purificationTicks > 0 && --s.purificationTicks % 2 == 0)
            YoumuSwordFeedback.purification((ServerLevel) player.level(), player, .5 + (8 - s.purificationTicks) * .125);
        if ((!s.charge.isEmpty() && (!player.isUsingItem() || player.getUseItem() != s.charge))
                || (!s.guard.isEmpty() && (!player.isUsingItem() || player.getUseItem() != s.guard))) {
            if (!s.charge.isEmpty()) {
                cooldown(player, s.charge, 12);
                actionBar(player, YoumuSwordFeedback.text("interrupted"));
            }
            s.charge = s.guard = s.partner = ItemStack.EMPTY;
        }
        if (s.attack != null) {
            Attack a = s.attack;
            if (player.getItemInHand(a.hand) != a.sword || a.sword.isEmpty() || player.isPassenger())
                s.attack = null;
            else
                advance((ServerLevel) player.level(), player, s, a);
        }
        s.cooldowns.entrySet().removeIf(entry -> now >= entry.getValue().until
                || !player.getCooldowns().isOnCooldown(entry.getValue().key));
        ItemStack held = player.getMainHandItem().getItem() instanceof YoumuSwordUsing ? player.getMainHandItem()
                : player.getOffhandItem().getItem() instanceof YoumuSwordUsing ? player.getOffhandItem() : ItemStack.EMPTY;
        CooldownWindow window = held.isEmpty() ? null : s.cooldowns.get(player.getCooldowns().getCooldownGroup(held));
        if (s.charge.isEmpty() && s.guard.isEmpty() && s.attack == null && window != null
                && now >= window.started + 20 && now % 5 == 0)
            actionBar(player, YoumuSwordFeedback.text("cooldown", held.getHoverName(), String.format(Locale.ROOT, "%.1f", (window.until - now) / 20.0)));
        if (s.charge.isEmpty() && s.guard.isEmpty() && s.attack == null && s.cooldowns.isEmpty())
            STATES.remove(player);
    }

    private static void advance(ServerLevel world, Player player, State s, Attack a) {
        if (a.dashTicks > 0) {
            Vec3 before = player.position(), next = before;
            for (int part = 0; part < 8; part++) {
                Vec3 candidate = next.add(a.direction.scale(a.step / 8));
                BlockPos pos = BlockPos.containing(candidate);
                if (!world.hasChunkAt(pos) || !world.getWorldBorder().isWithinBounds(pos)
                        || !world.noCollision(player, player.getBoundingBox().move(candidate.subtract(before))))
                    break;
                next = candidate;
            }
            if (next.distanceToSqr(before) > 1e-8)
                player.teleportTo(next.x, next.y, next.z);
            slash(world, player, before.add(0, 1, 0), a.direction, next.distanceTo(before) + 1.5, .9, a.damage, a.hit);
            clearBullets(world, player, before.add(0, 1, 0), next.add(0, 1, 0));
            YoumuSwordFeedback.slash(world, before.add(0, .8, 0), a.direction, a.full);
            if (next.distanceToSqr(before) < .01)
                a.dashTicks = 1;
            if (--a.dashTicks == 0 && a.full) {
                a.wavePosition = player.getEyePosition();
                a.waveTicks = 8;
            } else if (a.dashTicks == 0)
                s.attack = null;
        } else if (a.waveTicks > 0) {
            Vec3 start = a.wavePosition, end = start.add(a.waveDirection.scale(1.5));
            if (!world.hasChunkAt(BlockPos.containing(end))) {
                s.attack = null;
                return;
            }
            HitResult block = world.clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
            boolean stopped = block.getType() != HitResult.Type.MISS;
            if (stopped)
                end = block.getLocation();
            if (!world.hasChunkAt(BlockPos.containing(end))) {
                s.attack = null;
                return;
            }
            slash(world, player, start, a.waveDirection, start.distanceTo(end), .75, a.damage * .7f, a.hit);
            YoumuSwordFeedback.wave(world, end, a.waveDirection);
            clearBullets(world, player, start, end);
            if (stopped)
                YoumuSwordFeedback.impact(world, end);
            a.wavePosition = end;
            if (stopped || --a.waveTicks == 0)
                s.attack = null;
        }
    }

    private static void clearBullets(ServerLevel world, Player player, Vec3 from, Vec3 to) {
        int removed = 0;
        for (Projectile projectile : world.getEntitiesOfClass(Projectile.class, new AABB(from, to).inflate(1))) {
            if (removed >= 16)
                break;
            if (!(projectile instanceof BaseDanmakuEntity) || projectile.getOwner() == player)
                continue;
            Entity owner = projectile.getOwner();
            if (owner != null && (owner.isAlliedTo(player) || player.isAlliedTo(owner)))
                continue;
            if (!YoumuSwordGeometry.intersects(from, to, projectile.getBoundingBox(), .8))
                continue;
            if (world.clip(new ClipContext(from, projectile.position(), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player)).getType() != HitResult.Type.MISS)
                continue;
            YoumuSwordFeedback.impact(world, projectile.position());
            projectile.discard();
            removed++;
        }
    }

    private static void stopProjectile(Projectile projectile) {
        if (projectile instanceof BaseDanmakuEntity)
            projectile.discard();
        // Vanilla arrows/tridents retain their own failed-hit bounce and pickup/return behavior.
    }

    private static void actionBar(Player player, Component text) {
        if (player instanceof ServerPlayer serverPlayer)
            serverPlayer.connection.send(new ClientboundSetActionBarTextPacket(text));
    }

    public static <T> T projectileDamage(Projectile projectile, Supplier<T> damage) {
        Projectile previous = INCOMING.get();
        INCOMING.set(projectile);
        try {
            return damage.get();
        } finally {
            if (previous == null)
                INCOMING.remove();
            else
                INCOMING.set(previous);
        }
    }

    public static void clear(MinecraftServer server) {
        STATES.keySet().removeIf(player -> player.level().getServer() == server);
    }
}
