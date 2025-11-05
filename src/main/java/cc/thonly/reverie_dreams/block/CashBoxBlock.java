package cc.thonly.reverie_dreams.block;

import cc.thonly.reverie_dreams.block.entity.CustomChestBlockEntity;
import cc.thonly.reverie_dreams.gui.CustomChestBlockGui;
import cc.thonly.reverie_dreams.registry.content.item.RDItems;
import cc.thonly.reverie_dreams.server.player.FaithComponent;
import cc.thonly.reverie_dreams.server.player.PlayerComponent;
import cc.thonly.reverie_dreams.server.player.PlayerDataComponentManager;
import cc.thonly.reverie_dreams.util.PredicateSlot;
import com.mojang.serialization.MapCodec;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class CashBoxBlock extends HorizontalDirectionalBlock implements EntityBlock {
    public static final MapCodec<CashBoxBlock> CODEC = simpleCodec(CashBoxBlock::new);

    public CashBoxBlock(Properties settings) {
        super(settings);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState playerWillDestroy(Level world, BlockPos pos, BlockState state, Player player) {
        if (!world.isClientSide() && world instanceof ServerLevel serverWorld) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof CustomChestBlockEntity customChestBlockEntity) {
                SimpleContainer inventory = customChestBlockEntity.getInventory();
                for (int i = 0; i < inventory.getContainerSize(); i++) {
                    ItemStack stack = inventory.getItem(i);
                    if (stack.isEmpty()) {
                        continue;
                    }
                    ItemEntity itemEntity = new ItemEntity(serverWorld, pos.getX(), pos.getY(), pos.getZ(), stack);
                    serverWorld.addFreshEntity(itemEntity);
                }
            }
        }
        return super.playerWillDestroy(world, pos, state, player);
    }

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
        if (!world.isClientSide && player instanceof ServerPlayer serverPlayer) {
            if (!(world.getBlockEntity(pos) instanceof CustomChestBlockEntity chestBlockEntity)) {
                return InteractionResult.FAIL;
            }
            PlayerDataComponentManager playerDataComponentManager = PlayerDataComponentManager.getInstance();
            ItemStack itemStack = player.getItemInHand(InteractionHand.MAIN_HAND);
            int cal = calValue(itemStack);
            if (cal > 0) {
                PlayerComponent<FaithComponent> faithComponents = playerDataComponentManager.getOrCreatePlayerComponent(serverPlayer, FaithComponent.class);
                FaithComponent faithComponent = faithComponents.get();
                long timeOfDay = world.getDayTime();
                long dayCount = timeOfDay / 24000;
                long dateOfLastPrayer = faithComponent.getDateOfLastPrayer();
                if (dayCount != dateOfLastPrayer) {
                    int base = faithComponent.getFaithValue() + cal;
                    int val = (int) (base + 7 * 1.5f * world.random.nextDouble());
                    if (base > FaithComponent.MAX_VALUE) {
                        faithComponent.setFaithValue(FaithComponent.MAX_VALUE);
                        player.playNotifySound(SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.BLOCKS, 1.0f, 1.0f);
                        player.displayClientMessage(Component.translatable("item.action.click.cashbox.fails.full", FaithComponent.MAX_VALUE), true);
                        return InteractionResult.SUCCESS_SERVER;
                    }
                    faithComponent.setFaithValue(val);
                    faithComponent.setDateOfLastPrayer(dayCount);
                    itemStack.consume(world.random.nextIntBetweenInclusive(1, 3), player);
                    player.playNotifySound(SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.BLOCKS, 1.0f, 1.0f);
                    player.displayClientMessage(Component.translatable("item.action.click.cashbox.success", val), true);
                    return InteractionResult.SUCCESS_SERVER;
                }
                player.displayClientMessage(Component.translatable("item.action.click.cashbox.fails.used"), true);
                return InteractionResult.SUCCESS_SERVER;
            } else {
                SimpleGui chestGui = new CustomChestBlockGui(this, chestBlockEntity, serverPlayer, (inventory, index, x, y) -> new PredicateSlot(inventory, index, x, y, (stack) -> CustomChestBlockGui.COIN_ITEMS.contains(stack.getItem())));
                chestGui.open();
                return InteractionResult.SUCCESS_SERVER;
            }
        }
        return InteractionResult.SUCCESS;
    }

    protected static int calValue(ItemStack itemStack) {
        int cal = 0;
        if (itemStack.getItem() == RDItems.COPPER_COIN) {
            cal += 1;
        }
        if (itemStack.getItem() == RDItems.SILVER_COIN) {
            cal += 3;
        }
        if (itemStack.getItem() == RDItems.GOLD_COIN) {
            cal += 8;
        }
        return cal;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return this.defaultBlockState().setValue(FACING, ctx.getHorizontalDirection());
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CustomChestBlockEntity(pos, state);
    }
}
