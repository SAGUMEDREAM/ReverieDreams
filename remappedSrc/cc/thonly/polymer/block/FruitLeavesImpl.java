package cc.thonly.polymer.block;

import cc.thonly.reverie_dreams.block.base.FruitLeavesBlock;
import eu.pb4.factorytools.api.block.FactoryBlock;
import eu.pb4.factorytools.api.virtualentity.BlockModel;
import eu.pb4.factorytools.api.virtualentity.ItemDisplayElementUtil;
import eu.pb4.polymer.blocks.api.PolymerTexturedBlock;
import eu.pb4.polymer.core.api.block.PolymerBlock;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.attachment.BlockBoundAttachment;
import eu.pb4.polymer.virtualentity.api.attachment.HolderAttachment;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import xyz.nucleoid.packettweaker.PacketContext;

public class FruitLeavesImpl implements PolymerBlock, PolymerTexturedBlock, FactoryBlock {
    private final FruitLeavesBlock full;

    public FruitLeavesImpl(FruitLeavesBlock block) {
        this.full = block;
    }

    @Override
    public BlockState getPolymerBreakEventBlockState(BlockState state, PacketContext context) {
        return Blocks.OAK_LEAVES.defaultBlockState();
    }

    @Override
    public BlockState getPolymerBlockState(BlockState state, PacketContext context) {
        return Blocks.BARRIER.defaultBlockState();
    }

    @Override
    public @Nullable ElementHolder createElementHolder(ServerLevel world, BlockPos pos, BlockState initialBlockState) {
        return new Model(world, pos, initialBlockState);
    }

    @Getter
    public class Model extends BlockModel {
        private final ServerLevel world;
        private final BlockPos blockPos;
        private final BlockState state;
        private final Block empty;
        private ItemDisplayElement main;
        private final ResourceLocation defaultId;
        private final ResourceLocation modelId;

        public Model(ServerLevel world, BlockPos blockPos, BlockState state) {
            this.world = world;
            this.blockPos = blockPos;
            this.empty = FruitLeavesImpl.this.full.getEmptyLeavesBlock();
            this.state = state;
            ResourceLocation identifier = BuiltInRegistries.BLOCK.getKey(FruitLeavesImpl.this.full);
            ResourceLocation emptyId = BuiltInRegistries.BLOCK.getKey(this.empty);
            this.defaultId = ResourceLocation.fromNamespaceAndPath(emptyId.getNamespace(), "block/" + emptyId.getPath());
            this.modelId = ResourceLocation.fromNamespaceAndPath(identifier.getNamespace(), "block/" + identifier.getPath());
            this.init(state);
        }

        public void init(BlockState state) {
            this.main = ItemDisplayElementUtil.createSimple();
            this.update(state);
        }

        public void update(BlockState state) {
            this.removeElement(this.main);
            this.main = this.getElement(state);
            this.main.setScale(new Vector3f(1));
            this.addElement(this.main);
        }

        @Override
        public void notifyUpdate(HolderAttachment.UpdateType updateType) {
            if (updateType == BlockBoundAttachment.BLOCK_STATE_UPDATE) {
                update(this.blockState());
                this.tick();
            }
            super.notifyUpdate(updateType);
        }

        public ItemDisplayElement getElement(BlockState state) {
            int age = state.getValue(FruitLeavesBlock.AGE_PROPERTY);
            return age <= 2 ? new ItemDisplayElement(ItemDisplayElementUtil.getModel(this.defaultId)) : new ItemDisplayElement(ItemDisplayElementUtil.getModel(this.modelId));
        }
    }
}
