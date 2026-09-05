package cc.thonly.reverie_dreams.polymer.block;

import eu.pb4.factorytools.api.block.FactoryBlock;
import eu.pb4.factorytools.api.block.model.generic.BSMMParticleBlock;
import eu.pb4.factorytools.api.block.model.generic.BlockStateModel;
import eu.pb4.factorytools.api.virtualentity.BlockModel;
import eu.pb4.polymer.blocks.api.BlockModelType;
import eu.pb4.polymer.blocks.api.PolymerBlockResourceUtils;
import eu.pb4.polymer.blocks.api.PolymerTexturedBlock;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.function.BiFunction;

public class BaseFactoryBlock implements FactoryBlock, PolymerTexturedBlock, BSMMParticleBlock {
    public static final BaseFactoryBlock BARRIER = new BaseFactoryBlock(Blocks.BARRIER. defaultBlockState(), false, BlockStateModel::longRange);
    public static final BaseFactoryBlock SAPLING = new BaseFactoryBlock(PolymerBlockResourceUtils.requestEmpty(BlockModelType.PLANT_BLOCK),false, BlockStateModel::midRange);
    public static final BaseFactoryBlock TRIPWIRE = new BaseFactoryBlock(PolymerBlockResourceUtils.requestEmpty(BlockModelType.TRIPWIRE_BLOCK),false, BlockStateModel::midRange);
    public static final BaseFactoryBlock TRIPWIRE_FLAT = new BaseFactoryBlock(PolymerBlockResourceUtils.requestEmpty(BlockModelType.TRIPWIRE_BLOCK_FLAT),false, BlockStateModel::midRange);

    private final BlockState clientState;
    private final boolean tick;
    private final BiFunction<BlockState, BlockPos, BlockModel> modelFunction;

    public BaseFactoryBlock(BlockState clientState, boolean tick, BiFunction<BlockState, BlockPos, BlockModel> modelFunction) {
        this.clientState = clientState;
        this.tick = tick;
        this.modelFunction = modelFunction;
    }

    public BlockState clientState() {
        return this.clientState;
    }

    public boolean tick() {
        return this.tick;
    }

    public BiFunction<BlockState, BlockPos, BlockModel> modelFunction() {
        return this.modelFunction;
    }

    @Override
    public BlockState getPolymerBlockState(BlockState state, PacketContext context) {
        return clientState;
    }

    @Override
    public @Nullable ElementHolder createElementHolder(ServerLevel world, BlockPos pos, BlockState initialBlockState) {
        return this.modelFunction.apply(initialBlockState, pos);
    }

    @Override
    public boolean tickElementHolder(ServerLevel world, BlockPos pos, BlockState initialBlockState) {
        return this.tick;
    }

    @Override
    public boolean isIgnoringBlockInteractionPlaySoundExceptedEntity(BlockState state, ServerPlayer player, InteractionHand hand, ItemStack stack, ServerLevel world, BlockHitResult blockHitResult) {
        return true;
    }
}
