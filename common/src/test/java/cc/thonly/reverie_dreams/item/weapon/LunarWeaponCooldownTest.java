package cc.thonly.reverie_dreams.item.weapon;

import net.minecraft.SharedConstants;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.UseCooldown;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import sun.misc.Unsafe;

import static org.junit.jupiter.api.Assertions.*;

class LunarWeaponCooldownTest {
    private static Unsafe unsafe;

    @BeforeAll static void bootstrap() throws Exception {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        var field = Unsafe.class.getDeclaredField("theUnsafe");
        field.setAccessible(true);
        unsafe = (Unsafe) field.get(null);
        // Registered vanilla carrier allows real stack persistence without loader startup.
        if (!Items.BOW.builtInRegistryHolder().areComponentsBound()) {
            Items.BOW.builtInRegistryHolder().bindComponents(DataComponentMap.builder()
                    .set(DataComponents.MAX_STACK_SIZE, 1).set(DataComponents.MAX_DAMAGE, 384)
                    .set(DataComponents.DAMAGE, 0).build());
        }
    }

    @Test void serverInventoryTickGivesIdenticalFreshGunsDifferentGroupsForEveryType() throws Exception {
        for (var type : LunarWeaponType.values()) {
            ItemStack a = gun(), b = gun();
            var cooldowns = new ItemCooldowns();
            assertEquals(cooldowns.getCooldownGroup(a), cooldowns.getCooldownGroup(b));
            var weapon = weapon(type);
            weapon.inventoryTick(a, null, null, null);
            weapon.inventoryTick(b, null, null, null);
            assertNotEquals(cooldowns.getCooldownGroup(a), cooldowns.getCooldownGroup(b));
            assertEquals(type.shotInterval(), a.get(DataComponents.USE_COOLDOWN).ticks());
        }
    }

    @Test void reloadingAAllowsBToFireAndFinishItsOwnShotCooldownWhileAStillWaits() {
        for (var type : LunarWeaponType.values()) {
            ItemStack a = prepared(type), b = prepared(type);
            var cooldowns = new ItemCooldowns();
            cooldowns.addCooldown(a, type.reloadTicks());
            assertTrue(cooldowns.isOnCooldown(a));
            assertFalse(cooldowns.isOnCooldown(b), type.name());
            assertEquals(0, cooldowns.getCooldownPercent(b, 0));

            cooldowns.addCooldown(b, type.shotInterval());
            for (int tick = 0; tick < type.shotInterval(); tick++) cooldowns.tick();
            assertFalse(cooldowns.isOnCooldown(b));
            assertTrue(cooldowns.isOnCooldown(a));
            for (int tick = type.shotInterval(); tick < type.reloadTicks(); tick++) cooldowns.tick();
            assertFalse(cooldowns.isOnCooldown(a));
        }
    }

    @Test void twoReloadsCountDownIndependentlyWithoutExtendingTheFirst() {
        ItemStack a = prepared(LunarWeaponType.RIFLE), b = prepared(LunarWeaponType.RIFLE);
        var cooldowns = new ItemCooldowns();
        cooldowns.addCooldown(a, 40);
        for (int tick = 0; tick < 10; tick++) cooldowns.tick();
        cooldowns.addCooldown(b, 40);
        for (int tick = 0; tick < 30; tick++) cooldowns.tick();
        assertFalse(cooldowns.isOnCooldown(a));
        assertTrue(cooldowns.isOnCooldown(b));
        assertEquals(.25f, cooldowns.getCooldownPercent(b, 0), .0001f);
        for (int tick = 0; tick < 10; tick++) cooldowns.tick();
        assertFalse(cooldowns.isOnCooldown(b));
    }

    @Test void repeatedInitializationAndStackTransfersDoNotClearAnActiveCooldown() throws Exception {
        ItemStack a = prepared(LunarWeaponType.SNIPER);
        var cooldowns = new ItemCooldowns();
        var group = cooldowns.getCooldownGroup(a);
        cooldowns.addCooldown(a, 60);
        for (int tick = 0; tick < 20; tick++) {
            weapon(LunarWeaponType.SNIPER).inventoryTick(a, null, null, null);
            cooldowns.tick();
        }
        assertEquals(group, cooldowns.getCooldownGroup(a));
        // Copies here represent the same serialized gun moved between inventory slots.
        ItemStack moved = a.copy();
        WeaponOfTheMoon.ensureCooldownGroup(moved, LunarWeaponType.SNIPER);
        assertEquals(group, cooldowns.getCooldownGroup(moved));
        assertEquals(2 / 3.0f, cooldowns.getCooldownPercent(moved, 0), .0001f);
    }

    @Test void vanillaSaveAndClientCooldownGroupKeepAAndBDistinct() {
        ItemStack a = prepared(LunarWeaponType.PISTOL), b = prepared(LunarWeaponType.PISTOL);
        var ops = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY)
                .createSerializationContext(NbtOps.INSTANCE);
        ItemStack clientA = ItemStack.CODEC.parse(ops, ItemStack.CODEC.encodeStart(ops, a).getOrThrow()).getOrThrow();
        ItemStack clientB = ItemStack.CODEC.parse(ops, ItemStack.CODEC.encodeStart(ops, b).getOrThrow()).getOrThrow();
        var server = new ItemCooldowns();
        var client = new ItemCooldowns();
        var group = server.getCooldownGroup(a);
        // The native cooldown packet conveys this group and duration to the client.
        client.addCooldown(group, LunarWeaponType.PISTOL.reloadTicks());
        assertEquals(group, client.getCooldownGroup(clientA));
        assertTrue(client.isOnCooldown(clientA));
        assertFalse(client.isOnCooldown(clientB));
        assertTrue(ItemStack.matches(a, clientA));
        assertTrue(ItemStack.matches(b, clientB));
    }

    @Test void legacyStacksWithNoGroupAreMigratedOnceAndEmptyStacksAreIgnored() {
        ItemStack a = gun();
        a.set(DataComponents.USE_COOLDOWN, new UseCooldown(.5f));
        WeaponOfTheMoon.ensureCooldownGroup(a, LunarWeaponType.SMG);
        var component = a.get(DataComponents.USE_COOLDOWN);
        assertTrue(component.cooldownGroup().isPresent());
        WeaponOfTheMoon.ensureCooldownGroup(a, LunarWeaponType.SMG);
        assertSame(component, a.get(DataComponents.USE_COOLDOWN));
        WeaponOfTheMoon.ensureCooldownGroup(ItemStack.EMPTY, LunarWeaponType.SMG);
        assertFalse(ItemStack.EMPTY.has(DataComponents.USE_COOLDOWN));
    }

    private static ItemStack gun() { return new ItemStack(Items.BOW); }

    private static ItemStack prepared(LunarWeaponType type) {
        ItemStack stack = gun();
        WeaponOfTheMoon.ensureCooldownGroup(stack, type);
        return stack;
    }

    private static FixtureWeapon weapon(LunarWeaponType type) throws Exception {
        var weapon = (FixtureWeapon) unsafe.allocateInstance(FixtureWeapon.class);
        weapon.type = type;
        return weapon;
    }

    private static class FixtureWeapon extends WeaponOfTheMoon {
        LunarWeaponType type;
        private FixtureWeapon() { super(null); }
        @Override public LunarWeaponType getWeaponType() { return type; }
    }
}
