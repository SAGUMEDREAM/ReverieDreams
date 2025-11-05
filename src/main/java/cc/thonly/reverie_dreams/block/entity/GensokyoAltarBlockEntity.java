package cc.thonly.reverie_dreams.block.entity;

import cc.thonly.polymer.block.GensokyoAltarImpl;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

@Getter
public class GensokyoAltarBlockEntity extends BlockEntity {
    private SimpleContainer inventory = new SimpleContainer(9);
    public int tick = 0;

    public GensokyoAltarBlockEntity(BlockPos pos, BlockState state) {
        super(RDBlockEntityTypes.GENSOKYO_ALTAR_BLOCK_ENTITY, pos, state);
    }

    public static void tick(Level world, BlockPos pos, BlockState state, GensokyoAltarBlockEntity blockEntity) {
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
    protected void saveAdditional(ValueOutput view) {
        super.saveAdditional(view);
        ContainerHelper.saveAllItems(view, inventory.items);
    }

    @Override
    protected void loadAdditional(ValueInput view) {
        super.loadAdditional(view);
        SimpleContainer inventory = new SimpleContainer(9);
        ContainerHelper.loadAllItems(view, inventory.items);
        this.inventory = inventory;
    }

}
