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
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public class BasicPolymerButtonBlock extends ButtonBlock implements FactoryBlock {
    ResourceLocation blockId;
    Block template = Blocks.MANGROVE_BUTTON;

    public BasicPolymerButtonBlock(BlockSetType blockSetType, int pressTicks, Properties settings) {
        super(blockSetType, pressTicks, settings);
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
        private static final Map<ResourceLocation, Models> MODEL_CACHE = new ConcurrentHashMap<>();

        private final Models models;
        private ItemDisplayElement main;

        public Model(BlockState state, ResourceLocation id) {
            this.models = MODEL_CACHE.computeIfAbsent(id, Models::new);
            init(state);
        }

        private void init(BlockState state) {
            this.main = ItemDisplayElementUtil.createSimple();
            this.main.setTeleportDuration(0);
            this.main.setInterpolationDuration(0);

            updateItem(state);
            addElement(this.main);
        }

        private void updateItem(BlockState state) {
            // 切换模型（按下 / 未按下）
            main.setItem(state.getValue(POWERED) ? models.POWERED : models.UNPOWERED);

            // 旋转逻辑（与你原本的一致）
            if (state.getValue(FACE) == AttachFace.WALL) {
                main.setRightRotation(state.getValue(FACING).getRotation());
            } else if (state.getValue(FACE) == AttachFace.CEILING) {
                main.setRightRotation(
                        Axis.ZP.rotationDegrees(180)
                                .mul(Axis.YP.rotationDegrees(state.getValue(FACING).toYRot()))
                );
            } else {
                main.setRightRotation(
                        Axis.YP.rotationDegrees(state.getValue(FACING).toYRot())
                );
            }

            // 轻微放大避免 Z-Fighting
            float scale = 1.00125f;
            main.setScale(new Vector3f(scale));

            float scaleOffset = (scale - 1) / 2;

            // 位移逻辑（与你原本的一致）
            if (state.getValue(FACE) == AttachFace.WALL) {
                main.setTranslation(
                        new Vector3f(scaleOffset, scaleOffset, scaleOffset)
                                .mul(state.getValue(FACING).step())
                );
            } else {
                main.setTranslation(new Vector3f(
                        0,
                        state.getValue(FACE) == AttachFace.FLOOR ? scaleOffset : -scaleOffset,
                        0
                ));
            }
        }

        @Override
        public void notifyUpdate(HolderAttachment.UpdateType updateType) {
            if (updateType == BlockBoundAttachment.BLOCK_STATE_UPDATE) {
                updateItem(this.blockState());
                this.tick();
            }
            super.notifyUpdate(updateType);
        }

        private static final class Models {
            final ItemStack UNPOWERED;
            final ItemStack POWERED;

            private Models(ResourceLocation id) {
                this.UNPOWERED = ItemDisplayElementUtil.getModel(
                        id.withPath("block/" + id.getPath())
                );
                this.POWERED = ItemDisplayElementUtil.getModel(
                        id.withPath("block/" + id.getPath() + "_pressed")
                );
            }
        }
    }

}
