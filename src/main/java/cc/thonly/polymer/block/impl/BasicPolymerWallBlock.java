package cc.thonly.polymer.block.impl;


import cc.thonly.reverie_dreams.util.IdentifierGetter;
import eu.pb4.factorytools.api.block.FactoryBlock;
import eu.pb4.factorytools.api.virtualentity.BlockModel;
import eu.pb4.factorytools.api.virtualentity.ItemDisplayElementUtil;
import eu.pb4.polymer.blocks.api.PolymerTexturedBlock;
import eu.pb4.polymer.virtualentity.api.BlockWithElementHolder;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.attachment.HolderAttachment;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.WallSide;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.Map;

@Setter
@Getter
public class BasicPolymerWallBlock extends WallBlock implements FactoryBlock, PolymerTexturedBlock {
    private final ResourceLocation blockId;

    public BasicPolymerWallBlock(Properties settings) {
        super(settings);
        assert settings.id != null;
        this.blockId = settings.id.location();
    }


    @Override
    public BlockState getPolymerBlockState(BlockState state, PacketContext context) {
        return Blocks.STONE_BRICK_WALL.withPropertiesOf(state);
    }

    @Override
    public BlockState getPolymerBreakEventBlockState(BlockState state, PacketContext context) {
        return FactoryBlock.super.getPolymerBreakEventBlockState(state, context);
    }

    @Override
    public @Nullable ElementHolder createElementHolder(ServerLevel world, BlockPos pos, BlockState initialBlockState) {
        return new Model(initialBlockState, this.blockId);
//        return ShiftyBlockStateModel.longRange(initialBlockState, pos);
    }

    public static final class Model extends BlockModel {
        public final ItemDisplayElement post;
        public static final EnumProperty<WallSide> EAST_WALL_SHAPE = BlockStateProperties.EAST_WALL;
        public static final EnumProperty<WallSide> NORTH_WALL_SHAPE = BlockStateProperties.NORTH_WALL;
        public static final EnumProperty<WallSide> SOUTH_WALL_SHAPE = BlockStateProperties.SOUTH_WALL;
        public static final EnumProperty<WallSide> WEST_WALL_SHAPE = BlockStateProperties.WEST_WALL;
        public static final BooleanProperty up = WallBlock.UP;
        public final Map<EnumProperty<WallSide>, Map<WallSide, ItemDisplayElement>> map = new Object2ObjectOpenHashMap<>();

        public Model(BlockState state, ResourceLocation id) {
            var offset = 0;
            ItemStack MODEL_POST = ItemDisplayElementUtil.getModel(ResourceLocation.fromNamespaceAndPath(id.getNamespace(), "block/%s_post".formatted(id.getPath())));
            ItemStack MODEL_SIDE = ItemDisplayElementUtil.getModel(ResourceLocation.fromNamespaceAndPath(id.getNamespace(), "block/%s_side".formatted(id.getPath())));
            ItemStack MODEL_SIDE_TAIL = ItemDisplayElementUtil.getModel(ResourceLocation.fromNamespaceAndPath(id.getNamespace(), "block/%s_side_tail".formatted(id.getPath())));

            Map<WallSide, ItemDisplayElement> east = new Object2ObjectOpenHashMap<>();
            east.put(WallSide.NONE, create(ItemStack.EMPTY, new Vector3f(offset, 0f, 0f), 0f, -90f));
            east.put(WallSide.LOW, create(MODEL_SIDE, new Vector3f(offset, 0f, 0f), 0f, -90f));
            east.put(WallSide.TALL, create(MODEL_SIDE_TAIL, new Vector3f(offset, 0f, 0f), 0f, -90f));

            Map<WallSide, ItemDisplayElement> north = new Object2ObjectOpenHashMap<>();
            north.put(WallSide.NONE, create(ItemStack.EMPTY, new Vector3f(0f, 0f, offset), 0f, 180f));
            north.put(WallSide.LOW, create(MODEL_SIDE, new Vector3f(0f, 0f, 0f), 0f, 180f));
            north.put(WallSide.TALL, create(MODEL_SIDE_TAIL, new Vector3f(0f, 0f, offset), 0f, 180f));

            Map<WallSide, ItemDisplayElement> south = new Object2ObjectOpenHashMap<>();
            south.put(WallSide.NONE, create(ItemStack.EMPTY, new Vector3f(offset, 0f, -offset), 0f, 0f));
            south.put(WallSide.LOW, create(MODEL_SIDE, new Vector3f(offset, 0f, 0f), 0f, 0f));
            south.put(WallSide.TALL, create(MODEL_SIDE_TAIL, new Vector3f(offset, 0f, -offset), 0f, 0f));

            Map<WallSide, ItemDisplayElement> west = new Object2ObjectOpenHashMap<>();
            west.put(WallSide.NONE, create(ItemStack.EMPTY, new Vector3f(-offset, 0f, 0f), 0f, 90f));
            west.put(WallSide.LOW, create(MODEL_SIDE, new Vector3f(-offset, 0f, 0f), 0f, 90f));
            west.put(WallSide.TALL, create(MODEL_SIDE_TAIL, new Vector3f(-offset, 0f, 0f), 0f, 90f));

            this.map.put(EAST_WALL_SHAPE, east);
            this.map.put(NORTH_WALL_SHAPE, north);
            this.map.put(SOUTH_WALL_SHAPE, south);
            this.map.put(WEST_WALL_SHAPE, west);

            for (var shapeMap : map.values()) {
                for (var elem : shapeMap.values()) {
                    addElement(elem);
                }
            }

            post = create(MODEL_POST, new Vector3f(0f, 0f, 0f),0,0);
            addElement(post);

            this.updateItem(state);
        }

        private ItemDisplayElement create(ItemStack itemStack, Vector3f translation, float pitch, float yaw) {
            var element = ItemDisplayElementUtil.createSimple(itemStack);
            element.setScale(new Vector3f(1.00275f));
            element.setTranslation(translation);
            element.setRotation(pitch, yaw);
            return element;
        }

        private void updateItem(BlockState state) {
            setVisibility(post, state.getValue(UP));
            for (var entry : map.entrySet()) {
                EnumProperty<WallSide> prop = entry.getKey();
                Map<WallSide, ItemDisplayElement> shapeMap = entry.getValue();

                WallSide shape = state.getValue(prop);

                for (ItemDisplayElement elem : shapeMap.values()) {
                    setVisibility(elem, false);
                }

                ItemDisplayElement visibleElem = shapeMap.get(shape);
                if (visibleElem != null) {
                    setVisibility(visibleElem, true);
                }
            }
        }

        private void setVisibility(ItemDisplayElement elem, boolean visible) {
            elem.setViewRange(visible ? 0.75f : 0f);
        }

        @Override
        public void notifyUpdate(HolderAttachment.UpdateType updateType) {
            this.updateItem(this.blockState());
            this.tick();
            super.notifyUpdate(updateType);
        }
    }
}