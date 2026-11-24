package cc.thonly.reverie_dreams.polymer.block;

import cc.thonly.reverie_dreams.polymer.block.model.TransparentFlatTripWire;
import cc.thonly.reverie_dreams.block.cooking.AbstractKitchenwareBlock;
import eu.pb4.factorytools.api.block.FactoryBlock;
import eu.pb4.factorytools.api.virtualentity.BlockModel;
import eu.pb4.factorytools.api.virtualentity.ItemDisplayElementUtil;
import eu.pb4.polymer.blocks.api.PolymerTexturedBlock;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.attachment.BlockBoundAttachment;
import eu.pb4.polymer.virtualentity.api.attachment.HolderAttachment;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class AbstractKitchenwareImpl implements FactoryBlock, PolymerTexturedBlock, TransparentFlatTripWire {
    private final AbstractKitchenwareBlock block;
    public AbstractKitchenwareImpl(AbstractKitchenwareBlock block) {
        this.block = block;
    }

    @Override
    public @Nullable ElementHolder createElementHolder(ServerLevel world, BlockPos pos, BlockState initialBlockState) {
        return new Model(world, pos, initialBlockState);
    }

    public class Model extends BlockModel {
        private final ItemDisplayElement main;

        public Model(ServerLevel world, BlockPos pos, BlockState state) {
            this.main = ItemDisplayElementUtil.createSimple(state.getBlock().asItem());
            this.main.setItemDisplayContext(ItemDisplayContext.NONE);
            this.main.setOffset(AbstractKitchenwareImpl.this.block.getOffset());
            this.main.setScale(AbstractKitchenwareImpl.this.block.getScale());
            updateFacing(state);
            addElement(this.main);
        }

        @SuppressWarnings("deprecation")
        private void updateFacing(BlockState state) {
            Direction facing = state.getValue(AbstractKitchenwareBlock.FACING);
            float yaw = switch (facing) {
                case NORTH -> 180f;
                case EAST -> -90f;
                case SOUTH -> 0f;
                case WEST -> 90f;
                default -> 0f;
            };
            // Item displays render their model with an additional 180-degree Y rotation.
            this.main.setYaw(yaw);
        }

        @Override
        public void notifyUpdate(HolderAttachment.UpdateType updateType) {
            if (updateType == BlockBoundAttachment.BLOCK_STATE_UPDATE) {
                updateFacing(this.blockState());
                tick();
            }
            super.notifyUpdate(updateType);
        }
    }
}
