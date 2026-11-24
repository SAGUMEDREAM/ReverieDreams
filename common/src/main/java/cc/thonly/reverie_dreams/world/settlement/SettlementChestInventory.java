package cc.thonly.reverie_dreams.world.settlement;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

final class SettlementChestInventory {
    private record SlotAmount(int slot, int amount) {
    }

    private record TakePlan(ItemStack batch, List<SlotAmount> takes) {
    }

    private record PutPlan(ItemStack batch, List<SlotAmount> puts) {
    }

    private record ExchangePlan(ChestBlockEntity goodsChest, ChestBlockEntity tillChest,
                                TakePlan goods, PutPlan payment) {
    }

    private record FourChestExchangePlan(ChestBlockEntity goodsChest, ChestBlockEntity tillChest,
                                         ChestBlockEntity buyerPaymentChest, ChestBlockEntity buyerGoodsChest,
                                         TakePlan goods, PutPlan payment,
                                         TakePlan buyerPayment, PutPlan buyerGoods) {
    }

    private SettlementChestInventory() {
    }

    static boolean isAvailable(ServerLevel level, BlockPos chestPos) {
        return availableChest(level, chestPos) != null;
    }

    static ItemStack takeBatch(ServerLevel level, BlockPos chestPos, Item item, int count) {
        ChestBlockEntity chest = availableChest(level, chestPos);
        if (chest == null || item == null || count <= 0) {
            return ItemStack.EMPTY;
        }

        TakePlan plan = planTakeBatch(chest, item, count);
        if (plan == null) {
            return ItemStack.EMPTY;
        }
        applyTakePlan(chest, plan);
        return plan.batch();
    }

    static boolean canExchange(ServerLevel level, BlockPos goodsChest, ItemStack goods,
                               BlockPos tillChest, ItemStack payment) {
        return planExchange(level, goodsChest, goods, tillChest, payment) != null;
    }

    static boolean exchange(ServerLevel level, BlockPos goodsChest, ItemStack goods,
                            BlockPos tillChest, ItemStack payment) {
        ExchangePlan plan = planExchange(level, goodsChest, goods, tillChest, payment);
        if (plan == null) {
            return false;
        }

        applyTakePlan(plan.goodsChest(), plan.goods());
        applyPutPlan(plan.tillChest(), plan.payment());
        return true;
    }

    static boolean exchangeBetween(ServerLevel level, BlockPos goods, BlockPos till, BlockPos buyerPayment,
                                   BlockPos buyerGoods, ItemStack result, ItemStack payment) {
        FourChestExchangePlan plan = planExchangeBetween(level, goods, till, buyerPayment, buyerGoods, result, payment);
        if (plan == null) {
            return false;
        }

        applyExchangeBetween(plan);
        return true;
    }

    static boolean putBatch(ServerLevel level, BlockPos chestPos, ItemStack batch) {
        ChestBlockEntity chest = availableChest(level, chestPos);
        if (chest == null || batch == null || batch.isEmpty()) {
            return false;
        }

        PutPlan plan = planPutBatch(chest, batch);
        if (plan == null) {
            return false;
        }
        applyPutPlan(chest, plan);
        return true;
    }

    @Nullable
    private static ExchangePlan planExchange(ServerLevel level, BlockPos goodsChest, ItemStack goods,
                                             BlockPos tillChest, ItemStack payment) {
        if (level == null || !level.getServer().isSameThread()
                || goodsChest == null || tillChest == null || goodsChest.equals(tillChest)
                || goods == null || goods.isEmpty() || payment == null || payment.isEmpty()) {
            return null;
        }

        ChestBlockEntity goodsContainer = availableChest(level, goodsChest);
        ChestBlockEntity tillContainer = availableChest(level, tillChest);
        if (goodsContainer == null || tillContainer == null) {
            return null;
        }

        TakePlan goodsPlan = planTakeBatch(goodsContainer, goods, goods.getCount());
        PutPlan paymentPlan = planPutBatch(tillContainer, payment);
        if (goodsPlan == null || paymentPlan == null) {
            return null;
        }
        return new ExchangePlan(goodsContainer, tillContainer, goodsPlan, paymentPlan);
    }

    @Nullable
    private static FourChestExchangePlan planExchangeBetween(ServerLevel level, BlockPos goods, BlockPos till,
                                                             BlockPos buyerPayment, BlockPos buyerGoods,
                                                             ItemStack result, ItemStack payment) {
        if (level == null || !level.getServer().isSameThread()
                || goods == null || till == null || buyerPayment == null || buyerGoods == null
                || goods.equals(till) || goods.equals(buyerPayment) || goods.equals(buyerGoods)
                || till.equals(buyerPayment) || till.equals(buyerGoods) || buyerPayment.equals(buyerGoods)
                || result == null || payment == null || result.isEmpty() || payment.isEmpty()) {
            return null;
        }

        ChestBlockEntity goodsContainer = availableChest(level, goods);
        ChestBlockEntity tillContainer = availableChest(level, till);
        ChestBlockEntity buyerPaymentContainer = availableChest(level, buyerPayment);
        ChestBlockEntity buyerGoodsContainer = availableChest(level, buyerGoods);
        if (goodsContainer == null || tillContainer == null
                || buyerPaymentContainer == null || buyerGoodsContainer == null) {
            return null;
        }

        TakePlan goodsPlan = planTakeBatch(goodsContainer, result, result.getCount());
        PutPlan paymentPlan = planPutBatch(tillContainer, payment);
        TakePlan buyerPaymentPlan = planTakeBatch(buyerPaymentContainer, payment, payment.getCount());
        PutPlan buyerGoodsPlan = planPutBatch(buyerGoodsContainer, result);
        if (goodsPlan == null || paymentPlan == null
                || buyerPaymentPlan == null || buyerGoodsPlan == null) {
            return null;
        }

        return new FourChestExchangePlan(goodsContainer, tillContainer, buyerPaymentContainer, buyerGoodsContainer,
                goodsPlan, paymentPlan, buyerPaymentPlan, buyerGoodsPlan);
    }

    @Nullable
    private static TakePlan planTakeBatch(ChestBlockEntity chest, Item item, int count) {
        int size = chest.getContainerSize();
        for (int anchor = 0; anchor < size; anchor++) {
            ItemStack template = chest.getItem(anchor);
            if (template.isEmpty() || template.getItem() != item || count > template.getMaxStackSize()) {
                continue;
            }

            TakePlan plan = planTakeBatch(chest, template, count);
            if (plan != null) {
                return plan;
            }
        }
        return null;
    }

    @Nullable
    private static TakePlan planTakeBatch(ChestBlockEntity chest, ItemStack template, int count) {
        if (template.isEmpty() || count <= 0 || count > template.getMaxStackSize()) {
            return null;
        }

        int size = chest.getContainerSize();
        List<SlotAmount> takes = new ArrayList<>();
        int remaining = count;
        for (int slot = 0; slot < size && remaining > 0; slot++) {
            ItemStack slotItem = chest.getItem(slot);
            if (!isSameVariant(template, slotItem)) {
                continue;
            }

            int amount = Math.min(slotItem.getCount(), remaining);
            if (amount > 0) {
                takes.add(new SlotAmount(slot, amount));
                remaining -= amount;
            }
        }
        if (remaining != 0) {
            return null;
        }

        return new TakePlan(template.copyWithCount(count), takes);
    }

    private static void applyTakePlan(ChestBlockEntity chest, TakePlan plan) {
        applyTakePlanSlots(chest, plan);
        chest.setChanged();
    }

    private static void applyTakePlanSlots(ChestBlockEntity chest, TakePlan plan) {
        for (SlotAmount take : plan.takes()) {
            ItemStack slotItem = chest.getItem(take.slot());
            if (take.amount() == slotItem.getCount()) {
                chest.setItem(take.slot(), ItemStack.EMPTY);
            } else {
                chest.setItem(take.slot(), slotItem.copyWithCount(slotItem.getCount() - take.amount()));
            }
        }
    }

    @Nullable
    private static PutPlan planPutBatch(ChestBlockEntity chest, ItemStack batch) {
        if (batch.isEmpty()) {
            return null;
        }

        int maxStackSize = chest.getMaxStackSize(batch);
        if (batch.getCount() > maxStackSize) {
            return null;
        }

        int size = chest.getContainerSize();
        List<SlotAmount> puts = new ArrayList<>();
        int remaining = batch.getCount();
        for (int slot = 0; slot < size && remaining > 0; slot++) {
            ItemStack slotItem = chest.getItem(slot);
            int slotCapacity;
            if (slotItem.isEmpty()) {
                slotCapacity = maxStackSize;
            } else if (isSameVariant(batch, slotItem)) {
                slotCapacity = maxStackSize - slotItem.getCount();
            } else {
                continue;
            }

            int amount = Math.min(remaining, slotCapacity);
            if (amount > 0) {
                puts.add(new SlotAmount(slot, amount));
                remaining -= amount;
            }
        }
        if (remaining != 0) {
            return null;
        }

        return new PutPlan(batch.copy(), puts);
    }

    private static void applyPutPlan(ChestBlockEntity chest, PutPlan plan) {
        applyPutPlanSlots(chest, plan);
        chest.setChanged();
    }

    private static void applyPutPlanSlots(ChestBlockEntity chest, PutPlan plan) {
        for (SlotAmount put : plan.puts()) {
            ItemStack slotItem = chest.getItem(put.slot());
            if (slotItem.isEmpty()) {
                chest.setItem(put.slot(), plan.batch().copyWithCount(put.amount()));
            } else {
                ItemStack merged = slotItem.copy();
                merged.grow(put.amount());
                chest.setItem(put.slot(), merged);
            }
        }
    }

    private static void applyExchangeBetween(FourChestExchangePlan plan) {
        applyTakePlanSlots(plan.goodsChest(), plan.goods());
        applyPutPlanSlots(plan.tillChest(), plan.payment());
        applyTakePlanSlots(plan.buyerPaymentChest(), plan.buyerPayment());
        applyPutPlanSlots(plan.buyerGoodsChest(), plan.buyerGoods());

        plan.goodsChest().setChanged();
        plan.tillChest().setChanged();
        plan.buyerPaymentChest().setChanged();
        plan.buyerGoodsChest().setChanged();
    }

    @Nullable
    private static ChestBlockEntity availableChest(ServerLevel level, BlockPos chestPos) {
        if (level == null || chestPos == null) {
            return null;
        }

        BlockPos pos = chestPos.immutable();
        LevelChunk chunk = level.getChunkSource().getChunkNow(pos.getX() >> 4, pos.getZ() >> 4);
        if (chunk == null || !level.isPositionEntityTicking(pos)) {
            return null;
        }

        BlockState state = chunk.getBlockState(pos);
        if (state.getBlock() != Blocks.CHEST || state.getValue(ChestBlock.TYPE) != ChestType.SINGLE) {
            return null;
        }

        BlockEntity blockEntity = chunk.getBlockEntity(pos);
        if (blockEntity == null
                || blockEntity.getType() != BlockEntityType.CHEST
                || !(blockEntity instanceof ChestBlockEntity chest)
                || !chest.getBlockPos().equals(pos)
                || chest.getLevel() != level
                || !chest.isValidBlockState(state)
                || chest.isLocked()
                || chest.getLootTable() != null) {
            return null;
        }
        return chest;
    }

    private static boolean isSameVariant(ItemStack template, ItemStack stack) {
        return !stack.isEmpty() && ItemStack.isSameItemSameComponents(template, stack);
    }
}
