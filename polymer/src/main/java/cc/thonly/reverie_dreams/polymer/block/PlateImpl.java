package cc.thonly.reverie_dreams.polymer.block;

import cc.thonly.reverie_dreams.polymer.block.model.TransparentFlatTripWire;
import cc.thonly.reverie_dreams.block.entity.PlateBlockEntity;
import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.api.registry.block.CuisineBlockBindings;
import cc.thonly.reverie_dreams.block.kitchen.CuisineBlock;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemDisplayContext;
import eu.pb4.factorytools.api.block.FactoryBlock;
import eu.pb4.factorytools.api.virtualentity.BlockModel;
import eu.pb4.factorytools.api.virtualentity.ItemDisplayElementUtil;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.attachment.BlockBoundAttachment;
import eu.pb4.polymer.virtualentity.api.attachment.HolderAttachment;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.resources.Identifier;

public class PlateImpl implements FactoryBlock, TransparentFlatTripWire {
    public static final Map<ServerLevel, Map<Long, Model>> MAPPING = new Object2ObjectOpenHashMap<>();

    @Override
    public @Nullable ElementHolder createElementHolder(ServerLevel world, BlockPos pos, BlockState initialBlockState) {
        var model = new Model(world, initialBlockState.getBlock(), initialBlockState, pos);
        Map<Long, Model> longModelMap = MAPPING.computeIfAbsent(world, w -> new HashMap<>());
        longModelMap.put(pos.asLong(), model);
        return model;
    }

    @Getter
    public static class Model extends BlockModel {
        private final ServerLevel serverWorld;
        private final Block block;
        private final BlockPos blockPos;
        private final ItemDisplayElement main;
        private PlateBlockEntity blockEntity;
        private final ItemDisplayElement item;
        private ItemStack lastStack = ItemStack.EMPTY;
        private float lastYaw = Float.NaN;

        public Model(ServerLevel serverWorld, Block block, BlockState initialBlockState, BlockPos blockPos) {
            this.serverWorld = serverWorld;
            this.block = block;
            this.blockPos = blockPos;

            ItemStack plateModel = new ItemStack(Items.PAPER);
            plateModel.set(DataComponents.ITEM_MODEL, ReverieDreams.id("plated_cuisine/plate"));
            this.main = ItemDisplayElementUtil.createSimple(plateModel);
            this.main.setItemDisplayContext(ItemDisplayContext.NONE);
            this.main.setScale(new Vector3f(1));
            this.item = ItemDisplayElementUtil.createSimple(Items.AIR.getDefaultInstance());
            this.item.setScale(new Vector3f(0.5f));
            this.item.setOffset(new Vec3(0, -0.22, 0));

            addElement(this.main);
            addElement(this.item);
        }

        public void updateItem(BlockState blockState) {
            BlockEntity current = this.serverWorld.getBlockEntity(this.blockPos);
            if (!(current instanceof PlateBlockEntity plate)) return;
            this.blockEntity = plate;
            ItemStack content = plate.getItem().build();
            float yaw = plate.getYaw();
            if (ItemStack.matches(content, this.lastStack) && yaw == this.lastYaw) return;
            this.lastStack = content.copy();
            this.lastYaw = yaw;
            var cuisine = CuisineBlockBindings.find(content);
            if (cuisine.isPresent()) {
                var id = cuisine.orElseThrow().unwrapKey().orElseThrow().identifier();
                ItemStack display = new ItemStack(Items.PAPER);
                display.set(DataComponents.ITEM_MODEL, Identifier.fromNamespaceAndPath(id.getNamespace(), "plated_" + id.getPath()));
                this.item.setItem(display);
                this.item.setItemDisplayContext(ItemDisplayContext.NONE);
                this.item.setScale(new Vector3f(1));
                this.item.setOffset(new Vec3(0, CuisineBlock.PLATE_SURFACE, 0));
                this.item.setRotation(0, CuisineBlock.displayYaw(yaw));
            } else {
                this.item.setItem(content);
                this.item.setItemDisplayContext(ItemDisplayContext.FIXED);
                this.item.setScale(new Vector3f(0.5f));
                this.item.setOffset(new Vec3(0, -0.22, 0));
                this.item.setRotation(0, yaw + 180);
            }
            this.item.tick();
        }

        @Override
        public void destroy() {
            super.destroy();
            var models = MAPPING.get(this.serverWorld);
            if (models != null) {
                models.remove(this.blockPos.asLong(), this);
                if (models.isEmpty()) MAPPING.remove(this.serverWorld);
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
