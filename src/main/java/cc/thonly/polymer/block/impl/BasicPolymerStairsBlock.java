package cc.thonly.polymer.block.impl;

import cc.thonly.reverie_dreams.util.IdentifierGetter;
import com.mojang.math.Axis;
import eu.pb4.factorytools.api.block.FactoryBlock;
import eu.pb4.factorytools.api.virtualentity.BlockModel;
import eu.pb4.factorytools.api.virtualentity.ItemDisplayElementUtil;
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
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.StairsShape;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public class BasicPolymerStairsBlock extends StairBlock implements FactoryBlock {
    private final ResourceLocation blockId;
    private final Block template = Blocks.MANGROVE_STAIRS;

    public BasicPolymerStairsBlock(BlockState baseBlockState, Properties settings) {
        super(baseBlockState, settings);
        assert settings.id != null;
        this.blockId = settings.id.location();
    }

    @Override
    public BlockState getPolymerBlockState(BlockState state, PacketContext context) {
        return template.withPropertiesOf(state);
    }

    @Override
    public @Nullable ElementHolder createElementHolder(ServerLevel world, BlockPos pos, BlockState initialBlockState) {
        return new Model(initialBlockState, this.getBlockId());
    }

    public static final class Model extends BlockModel {
        private static final Map<ResourceLocation, Models> MODEL_CACHE = new ConcurrentHashMap<>();

        private final Models models;
        private ItemDisplayElement main;

        public Model(BlockState state, ResourceLocation location) {
            this.models = MODEL_CACHE.computeIfAbsent(location, Models::new);
            init(state);
        }

        private void init(BlockState state) {
            this.main = ItemDisplayElementUtil.createSimple();
            this.main.setTeleportDuration(0);
            this.main.setInterpolationDuration(0);

            updateItem(state);
            updateStatePos(state);

            addElement(this.main);
        }

        private void updateStatePos(BlockState state) {
            if (state.getValue(HALF) == Half.BOTTOM) {
                main.setYaw(state.getValue(FACING).toYRot() + switch (state.getValue(StairBlock.SHAPE)) {
                    case STRAIGHT -> -90;
                    case INNER_RIGHT, OUTER_RIGHT -> +270;
                    default -> +180;
                });
            } else {
                main.setYaw(state.getValue(FACING).toYRot() + switch (state.getValue(StairBlock.SHAPE)) {
                    case STRAIGHT, INNER_LEFT, OUTER_LEFT -> +90;
                    default -> -180;
                });
                main.setRightRotation(Axis.ZP.rotationDegrees(180));
            }
        }

        private void updateItem(BlockState state) {
            main.setItem(switch (state.getValue(StairBlock.SHAPE)) {
                case STRAIGHT -> models.STRAIGHT;
                case INNER_LEFT, INNER_RIGHT -> models.INNER;
                case OUTER_LEFT, OUTER_RIGHT -> models.OUTER;
            });

            float scale = 1.004f;
            boolean straight = state.getValue(StairBlock.SHAPE) == StairsShape.STRAIGHT;

            main.setScale(new Vector3f(straight ? 2 * scale : scale));

            float scaleOffset = (scale - 1) / 4;
            boolean isTop = state.getValue(HALF) == Half.TOP;

            main.setTranslation(new Vector3f(
                    isTop ? -scaleOffset : scaleOffset,
                    isTop ? -scaleOffset : scaleOffset,
                    straight ? 0 : scaleOffset
            ));
        }

        @Override
        public void notifyUpdate(HolderAttachment.UpdateType updateType) {
            if (updateType == BlockBoundAttachment.BLOCK_STATE_UPDATE) {
                BlockState state = this.blockState();
                updateItem(state);
                updateStatePos(state);
                this.tick();
            }
            super.notifyUpdate(updateType);
        }

        private static final class Models {
            final ItemStack STRAIGHT;
            final ItemStack INNER;
            final ItemStack OUTER;

            private Models(ResourceLocation location) {
                this.STRAIGHT = ItemDisplayElementUtil.getModel(
                        location.withPath("block/" + location.getPath())
                );
                this.INNER = ItemDisplayElementUtil.getModel(
                        location.withPath("block/" + location.getPath() + "_inner")
                );
                this.OUTER = ItemDisplayElementUtil.getModel(
                        location.withPath("block/" + location.getPath() + "_outer")
                );
            }
        }
    }

}