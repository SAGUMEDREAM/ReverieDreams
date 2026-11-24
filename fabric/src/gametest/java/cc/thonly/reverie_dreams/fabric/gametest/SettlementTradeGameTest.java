package cc.thonly.reverie_dreams.fabric.gametest;

import cc.thonly.reverie_dreams.entity.HumanEntity;
import cc.thonly.reverie_dreams.registry.content.entity.RDEntityTypes;
import cc.thonly.reverie_dreams.world.settlement.SettlementPilotData;
import cc.thonly.reverie_dreams.world.settlement.SettlementTrade;
import eu.pb4.sgui.impl.virtual.merchant.VirtualMerchantScreenHandler;
import net.fabricmc.fabric.api.gametest.v1.CustomTestMethodInvoker;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.network.HashedStack;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class SettlementTradeGameTest implements CustomTestMethodInvoker {
    private static final Identifier POST = id("trade_post");
    private static final Identifier RESIDENT = id("trade_resident");
    private static final int PAYMENT_COUNT = 2;
    private static final short OUTPUT_SLOT = 2;

    @Override
    public void invokeTestMethod(GameTestHelper helper, Method method) throws ReflectiveOperationException {
        method.invoke(this, helper);
    }

    @GameTest(setupTicks = 1, maxTicks = 200)
    public void realResultClickConsumesPaymentAndPersistsOneUse(GameTestHelper helper) {
        TradeFixture fixture = prepareTrade(helper, new BlockPos(4, 1, 4));
        ServerPlayer player = mockPlayerAt(helper, new BlockPos(3, 1, 4));
        ItemStack payment = payment();
        ItemStack result = result();

        helper.assertTrue(SettlementPilotData.registerTrade(
                fixture.resident(), POST, payment, result, 1), "the enrolled human receives one trade");
        SettlementTrade registered = currentTrade(helper, fixture);
        helper.assertTrue(registered.stock() == 1 && registered.uses() == 0,
                "the registered offer starts with one use of stock available");
        helper.assertTrue(ItemStack.matches(registered.payment(), payment)
                        && ItemStack.matches(registered.result(), result),
                "the registered offer stores defensive payment and result copies");
        helper.assertFalse(SettlementPilotData.registerTrade(
                fixture.resident(), POST, payment, result, 1), "a resident cannot receive a second active trade");

        helper.assertTrue(fixture.resident().mobInteract(player, InteractionHand.MAIN_HAND).consumesAction(),
                "an empty-handed server player opens the real settlement merchant menu");
        VirtualMerchantScreenHandler menu = merchantMenu(player);

        menu.getSlot(0).setByPlayer(payment.copyWithCount(1));
        helper.assertTrue(menu.getSlot(2).getItem().isEmpty(),
                "partial payment does not create a result");
        clickOutputSlot(player, menu);
        helper.assertTrue(menu.getCarried().isEmpty(), "clicking an empty result does not produce goods");
        helper.assertTrue(currentTrade(helper, fixture).uses() == 0,
                "a failed result click does not consume saved stock");

        menu.getSlot(0).setByPlayer(payment.copy());
        helper.assertTrue(ItemStack.matches(menu.getSlot(2).getItem(), result),
                "full payment creates the exact result in the vanilla merchant result slot");
        clickOutputSlot(player, menu, ContainerInput.QUICK_MOVE, (byte) 0);
        clickOutputSlot(player, menu, ContainerInput.PICKUP, (byte) 1);
        helper.assertTrue(menu.getCarried().isEmpty() && ItemStack.matches(menu.getSlot(0).getItem(), payment)
                        && currentTrade(helper, fixture).uses() == 0,
                "shift and right clicks are refused before payment or delivery");
        clickOutputSlot(player, menu);
        helper.assertTrue(ItemStack.matches(menu.getCarried(), result),
                "a normal PICKUP click with an empty cursor takes the result");
        helper.assertTrue(menu.getSlot(0).getItem().isEmpty(), "the successful trade consumes its payment");

        SettlementTrade completed = currentTrade(helper, fixture);
        helper.assertTrue(completed.stock() == 1 && completed.uses() == 1,
                "the successful real click persists exactly one use in SavedData");

        menu.setCarried(ItemStack.EMPTY);
        menu.getSlot(0).setByPlayer(payment.copy());
        helper.assertTrue(menu.getSlot(2).getItem().isEmpty(),
                "an exhausted offer does not create another result even with full payment");
        clickOutputSlot(player, menu);
        helper.assertTrue(menu.getCarried().isEmpty(), "an exhausted offer cannot be clicked again");
        helper.assertTrue(currentTrade(helper, fixture).uses() == 1,
                "clicking an exhausted offer does not change saved uses");

        fixture.resident().setRemainingFireTicks(60);

        helper.startSequence()
                .thenWaitUntil(() -> helper.assertFalse(fixture.resident().settlementTradeSession().isActive(),
                        "danger closes the active settlement trade session"))
                .thenExecute(() -> {
                    fixture.resident().clearFire();
                    SettlementPilotData.unassign(fixture.resident());
                    SettlementPilotData.getOrCreate(fixture.level()).removeLayout(fixture.settlement());
                    helper.assertTrue(currentTradeOrNull(fixture.level(), fixture.resident().getUUID()) == null,
                            "unassignment clears the saved trade");
                })
                .thenSucceed();
    }

    @GameTest(setupTicks = 1, maxTicks = 100)
    public void registrationGuardsDistanceSingleSessionAndSavedOffer(GameTestHelper helper) {
        BlockPos post = helper.absolutePos(new BlockPos(6, 1, 6));
        prepareFloor(helper, 8);
        ServerLevel level = helper.getLevel();
        Identifier settlement = id("guard_village/" + UUID.randomUUID());
        SettlementPilotData.getOrCreate(level).putLayout(settlement, new SettlementPilotData.Layout(
                Map.of(POST, post),
                Map.of(RESIDENT, new SettlementPilotData.Duty(
                        SettlementPilotData.Behavior.HOLD_POST, List.of(POST)))));
        HumanEntity resident = helper.spawn(RDEntityTypes.HUMAN.get(), new BlockPos(1, 1, 1));
        helper.assertTrue(SettlementPilotData.assign(resident, settlement, RESIDENT),
                "the human joins the holding-post settlement role");
        helper.assertFalse(SettlementPilotData.registerTrade(
                resident, id("wrong_post"), payment(), result(), 1), "the trade must name the resident's duty site");
        helper.assertTrue(SettlementPilotData.registerTrade(resident, POST, payment(), result(), 2),
                "the matching duty site accepts the explicit trade");

        ServerPlayer distantPlayer = mockPlayerAt(helper, new BlockPos(1, 1, 2));
        helper.assertFalse(resident.mobInteract(distantPlayer, InteractionHand.MAIN_HAND).consumesAction(),
                "a resident away from its post cannot open the trade menu");
        helper.assertFalse(resident.settlementTradeSession().isActive(),
                "the rejected interaction leaves no active session");

        resident.snapTo(Vec3.atBottomCenterOf(post));
        ServerPlayer customer = mockPlayerAt(helper, new BlockPos(5, 1, 6));
        helper.assertTrue(resident.mobInteract(customer, InteractionHand.MAIN_HAND).consumesAction(),
                "the resident opens its trade after reaching the registered post");
        helper.assertTrue(resident.settlementTradeSession().isActive(), "the customer owns the single active session");

        ServerPlayer secondCustomer = mockPlayerAt(helper, new BlockPos(5, 1, 7));
        helper.assertFalse(resident.mobInteract(secondCustomer, InteractionHand.MAIN_HAND).consumesAction(),
                "a second customer cannot replace the active single session");
        helper.assertTrue(secondCustomer.containerMenu == secondCustomer.inventoryMenu,
                "the second customer does not receive the first customer's menu");

        var serialized = SettlementPilotData.CODEC
                .encodeStart(NbtOps.INSTANCE, SettlementPilotData.getOrCreate(level))
                .getOrThrow();
        SettlementPilotData restored = SettlementPilotData.CODEC.parse(NbtOps.INSTANCE, serialized).getOrThrow();
        SettlementTrade saved = restored.trade(resident.getUUID());
        helper.assertTrue(saved != null && saved.stock() == 2 && saved.uses() == 0,
                "the saved offer retains stock and uses");
        helper.assertTrue(saved != null && ItemStack.matches(saved.payment(), payment())
                        && ItemStack.matches(saved.result(), result()),
                "the saved offer round-trips its exact payment and result stacks");

        var menu = merchantMenu(customer);
        menu.getSlot(0).setByPlayer(payment());
        helper.assertTrue(!menu.getSlot(2).getItem().isEmpty(), "a ready offer has a visible output");
        resident.setRemainingFireTicks(60);
        clickOutputSlot(customer, menu);
        helper.assertTrue(menu.getCarried().isEmpty() && ItemStack.matches(menu.getSlot(0).getItem(), payment())
                        && currentTradeOrNull(level, resident.getUUID()).uses() == 0,
                "a newly invalid session refuses the visible result before the next menu tick");
        resident.clearFire();
        SettlementPilotData.unassign(resident);
        helper.assertFalse(resident.settlementTradeSession().isActive(), "unassignment closes the active menu");
        helper.assertTrue(currentTradeOrNull(level, resident.getUUID()) == null,
                "unassignment removes the saved trade");
        SettlementPilotData.getOrCreate(level).removeLayout(settlement);
        helper.succeed();
    }

    private static TradeFixture prepareTrade(GameTestHelper helper, BlockPos residentPos) {
        prepareFloor(helper, 8);
        BlockPos post = helper.absolutePos(residentPos);
        ServerLevel level = helper.getLevel();
        Identifier settlement = id("trade_village/" + UUID.randomUUID());
        SettlementPilotData.getOrCreate(level).putLayout(settlement, new SettlementPilotData.Layout(
                Map.of(POST, post),
                Map.of(RESIDENT, new SettlementPilotData.Duty(
                        SettlementPilotData.Behavior.HOLD_POST, List.of(POST)))));
        HumanEntity resident = helper.spawn(RDEntityTypes.HUMAN.get(), residentPos);
        helper.assertTrue(SettlementPilotData.assign(resident, settlement, RESIDENT),
                "the actual human resident joins the trading pilot");
        return new TradeFixture(level, settlement, resident);
    }

    private static ServerPlayer mockPlayerAt(GameTestHelper helper, BlockPos localPos) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.snapTo(Vec3.atBottomCenterOf(helper.absolutePos(localPos)));
        return player;
    }

    private static VirtualMerchantScreenHandler merchantMenu(ServerPlayer player) {
        AbstractContainerMenu menu = player.containerMenu;
        if (!(menu instanceof VirtualMerchantScreenHandler merchantMenu)) {
            throw new AssertionError("Settlement trade did not open the SGUI merchant menu");
        }
        return merchantMenu;
    }

    private static void clickOutputSlot(ServerPlayer player, AbstractContainerMenu menu) {
        clickOutputSlot(player, menu, ContainerInput.PICKUP, (byte) 0);
    }

    private static void clickOutputSlot(ServerPlayer player, AbstractContainerMenu menu, ContainerInput action, byte button) {
        // Loom may relocate the packet's bundled collection type in the test runtime.
        try {
            var map = ServerboundContainerClickPacket.class.getMethod("changedSlots").getReturnType();
            var maps = Class.forName(map.getName() + "s", true, map.getClassLoader());
            var packet = ServerboundContainerClickPacket.class.getConstructor(int.class, int.class, short.class,
                    byte.class, ContainerInput.class, map, HashedStack.class);
            player.connection.handleContainerClick(packet.newInstance(menu.containerId, menu.getStateId(),
                    OUTPUT_SLOT, button, action, maps.getMethod("emptyMap").invoke(null), HashedStack.EMPTY));
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError("Cannot construct the vanilla merchant click packet", exception);
        }
    }

    private static SettlementTrade currentTrade(GameTestHelper helper, TradeFixture fixture) {
        SettlementTrade trade = currentTradeOrNull(fixture.level(), fixture.resident().getUUID());
        helper.assertTrue(trade != null, "the resident has a saved trade");
        return trade;
    }

    private static SettlementTrade currentTradeOrNull(ServerLevel level, UUID resident) {
        return SettlementPilotData.getOrCreate(level).trade(resident);
    }

    private static ItemStack payment() {
        return new ItemStack(Items.DIAMOND, PAYMENT_COUNT);
    }

    private static ItemStack result() {
        return new ItemStack(Items.EMERALD, 1);
    }

    private static void prepareFloor(GameTestHelper helper, int size) {
        for (int x = 0; x < size; x++) {
            for (int z = 0; z < size; z++) {
                helper.setBlock(new BlockPos(x, 0, z), Blocks.STONE);
            }
        }
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath("reverie_dreams_board_client_test", path);
    }

    private record TradeFixture(
            ServerLevel level,
            Identifier settlement,
            HumanEntity resident) {
    }
}
