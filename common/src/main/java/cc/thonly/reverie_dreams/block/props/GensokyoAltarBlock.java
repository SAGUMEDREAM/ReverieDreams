package cc.thonly.reverie_dreams.block.props;

import cc.thonly.reverie_dreams.block.entity.GensokyoAltarBlockEntity;
import cc.thonly.reverie_dreams.gui.block.GensokyoAltarGui;
import cc.thonly.reverie_dreams.item.IngredientStack;
import cc.thonly.reverie_dreams.recipe.entry.GensokyoAltarRecipe;
import cc.thonly.reverie_dreams.recipe.type.GensokyoAltarRecipeType;
import cc.thonly.reverie_dreams.registry.content.block.RDWoodBlocks;
import cc.thonly.reverie_dreams.registry.content.block.entity.RDBlockEntityTypes;
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
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Getter
public class GensokyoAltarBlock extends BaseEntityBlock {
    private static final VoxelShape SHAPE = Block.column(16.0, 0.0, 12.0);
    public static final int[][] OFFSETS = {
            {0, -4}, {-3, -3}, {3, -3},
            {-4, 0}, {4, 0},
            {-3, 3}, {3, 3}, {0, 4}, {0, 0}
    };


    public GensokyoAltarBlock(Properties settings) {
        super(settings);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
        if (!world.isClientSide() && player instanceof ServerPlayer serverPlayer && world.getBlockEntity(pos) instanceof GensokyoAltarBlockEntity blockEntity) {
            boolean hasStructure = testStructure(world, pos);
            player.swing(player.getUsedItemHand(), true);
            ServerLevel serverWorld = (ServerLevel) world;
            if (player.isShiftKeyDown()) {
                if (!hasStructure) {
                    serverPlayer.sendSystemMessage(Component.translatable("message.gensokyo_altar.miss_structure"), false);
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
                    consumeRecipe(inventory, craft, serverPlayer);
                    world.playSound(null, pos, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS);
                    blockEntity.setChanged();
                    SimpleTriggerFactory.create(SimpleTriggerKeys.GENSOKYO_ALTAR_CRAFTING).trigger(serverPlayer);
                } else {
                    serverPlayer.sendSystemMessage(Component.translatable("message.gensokyo_altar.miss_recipe"), false);
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

    private static void consumeRecipe(SimpleContainer inventory, GensokyoAltarRecipe recipe, ServerPlayer player) {
        for (int i = 0; i < 8; i++) {
            IngredientStack required = recipe.getSlots().get(i);
            if (required.isEmpty()) {
                continue;
            }
            ItemStack stack = inventory.getItem(i);
            stack.shrink(Math.min(required.getCount(), stack.getCount()));
        }

        IngredientStack requiredCore = recipe.getCore();
        ItemStack coreStack = inventory.getItem(8);
        if (!requiredCore.isEmpty() && !coreStack.isEmpty()) {
            int consumed = Math.min(requiredCore.getCount(), coreStack.getCount());
            if (coreStack.getCount() > consumed) {
                ItemStack leftover = coreStack.copy();
                leftover.setCount(coreStack.getCount() - consumed);
                if (!player.getInventory().add(leftover)) {
                    player.drop(leftover, false);
                }
            }
            coreStack.shrink(consumed);
        }

        // 输出槽与核心槽共用 slot 8：先取出剩余的 core，再放入结果。
        inventory.setItem(8, recipe.getOutput().getLazyStack().copy());
    }

    protected GensokyoAltarRecipe tryCraft(SimpleContainer inventory, BlockPos pos) {
        List<GensokyoAltarRecipe> matches = GensokyoAltarRecipeType.getInstance().getMatches(readIngredients(inventory));
        if (matches.isEmpty()) {
            return null;
        }
        return matches.getFirst();
    }

    public static List<IngredientStack> readIngredients(SimpleContainer inventory) {
        return java.util.stream.IntStream.range(0, 9)
                .mapToObj(slot -> new IngredientStack(inventory.getItem(slot).copy()))
                .toList();
    }

    public boolean testStructure(Level world, BlockPos center) {
        return this.findStructureIssue(world, center) == null;
    }

    public StructureIssue findStructureIssue(Level world, BlockPos center) {
        Block base = RDWoodBlocks.SPIRITUAL_BUNDLE.strippedLog().asBlock();
        Block top = RDWoodBlocks.BLESSED_SPIRITUAL_LOG.asBlock();
        for (int y = 0; y <= 2; y++) {
            Block expected = y == 2 ? top : base;
            for (int i = 0; i < OFFSETS.length - 1; i++) {
                int[] offset = OFFSETS[i];
                if (!world.getBlockState(center.offset(offset[0], y, offset[1])).is(expected)) {
                    return new StructureIssue(offset[0], y, offset[1], expected);
                }
            }
        }
        return null;
    }

    public record StructureIssue(int x, int y, int z, Block expected) { }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, RDBlockEntityTypes.GENSOKYO_ALTAR.value(), GensokyoAltarBlockEntity::tick);
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
