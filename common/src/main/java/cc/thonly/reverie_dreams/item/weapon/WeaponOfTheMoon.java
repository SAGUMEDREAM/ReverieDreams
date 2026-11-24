package cc.thonly.reverie_dreams.item.weapon;

import cc.thonly.reverie_dreams.component.DanmakuProperties;
import cc.thonly.reverie_dreams.entity.misc.DanmakuEntity;
import cc.thonly.reverie_dreams.networking.payload.LunarShotPosePacket;
import cc.thonly.reverie_dreams.networking.payload.LunarReloadPosePacket;
import cc.thonly.reverie_dreams.registry.content.RDDamageTypes;
import cc.thonly.reverie_dreams.registry.content.advancements.RDCriteriaTriggers;
import cc.thonly.reverie_dreams.registry.content.component.RDDataComponentTypes;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.protocol.game.ClientboundPlayerRotationPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Unit;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.UseCooldown;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

@SuppressWarnings("NullableProblems")
public class WeaponOfTheMoon extends Item {
    private final LunarWeaponType weaponType;
    /**
     * Palette colour of the ammo bar.
     */
    private static final int AMMO_BAR_COLOR = 0xA9C9FF;
    public static final int AIM_HOLD_TICKS = 6;
    private static final int AIM_EFFECT_TICKS = 4;
    private static final int MAX_RECOIL_REDUCTION_LEVEL = 32;
    private static final float MIN_RECOIL_MULTIPLIER = .02f;
    private static final float NAUSEA_RECOIL_MULTIPLIER = 1.3f;

    public WeaponOfTheMoon(Properties properties) {
        this(properties, LunarWeaponType.RIFLE);
    }

    public WeaponOfTheMoon(Properties properties, LunarWeaponType weaponType) {
        super(properties);
        this.weaponType = weaponType;
    }

    public LunarWeaponType getWeaponType() {
        return weaponType;
    }

    public int capacity() {
        return getWeaponType().capacity();
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, @Nullable EquipmentSlot slot) {
        super.inventoryTick(stack, level, entity, slot);
        ensureCooldownGroup(stack, getWeaponType());
    }

    /**
     * Stored on the individual gun, so inventory movement keeps its native cooldown.
     */
    static void ensureCooldownGroup(ItemStack stack, LunarWeaponType type) {
        if (stack.isEmpty())
            return;
        UseCooldown cooldown = stack.get(DataComponents.USE_COOLDOWN);
        if (cooldown != null && cooldown.cooldownGroup().isPresent())
            return;
        Identifier group = Identifier.fromNamespaceAndPath("reverie_dreams", "lunar_weapon/" + UUID.randomUUID());
        stack.set(DataComponents.USE_COOLDOWN, new UseCooldown(type.shotInterval() / 20.0f, Optional.of(group)));
    }

    public int getRounds(ItemStack stack) {
        return Mth.clamp(stack.getOrDefault(RDDataComponentTypes.MAGAZINE_ROUNDS.get(), 0), 0, capacity());
    }

    public void setRounds(ItemStack stack, int rounds) {
        stack.set(RDDataComponentTypes.MAGAZINE_ROUNDS.get(), Mth.clamp(rounds, 0, capacity()));
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return isTriggerHeld(stack) ? ItemUseAnimation.NONE : ItemUseAnimation.BOW;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000;
    }

    public static boolean isAiming(Player player) {
        ItemStack stack = player.getUseItem();
        return player.isAlive() && !player.isSpectator() && player.isUsingItem() && player.isShiftKeyDown()
                && stack.getItem() instanceof WeaponOfTheMoon && !isTriggerHeld(stack)
                && player.getTicksUsingItem() >= AIM_HOLD_TICKS;
    }

    public static void handleAimInput(ServerPlayer player, boolean fire) {
        if (!fire) {
            if (player.getUseItem().getItem() instanceof WeaponOfTheMoon)
                player.stopUsingItem();
            return;
        }
        if (!isAiming(player))
            return;
        InteractionHand hand = player.getUsedItemHand();
        ItemStack stack = player.getUseItem();
        if (stack != player.getItemInHand(hand) || !(stack.getItem() instanceof WeaponOfTheMoon gun))
            return;

        if (gun.fireShot(player, player.level(), hand, 0, true))
            gun.awardShot(player, stack);
    }

    @Override
    public void onUseTick(Level level, LivingEntity user, ItemStack stack, int remainingUseTicks) {
        if (!(user instanceof Player player)) {
            return;
        }
        if (!player.isAlive() || player.isSpectator()) {
            player.stopUsingItem();
            return;
        }
        if (isTriggerHeld(stack)) {
            if (player.isShiftKeyDown()) {
                player.stopUsingItem();
            } else if (getRounds(stack) <= 0) {
                player.stopUsingItem();
            } else if (level instanceof ServerLevel world && !level.isClientSide() && getWeaponType().automatic()) {
                fireShot(player, world, player.getUsedItemHand(), 0, false);
            }
            return;
        }
        if (!player.isShiftKeyDown()) {
            player.stopUsingItem();
            return;
        }
        if (level.isClientSide() || this.getUseDuration(stack, user) - remainingUseTicks < AIM_HOLD_TICKS) {
            return;
        }

        MobEffectInstance current = player.getEffect(MobEffects.SLOWNESS);
        int amplifier = getWeaponType().aimSlowness();
        if (current != null && current.getAmplifier() >= amplifier
                && (current.isInfiniteDuration() || current.getDuration() >= AIM_EFFECT_TICKS)) {
            return;
        }
        // Let this expire naturally: removing SLOWNESS would also erase potion effects.
        player.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, AIM_EFFECT_TICKS,
                amplifier, false, false, false));
    }

    @Override
    public boolean releaseUsing(ItemStack stack, Level level, LivingEntity user, int remainingUseTicks) {
        if (isTriggerHeld(stack)) {
            stack.remove(RDDataComponentTypes.LUNAR_TRIGGER_HELD.get());
            return false;
        }
        if (level.isClientSide() || !(level instanceof ServerLevel world)
                || !(user instanceof Player player) || !player.isShiftKeyDown()) {
            return false;
        }
        int heldTicks = this.getUseDuration(stack, user) - remainingUseTicks;
        if (heldTicks < 0) {
            return false;
        }
        if (heldTicks >= AIM_HOLD_TICKS) {
            if (stack.isEmpty() || !stack.is(this))
                return false;
            boolean fired = fireShot(player, world, player.getUsedItemHand(), 0, true);
            if (fired)
                awardShot(player, stack);
            return fired;
        }
        if (this.reload(player, player.getUsedItemHand(), stack)) {
            player.awardStat(Stats.ITEM_USED.get(this));
            return true;
        }
        if (this.getRounds(stack) <= 0) {
            this.playEmptyClick(world, player);
        }
        return false;
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return this.getRounds(stack) < capacity();
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.round(13.0F * this.getRounds(stack) / capacity());
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return AMMO_BAR_COLOR;
    }

    public void tryShoot(LivingEntity owner, ServerLevel world, @Nullable InteractionHand hand) {
        this.tryShoot(owner, world, hand, 0);
    }

    public void tryShoot(LivingEntity owner, ServerLevel world, @Nullable InteractionHand hand, float d) {
        fireShot(owner, world, hand, d, false);
    }

    protected boolean fireShot(LivingEntity owner, ServerLevel world, @Nullable InteractionHand hand, float spreadOverride, boolean aimed) {
        if (!owner.isAlive() || owner.isSpectator())
            return false;
        if (owner instanceof Player player && hand != null) {
            ItemStack weapon = player.getItemInHand(hand);
            if (!weapon.is(this))
                return false;
            ensureCooldownGroup(weapon, getWeaponType());
            if (player.getCooldowns().isOnCooldown(weapon))
                return false;
            int remainingRounds = roundsAfterShot(this.getRounds(weapon));
            if (remainingRounds < 0) {
                this.playEmptyClick(world, owner);
                return false;
            }
            this.setRounds(weapon, remainingRounds);
            player.getCooldowns().addCooldown(weapon, getWeaponType().shotInterval());
        }

        LunarWeaponType type = getWeaponType();
        float damage = type.damageAtLevel(owner instanceof Player player ? player.experienceLevel : 0);
        DanmakuProperties properties = new DanmakuProperties(DanmakuProperties.DEFAULT_TEMPLATE_ID, 1,
                damage, RDDamageTypes.DANMAKU_GENERIC, 1.0f, type.speed(), 0,
                false, false, DanmakuProperties.CURRENT_SPEED_VERSION, type.bulletHitboxScale());
        float spread = spreadOverride > 0 ? spreadOverride : type.spread(aimed);
        for (int pellet = 0; pellet < type.pellets(); pellet++) {
            DanmakuEntity projectile = new DanmakuEntity(owner, owner.getX(), owner.getEyeY(), owner.getZ(), world,
                    LunarBulletVisuals.forWeapon(type));
            projectile.setDanmakuProperties(properties);
            projectile.setDuration(type.lifetime());
            // Firearm speeds are trusted weapon settings, separate from the spellcard component cap.
            projectile.shootFromRotation(owner, owner.getXRot(), owner.getYRot(), 0, type.speed(), spread);
            world.addFreshEntity(projectile);
        }
        LunarWeaponSounds.play(world, owner, type);

        RandomSource random = world.getRandom();
        float recoilMultiplier = aimed ? .5f : 1.0f;
        if (owner instanceof Player player) {
            // The floor also applies after aiming, so recoil never disappears at high levels.
            float levelProgress = Mth.clamp(player.experienceLevel, 0, MAX_RECOIL_REDUCTION_LEVEL)
                    / (float) MAX_RECOIL_REDUCTION_LEVEL;
            float experienceMultiplier = Mth.lerp(levelProgress, 1.0f, MIN_RECOIL_MULTIPLIER);
            recoilMultiplier = Math.max(MIN_RECOIL_MULTIPLIER, recoilMultiplier * experienceMultiplier);
            if (player.hasEffect(MobEffects.NAUSEA))
                recoilMultiplier *= NAUSEA_RECOIL_MULTIPLIER;
        }
        float recoilPitch = type.recoil() * recoilMultiplier;
        float recoilYaw = (random.nextFloat() - 0.5f) * recoilPitch * .5f;

        owner.turn(
                recoilYaw,
                -recoilPitch
        );
        if (owner instanceof ServerPlayer player && player.connection != null) {
            player.connection.send(new ClientboundPlayerRotationPacket(owner.getYRot(), false, owner.getXRot(), false));
            if (hand != null)
                LunarShotPosePacket.broadcast(player, hand, type);
        }
        return true;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack weapon = player.getItemInHand(hand);
        if (!level.isClientSide())
            ensureCooldownGroup(weapon, getWeaponType());
        if (player.isSpectator() || player.getCooldowns().isOnCooldown(weapon))
            return InteractionResult.FAIL;
        // Delay the magazine swap until release so holding the same control can aim.
        if (player.isShiftKeyDown()) {
            if (!weapon.isEmpty())
                weapon.remove(RDDataComponentTypes.LUNAR_TRIGGER_HELD.get());
            player.startUsingItem(hand);
            return InteractionResult.CONSUME;
        }
        boolean empty = this.getRounds(weapon) <= 0;

        if (level.isClientSide()) {
            weapon.set(RDDataComponentTypes.LUNAR_TRIGGER_HELD.get(), Unit.INSTANCE);
            player.startUsingItem(hand);
            return InteractionResult.CONSUME;
        }
        if (!(level instanceof ServerLevel world))
            return InteractionResult.FAIL;

        // A dry weapon reloads itself before it gives up and clicks.
        if (empty) {
            if (this.reload(player, hand, weapon)) {
                player.awardStat(Stats.ITEM_USED.get(this));
                weapon.set(RDDataComponentTypes.LUNAR_TRIGGER_HELD.get(), Unit.INSTANCE);
                player.startUsingItem(hand);
                return InteractionResult.CONSUME;
            }
            this.playEmptyClick(world, player);
            return InteractionResult.FAIL;
        }

        if (!fireShot(player, world, hand, 0, false))
            return InteractionResult.FAIL;
        weapon.set(RDDataComponentTypes.LUNAR_TRIGGER_HELD.get(), Unit.INSTANCE);
        player.startUsingItem(hand);
        awardShot(player, weapon);
        return InteractionResult.CONSUME;
    }

    private void awardShot(Player player, ItemStack weapon) {
        player.awardStat(Stats.ITEM_USED.get(this));
        if (player instanceof ServerPlayer serverPlayer)
            RDCriteriaTriggers.USE_ITEM.value().trigger(serverPlayer, weapon);
    }

    private static boolean isTriggerHeld(ItemStack stack) {
        return !stack.isEmpty() && stack.has(RDDataComponentTypes.LUNAR_TRIGGER_HELD.get());
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity interactionTarget, InteractionHand usedHand) {
        return this.use(player.level(), player, usedHand);
    }

    /**
     * Moves the rounds of one magazine from the off hand (or the inventory)
     * into the weapon. Creative keeps the source magazine; its loaded gun still spends rounds.
     */
    private boolean reload(Player player, InteractionHand hand, ItemStack weapon) {
        ensureCooldownGroup(weapon, getWeaponType());
        if (player.getCooldowns().isOnCooldown(weapon))
            return false;
        int previousRounds = this.getRounds(weapon);
        if (previousRounds >= capacity()) {
            return false;
        }

        ItemStack magazine = findMagazine(player);
        if (magazine.isEmpty()) {
            return false;
        }

        int rounds = MagazineItem.getRounds(magazine);
        if (rounds <= 0) {
            return false;
        }

        MagazineSwap swap = planMagazineSwap(previousRounds, rounds, capacity(), player.isCreative());
        if (swap == null)
            return false;
        this.setRounds(weapon, swap.loadedRounds());
        if (swap.consumeSource()) {
            ItemStack returned = swap.returnedRounds() > 0 ? new ItemStack(magazine.getItem()) : ItemStack.EMPTY;
            if (!returned.isEmpty())
                MagazineItem.setRounds(returned, swap.returnedRounds());
            magazine.shrink(1);
            if (!returned.isEmpty() && !player.getInventory().add(returned))
                player.drop(returned, false);
        }
        player.getCooldowns().addCooldown(weapon, getWeaponType().reloadTicks());
        if (player instanceof ServerPlayer serverPlayer)
            LunarReloadPosePacket.send(serverPlayer, hand, getWeaponType().number());
        player.level().playSound(null,
                player.getX(), player.getY(), player.getZ(),
                SoundEvents.LODESTONE_COMPASS_LOCK, SoundSource.PLAYERS,
                1.0F, 1.4F
        );
        return true;
    }

    /**
     * No game-mode exemption: creative guns must also spend their loaded rounds.
     */
    static int roundsAfterShot(int rounds) {
        return rounds > 0 ? rounds - 1 : -1;
    }

    record MagazineSwap(int loadedRounds, int returnedRounds, boolean consumeSource) {
    }

    static MagazineSwap planMagazineSwap(int current, int incoming, int capacity, boolean creative) {
        if (capacity <= 0 || current >= capacity || incoming <= 0)
            return null;
        return new MagazineSwap(Math.min(incoming, capacity), creative ? 0 : Math.max(0, current), !creative);
    }

    private ItemStack findMagazine(Player player) {
        ItemStack offhand = player.getOffhandItem();
        if (isLoadedMagazine(offhand)) {
            return offhand;
        }

        Inventory inventory = player.getInventory();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (isLoadedMagazine(stack)) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    private boolean isLoadedMagazine(ItemStack stack) {
        return stack.getItem() instanceof MagazineItem magazine && magazine.getWeaponType() == getWeaponType()
                && MagazineItem.getRounds(stack) > 0;
    }

    private void playEmptyClick(Level level, LivingEntity owner) {
        level.playSound(null,
                owner.getX(), owner.getY(), owner.getZ(),
                SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.PLAYERS,
                0.5F, 1.6F
        );
    }
}
