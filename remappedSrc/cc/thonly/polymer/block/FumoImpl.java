package cc.thonly.polymer.block;

import cc.thonly.polymer.block.model.TransparentFlatTripWire;
import cc.thonly.polymer.block.model.TransparentPlant;
import cc.thonly.polymer.block.model.TransparentTripWire;
import cc.thonly.reverie_dreams.block.BaseFumoBlock;
import cc.thonly.reverie_dreams.fumo.Fumos;
import eu.pb4.factorytools.api.block.FactoryBlock;
import eu.pb4.factorytools.api.virtualentity.ItemDisplayElementUtil;
import eu.pb4.polymer.blocks.api.PolymerTexturedBlock;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import xyz.nucleoid.packettweaker.PacketContext;

public class FumoImpl implements PolymerTexturedBlock, FactoryBlock {
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
        return new Model(initialBlockState, this.fumoBlock.getOffsets());
    }

    public static final class Model extends ElementHolder {
        private final Block block;
        private final ItemDisplayElement main;

        public Model(BlockState state, Vec3 offsets) {
            this.block = state.getBlock();
            this.main = ItemDisplayElementUtil.createSimple(state.getBlock().asItem());
            this.main.setDisplaySize(this.getDisplaySizeWidth(), this.getDisplaySizeHeight());
            this.main.setOffset(this.modifyOffset(offsets));
            this.main.setScale(this.getScale());
            this.main.setItemDisplayContext(ItemDisplayContext.NONE);
            var yaw = state.getValue(BaseFumoBlock.FACING_16).getYaw();
            this.main.setYaw(yaw);
            this.addElement(this.main);
        }

        public Vec3 modifyOffset(Vec3 offsets) {
            if (this.block == Fumos.TAN_CIRNO.block()) {
                return offsets.add(new Vec3(0, 0.5, 0));
            }
            return offsets;
        }

        public Vector3f getScale() {
            if (this.block == Fumos.TAN_CIRNO.block()) {
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
