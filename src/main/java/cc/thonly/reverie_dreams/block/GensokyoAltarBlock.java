package cc.thonly.reverie_dreams.block;

import cc.thonly.reverie_dreams.block.entity.GensokyoAltarBlockEntity;
import cc.thonly.reverie_dreams.block.entity.RDBlockEntityTypes;
import cc.thonly.reverie_dreams.gui.recipe.gui.GensokyoAltarGui;
import cc.thonly.reverie_dreams.recipe.ItemStackWrapper;
import cc.thonly.reverie_dreams.recipe.entry.GensokyoAltarRecipe;
import cc.thonly.reverie_dreams.recipe.type.GensokyoAltarRecipeType;
import cc.thonly.reverie_dreams.registry.content.block.RDWoodBlocks;
import cc.thonly.reverie_dreams.util.advancements.SimpleTriggerFactory;
import cc.thonly.reverie_dreams.util.advancements.SimpleTriggerKeys;
import com.mojang.serialization.MapCodec;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Getter
public class GensokyoAltarBlock extends BaseEntityBlock {
    public static final int[][] OFFSETS = {
            {0, -4}, {-3, -3}, {3, -3},
            {-4, 0}, {4, 0},
            {-3, 3}, {3, 3}, {0, 4}, {0, 0}
    };


    public GensokyoAltarBlock(Properties settings) {
        super(settings);
    }

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
        if (!world.isClientSide() && player instanceof ServerPlayer serverPlayer && world.getBlockEntity(pos) instanceof GensokyoAltarBlockEntity blockEntity) {
            boolean b = canUse(world, pos);
            player.swing(player.getUsedItemHand(), true);
            ServerLevel serverWorld = (ServerLevel) world;
            if (player.isShiftKeyDown()) {
                if (!b) {
                    serverPlayer.displayClientMessage(Component.translatable("message.gensokyo_altar.miss_structure"), false);
                    return InteractionResult.SUCCESS_SERVER;
                }
                SimpleContainer inventory = blockEntity.getInventory();
                GensokyoAltarRecipe craft = this.tryCraft(inventory, pos);
                if (craft != null) {
                    List<ServerPlayer> players = serverWorld.players();
                    for (var serverPlayerEntity : players) {
                        serverWorld.sendParticles(serverPlayerEntity, ParticleTypes.ENCHANT, true, false, pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, 10000, 6, 6, 6, 0.5);
                        serverWorld.sendParticles(serverPlayerEntity, ParticleTypes.WITCH, true, false, pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, 10000, 0, 0, 0, 0.5);
                        for (var offset : OFFSETS) {
                            serverWorld.sendParticles(serverPlayerEntity, ParticleTypes.PORTAL, true, false, offset[0] + 0.5, pos.getY(), offset[1] + 0.5, 800, 3, 5, 3, 0.5);
                        }
                    }
                    inventory.clearContent();
                    inventory.setItem(8, craft.getOutput().getItemStack().copy());
                    world.playSound(null, pos, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS);
                    blockEntity.setChanged();
                    SimpleTriggerFactory.create(SimpleTriggerKeys.GENSOKYO_ALTAR_CRAFTING).trigger(serverPlayer);
                } else {
                    serverPlayer.displayClientMessage(Component.translatable("message.gensokyo_altar.miss_recipe"), false);
                }
            } else {
                GensokyoAltarGui gui = new GensokyoAltarGui(serverPlayer, state, world, pos);
                gui.open();
            }
            return InteractionResult.SUCCESS_SERVER;
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public BlockState playerWillDestroy(Level world, BlockPos pos, BlockState state, Player player) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof GensokyoAltarBlockEntity GABE) {
            SimpleContainer inventory = GABE.getInventory();
            for (int i = 0; i < inventory.getContainerSize(); i++) {
                ItemStack stack = inventory.getItem(i);
                ItemEntity itemEntity = new ItemEntity(world, pos.getX(), pos.getY(), pos.getZ(), stack.copy());
                world.addFreshEntity(itemEntity);
            }
        }
        return super.playerWillDestroy(world, pos, state, player);
    }

    protected GensokyoAltarRecipe tryCraft(SimpleContainer inventory, BlockPos pos) {
        List<GensokyoAltarRecipe> matches = GensokyoAltarRecipeType.getInstance().getMatches(List.of(
                new ItemStackWrapper(inventory.getItem(0)),
                new ItemStackWrapper(inventory.getItem(1)),
                new ItemStackWrapper(inventory.getItem(2)),
                new ItemStackWrapper(inventory.getItem(3)),
                new ItemStackWrapper(inventory.getItem(4)),
                new ItemStackWrapper(inventory.getItem(5)),
                new ItemStackWrapper(inventory.getItem(6)),
                new ItemStackWrapper(inventory.getItem(7)),
                new ItemStackWrapper(inventory.getItem(8))
        ));
        if (matches.isEmpty()) {
            return null;
        }
        return matches.getFirst();
    }

    public boolean canUse(Level world, BlockPos center) {
        Block blockType = RDWoodBlocks.SPIRITUAL.strippedLog();
        Block topBlockType = RDWoodBlocks.BLESSED_SPIRITUAL_LOG;

        for (int dy = 0; dy <= 2; dy++) {

            boolean isTopLayer = (dy == 2);

            for (int i = 0; i < OFFSETS.length; i++) {
                int[] offset = OFFSETS[i];

                if (i == 8) continue;

                BlockPos checkPos = center.offset(offset[0], dy, offset[1]);
                Block blockAtPos = world.getBlockState(checkPos).getBlock();

                if (isTopLayer) {
                    if (blockAtPos != topBlockType) {
                        return false;
                    }
                } else {
                    if (blockAtPos != blockType) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, RDBlockEntityTypes.GENSOKYO_ALTAR, GensokyoAltarBlockEntity::tick);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return simpleCodec(GensokyoAltarBlock::new);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new GensokyoAltarBlockEntity(pos, state);
    }

}
