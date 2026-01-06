package cc.thonly.polymer.block;

import cc.thonly.polymer.block.model.TransparentFlatTripWire;
import cc.thonly.reverie_dreams.block.kitchen.AbstractKitchenwareBlock;
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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public interface AbstractKitchenwareInf extends FactoryBlock, PolymerTexturedBlock, TransparentFlatTripWire {

    @Override
    public @Nullable
    default ElementHolder createElementHolder(ServerLevel world, BlockPos pos, BlockState initialBlockState) {
        return new Model(world, pos, initialBlockState);
    }

    public class Model extends BlockModel {
        private ItemDisplayElement main;

        public Model(ServerLevel world, BlockPos pos, BlockState state) {
            init(state);
        }

        public void init(BlockState state) {
            Block block = state.getBlock();
            this.main = ItemDisplayElementUtil.createSimple(block.asItem());
            Direction facing = state.getValue(AbstractKitchenwareBlock.FACING);
            float yaw = switch (facing) {
                case NORTH -> 180f;
                case EAST -> -90f;
                case SOUTH -> 0f;
                case WEST -> 90f;
                default -> 0f;
            };
            if (block instanceof AbstractKitchenwareBlock kitchenwareBlock) {
                main.setOffset(kitchenwareBlock.getOffset());
                main.setScale(kitchenwareBlock.getScale());
            }
            this.main.setYaw(yaw);
            addElement(this.main);
        }

        private void updateItem(BlockState state) {
            this.removeElement(this.main);
            init(state);
        }

        @Override
        public void notifyUpdate(HolderAttachment.UpdateType updateType) {
            if (updateType == BlockBoundAttachment.BLOCK_STATE_UPDATE) {
                updateItem(this.blockState());
            }
            super.notifyUpdate(updateType);
        }
    }
}
