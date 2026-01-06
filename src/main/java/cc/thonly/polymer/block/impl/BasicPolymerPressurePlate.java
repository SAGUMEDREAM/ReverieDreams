package cc.thonly.polymer.block.impl;

import cc.thonly.reverie_dreams.util.IdentifierGetter;
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
import net.minecraft.server.dedicated.Settings;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.PressurePlateBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public class BasicPolymerPressurePlate extends PressurePlateBlock implements FactoryBlock {
    private final ResourceLocation blockId;
    private final Block template = Blocks.MANGROVE_PRESSURE_PLATE;

    public BasicPolymerPressurePlate(BlockSetType blockSet, Properties settings) {
        super(blockSet, settings);
        assert settings.id != null;
        this.blockId = settings.id.location();
    }

    @Override
    public BlockState getPolymerBlockState(BlockState state, PacketContext context) {
        return template.withPropertiesOf(state);
    }

    @Override
    public @Nullable ElementHolder createElementHolder(ServerLevel world, BlockPos pos, BlockState initialBlockState) {
        return new Model(initialBlockState, this.blockId);
    }

    public static final class Model extends BlockModel {
        /**
         * 每个 blockId 对应一组模型（未按下 / 按下）
         */
        private static final Map<ResourceLocation, Models> MODEL_CACHE = new ConcurrentHashMap<>();
        private static final Timer timer = new Timer("Polywood: Pressure Plate Update Timer");
        private static final Queue<Model> toBeTicked = new ArrayDeque<>();
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

            updateItem(state.getValue(POWERED));
            addElement(this.main);
        }

        private void updateItem(boolean powered) {
            main.setItem(powered ? models.POWERED : models.UNPOWERED);

            float scale = 1.0025f;
            main.setScale(new Vector3f(powered ? scale : 2 * scale));

            float scaleOffset = (scale - 1) / 2;
            main.setTranslation(new Vector3f(0, scaleOffset, 0));
        }

        @Override
        public void notifyUpdate(HolderAttachment.UpdateType updateType) {
            if (updateType == BlockBoundAttachment.BLOCK_STATE_UPDATE) {
                if (!this.blockState().getValue(POWERED)) {
                    this.updateItem(false);
                    this.tick();
                } else { // This fixes the regular pressure plate flashing for a brief moment when activated
                    toBeTicked.add(this);

                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            if (!toBeTicked.isEmpty()) {
                                Model model = toBeTicked.peek();
                                model.updateItem(true);
                                model.tick();
                                toBeTicked.remove();
                            }
                        }
                    }, 100);
                }
            }
            super.notifyUpdate(updateType);
        }

    }

    private static final class Models {
        final ItemStack UNPOWERED;
        final ItemStack POWERED;

        private Models(ResourceLocation id) {
            this.UNPOWERED = ItemDisplayElementUtil.getModel(
                    id.withPath("block/" + id.getPath())
            );
            this.POWERED = ItemDisplayElementUtil.getModel(
                    id.withPath("block/" + id.getPath() + "_down")
            );
        }
    }

}
