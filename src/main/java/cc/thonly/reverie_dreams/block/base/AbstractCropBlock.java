package cc.thonly.reverie_dreams.block.base;

import cc.thonly.polymer.block.model.TransparentTripWire;
import cc.thonly.reverie_dreams.compat.BorukvaFoodCompatImpl;
import cc.thonly.reverie_dreams.inf.IMatureBlock;
import com.mojang.serialization.MapCodec;
import eu.pb4.factorytools.api.block.FactoryBlock;
import eu.pb4.factorytools.api.virtualentity.BlockModel;
import eu.pb4.factorytools.api.virtualentity.ItemDisplayElementUtil;
import eu.pb4.polymer.blocks.api.PolymerTexturedBlock;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.attachment.BlockBoundAttachment;
import eu.pb4.polymer.virtualentity.api.attachment.HolderAttachment;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.redstone.Orientation;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.Map;

@Setter
@Getter
@ToString
public abstract class AbstractCropBlock extends BushBlock implements BonemealableBlock, IMatureBlock, PolymerTexturedBlock, FactoryBlock, TransparentTripWire {
    private final Map<Integer, ItemStack> age2itemStackHolder = new Object2ObjectLinkedOpenHashMap<>();
    protected Item seed;

    protected AbstractCropBlock(Properties settings) {
        super(settings.noOcclusion().noCollission().randomTicks().instabreak().sound(SoundType.CROP));
        this.registerDefaultState(this.stateDefinition.any().setValue(this.getAgeProperty(), 0));
        this.parse();
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(this.getAgeProperty());
    }

    public abstract Integer getMaxAge();

    public abstract IntegerProperty getAgeProperty();

    @Override
    protected abstract MapCodec<? extends BushBlock> codec();

    protected ItemLike getSeedsItem() {
        return this.seed;
    }

    @Override
    protected ItemStack getCloneItemStack(LevelReader world, BlockPos pos, BlockState state, boolean includeData) {
        return new ItemStack(this.getSeedsItem());
    }

    @Override
    protected boolean mayPlaceOn(BlockState floor, BlockGetter world, BlockPos pos) {
        if (!BorukvaFoodCompatImpl.hasBorukvaFood()) {
            return floor.is(Blocks.FARMLAND);
        } else {
            return floor.is(Blocks.FARMLAND) || floor.is(BorukvaFoodCompatImpl.BETTER_FARMLAND);
        }
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader world, BlockPos pos, BlockState state) {
        return !this.isMature(state);
    }

    @Override
    public boolean isBonemealSuccess(Level world, RandomSource random, BlockPos pos, BlockState state) {
        return true;
    }

    public final boolean isMature(BlockState state) {
        return this.getAge(state) >= this.getMaxAge();
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return !this.isMature(state);
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
        int age = this.getAge(state);
        if (age >= this.getMaxAge()) return;

        if (world.getRawBrightness(pos, 0) >= 9) {
            float moisture = getAvailableMoisture(this, world, pos);

            int chance = (int) (15.0f / moisture) + 1;

            if (random.nextInt(chance) == 0) {
                world.setBlock(pos, this.withAge(age + 1), Block.UPDATE_CLIENTS);
            }
        }
    }

    @Override
    protected void neighborChanged(BlockState state, Level world, BlockPos pos, Block sourceBlock, @Nullable Orientation wireOrientation, boolean notify) {
        super.neighborChanged(state, world, pos, sourceBlock, wireOrientation, notify);
        if (!state.canSurvive(world, pos)) {
            world.destroyBlock(pos, true);
        }
    }

    @Override
    public void performBonemeal(ServerLevel world, RandomSource random, BlockPos pos, BlockState state) {
        this.applyGrowth(world, pos, state);
    }

    public void applyGrowth(Level world, BlockPos pos, BlockState state) {
        int i = Math.min(this.getMaxAge(), this.getAge(state) + this.getGrowthAmount(world));
        world.setBlock(pos, this.withAge(i), Block.UPDATE_CLIENTS);
    }

    protected int getGrowthAmount(Level world) {
        return Mth.nextInt(world.random, 1, 3);
    }

    public BlockState withAge(int age) {
        return (BlockState) this.defaultBlockState().setValue(this.getAgeProperty(), age);
    }

    public int getAge(BlockState state) {
        return state.getValue(this.getAgeProperty());
    }

    protected static float getAvailableMoisture(Block block, BlockGetter world, BlockPos pos) {
        boolean bl2;
        float f = 1.0f;
        BlockPos blockPos = pos.below();
        for (int i = -1; i <= 1; ++i) {
            for (int j = -1; j <= 1; ++j) {
                float g = 0.0f;
                BlockState blockState = world.getBlockState(blockPos.offset(i, 0, j));
                if (blockState.is(Blocks.FARMLAND)) {
                    g = 1.0f;
                    if (blockState.getValue(FarmBlock.MOISTURE) > 0) {
                        g = 3.0f;
                    }
                }
                if (i != 0 || j != 0) {
                    g /= 4.0f;
                }
                f += g;
            }
        }
        BlockPos blockPos2 = pos.north();
        BlockPos blockPos3 = pos.south();
        BlockPos blockPos4 = pos.west();
        BlockPos blockPos5 = pos.east();
        boolean bl = world.getBlockState(blockPos4).is(block) || world.getBlockState(blockPos5).is(block);
        boolean bl3 = bl2 = world.getBlockState(blockPos2).is(block) || world.getBlockState(blockPos3).is(block);
        if (bl && bl2) {
            f /= 2.0f;
        } else {
            boolean bl32;
            boolean bl4 = bl32 = world.getBlockState(blockPos4.north()).is(block) || world.getBlockState(blockPos5.north()).is(block) || world.getBlockState(blockPos5.south()).is(block) || world.getBlockState(blockPos4.south()).is(block);
            if (bl32) {
                f /= 2.0f;
            }
        }
        return f;
    }

    void parse() {
        assert properties.id != null;
        ResourceLocation key = properties.id.location();
        for (int index = 0; index <= this.getMaxAge(); index++) {
            String modelId = "%s:block/%s_stage%s".formatted(key.getNamespace(), key.getPath(), index);
            ItemStack model = ItemDisplayElementUtil.getModel(ResourceLocation.parse(modelId));
//            System.out.println(modelId);
            this.age2itemStackHolder.put(index, model);
        }
//        System.out.println(this.age2itemStackHolder);
    }

    @Override
    public BlockState getPolymerBreakEventBlockState(BlockState state, PacketContext context) {
        return Blocks.WHEAT.defaultBlockState();
    }


    @Override
    public @Nullable ElementHolder createElementHolder(ServerLevel world, BlockPos pos, BlockState initialBlockState) {
        return new Model(world, pos, initialBlockState);
    }

    public class Model extends BlockModel {
        private final ServerLevel world;
        private final BlockPos blockPos;
        private final BlockState blockState;
        public ItemDisplayElement main;

        public Model(ServerLevel world, BlockPos pos, BlockState initialBlockState) {
            this.world = world;
            this.blockPos = pos;
            this.blockState = initialBlockState;
            init(initialBlockState);
        }

        public void init(BlockState state) {
            this.main = ItemDisplayElementUtil.createSimple();
            updateItem(state);
            this.main.setScale(new Vector3f(1));
            this.addElement(main);
        }

        protected void updateItem(BlockState state) {
            int age = state.getValue(AbstractCropBlock.this.getAgeProperty());
            this.main.setItem(AbstractCropBlock.this.age2itemStackHolder.getOrDefault(age, new ItemStack(Items.BARRIER)));
        }

        @Override
        public void notifyUpdate(HolderAttachment.UpdateType updateType) {
            if (updateType == BlockBoundAttachment.BLOCK_STATE_UPDATE) {
                updateItem(this.blockState());
                this.tick();
            }
            super.notifyUpdate(updateType);
        }
    }

}
