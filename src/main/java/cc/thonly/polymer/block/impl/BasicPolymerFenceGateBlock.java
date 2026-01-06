package cc.thonly.polymer.block.impl;

import com.mojang.math.Axis;
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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.WoodType;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import xyz.nucleoid.packettweaker.PacketContext;

@Getter
public class BasicPolymerFenceGateBlock extends FenceGateBlock implements PolymerTexturedBlock, FactoryBlock {
    private final ResourceLocation blockId;
    private final Block template = Blocks.MANGROVE_FENCE_GATE;

    public BasicPolymerFenceGateBlock(WoodType type, Properties settings) {
        super(type, settings);
        assert settings.id != null;
        this.blockId = settings.id.location();
    }


    @Override
    public BlockState getPolymerBlockState(BlockState state, PacketContext packetContext) {
        return template.withPropertiesOf(state);
    }

    @Override
    public @Nullable ElementHolder createElementHolder(ServerLevel world, BlockPos pos, BlockState initialBlockState) {
        return new Model(initialBlockState, this.getBlockId());
    }

    public static final class Model extends BlockModel {
        public final ItemStack MODEL_CLOSED;
        public final ItemStack MODEL_OPEN;
        public final ItemDisplayElement[] main = new ItemDisplayElement[2];

        public Model(BlockState state, ResourceLocation id) {
            MODEL_CLOSED = ItemDisplayElementUtil.getModel(ResourceLocation.fromNamespaceAndPath(id.getNamespace(), "block/" + id.getPath()));
            MODEL_OPEN = ItemDisplayElementUtil.getModel(ResourceLocation.fromNamespaceAndPath(id.getNamespace(), "block/" + id.getPath() + "_open"));

            main[0] = ItemDisplayElementUtil.createSimple();
            main[1] = ItemDisplayElementUtil.createSimple();
            this.updateItem(state);
            addElement(main[0]);
            addElement(main[1]);
        }

        private void updateItem(BlockState state) {
            for (int i = 0; i < 2; i++) {
                ItemDisplayElement elem = main[i];
                elem.setItem(state.getValue(OPEN) ? MODEL_OPEN : MODEL_CLOSED);
                float scale = 1.0025f;
                elem.setScale(new Vector3f(state.getValue(OPEN) ? scale : 2 * scale));
                //float scaleOffset = (scale - 1) / 2;
                float offset = i == 0 ? 0.001f : -0.001f;
                elem.setTranslation(new Vector3f(offset, offset + (state.getValue(IN_WALL) ? -0.1875f : 0), offset));
                elem.setRightRotation(state.getValue(FACING).getRotation().mul(Axis.XP.rotationDegrees(-90)).mul(Axis.YP.rotationDegrees(180)));
            }
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