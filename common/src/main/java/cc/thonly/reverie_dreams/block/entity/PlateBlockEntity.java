package cc.thonly.reverie_dreams.block.entity;

import cc.thonly.reverie_dreams.item.IngredientStack;
import cc.thonly.reverie_dreams.proxy.ByModsPlatformProxy;
import cc.thonly.reverie_dreams.registry.content.block.entity.RDBlockEntityTypes;
import com.google.gson.JsonElement;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
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

@Setter
@Getter
public class PlateBlockEntity extends BlockEntity {
    private IngredientStack item = IngredientStack.empty();
    private float yaw = 0;

    public PlateBlockEntity(BlockPos pos, BlockState state) {
        super(RDBlockEntityTypes.FOOD_DISPLAY.value(), pos, state);
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
        itemOptional.ifPresent(wrapper -> this.item = wrapper);
        this.yaw = view.getFloatOr("Yaw", 0);
    }

    @Override
    protected void saveAdditional(ValueOutput view) {
        super.saveAdditional(view);
        DataResult<JsonElement> dataResult = IngredientStack.CODEC.encodeStart(JsonOps.INSTANCE, this.item);
        Optional<JsonElement> result = dataResult.result();
        if (result.isPresent()) {
            view.store("Item", IngredientStack.CODEC, this.item);
        }
        view.putDouble("Yaw", this.yaw);
    }

    @SuppressWarnings("DuplicatedCode")
    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        try (ProblemReporter.ScopedCollector logging = new ProblemReporter.ScopedCollector(this::toString, LogUtils.getLogger())) {
            TagValueOutput tagValue = TagValueOutput.createWithContext(logging, registries);
            this.saveAdditional(tagValue);
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
