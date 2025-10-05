package cc.thonly.mystias_izakaya.block.entity;

import cc.thonly.mystias_izakaya.block.ItemStackDisplay;
import cc.thonly.mystias_izakaya.block.MIBlockEntities;
import cc.thonly.polymer.block.ItemStackDisplayImpl;
import cc.thonly.reverie_dreams.recipe.ItemStackWrapper;
import com.google.gson.JsonElement;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Map;
import java.util.Optional;

@Setter
@Getter
public class ItemStackDisplayBlockEntity extends BlockEntity {
    private ItemStackWrapper item = ItemStackWrapper.empty();
    private double yaw = 0.0;

    public ItemStackDisplayBlockEntity(BlockPos pos, BlockState state) {
        super(MIBlockEntities.ITEM_DISPLAY_BLOCK_ENTITY, pos, state);
    }

    public void update() {
        if (!(this.getWorld() instanceof ServerWorld serverWorld)) {
            return;
        }
        Map<Long, ItemStackDisplayImpl.Model> longModelMap = ItemStackDisplayImpl.MAPPING.computeIfAbsent(serverWorld, w -> new Object2ObjectOpenHashMap<>());
        var model = longModelMap.get(this.getPos().asLong());
        if (!(this.getCachedState().getBlock() instanceof ItemStackDisplay)) {
            return;
        }
        if (model != null) {
            model.updateItem(this.getCachedState());
        }
    }

    public static void tick(World world, BlockPos pos, BlockState state, ItemStackDisplayBlockEntity blockEntity) {
        if (!(world instanceof ServerWorld serverWorld)) {
            return;
        }

        Map<Long, ItemStackDisplayImpl.Model> longModelMap = ItemStackDisplayImpl.MAPPING.computeIfAbsent(serverWorld, w -> new Object2ObjectOpenHashMap<>());
        var model = longModelMap.get(pos.asLong());
        if (model != null) {
            model.updateItem(state);
        }

    }

    @Override
    protected void readData(ReadView view) {
        super.readData(view);
        Optional<ItemStackWrapper> itemOptional = view.read("Item", ItemStackWrapper.CODEC);
        itemOptional.ifPresent(wrapper -> this.item = wrapper);
        this.yaw = view.getDouble("Yaw", 0);
    }

    @Override
    protected void writeData(WriteView view) {
        super.writeData(view);
        DataResult<JsonElement> dataResult = ItemStackWrapper.CODEC.encodeStart(JsonOps.INSTANCE, this.item);
        Optional<JsonElement> result = dataResult.result();
        if (result.isPresent()) {
            view.put("Item", ItemStackWrapper.CODEC, this.item);
        }
        view.putDouble("Yaw", this.yaw);
    }
}
