package cc.thonly.reverie_dreams.fabric.gametest;

import cc.thonly.reverie_dreams.entity.HumanEntity;
import cc.thonly.reverie_dreams.registry.content.entity.RDEntityTypes;
import cc.thonly.reverie_dreams.world.settlement.SettlementNPCExchange;
import cc.thonly.reverie_dreams.world.settlement.SettlementPilotData;
import cc.thonly.reverie_dreams.world.settlement.SettlementTrade;
import net.fabricmc.fabric.api.gametest.v1.CustomTestMethodInvoker;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
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

public final class SettlementNPCExchangeGameTest implements CustomTestMethodInvoker {
    private static final Identifier SELLER_POST = id("seller_post");
    private static final Identifier BUYER_POST = id("buyer_post");
    private static final Identifier SELLER = id("seller");
    private static final Identifier BUYER = id("buyer");
    private static final Identifier OUTSIDE_POST = id("outside_post");
    private static final Identifier OUTSIDE_BUYER = id("outside_buyer");
    private static final int PAYMENT_COUNT = 2;

    @Override
    public void invokeTestMethod(GameTestHelper helper, Method method) throws ReflectiveOperationException {
        method.invoke(this, helper);
    }

    @GameTest(setupTicks = 1, maxTicks = 100)
    public void fullReceiveChestRefusesWholeExchangeWithoutChanges(GameTestHelper helper) {
        ExchangeFixture fixture = prepareExchange(helper);
        fillChest(fixture.buyerReceive(), new ItemStack(Items.STONE, 64));

        helper.assertFalse(SettlementNPCExchange.exchange(
                fixture.seller(), fixture.buyer(), fixture.buyerPaymentPos(), fixture.buyerReceivePos()),
                "a full receive chest refuses the whole four-chest exchange");
        assertChest(helper, fixture.sellerGoods(), result());
        assertChest(helper, fixture.sellerTill(), ItemStack.EMPTY);
        assertChest(helper, fixture.buyerPayment(), payment());
        assertUniformChest(helper, fixture.buyerReceive(), new ItemStack(Items.STONE, 64),
                "the full receive chest remains unchanged");
        assertCurrentUses(helper, fixture, 0, "the refused exchange does not consume the registered offer");

        cleanup(fixture);
        helper.succeed();
    }

    @GameTest(setupTicks = 1, maxTicks = 100)
    public void successfulExchangeMovesStockOnceThenAgainAfterManualRestock(GameTestHelper helper) {
        ExchangeFixture fixture = prepareExchange(helper);

        helper.assertTrue(SettlementNPCExchange.exchange(
                fixture.seller(), fixture.buyer(), fixture.buyerPaymentPos(), fixture.buyerReceivePos()),
                "two valid idle residents complete the four-chest exchange");
        assertChest(helper, fixture.sellerGoods(), ItemStack.EMPTY);
        assertChest(helper, fixture.sellerTill(), payment());
        assertChest(helper, fixture.buyerPayment(), ItemStack.EMPTY);
        assertChest(helper, fixture.buyerReceive(), result());
        assertCurrentUses(helper, fixture, 0, "a completed physical NPC exchange does not advance saved uses");

        helper.assertFalse(SettlementNPCExchange.exchange(
                fixture.seller(), fixture.buyer(), fixture.buyerPaymentPos(), fixture.buyerReceivePos()),
                "an immediate repeat cannot move stock while the goods and payment chests are empty");
        assertChest(helper, fixture.sellerGoods(), ItemStack.EMPTY);
        assertChest(helper, fixture.sellerTill(), payment());
        assertChest(helper, fixture.buyerPayment(), ItemStack.EMPTY);
        assertChest(helper, fixture.buyerReceive(), result());
        assertCurrentUses(helper, fixture, 0, "the refused repeat does not advance saved uses");

        fixture.sellerGoods().setItem(0, result());
        fixture.buyerPayment().setItem(0, payment());
        helper.assertTrue(SettlementNPCExchange.exchange(
                fixture.seller(), fixture.buyer(), fixture.buyerPaymentPos(), fixture.buyerReceivePos()),
                "manually restoring both physical inventories allows another exchange");
        assertChest(helper, fixture.sellerGoods(), ItemStack.EMPTY);
        assertChest(helper, fixture.sellerTill(), new ItemStack(Items.DIAMOND, PAYMENT_COUNT * 2));
        assertChest(helper, fixture.buyerPayment(), ItemStack.EMPTY);
        assertChest(helper, fixture.buyerReceive(), new ItemStack(Items.EMERALD, 2));
        assertCurrentUses(helper, fixture, 0, "the second physical exchange also leaves saved uses unchanged");

        cleanup(fixture);
        helper.succeed();
    }

    @GameTest(setupTicks = 1, maxTicks = 100)
    public void buyerFromAnotherSettlementRefusesExchangeWithoutChanges(GameTestHelper helper) {
        ExchangeFixture fixture = prepareExchange(helper);
        Identifier outsideSettlement = id("npc_exchange_outside_village/" + UUID.randomUUID());
        SettlementPilotData.getOrCreate(fixture.level()).putLayout(outsideSettlement, new SettlementPilotData.Layout(
                Map.of(OUTSIDE_POST, fixture.buyerPostPos()),
                Map.of(OUTSIDE_BUYER, new SettlementPilotData.Duty(
                        SettlementPilotData.Behavior.HOLD_POST, List.of(OUTSIDE_POST)))));
        SettlementPilotData.unassign(fixture.buyer());
        helper.assertTrue(SettlementPilotData.assign(fixture.buyer(), outsideSettlement, OUTSIDE_BUYER),
                "the buyer joins a different settlement before the exchange attempt");

        helper.assertFalse(SettlementNPCExchange.exchange(
                fixture.seller(), fixture.buyer(), fixture.buyerPaymentPos(), fixture.buyerReceivePos()),
                "residents from different settlements cannot exchange physical stock");
        assertInitialChests(helper, fixture);
        assertCurrentUses(helper, fixture, 0, "the settlement refusal does not advance saved uses");

        SettlementPilotData.unassign(fixture.buyer());
        SettlementPilotData.getOrCreate(fixture.level()).removeLayout(outsideSettlement);
        cleanup(fixture);
        helper.succeed();
    }

    @GameTest(setupTicks = 1, maxTicks = 100)
    public void aliasedBuyerBoxesRefuseExchangeWithoutChanges(GameTestHelper helper) {
        ExchangeFixture fixture = prepareExchange(helper);

        helper.assertFalse(SettlementNPCExchange.exchange(
                fixture.seller(), fixture.buyer(), fixture.buyerReceivePos(), fixture.buyerReceivePos()),
                "the payment and receive boxes cannot be the same chest");
        assertInitialChests(helper, fixture);
        assertCurrentUses(helper, fixture, 0, "the aliased-box refusal does not advance saved uses");

        cleanup(fixture);
        helper.succeed();
    }

    @GameTest(setupTicks = 1, maxTicks = 100)
    public void buyerAwayFromItsPostRefusesExchangeWithoutChanges(GameTestHelper helper) {
        ExchangeFixture fixture = prepareExchange(helper);
        fixture.buyer().setNoAi(true);
        fixture.buyer().snapTo(Vec3.atBottomCenterOf(helper.absolutePos(new BlockPos(7, 1, 7))));

        helper.assertFalse(SettlementNPCExchange.exchange(
                fixture.seller(), fixture.buyer(), fixture.buyerPaymentPos(), fixture.buyerReceivePos()),
                "a buyer away from its post cannot start the exchange");
        assertInitialChests(helper, fixture);
        assertCurrentUses(helper, fixture, 0, "the distance refusal does not consume the registered offer");

        cleanup(fixture);
        helper.succeed();
    }

    @GameTest(setupTicks = 1, maxTicks = 100)
    public void dangerousBuyerRefusesExchangeWithoutChanges(GameTestHelper helper) {
        ExchangeFixture fixture = prepareExchange(helper);
        var attacker = helper.spawnWithNoFreeWill(EntityType.HUSK, new BlockPos(5, 1, 5));
        attacker.setNoAi(true);
        fixture.buyer().setTarget(attacker);
        fixture.buyer().setRemainingFireTicks(60);

        helper.assertFalse(SettlementNPCExchange.exchange(
                fixture.seller(), fixture.buyer(), fixture.buyerPaymentPos(), fixture.buyerReceivePos()),
                "a buyer with an active threat and fire cannot start the exchange");
        assertInitialChests(helper, fixture);
        assertCurrentUses(helper, fixture, 0, "the danger refusal does not consume the registered offer");

        fixture.buyer().setTarget(null);
        fixture.buyer().clearFire();
        attacker.discard();
        cleanup(fixture);
        helper.succeed();
    }

    private static ExchangeFixture prepareExchange(GameTestHelper helper) {
        prepareFloor(helper, 8);
        BlockPos sellerPostLocal = new BlockPos(2, 1, 2);
        BlockPos buyerPostLocal = new BlockPos(3, 1, 2);
        BlockPos sellerGoodsLocal = sellerPostLocal.west();
        BlockPos sellerTillLocal = sellerPostLocal.south();
        BlockPos buyerPaymentLocal = buyerPostLocal.east();
        BlockPos buyerReceiveLocal = buyerPostLocal.north();
        helper.setBlock(sellerGoodsLocal, Blocks.CHEST);
        helper.setBlock(sellerTillLocal, Blocks.CHEST);
        helper.setBlock(buyerPaymentLocal, Blocks.CHEST);
        helper.setBlock(buyerReceiveLocal, Blocks.CHEST);

        helper.assertTrue(sellerPostLocal.distManhattan(buyerPostLocal) == 1,
                "the two duty posts are horizontally adjacent");
        helper.assertTrue(areFourDistinct(sellerGoodsLocal, sellerTillLocal, buyerPaymentLocal, buyerReceiveLocal),
                "the seller and buyer use four different chests");
        helper.assertTrue(sellerGoodsLocal.distManhattan(sellerPostLocal) == 1
                        && sellerTillLocal.distManhattan(sellerPostLocal) == 1
                        && buyerPaymentLocal.distManhattan(buyerPostLocal) == 1
                        && buyerReceiveLocal.distManhattan(buyerPostLocal) == 1,
                "each exchange chest is adjacent to its resident's post");
        helper.assertTrue(isSingleChest(helper, sellerGoodsLocal) && isSingleChest(helper, sellerTillLocal)
                        && isSingleChest(helper, buyerPaymentLocal) && isSingleChest(helper, buyerReceiveLocal),
                "all four exchange containers are separate vanilla single chests");

        ServerLevel level = helper.getLevel();
        BlockPos sellerPost = helper.absolutePos(sellerPostLocal);
        BlockPos buyerPost = helper.absolutePos(buyerPostLocal);
        BlockPos sellerGoodsPos = helper.absolutePos(sellerGoodsLocal);
        BlockPos sellerTillPos = helper.absolutePos(sellerTillLocal);
        BlockPos buyerPaymentPos = helper.absolutePos(buyerPaymentLocal);
        BlockPos buyerReceivePos = helper.absolutePos(buyerReceiveLocal);
        Identifier settlement = id("npc_exchange_village/" + UUID.randomUUID());
        SettlementPilotData.getOrCreate(level).putLayout(settlement, new SettlementPilotData.Layout(
                Map.of(SELLER_POST, sellerPost, BUYER_POST, buyerPost),
                Map.of(
                        SELLER, new SettlementPilotData.Duty(
                                SettlementPilotData.Behavior.HOLD_POST, List.of(SELLER_POST)),
                        BUYER, new SettlementPilotData.Duty(
                                SettlementPilotData.Behavior.HOLD_POST, List.of(BUYER_POST)))));

        HumanEntity seller = helper.spawn(RDEntityTypes.HUMAN.get(), sellerPostLocal);
        HumanEntity buyer = helper.spawn(RDEntityTypes.HUMAN.get(), buyerPostLocal);
        helper.assertTrue(SettlementPilotData.assign(seller, settlement, SELLER),
                "the seller joins the exchange settlement as a human resident");
        helper.assertTrue(SettlementPilotData.assign(buyer, settlement, BUYER),
                "the buyer joins the same settlement in a different role");
        SettlementPilotData data = SettlementPilotData.getOrCreate(level);
        SettlementPilotData.Member sellerMember = data.member(seller.getUUID());
        SettlementPilotData.Member buyerMember = data.member(buyer.getUUID());
        helper.assertTrue(sellerMember != null && buyerMember != null
                        && sellerMember.settlement().equals(buyerMember.settlement())
                        && !sellerMember.role().equals(buyerMember.role()),
                "the two human residents belong to one settlement in different roles");

        ChestBlockEntity sellerGoods = helper.getBlockEntity(sellerGoodsLocal, ChestBlockEntity.class);
        ChestBlockEntity sellerTill = helper.getBlockEntity(sellerTillLocal, ChestBlockEntity.class);
        ChestBlockEntity buyerPayment = helper.getBlockEntity(buyerPaymentLocal, ChestBlockEntity.class);
        ChestBlockEntity buyerReceive = helper.getBlockEntity(buyerReceiveLocal, ChestBlockEntity.class);
        helper.assertTrue(sellerGoods != null && sellerTill != null && buyerPayment != null && buyerReceive != null,
                "all four exchange sides are real chest block entities");
        sellerGoods.setItem(0, result());
        buyerPayment.setItem(0, payment());
        helper.assertTrue(SettlementPilotData.registerStockTrade(
                seller, SELLER_POST, payment(), result(), sellerGoodsPos, sellerTillPos),
                "the seller has an existing registered physical offer");

        SettlementTrade registered = data.trade(seller.getUUID());
        helper.assertTrue(registered != null && registered.stock() == 1 && registered.uses() == 0,
                "the registered physical offer starts with saved uses at zero");
        var stockroom = registered == null ? null : registered.stockroom().orElse(null);
        helper.assertTrue(stockroom != null && stockroom.goods().equals(sellerGoodsPos)
                        && stockroom.till().equals(sellerTillPos),
                "the registered offer owns the seller goods and payment chests");
        ExchangeFixture fixture = new ExchangeFixture(level, settlement, seller, buyer, buyerPost,
                buyerPaymentPos, buyerReceivePos, sellerGoods, sellerTill, buyerPayment, buyerReceive);
        assertInitialChests(helper, fixture);
        return fixture;
    }

    private static boolean isSingleChest(GameTestHelper helper, BlockPos localPos) {
        var state = helper.getBlockState(localPos);
        return state.getBlock() == Blocks.CHEST && state.getValue(ChestBlock.TYPE) == ChestType.SINGLE;
    }

    private static boolean areFourDistinct(BlockPos first, BlockPos second, BlockPos third, BlockPos fourth) {
        return !first.equals(second) && !first.equals(third) && !first.equals(fourth)
                && !second.equals(third) && !second.equals(fourth) && !third.equals(fourth);
    }

    private static void assertInitialChests(GameTestHelper helper, ExchangeFixture fixture) {
        assertChest(helper, fixture.sellerGoods(), result());
        assertChest(helper, fixture.sellerTill(), ItemStack.EMPTY);
        assertChest(helper, fixture.buyerPayment(), payment());
        assertChest(helper, fixture.buyerReceive(), ItemStack.EMPTY);
    }

    private static void assertChest(GameTestHelper helper, ChestBlockEntity chest, ItemStack firstSlot) {
        for (int slot = 0; slot < chest.getContainerSize(); slot++) {
            ItemStack expected = slot == 0 ? firstSlot : ItemStack.EMPTY;
            helper.assertTrue(ItemStack.matches(chest.getItem(slot), expected),
                    "chest slot " + slot + " has the expected physical exchange content");
        }
    }

    private static void assertUniformChest(GameTestHelper helper, ChestBlockEntity chest, ItemStack stack,
                                            String message) {
        for (int slot = 0; slot < chest.getContainerSize(); slot++) {
            helper.assertTrue(ItemStack.matches(chest.getItem(slot), stack), message + " at slot " + slot);
        }
    }

    private static void assertCurrentUses(GameTestHelper helper, ExchangeFixture fixture, int uses, String message) {
        SettlementTrade trade = SettlementPilotData.getOrCreate(fixture.level()).trade(fixture.seller().getUUID());
        helper.assertTrue(trade != null && trade.uses() == uses, message);
    }

    private static void fillChest(ChestBlockEntity chest, ItemStack stack) {
        for (int slot = 0; slot < chest.getContainerSize(); slot++) {
            chest.setItem(slot, stack.copy());
        }
    }

    private static void cleanup(ExchangeFixture fixture) {
        fixture.seller().settlementTradeSession().close();
        fixture.buyer().settlementTradeSession().close();
        SettlementPilotData.unassign(fixture.seller());
        SettlementPilotData.unassign(fixture.buyer());
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

    private record ExchangeFixture(
            ServerLevel level,
            Identifier settlement,
            HumanEntity seller,
            HumanEntity buyer,
            BlockPos buyerPostPos,
            BlockPos buyerPaymentPos,
            BlockPos buyerReceivePos,
            ChestBlockEntity sellerGoods,
            ChestBlockEntity sellerTill,
            ChestBlockEntity buyerPayment,
            ChestBlockEntity buyerReceive) {
    }
}
