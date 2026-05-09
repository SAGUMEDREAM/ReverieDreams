package cc.thonly.reverie_dreams.fabric.polymer.block;

import cc.thonly.reverie_dreams.fabric.polymer.block.model.TransparentFlatTripWire;
import cc.thonly.reverie_dreams.block.base.BaseFumoBlock;
import cc.thonly.reverie_dreams.registry.content.FumoTypes;
import eu.pb4.factorytools.api.block.FactoryBlock;
import eu.pb4.factorytools.api.block.model.generic.BSMMParticleBlock;
import eu.pb4.factorytools.api.virtualentity.ItemDisplayElementUtil;
import eu.pb4.polymer.blocks.api.PolymerTexturedBlock;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;

public class FumoImpl implements PolymerTexturedBlock, FactoryBlock, BSMMParticleBlock {
    private final BaseFumoBlock fumoBlock;

    public FumoImpl(BaseFumoBlock fumoBlock) {
        this.fumoBlock = fumoBlock;
    }

    @Override
    public BlockState getPolymerBlockState(BlockState state, PacketContext context) {
        return TransparentFlatTripWire.TRANSPARENT_FLAT_TRIPIWIRE;
    }

    @Override
    public @Nullable ElementHolder createElementHolder(ServerLevel world, BlockPos pos, BlockState initialBlockState) {
        return new Model(initialBlockState);
    }

    public static final class Model extends ElementHolder {
        private final Block block;
        private final ItemDisplayElement main;

        public Model(BlockState state) {
            this.block = state.getBlock();
            this.main = ItemDisplayElementUtil.createSimple(state.getBlock().asItem().getDefaultInstance());
            this.main.setDisplaySize(this.getDisplaySizeWidth(), this.getDisplaySizeHeight());
            this.main.setScale(this.getScale());
            this.main.setItemDisplayContext(ItemDisplayContext.NONE);
            var yaw = state.getValue(BaseFumoBlock.FACING_16).getYaw();
            this.main.setYaw(yaw);
            this.addElement(this.main);
        }

        public Vector3f getScale() {
            if (this.block == FumoTypes.TAN_CIRNO.block()) {
                return new Vector3f(2f);
            }
            return new Vector3f(1f);
        }

        public float getDisplaySizeWidth() {
            return 1f;
        }

        public float getDisplaySizeHeight() {
            return 1f;
        }
    }
}
