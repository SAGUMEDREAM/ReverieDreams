package cc.thonly.polymer.block.impl;

import eu.pb4.factorytools.api.block.FactoryBlock;
import eu.pb4.factorytools.api.virtualentity.BlockModel;
import eu.pb4.factorytools.api.virtualentity.ItemDisplayElementUtil;
import eu.pb4.polymer.blocks.api.PolymerTexturedBlock;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.attachment.BlockBoundAttachment;
import eu.pb4.polymer.virtualentity.api.attachment.HolderAttachment;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.HashMap;
import java.util.Map;

@Getter
public class BasicPolymerFenceBlock extends FenceBlock implements PolymerTexturedBlock, FactoryBlock {
    Block template = Blocks.MANGROVE_FENCE;
    ResourceLocation blockId;

    public BasicPolymerFenceBlock(Properties settings) {
        super(settings);
        assert settings.id != null;
        this.blockId = settings.id.location();
    }

    @Override
    public BlockState getPolymerBlockState(BlockState state, PacketContext packetContext) {
        return template.withPropertiesOf(state);
    }

    @Override
    public @Nullable ElementHolder createElementHolder(ServerLevel world, BlockPos pos, BlockState initialBlockState) {
        return new Model(initialBlockState, this.blockId);
    }

    public static final class Model extends BlockModel {
        public final ItemDisplayElement post;
        public final Map<Block, Map<Direction, ItemDisplayElement>> SIDES = new HashMap<>();

        public Model(BlockState state, ResourceLocation id) {
            ItemStack MODEL_POST = ItemDisplayElementUtil.getModel(ResourceLocation.fromNamespaceAndPath(id.getNamespace(), "block/%s_post".formatted(id.getPath())));
            ItemStack MODEL_SIDE = ItemDisplayElementUtil.getModel(ResourceLocation.fromNamespaceAndPath(id.getNamespace(), "block/%s_side".formatted(id.getPath())));

            post = ItemDisplayElementUtil.createSimple(MODEL_POST);
            post.setScale(new Vector3f(1.00275f));
            addElement(post);
            for (Direction side : Direction.Plane.HORIZONTAL) {
                Map<Direction, ItemDisplayElement> sideMap = SIDES.computeIfAbsent(state.getBlock(), b -> new HashMap<>());
                sideMap.put(side, ItemDisplayElementUtil.createSimple(MODEL_SIDE));
                sideMap.get(side).setYaw(side.toYRot());
                sideMap.get(side).setScale(new Vector3f(1.00275f));
                addElement(sideMap.get(side));
            }
            this.updateItem(state);
        }

        private void updateItem(BlockState state) {
            Map<Direction, ItemDisplayElement> sideMap = SIDES.computeIfAbsent(state.getBlock(), b -> new HashMap<>());
            setVisibility(sideMap.get(Direction.NORTH), state.getValue(NORTH));
            setVisibility(sideMap.get(Direction.EAST), state.getValue(EAST));
            setVisibility(sideMap.get(Direction.SOUTH), state.getValue(SOUTH));
            setVisibility(sideMap.get(Direction.WEST), state.getValue(WEST));
        }

        private void setVisibility(ItemDisplayElement elem, boolean visible) {
            if (elem == null) {
                return;
            }
            elem.setViewRange(visible ? 0.75f : 0f);
        }

        @Override
        public void notifyUpdate(HolderAttachment.UpdateType updateType) {
            if (updateType == BlockBoundAttachment.BLOCK_STATE_UPDATE) {
                this.updateItem(this.blockState());
                this.tick();
            }
            super.notifyUpdate(updateType);
        }
    }

}
