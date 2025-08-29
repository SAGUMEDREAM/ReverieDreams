package cc.thonly.reverie_dreams.block;

import cc.thonly.reverie_dreams.block.entity.GensokyoAltarBlockEntity;
import cc.thonly.reverie_dreams.block.entity.ModBlockEntities;
import cc.thonly.reverie_dreams.gui.recipe.block.GensokyoAltarGui;
import cc.thonly.reverie_dreams.recipe.ItemStackWrapper;
import cc.thonly.reverie_dreams.recipe.entry.GensokyoAltarRecipe;
import cc.thonly.reverie_dreams.recipe.type.GensokyoAltarRecipeType;
import com.mojang.serialization.MapCodec;
import lombok.Getter;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Getter
public class GensokyoAltarBlock extends BlockWithEntity {
    public static final int[][] OFFSETS = {
            {0, -4}, {-3, -3}, {3, -3},
            {-4, 0}, {4, 0},
            {-3, 3}, {3, 3}, {0, 4}, {0, 0}
    };


    public GensokyoAltarBlock(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (!world.isClient && player instanceof ServerPlayerEntity serverPlayer && world.getBlockEntity(pos) instanceof GensokyoAltarBlockEntity blockEntity) {
            boolean b = canUse(world, pos);
            player.swingHand(player.getActiveHand(), true);
            ServerWorld serverWorld = (ServerWorld) world;
            if (player.isSneaking()) {
                if (!b) {
                    serverPlayer.sendMessage(Text.translatable("message.gensokyo_altar.miss_structure"), false);
                    return ActionResult.SUCCESS_SERVER;
                }
                SimpleInventory inventory = blockEntity.getInventory();
                GensokyoAltarRecipe craft = this.tryCraft(inventory, pos);
                if (craft != null) {
                    List<ServerPlayerEntity> players = serverWorld.getPlayers();
                    for (var serverPlayerEntity : players) {
                        serverWorld.spawnParticles(serverPlayerEntity, ParticleTypes.ENCHANT, true, false, pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, 10000, 6, 6, 6, 0.5);
                        serverWorld.spawnParticles(serverPlayerEntity, ParticleTypes.WITCH, true, false, pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, 10000, 0, 0, 0, 0.5);
                        for (var offset : OFFSETS) {
                            serverWorld.spawnParticles(serverPlayerEntity, ParticleTypes.PORTAL, true, false, offset[0] + 0.5, pos.getY(), offset[1] + 0.5, 800, 3, 5, 3, 0.5);
                        }
                    }
                    inventory.clear();
                    inventory.setStack(8, craft.getOutput().getItemStack().copy());
                    world.playSound(null, pos, SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, SoundCategory.BLOCKS);
                    blockEntity.markDirty();
                } else {
                    serverPlayer.sendMessage(Text.translatable("message.gensokyo_altar.miss_recipe"), false);
                }
            } else {
                GensokyoAltarGui gui = new GensokyoAltarGui(serverPlayer, state, world, pos);
                gui.open();
            }
            return ActionResult.SUCCESS_SERVER;
        }
        return ActionResult.SUCCESS;
    }

    @Override
    public BlockState onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof GensokyoAltarBlockEntity GABE) {
            SimpleInventory inventory = GABE.getInventory();
            for (int i = 0; i < inventory.size(); i++) {
                ItemStack stack = inventory.getStack(i);
                ItemEntity itemEntity = new ItemEntity(world, pos.getX(), pos.getY(), pos.getZ(), stack.copy());
                world.spawnEntity(itemEntity);
            }
        }
        return super.onBreak(world, pos, state, player);
    }

    protected GensokyoAltarRecipe tryCraft(SimpleInventory inventory, BlockPos pos) {
        List<GensokyoAltarRecipe> matches = GensokyoAltarRecipeType.getInstance().getMatches(List.of(
                new ItemStackWrapper(inventory.getStack(0)),
                new ItemStackWrapper(inventory.getStack(1)),
                new ItemStackWrapper(inventory.getStack(2)),
                new ItemStackWrapper(inventory.getStack(3)),
                new ItemStackWrapper(inventory.getStack(4)),
                new ItemStackWrapper(inventory.getStack(5)),
                new ItemStackWrapper(inventory.getStack(6)),
                new ItemStackWrapper(inventory.getStack(7)),
                new ItemStackWrapper(inventory.getStack(8))
        ));
        if (matches.isEmpty()) {
            return null;
        }
        return matches.getFirst();
    }

    public boolean canUse(World world, BlockPos center) {
        Block blockType = ModBlocks.SPIRITUAL.strippedLog();

        for (int dy = 0; dy <= 2; dy++) {
            for (int i = 0; i < OFFSETS.length; i++) {
                int[] offset = OFFSETS[i];
                if (i == 8) continue;
                BlockPos checkPos = center.add(offset[0], dy, offset[1]);
                Block blockAtPos = world.getBlockState(checkPos).getBlock();
                if (blockAtPos != blockType) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return validateTicker(type, ModBlockEntities.GENSOKYO_ALTAR_BLOCK_ENTITY, GensokyoAltarBlockEntity::tick);
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return createCodec(GensokyoAltarBlock::new);
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new GensokyoAltarBlockEntity(pos, state);
    }

}
