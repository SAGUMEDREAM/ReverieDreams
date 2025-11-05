package cc.thonly.reverie_dreams.block;

import cc.thonly.reverie_dreams.block.entity.FoodDisplayBlockEntity;
import cc.thonly.reverie_dreams.block.entity.RDBlockEntityTypes;
import cc.thonly.reverie_dreams.interfaces.IItemStack;
import cc.thonly.reverie_dreams.recipe.ItemStackWrapper;
import com.mojang.serialization.MapCodec;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.UseRemainder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

@Setter
@Getter
public class FoodDisplayBlock extends BaseEntityBlock {
    public static final MapCodec<FoodDisplayBlock> CODEC = FoodDisplayBlock.simpleCodec(FoodDisplayBlock::new);

    public FoodDisplayBlock(Properties settings) {
        super(settings);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
        ItemStack stack = player.getMainHandItem();
        if (!world.isClientSide && world instanceof ServerLevel serverWorld) {
            if (player.isShiftKeyDown()) {
                if (!(serverWorld.getBlockEntity(pos) instanceof FoodDisplayBlockEntity isdBlockEntity)) {
                    return InteractionResult.PASS;
                }
                boolean isFood = ((IItemStack) (Object) isdBlockEntity.getItem().getItemStack()).isFood();
                if (isFood) {
                    ItemStack contentStack = isdBlockEntity.getItem().getItemStack();
                    Consumable consumableComponent = contentStack.get(DataComponents.CONSUMABLE);
                    UseRemainder useRemainderComponent = contentStack.get(DataComponents.USE_REMAINDER);
                    contentStack.finishUsingItem(serverWorld, player);
                    if (consumableComponent != null) {
                        Holder<SoundEvent> sound = consumableComponent.sound();
                        world.playSound(null, player.blockPosition(), sound.value(), SoundSource.BLOCKS, 1.0f, 1.0f);
                    }
                    contentStack.consume(1, player);
                    if (useRemainderComponent != null && !player.hasInfiniteMaterials()) {
                        ItemStack itemStack = useRemainderComponent.convertIntoRemainder(contentStack, contentStack.getCount(), player.hasInfiniteMaterials(), player::handleExtraItemsCreatedOnUse);
                        isdBlockEntity.setItem(ItemStackWrapper.of(itemStack));
                    }
                    isdBlockEntity.update();
                    serverWorld.sendBlockUpdated(pos, state, state, Block.UPDATE_ALL);
                    isdBlockEntity.setChanged();
                    return InteractionResult.SUCCESS_SERVER;
                }
            } else {
                if (serverWorld.getBlockEntity(pos) instanceof FoodDisplayBlockEntity isdBlockEntity) {
                    ItemStackWrapper item = isdBlockEntity.getItem();
                    if (!stack.isEmpty() && item.isEmpty()) {
                        ItemStackWrapper itemStackWrapper = ItemStackWrapper.of(stack.copy());
                        itemStackWrapper.getItemStack().setCount(1);
                        stack.consume(1, player);
                        isdBlockEntity.setItem(itemStackWrapper);
                        isdBlockEntity.setYaw(player.getYRot());
                        isdBlockEntity.update();
                        isdBlockEntity.setChanged();
                    } else {
                        ItemEntity itemEntity = new ItemEntity(serverWorld, pos.getX(), pos.getY(), pos.getZ(), item.getItemStack(), 0, 0.2, 0);
                        isdBlockEntity.setItem(ItemStackWrapper.empty());
                        serverWorld.addFreshEntity(itemEntity);
                        isdBlockEntity.update();
                    }
                    serverWorld.sendBlockUpdated(pos, state, state, Block.UPDATE_ALL);
                    isdBlockEntity.setChanged();
                    isdBlockEntity.update();
                }
            }
            return InteractionResult.SUCCESS_SERVER;
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public BlockState playerWillDestroy(Level world, BlockPos pos, BlockState state, Player player) {
        if (!world.isClientSide() && world instanceof ServerLevel serverWorld && world.getBlockEntity(pos) instanceof FoodDisplayBlockEntity isdBlockEntity) {
            ItemStackWrapper item = isdBlockEntity.getItem();
            if (!item.isEmpty()) {
                ItemEntity itemEntity = new ItemEntity(world, pos.getX(), pos.getY(), pos.getZ(), item.getItemStack(), 0, 0.2, 0);
                isdBlockEntity.setItem(ItemStackWrapper.empty());
                serverWorld.addFreshEntity(itemEntity);
            }
        }
        return super.playerWillDestroy(world, pos, state, player);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FoodDisplayBlockEntity(pos, state);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, RDBlockEntityTypes.ITEM_DISPLAY_BLOCK_ENTITY, FoodDisplayBlockEntity::tick);
    }

}
