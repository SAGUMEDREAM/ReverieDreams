package cc.thonly.reverie_dreams.polymer.block;

import cc.thonly.reverie_dreams.block.GensokyoAltarBlock;
import cc.thonly.reverie_dreams.block.entity.GensokyoAltarBlockEntity;
import eu.pb4.factorytools.api.block.FactoryBlock;
import eu.pb4.factorytools.api.virtualentity.BlockModel;
import eu.pb4.factorytools.api.virtualentity.FastItemDisplayElement;
import eu.pb4.factorytools.api.virtualentity.ItemDisplayElementUtil;
import eu.pb4.polymer.blocks.api.PolymerTexturedBlock;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.attachment.BlockBoundAttachment;
import eu.pb4.polymer.virtualentity.api.attachment.HolderAttachment;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;

import java.util.HashMap;
import java.util.Map;

public class GensokyoAltarImpl implements FactoryBlock, PolymerTexturedBlock {
    public static final Map<Long, Model> POS_TO_MODEL = new HashMap<>();

    @Override
    public BlockState getPolymerBlockState(BlockState state, PacketContext context) {
//        return Blocks.BARRIER.defaultBlockState();
        return Blocks.SNOW.defaultBlockState().setValue(SnowLayerBlock.LAYERS, 6);
    }

    @Override
    public BlockState getPolymerBreakEventBlockState(BlockState state, PacketContext context) {
        return Blocks.ENCHANTING_TABLE.defaultBlockState();
    }

    @Override
    public @Nullable ElementHolder createElementHolder(ServerLevel world, BlockPos pos, BlockState state) {
        Model altarModel = new Model(world, pos, state);
        POS_TO_MODEL.put(pos.asLong(), altarModel);
        return altarModel;
    }

    public static class Model extends BlockModel {
        protected ItemDisplayElement main;
        public final ItemDisplayElement[] itemStackDisplay = new ItemDisplayElement[9];
        private BlockState state;
        private BlockPos pos;
        private ServerLevel world;
        private GensokyoAltarBlockEntity blockEntity;
        private SimpleContainer inventory;
        public float angle = 0;

        public Model(ServerLevel world, BlockPos pos, BlockState state) {
            init(state);
            this.world = world;
            this.pos = pos;
            this.state = state;
        }

        public GensokyoAltarBlockEntity getBlockEntityFromWorld() {
            if (this.world == null) {
                return null;
            }
            return (GensokyoAltarBlockEntity) this.world.getBlockEntity(this.pos);
        }

        public void update() {
            this.blockEntity = this.getBlockEntityFromWorld();
            if (this.blockEntity == null) return;
            this.inventory = this.blockEntity.getInventory();

            for (int i = 0; i < this.itemStackDisplay.length; i++) {
                ItemStack stack = this.inventory.getItem(i);
                ItemDisplayElement element = this.itemStackDisplay[i];

                if (stack.isEmpty()) {
                    if (element != null) {
                        this.removeElement(element);
                        this.itemStackDisplay[i] = null;
                    }
                    continue;
                }

                if (element == null || !ItemStack.matches(stack, element.getItem())) {
                    if (element != null) {
                        this.removeElement(element);
                    }

                    FastItemDisplayElement newElement = new FastItemDisplayElement(stack.copy());
                    int[] offset = GensokyoAltarBlock.OFFSETS[i];
                    newElement.setScale(new Vector3f(i != 8 ? 0.6f : 0.5f));
                    newElement.setDisplaySize(0.5f, 0.5f);
                    newElement.setOffset(new Vec3(offset[0], i != 8 ? 3 : 0.5, offset[1]));

                    this.addElement(newElement);
                    this.itemStackDisplay[i] = newElement;
                }
            }
        }

        public void init(BlockState state) {
            main = ItemDisplayElementUtil.createSimple(state.getBlock().asItem().getDefaultInstance());
            main.setScale(new Vector3f(2f));
            addElement(main);
        }

        protected void updateItem(BlockState state) {
            this.removeElement(main);
            init(state);
        }

        @Override
        public void notifyUpdate(HolderAttachment.UpdateType updateType) {
            if (updateType == BlockBoundAttachment.BLOCK_STATE_UPDATE) {
                updateItem(this.blockState());
                this.update();
            }
            super.notifyUpdate(updateType);
        }
    }
}
