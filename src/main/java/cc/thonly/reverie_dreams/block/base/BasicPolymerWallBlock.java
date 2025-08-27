package cc.thonly.reverie_dreams.block.base;

import cc.thonly.reverie_dreams.Touhou;
import cc.thonly.reverie_dreams.util.IdentifierGetter;
import eu.pb4.factorytools.api.block.FactoryBlock;
import eu.pb4.factorytools.api.block.model.generic.BSMMParticleBlock;
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
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.WallBlock;
import net.minecraft.block.enums.WallShape;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.Map;

@Setter
@Getter
public class BasicPolymerWallBlock extends WallBlock implements FactoryBlock, PolymerTexturedBlock, IdentifierGetter {
    private final Identifier identifier;

    public BasicPolymerWallBlock(Identifier identifier, Settings settings) {
        super(settings.registryKey(RegistryKey.of(RegistryKeys.BLOCK, identifier)));
        this.identifier = identifier;
    }

    public BasicPolymerWallBlock(String path, Settings settings) {
        this(Touhou.id(path), settings);
    }

    @Override
    public BlockState getPolymerBlockState(BlockState state, PacketContext context) {
        return Blocks.STONE_BRICK_WALL.getStateWithProperties(state);
    }

    @Override
    public BlockState getPolymerBreakEventBlockState(BlockState state, PacketContext context) {
        return FactoryBlock.super.getPolymerBreakEventBlockState(state, context);
    }

    @Override
    public @Nullable ElementHolder createElementHolder(ServerWorld world, BlockPos pos, BlockState initialBlockState) {
        return new Model(initialBlockState, this.getIdentifier());
//        return ShiftyBlockStateModel.longRange(initialBlockState, pos);
    }

    public static final class Model extends BlockModel {
        public final ItemDisplayElement post;
        public static final EnumProperty<WallShape> EAST_WALL_SHAPE = WallBlock.EAST_WALL_SHAPE;
        public static final EnumProperty<WallShape> NORTH_WALL_SHAPE = WallBlock.NORTH_WALL_SHAPE;
        public static final EnumProperty<WallShape> SOUTH_WALL_SHAPE = WallBlock.SOUTH_WALL_SHAPE;
        public static final EnumProperty<WallShape> WEST_WALL_SHAPE = WallBlock.WEST_WALL_SHAPE;
        public static final BooleanProperty up = WallBlock.UP;
        public final Map<EnumProperty<WallShape>, Map<WallShape, ItemDisplayElement>> map = new Object2ObjectOpenHashMap<>();

        public Model(BlockState state, Identifier id) {
            var offset = 0;
            ItemStack MODEL_POST = ItemDisplayElementUtil.getModel(Identifier.of(id.getNamespace(), "block/%s_post".formatted(id.getPath())));
            ItemStack MODEL_SIDE = ItemDisplayElementUtil.getModel(Identifier.of(id.getNamespace(), "block/%s_side".formatted(id.getPath())));
            ItemStack MODEL_SIDE_TAIL = ItemDisplayElementUtil.getModel(Identifier.of(id.getNamespace(), "block/%s_side_tail".formatted(id.getPath())));

            Map<WallShape, ItemDisplayElement> east = new Object2ObjectOpenHashMap<>();
            east.put(WallShape.NONE, create(ItemStack.EMPTY, new Vector3f(offset, 0f, 0f), 0f, -90f));
            east.put(WallShape.LOW, create(MODEL_SIDE, new Vector3f(offset, 0f, 0f), 0f, -90f));
            east.put(WallShape.TALL, create(MODEL_SIDE_TAIL, new Vector3f(offset, 0f, 0f), 0f, -90f));

            Map<WallShape, ItemDisplayElement> north = new Object2ObjectOpenHashMap<>();
            north.put(WallShape.NONE, create(ItemStack.EMPTY, new Vector3f(0f, 0f, offset), 0f, 180f));
            north.put(WallShape.LOW, create(MODEL_SIDE, new Vector3f(0f, 0f, 0f), 0f, 180f));
            north.put(WallShape.TALL, create(MODEL_SIDE_TAIL, new Vector3f(0f, 0f, offset), 0f, 180f));

            Map<WallShape, ItemDisplayElement> south = new Object2ObjectOpenHashMap<>();
            south.put(WallShape.NONE, create(ItemStack.EMPTY, new Vector3f(offset, 0f, -offset), 0f, 0f));
            south.put(WallShape.LOW, create(MODEL_SIDE, new Vector3f(offset, 0f, 0f), 0f, 0f));
            south.put(WallShape.TALL, create(MODEL_SIDE_TAIL, new Vector3f(offset, 0f, -offset), 0f, 0f));

            Map<WallShape, ItemDisplayElement> west = new Object2ObjectOpenHashMap<>();
            west.put(WallShape.NONE, create(ItemStack.EMPTY, new Vector3f(-offset, 0f, 0f), 0f, 90f));
            west.put(WallShape.LOW, create(MODEL_SIDE, new Vector3f(-offset, 0f, 0f), 0f, 90f));
            west.put(WallShape.TALL, create(MODEL_SIDE_TAIL, new Vector3f(-offset, 0f, 0f), 0f, 90f));

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
            setVisibility(post, state.get(UP));
            for (var entry : map.entrySet()) {
                EnumProperty<WallShape> prop = entry.getKey();
                Map<WallShape, ItemDisplayElement> shapeMap = entry.getValue();

                WallShape shape = state.get(prop);

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
