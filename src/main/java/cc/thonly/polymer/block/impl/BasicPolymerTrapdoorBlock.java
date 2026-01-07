package cc.thonly.polymer.block.impl;

import cc.thonly.reverie_dreams.util.IdentifierGetter;
import eu.pb4.factorytools.api.block.FactoryBlock;
import eu.pb4.factorytools.api.virtualentity.BlockModel;
import eu.pb4.factorytools.api.virtualentity.ItemDisplayElementUtil;
import eu.pb4.polymer.blocks.api.BlockModelType;
import eu.pb4.polymer.blocks.api.PolymerBlockModel;
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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.Half;
import xyz.nucleoid.packettweaker.PacketContext;

@Getter
public class BasicPolymerTrapdoorBlock extends TrapDoorBlock implements PolymerTexturedBlock, FactoryBlock {
    private final ResourceLocation blockId;
    private final PolymerBlockModel openModel;
    private final PolymerBlockModel bottomModel;
    private final PolymerBlockModel topModel;

    public BasicPolymerTrapdoorBlock(Properties settings) {
        super(BlockSetType.OAK, settings);
        assert settings.id != null;
        this.blockId = settings.id.location();
        this.openModel = PolymerBlockModel.of(ResourceLocation.fromNamespaceAndPath(this.blockId.getNamespace(), "block/" + this.blockId.getPath() + "_open"));
        this.bottomModel = PolymerBlockModel.of(ResourceLocation.fromNamespaceAndPath(this.blockId.getNamespace(), "block/" + this.blockId.getPath() + "_bottom"));
        this.topModel = PolymerBlockModel.of(ResourceLocation.fromNamespaceAndPath(this.blockId.getNamespace(), "block/" + this.blockId.getPath() + "_top"));
    }

    private PolymerBlockModel getModel(String type, int x, int y) {
        return PolymerBlockModel.of(ResourceLocation.fromNamespaceAndPath(this.blockId.getNamespace(), "block/%s_%s".formatted(this.blockId.getPath(), type)), x, y);
    }

    @Override
    public BlockState getPolymerBreakEventBlockState(BlockState state, PacketContext context) {
        return Blocks.OAK_TRAPDOOR.withPropertiesOf(state);
    }

    @Override
    public BlockState getPolymerBlockState(BlockState state, PacketContext context) {
        return Blocks.OAK_TRAPDOOR.withPropertiesOf(state);
    }

    @Override
    public ElementHolder createElementHolder(ServerLevel world, BlockPos pos, BlockState initialBlockState) {
        return new Model(this, initialBlockState, this.blockId);
    }

    @Getter
    public final class Model extends BlockModel {
        private final BasicPolymerTrapdoorBlock block;
        private final BlockState state;
        private final ResourceLocation id;
        public final ItemStack openModel;
        public final ItemStack bottomModel;
        public final ItemStack topModel;
        public ItemDisplayElement main;

        public Model(BasicPolymerTrapdoorBlock block, BlockState state, ResourceLocation id) {
            this.block = block;
            this.state = state;
            this.id = id;
            this.openModel = ItemDisplayElementUtil.getModel(block.getOpenModel().model());
            this.bottomModel = ItemDisplayElementUtil.getModel(block.getBottomModel().model());
            this.topModel = ItemDisplayElementUtil.getModel(block.getTopModel().model());
            this.init();
        }

        public void init() {
            update(this.state);
        }

        public void update(BlockState state) {
            if (this.main != null) {
                this.removeElement(this.main);
                this.main = null;
            }
            ItemDisplayElement element = null;
            boolean open = state.getValue(TrapDoorBlock.OPEN);
            Direction direction = state.getValue(HorizontalDirectionalBlock.FACING);
            Half blockHalf = state.getValue(HALF);
            element = switch (blockHalf) {
                case Half.TOP -> new ItemDisplayElement(this.topModel);
                case Half.BOTTOM -> new ItemDisplayElement(this.bottomModel);
                default -> new ItemDisplayElement(this.topModel);
            };
            if (open) {
                element = new ItemDisplayElement(this.openModel);
            }

            element.setYaw(direction.toYRot());
            this.main = element;
            this.addElement(element);
        }

        @Override
        public void notifyUpdate(HolderAttachment.UpdateType updateType) {
            if (updateType == BlockBoundAttachment.BLOCK_STATE_UPDATE) {
                update(this.blockState());
                this.tick();
            }
            super.notifyUpdate(updateType);
        }
    }
}