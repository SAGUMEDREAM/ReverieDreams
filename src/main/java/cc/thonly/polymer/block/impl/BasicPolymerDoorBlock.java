package cc.thonly.polymer.block.impl;


import cc.thonly.reverie_dreams.util.IdentifierGetter;
import eu.pb4.factorytools.api.block.FactoryBlock;
import eu.pb4.factorytools.api.virtualentity.BlockModel;
import eu.pb4.factorytools.api.virtualentity.ItemDisplayElementUtil;
import eu.pb4.polymer.blocks.api.BlockModelType;
import eu.pb4.polymer.blocks.api.PolymerBlockResourceUtils;
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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import xyz.nucleoid.packettweaker.PacketContext;

@Getter
public class BasicPolymerDoorBlock extends DoorBlock implements FactoryBlock, PolymerTexturedBlock {
    private final BlockState NORTH_DOOR;
    private final BlockState EAST_DOOR;
    private final BlockState SOUTH_DOOR;
    private final BlockState WEST_DOOR;
    protected final ItemStack MODEL_TOP_RIGHT;
    protected final ItemStack MODEL_TOP_LEFT;
    protected final ItemStack MODEL_BOTTOM_RIGHT;
    protected final ItemStack MODEL_BOTTOM_LEFT;
    private final ResourceLocation blockId;

    public BasicPolymerDoorBlock(Properties settings) {
        super(BlockSetType.OAK, settings);
        assert settings.id != null;
        this.blockId = settings.id.location();

        NORTH_DOOR = PolymerBlockResourceUtils.requestEmpty(BlockModelType.NORTH_DOOR);
        EAST_DOOR = PolymerBlockResourceUtils.requestEmpty(BlockModelType.EAST_DOOR);
        SOUTH_DOOR = PolymerBlockResourceUtils.requestEmpty(BlockModelType.SOUTH_DOOR);
        WEST_DOOR = PolymerBlockResourceUtils.requestEmpty(BlockModelType.WEST_DOOR);

        MODEL_TOP_RIGHT = ItemDisplayElementUtil.getModel(ResourceLocation.fromNamespaceAndPath(this.blockId.getNamespace(), "block/%s_top_left".formatted(this.blockId.getPath())));
        MODEL_TOP_LEFT = ItemDisplayElementUtil.getModel(ResourceLocation.fromNamespaceAndPath(this.blockId.getNamespace(), "block/%s_top_right".formatted(this.blockId.getPath())));
        MODEL_BOTTOM_RIGHT = ItemDisplayElementUtil.getModel(ResourceLocation.fromNamespaceAndPath(this.blockId.getNamespace(), "block/%s_bottom_left".formatted(this.blockId.getPath())));
        MODEL_BOTTOM_LEFT = ItemDisplayElementUtil.getModel(ResourceLocation.fromNamespaceAndPath(this.blockId.getNamespace(), "block/%s_bottom_right".formatted(this.blockId.getPath())));
    }

    @Override
    public BlockState getPolymerBreakEventBlockState(BlockState state, PacketContext context) {
        return Blocks.OAK_DOOR.withPropertiesOf(state);
    }

    @Override
    public BlockState getPolymerBlockState(BlockState state, PacketContext context) {
        return Blocks.OAK_DOOR.withPropertiesOf(state);
    }

    @Override
    public @Nullable ElementHolder createElementHolder(ServerLevel world, BlockPos pos, BlockState initialBlockState) {
        return new Model(initialBlockState);
    }

    public static final class Model extends BlockModel {
        public ItemDisplayElement main;

        public Model(BlockState state) {
            main = ItemDisplayElementUtil.createSimple();
            main.setTeleportDuration(0);
            main.setInterpolationDuration(0);
            this.updateItem(state);
            updateStatePos(state);
            addElement(main);
        }

        private void updateStatePos(BlockState state) {
            var rotation = state.getValue(FACING).toYRot() + 270;
            var open = state.getValue(OPEN);
            if (state.getValue(BasicPolymerDoorBlock.HINGE).equals(DoorHingeSide.LEFT)) {
                rotation += open ? 90 : 0;
            } else {
                rotation += open ? 270 : 0;
            }
            main.setYaw(rotation);
        }

        private void updateItem(BlockState state) {
            BasicPolymerDoorBlock door = (BasicPolymerDoorBlock) state.getBlock();
            boolean useRightModel = state.getValue(HINGE).equals(DoorHingeSide.LEFT) ^ state.getValue(OPEN);
            main.setScale(new Vector3f(1.00275f));
            if (state.getValue(HALF) == DoubleBlockHalf.UPPER)
                main.setItem(useRightModel ? door.MODEL_TOP_RIGHT : door.MODEL_TOP_LEFT);
            else
                main.setItem(useRightModel ? door.MODEL_BOTTOM_RIGHT : door.MODEL_BOTTOM_LEFT);
        }

        @Override
        public void notifyUpdate(HolderAttachment.UpdateType updateType) {
            if (updateType == BlockBoundAttachment.BLOCK_STATE_UPDATE) {
                updateStatePos(this.blockState());
                updateItem(this.blockState());
                this.tick();
            }
            super.notifyUpdate(updateType);
        }
    }
}