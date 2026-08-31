package cc.thonly.reverie_dreams.block.entity;

import cc.thonly.reverie_dreams.inventory.InfiniteInventory;
import cc.thonly.reverie_dreams.registry.content.block.entity.RDBlockEntityTypes;
import cc.thonly.reverie_dreams.util.InfiniteInventoryBlockEntity;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

public class CupboardBlockEntity extends BlockEntity implements InfiniteInventoryBlockEntity {
    @Getter
    private final InfiniteInventory inventory = new InfiniteInventory(36);

    public CupboardBlockEntity(BlockPos worldPosition, BlockState blockState) {
        super(RDBlockEntityTypes.CUPBOARD.get(), worldPosition, blockState);
    }

    public static void onBlockEntityTick(Level level, BlockPos pos, BlockState state, CupboardBlockEntity blockEntity) {

    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void saveAdditional(ValueOutput view) {
        super.saveAdditional(view);
        InfiniteInventory.saveAllItems(view, this.inventory);
    }

    @Override
    public void loadAdditional(ValueInput view) {
        super.loadAdditional(view);
        InfiniteInventory.loadAllItems(view, this.inventory);
    }
}
