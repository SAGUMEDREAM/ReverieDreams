package cc.thonly.reverie_dreams.block.base;

import cc.thonly.reverie_dreams.state.RDBlockStateTemplates;
import cc.thonly.reverie_dreams.state.SixteenDirection;
import com.mojang.serialization.MapCodec;
import eu.pb4.factorytools.api.block.FactoryBlock;
import eu.pb4.factorytools.api.virtualentity.BlockModel;
import eu.pb4.factorytools.api.virtualentity.ItemDisplayElementUtil;
import eu.pb4.polymer.blocks.api.PolymerTexturedBlock;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

public class ModelBlock extends HorizontalDirectionalBlock implements PolymerTexturedBlock, FactoryBlock {
    public static final MapCodec<ModelBlock> CODEC = simpleCodec(ModelBlock::new);
    public static final EnumProperty<SixteenDirection> FACING_16 = RDBlockStateTemplates.FACING_16;

    public ModelBlock(Properties settings) {
        super(settings);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING_16, SixteenDirection.NORTH));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        double yaw = ctx.getRotation();
        SixteenDirection direction = SixteenDirection.fromYaw(yaw);
        return this.defaultBlockState().setValue(FACING_16, direction);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING_16);
    }

    @Override
    public MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockState getPolymerBlockState(BlockState state, PacketContext context) {
        return Blocks.BARRIER.defaultBlockState();
    }

    @Override
    public @Nullable ElementHolder createElementHolder(ServerLevel world, BlockPos pos, BlockState initialBlockState) {
        return new Model(world, pos, initialBlockState, this);
    }

    @Getter
    public static final class Model extends BlockModel {
        private final ModelBlock block;
        private final ItemDisplayElement main;

        public Model(ServerLevel world, BlockPos pos, BlockState initialBlockState, ModelBlock block) {
            this.block = block;
            this.main = ItemDisplayElementUtil.createSimple(block.asItem());
            this.main.setModelTransformation(ItemDisplayContext.NONE);
            var yaw = initialBlockState.getValue(ModelBlock.FACING_16).getYaw();
            this.main.setYaw(yaw);
            this.addElement(this.main);
        }
    }
}
