package cc.thonly.mystias_izakaya.block.entity;

import cc.thonly.mystias_izakaya.block.ItemStackDisplay;
import cc.thonly.mystias_izakaya.block.MIBlockEntities;
import cc.thonly.reverie_dreams.recipe.ItemStackRecipeWrapper;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import vectorwing.farmersdelight.common.item.component.ItemStackWrapper;

import java.util.Optional;

@Setter
@Getter
public class ItemStackDisplayBlockEntity extends BlockEntity {
    private ItemStackRecipeWrapper item = ItemStackRecipeWrapper.empty();
    private double yaw = 0.0;
    private int tick = 0;

    public ItemStackDisplayBlockEntity(BlockPos pos, BlockState state) {
        super(MIBlockEntities.ITEM_DISPLAY_BLOCK_ENTITY, pos, state);
    }

    public static void tick(World world, BlockPos pos, BlockState state, ItemStackDisplayBlockEntity blockEntity) {
        if (blockEntity.tick > 5) {
            var model = ItemStackDisplay.POS_TO_MODEL.get(pos.asLong());
            if (model != null) {
                model.updateItem(state);
            }
            blockEntity.tick = 0;
        }
        blockEntity.tick++;
    }

    @Override
    protected void readData(ReadView view) {
        super.readData(view);
        Optional<ItemStackRecipeWrapper> itemOptional = view.read("Item", ItemStackRecipeWrapper.CODEC);
        itemOptional.ifPresent(wrapper -> this.item = wrapper);
        this.yaw = view.getDouble("Yaw", 0);
    }

    @Override
    protected void writeData(WriteView view) {
        super.writeData(view);
        DataResult<JsonElement> dataResult = ItemStackRecipeWrapper.CODEC.encodeStart(JsonOps.INSTANCE, this.item);
        Optional<JsonElement> result = dataResult.result();
        if (result.isPresent()) {
            view.put("Item", ItemStackRecipeWrapper.CODEC, this.item);
        }
        view.putDouble("Yaw", this.yaw);
    }
}
