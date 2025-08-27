//package cc.thonly.mystias_izakaya.block;
//
//import cc.thonly.mystias_izakaya.MystiasIzakaya;
//import cc.thonly.mystias_izakaya.block.entity.CooktopBlockEntity;
//import cc.thonly.mystias_izakaya.block.entity.KitchenwareBlockEntity;
////import cc.thonly.mystias_izakaya.gui.recipe.block.CooktopBlockGui;
//import cc.thonly.reverie_dreams.util.IdentifierGetter;
//import com.mojang.serialization.MapCodec;
//import eu.pb4.factorytools.api.block.FactoryBlock;
//import eu.pb4.factorytools.api.virtualentity.BlockModel;
//import eu.pb4.factorytools.api.virtualentity.ItemDisplayElementUtil;
//import eu.pb4.polymer.blocks.api.PolymerTexturedBlock;
//import eu.pb4.polymer.virtualentity.api.ElementHolder;
//import eu.pb4.polymer.virtualentity.api.attachment.BlockBoundAttachment;
//import eu.pb4.polymer.virtualentity.api.attachment.HolderAttachment;
//import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
//import lombok.Getter;
//import lombok.Setter;
//import lombok.ToString;
//import net.minecraft.block.Block;
//import net.minecraft.block.BlockState;
//import net.minecraft.block.BlockWithEntity;
//import net.minecraft.block.Blocks;
//import net.minecraft.block.entity.BlockEntity;
//import net.minecraft.block.entity.BlockEntityTicker;
//import net.minecraft.block.entity.BlockEntityType;
//import net.minecraft.entity.ItemEntity;
//import net.minecraft.entity.entity.PlayerEntity;
//import net.minecraft.inventory.SimpleInventory;
//import net.minecraft.item.ItemPlacementContext;
//import net.minecraft.item.ItemStack;
//import net.minecraft.registry.RegistryKey;
//import net.minecraft.registry.RegistryKeys;
//import net.minecraft.server.network.ServerPlayerEntity;
//import net.minecraft.server.world.ServerWorld;
//import net.minecraft.state.StateManager;
//import net.minecraft.state.property.BooleanProperty;
//import net.minecraft.state.property.EnumProperty;
//import net.minecraft.state.property.Properties;
//import net.minecraft.util.*;
//import net.minecraft.util.hit.BlockHitResult;
//import net.minecraft.util.math.BlockPos;
//import net.minecraft.util.math.Direction;
//import net.minecraft.world.World;
//import org.jetbrains.annotations.Nullable;
//import org.joml.Vector3f;
//import xyz.nucleoid.packettweaker.PacketContext;
//
//@Setter
//@Getter
//@ToString
//@Deprecated
//public class CooktopBlock extends BlockWithEntity implements PolymerTexturedBlock, FactoryBlock, IdentifierGetter {
//    public static final MapCodec<CooktopBlock> CODEC = createCodec(CooktopBlock::new);
//    public static final EnumProperty<Direction> FACING = Properties.HORIZONTAL_FACING;
//    public static final BooleanProperty LIT = Properties.LIT;
//    private Identifier identifier;
//
//    public CooktopBlock(Settings settings) {
//        super(settings);
//    }
//
//    public CooktopBlock(String id, Settings settings) {
//        this(MystiasIzakaya.id(id), settings);
//    }
//
//    public CooktopBlock(Identifier identifier, Settings settings) {
//        super(settings.registryKey(RegistryKey.of(RegistryKeys.BLOCK, identifier)));
//        this.identifier = identifier;
//        this.setDefaultState(this.stateManager.getDefaultState().with(FACING, Direction.NORTH));
//        this.setDefaultState(this.stateManager.getDefaultState().with(LIT, false));
//    }
//
//    public static int getLuminance(BlockState blockState) {
//        Block block = blockState.getBlock();
//        if (block instanceof CooktopBlock) {
//            return blockState.get(LIT) ? 11 : 0;
//        }
//        return 0;
//    }
//
//    @Override
//    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity entity, BlockHitResult hit) {
//        if (!world.isClient()) {
//            ServerPlayerEntity serverPlayer = (ServerPlayerEntity) entity;
//            BlockEntity blockEntity = world.getBlockEntity(pos);
//            if (blockEntity instanceof CooktopBlockEntity cooktopBlockEntity) {
//                if (serverPlayer.isSneaking()) {
//                    cooktopBlockEntity.use();
//                } else {
//                    CooktopBlockGui simple = new CooktopBlockGui(serverPlayer, cooktopBlockEntity);
//                    simple.open();
//                }
//            }
//            serverPlayer.swingHand(Hand.MAIN_HAND);
//            return ActionResult.SUCCESS_SERVER;
//        }
//        return super.onUse(state, world, pos, entity, hit);
//    }
//
//    @Override
//    public BlockState onBreak(World world, BlockPos pos, BlockState state, PlayerEntity entity) {
//        if (!world.isClient() && world instanceof ServerWorld serverWorld) {
//            BlockEntity blockEntity = world.getBlockEntity(pos);
//            if (blockEntity instanceof CooktopBlockEntity cooktopBlockEntity) {
//                SimpleInventory inventory = cooktopBlockEntity.getInventory();
//                for (int i = 0; i < inventory.size(); i++) {
//                    ItemStack stack = inventory.getStack(i);
//                    if (stack.isEmpty()) {
//                        continue;
//                    }
//                    ItemEntity itemEntity = new ItemEntity(serverWorld, pos.getX(), pos.getY(), pos.getZ(), stack);
//                    serverWorld.spawnEntity(itemEntity);
//                }
//            }
//        }
//        return super.onBreak(world, pos, state, entity);
//    }
//
//    @Override
//    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
//        return new CooktopBlockEntity(pos, state);
//    }
//
//    @Override
//    protected MapCodec<? extends BlockWithEntity> getCodec() {
//        return CODEC;
//    }
//
//    @Override
//    public BlockState getPolymerBlockState(BlockState state, PacketContext packetContext) {
//        return Blocks.BARRIER.getDefaultState();
//    }
//
//    @Override
//    protected BlockState rotate(BlockState state, BlockRotation rotation) {
//        return state.with(FACING, rotation.rotate(state.get(FACING)));
//    }
//
//    @Override
//    protected BlockState mirror(BlockState state, BlockMirror mirror) {
//        return state.rotate(mirror.getRotation(state.get(FACING)));
//    }
//
//    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
//        super.appendProperties(builder);
//        builder.add(FACING);
//        builder.add(LIT);
//    }
//
//    @Override
//    public @Nullable ElementHolder createElementHolder(ServerWorld world, BlockPos pos, BlockState initialBlockState) {
//        return new Model(initialBlockState);
//    }
//
//    @Nullable
//    @Override
//    public BlockState getPlacementState(ItemPlacementContext ctx) {
//        return this.getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing().getOpposite());
//    }
//
//    @Override
//    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
//        return validateTicker(type, MiBlockEntities.COOKTOP_BLOCK_ENTITY, CooktopBlockEntity::tick);
//    }
//
//    public static final class Model extends BlockModel {
//        public static final ItemStack LIT_FALSE = ItemDisplayElementUtil.getModel(Identifier.of(MystiasIzakaya.MOD_ID, "block/cooktop"));
//        public static final ItemStack LIT_TRUE = ItemDisplayElementUtil.getModel(Identifier.of(MystiasIzakaya.MOD_ID, "block/cooktop_on"));
//
//        public ItemDisplayElement stove;
//
//        public Model(BlockState state) {
//            init(state);
//        }
//
//        public void init(BlockState state) {
//            this.stove = state.get(LIT) ? ItemDisplayElementUtil.createSimple(LIT_TRUE) : ItemDisplayElementUtil.createSimple(LIT_FALSE);
//            this.stove.setScale(new Vector3f(2));
//            this.updateStatePos(state);
//            this.addElement(stove);
//        }
//
//        private void updateStatePos(BlockState state) {
//            var direction = state.get(FACING);
//            this.stove.setYaw(direction.getPositiveHorizontalDegrees());
//        }
//
//        private void updateItem(BlockState state) {
//            this.stove.setItem(state.get(LIT) ? LIT_TRUE : LIT_FALSE);
//        }
//
//        @Override
//        public void notifyUpdate(HolderAttachment.UpdateType updateType) {
//            if (updateType == BlockBoundAttachment.BLOCK_STATE_UPDATE) {
//                updateStatePos(this.blockState());
//                updateItem(this.blockState());
//                this.tick();
//            }
//            super.notifyUpdate(updateType);
//        }
//    }
//}
