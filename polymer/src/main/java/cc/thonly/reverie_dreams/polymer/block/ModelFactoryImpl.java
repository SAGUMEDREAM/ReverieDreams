package cc.thonly.reverie_dreams.polymer.block;

import cc.thonly.reverie_dreams.block.base.ModelBlock;
import eu.pb4.factorytools.api.block.FactoryBlock;
import eu.pb4.factorytools.api.block.model.generic.BSMMParticleBlock;
import eu.pb4.factorytools.api.virtualentity.BlockModel;
import eu.pb4.factorytools.api.virtualentity.ItemDisplayElementUtil;
import eu.pb4.polymer.blocks.api.PolymerTexturedBlock;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

public class ModelFactoryImpl implements PolymerTexturedBlock, FactoryBlock, BSMMParticleBlock {
    private final ModelBlock block;

    public ModelFactoryImpl(ModelBlock block) {
        this.block = block;
    }

    @Override
    public BlockState getPolymerBlockState(BlockState state, PacketContext context) {
        return Blocks.BARRIER.defaultBlockState();
    }

    @Override
    public @Nullable ElementHolder createElementHolder(ServerLevel world, BlockPos pos, BlockState initialBlockState) {
        return new Model(world, pos, initialBlockState, this.block);
    }

    @Getter
    public static final class Model extends BlockModel {
        private final ModelBlock block;
        private final ItemDisplayElement main;

        public Model(ServerLevel world, BlockPos pos, BlockState initialBlockState, ModelBlock block) {
            this.block = block;
            this.main = ItemDisplayElementUtil.createSimple(block.asItem());
            this.main.setItemDisplayContext(ItemDisplayContext.NONE);
            var yaw = initialBlockState.getValue(ModelBlock.FACING_16).getYaw();
            this.main.setYaw(yaw);
            this.addElement(this.main);
        }
    }
}
