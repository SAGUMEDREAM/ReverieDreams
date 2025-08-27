package cc.thonly.reverie_dreams.block;

import cc.thonly.reverie_dreams.Touhou;
import cc.thonly.reverie_dreams.fumo.Fumos;
import cc.thonly.reverie_dreams.sound.SoundEventInit;
import cc.thonly.reverie_dreams.state.ModBlockStateTemplates;
import cc.thonly.reverie_dreams.state.SixteenDirection;
import cc.thonly.reverie_dreams.util.IdentifierGetter;
import com.mojang.serialization.MapCodec;
import eu.pb4.factorytools.api.block.FactoryBlock;
import eu.pb4.factorytools.api.virtualentity.ItemDisplayElementUtil;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import xyz.nucleoid.packettweaker.PacketContext;

@Getter
@Setter
@ToString
public class BasicFumoBlock extends HorizontalFacingBlock implements FactoryBlock, IdentifierGetter {
    public static final MapCodec<BasicFumoBlock> CODEC = createCodec(BasicFumoBlock::new);
    public static final EnumProperty<SixteenDirection> FACING_16 = ModBlockStateTemplates.FACING_16;

    protected Identifier identifier;
    protected Vec3d offsets = new Vec3d(0, 0, 0);

    public BasicFumoBlock(Identifier identifier, Vec3d offsets, Settings settings) {
        super(settings.nonOpaque().registryKey(RegistryKey.of(RegistryKeys.BLOCK, identifier)));
        this.identifier = identifier;
        this.offsets = offsets;
        this.setDefaultState(this.stateManager.getDefaultState().with(FACING_16, SixteenDirection.NORTH));
    }

    public BasicFumoBlock(String path, Vec3d offsets, Settings settings) {
        this(Touhou.id(path), offsets, settings);
    }

    public BasicFumoBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected MapCodec<? extends HorizontalFacingBlock> getCodec() {
        return CODEC;
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        double yaw = ctx.getPlayerYaw();
        SixteenDirection direction = SixteenDirection.fromYaw(yaw);
        return this.getDefaultState().with(FACING_16, direction);
    }

    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING_16);
    }

    @Override
    protected void onProjectileHit(World world, BlockState state, BlockHitResult hit, ProjectileEntity projectile) {
        world.playSound(null, hit.getBlockPos(), SoundEvents.BLOCK_WOOL_PLACE, SoundCategory.BLOCKS, 0.5f, 1);
        super.onProjectileHit(world, state, hit, projectile);
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (!world.isClient) {
            world.playSound(null, pos, SoundEventInit.randomFumo(), SoundCategory.BLOCKS, 1f, 1);
            return ActionResult.SUCCESS_SERVER;
        }
        return ActionResult.SUCCESS;
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        if (!world.isClient) {
            world.playSound(null, pos, SoundEvents.BLOCK_WOOL_PLACE, SoundCategory.BLOCKS, 0.5f, 1);
        }
        super.onPlaced(world, pos, state, placer, itemStack);
    }

    @Override
    public BlockState getPolymerBlockState(BlockState state, PacketContext context) {
        return Blocks.BARRIER.getDefaultState();
    }

    @Override
    public BlockState getPolymerBreakEventBlockState(BlockState state, PacketContext context) {
        return Blocks.WHITE_WOOL.getDefaultState();
    }

    @Override
    protected BlockSoundGroup getSoundGroup(BlockState state) {
        return BlockSoundGroup.WOOL;
    }

    @Override
    public @Nullable ElementHolder createElementHolder(ServerWorld world, BlockPos pos, BlockState initialBlockState) {
        return new Model(initialBlockState, offsets);
    }

    public static final class Model extends ElementHolder {
        private final Block block;
        private final ItemDisplayElement main;

        public Model(BlockState state, Vec3d offsets) {
            this.block = state.getBlock();
            this.main = ItemDisplayElementUtil.createSimple(state.getBlock().asItem());
            this.main.setDisplaySize(this.getDisplaySizeWidth(), this.getDisplaySizeHeight());
            this.main.setOffset(this.modifyOffset(offsets));
            this.main.setScale(this.getScale());
            this.main.setItemDisplayContext(ItemDisplayContext.NONE);
            var yaw = state.get(FACING_16).getYaw();
            this.main.setYaw(yaw);
            this.addElement(this.main);
        }

        public Vec3d modifyOffset(Vec3d offsets) {
            if (this.block == Fumos.TAN_CIRNO.block()) {
                return offsets.add(new Vec3d(0, 0.5, 0));
            }
            return offsets;
        }

        public Vector3f getScale() {
            if (this.block == Fumos.TAN_CIRNO.block()) {
                return new Vector3f(2f);
            }
            return new Vector3f(1f);
        }

        public float getDisplaySizeWidth() {
            return 1f;
        }

        public float getDisplaySizeHeight() {
            return 1f;
        }
    }
}
