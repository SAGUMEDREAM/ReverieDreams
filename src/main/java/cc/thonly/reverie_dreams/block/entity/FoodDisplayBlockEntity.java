package cc.thonly.reverie_dreams.block.entity;

import cc.thonly.polymer.block.ItemStackDisplayImpl;
import cc.thonly.reverie_dreams.block.FoodDisplayBlock;
import cc.thonly.reverie_dreams.recipe.ItemStackWrapper;
import com.google.gson.JsonElement;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.Map;
import java.util.Optional;

@Setter
@Getter
public class FoodDisplayBlockEntity extends BlockEntity {
    private ItemStackWrapper item = ItemStackWrapper.empty();
    private double yaw = 0.0;

    public FoodDisplayBlockEntity(BlockPos pos, BlockState state) {
        super(RDBlockEntityTypes.ITEM_DISPLAY_BLOCK_ENTITY, pos, state);
    }

    public void update() {
        if (!(this.getLevel() instanceof ServerLevel serverWorld)) {
            return;
        }
        Map<Long, ItemStackDisplayImpl.Model> longModelMap = ItemStackDisplayImpl.MAPPING.computeIfAbsent(serverWorld, w -> new Object2ObjectOpenHashMap<>());
        var model = longModelMap.get(this.getBlockPos().asLong());
        if (!(this.getBlockState().getBlock() instanceof FoodDisplayBlock)) {
            return;
        }
        if (model != null) {
            model.updateItem(this.getBlockState());
        }
    }

    public static void tick(Level world, BlockPos pos, BlockState state, FoodDisplayBlockEntity blockEntity) {
        if (!(world instanceof ServerLevel serverWorld)) {
            return;
        }

        Map<Long, ItemStackDisplayImpl.Model> longModelMap = ItemStackDisplayImpl.MAPPING.computeIfAbsent(serverWorld, w -> new Object2ObjectOpenHashMap<>());
        var model = longModelMap.get(pos.asLong());
        if (model != null) {
            model.updateItem(state);
        }

    }

    @Override
    protected void loadAdditional(ValueInput view) {
        super.loadAdditional(view);
        Optional<ItemStackWrapper> itemOptional = view.read("Item", ItemStackWrapper.CODEC);
        itemOptional.ifPresent(wrapper -> this.item = wrapper);
        this.yaw = view.getDoubleOr("Yaw", 0);
    }

    @Override
    protected void saveAdditional(ValueOutput view) {
        super.saveAdditional(view);
        DataResult<JsonElement> dataResult = ItemStackWrapper.CODEC.encodeStart(JsonOps.INSTANCE, this.item);
        Optional<JsonElement> result = dataResult.result();
        if (result.isPresent()) {
            view.store("Item", ItemStackWrapper.CODEC, this.item);
        }
        view.putDouble("Yaw", this.yaw);
    }
}
