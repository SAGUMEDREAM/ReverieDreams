package cc.thonly.reverie_dreams.polymer.block;

import cc.thonly.reverie_dreams.polymer.block.model.TransparentFlatTripWire;
import cc.thonly.reverie_dreams.block.base.BaseFumoBlock;
import cc.thonly.reverie_dreams.block.entity.FumoBlockEntity;
import cc.thonly.reverie_dreams.registry.content.FumoTypes;
import eu.pb4.factorytools.api.block.FactoryBlock;
import eu.pb4.factorytools.api.block.model.generic.BSMMParticleBlock;
import eu.pb4.factorytools.api.virtualentity.ItemDisplayElementUtil;
import eu.pb4.factorytools.api.virtualentity.BlockModel;
import eu.pb4.polymer.blocks.api.PolymerTexturedBlock;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.attachment.BlockBoundAttachment;
import eu.pb4.polymer.virtualentity.api.attachment.HolderAttachment;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;

public class FumoImpl implements PolymerTexturedBlock, FactoryBlock, BSMMParticleBlock {
    public static final FumoImpl INSTANCE = new FumoImpl();

    private FumoImpl() {
    }

    @Override
    public BlockState getPolymerBlockState(BlockState state, PacketContext context) {
        return TransparentFlatTripWire.TRANSPARENT_FLAT_TRIPIWIRE;
    }

    @Override
    public @Nullable ElementHolder createElementHolder(ServerLevel world, BlockPos pos, BlockState initialBlockState) {
        return new Model(world, pos, initialBlockState);
    }

    @Override
    public boolean tickElementHolder(ServerLevel world, BlockPos pos, BlockState state) { return true; }

    public static final class Model extends BlockModel {
        private final Block block;
        private final ItemDisplayElement main;
        private final ServerLevel world;
        private final BlockPos pos;
        private FumoBlockEntity bound;
        private final Runnable growthUpdate = this::updateGrowth;

        public Model(ServerLevel world, BlockPos pos, BlockState state) {
            this.block = state.getBlock();
            this.world = world;
            this.pos = pos;
            this.main = ItemDisplayElementUtil.createSimple(state.getBlock().asItem().getDefaultInstance());
            this.main.setDisplaySize(this.getDisplaySizeWidth(), this.getDisplaySizeHeight());
            this.main.setScale(this.getScale());
            this.main.setItemDisplayContext(ItemDisplayContext.NONE);
            this.updateRotation(state);
            this.addElement(this.main);
            updateGrowth();
        }

        private void bind() {
            var chunk = world.getChunkSource().getChunkNow(pos.getX() >> 4, pos.getZ() >> 4);
            if (chunk != null && chunk.getBlockEntity(pos) instanceof FumoBlockEntity next && next != bound) {
                if (bound != null) bound.clearGrowthUpdate(growthUpdate);
                bound = next;
                bound.setGrowthUpdate(growthUpdate);
            }
        }

        private void updateGrowth() {
            Vector3f scale = getScale();
            main.setScale(scale);
            main.setOffset(new Vec3(0, (scale.y - 1) * .5, 0));
            main.setDisplaySize(Math.max(1, scale.x * 1.5f), Math.max(1, scale.y * 1.5f));
            // During construction the holder is not attached yet; publish only after binding.
            if (bound != null) main.tick();
        }

        @Override protected void onTick() {
            super.onTick();
            if (bound == null || bound.isRemoved()) bind();
        }

        private void updateRotation(BlockState state) {
            // The vanilla ItemDisplayRenderer adds a 180-degree Y rotation.
            // Compensate here instead of changing the placed block's direction.
            this.main.setYaw(state.getValue(BaseFumoBlock.FACING_16).getYaw() + 180.0f);
        }

        @Override
        public void notifyUpdate(HolderAttachment.UpdateType updateType) {
            bind();
            if (updateType == BlockBoundAttachment.BLOCK_STATE_UPDATE) {
                this.updateRotation(this.blockState());
                this.tick();
            }
            super.notifyUpdate(updateType);
        }

        public Vector3f getScale() {
            float growth = bound == null ? 1 : bound.getGrowthScale();
            if (this.block == FumoTypes.TAN_CIRNO.block()) {
                return new Vector3f(2f * growth);
            }
            return new Vector3f(growth);
        }

        @Override public void destroy() {
            if (bound != null) bound.clearGrowthUpdate(growthUpdate);
            bound = null;
            super.destroy();
        }

        public float getDisplaySizeWidth() {
            return 1f;
        }

        public float getDisplaySizeHeight() {
            return 1f;
        }
    }
}
