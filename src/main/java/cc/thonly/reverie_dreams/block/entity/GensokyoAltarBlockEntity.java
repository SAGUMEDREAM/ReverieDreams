package cc.thonly.reverie_dreams.block.entity;

import cc.thonly.polymer.block.GensokyoAltarImpl;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

@Getter
public class GensokyoAltarBlockEntity extends BlockEntity {
    private SimpleContainer inventory = new SimpleContainer(9);
    public int tick = 0;

    public GensokyoAltarBlockEntity(BlockPos pos, BlockState state) {
        super(RDBlockEntityTypes.GENSOKYO_ALTAR_BLOCK_ENTITY, pos, state);
    }

    public static void tick(Level world, BlockPos pos, BlockState state, GensokyoAltarBlockEntity blockEntity) {
        if (blockEntity.tick > 5) {
            GensokyoAltarImpl.Model altarModel = GensokyoAltarImpl.POS_TO_MODEL.get(pos.asLong());
            if (altarModel != null) {
                altarModel.update();
            }
            blockEntity.tick = 0;
        }
        GensokyoAltarImpl.Model altarModel = GensokyoAltarImpl.POS_TO_MODEL.get(pos.asLong());
        if (altarModel != null) {
            altarModel.angle += 2f;
            if (altarModel.angle >= 360) {
                altarModel.angle = 0;
            }
        }
        blockEntity.tick++;
    }

    @Override
    protected void saveAdditional(CompoundTag compoundTag, HolderLookup.Provider provider) {
        super.saveAdditional(compoundTag, provider);
        ContainerHelper.saveAllItems(compoundTag, inventory.items, provider);
    }

    @Override
    protected void loadAdditional(CompoundTag compoundTag, HolderLookup.Provider provider) {
        super.loadAdditional(compoundTag, provider);
        SimpleContainer inventory = new SimpleContainer(9);
        ContainerHelper.loadAllItems(compoundTag, inventory.items, provider);
        this.inventory = inventory;
    }

}
