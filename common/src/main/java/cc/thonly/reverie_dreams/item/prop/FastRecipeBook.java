package cc.thonly.reverie_dreams.item.prop;

import cc.thonly.reverie_dreams.block.entity.KitchenwareBlockEntity;
import cc.thonly.reverie_dreams.gui.item.FastRecipeBookGui;
import cc.thonly.reverie_dreams.item.IngredientStack;
import cc.thonly.reverie_dreams.recipe.entry.KitchenRecipe;
import cc.thonly.reverie_dreams.registry.content.component.RDDataComponentTypes;
import cc.thonly.reverie_dreams.registry.tag.RDBlockTags;
import cc.thonly.reverie_dreams.util.entity.PlayerHelper;
import cc.thonly.reverie_dreams.util.sound.SoundEventPlayUtils;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.Objects;

public class FastRecipeBook extends Item {
    public FastRecipeBook(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide()) {
            ItemStack itemStack = player.getItemInHand(hand);
            SimpleGui gui = new FastRecipeBookGui((ServerPlayer) player, itemStack);
            gui.open();
            return InteractionResult.SUCCESS_SERVER;
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (context.getLevel().isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        this.useOnKitchenBlock(context);
        return super.useOn(context);
    }

    public void useOnKitchenBlock(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return;
        }
        Level level = context.getLevel();
        if (level.isClientSide()) {
            return;
        }
        BlockPos blockPos = context.getClickedPos();
        BlockState blockState = level.getBlockState(blockPos);
        if (!blockState.is(RDBlockTags.KITCHENWARE)) {
            return;
        }
        BlockEntity be = level.getBlockEntity(blockPos);
        if (!(be instanceof KitchenwareBlockEntity blockEntity)) {
            return;
        }
        if (blockEntity.getTickLeft() > 1.0) {
            return;
        }
        InteractionHand hand = context.getHand();
        ItemStack stack = player.getItemInHand(hand);
        KitchenRecipe.IdEntry recipeIdEntry = stack.get(RDDataComponentTypes.RECIPE_MEMORY.value());
        if (recipeIdEntry == null) {
            return;
        }
        recipeIdEntry.map((key, recipe) -> {
            if (!Objects.equals(blockEntity.getTypeInstance(), recipe.getTypeInstance())) {
                return;
            }
            SimpleContainer inventory = blockEntity.getInventory();
            if (!blockEntity.testIngredients(recipe, List.of(
                    IngredientStack.of(inventory.getItem(0)),
                    IngredientStack.of(inventory.getItem(1)),
                    IngredientStack.of(inventory.getItem(2)),
                    IngredientStack.of(inventory.getItem(3)),
                    IngredientStack.of(inventory.getItem(4))
            ))) {
                return;
            }
            blockEntity.consume(recipe.getIngredients());
            blockEntity.setOutput(recipe.getOutput().copy(), recipe.getCostTime() * 20.0 + 20 * 0.25 * recipe.getIngredients().size());
            List<ServerPlayer> nearbyPlayers = PlayerHelper.getNearbyPlayers((ServerLevel) level, blockEntity.getBlockPos(), 16);
            for (ServerPlayer serverPlayer : nearbyPlayers) {
                SoundEventPlayUtils.playUISound(serverPlayer, SoundEvents.NOTE_BLOCK_PLING.value(), 1.0f, 1.0f);
            }
        });
        player.swing(hand);
    }
}
