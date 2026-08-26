package cc.thonly.reverie_dreams.api.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public final class DispenserBlockItemBehaviors {
    private static final DispenserBlockItemBehaviors INSTANCE =
            new DispenserBlockItemBehaviors();

    private final List<BehaviorEntry> registry = new ArrayList<>();

    private DispenserBlockItemBehaviors() {
    }

    public static void add(
            Predicate<ItemStack> predicate,
            BehaviorDefinition behaviorDefinition
    ) {
        INSTANCE.registerBehavior(predicate, behaviorDefinition);
    }

    public void registerBehavior(
            Predicate<ItemStack> predicate,
            BehaviorDefinition behaviorDefinition
    ) {
        this.registry.add(new BehaviorEntry(predicate, behaviorDefinition));
    }

    public TriggerResult onTrigger(
            ServerLevel level,
            BlockState state,
            BlockPos pos
    ) {
        DispenserBlockEntity blockEntity = level.getBlockEntity(pos, BlockEntityType.DISPENSER).orElse(null);

        if (blockEntity == null) {
            return TriggerResult.pass();
        }

        RandomSource random = level.getRandom();
        int slot = blockEntity.getRandomSlot(random);

        if (slot < 0) {
            return TriggerResult.pass();
        }

        ItemStack stack = blockEntity.getItem(slot);
        if (stack.isEmpty()) {
            return TriggerResult.pass();
        }

        BlockSource source = new BlockSource(level, pos, state, blockEntity);
        for (BehaviorEntry entry : this.registry) {
            if (!entry.predicate().test(stack)) {
                continue;
            }

            TriggerResult result = entry.behavior().dispense(source, stack);

            if (result.isPass()) {
                continue;
            }

            blockEntity.setItem(slot, result.itemStack());

            return result;
        }

        return TriggerResult.pass();
    }

    public static DispenserBlockItemBehaviors get() {
        return INSTANCE;
    }

    private record BehaviorEntry(Predicate<ItemStack> predicate, BehaviorDefinition behavior) {
    }

    public interface BehaviorDefinition {
        TriggerResult dispense(BlockSource source, ItemStack dispensed);
    }

    public record TriggerResult(Result result, ItemStack itemStack) {
        public static TriggerResult success(ItemStack itemStack) {
            return new TriggerResult(Result.SUCCESS, itemStack);
        }

        public static TriggerResult fail(ItemStack itemStack) {
            return new TriggerResult(Result.FAIL, itemStack);
        }

        public static TriggerResult pass() {
            return new TriggerResult(Result.PASS, ItemStack.EMPTY);
        }

        public boolean isSuccess() {
            return this.result == Result.SUCCESS;
        }

        public boolean isPass() {
            return this.result == Result.PASS;
        }

        public boolean isFail() {
            return this.result == Result.FAIL;
        }
    }

    public enum Result {
        SUCCESS,
        FAIL,
        PASS
    }
}