package cc.thonly.polymer.block;

import cc.thonly.reverie_dreams.block.ModelBlock;
import eu.pb4.factorytools.api.block.FactoryBlock;
import eu.pb4.factorytools.api.block.model.generic.BSMMParticleBlock;
import eu.pb4.factorytools.api.virtualentity.BlockModel;
import eu.pb4.factorytools.api.virtualentity.ItemDisplayElementUtil;
import eu.pb4.polymer.blocks.api.PolymerTexturedBlock;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import lombok.Getter;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

public class ModelFactoryImpl implements PolymerTexturedBlock, FactoryBlock, BSMMParticleBlock {
    private final ModelBlock block;

    public ModelFactoryImpl(ModelBlock block) {
        this.block = block;
    }

    @Override
    public BlockState getPolymerBlockState(BlockState state, PacketContext context) {
        return Blocks.BARRIER.getDefaultState();
    }

    @Override
    public @Nullable ElementHolder createElementHolder(ServerWorld world, BlockPos pos, BlockState initialBlockState) {
        return new Model(world, pos, initialBlockState, this.block);
    }

    @Getter
    public static final class Model extends BlockModel {
        private final ModelBlock block;
        private final ItemDisplayElement main;

        public Model(ServerWorld world, BlockPos pos, BlockState initialBlockState, ModelBlock block) {
            this.block = block;
            this.main = ItemDisplayElementUtil.createSimple(block.asItem());
            this.main.setItemDisplayContext(ItemDisplayContext.NONE);
            var yaw = initialBlockState.get(ModelBlock.FACING_16).getYaw();
            this.main.setYaw(yaw);
            this.addElement(this.main);
        }
    }
}
