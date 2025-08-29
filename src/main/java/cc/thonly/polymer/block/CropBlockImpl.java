package cc.thonly.polymer.block;

import cc.thonly.polymer.block.model.TransparentFlatTripWire;
import cc.thonly.polymer.block.model.TransparentPlantWatterlogged;
import cc.thonly.reverie_dreams.block.PolymerCropCreator;
import cc.thonly.reverie_dreams.block.base.AbstractCropBlock;
import cc.thonly.reverie_dreams.util.CropAgeModelProvider;
import eu.pb4.factorytools.api.block.FactoryBlock;
import eu.pb4.factorytools.api.virtualentity.BlockModel;
import eu.pb4.factorytools.api.virtualentity.ItemDisplayElementUtil;
import eu.pb4.polymer.blocks.api.PolymerTexturedBlock;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.attachment.BlockBoundAttachment;
import eu.pb4.polymer.virtualentity.api.attachment.HolderAttachment;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.Getter;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.Waterloggable;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.Map;
import java.util.Optional;

public class CropBlockImpl implements PolymerTexturedBlock, FactoryBlock {
    private final AbstractCropBlock block;

    public CropBlockImpl(AbstractCropBlock block) {
        this.block = block;
    }

    @Override
    public BlockState getPolymerBreakEventBlockState(BlockState state, PacketContext context) {
        return Blocks.WHEAT.getDefaultState();
    }

    @Override
    public BlockState getPolymerBlockState(BlockState state, PacketContext context) {
        if (state.getBlock() instanceof Waterloggable) {
            return TransparentPlantWatterlogged.TRANSPARENT_WATTERLOGGED;
        }
        return TransparentFlatTripWire.TRANSPARENT_FLAT_TRIPIWIRE;
    }

    @Override
    public @Nullable ElementHolder createElementHolder(ServerWorld world, BlockPos pos, BlockState initialBlockState) {
        return new Model(world, pos, initialBlockState, this.block.getMaxAge());
    }

    @Getter
    public static class Model extends BlockModel {
        private static final Map<AbstractCropBlock, ItemStack[]> MODELS = new Object2ObjectOpenHashMap<>();
        private final ServerWorld world;
        private final BlockPos blockPos;
        private final BlockState blockState;
        private final AbstractCropBlock block;
        private final Integer maxAge;
        private final ItemStack[] models;
        private boolean isNormal = false;
        public ItemDisplayElement main;

        public Model(ServerWorld world, BlockPos blockPos, BlockState blockState, Integer maxAge) {
            this.world = world;
            this.blockPos = blockPos;
            this.blockState = blockState;
            this.maxAge = maxAge;
            Block block = blockState.getBlock();
            Optional<PolymerCropCreator.Instance> instanceOptional = PolymerCropCreator.getInstance(block);
            boolean isPresent = instanceOptional.isPresent();
            if (isPresent && block instanceof AbstractCropBlock cropBlock) {
                PolymerCropCreator.Instance instance = instanceOptional.get();
                this.block = (AbstractCropBlock) blockState.getBlock();
                this.models = MODELS.computeIfAbsent(cropBlock, (x) -> new ItemStack[this.maxAge]);
                Identifier identifier = instance.getIdentifier();
                String namespace = identifier.getNamespace();
                String path = identifier.getPath();
                for (int i = 0; i < this.maxAge; i++) {
                    Identifier modelId = Identifier.of(namespace, "block/" + path + "_stage" + i);
                    this.models[i] = ItemDisplayElementUtil.getModel(modelId);
                }
                this.isNormal = true;
                this.init(blockState);
            } else {
                this.block = null;
                this.models = MODELS.computeIfAbsent(null, (x) -> new ItemStack[1]);
            }
        }

        public void init(BlockState state) {
            this.main = ItemDisplayElementUtil.createSimple();
            updateItem(state);
            this.main.setScale(new Vector3f(1));
            this.addElement(main);
        }

        protected void updateItem(BlockState state) {
            int age = state.get(this.block.getAgeProperty());
            CropAgeModelProvider modelProvider = this.block.getModelProvider();
            this.main.setItem(modelProvider.get(this.models, age));
        }

        @Override
        public void notifyUpdate(HolderAttachment.UpdateType updateType) {
            if (this.isNormal && updateType == BlockBoundAttachment.BLOCK_STATE_UPDATE) {
                updateItem(this.blockState());
                this.tick();
            }
            super.notifyUpdate(updateType);
        }

        public ItemStack[] getModels() {
            return MODELS.get(this.block);
        }
    }
}
