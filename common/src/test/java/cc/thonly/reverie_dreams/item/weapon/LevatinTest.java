package cc.thonly.reverie_dreams.item.weapon;

import cc.thonly.reverie_dreams.api.proxy.SafeClientAccess;
import cc.thonly.reverie_dreams.component.tooltip.InitTooltips;
import cc.thonly.reverie_dreams.registry.content.ItemPropDescriptionConsts;
import com.mojang.authlib.GameProfile;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.Bootstrap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityEquipment;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import sun.misc.Unsafe;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.Holder;

import static org.junit.jupiter.api.Assertions.*;

class LevatinTest {
    private static Unsafe unsafe;
    private static Levatin weapon;
    private static Holder.Reference<Item> powerBlock;
    private static Holder.Reference<Item> power;

    @BeforeAll static void bootstrap() throws Exception {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        var field = Unsafe.class.getDeclaredField("theUnsafe");
        field.setAccessible(true);
        unsafe = (Unsafe) field.get(null);
        weapon = (Levatin) unsafe.allocateInstance(Levatin.class);
        // Use a registered vanilla sword for real ItemStack persistence without loader init.
        if (!Items.IRON_SWORD.builtInRegistryHolder().areComponentsBound()) {
            Items.IRON_SWORD.builtInRegistryHolder().bindComponents(DataComponentMap.builder()
                    .set(DataComponents.MAX_STACK_SIZE, 1).set(DataComponents.MAX_DAMAGE, 1200)
                    .set(DataComponents.DAMAGE, 0).set(DataComponents.ITEM_MODEL, Levatin.STAFF_MODEL).build());
        }
        powerBlock = fuel("power_block");
        power = fuel("power");
    }

    private static Holder.Reference<Item> fuel(String path) throws Exception {
        var holder = Holder.Reference.createStandAlone(BuiltInRegistries.ITEM,
                ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("reverie_dreams", path)));
        var bindValue = Holder.Reference.class.getDeclaredMethod("bindValue", Object.class);
        bindValue.setAccessible(true);
        bindValue.invoke(holder, Items.IRON_BLOCK);
        holder.bindComponents(DataComponentMap.EMPTY);
        return holder;
    }

    @Test void oldStacksWithoutAFormOverrideDefaultToStaff() {
        ItemStack stack = new ItemStack(Items.IRON_SWORD);
        assertFalse(Levatin.isFlameForm(stack));
        stack.remove(DataComponents.ITEM_MODEL);
        assertFalse(Levatin.isFlameForm(stack));
    }

    @Test void sneakUseUnfoldsAndFoldsInEitherHand() throws Exception {
        for (InteractionHand hand : InteractionHand.values()) {
            var player = player(false);
            ItemStack stack = player.getItemInHand(hand);
            assertEquals(InteractionResult.SUCCESS_SERVER, weapon.use(player.world, player, hand));
            assertTrue(Levatin.isFlameForm(stack));
            assertFalse(Levatin.isFlameForm(player.getItemInHand(other(hand))));
            assertEquals(1, player.messages);
            assertEquals(1, player.inventory.getItem(0).getCount());
            assertEquals(1, player.world.fireSounds);
            expireCooldown(player);
            weapon.use(player.world, player, hand);
            assertEquals(Levatin.STAFF_MODEL, stack.get(DataComponents.ITEM_MODEL));
            assertEquals(2, player.messages);
            assertEquals(1, player.inventory.getItem(0).getCount());
            assertEquals(2, player.world.fireSounds);
        }
    }

    @Test void blockUseThenAirUseDoesNotToggleTwice() throws Exception {
        var player = player(false);
        assertEquals(InteractionResult.SUCCESS_SERVER, weapon.useOn(context(player, InteractionHand.MAIN_HAND)));
        weapon.use(player.world, player, InteractionHand.MAIN_HAND);
        assertTrue(Levatin.isFlameForm(player.main));
        assertEquals(1, player.messages);
        assertEquals(1, player.inventory.getItem(0).getCount());
        assertEquals(1, player.world.fireSounds);
        for (int tick = 0; tick < 9; tick++) player.cooldowns.tick();
        weapon.use(player.world, player, InteractionHand.MAIN_HAND);
        assertTrue(Levatin.isFlameForm(player.main));
        player.cooldowns.tick();
        weapon.useOn(context(player, InteractionHand.MAIN_HAND));
        assertFalse(Levatin.isFlameForm(player.main));
    }

    @Test void clientPredictsConsumptionWithoutChangingComponentsOrStartingCooldown() throws Exception {
        var player = player(true);
        var before = player.main.copy();
        assertEquals(InteractionResult.SUCCESS, weapon.use(player.world, player, InteractionHand.MAIN_HAND));
        assertEquals(InteractionResult.SUCCESS, weapon.useOn(context(player, InteractionHand.MAIN_HAND)));
        assertTrue(ItemStack.matches(before, player.main));
        assertFalse(player.cooldowns.isOnCooldown(player.main));
        assertEquals(0, player.messages);
        assertEquals(2, player.inventory.getItem(0).getCount());
        assertEquals(0, player.world.fireSounds);
    }

    @Test void ordinaryRightClickPassesThroughOnAirAndBlocks() throws Exception {
        for (boolean client : new boolean[]{false, true}) {
            var player = player(client);
            player.sneaking = false;
            assertEquals(InteractionResult.PASS, weapon.use(player.world, player, InteractionHand.MAIN_HAND));
            assertEquals(InteractionResult.PASS, weapon.useOn(context(player, InteractionHand.MAIN_HAND)));
            assertFalse(Levatin.isFlameForm(player.main));
            assertFalse(player.cooldowns.isOnCooldown(player.main));
        }
    }

    @Test void switchingPreservesDurabilityNameAndUnrelatedCustomData() throws Exception {
        var player = player(false);
        player.main.setDamageValue(417);
        player.main.set(DataComponents.CUSTOM_NAME, Component.literal("My Laevateinn"));
        CustomData.update(DataComponents.CUSTOM_DATA, player.main, tag -> tag.putString("other_mod", "keep"));
        var before = player.main.copy();
        weapon.use(player.world, player, InteractionHand.MAIN_HAND);
        assertEquals(before.getDamageValue(), player.main.getDamageValue());
        assertEquals(before.get(DataComponents.CUSTOM_NAME), player.main.get(DataComponents.CUSTOM_NAME));
        assertEquals("keep", player.main.get(DataComponents.CUSTOM_DATA).copyTag().getStringOr("other_mod", ""));
        expireCooldown(player);
        weapon.use(player.world, player, InteractionHand.MAIN_HAND);
        assertTrue(ItemStack.matches(before, player.main));
    }

    @Test void insufficientPowerDoesNotSpendFuelOrSwitch() throws Exception {
        var player = player(false);
        player.inventory.setItem(0, new ItemStack(Items.IRON_SWORD));
        player.inventory.setItem(1, new ItemStack(power, 5));
        player.inventory.setItem(2, new ItemStack(power, 3));
        weapon.use(player.world, player, InteractionHand.MAIN_HAND);
        assertFalse(Levatin.isFlameForm(player.main));
        assertEquals(1, player.inventory.getItem(0).getCount());
        assertEquals(5, player.inventory.getItem(1).getCount());
        assertEquals(3, player.inventory.getItem(2).getCount());
        assertEquals(0, player.world.fireSounds);
        assertFalse(player.cooldowns.isOnCooldown(player.main));
    }

    @Test void creativeActivationDoesNotRequireOrSpendFuel() throws Exception {
        var player = player(false);
        player.creative = true;
        player.inventory.setItem(0, ItemStack.EMPTY);
        assertEquals(InteractionResult.SUCCESS_SERVER, weapon.use(player.world, player, InteractionHand.MAIN_HAND));
        assertTrue(Levatin.isFlameForm(player.main));

        expireCooldown(player);
        weapon.use(player.world, player, InteractionHand.MAIN_HAND);
        assertFalse(Levatin.isFlameForm(player.main));
        expireCooldown(player);
        player.inventory.setItem(0, new ItemStack(powerBlock, 2));
        player.inventory.setItem(1, new ItemStack(power, 9));
        weapon.use(player.world, player, InteractionHand.MAIN_HAND);
        assertTrue(Levatin.isFlameForm(player.main));
        assertEquals(2, player.inventory.getItem(0).getCount());
        assertEquals(9, player.inventory.getItem(1).getCount());
    }

    @Test void ninePowerCanBePaidAcrossSeveralStacks() throws Exception {
        var player = player(false);
        player.inventory.setItem(0, new ItemStack(power, 4));
        player.inventory.setItem(1, new ItemStack(power, 7));
        weapon.use(player.world, player, InteractionHand.MAIN_HAND);
        assertTrue(Levatin.isFlameForm(player.main));
        assertTrue(player.inventory.getItem(0).isEmpty());
        assertEquals(2, player.inventory.getItem(1).getCount());
        assertEquals(1, player.world.fireSounds);
    }

    @Test void powerBlockTakesPriorityOverLoosePower() throws Exception {
        var player = player(false);
        player.inventory.setItem(0, new ItemStack(power, 12));
        player.inventory.setItem(8, new ItemStack(powerBlock));
        weapon.use(player.world, player, InteractionHand.MAIN_HAND);
        assertTrue(Levatin.isFlameForm(player.main));
        assertEquals(12, player.inventory.getItem(0).getCount());
        assertTrue(player.inventory.getItem(8).isEmpty());
    }

    @Test void expirationFollowsWorldTimeAndDoesNotPlayTwice() throws Exception {
        var player = player(false);
        player.world.tick = 300;
        weapon.use(player.world, player, InteractionHand.MAIN_HAND);
        ItemStack stowed = player.main.copy();
        player.world.tick += Levatin.FLAME_DURATION_TICKS - 1;
        weapon.inventoryTick(stowed, player.world, player, null);
        assertTrue(Levatin.isFlameForm(stowed));
        player.world.tick++;
        weapon.inventoryTick(stowed, player.world, player, null);
        assertFalse(Levatin.isFlameForm(stowed));
        assertFalse(stowed.has(DataComponents.CUSTOM_DATA));
        weapon.inventoryTick(stowed, player.world, player, null);
        assertEquals(2, player.world.fireSounds);
        assertEquals(1, player.inventory.getItem(0).getCount());
    }

    @Test void legacyFlameFormDoesNotKeepAFreeCharge() throws Exception {
        var player = player(false);
        player.main.set(DataComponents.ITEM_MODEL, Levatin.FLAME_MODEL);
        weapon.useOn(context(player, InteractionHand.MAIN_HAND));
        weapon.use(player.world, player, InteractionHand.MAIN_HAND);
        assertFalse(Levatin.isFlameForm(player.main));
        assertEquals(2, player.inventory.getItem(0).getCount());
        assertEquals(1, player.world.fireSounds);
    }

    @Test void realItemCodecAndCopiesKeepEachFormAcrossSaveLoadAndHandTransfer() throws Exception {
        var player = player(false);
        for (int form = 0; form < 2; form++) {
            var ops = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY).createSerializationContext(NbtOps.INSTANCE);
            var saved = ItemStack.CODEC.encodeStart(ops, player.main).getOrThrow();
            var loaded = ItemStack.CODEC.parse(ops, saved).getOrThrow();
            assertTrue(ItemStack.matches(player.main, loaded));
            assertEquals(form == 1, Levatin.isFlameForm(loaded.copy()));
            player.off = loaded.copy();
            assertEquals(form == 1, Levatin.isFlameForm(player.getItemInHand(InteractionHand.OFF_HAND)));
            if (form == 0) weapon.use(player.world, player, InteractionHand.MAIN_HAND);
        }
    }

    @Test void tooltipEventReportsCurrentFormWithoutRepeatingTheDetailHint() throws Exception {
        var player = player(false);
        player.main = tooltipStack();
        for (String form : List.of("staff", "flame")) {
            var lines = new ArrayList<Component>();
            InitTooltips.appendLevatinTooltip(player.main, null, TooltipDisplay.DEFAULT, player, lines::add, TooltipFlag.NORMAL);
            assertEquals(1, lines.size());
            assertEquals(Component.translatable("item.reverie_dreams.levatin.form",
                    Component.translatable("item.reverie_dreams.levatin.form." + form)).getString(), lines.getFirst().getString());
            player.main.set(DataComponents.ITEM_MODEL, Levatin.FLAME_MODEL);
        }
        var unrelated = new ArrayList<Component>();
        InitTooltips.appendLevatinTooltip(new ItemStack(Items.IRON_SWORD), null, TooltipDisplay.DEFAULT, player, unrelated::add, TooltipFlag.NORMAL);
        assertTrue(unrelated.isEmpty());
    }

    @Test void fixedInstructionsUseShiftDetailsAndDoNotWriteLore() {
        var previous = SafeClientAccess.ref.get();
        var stack = tooltipStack();
        boolean[] shift = {false};
        try {
            SafeClientAccess.ref.set(new SafeClientAccess.DefaultImpl() {
                @Override public boolean isShiftDown() { return shift[0]; }
            });
            InitTooltips.addItemForDetail(weapon, List.of(ItemPropDescriptionConsts.LEVATIN_DESCRIPTION_0,
                    ItemPropDescriptionConsts.LEVATIN_TOGGLE_HINT));
            var lines = new ArrayList<Component>();
            InitTooltips.appendItemDescriptionForDetail(stack, null, TooltipDisplay.DEFAULT, null, lines::add, TooltipFlag.NORMAL);
            assertTrue(lines.isEmpty());
            shift[0] = true;
            InitTooltips.appendItemDescriptionForDetail(stack, null, TooltipDisplay.DEFAULT, null, lines::add, TooltipFlag.NORMAL);
            assertEquals(2, lines.size());
            assertEquals(Component.translatable(ItemPropDescriptionConsts.LEVATIN_TOGGLE_HINT).getString(), lines.getLast().getString());
            assertFalse(stack.has(DataComponents.LORE));
        } finally {
            InitTooltips.ITEM_DESCRIPTION_FOR_DETAIL_RENDER_DATA.remove(weapon);
            SafeClientAccess.ref.set(previous);
        }
    }

    private static ItemStack tooltipStack() {
        // A private holder supplies real stack components without modifying the frozen registry.
        var holder = Holder.Reference.createIntrusive(BuiltInRegistries.ITEM, weapon);
        holder.bindComponents(DataComponentMap.builder().set(DataComponents.ITEM_MODEL, Levatin.STAFF_MODEL).build());
        return new ItemStack(holder);
    }

    private static InteractionHand other(InteractionHand hand) {
        return hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
    }
    private static void expireCooldown(FixturePlayer player) {
        for (int tick = 0; tick < 10; tick++) player.cooldowns.tick();
    }
    private static UseOnContext context(FixturePlayer player, InteractionHand hand) {
        return new UseOnContext(player, hand, new BlockHitResult(Vec3.ZERO, Direction.UP, BlockPos.ZERO, false));
    }
    private static FixturePlayer player(boolean client) throws Exception {
        var world = (FixtureLevel) unsafe.allocateInstance(FixtureLevel.class);
        world.client = client;
        var player = (FixturePlayer) unsafe.allocateInstance(FixturePlayer.class);
        player.world = world;
        player.sneaking = true;
        player.main = new ItemStack(Items.IRON_SWORD);
        player.off = new ItemStack(Items.IRON_SWORD);
        player.cooldowns = new ItemCooldowns();
        player.inventory = new Inventory(player, new EntityEquipment());
        player.inventory.setItem(0, new ItemStack(powerBlock, 2));
        return player;
    }
    // Real interaction methods, ItemStack codecs and cooldowns; world/network setup is a fixture.
    private static class FixtureLevel extends ServerLevel {
        boolean client;
        long tick;
        int fireSounds;
        private FixtureLevel() { super(null, null, null, null, null, null, false, 0, List.of(), false); }
        @Override public boolean isClientSide() { return client; }
        @Override public long getGameTime() { return tick; }
        @Override public void playSound(Entity source, BlockPos pos, SoundEvent sound, SoundSource category, float volume, float pitch) {
            assertSame(SoundEvents.FIRECHARGE_USE, sound);
            fireSounds++;
        }
    }
    private static class FixturePlayer extends Player {
        FixtureLevel world;
        ItemStack main, off;
        ItemCooldowns cooldowns;
        Inventory inventory;
        boolean sneaking;
        boolean creative;
        int messages;
        private FixturePlayer() { super(null, new GameProfile(UUID.randomUUID(), "levatin-test")); }
        @Override public GameType gameMode() { return creative ? GameType.CREATIVE : GameType.SURVIVAL; }
        @Override public Level level() { return world; }
        @Override public boolean isShiftKeyDown() { return sneaking; }
        @Override public ItemStack getItemInHand(InteractionHand hand) { return hand == InteractionHand.MAIN_HAND ? main : off; }
        @Override public ItemCooldowns getCooldowns() { return cooldowns; }
        @Override public Inventory getInventory() { return inventory; }
        @Override public void sendOverlayMessage(Component message) { messages++; }
    }
}
