package cc.thonly.reverie_dreams.fabric.gametest;

import cc.thonly.reverie_dreams.entity.HumanEntity;
import cc.thonly.reverie_dreams.registry.content.entity.RDEntityTypes;
import cc.thonly.reverie_dreams.world.settlement.SettlementPilotData;
import cc.thonly.reverie_dreams.world.settlement.SettlementTrade;
import eu.pb4.sgui.impl.virtual.merchant.VirtualMerchantScreenHandler;
import net.fabricmc.fabric.api.gametest.v1.CustomTestMethodInvoker;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.HashedStack;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.phys.Vec3;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class SettlementStockTradeGameTest implements CustomTestMethodInvoker {
    private static final Identifier POST = id("stock_post");
    private static final Identifier RESIDENT = id("stock_resident");
    private static final Identifier SUPPLY = id("supply");
    private static final Identifier DELIVERY = id("delivery");
    private static final int PAYMENT_COUNT = 2;
    private static final short OUTPUT_SLOT = 2;

    @Override
    public void invokeTestMethod(GameTestHelper helper, Method method) throws ReflectiveOperationException {
        method.invoke(this, helper);
    }

    @GameTest(setupTicks = 1, maxTicks = 200)
    public void realClickMovesPhysicalStockOnceThenSecondPlayerIsRefused(GameTestHelper helper) {
        StockFixture fixture = prepareStock(helper);
        ServerPlayer firstPlayer = mockPlayerAt(helper, new BlockPos(5, 1, 4));
        ServerPlayer secondPlayer = mockPlayerAt(helper, new BlockPos(3, 1, 4));
        ItemStack payment = payment();
        ItemStack result = result();

        helper.assertTrue(payment.getComponentsPatch().isEmpty(),
                "the core stock trade uses an ordinary payment without extra components");
        fixture.goodsChest().setItem(0, result.copy());
        helper.assertTrue(SettlementPilotData.registerStockTrade(
                fixture.resident(), POST, payment, result, fixture.goodsChestPos(), fixture.tillChestPos()),
                "the enrolled resident accepts a physical stock trade");

        SettlementTrade registered = currentTrade(helper, fixture);
        helper.assertTrue(registered.stock() == 1 && registered.uses() == 0,
                "a physical offer starts as a single saved batch");
        var stockroom = registered.stockroom().orElse(null);
        helper.assertTrue(stockroom != null && stockroom.goods().equals(fixture.goodsChestPos())
                        && stockroom.till().equals(fixture.tillChestPos()),
                "the physical offer stores both separate stockroom chests");

        helper.assertTrue(fixture.resident().mobInteract(firstPlayer, InteractionHand.MAIN_HAND).consumesAction(),
                "the first player opens the physical merchant menu");
        helper.assertFalse(fixture.resident().mobInteract(secondPlayer, InteractionHand.MAIN_HAND).consumesAction(),
                "a second player cannot replace the active physical menu");
        helper.assertTrue(secondPlayer.containerMenu == secondPlayer.inventoryMenu,
                "the second player does not receive the first player's menu");

        VirtualMerchantScreenHandler menu = merchantMenu(firstPlayer);
        menu.getSlot(0).setByPlayer(payment.copy());
        helper.assertTrue(ItemStack.matches(menu.getSlot(2).getItem(), result),
                "the matching goods create the exact result in the merchant output");
        fixture.goodsChest().setItem(0, ItemStack.EMPTY);
        clickOutputSlot(firstPlayer, menu);
        helper.assertTrue(menu.getCarried().isEmpty() && ItemStack.matches(menu.getSlot(0).getItem(), payment)
                && fixture.tillChest().isEmpty(), "removing goods after opening refuses the whole trade");
        fixture.goodsChest().setItem(0, result.copy());
        for (int slot = 0; slot < fixture.tillChest().getContainerSize(); slot++) {
            fixture.tillChest().setItem(slot, new ItemStack(Items.STONE, 64));
        }
        clickOutputSlot(firstPlayer, menu);
        helper.assertTrue(menu.getCarried().isEmpty() && ItemStack.matches(menu.getSlot(0).getItem(), payment)
                && ItemStack.matches(fixture.goodsChest().getItem(0), result), "a full till refuses the whole trade");
        fixture.tillChest().clearContent();
        ItemStack actualPayment = payment.copy();
        actualPayment.set(DataComponents.CUSTOM_NAME, Component.literal("Marked payment"));
        menu.getSlot(0).setByPlayer(actualPayment.copy());
        clickOutputSlot(firstPlayer, menu);
        helper.assertTrue(ItemStack.matches(menu.getCarried(), result),
                "a real PICKUP click takes the physical result");
        helper.assertTrue(menu.getSlot(0).getItem().isEmpty(), "the successful trade consumes its payment");
        assertSingleMatchingStack(helper, fixture.goodsChest(), ItemStack.EMPTY,
                "the successful trade removes the sold goods");
        assertSingleMatchingStack(helper, fixture.tillChest(), actualPayment,
                "the successful trade deposits the actual payment with its components");
        helper.assertTrue(currentTrade(helper, fixture).uses() == 0,
                "physical stock completion is represented by the chest contents");

        menu.setCarried(ItemStack.EMPTY);
        menu.getSlot(0).setByPlayer(payment.copy());
        helper.assertTrue(menu.getSlot(2).getItem().isEmpty(),
                "the same open menu cannot create another batch after its stock was taken");
        clickOutputSlot(firstPlayer, menu);
        helper.assertTrue(menu.getCarried().isEmpty()
                        && ItemStack.matches(menu.getSlot(0).getItem(), payment),
                "the second real click in the same menu is refused without taking another payment");
        assertSingleMatchingStack(helper, fixture.tillChest(), actualPayment,
                "the refused second click does not duplicate the payment in the till");

        fixture.resident().settlementTradeSession().close();
        helper.assertFalse(fixture.resident().settlementTradeSession().isActive(),
                "closing the first menu clears the active session");
        helper.assertTrue(firstPlayer.containerMenu == firstPlayer.inventoryMenu,
                "closing the session removes the first player's menu");

        helper.assertFalse(fixture.resident().mobInteract(secondPlayer, InteractionHand.MAIN_HAND).consumesAction(),
                "the second player cannot reopen after the last physical goods were taken");
        helper.assertTrue(secondPlayer.containerMenu == secondPlayer.inventoryMenu,
                "the second player still has no merchant menu");
        helper.assertFalse(fixture.resident().settlementTradeSession().isActive(),
                "the rejected reopen leaves no active session");

        cleanup(fixture);
        helper.succeed();
    }

    @GameTest(setupTicks = 10, maxTicks = 480)
    public void residentRestocksGoodsChestAndPlayerCanBuy(GameTestHelper helper) {
        StockFixture fixture = prepareStock(helper);
        ServerLevel level = fixture.level();
        ServerPlayer player = mockPlayerAt(helper, new BlockPos(5, 1, 4));
        helper.assertTrue(SettlementPilotData.registerStockTrade(fixture.resident(), POST, payment(), result(),
                fixture.goodsChestPos(), fixture.tillChestPos()), "register an empty shop");
        helper.assertFalse(fixture.resident().mobInteract(player, InteractionHand.MAIN_HAND).consumesAction(),
                "an empty shop refuses to sell");

        BlockPos sourceLocal = new BlockPos(1, 1, 1);
        BlockPos deliveryLocal = new BlockPos(4, 1, 2);
        helper.setBlock(sourceLocal.north(), Blocks.CHEST);
        var source = helper.getBlockEntity(sourceLocal.north(), ChestBlockEntity.class);
        source.setItem(0, result());
        var carrier = helper.spawn(RDEntityTypes.HUMAN.get(), sourceLocal);
        var layoutId = id("resupply/" + UUID.randomUUID());
        BlockPos from = helper.absolutePos(sourceLocal);
        BlockPos to = helper.absolutePos(deliveryLocal);
        for (int x = (Math.min(from.getX(), to.getX()) - 24) >> 4;
             x <= (Math.max(from.getX(), to.getX()) + 24) >> 4; x++) {
            for (int z = (Math.min(from.getZ(), to.getZ()) - 24) >> 4;
                 z <= (Math.max(from.getZ(), to.getZ()) + 24) >> 4; z++) {
                level.setChunkForced(x, z, true);
                level.getChunk(x, z);
            }
        }
        fixture.data().putLayout(layoutId, new SettlementPilotData.Layout(Map.of(SUPPLY, from, DELIVERY, to),
                Map.of(RESIDENT, new SettlementPilotData.Duty(SettlementPilotData.Behavior.HOLD_POST, List.of(SUPPLY)))));
        helper.assertTrue(SettlementPilotData.assign(carrier, layoutId, RESIDENT), "assign the supply carrier");
        helper.assertTrue(SettlementPilotData.queueHaul(carrier, SUPPLY, helper.absolutePos(sourceLocal.north()),
                DELIVERY, fixture.goodsChestPos(), Items.EMERALD, 1), "queue a real delivery to the shop");

        helper.startSequence().thenWaitUntil(() -> helper.assertTrue(fixture.data().haul(carrier.getUUID()) == null,
                "the carrier completes the shop's delivery"))
                .thenExecute(() -> {
                    helper.assertTrue(source.isEmpty() && ItemStack.matches(fixture.goodsChest().getItem(0), result()),
                            "goods arrive in the actual shop inventory");
                    helper.assertTrue(fixture.resident().mobInteract(player, InteractionHand.MAIN_HAND).consumesAction(),
                            "delivery makes the offer available again");
                    var menu = merchantMenu(player);
                    menu.getSlot(0).setByPlayer(payment());
                    clickOutputSlot(player, menu);
                    helper.assertTrue(ItemStack.matches(menu.getCarried(), result()) && fixture.goodsChest().isEmpty()
                                    && ItemStack.matches(fixture.tillChest().getItem(0), payment()),
                            "the restocked goods can be purchased exactly once");
                    SettlementPilotData.unassign(carrier);
                    fixture.data().removeLayout(layoutId);
                    carrier.discard();
                    cleanup(fixture);
                }).thenSucceed();
    }

    private static StockFixture prepareStock(GameTestHelper helper) {
        prepareFloor(helper, 8);
        BlockPos postLocal = new BlockPos(4, 1, 4);
        BlockPos goodsLocal = postLocal.north();
        BlockPos tillLocal = postLocal.south();
        helper.setBlock(goodsLocal, Blocks.CHEST);
        helper.setBlock(tillLocal, Blocks.CHEST);

        helper.assertTrue(!goodsLocal.equals(tillLocal)
                        && goodsLocal.distManhattan(postLocal) == 1
                        && tillLocal.distManhattan(postLocal) == 1,
                "the goods and till are two different horizontally adjacent chests");
        helper.assertTrue(helper.getBlockState(goodsLocal).getValue(ChestBlock.TYPE) == ChestType.SINGLE
                        && helper.getBlockState(tillLocal).getValue(ChestBlock.TYPE) == ChestType.SINGLE,
                "the two stockroom chests remain vanilla single chests");

        ServerLevel level = helper.getLevel();
        BlockPos post = helper.absolutePos(postLocal);
        BlockPos goodsChestPos = helper.absolutePos(goodsLocal);
        BlockPos tillChestPos = helper.absolutePos(tillLocal);
        Identifier settlement = id("stock_village/" + UUID.randomUUID());
        SettlementPilotData.getOrCreate(level).putLayout(settlement, new SettlementPilotData.Layout(
                Map.of(POST, post),
                Map.of(RESIDENT, new SettlementPilotData.Duty(
                        SettlementPilotData.Behavior.HOLD_POST, List.of(POST)))));

        HumanEntity resident = helper.spawn(RDEntityTypes.HUMAN.get(), postLocal);
        helper.assertTrue(SettlementPilotData.assign(resident, settlement, RESIDENT),
                "the actual human resident joins the physical stock trade pilot");

        ChestBlockEntity goodsChest = helper.getBlockEntity(goodsLocal, ChestBlockEntity.class);
        ChestBlockEntity tillChest = helper.getBlockEntity(tillLocal, ChestBlockEntity.class);
        helper.assertTrue(goodsChest != null && tillChest != null,
                "both sides of the stockroom are available as separate chest inventories");
        return new StockFixture(
                level,
                settlement,
                resident,
                goodsChestPos,
                tillChestPos,
                goodsChest,
                tillChest);
    }

    private static ServerPlayer mockPlayerAt(GameTestHelper helper, BlockPos localPos) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.snapTo(Vec3.atBottomCenterOf(helper.absolutePos(localPos)));
        return player;
    }

    private static VirtualMerchantScreenHandler merchantMenu(ServerPlayer player) {
        AbstractContainerMenu menu = player.containerMenu;
        if (!(menu instanceof VirtualMerchantScreenHandler merchantMenu)) {
            throw new AssertionError("Settlement stock trade did not open the SGUI merchant menu");
        }
        return merchantMenu;
    }

    private static void clickOutputSlot(ServerPlayer player, AbstractContainerMenu menu) {
        // Loom may relocate the packet's bundled collection type in the test runtime.
        try {
            var map = ServerboundContainerClickPacket.class.getMethod("changedSlots").getReturnType();
            var maps = Class.forName(map.getName() + "s", true, map.getClassLoader());
            var packet = ServerboundContainerClickPacket.class.getConstructor(int.class, int.class, short.class,
                    byte.class, ContainerInput.class, map, HashedStack.class);
            player.connection.handleContainerClick(packet.newInstance(menu.containerId, menu.getStateId(),
                    OUTPUT_SLOT, (byte) 0, ContainerInput.PICKUP,
                    maps.getMethod("emptyMap").invoke(null), HashedStack.EMPTY));
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError("Cannot construct the vanilla merchant click packet", exception);
        }
    }

    private static SettlementTrade currentTrade(GameTestHelper helper, StockFixture fixture) {
        SettlementTrade trade = fixture.data().trade(fixture.resident().getUUID());
        helper.assertTrue(trade != null, "the resident has a saved physical trade");
        return trade;
    }

    private static void assertSingleMatchingStack(GameTestHelper helper, ChestBlockEntity chest,
                                                   ItemStack expected, String message) {
        if (expected.isEmpty()) {
            helper.assertTrue(chest.isEmpty(), message);
            return;
        }

        ItemStack matching = ItemStack.EMPTY;
        int matchingSlots = 0;
        int matchingCount = 0;
        for (int slot = 0; slot < chest.getContainerSize(); slot++) {
            ItemStack stack = chest.getItem(slot);
            if (ItemStack.isSameItemSameComponents(stack, expected)) {
                matchingSlots++;
                matchingCount += stack.getCount();
                matching = stack;
            }
        }
        helper.assertTrue(matchingSlots == 1 && matchingCount == expected.getCount(),
                message + " (one stack with the full count)");
        helper.assertTrue(ItemStack.matches(matching, expected), message + " (exact components)");
    }

    private static void cleanup(StockFixture fixture) {
        fixture.resident().settlementTradeSession().close();
        SettlementPilotData.unassign(fixture.resident());
        SettlementPilotData.getOrCreate(fixture.level()).removeLayout(fixture.settlement());
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

    private record StockFixture(
            ServerLevel level,
            Identifier settlement,
            HumanEntity resident,
            BlockPos goodsChestPos,
            BlockPos tillChestPos,
            ChestBlockEntity goodsChest,
            ChestBlockEntity tillChest) {
        SettlementPilotData data() {
            // An older GameTest may replace the dimension SavedData instance.
            return SettlementPilotData.getOrCreate(this.level);
        }
    }
}
