package cc.thonly.reverie_dreams.polymer.block;

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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;

public class HorizontalFacingImpl implements FactoryBlock, PolymerTexturedBlock {
    private final Block block;

    public HorizontalFacingImpl(Block block) {
        this.block = block;
    }

    @Override
    public @Nullable ElementHolder createElementHolder(ServerLevel world, BlockPos pos, BlockState initialBlockState) {
        return new Model(world, pos, initialBlockState);
    }

    @Override
    public BlockState getPolymerBlockState(BlockState state, PacketContext context) {
        return Blocks.BARRIER.defaultBlockState();
    }

    public class Model extends BlockModel {
        private ItemDisplayElement main;

        public Model(ServerLevel world, BlockPos pos, BlockState state) {
            init(state);
        }

        public void init(BlockState state) {
            this.main = ItemDisplayElementUtil.createSimple(state.getBlock().asItem());
            Direction facing = state.getValue(AbstractKitchenwareBlock.FACING);
            float yaw = switch (facing) {
                case NORTH -> 180f;
                case EAST -> -90f;
                case SOUTH -> 0f;
                case WEST -> 90f;
                default -> 0f;
            } + 180f;
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
