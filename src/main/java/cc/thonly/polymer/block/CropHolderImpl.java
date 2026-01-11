package cc.thonly.polymer.block;

import cc.thonly.reverie_dreams.block.base.AbstractCropBlock;
import eu.pb4.factorytools.api.block.FactoryBlock;
import eu.pb4.factorytools.api.virtualentity.BlockModel;
import eu.pb4.factorytools.api.virtualentity.ItemDisplayElementUtil;
import eu.pb4.polymer.blocks.api.PolymerTexturedBlock;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.attachment.BlockBoundAttachment;
import eu.pb4.polymer.virtualentity.api.attachment.HolderAttachment;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.Map;

public class CropHolderImpl implements PolymerTexturedBlock, FactoryBlock {
    private final AbstractCropBlock block;
    private final Map<Integer, ItemStack> age2itemStackHolder = new Object2ObjectLinkedOpenHashMap<>();

    public CropHolderImpl(AbstractCropBlock block) {
        this.block = block;
    }

    void parse() {
        Identifier key = BuiltInRegistries.BLOCK.getKey(this.block);
        for (int index = 0; index <= this.block.getMaxAge(); index++) {
            String modelId = "%s:block/%s_stage%s".formatted(key.getNamespace(), key.getPath(), index);
            ItemStack model = ItemDisplayElementUtil.getModel(Identifier.parse(modelId));
//            System.out.println(modelId);
            this.age2itemStackHolder.put(index, model);
        }
//        System.out.println(this.age2itemStackHolder);
    }

    @Override
    public BlockState getPolymerBreakEventBlockState(BlockState state, PacketContext context) {
        return Blocks.WHEAT.defaultBlockState();
    }

    @Override
    public BlockState getPolymerBlockState(BlockState blockState, PacketContext packetContext) {
        return BaseFactoryBlock.TRIPWIRE_FLAT.clientState();
    }

    @Override
    public @Nullable ElementHolder createElementHolder(ServerLevel world, BlockPos pos, BlockState initialBlockState) {
        if (this.age2itemStackHolder.isEmpty()) {
            this.parse();
        }
        return new Model(world, pos, initialBlockState);
    }

    public class Model extends BlockModel {
        private final ServerLevel world;
        private final BlockPos blockPos;
        private final BlockState blockState;
        public ItemDisplayElement main;

        public Model(ServerLevel world, BlockPos pos, BlockState initialBlockState) {
            this.world = world;
            this.blockPos = pos;
            this.blockState = initialBlockState;
            init(initialBlockState);
        }

        public void init(BlockState state) {
            this.main = ItemDisplayElementUtil.createSimple();
            updateItem(state);
            this.main.setScale(new Vector3f(1));
            this.addElement(main);
        }

        protected void updateItem(BlockState state) {
            int age = state.getValue(CropHolderImpl.this.block.getAgeProperty());
            this.main.setItem(CropHolderImpl.this.age2itemStackHolder.getOrDefault(age, new ItemStack(Items.BARRIER)));
        }

        @Override
        public void notifyUpdate(HolderAttachment.UpdateType updateType) {
            if (updateType == BlockBoundAttachment.BLOCK_STATE_UPDATE) {
                updateItem(this.blockState());
                this.tick();
            }
            super.notifyUpdate(updateType);
        }
    }
}
