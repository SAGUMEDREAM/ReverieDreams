package cc.thonly.reverie_dreams.item.prop;

import cc.thonly.reverie_dreams.advancement.SimpleTrigger;
import cc.thonly.reverie_dreams.registry.content.component.RDDataComponentTypes;
import cc.thonly.reverie_dreams.util.advancements.SimpleTriggerKeys;
import cc.thonly.reverie_dreams.util.item.ItemUtils;
import com.mojang.authlib.GameProfile;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Objects;

public class Cheque extends Item {

    public Cheque(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        ItemStack cheque = player.getItemInHand(hand);
        boolean creative = player.isCreative();
        String chequePlayerId = cheque.getOrDefault(RDDataComponentTypes.CHEQUE_PLAYER_ID.get(), "");
        String playerId = player.getGameProfile().name();
        boolean isSelf = chequePlayerId.isEmpty() || Objects.equals(playerId, chequePlayerId);

        if (player.isShiftKeyDown()) {
            if (!creative && !isSelf) {
                return InteractionResult.FAIL;
            }

            int playerMoney = ItemUtils.getPlayerCoinValue(player);

            if (playerMoney < 10) {
                return InteractionResult.FAIL;
            }

            int currentAmount = cheque.getOrDefault(RDDataComponentTypes.CHEQUE_AMOUNT.get(), 0);

            int newAmount = currentAmount + playerMoney;

            if (!creative) {
                boolean removed = ItemUtils.removeCoins(player, playerMoney);
                if (!removed) {
                    return InteractionResult.FAIL;
                }
            }

            cheque.set(RDDataComponentTypes.CHEQUE_PLAYER_ID.get(), playerId);
            cheque.set(RDDataComponentTypes.CHEQUE_NAME.get(), player.getDisplayName());
            cheque.set(RDDataComponentTypes.CHEQUE_AMOUNT.get(), newAmount);
            player.sendSystemMessage(
                    Component.literal("§e+%s".formatted(playerMoney))
            );
            if (newAmount >= 1500) {
                SimpleTrigger.trigger((ServerPlayer) player, SimpleTriggerKeys.ASKING_FOR_MONEY);
            }

            return InteractionResult.SUCCESS_SERVER;
        }

        int amount = cheque.getOrDefault(RDDataComponentTypes.CHEQUE_AMOUNT.get(), 0);
        if (amount <= 0) {
            return InteractionResult.FAIL;
        }

        if (creative) {
            return InteractionResult.FAIL;
        }

        List<ItemStack> itemStacks = ItemUtils.calculateCoins(amount);
        for (ItemStack stack : itemStacks) {
            player.getInventory().placeItemBackInInventory(stack);
        }

        cheque.remove(RDDataComponentTypes.CHEQUE_AMOUNT.get());
        cheque.remove(RDDataComponentTypes.CHEQUE_PLAYER_ID.get());
        cheque.remove(RDDataComponentTypes.CHEQUE_NAME.get());

        return InteractionResult.SUCCESS_SERVER;
    }
}
