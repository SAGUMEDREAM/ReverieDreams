package cc.thonly.reverie_dreams.block.entity;

import cc.thonly.polymer.block.GensokyoAltarImpl;
import lombok.Getter;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Getter
public class GensokyoAltarBlockEntity extends BlockEntity {
    private SimpleInventory inventory = new SimpleInventory(9);
    public int tick = 0;

    public GensokyoAltarBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.GENSOKYO_ALTAR_BLOCK_ENTITY, pos, state);
    }

    public static void tick(World world, BlockPos pos, BlockState state, GensokyoAltarBlockEntity blockEntity) {
        if (blockEntity.tick > 5) {
            GensokyoAltarImpl.Model altarModel = GensokyoAltarImpl.STATE_TO_MODEL.get(state);
            if (altarModel != null) {
                altarModel.update();
            }
            blockEntity.tick = 0;
        }
        GensokyoAltarImpl.Model altarModel = GensokyoAltarImpl.STATE_TO_MODEL.get(state);
        if (altarModel != null) {
            altarModel.angle += 2f;
            if (altarModel.angle >= 360) {
                altarModel.angle = 0;
            }
        }
        blockEntity.tick++;
    }

    @Override
    protected void writeData(WriteView view) {
        super.writeData(view);
        Inventories.writeData(view, inventory.heldStacks);
    }

    @Override
    protected void readData(ReadView view) {
        super.readData(view);
        SimpleInventory inventory = new SimpleInventory(9);
        Inventories.readData(view, inventory.heldStacks);
        this.inventory = inventory;
    }

}
