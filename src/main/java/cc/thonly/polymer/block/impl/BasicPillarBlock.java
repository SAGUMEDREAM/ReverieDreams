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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import xyz.nucleoid.packettweaker.PacketContext;

@Getter
public class BasicPillarBlock extends RotatedPillarBlock implements PolymerTexturedBlock, FactoryBlock {
    private final ResourceLocation blockId;
    private final BlockState[] model = new BlockState[3];

    public BasicPillarBlock(Properties settings) {
        super(settings.noOcclusion());
        assert settings.id != null;
        this.blockId = settings.id.location();
    }
//        model[0] = PolymerBlockResourceUtils.requestBlock(BlockModelType.FULL_BLOCK, PolymerBlockModel.of(ResourceLocation.fromNamespaceAndPath(this.blockId.getNamespace(),"block/" + this.blockId.getPath()), 90, 90));
//        model[1] = PolymerBlockResourceUtils.requestBlock(BlockModelType.FULL_BLOCK, PolymerBlockModel.of(ResourceLocation.fromNamespaceAndPath(this.blockId.getNamespace(),"block/" + this.blockId.getPath()), 0, 0));
//        model[2] = PolymerBlockResourceUtils.requestBlock(BlockModelType.FULL_BLOCK, PolymerBlockModel.of(ResourceLocation.fromNamespaceAndPath(this.blockId.getNamespace(),"block/" + this.blockId.getPath()), 90, 0));
//    }

//    @Override
//    public BlockState getPolymerBreakEventBlockState(BlockState state, PacketContext context) {
//        return Blocks.OAK_LOG.defaultBlockState();
//    }
//
//    @Override
//    public BlockState getPolymerBlockState(BlockState state, PacketContext context) {
//        return switch (state.getValue(RotatedPillarBlock.AXIS)) {
//            case X -> model[0];
//            case Y -> model[1];
//            case Z -> model[2];
//        };
//    }


    @Override
    public BlockState getPolymerBlockState(BlockState blockState, PacketContext packetContext) {
        return Blocks.BARRIER.defaultBlockState();
    }

    @Override
    public @Nullable ElementHolder createElementHolder(ServerLevel world, BlockPos pos, BlockState state) {
        return new Model(world, pos, state);
    }


    public static class Model extends BlockModel {
        private ItemDisplayElement main;
        private Level world;
        private BlockPos pos;

        public Model(Level world, BlockPos pos, BlockState state) {
            this.world = world;
            this.pos = pos;
            init(state);
        }

        private void init(BlockState state) {
            main = ItemDisplayElementUtil.createSimple(state.getBlock().asItem());
            switch (state.getValue(BlockStateProperties.AXIS)) {
                case X: {
                    main.setYaw(90);
                    main.setPitch(90);
                    break;
                }
                case Z: {
                    main.setPitch(90);
                    break;
                }
            }
            main.setScale(new Vector3f(2f));
            addElement(main);
        }

        private void updateItem(BlockState state) {
            this.removeElement(main);
            init(state);
        }

        @Override
        public void notifyUpdate(HolderAttachment.UpdateType updateType) {
            if (updateType == BlockBoundAttachment.BLOCK_STATE_UPDATE) {
                updateItem(this.blockState());
            }
            super.notifyUpdate(updateType);
        }
    }
}

