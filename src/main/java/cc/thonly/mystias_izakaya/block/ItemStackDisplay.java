package cc.thonly.mystias_izakaya.block;

import cc.thonly.mystias_izakaya.block.entity.ItemStackDisplayBlockEntity;
import cc.thonly.reverie_dreams.interfaces.IItemStack;
import cc.thonly.reverie_dreams.recipe.ItemStackWrapper;
import com.mojang.serialization.MapCodec;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ConsumableComponent;
import net.minecraft.component.type.UseRemainderComponent;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

@Setter
@Getter
public class ItemStackDisplay extends BlockWithEntity {
    public static final MapCodec<ItemStackDisplay> CODEC = ItemStackDisplay.createCodec(ItemStackDisplay::new);

    public ItemStackDisplay(Settings settings) {
        super(settings);
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        ItemStack stack = player.getMainHandStack();
        if (stack != null && !world.isClient && world instanceof ServerWorld serverWorld) {
            if (player.isSneaking()) {
                if (!(serverWorld.getBlockEntity(pos) instanceof ItemStackDisplayBlockEntity isdBlockEntity)) {
                    return ActionResult.PASS;
                }
                boolean isFood = ((IItemStack) (Object) isdBlockEntity.getItem().getItemStack()).isFood();
                if (isFood) {
                    ItemStack contentStack = isdBlockEntity.getItem().getItemStack();
                    ConsumableComponent consumableComponent = contentStack.get(DataComponentTypes.CONSUMABLE);
                    UseRemainderComponent useRemainderComponent = contentStack.get(DataComponentTypes.USE_REMAINDER);
                    contentStack.finishUsing(serverWorld, player);
                    if (consumableComponent != null) {
                        RegistryEntry<SoundEvent> sound = consumableComponent.sound();
                        world.playSound(null, player.getBlockPos(), sound.value(), SoundCategory.BLOCKS, 1.0f, 1.0f);
                    }
                    contentStack.decrementUnlessCreative(1, player);
                    if (useRemainderComponent != null && !player.isInCreativeMode()) {
                        ItemStack itemStack = useRemainderComponent.convert(contentStack, contentStack.getCount(), player.isInCreativeMode(), player::giveOrDropStack);
                        isdBlockEntity.setItem(ItemStackWrapper.of(itemStack));
                    }
                    isdBlockEntity.update();
                    serverWorld.updateListeners(pos, state, state, Block.NOTIFY_ALL);
                    isdBlockEntity.markDirty();
                    return ActionResult.SUCCESS_SERVER;
                }
            } else {
                if (serverWorld.getBlockEntity(pos) instanceof ItemStackDisplayBlockEntity isdBlockEntity) {
                    ItemStackWrapper item = isdBlockEntity.getItem();
                    if (!stack.isEmpty() && item.isEmpty()) {
                        ItemStackWrapper itemStackWrapper = ItemStackWrapper.of(stack.copy());
                        itemStackWrapper.getItemStack().setCount(1);
                        stack.decrementUnlessCreative(1, player);
                        isdBlockEntity.setItem(itemStackWrapper);
                        isdBlockEntity.setYaw(player.getYaw());
                        isdBlockEntity.update();
                        isdBlockEntity.markDirty();
                    } else {
                        ItemEntity itemEntity = new ItemEntity(serverWorld, pos.getX(), pos.getY(), pos.getZ(), item.getItemStack(), 0, 0.2, 0);
                        isdBlockEntity.setItem(ItemStackWrapper.empty());
                        serverWorld.spawnEntity(itemEntity);
                    }
                    serverWorld.updateListeners(pos, state, state, Block.NOTIFY_ALL);
                    isdBlockEntity.markDirty();
                }
            }
            return ActionResult.SUCCESS_SERVER;
        }
        return ActionResult.SUCCESS;
    }

    @Override
    public BlockState onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        if (!world.isClient() && world instanceof ServerWorld serverWorld && world.getBlockEntity(pos) instanceof ItemStackDisplayBlockEntity isdBlockEntity) {
            ItemStackWrapper item = isdBlockEntity.getItem();
            if (!item.isEmpty()) {
                ItemEntity itemEntity = new ItemEntity(world, pos.getX(), pos.getY(), pos.getZ(), item.getItemStack(), 0, 0.2, 0);
                isdBlockEntity.setItem(ItemStackWrapper.empty());
                serverWorld.spawnEntity(itemEntity);
            }
        }
        return super.onBreak(world, pos, state, player);
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new ItemStackDisplayBlockEntity(pos, state);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return validateTicker(type, MIBlockEntities.ITEM_DISPLAY_BLOCK_ENTITY, ItemStackDisplayBlockEntity::tick);
    }

}
