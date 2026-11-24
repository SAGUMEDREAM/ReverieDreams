package cc.thonly.reverie_dreams.item.weapon;

import com.mojang.authlib.GameProfile;
import net.minecraft.SharedConstants;
import net.minecraft.core.Holder;
import net.minecraft.server.Bootstrap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import sun.misc.Unsafe;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class WeaponOfTheMoonAimingTest {
    private static Unsafe unsafe;

    @BeforeAll static void bootstrap() throws Exception {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        var field = Unsafe.class.getDeclaredField("theUnsafe");
        field.setAccessible(true);
        unsafe = (Unsafe) field.get(null);
    }

    @Test void sneakUseStartsHoldingOnBothSidesWithoutReloadingOrShooting() throws Exception {
        for (boolean client : new boolean[]{false, true}) {
            var player = player(client);
            var weapon = weapon();
            assertEquals(InteractionResult.CONSUME, weapon.use(player.level(), player, InteractionHand.OFF_HAND));
            assertTrue(player.using);
            assertEquals(InteractionHand.OFF_HAND, player.hand);
            assertEquals(0, weapon.roundReads);
            assertFalse(player.hasEffect(MobEffects.SLOWNESS));
        }
    }

    @Test void aGunOnCooldownCannotStartAimingOnEitherSide() throws Exception {
        for (boolean client : new boolean[]{false, true}) {
            var player = player(client);
            player.using = false;
            player.cooldowns.addCooldown(ItemStack.EMPTY, 40);
            assertEquals(InteractionResult.FAIL, weapon().use(player.level(), player, InteractionHand.MAIN_HAND));
            assertFalse(player.using);
        }
    }

    @Test void aShortTapDuringCooldownCannotReloadAgain() throws Exception {
        var player = player(false);
        var weapon = weapon();
        player.cooldowns.addCooldown(ItemStack.EMPTY, 40);
        assertFalse(weapon.releaseUsing(ItemStack.EMPTY, player.level(), player, 72000 - 5));
        // The post-failure empty check reads once; reload must not read or swap rounds.
        assertEquals(1, weapon.roundReads);
    }

    @Test void aShortTapReachesTheExistingReloadPathWithoutApplyingSlowness() throws Exception {
        var player = player(false);
        var weapon = weapon();
        tick(weapon, player, 5);
        assertFalse(player.hasEffect(MobEffects.SLOWNESS));
        // A full magazine makes the real reload path a no-op; it must still be consulted.
        assertFalse(weapon.releaseUsing(ItemStack.EMPTY, player.level(), player, 72000 - 5));
        assertTrue(weapon.roundReads > 0);
    }

    @Test void holdingPastTheThresholdAimsWithoutReloadingOnRelease() throws Exception {
        var player = player(false);
        var weapon = weapon();
        tick(weapon, player, 6);
        var effect = player.getEffect(MobEffects.SLOWNESS);
        assertNotNull(effect);
        assertEquals(3, effect.getAmplifier());
        assertFalse(effect.isVisible());
        assertFalse(effect.showIcon());
        assertFalse(weapon.releaseUsing(ItemStack.EMPTY, player.level(), player, 72000 - 6));
        assertEquals(0, weapon.roundReads);
        player.tickEffect(4);
        assertFalse(player.hasEffect(MobEffects.SLOWNESS));
    }

    @Test void aLongHoldRefreshesTheShortEffectWithoutFlickeringOrLeavingItBehind() throws Exception {
        var player = player(false);
        var weapon = weapon();
        for (int elapsed = 6; elapsed < 80; elapsed++) {
            tick(weapon, player, elapsed);
            player.tickEffect(1);
            assertTrue(player.hasEffect(MobEffects.SLOWNESS));
            assertEquals(3, player.getEffect(MobEffects.SLOWNESS).getAmplifier());
        }
        player.stopUsingItem();
        player.tickEffect(4);
        assertFalse(player.hasEffect(MobEffects.SLOWNESS));
    }

    @Test void releasingSneakStopsUsingAndLetsTheAimEffectExpire() throws Exception {
        var player = player(false);
        var weapon = weapon();
        tick(weapon, player, 6);
        player.sneaking = false;
        tick(weapon, player, 7);
        assertFalse(player.using);
        player.tickEffect(4);
        assertFalse(player.hasEffect(MobEffects.SLOWNESS));
        assertFalse(weapon.releaseUsing(ItemStack.EMPTY, player.level(), player, 72000 - 2));
        assertEquals(0, weapon.roundReads);
    }

    @Test void clientPredictionNeverAddsAnAuthoritativePotionEffect() throws Exception {
        var player = player(true);
        var weapon = weapon();
        tick(weapon, player, 20);
        assertFalse(player.hasEffect(MobEffects.SLOWNESS));
        assertFalse(weapon.releaseUsing(ItemStack.EMPTY, player.level(), player, 72000 - 2));
        assertEquals(0, weapon.roundReads);
    }

    @Test void deathAndSpectatorModeStopAiming() throws Exception {
        for (boolean spectator : new boolean[]{false, true}) {
            var player = player(false);
            player.spectator = spectator;
            player.alive = spectator;
            tick(weapon(), player, 20);
            assertFalse(player.using);
            assertFalse(player.hasEffect(MobEffects.SLOWNESS));
        }
    }

    @Test void existingSameOrStrongerSlownessIsPreservedIncludingItsDisplayFlags() throws Exception {
        for (int amplifier : new int[]{3, 5}) {
            var player = player(false);
            var original = new MobEffectInstance(MobEffects.SLOWNESS, 200, amplifier, false, true, true);
            player.addEffect(original);
            tick(weapon(), player, 20);
            assertSame(original, player.getEffect(MobEffects.SLOWNESS));
            assertEquals(200, original.getDuration());
            assertEquals(amplifier, original.getAmplifier());
            assertTrue(original.isVisible());
            assertTrue(original.showIcon());
        }
    }

    @Test void weakerPotionReturnsWithItsRemainingTimeAfterTheAimEffectExpires() throws Exception {
        var player = player(false);
        player.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 200, 0, false, true, true));
        tick(weapon(), player, 20);
        assertEquals(3, player.getEffect(MobEffects.SLOWNESS).getAmplifier());
        player.tickEffect(4);
        var restored = player.getEffect(MobEffects.SLOWNESS);
        assertNotNull(restored);
        assertEquals(0, restored.getAmplifier());
        assertEquals(196, restored.getDuration());
        assertTrue(restored.isVisible());
        assertTrue(restored.showIcon());
    }

    @Test void anInfiniteWeakerPotionAlsoReturnsAfterAiming() throws Exception {
        var player = player(false);
        player.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, -1, 0));
        tick(weapon(), player, 20);
        player.tickEffect(4);
        var restored = player.getEffect(MobEffects.SLOWNESS);
        assertNotNull(restored);
        assertTrue(restored.isInfiniteDuration());
        assertEquals(0, restored.getAmplifier());
    }

    @Test void aimingUsesVanillaSlownessMovementModifiersThatDriveTheFovChange() throws Exception {
        var player = player(false);
        tick(weapon(), player, 20);
        var attributes = new AttributeMap(Player.createAttributes().build());
        double baseSpeed = attributes.getValue(Attributes.MOVEMENT_SPEED);
        var effect = player.getEffect(MobEffects.SLOWNESS);
        assertNotNull(effect);
        effect.getEffect().value().addAttributeModifiers(attributes, effect.getAmplifier());
        assertEquals(baseSpeed * 0.4, attributes.getValue(Attributes.MOVEMENT_SPEED), 0.000001);
    }

    private static void tick(WeaponOfTheMoon weapon, FixturePlayer player, int elapsed) {
        weapon.onUseTick(player.level(), player, ItemStack.EMPTY, 72000 - elapsed);
    }

    private static FixtureWeapon weapon() throws Exception {
        return (FixtureWeapon) unsafe.allocateInstance(FixtureWeapon.class);
    }

    private static FixturePlayer player(boolean client) throws Exception {
        var world = (FixtureLevel) unsafe.allocateInstance(FixtureLevel.class);
        world.client = client;
        var player = (FixturePlayer) unsafe.allocateInstance(FixturePlayer.class);
        player.world = world;
        player.sneaking = true;
        player.alive = true;
        player.using = true;
        player.cooldowns = new ItemCooldowns();
        var effects = LivingEntity.class.getDeclaredField("activeEffects");
        effects.setAccessible(true);
        effects.set(player, new HashMap<Holder<MobEffect>, MobEffectInstance>());
        return player;
    }

    private static class FixtureWeapon extends WeaponOfTheMoon {
        int roundReads;
        private FixtureWeapon() { super(null); }
        @Override public LunarWeaponType getWeaponType() { return LunarWeaponType.RIFLE; }
        @Override public int getRounds(ItemStack stack) { roundReads++; return MagazineItem.CAPACITY; }
    }

    private static class FixtureLevel extends ServerLevel {
        boolean client;
        private FixtureLevel() { super(null, null, null, null, null, null, false, 0, List.of(), false); }
        @Override public boolean isClientSide() { return client; }
    }

    // Real LivingEntity.addEffect and MobEffectInstance duration/hidden-effect rules;
    // only unrelated player networking, world setup and effect attribute callbacks are fixtures.
    private static class FixturePlayer extends Player {
        @Override public ItemStack getItemInHand(InteractionHand hand) { return ItemStack.EMPTY; }
        FixtureLevel world;
        boolean sneaking, alive, using, spectator;
        InteractionHand hand;
        ItemCooldowns cooldowns;
        private FixturePlayer() { super(null, new GameProfile(UUID.randomUUID(), "moon-aim-test")); }
        @Override public GameType gameMode() { return GameType.SURVIVAL; }
        @Override public Level level() { return world; }
        @Override public ItemCooldowns getCooldowns() { return cooldowns; }
        @Override public boolean isShiftKeyDown() { return sneaking; }
        @Override public boolean isAlive() { return alive; }
        @Override public boolean isSpectator() { return spectator; }
        @Override public void startUsingItem(InteractionHand hand) { this.hand = hand; using = true; }
        @Override public void stopUsingItem() { using = false; }
        @Override public boolean canBeAffected(MobEffectInstance effect) { return true; }
        @Override protected void onEffectAdded(MobEffectInstance effect, Entity source) { }
        @Override protected void onEffectUpdated(MobEffectInstance effect, boolean refresh, Entity source) { }

        void tickEffect(int ticks) {
            for (int i = 0; i < ticks; i++) {
                var iterator = getActiveEffects().iterator();
                while (iterator.hasNext()) {
                    if (!iterator.next().tickServer(world, this, () -> { })) iterator.remove();
                }
            }
        }
    }
}
