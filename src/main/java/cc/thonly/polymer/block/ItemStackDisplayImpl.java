package cc.thonly.polymer.block;

import cc.thonly.mystias_izakaya.block.entity.ItemStackDisplayBlockEntity;
import cc.thonly.polymer.block.model.TransparentFlatTripWire;
import cc.thonly.reverie_dreams.recipe.ItemStackWrapper;
import eu.pb4.factorytools.api.block.FactoryBlock;
import eu.pb4.factorytools.api.virtualentity.BlockModel;
import eu.pb4.factorytools.api.virtualentity.ItemDisplayElementUtil;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.attachment.BlockBoundAttachment;
import eu.pb4.polymer.virtualentity.api.attachment.HolderAttachment;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.Getter;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;

public class ItemStackDisplayImpl implements FactoryBlock, TransparentFlatTripWire {
    public static final Map<ServerWorld, Map<Long, Model>> MAPPING = new Object2ObjectOpenHashMap<>();
    public static final Map<Long, Model> POS_TO_MODEL = new HashMap<>();

    @Override
    public @Nullable ElementHolder createElementHolder(ServerWorld world, BlockPos pos, BlockState initialBlockState) {
        var model = new Model(world, initialBlockState.getBlock(), initialBlockState, pos);
        Map<Long, Model> longModelMap = MAPPING.computeIfAbsent(world, w -> new HashMap<>());
        longModelMap.put(pos.asLong(), model);
        return model;
    }

    @Getter
    public static class Model extends BlockModel {
        private final ServerWorld serverWorld;
        private final Block block;
        private final BlockPos blockPos;
        private final ItemDisplayElement main;
        private ItemStackDisplayBlockEntity blockEntity;
        private final ItemDisplayElement item;

        public Model(ServerWorld serverWorld, Block block, BlockState initialBlockState, BlockPos blockPos) {
            this.serverWorld = serverWorld;
            this.block = block;
            this.blockPos = blockPos;

            this.main = ItemDisplayElementUtil.createSimple(initialBlockState.getBlock().asItem());
            this.main.setScale(new Vector3f(1.8f));
            this.main.setOffset(new Vec3d(0, -0.05, 0));
            this.item = ItemDisplayElementUtil.createSimple(Items.AIR);
            this.item.setScale(new Vector3f(0.5f));
            this.item.setOffset(new Vec3d(0, -0.22, 0));

            addElement(this.main);
            addElement(this.item);
        }

        public void updateItem(BlockState blockState) {
            BlockEntity blockEntity = this.serverWorld.getBlockEntity(this.blockPos);
            if (this.blockEntity == null && blockEntity instanceof ItemStackDisplayBlockEntity itemStackDisplayBlockEntity) {
                this.blockEntity = itemStackDisplayBlockEntity;
            }

            ItemStackWrapper item;
            if (this.blockEntity != null && !ItemStack.areEqual(this.blockEntity.getItem().getItemStack(), this.item.getItem())) {
                removeElement(this.item);
                item = this.blockEntity.getItem();
                this.item.setItem(item.getItemStack().copy());
                this.item.setOffset(new Vec3d(0, -0.22, 0));
                this.item.setRotation((float) 0, (float) this.blockEntity.getYaw() + 180);
                addElement(this.item);
            }
        }

        @Override
        public void notifyUpdate(HolderAttachment.UpdateType updateType) {
            if (updateType == BlockBoundAttachment.BLOCK_STATE_UPDATE) {
                updateItem(this.blockState());
            }
            this.tick();
            super.notifyUpdate(updateType);
        }
    }
}
