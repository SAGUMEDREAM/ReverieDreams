package cc.thonly.reverie_dreams.block.entity;

import cc.thonly.reverie_dreams.item.IngredientStack;
import cc.thonly.reverie_dreams.proxy.ByModsPlatformProxy;
import cc.thonly.reverie_dreams.registry.content.block.entity.RDBlockEntityTypes;
import com.mojang.logging.LogUtils;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;

@Setter
@Getter
public class PlateBlockEntity extends BlockEntity {
    private IngredientStack item = IngredientStack.empty();
    @Setter(lombok.AccessLevel.NONE)
    private @Nullable UUID restaurantBell;
    @Setter(lombok.AccessLevel.NONE)
    private @Nullable UUID preparedFor;
    private float yaw = 0;

    public PlateBlockEntity(BlockPos pos, BlockState state) {
        super(RDBlockEntityTypes.PLATE.value(), pos, state);
    }

    /**
     * Every writer, including NPC goals, publishes the same persistent and visual state.
     */
    public void setItem(IngredientStack item) {
        setRestaurantDish(item, null, null);
    }

    public void setRestaurantDish(IngredientStack item, @Nullable UUID bell, @Nullable UUID customer) {
        this.item = normalizeContents(item);
        this.restaurantBell = this.item.isEmpty() ? null : bell;
        this.preparedFor = this.restaurantBell == null ? null : customer;
        publishChanges();
    }

    public boolean hasRestaurantDish() {
        return restaurantBell != null;
    }

    public boolean isUnassignedDish(UUID bell) {
        return bell.equals(restaurantBell) && preparedFor == null;
    }

    public boolean canTakeDish(UUID bell, UUID customer) {
        return restaurantBell == null || (bell.equals(restaurantBell) && customer.equals(preparedFor));
    }

    public void releaseDish(UUID customer) {
        if (customer.equals(preparedFor)) {
            preparedFor = null;
            setChanged();
        }
    }

    public void setYaw(float yaw) {
        float next = Float.isFinite(yaw) ? yaw : 0;
        if (this.yaw == next)
            return;
        this.yaw = next;
        publishChanges();
    }

    private static IngredientStack normalizeContents(IngredientStack item) {
        return item.build().isEmpty() ? IngredientStack.empty() : item.copy();
    }

    private void publishChanges() {
        this.setChanged();
        if (this.level instanceof ServerLevel server) {
            server.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
            this.update();
        }
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        if (this.level instanceof ServerLevel && !this.item.isEmpty()) {
            var stack = this.item.build();
            this.item = IngredientStack.empty();
            Block.popResource(this.level, pos, stack);
        }
        super.preRemoveSideEffects(pos, state);
    }

    public void update() {
        ByModsPlatformProxy.PLATE_BLOCK_ENTITY_UPDATER.map(method -> {
            method.handle(this);
            return null;
        });
    }

    public static void tick(Level world, BlockPos pos, BlockState state, PlateBlockEntity blockEntity) {
        ByModsPlatformProxy.PLATE_BLOCK_ENTITY_TICKER.map(method -> {
            method.handle(world, pos, state, blockEntity);
            return null;
        });
    }

    @Override
    protected void loadAdditional(ValueInput view) {
        super.loadAdditional(view);
        Optional<IngredientStack> itemOptional = view.read("Item", IngredientStack.CODEC);
        this.item = normalizeContents(itemOptional.orElseGet(IngredientStack::empty));
        this.restaurantBell = this.item.isEmpty() ? null
                : RestaurantBlockEntity.uuid(view.getStringOr("RestaurantBell", ""), null);
        this.preparedFor = this.restaurantBell == null ? null
                : RestaurantBlockEntity.uuid(view.getStringOr("RestaurantCustomer", ""), null);
        this.yaw = view.getFloatOr("Yaw", 0);
        if (!Float.isFinite(this.yaw))
            this.yaw = 0;
    }

    @Override
    protected void saveAdditional(ValueOutput view) {
        super.saveAdditional(view);
        writeContents(view);
        if (restaurantBell != null)
            view.putString("RestaurantBell", restaurantBell.toString());
        if (preparedFor != null)
            view.putString("RestaurantCustomer", preparedFor.toString());
    }

    private void writeContents(ValueOutput view) {
        view.store("Item", IngredientStack.CODEC, this.item);
        view.putFloat("Yaw", this.yaw);
    }

    @SuppressWarnings("DuplicatedCode")
    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        try (ProblemReporter.ScopedCollector logging = new ProblemReporter.ScopedCollector(this::toString, LogUtils.getLogger())) {
            TagValueOutput tagValue = TagValueOutput.createWithContext(logging, registries);
            writeContents(tagValue);
            return tagValue.buildResult();
        } catch (Exception any) {
            return super.getUpdateTag(registries);
        }
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
