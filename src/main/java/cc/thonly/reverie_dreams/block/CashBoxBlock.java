package cc.thonly.reverie_dreams.block;

import cc.thonly.reverie_dreams.block.entity.CustomChestBlockEntity;
import cc.thonly.reverie_dreams.gui.CustomChestBlockGui;
import cc.thonly.reverie_dreams.item.ModItems;
import cc.thonly.reverie_dreams.item.weapon.HakureiCane;
import cc.thonly.reverie_dreams.item.weapon.WindBlessingCane;
import cc.thonly.reverie_dreams.server.player.FaithComponent;
import cc.thonly.reverie_dreams.server.player.PlayerComponent;
import cc.thonly.reverie_dreams.server.player.PlayerDataComponentManager;
import com.mojang.serialization.MapCodec;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class CashBoxBlock extends HorizontalFacingBlock implements BlockEntityProvider {
    public static final MapCodec<CashBoxBlock> CODEC = createCodec(CashBoxBlock::new);

    public CashBoxBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState().with(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends HorizontalFacingBlock> getCodec() {
        return CODEC;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        if (!world.isClient() && world instanceof ServerWorld serverWorld) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof CustomChestBlockEntity customChestBlockEntity) {
                SimpleInventory inventory = customChestBlockEntity.getInventory();
                for (int i = 0; i < inventory.size(); i++) {
                    ItemStack stack = inventory.getStack(i);
                    if (stack.isEmpty()) {
                        continue;
                    }
                    ItemEntity itemEntity = new ItemEntity(serverWorld, pos.getX(), pos.getY(), pos.getZ(), stack);
                    serverWorld.spawnEntity(itemEntity);
                }
            }
        }
        return super.onBreak(world, pos, state, player);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (!world.isClient && player instanceof ServerPlayerEntity serverPlayer) {
            if (!(world.getBlockEntity(pos) instanceof CustomChestBlockEntity chestBlockEntity)) {
                return ActionResult.FAIL;
            }
            PlayerDataComponentManager playerDataComponentManager = PlayerDataComponentManager.getInstance();
            ItemStack itemStack = player.getStackInHand(Hand.MAIN_HAND);
            int cal = calValue(itemStack);
            if (cal > 0) {
                PlayerComponent<FaithComponent> faithComponents = playerDataComponentManager.getOrCreatePlayerComponent(serverPlayer, FaithComponent.class);
                FaithComponent faithComponent = faithComponents.get();
                long timeOfDay = world.getTimeOfDay();
                long dayCount = timeOfDay / 24000;
                long dateOfLastPrayer = faithComponent.getDateOfLastPrayer();
                if (dayCount != dateOfLastPrayer) {
                    int base = faithComponent.getFaithValue() + cal;
                    int val = (int) (base + 7 * 1.5f * world.random.nextDouble());
                    if (base > FaithComponent.MAX_VALUE) {
                        faithComponent.setFaithValue(FaithComponent.MAX_VALUE);
                        player.playSoundToPlayer(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.BLOCKS, 1.0f, 1.0f);
                        player.sendMessage(Text.translatable("item.action.click.cashbox.fails.full", FaithComponent.MAX_VALUE), true);
                        return ActionResult.SUCCESS_SERVER;
                    }
                    faithComponent.setFaithValue(val);
                    faithComponent.setDateOfLastPrayer(dayCount);
                    itemStack.decrementUnlessCreative(world.random.nextBetween(1, 3), player);
                    player.playSoundToPlayer(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.BLOCKS, 1.0f, 1.0f);
                    player.sendMessage(Text.translatable("item.action.click.cashbox.success", val), true);
                    return ActionResult.SUCCESS_SERVER;
                }
                player.sendMessage(Text.translatable("item.action.click.cashbox.fails.used"), true);
                return ActionResult.SUCCESS_SERVER;
            } else {
                SimpleGui chestGui = new CustomChestBlockGui(this, chestBlockEntity, serverPlayer);
                chestGui.open();
                return ActionResult.SUCCESS_SERVER;
            }
        }
        return ActionResult.SUCCESS;
    }

    protected static int calValue(ItemStack itemStack) {
        int cal = 0;
        if (itemStack.getItem() == ModItems.COPPER_COIN) {
            cal += 1;
        }
        if (itemStack.getItem() == ModItems.SILVER_COIN) {
            cal += 3;
        }
        if (itemStack.getItem() == ModItems.GOLD_COIN) {
            cal += 8;
        }
        return cal;
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return this.getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing());
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new CustomChestBlockEntity(pos, state);
    }
}
