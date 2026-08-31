package cc.thonly.reverie_dreams.block.entity;

import cc.thonly.reverie_dreams.registry.SerializableProvider;
import cc.thonly.reverie_dreams.registry.content.block.entity.RDBlockEntityTypes;
import cc.thonly.reverie_dreams.registry.tag.RDItemTags;
import cc.thonly.reverie_dreams.util.DistributedTickTask;
import cc.thonly.reverie_dreams.util.math.ModMth;
import cc.thonly.reverie_dreams.util.sound.SoundEventPlayUtils;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class IceMakingMachineBlockEntity extends BlockEntity {
    public static final double NORMAL_EFFICIENCY = 1.0;
    @Getter
    private double efficiency = NORMAL_EFFICIENCY;
    @Getter
    private final List<ItemStack> output = new ArrayList<>();

    private int tick = 0;

    private final DistributedTickTask tickTask;

    public IceMakingMachineBlockEntity(
            BlockPos worldPosition,
            BlockState blockState
    ) {
        super(RDBlockEntityTypes.ICE_MAKING_MACHINE.get(), worldPosition, blockState);

        this.tickTask = DistributedTickTask.createTickTask(() -> {
            if (this.level == null) {
                return;
            }

            RandomSource random = this.level.getRandom();
            SoundEventPlayUtils.playSound(
                    this.level,
                    this.getBlockPos(),
                    random.nextBoolean() ? SoundEvents.GLASS_PLACE : SoundEvents.GLASS_BREAK,
                    SoundSource.BLOCKS
            );
        }, 5);
    }

    public static void onBlockEntityTick(
            Level level,
            BlockPos pos,
            BlockState state,
            IceMakingMachineBlockEntity blockEntity
    ) {
        if (level.isClientSide()) {
            return;
        }

        if (blockEntity.tick <= 0) {
            return;
        }

        blockEntity.tick--;
        blockEntity.tickTask.tick();

        if (blockEntity.tick <= 0) {
            blockEntity.finished();
            blockEntity.setChanged();
        }
    }

    private void updateComparator() {
        if (this.level == null) {
            return;
        }

        this.level.updateNeighbourForOutputSignal(
                this.worldPosition,
                this.getBlockState().getBlock()
        );
    }

    public Optional<ItemStack> take() {
        if (this.output.isEmpty()) {
            return Optional.empty();
        }

        ItemStack result = this.output.removeFirst();

        this.setChanged();
        this.updateComparator();

        return Optional.of(result);
    }

    public Optional<ItemStack> takeLast() {
        if (this.output.isEmpty()) {
            return Optional.empty();
        }

        ItemStack result = this.output.removeLast();

        this.setChanged();
        this.updateComparator();

        return Optional.of(result);
    }

    public List<ItemStack> takeAll() {
        if (this.output.isEmpty()) {
            return List.of();
        }

        List<ItemStack> result = List.copyOf(this.output);

        this.output.clear();
        this.setChanged();
        this.updateComparator();

        return result;
    }

    public boolean start() {
        if (!this.canStart()) {
            return false;
        }

        this.tick = 20 * 8;
        this.setChanged();
        return true;
    }

    public boolean canStart() {
        return this.tick <= 0 && this.output.isEmpty();
    }

    public boolean tryStart(ItemStack itemStack) {
        if (!itemStack.is(Items.WATER_BUCKET)) {
            return false;
        }

        if (!this.canStart()) {
            return false;
        }

        this.start();
        return true;
    }

    public void finished() {
        if (this.level == null) {
            return;
        }

        RegistryAccess registryAccess = this.level.registryAccess();
        Registry<Item> items = registryAccess.lookupOrThrow(Registries.ITEM);
        List<Holder<Item>> list = ModMth.toList(
                items.getTagOrEmpty(RDItemTags.ICE_MAKING_MACHINE_OUTPUT)
        );

        if (list.isEmpty()) {
            return;
        }

        List<ItemStack> output = new ArrayList<>();

        for (int i = 0; i < this.efficiencyToSize(); i++) {
            Holder<Item> itemHolder = ModMth.getRandomElement(
                    this.level.getRandom(),
                    list
            );
            output.add(new ItemStack(itemHolder));
        }

        this.output.clear();
        this.output.addAll(output);

        this.setChanged();
        this.updateComparator();
    }

    public int efficiencyToSize() {
        return (int) (this.efficiency * 8);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);

        output.putDouble("Efficiency", this.efficiency);
        output.putInt("Tick", this.tick);

        ValueOutput.TypedOutputList<ItemStack> list = output.list("Output", SerializableProvider.ITEM_STACK_CODEC);

        for (ItemStack itemStack : this.output) {
            if (itemStack.isEmpty()) {
                continue;
            }

            list.add(itemStack);
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);

        this.efficiency = input.getDoubleOr("Efficiency", NORMAL_EFFICIENCY);
        this.tick = input.getIntOr("Tick", 0);
        this.output.clear();

        for (ItemStack itemStack : input.listOrEmpty("Output", SerializableProvider.ITEM_STACK_CODEC)) {
            if (itemStack.isEmpty()) {
                continue;
            }

            this.output.add(itemStack);
        }
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}